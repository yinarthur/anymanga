import { EngineTemplate } from '../types';
import { madaraTemplate } from '../templates/madara';
import { themesiaTemplate } from '../templates/themesia';
import { foolslideTemplate } from '../templates/foolslide';
import { wpcomicsTemplate } from '../templates/wpcomics';
import { mmrcmsTemplate } from '../templates/mmrcms';

export class EngineService {
    private templates: Map<string, EngineTemplate>;

    constructor() {
        this.templates = new Map([
            ['madara', madaraTemplate],
            ['mangathemesia', themesiaTemplate],
            ['foolslide', foolslideTemplate],
            ['wpcomics', wpcomicsTemplate],
            ['mmrcms', mmrcmsTemplate]
        ]);
    }

    getTemplate(engine: string): EngineTemplate | null {
        return this.templates.get(engine) || null;
    }

    buildAPI(baseUrl: string, engine: string) {
        const template = this.getTemplate(engine);

        if (!template) {
            return null;
        }

        // Clone template and replace {baseUrl}
        const apiString = JSON.stringify(template);
        const replacedString = apiString.replace(/{baseUrl}/g, baseUrl);
        const api = JSON.parse(replacedString);

        return {
            engine: template.name,
            endpoints: api.endpoints,
            selectors: api.selectors
        };
    }

    detectEngine(pkg: string, baseUrl?: string): string {
        // Manual overrides for known sites
        if (baseUrl) {
            const domain = baseUrl.toLowerCase();
            if (domain.includes('arabtoons')) return 'madara';
            if (domain.includes('mangalek')) return 'madara';
            if (domain.includes('kenmanga')) return 'madara';
            if (domain.includes('3asq.org')) return 'madara';
            if (domain.includes('azoramoon')) return 'madara';
            if (domain.includes('mangapeak')) return 'madara';
            if (domain.includes('hijala')) return 'mangathemesia';
        }

        // Package-based detection
        if (pkg.includes('.multisrc.madara')) return 'madara';
        if (pkg.includes('.multisrc.mangathemesia')) return 'mangathemesia';
        if (pkg.includes('.multisrc.foolslide')) return 'foolslide';
        if (pkg.includes('.multisrc.wpcomics')) return 'wpcomics';
        if (pkg.includes('.multisrc.mmrcms')) return 'mmrcms';
        if (pkg.includes('.multisrc.heancms')) return 'heancms';

        return 'custom';
    }
}
