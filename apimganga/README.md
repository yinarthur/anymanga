# Keiyoushi Templates Generator

## Local run

```bash
python script.py --outdir . --cache-dir .cache
```

The generator writes outputs to the selected output directory:

- `templates.json`
- `templates.min.json`
- `templates.min.json.sha256`
- `version.json`

## Android app usage

- Send `If-None-Match` with the cached ETag when fetching `templates.min.json`.
- If the server returns `304 Not Modified`, reuse the cached templates.
- When you receive a new `templates.min.json`, verify its SHA-256 checksum matches
  `templates.min.json.sha256` before loading.
- The `templatesSha256` field is computed from the minified payload with an empty
  `templatesSha256` field to avoid self-referential hashing.
