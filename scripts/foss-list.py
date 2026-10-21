#!/usr/bin/env python3
"""Build a third-party directory from jars that jcsa_dist ships."""

from __future__ import annotations

import argparse
import hashlib
import re
import shutil
import sys
import urllib.error
import urllib.request
import zipfile
from pathlib import Path
from xml.etree import ElementTree

SPDX_RULES: list[tuple[str, list[str]]] = [
    ("Apache-2.0", [
        r"apache-2\.0",
        r"apache(?: software)? license,?(?: version | v)?2(?:\.0)?",
        r"apache 2(?:\.0)?",
        r"licenses/license-2\.0",
        r"opensource\.org/licenses/apache-2\.0",
    ]),
    ("LGPL-2.1", [
        r"lgpl[- ]?2\.1",
        r"lesser general public license(?: version | v)?2\.1",
        r"lgpl\.html",
    ]),
    ("EPL-2.0", [
        r"epl[- ]?(?:v|-)2",
        r"epl-v20",
        r"eclipse public license(?: \(epl\))?[- ]*(?:v(?:ersion )?|-)2",
    ]),
    ("EPL-1.0", [
        r"epl[- ]?(?:v|-)1",
        r"epl-v10",
        r"eclipse public license",
    ]),
    ("BSD-3-Clause", [
        r"bsd-3-clause",
        r"bsd 3-clause",
        r"bsd3",
        r"the bsd3 license",
        r"opensource\.org/licenses/bsd-3-clause",
        r"asm\.ow2\.io/license",
    ]),
    ("MIT", [
        r"the mit license",
        r"(?:^| )mit(?: |$)",
        r"mit-license",
        r"opensource\.org/licenses/mit",
    ]),
]

SPDX_TEXT_URL = {
    "Apache-2.0": "https://www.apache.org/licenses/LICENSE-2.0.txt",
    "LGPL-2.1": "https://www.gnu.org/licenses/lgpl-2.1.txt",
    "MIT": "https://spdx.org/licenses/MIT.txt",
    "EPL-1.0": "https://spdx.org/licenses/EPL-1.0.txt",
    "EPL-2.0": "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt",
    "BSD-3-Clause": "https://spdx.org/licenses/BSD-3-Clause.txt",
}

USER_AGENT = "jcsa-foss-list"


def local_tag(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def xml_child(el: ElementTree.Element, name: str) -> ElementTree.Element | None:
    for child in el:
        if local_tag(child.tag) == name:
            return child
    return None


def xml_text(el: ElementTree.Element | None) -> str:
    if el is None or not el.text:
        return ""
    return el.text.strip()


def parse_xml(text: str) -> ElementTree.Element | None:
    try:
        return ElementTree.fromstring(text)
    except ElementTree.ParseError:
        return None


def parse_pom_properties(text: str) -> dict[str, str]:
    props: dict[str, str] = {}
    for raw in text.splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def pom_licenses(root: ElementTree.Element) -> list[tuple[str, str]]:
    licenses_el = xml_child(root, "licenses")
    if licenses_el is None:
        return []
    licenses: list[tuple[str, str]] = []
    for el in licenses_el:
        if local_tag(el.tag) != "license":
            continue
        name = xml_text(xml_child(el, "name"))
        url = xml_text(xml_child(el, "url"))
        if name or url:
            licenses.append((name, url))
    return licenses


def pom_parent(root: ElementTree.Element) -> tuple[str, str, str] | None:
    parent = xml_child(root, "parent")
    if parent is None:
        return None
    group = xml_text(xml_child(parent, "groupId"))
    artifact = xml_text(xml_child(parent, "artifactId"))
    version = xml_text(xml_child(parent, "version"))
    if not (group and artifact and version):
        return None
    return group, artifact, version


def pom_gav(root: ElementTree.Element) -> tuple[str, str, str]:
    parent = pom_parent(root)
    group = xml_text(xml_child(root, "groupId")) or (parent[0] if parent else "")
    artifact = xml_text(xml_child(root, "artifactId"))
    version = xml_text(xml_child(root, "version")) or (parent[2] if parent else "")
    return group, artifact, version


def manifest_license(text: str) -> str:
    folded: list[str] = []
    for line in text.splitlines():
        if line.startswith(" ") and folded:
            folded[-1] += line[1:]
        else:
            folded.append(line)
    for line in folded:
        if line.lower().startswith("bundle-license:"):
            return line.split(":", 1)[1].strip()
    return ""


def read_entry(zf: zipfile.ZipFile, name: str) -> str:
    with zf.open(name) as handle:
        return handle.read().decode("utf-8", errors="replace")


def unique_licenses(items: list[tuple[str, str]]) -> list[tuple[str, str]]:
    seen: set[tuple[str, str]] = set()
    out: list[tuple[str, str]] = []
    for item in items:
        if item in seen:
            continue
        seen.add(item)
        out.append(item)
    return out


def expand_declared(name: str, url: str) -> tuple[str, str]:
    if ";link=" in name and not url:
        ident, link = name.split(";link=", 1)
        return ident.strip(), link.strip()
    if name.startswith("http://") or name.startswith("https://"):
        return "", name
    return name, url


def skip_declared(name: str, url: str) -> bool:
    return name.lower().startswith("see ") and not url


def classify_license(name: str, url: str) -> str | None:
    blob = f"{name} {url}".lower()
    blob = blob.replace("%20", " ")
    for spdx, patterns in SPDX_RULES:
        if any(re.search(pattern, blob) for pattern in patterns):
            return spdx
    return None


class MavenRepo:
    def __init__(self, root: Path) -> None:
        self.root = root
        self._by_pom_name: dict[str, Path] | None = None

    def pom_file(self, group: str, artifact: str, version: str) -> Path:
        return (self.root / group.replace(".", "/") / artifact / version
                / f"{artifact}-{version}.pom")

    def find_pom_for_jar(self, jar_name: str) -> Path | None:
        stem = jar_name[:-4] if jar_name.endswith(".jar") else jar_name
        if self._by_pom_name is None:
            index: dict[str, Path] = {}
            if self.root.is_dir():
                for path in self.root.rglob("*.pom"):
                    index.setdefault(path.name, path)
            self._by_pom_name = index
        return self._by_pom_name.get(f"{stem}.pom")

    def licenses_for(self, group: str, artifact: str, version: str) -> list[tuple[str, str]]:
        licenses: list[tuple[str, str]] = []
        seen: set[tuple[str, str, str]] = set()
        current: tuple[str, str, str] | None = (group, artifact, version)
        while current and current not in seen:
            seen.add(current)
            path = self.pom_file(*current)
            if not path.is_file():
                break
            root = parse_xml(path.read_text(encoding="utf-8", errors="replace"))
            if root is None:
                break
            licenses.extend(pom_licenses(root))
            if licenses:
                return unique_licenses(licenses)
            current = pom_parent(root)
        return unique_licenses(licenses)


def inspect_jar(path: Path, repo: MavenRepo) -> dict[str, object]:
    group = ""
    artifact = ""
    version = ""
    licenses: list[tuple[str, str]] = []
    parent: tuple[str, str, str] | None = None
    with zipfile.ZipFile(path) as zf:
        names = zf.namelist()
        props_names = sorted(
            n for n in names
            if n.startswith("META-INF/maven/") and n.endswith("pom.properties"))
        for props_name in props_names:
            props = parse_pom_properties(read_entry(zf, props_name))
            if props.get("artifactId"):
                group = props.get("groupId", group)
                artifact = props.get("artifactId", artifact)
                version = props.get("version", version)
            pom_name = props_name[: -len("pom.properties")] + "pom.xml"
            if pom_name in names:
                root = parse_xml(read_entry(zf, pom_name))
                if root is not None:
                    licenses.extend(pom_licenses(root))
                    if parent is None:
                        parent = pom_parent(root)
        if not licenses and "META-INF/MANIFEST.MF" in names:
            bundle = manifest_license(read_entry(zf, "META-INF/MANIFEST.MF"))
            if bundle:
                licenses.append((bundle, ""))
        if not licenses:
            for name in names:
                if Path(name).name.lower() in {"license", "license.txt", "license.md"}:
                    licenses.append((f"see {name}", ""))
                    break

    if not (group and artifact and version):
        pom_path = repo.find_pom_for_jar(path.name)
        if pom_path is not None:
            root = parse_xml(pom_path.read_text(encoding="utf-8", errors="replace"))
            if root is not None:
                group, artifact, version = pom_gav(root)
                licenses.extend(pom_licenses(root))
                if parent is None:
                    parent = pom_parent(root)

    if not licenses and group and artifact and version:
        licenses.extend(repo.licenses_for(group, artifact, version))
    if not licenses and parent:
        licenses.extend(repo.licenses_for(*parent))

    if not artifact:
        artifact = path.stem
    return {
        "jar": path.name,
        "group": group,
        "artifact": artifact,
        "version": version,
        "licenses": unique_licenses(licenses),
    }


def collect(dirs: list[Path], repo: MavenRepo) -> list[dict[str, object]]:
    by_key: dict[str, dict[str, object]] = {}
    for directory in dirs:
        if not directory.is_dir():
            continue
        for jar in sorted(directory.glob("*.jar")):
            info = inspect_jar(jar, repo)
            key = f"{info['group']}:{info['artifact']}:{info['version']}:{info['jar']}"
            by_key[key] = info
    return [by_key[k] for k in sorted(by_key)]


def component_license_ids(info: dict[str, object]) -> list[str]:
    ids: list[str] = []
    seen: set[str] = set()
    for raw_name, raw_url in info["licenses"]:
        name, url = expand_declared(str(raw_name), str(raw_url))
        if skip_declared(name, url):
            continue
        ident = classify_license(name, url)
        if ident is None:
            continue
        if ident not in seen:
            seen.add(ident)
            ids.append(ident)
    return ids


def needed_license_ids(components: list[dict[str, object]]) -> list[str]:
    ids: set[str] = set()
    for info in components:
        ids.update(component_license_ids(info))
    return sorted(ids)


def github_raw_url(url: str) -> str:
    match = re.match(
        r"https?://github\.com/([^/]+)/([^/]+)/blob/([^/]+)/(.*)", url)
    if match is None:
        return url
    user, repo, ref, path = match.groups()
    return f"https://raw.githubusercontent.com/{user}/{repo}/{ref}/{path}"


def download_text(url: str) -> str:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        data = response.read()
    return data.decode("utf-8", errors="replace")


def write_license_files(license_dir: Path, ids: list[str]) -> dict[str, str]:
    license_dir.mkdir(parents=True, exist_ok=True)
    files: dict[str, str] = {}
    hash_to_spdx: dict[str, str] = {}
    for spdx in ids:
        url = SPDX_TEXT_URL.get(spdx)
        if url is None:
            raise RuntimeError(f"no download URL for license {spdx}")
        text = download_text(github_raw_url(url))
        digest = hashlib.sha256(text.encode("utf-8")).hexdigest()
        if digest in hash_to_spdx:
            existing = hash_to_spdx[digest]
            files[spdx] = files[existing]
            continue
        filename = f"{spdx}.txt"
        (license_dir / filename).write_text(text, encoding="utf-8")
        files[spdx] = f"licenses/{filename}"
        hash_to_spdx[digest] = spdx
    return files


def format_component(info: dict[str, object], license_files: dict[str, str]) -> str:
    group = str(info["group"])
    artifact = str(info["artifact"])
    version = str(info["version"])
    jar_name = str(info["jar"])
    if group and version:
        heading = f"{group}:{artifact}:{version}"
    elif version:
        heading = f"{artifact}:{version}"
    else:
        heading = artifact
    lines = [heading, f"  Jar: {jar_name}"]
    if group and version:
        lines.append(
            "  Download: https://repo1.maven.org/maven2/"
            f"{group.replace('.', '/')}/{artifact}/{version}/{jar_name}")
    ids = component_license_ids(info)
    if not ids:
        lines.append("  License: UNKNOWN")
    for ident in ids:
        lines.append(f"  License: {ident} ({license_files[ident]})")
    return "\n".join(lines)


def render(components: list[dict[str, object]], license_files: dict[str, str]) -> str:
    body = "\n\n".join(format_component(c, license_files) for c in components)
    return (
        "Third-party components distributed with JCSA\n"
        "\n"
        "This list is generated from the jars copied by jcsa_dist\n"
        "(lib/shared, lib/stack, lib/esar).\n"
        "License texts are stored once under licenses/.\n"
        "\n"
        f"{body}\n"
    )


def write_third_party_dir(out_dir: Path,
                          components: list[dict[str, object]]) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    license_dir = out_dir / "licenses"
    if license_dir.exists():
        shutil.rmtree(license_dir)
    third_party_list = out_dir / "THIRD-PARTY.txt"
    if third_party_list.exists():
        third_party_list.unlink()
    ids = needed_license_ids(components)
    license_files = write_license_files(license_dir, ids)
    third_party_list.write_text(render(components, license_files), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Write third-party directory from distributed dependency jars.")
    parser.add_argument("--out", type=Path, required=True,
                        help="Output directory (list + downloaded licenses).")
    parser.add_argument("--dir", type=Path, action="append", required=True)
    parser.add_argument(
        "--m2",
        type=Path,
        default=Path.home() / ".m2" / "repository",
        help="Local Maven repository used to resolve POM licenses.")
    args = parser.parse_args()

    repo = MavenRepo(args.m2)
    components = collect(args.dir, repo)
    missing = [c for c in components if not component_license_ids(c)]
    try:
        write_third_party_dir(args.out, components)
    except (urllib.error.URLError, RuntimeError) as exc:
        print(f"license download failed: {exc}", file=sys.stderr)
        return 1
    print(f"wrote {args.out} ({len(components)} components, "
          f"{len(needed_license_ids(components))} licenses)", file=sys.stderr)
    if missing:
        for info in missing:
            print(f"unknown license: {info['jar']}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
