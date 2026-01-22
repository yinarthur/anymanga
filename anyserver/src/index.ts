import express from 'express';
import cors from 'cors';
import apiRoutes from './routes/api';

const app = express();
const PORT = Number(process.env.PORT) || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api', apiRoutes);

// Health check
app.get('/health', (req, res) => {
    res.json({
        status: 'ok',
        timestamp: new Date().toISOString(),
        service: 'AnyManga Server'
    });
});

// Root endpoint
app.get('/', (req, res) => {
    res.json({
        name: 'AnyManga Server',
        version: '1.0.0',
        description: 'Provides manga source APIs dynamically',
        endpoints: {
            health: '/health',
            searchByUrl: 'POST /api/search/url',
            searchByName: 'POST /api/search/name',
            getSourceAPI: 'GET /api/source/:sourceId/api',
            stats: 'GET /api/stats'
        }
    });
});

// Start server - Listen on 0.0.0.0 to allow external connections (Ngrok, LAN, etc.)
app.listen(PORT, '0.0.0.0', () => {
    console.log('🚀 AnyManga Server');
    console.log(`📡 Server running on http://0.0.0.0:${PORT}`);
    console.log(`🔍 Local access: http://localhost:${PORT}/api`);
    console.log(`💚 Health check: http://localhost:${PORT}/health`);
    console.log(`🌐 Ready for Ngrok tunnel or LAN access`);
});
