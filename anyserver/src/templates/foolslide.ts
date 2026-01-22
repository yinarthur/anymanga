import { EngineTemplate } from '../types';

export const foolslideTemplate: EngineTemplate = {
    name: 'FoolSlide',
    endpoints: {
        popular: {
            url: '{baseUrl}/directory/{page}/',
            method: 'GET'
        },
        latest: {
            url: '{baseUrl}/latest/{page}/',
            method: 'GET'
        },
        search: {
            url: '{baseUrl}/search/',
            method: 'GET',
            params: { search: '{query}' }
        },
        mangaDetails: {
            url: '{baseUrl}/series/{slug}/',
            method: 'GET'
        },
        chapters: {
            url: '{baseUrl}/series/{slug}/',
            method: 'GET'
        },
        pages: {
            url: '{baseUrl}/read/{slug}/{chapter}/page/{page}',
            method: 'GET'
        }
    },
    selectors: {
        mangaList: '.group',
        mangaTitle: '.title',
        mangaCover: '.thumbnail img',
        chapterList: '.element',
        pageImages: '.img-responsive'
    }
};
