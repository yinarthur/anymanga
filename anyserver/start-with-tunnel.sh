#!/bin/bash

# AnyManga Server + Ngrok Tunnel Startup Script
# This script starts the development server and creates a public tunnel

echo "🚀 Starting AnyManga Server with Ngrok Tunnel..."
echo ""

# Start the server in background
echo "📡 Starting development server..."
npm run dev &
SERVER_PID=$!

# Wait for server to start
echo "⏳ Waiting for server to initialize..."
sleep 3

# Start ngrok tunnel
echo "🌐 Creating public tunnel with Ngrok..."
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Copy the HTTPS URL from Ngrok below"
echo "  and use it in ApiConfig.kt"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

ngrok http 3000

# Cleanup on exit
trap "kill $SERVER_PID" EXIT
