# AnyServer - Manga API Server

API server for AnyManga application that provides manga source APIs dynamically.

## Features
- Dynamic manga source support
- RESTful API endpoints
- Cloudflare bypass support
- 1300+ manga sources

## Deployment

### Deploy to Render

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy)

Or manually:

1. Fork this repository
2. Create a new Web Service on Render
3. Connect your GitHub repository
4. Use these settings:
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
   - **Environment**: Node

## API Endpoints

- `GET /api/stats` - Server statistics
- `POST /api/search` - Search manga
- `POST /api/chapters` - Get manga chapters
- `POST /api/pages` - Get chapter pages

## Local Development

```bash
npm install
npm run dev
```

Server runs on http://localhost:3000

## Environment Variables

- `PORT` - Server port (default: 3000)
- `NODE_ENV` - Environment (development/production)
