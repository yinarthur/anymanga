#!/usr/bin/env python3
"""Generate Play-Store-compliant sources templates from Keiyoushi index."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import sys
import time
from collections import Counter
from dataclasses import dataclass
from typing import Any, Iterable, Optional
from urllib.error import HTTPError, URLError
from urllib.parse import urlparse, urlunparse
from urllib.request import Request, urlopen

INDEX_URLS = [
    "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json",
    "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.json",
    "https://raw.githubusercontent.com/keiyoushi/extensions/refs/heads/repo/index.json",
]

DEFAULT_CACHE_DIR = ".cache"


@dataclass
class FetchResult:
    url: str
    status: int
    etag: Optional[str]
    payload: Optional[bytes]


class FetchError(Exception):
    pass


def read_json(path: str) -> dict[str, Any]:
    try:
        with open(path, "r", encoding="utf-8") as handle:
            return json.load(handle)
    except FileNotFoundError:
        return {}


def write_json(path: str, payload: dict[str, Any]) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, indent=2, sort_keys=True)


def load_etag_cache(path: str) -> dict[str, str]:
    data = read_json(path)
    if isinstance(data, dict):
        return {str(key): str(value) for key, value in data.items()}
    return {}


def save_etag_cache(path: str, data: dict[str, str]) -> None:
    write_json(path, data)


def fetch_index(urls: Iterable[str], etag_cache: dict[str, str]) -> FetchResult:
    last_error: Optional[Exception] = None
    for url in urls:
        headers = {"User-Agent": "templates-generator/1.0"}
        if url in etag_cache:
            headers["If-None-Match"] = etag_cache[url]
        request = Request(url, headers=headers)
        try:
            with urlopen(request, timeout=30) as response:
                payload = response.read()
                response_etag = response.headers.get("ETag")
                return FetchResult(url=url, status=response.status, etag=response_etag, payload=payload)
        except HTTPError as exc:
            if exc.code == 304:
                return FetchResult(url=url, status=304, etag=etag_cache.get(url), payload=None)
            last_error = exc
        except URLError as exc:
            last_error = exc
    raise FetchError(f"Failed to fetch index: {last_error}")


def json_load(payload: bytes) -> Any:
    return json.loads(payload.decode("utf-8"))


def iter_entries(data: Any) -> Iterable[dict[str, Any]]:
    if isinstance(data, list):
        for item in data:
            if isinstance(item, dict):
                yield item
        return
    if isinstance(data, dict):
        for key in ("extensions", "entries", "sources", "data", "items"):
            value = data.get(key)
            if isinstance(value, list):
                for item in value:
                    if isinstance(item, dict):
                        yield item
                return
        for value in data.values():
            if isinstance(value, list):
                for item in value:
                    if isinstance(item, dict):
                        yield item
                return
    return


def normalize_domain(hostname: str) -> Optional[str]:
    cleaned = hostname.strip().lower().rstrip(".")
    if cleaned.startswith("www."):
        cleaned = cleaned[4:]
    if not cleaned:
        return None
    try:
        return cleaned.encode("idna").decode("ascii")
    except UnicodeError:
        return None


def normalize_url(value: str) -> tuple[Optional[str], Optional[str]]:
    raw = value.strip()
    if not raw:
        return None, None
    if not raw.startswith(("http://", "https://")):
        raw = f"https://{raw}"
    parsed = urlparse(raw)
    hostname = parsed.hostname
    if not hostname:
        return None, None
    domain = normalize_domain(hostname)
    if not domain:
        return None, None
    netloc = parsed.netloc
    if parsed.port:
        netloc = f"{hostname}:{parsed.port}"
    base = urlunparse((parsed.scheme, netloc, parsed.path or "", "", "", ""))
    return base, domain


def find_base_url(entry: dict[str, Any]) -> Optional[str]:
    for key in (
        "baseUrl",
        "baseurl",
        "baseURL",
        "website",
        "site",
        "url",
        "domain",
        "sourceUrl",
        "sourceURL",
    ):
        value = entry.get(key)
        if isinstance(value, str) and value.strip():
            return value
    if isinstance(entry.get("sources"), list):
        for source in entry["sources"]:
            if isinstance(source, dict):
                value = source.get("baseUrl") or source.get("website")
                if isinstance(value, str) and value.strip():
                    return value
    return None


# Known Madara keywords and domains for heuristics
MADARA_KEYWORDS = {
    "madara", "azora", "manga-spark", "mangaspark", "mangapro", "manga-pro",
    "mangasoul", "manga-soul", "areamanga", "area-manga", "umimanga",
    "hadess", "lekmanga", "link-manga", "mangalionz", "mangaspark",
    "manga-starz", "mangaswat", "mangatales", "mangatuk", "manhuarmtl",
    "seraphicdeviltry", "ravensscans", "lavatoons", "thunderscans",
    "falconmanga", "manga-swat", "akuma", "gmanga", "manga-planet"
}

# Mapping multisrc package IDs to engine types
MULTISRC_MAP = {
    "madara": "MADARA",
    "mangastream": "MANGASTREAM",
    "mangathemesia": "MANGATHEMESIA",
    "wpcomics": "WPCOMICS",
    "foolslide": "FOOLSLIDE",
    "heancms": "HEANCMS",
    "fmreader": "FMREADER",
    "mmrcms": "MMRCMS",
    "genkan": "GENKAN",
    "mangareader": "MANGAREADER",
}


def infer_engine_type(entry: dict[str, Any]) -> str:
    # 1. Check for explicit metadata
    for key in ("engineType", "engine", "sourceType", "type"):
        value = entry.get(key)
        if isinstance(value, str) and value.strip():
            return value.strip().upper()

    # 2. Heuristics based on package name, source name, or baseUrl
    pkg = str(entry.get("pkg", "")).lower()
    name = str(entry.get("name", "")).lower()
    
    # 2a. Check Multisrc pattern (e.g., eu.kanade.tachiyomi.extension.all.madara)
    for ms_key, engine_val in MULTISRC_MAP.items():
        if f".all.{ms_key}" in pkg or f".{ms_key}." in pkg:
            return engine_val

    # 2b. Check keywords in all text
    all_text = pkg + " " + name
    if isinstance(entry.get("sources"), list):
        for s in entry["sources"]:
            if isinstance(s, dict):
                all_text += " " + str(s.get("name", "")).lower()
                all_text += " " + str(s.get("baseUrl", "")).lower()

    if any(kw in all_text for kw in MADARA_KEYWORDS):
        return "MADARA"

    # Default fallback
    return "GENERIC"


def parse_epoch(value: Any, fallback_ms: int) -> int:
    if isinstance(value, (int, float)):
        epoch = int(value)
        if epoch < 1_000_000_000_000:
            return epoch * 1000
        return epoch
    return fallback_ms


def calculate_version(
    source_sha: str, version_cache: dict[str, Any], now_ms: int
) -> tuple[int, int, bool]:
    last_sha = version_cache.get("sourceIndexSha256")
    last_version = int(version_cache.get("version", 0)) if version_cache else 0
    last_generated = int(version_cache.get("generatedAtEpoch", 0)) if version_cache else 0

    changed = source_sha != last_sha
    version = last_version + 1 if changed else (last_version or 1)
    generated_at = now_ms if changed else (last_generated or now_ms)
    return version, generated_at, changed


def build_templates(
    entries: Iterable[dict[str, Any]],
    now_ms: int,
    verbose: bool = False,
) -> tuple[list[dict[str, Any]], Counter[str]]:
    templates: list[dict[str, Any]] = []
    seen_domains: set[str] = set()
    skipped: Counter[str] = Counter()

    for entry in entries:
        raw_base = find_base_url(entry)
        if not raw_base:
            skipped["missing_base_url"] += 1
            if verbose:
                print("warning: missing baseUrl/domain; skipping", file=sys.stderr)
            continue
        base_url, domain = normalize_url(raw_base)
        if not domain or not base_url:
            skipped["invalid_base_url"] += 1
            if verbose:
                print(f"warning: invalid baseUrl {raw_base!r}", file=sys.stderr)
            continue
        if domain in seen_domains:
            skipped["duplicate_domain"] += 1
            if verbose:
                print(f"warning: duplicate domain {domain}", file=sys.stderr)
            continue
        seen_domains.add(domain)

        stable_key = domain or base_url
        template_id = hashlib.sha1(stable_key.encode("utf-8")).hexdigest()
        name = entry.get("name") or entry.get("sourceName") or entry.get("title") or domain
        lang = entry.get("lang") or entry.get("language")
        is_nsfw = bool(entry.get("nsfw", entry.get("isNsfw", False)))
        has_cloudflare = bool(entry.get("hasCloudflare", entry.get("cloudflare", False)))
        is_dead = bool(entry.get("isDead", entry.get("obsolete", False)))
        updated_at = parse_epoch(
            entry.get("updatedAt")
            or entry.get("updateTime")
            or entry.get("versionDate")
            or entry.get("lastUpdated"),
            now_ms,
        )

        templates.append(
            {
                "id": template_id,
                "name": str(name),
                "domain": domain,
                "baseUrl": base_url,
                "engineType": infer_engine_type(entry),
                "lang": str(lang) if lang is not None else None,
                "isNsfw": is_nsfw,
                "hasCloudflare": has_cloudflare,
                "isDead": is_dead,
                "updatedAtEpoch": updated_at,
            }
        )

    return templates, skipped


def build_output(
    templates: list[dict[str, Any]],
    version: int,
    generated_at: int,
    source_url: str,
    source_sha: str,
    templates_sha: str,
) -> dict[str, Any]:
    return {
        "version": version,
        "generatedAtEpoch": generated_at,
        "count": len(templates),
        "sourceIndexUrl": source_url,
        "sourceIndexSha256": source_sha,
        "templatesSha256": templates_sha,
        "templates": templates,
    }


def compute_templates_sha(
    templates: list[dict[str, Any]],
    version: int,
    generated_at: int,
    source_url: str,
    source_sha: str,
) -> str:
    payload = build_output(
        templates=templates,
        version=version,
        generated_at=generated_at,
        source_url=source_url,
        source_sha=source_sha,
        templates_sha="",
    )
    min_json = json.dumps(payload, separators=(",", ":"), ensure_ascii=False, sort_keys=True)
    return sha256_hex(min_json.encode("utf-8"))


def write_outputs(payload: dict[str, Any], output_dir: str) -> tuple[str, str]:
    pretty_path = os.path.join(output_dir, "templates.json")
    min_path = os.path.join(output_dir, "templates.min.json")

    pretty_json = json.dumps(payload, indent=2, ensure_ascii=False, sort_keys=True)
    min_json = json.dumps(payload, separators=(",", ":"), ensure_ascii=False, sort_keys=True)

    with open(pretty_path, "w", encoding="utf-8") as handle:
        handle.write(pretty_json)
    with open(min_path, "w", encoding="utf-8") as handle:
        handle.write(min_json)

    return pretty_path, min_path


def sha256_hex(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--outdir", default=".", help="Directory to write output files")
    parser.add_argument("--cache-dir", default=DEFAULT_CACHE_DIR, help="Cache directory")
    parser.add_argument("--verbose", action="store_true", help="Enable verbose warnings")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    os.makedirs(args.cache_dir, exist_ok=True)

    etag_path = os.path.join(args.cache_dir, "etag.json")
    version_path = os.path.join(args.cache_dir, "version.json")

    etag_cache = load_etag_cache(etag_path)

    try:
        result = fetch_index(INDEX_URLS, etag_cache)
    except FetchError as exc:
        print(str(exc), file=sys.stderr)
        return 1

    if result.status == 304:
        print("No changes")
        return 0

    if not result.payload:
        print("No payload received.", file=sys.stderr)
        return 1

    if result.etag:
        etag_cache[result.url] = result.etag
        save_etag_cache(etag_path, etag_cache)

    source_sha = sha256_hex(result.payload)
    version_cache = read_json(version_path)
    now_ms = int(time.time() * 1000)
    version, generated_at, changed = calculate_version(source_sha, version_cache, now_ms)

    data = json_load(result.payload)
    entries = list(iter_entries(data))
    templates, skipped = build_templates(entries, generated_at, args.verbose)

    templates_sha = compute_templates_sha(
        templates=templates,
        version=version,
        generated_at=generated_at,
        source_url=result.url,
        source_sha=source_sha,
    )

    output_payload = build_output(
        templates,
        version,
        generated_at,
        result.url,
        source_sha,
        templates_sha,
    )

    _, min_path = write_outputs(output_payload, args.outdir)
    with open(min_path, "rb") as handle:
        actual_min_sha = sha256_hex(handle.read())

    checksum_path = os.path.join(args.outdir, "templates.min.json.sha256")
    with open(checksum_path, "w", encoding="utf-8") as handle:
        handle.write(actual_min_sha)

    version_output = os.path.join(args.outdir, "version.json")
    with open(version_output, "w", encoding="utf-8") as handle:
        json.dump(
            {"version": version, "generatedAtEpoch": generated_at},
            handle,
            indent=2,
            sort_keys=True,
        )

    if changed:
        write_json(
            version_path,
            {
                "version": version,
                "sourceIndexSha256": source_sha,
                "generatedAtEpoch": generated_at,
            },
        )

    total_skipped = sum(skipped.values())
    top_reasons = ", ".join(
        f"{reason}={count}" for reason, count in skipped.most_common(5)
    ) or "none"

    print(
        "Summary:\n"
        f"- input URL: {result.url}\n"
        f"- http status: {result.status}\n"
        f"- etag: {result.etag or 'none'}\n"
        f"- sourceIndexSha256: {source_sha}\n"
        f"- total entries: {len(entries)}\n"
        f"- templates: {len(templates)}\n"
        f"- skipped: {total_skipped} (top reasons: {top_reasons})"
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
