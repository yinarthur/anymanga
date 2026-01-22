import { EngineTemplate } from '../types';

export const mmrcmsTemplate: EngineTemplate = {
    name: 'MMRCMS',
    endpoints: {
        popular: {
            url: '{baseUrl}/filterList',
            method: 'GET',
            params: { page: '{page}', sortBy: 'views', asc: 'false' }
        },
        latest: {
            url: '{baseUrl}/latest-release',
            method: 'GET',
            params: { page: '{page}' }
        },
        search: {
            url: '{baseUrl}/search',
            method: 'GET',
            params: { query: '{query}' }
        },
        mangaDetails: {
            url: '{baseUrl}/manga/{slug}',
            method: 'GET'
        },
        chapters: {
            url: '{baseUrl}/manga/{slug}',
            method: 'GET'
        },
        pages: {
            url: '{baseUrl}/manga/{slug}/{chapter}',
            method: 'GET'
        }
    },
    selectors: {
        mangaList: 'div.media, div.manga-item',
        mangaTitle: '.media-heading a, .manga-heading a',
        mangaCover: '.media-left img, .row img.img-responsive',
        chapterList: 'ul.chapters > li:not(.btn)',
        pageImages: '#all > img.img-responsive'
    }
};
