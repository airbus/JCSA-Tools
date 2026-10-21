#!/usr/bin/env python3
"""List commit subjects per src/ directory for a revision or range."""

from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path


def git(root: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", "-C", str(root), *args],
        check=False,
        text=True,
        capture_output=True,
    )
    if result.returncode != 0:
        sys.stderr.write(result.stderr)
        raise SystemExit(result.returncode)
    return result.stdout


def repo_root() -> Path:
    result = subprocess.run(
        ["git", "rev-parse", "--show-toplevel"],
        check=False,
        text=True,
        capture_output=True,
    )
    if result.returncode != 0:
        sys.stderr.write(result.stderr)
        raise SystemExit(result.returncode)
    return Path(result.stdout.strip())


def src_dirs(root: Path) -> list[str]:
    src = root / "src"
    if not src.is_dir():
        return []
    return sorted(
        path.relative_to(root).as_posix()
        for path in src.iterdir()
        if path.is_dir()
    )


def tags_by_commit(root: Path) -> dict[str, list[str]]:
    result = subprocess.run(
        ["git", "-C", str(root), "show-ref", "--tags", "--dereference"],
        check=False,
        text=True,
        capture_output=True,
    )
    if result.returncode == 1 and not result.stdout:
        return {}
    if result.returncode != 0:
        sys.stderr.write(result.stderr)
        raise SystemExit(result.returncode)
    peeled: set[str] = set()
    found: dict[str, list[str]] = {}
    lightweight: list[tuple[str, str]] = []
    for line in result.stdout.splitlines():
        if not line.strip():
            continue
        sha, ref = line.split(" ", 1)
        if ref.endswith("^{}"):
            name = ref.removeprefix("refs/tags/")[:-3]
            peeled.add(name)
            found.setdefault(sha, []).append(name)
        else:
            lightweight.append((sha, ref.removeprefix("refs/tags/")))
    for sha, name in lightweight:
        if name not in peeled:
            found.setdefault(sha, []).append(name)
    for names in found.values():
        names.sort()
    return found


def format_commit(short: str, subject: str, tags: list[str]) -> str:
    if not tags:
        return f"{short} {subject}"
    decorated = ", ".join(f"tag: {name}" for name in tags)
    return f"{short} ({decorated}) {subject}"


def subjects(root: Path, revision: str, directory: str, tags: dict[str, list[str]]) -> list[str]:
    log = git(
        root,
        "log",
        r"--format=%H%x1f%h%x1f%s",
        revision,
        "--",
        directory,
    )
    lines: list[str] = []
    for record in log.split("\n"):
        if not record.strip():
            continue
        full, short, subject = record.split("\x1f", 2)
        lines.append(format_commit(short, subject, tags.get(full, [])))
    return lines


def verify_range(root: Path, spec: str) -> None:
    if "..." in spec:
        left, right = spec.split("...", 1)
    else:
        left, right = spec.split("..", 1)
    for endpoint in (left, right):
        if endpoint:
            git(root, "rev-parse", "--verify", f"{endpoint}^{{commit}}")


def working_tree_paths(root: Path, directory: str) -> list[str]:
    diff = git(root, "diff", "--name-only", "HEAD", "--", directory)
    untracked = git(
        root,
        "ls-files",
        "--others",
        "--exclude-standard",
        "--",
        directory,
    )
    paths = [line for line in diff.splitlines() if line.strip()]
    paths.extend(line for line in untracked.splitlines() if line.strip())
    return paths


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "rev",
        help="commit, tag, HEAD, or a range such as id..HEAD. "
             "A single revision lists changes from there to HEAD. "
             "HEAD lists uncommitted changes under src/",
    )
    args = parser.parse_args()
    root = repo_root()
    ranged = ".." in args.rev
    if ranged:
        verify_range(root, args.rev)
        revision = args.rev
        at_head = False
    else:
        rev = git(root, "rev-parse", "--verify", f"{args.rev}^{{commit}}").strip()
        head = git(root, "rev-parse", "--verify", "HEAD").strip()
        at_head = rev == head
        revision = f"{args.rev}..HEAD"
    tags = {} if at_head else tags_by_commit(root)
    printed = False
    for directory in src_dirs(root):
        if at_head:
            lines = working_tree_paths(root, directory)
        else:
            lines = subjects(root, revision, directory, tags)
        if not lines:
            continue
        if printed:
            print()
        print(directory)
        for line in lines:
            print(line)
        printed = True
    if not printed:
        if ranged:
            print(f"no changes in {args.rev}")
        else:
            print(f"no changes since {args.rev}")


if __name__ == "__main__":
    main()
