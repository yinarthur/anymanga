import os
import re
import json
from pathlib import Path

# Definitions
EXTENSIONS_DIR = Path("extensions-source/src")
MULTISRC_DIR = Path("extensions-source/lib-multisrc")

def get_multisrc_engines():
    """Returns a set of known multisrc engine names from lib-multisrc."""
    if not MULTISRC_DIR.exists():
        return set()
    return {d.name for d in MULTISRC_DIR.iterdir() if d.is_dir()}

def analyze_extension(ext_path, multisrc_engines):
    """Analyzes a specific extension directory to find its Engine and BaseURL."""
    
    engine = "Custom/Unknown"
    base_url = None
    name = ext_path.name

    # 1. Check build.gradle or build.gradle.kts for Multisrc dependency
    gradle_files = [ext_path / "build.gradle.kts", ext_path / "build.gradle"]
    for build_file in gradle_files:
        if build_file.exists():
            try:
                content = build_file.read_text(encoding="utf-8")
                # Look for project(":lib-multisrc:madara") or similar
                # Regex handles both Kotlin DSL (parentheses/quotes) and Groovy (quotes/no parentheses)
                match = re.search(r'project\(\W*:lib-multisrc:(\w+)\W*\)', content)
                if match:
                    engine = match.group(1).upper() # e.g., MADARA
                    break
                
                # 1b. Check for 'themePkg' in Groovy ext block
                theme_match = re.search(r"themePkg\s*=\s*['\"](\w+)['\"]", content)
                if theme_match:
                     engine = theme_match.group(1).upper()
                
                # Check for baseUrl in ext block while we are at it
                url_match = re.search(r"baseUrl\s*=\s*['\"]([^'\"]+)['\"]", content)
                if url_match and not base_url:
                    base_url = url_match.group(1)

            except Exception:
                pass

    # 2. Scan Kotlin files for baseUrl and Class Inheritance (Fallback for Engine)
    # Usually in src/eu/kanade/tachiyomi/extension/<lang>/<name>/SomeFile.kt
    
    kt_files = list(ext_path.rglob("*.kt"))
    for kt_file in kt_files:
        try:
            content = kt_file.read_text(encoding="utf-8", errors="ignore")
            
            # Simple regex to find baseUrl
            # override val baseUrl = "https://example.com"
            if not base_url:
                url_match = re.search(r'override\s+val\s+baseUrl\s*=\s*"([^"]+)"', content)
                if url_match:
                    base_url = url_match.group(1)

            # Fallback: Check for class inheritance if engine is still unknown
            if engine == "Custom/Unknown":
                 # class SomeClass : Madara(
                 inheritance_match = re.search(r'class\s+\w+\s*:\s*(\w+)\s*\(', content)
                 if inheritance_match:
                     potential_engine = inheritance_match.group(1).upper()
                     # specific check for common engines
                     if potential_engine in ["MADARA", "MANGATHEMESIA", "MANGASTREAM", "FOOLSLIDE", "WPCOMICS"]:
                         engine = potential_engine

        except Exception:
            continue

    if base_url:
        return {
            "name": name,
            "engine": engine,
            "baseUrl": base_url,
            "path": str(ext_path)
        }
    return None

def main():
    if not EXTENSIONS_DIR.exists():
        print(f"Error: Could not find {EXTENSIONS_DIR}. Make sure you are running this in the 'apimganga' directory.")
        return

    print("Scanning extensions source code...")
    
    multisrc_engines = get_multisrc_engines()
    print(f"Found known engines: {multisrc_engines}")

    results = []
    
    # Iterate over language folders (ar, en, es, etc.)
    for lang_dir in EXTENSIONS_DIR.iterdir():
        if not lang_dir.is_dir():
            continue
            
        # Iterate over extension folders inside language
        for ext_dir in lang_dir.iterdir():
            if not ext_dir.is_dir():
                continue
                
            info = analyze_extension(ext_dir, multisrc_engines)
            if info:
                # Add language info
                info['lang'] = lang_dir.name
                results.append(info)

    # Output results
    output_file = "local_extensions_analysis.json"
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)

    print(f"\nAnalysis Complete!")
    print(f"Found {len(results)} valid extensions.")
    print(f"Results saved to {output_file}")
    
    # Print statistics
    from collections import Counter
    engines_count = Counter(r['engine'] for r in results)
    print("\nEngine Distribution:")
    for engine, count in engines_count.most_common():
        print(f"{engine}: {count}")

if __name__ == "__main__":
    main()
