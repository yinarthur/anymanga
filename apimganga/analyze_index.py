import urllib.request
import json
from collections import Counter

url = 'https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json'

def analyze():
    print(f"Fetching {url}...")
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode('utf-8'))
    
    print(f"Total entries: {len(data)}")
    
    pkgs = [e['pkg'] for e in data]
    
    # Analyze package structures
    # eu.kanade.tachiyomi.extension.all.ahottie -> split by dot
    segments = [p.split('.') for p in pkgs]
    
    # Looking for common theme identifiers (usually at index 5 if using multi-package)
    # eu.kanade.tachiyomi.extension.<lang>.<theme>.<sitename>
    # index: 0    1       2          3        4        5
    
    potential_themes = []
    for s in segments:
        if len(s) > 5:
            potential_themes.append(s[4])
            
    theme_counts = Counter(potential_themes)
    print("\nMost common theme segments (index 4 in pkg):")
    for theme, count in theme_counts.most_common(50):
        print(f"  {theme}: {count}")

    # Search for known themes in all text
    all_names = [e['name'].lower() for e in data]
    keyword_counts = Counter()
    keywords = ["madara", "themesia", "mangastream", "foolslide", "genkan", "wp-manga", "mangadventure", "mangarock"]
    
    for name in all_names:
        for kw in keywords:
            if kw in name:
                keyword_counts[kw] += 1
                
    print("\nKeyword matches in names:")
    for kw, count in keyword_counts.items():
        print(f"  {kw}: {count}")

    # Check for sources field indicators
    source_names = []
    for e in data:
        if 'sources' in e:
            for s in e['sources']:
                source_names.append(s.get('name', '').lower())
    
    source_keyword_counts = Counter()
    for name in source_names:
        for kw in keywords:
            if kw in name:
                source_keyword_counts[kw] += 1
                
    print("\nKeyword matches in source names:")
    for kw, count in source_keyword_counts.items():
        print(f"  {kw}: {count}")

if __name__ == "__main__":
    analyze()
