const fs = require('fs');
const path = require('path');

// قراءة index.min.json
const indexPath = path.join(__dirname, 'extensions', 'index.min.json');
const extensions = JSON.parse(fs.readFileSync(indexPath, 'utf8'));

// استخراج جميع المصادر العربية
const arabicSources = [];

extensions.forEach(ext => {
    if (ext.sources) {
        ext.sources.forEach(source => {
            if (source.lang === 'ar') {
                arabicSources.push({
                    id: source.id,
                    name: source.name,
                    baseUrl: source.baseUrl,
                    extensionName: ext.name,
                    pkg: ext.pkg
                });
            }
        });
    }
});

console.log(`Found ${arabicSources.length} Arabic sources`);
console.log(JSON.stringify(arabicSources, null, 2));

// حفظ النتيجة
fs.writeFileSync(
    path.join(__dirname, 'arabic_sources.json'),
    JSON.stringify(arabicSources, null, 2)
);
