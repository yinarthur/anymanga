import * as fs from 'fs/promises';
import * as path from 'path';
import { Extension, SourceInfo } from '../types';
import { EngineService } from './engine';

export class ExtensionLoader {
    private extensions: Extension[] = [];
    private sourceIndex = new Map<string, SourceInfo>();
    private engineService: EngineService;

    constructor() {
        this.engineService = new EngineService();
    }

    async load() {
        try {
            const dataPath = path.join(__dirname, '../../extensions-index.json');
            const data = await fs.readFile(dataPath, 'utf-8');
            this.extensions = JSON.parse(data);
            this.buildIndex();
            console.log(`✅ Loaded ${this.extensions.length} extensions`);
            console.log(`✅ Indexed ${this.sourceIndex.size} sources`);
        } catch (error) {
            console.error('❌ Error loading extensions:', error);
            throw error;
        }
    }

    private buildIndex() {
        this.extensions.forEach(ext => {
            ext.sources.forEach(source => {
                // Pass baseUrl to detectEngine for better detection
                const engine = this.engineService.detectEngine(ext.pkg, source.baseUrl);

                this.sourceIndex.set(source.baseUrl, {
                    sourceId: source.id,
                    name: source.name,
                    lang: source.lang,
                    engine: engine,
                    nsfw: ext.nsfw === 1
                });
            });
        });
    }

    findByUrl(url: string): { source: SourceInfo; baseUrl: string } | null {
        // Try exact match first
        let source = this.sourceIndex.get(url);
        let baseUrl = url;

        // If not found, try to extract domain
        if (!source) {
            try {
                const urlObj = new URL(url);
                const origin = urlObj.origin;
                source = this.sourceIndex.get(origin);
                baseUrl = origin;
            } catch {
                // Invalid URL
                return null;
            }
        }

        if (!source) {
            return null;
        }

        return { source, baseUrl };
    }

    findByName(name: string): Array<SourceInfo & { baseUrl: string }> {
        const results: Array<SourceInfo & { baseUrl: string }> = [];
        const searchTerm = name.toLowerCase();

        for (const [url, source] of this.sourceIndex) {
            if (source.name.toLowerCase().includes(searchTerm)) {
                results.push({ ...source, baseUrl: url });
            }
        }

        return results;
    }

    findById(sourceId: string): { source: SourceInfo; baseUrl: string } | null {
        for (const [url, source] of this.sourceIndex) {
            if (source.sourceId === sourceId) {
                return { source, baseUrl: url };
            }
        }
        return null;
    }

    getStats() {
        const stats = {
            totalExtensions: this.extensions.length,
            totalSources: this.sourceIndex.size,
            byEngine: {} as Record<string, number>,
            byLanguage: {} as Record<string, number>
        };

        for (const source of this.sourceIndex.values()) {
            stats.byEngine[source.engine] = (stats.byEngine[source.engine] || 0) + 1;
            stats.byLanguage[source.lang] = (stats.byLanguage[source.lang] || 0) + 1;
        }

        return stats;
    }
}
