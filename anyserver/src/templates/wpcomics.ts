import { EngineTemplate } from '../types';

export const wpcomicsTemplate: EngineTemplate = {
    name: 'WPComics',
    endpoints: {
        popular: {
            url: '{baseUrl}/hot',
            method: 'GET',
            params: { page: '{page}' }
        },
        latest: {
            url: '{baseUrl}/',
            method: 'GET',
            params: { page: '{page}' }
        },
        search: {
            url: '{baseUrl}/tim-truyen',
            method: 'GET',
            params: { keyword: '{query}', page: '{page}' }
        },
        mangaDetails: {
            url: '{baseUrl}/series/{slug}/', // Note: slug usually matches relative URL
            method: 'GET'
        },
        chapters: {
            url: '{baseUrl}/series/{slug}/',
            method: 'GET'
        },
        pages: {
            url: '{baseUrl}/read/{slug}/{chapter}/',
            method: 'GET'
        }
    },
    selectors: {
        mangaList: 'div.items div.item',
        mangaTitle: 'h3 a',
        mangaCover: 'div.image img',
        chapterList: 'div.list-chapter li.row:not(.heading)',
        pageImages: 'div.page-chapter > img'
    }
};
