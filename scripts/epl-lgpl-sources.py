#!/usr/bin/env python3
"""Zip the Maven source jars for EPL and LGPL dependencies that jcsa_dist ships."""

from __future__ import annotations

import argparse
import importlib.util
import sys
import urllib.error
import urllib.request
import zipfile
from pathlib import Path

SOURCE_LICENSES = ("EPL-1.0", "EPL-2.0", "LGPL-2.1")
MAVEN_CENTRAL = "https://repo1.maven.org/maven2"
USER_AGENT = "jcsa-epl-lgpl-sources"


def load_foss_list():
    path = Path(__file__).with_name("foss-list.py")
    spec = importlib.util.spec_from_file_location("foss_list", path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load {path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def source_licenses(foss, info: dict[str, object]) -> list[str]:
    wanted = set(SOURCE_LICENSES)
    return [ident for ident in foss.component_license_ids(info) if ident in wanted]


def maven_relative(group: str, artifact: str, version: str, filename: str) -> str:
    return f"{group.replace('.', '/')}/{artifact}/{version}/{filename}"


def source_jar_name(artifact: str, version: str) -> str:
    return f"{artifact}-{version}-sources.jar"


def local_source_jar(m2: Path, group: str, artifact: str, version: str) -> Path | None:
    path = m2 / maven_relative(group, artifact, version, source_jar_name(artifact, version))
    if path.is_file():
        return path
    return None


def download(url: str) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=120) as response:
        return response.read()


def fetch_source_jar(m2: Path, group: str, artifact: str, version: str) -> tuple[str, bytes]:
    filename = source_jar_name(artifact, version)
    local = local_source_jar(m2, group, artifact, version)
    if local is not None:
        return filename, local.read_bytes()
    url = f"{MAVEN_CENTRAL}/{maven_relative(group, artifact, version, filename)}"
    try:
        return filename, download(url)
    except urllib.error.HTTPError as exc:
        raise RuntimeError(f"no source jar for {group}:{artifact}:{version} ({url}): {exc}") from exc
    except urllib.error.URLError as exc:
        raise RuntimeError(f"could not download {url}: {exc}") from exc


def index_text(entries: list[tuple[dict[str, object], list[str], str]]) -> str:
    lines = [
        "Source jars for the EPL and LGPL components shipped by jcsa_dist.",
        "Each file is the Maven -sources jar for the same group, artifact, and version",
        "as the binary jar in the distribution.",
        "",
    ]
    for info, licenses, filename in entries:
        lines.append(f"{info['group']}:{info['artifact']}:{info['version']}")
        lines.append(f"  Binary: {info['jar']}")
        lines.append(f"  Sources: {filename}")
        for ident in licenses:
            lines.append(f"  License: {ident}")
        lines.append("")
    return "\n".join(lines).rstrip() + "\n"


def write_zip(out: Path,
              m2: Path,
              entries: list[tuple[dict[str, object], list[str]]]) -> None:
    prefix = out.stem
    packed: list[tuple[dict[str, object], list[str], str]] = []
    out.parent.mkdir(parents=True, exist_ok=True)
    partial = out.with_suffix(out.suffix + ".partial")
    if partial.exists():
        partial.unlink()
    try:
        with zipfile.ZipFile(partial, "w", compression=zipfile.ZIP_DEFLATED) as zf:
            for info, licenses in entries:
                group = str(info["group"])
                artifact = str(info["artifact"])
                version = str(info["version"])
                filename, data = fetch_source_jar(m2, group, artifact, version)
                member = f"{prefix}/{maven_relative(group, artifact, version, filename)}"
                zf.writestr(member, data)
                packed.append((info, licenses, filename))
                print(f"packed {group}:{artifact}:{version}", file=sys.stderr)
            zf.writestr(f"{prefix}/SOURCES.txt", index_text(packed))
        partial.replace(out)
    except Exception:
        if partial.exists():
            partial.unlink()
        raise


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Zip Maven source jars for shipped EPL and LGPL dependencies.")
    parser.add_argument("--out", type=Path, required=True, help="Zip file to write.")
    parser.add_argument("--dir", type=Path, action="append", required=True)
    parser.add_argument(
        "--m2",
        type=Path,
        default=Path.home() / ".m2" / "repository",
        help="Local Maven repository checked before Maven Central.")
    args = parser.parse_args()

    foss = load_foss_list()
    components = foss.collect(args.dir, foss.MavenRepo(args.m2))
    selected: list[tuple[dict[str, object], list[str]]] = []
    for info in components:
        licenses = source_licenses(foss, info)
        if not licenses:
            continue
        if not (info["group"] and info["artifact"] and info["version"]):
            print(f"missing coordinates for {info['jar']}", file=sys.stderr)
            return 1
        selected.append((info, licenses))
    if not selected:
        print("no EPL or LGPL jars found", file=sys.stderr)
        return 1
    try:
        write_zip(args.out, args.m2, selected)
    except RuntimeError as exc:
        print(exc, file=sys.stderr)
        return 1
    print(f"wrote {args.out} ({len(selected)} source jars)", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
