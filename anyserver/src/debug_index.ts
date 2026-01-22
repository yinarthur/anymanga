import * as fs from 'fs';
import * as path from 'path';

try {
    const indexPath = path.join(__dirname, '../extensions/index.min.json');
    const data = JSON.parse(fs.readFileSync(indexPath, 'utf-8'));

    console.log(`Total extensions: ${data.length}`);

    const targets = ['mangalek', 'arabtoons', 'kenmanga', '3asq'];

    targets.forEach(target => {
        console.log(`\n--- Searching for: ${target} ---`);
        const found = data.filter((ext: any) =>
            ext.name.toLowerCase().includes(target) ||
            ext.pkg.includes(target) ||
            ext.sources.some((s: any) => s.baseUrl.includes(target) || s.name.toLowerCase().includes(target))
        );

        if (found.length > 0) {
            found.forEach((ext: any) => {
                console.log(`Found extension: ${ext.name}`);
                console.log(`Package: ${ext.pkg}`);
                console.log(`Sources:`);
                ext.sources.forEach((s: any) => {
                    console.log(`  - Name: ${s.name}`);
                    console.log(`  - ID: ${s.id}`);
                    console.log(`  - BaseURL: ${s.baseUrl}`);
                });
            });
        } else {
            console.log(`❌ Not found.`);
        }
    });

} catch (err) {
    console.error(err);
}
