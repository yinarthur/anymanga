import { EngineTemplate } from '../types';

export const madaraTemplate: EngineTemplate = {
    name: 'Madara',
    endpoints: {
        popular: {
            url: '{baseUrl}/manga/page/{page}/',
            method: 'GET',
            params: { m_orderby: 'views' }
        },
        latest: {
            url: '{baseUrl}/manga/page/{page}/',
            method: 'GET',
            params: { m_orderby: 'latest' }
        },
        search: {
            url: '{baseUrl}/',
            method: 'GET',
            params: { s: '{query}', post_type: 'wp-manga' }
        },
        mangaDetails: {
            url: '{baseUrl}/manga/{slug}/',
            method: 'GET'
        },
        chapters: {
            url: '{baseUrl}/manga/{slug}/ajax/chapters/',
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        },
        pages: {
            url: '{baseUrl}/manga/{slug}/{chapter}/',
            method: 'GET'
        }
    },
    selectors: {
        mangaList: '.page-item-detail',
        mangaTitle: '.post-title h1, .post-title h3',
        mangaCover: '.summary_image img',
        chapterList: '.wp-manga-chapter',
        pageImages: '.reading-content img, .page-break img'
    }
};
