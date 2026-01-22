import { ExtensionLoader } from './services/loader';
import { EngineService } from './services/engine';

async function verify() {
    console.log('🔍 Starting AnyManga Server Verification...');

    const loader = new ExtensionLoader();
    const engineService = new EngineService();

    try {
        await loader.load();
        const stats = loader.getStats();

        console.log('\n--- 📊 Statistics ---');
        console.log(`Total Extensions: ${stats.totalExtensions}`);
        console.log(`Total Sources: ${stats.totalSources}`);
        console.log(`Arabic Sources: ${stats.byLanguage['ar'] || 0}`);

        console.log('\n--- 🧪 Testing Popular Arabic Sources ---');
        const testUrsl = [
            'https://mangalek.net',
            'https://arabtoons.net',
            'https://kenmanga.com'
        ];

        for (const url of testUrsl) {
            const result = loader.findByUrl(url);
            if (result) {
                const { source, baseUrl } = result;
                const api = engineService.buildAPI(baseUrl, source.engine);
                console.log(`✅ [${source.name}] Found! Engine: ${source.engine}`);
                if (api) {
                    console.log(`   🚀 API Endpoints generated successfully.`);
                } else {
                    console.log(`   ⚠️  No API template for engine: ${source.engine}`);
                }
            } else {
                console.log(`❌ [${url}] Not found in index.`);
            }
        }

        console.log('\n--- ✨ Verification Complete ---');
    } catch (error) {
        console.error('❌ Verification failed:', error);
    }
}

verify();
