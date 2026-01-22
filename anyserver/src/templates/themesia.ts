import { EngineTemplate } from '../types';

export const themesiaTemplate: EngineTemplate = {
    name: 'MangaThemesia',
    endpoints: {
        popular: {
            url: '{baseUrl}/manga/',
            method: 'GET',
            params: { page: '{page}', order: 'popular' }
        },
        latest: {
            url: '{baseUrl}/manga/',
            method: 'GET',
            params: { page: '{page}', order: 'update' }
        },
        search: {
            url: '{baseUrl}/',
            method: 'GET',
            params: { s: '{query}' }
        },
        mangaDetails: {
            url: '{baseUrl}/manga/{slug}/',
            method: 'GET'
        },
        chapters: {
            url: '{baseUrl}/manga/{slug}/',
            method: 'GET'
        },
        pages: {
            url: '{baseUrl}/manga/{slug}/{chapter}/',
            method: 'GET'
        }
    },
    selectors: {
        mangaList: '.listupd .bs, .bsx',
        mangaTitle: '.entry-title',
        mangaCover: '.thumb img',
        chapterList: '#chapterlist li',
        pageImages: '#readerarea img'
    }
};
