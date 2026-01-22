// TypeScript Types for AnyManga Server

export interface Source {
  name: string;
  lang: string;
  id: string;
  baseUrl: string;
}

export interface Extension {
  name: string;
  pkg: string;
  apk: string;
  lang: string;
  version: string;
  nsfw: number;
  sources: Source[];
}

export interface SourceInfo {
  sourceId: string;
  name: string;
  lang: string;
  engine: string;
  nsfw: boolean;
}

export interface Endpoint {
  url: string;
  method: 'GET' | 'POST';
  params?: Record<string, string>;
  headers?: Record<string, string>;
}

export interface EngineTemplate {
  name: string;
  endpoints: {
    popular: Endpoint;
    latest: Endpoint;
    search: Endpoint;
    mangaDetails: Endpoint;
    chapters: Endpoint;
    pages: Endpoint;
  };
  selectors: {
    mangaList: string;
    mangaTitle: string;
    mangaCover: string;
    chapterList: string;
    pageImages: string;
  };
}

export interface APIResponse {
  found: boolean;
  source?: {
    id: string;
    name: string;
    lang: string;
    baseUrl: string;
    nsfw: boolean;
  };
  engine?: string;
  api?: {
    engine: string;
    endpoints: EngineTemplate['endpoints'];
    selectors: EngineTemplate['selectors'];
  };
}
