import json
import unittest

import script


class TemplateTests(unittest.TestCase):
    def test_normalize_url_variants(self):
        base, domain = script.normalize_url("example.com/path")
        self.assertEqual(domain, "example.com")
        self.assertEqual(base, "https://example.com/path")

        base, domain = script.normalize_url("https://www.Example.com")
        self.assertEqual(domain, "example.com")
        self.assertEqual(base, "https://www.Example.com")

        base, domain = script.normalize_url("https://example.com.")
        self.assertEqual(domain, "example.com")

        base, domain = script.normalize_url("https://bücher.example")
        self.assertEqual(domain, "xn--bcher-kva.example")

    def test_deduplicate_domains(self):
        now_ms = 1_700_000_000_000
        entries = [
            {"name": "A", "baseUrl": "https://example.com"},
            {"name": "B", "website": "https://example.com"},
            {"name": "C", "baseUrl": "https://other.com"},
        ]
        templates, skipped = script.build_templates(entries, now_ms)
        self.assertEqual(len(templates), 2)
        self.assertEqual(skipped["duplicate_domain"], 1)

    def test_schema_validation(self):
        templates = [
            {
                "id": "abc",
                "name": "Test",
                "domain": "example.com",
                "baseUrl": "https://example.com",
                "engineType": "GENERIC",
                "lang": None,
                "isNsfw": False,
                "hasCloudflare": False,
                "isDead": False,
                "updatedAtEpoch": 123,
            }
        ]
        payload = script.build_output(
            templates=templates,
            version=1,
            generated_at=123,
            source_url="https://example.com/index.json",
            source_sha="deadbeef",
            templates_sha="bead",
        )
        self.assertEqual(payload["count"], 1)
        self.assertEqual(payload["templates"][0]["domain"], "example.com")
        json.dumps(payload)

    def test_version_increment(self):
        now_ms = 1_700_000_000_000
        cache = {"version": 2, "sourceIndexSha256": "old", "generatedAtEpoch": 1000}
        version, generated_at, changed = script.calculate_version("new", cache, now_ms)
        self.assertTrue(changed)
        self.assertEqual(version, 3)
        self.assertEqual(generated_at, now_ms)

        version, generated_at, changed = script.calculate_version("old", cache, now_ms)
        self.assertFalse(changed)
        self.assertEqual(version, 2)
        self.assertEqual(generated_at, 1000)


if __name__ == "__main__":
    unittest.main()
