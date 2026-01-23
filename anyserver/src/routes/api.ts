import { Router, Request, Response } from 'express';
import { ExtensionLoader } from '../services/loader';
import { EngineService } from '../services/engine';
import { APIResponse } from '../types';
const cloudscraper = require('cloudscraper');

const router = Router();
const loader = new ExtensionLoader();
const engineService = new EngineService();

// Initialize loader
let isLoaded = false;
loader.load().then(() => {
    isLoaded = true;
}).catch(err => {
    console.error('Failed to load extensions:', err);
});

// Middleware to check if data is loaded
const checkLoaded = (req: Request, res: Response, next: Function) => {
    if (!isLoaded) {
        return res.status(503).json({ error: 'Server is still loading data, please try again' });
    }
    next();
};

// Search by URL
router.post('/search/url', checkLoaded, (req: Request, res: Response) => {
    const { url } = req.body;

    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    const result = loader.findByUrl(url);

    if (!result) {
        return res.json({ found: false } as APIResponse);
    }

    const { source, baseUrl } = result;
    const api = engineService.buildAPI(baseUrl, source.engine);

    const response: APIResponse = {
        found: true,
        source: {
            id: source.sourceId,
            name: source.name,
            lang: source.lang,
            baseUrl: baseUrl,
            nsfw: source.nsfw
        },
        engine: source.engine,
        api: api || undefined
    };

    res.json(response);
});

// Search by name
router.post('/search/name', checkLoaded, (req: Request, res: Response) => {
    const { name } = req.body;

    if (!name) {
        return res.status(400).json({ error: 'Name is required' });
    }

    const results = loader.findByName(name);

    res.json({
        found: results.length > 0,
        count: results.length,
        sources: results.map(source => ({
            id: source.sourceId,
            name: source.name,
            lang: source.lang,
            baseUrl: source.baseUrl,
            engine: source.engine,
            nsfw: source.nsfw
        }))
    });
});

// Get API for specific source by ID
router.get('/source/:sourceId/api', checkLoaded, (req: Request, res: Response) => {
    const { sourceId } = req.params;

    const result = loader.findById(sourceId);

    if (!result) {
        return res.status(404).json({ error: 'Source not found' });
    }

    const { source, baseUrl } = result;
    const api = engineService.buildAPI(baseUrl, source.engine);

    res.json({
        source: {
            id: source.sourceId,
            name: source.name,
            lang: source.lang,
            baseUrl: baseUrl,
            nsfw: source.nsfw
        },
        engine: source.engine,
        api: api
    });
});

// Get server statistics
router.get('/stats', checkLoaded, (req: Request, res: Response) => {
    const stats = loader.getStats();
    res.json(stats);
});

// Fetch full HTML (useful for the app to parse content via proxy)
router.post('/fetch/html', async (req: Request, res: Response) => {
    const { url } = req.body;

    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    try {
        const html = await new Promise<string>((resolve, reject) => {
            cloudscraper.get(url, (error: any, response: any, body: string) => {
                if (error) {
                    reject(error);
                } else {
                    resolve(body);
                }
            });
        });

        res.json({
            success: true,
            url: url,
            html: html
        });
    } catch (error: any) {
        res.status(500).json({
            success: false,
            error: error.message
        });
    }
});

// Test endpoint to fetch manga data
router.post('/test/fetch', async (req: Request, res: Response) => {
    const { url } = req.body;

    if (!url) {
        return res.status(400).json({ error: 'URL is required' });
    }

    try {
        // Use cloudscraper to bypass Cloudflare
        const html = await new Promise<string>((resolve, reject) => {
            cloudscraper.get(url, (error: any, response: any, body: string) => {
                if (error) {
                    reject(error);
                } else {
                    resolve(body);
                }
            });
        });

        // Extract basic info
        const titleMatch = html.match(/<title>(.*?)<\/title>/);
        const title = titleMatch ? titleMatch[1] : 'Unknown';

        // Extract image URLs (for manga pages) - more generic regex
        const imageRegex = /https?:\/\/[^"'\s]+\.(jpg|jpeg|png|webp|gif)/gi;
        const images = [...new Set(html.match(imageRegex) || [])];

        // Try to extract chapter images specifically
        const chapterImages = images.filter(img =>
            img.includes('WP-manga/data/manga_') ||
            img.includes('/upload/') ||
            img.includes('storage.') ||
            img.includes('/manga/') ||
            img.includes('/chapter/')
        );

        res.json({
            success: true,
            url: url,
            title: title,
            contentLength: html.length,
            totalImages: images.length,
            chapterImages: chapterImages,
            preview: html.substring(0, 500)
        });
    } catch (error: any) {
        res.status(500).json({
            success: false,
            error: error.message
        });
    }
});

export default router;
