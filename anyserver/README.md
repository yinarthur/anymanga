# AnyManga Server 🚀

Server-side API that provides manga source endpoints dynamically for the AnyManga application.

## Features

- ✅ 1300+ manga sources from Keiyoushi extensions
- ✅ Automatic engine detection (Madara, MangaThemesia, FoolSlide)
- ✅ Search by URL or name
- ✅ Dynamic API endpoint generation
- ✅ Arabic sources support (49 sources)

## Quick Start

### 1. Install Dependencies

```bash
npm install
```

### 2. Copy index.min.json

Copy the `index.min.json` file to `extensions/` directory:

```bash
# From extensions repository
cp path/to/extensions/index.min.json extensions/
```

### 3. Run Development Server

```bash
npm run dev
```

Server will start on `http://localhost:3000`

## API Endpoints

### Search by URL

```bash
POST /api/search/url
Content-Type: application/json

{
  "url": "https://mangalek.net"
}
```

### Search by Name

```bash
POST /api/search/name
Content-Type: application/json

{
  "name": "mangalek"
}
```

### Get Source API

```bash
GET /api/source/:sourceId/api
```

### Get Statistics

```bash
GET /api/stats
```

## Example Response

```json
{
  "found": true,
  "source": {
    "id": "918460697583900080",
    "name": "مانجا ليك",
    "lang": "ar",
    "baseUrl": "https://mangalek.net",
    "nsfw": false
  },
  "engine": "madara",
  "api": {
    "engine": "Madara",
    "endpoints": {
      "popular": {
        "url": "https://mangalek.net/manga/page/{page}/",
        "method": "GET",
        "params": { "m_orderby": "views" }
      },
      "search": {
        "url": "https://mangalek.net/",
        "method": "GET",
        "params": { "s": "{query}", "post_type": "wp-manga" }
      }
    }
  }
}
```

## Supported Engines

- **Madara** (70% of sources)
- **MangaThemesia** (15% of sources)
- **FoolSlide** (10% of sources)
- **Custom** (5% of sources)

## Build for Production

```bash
npm run build
npm start
```

## License

MIT
