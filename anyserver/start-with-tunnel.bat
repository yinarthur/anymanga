@echo off
REM AnyManga Server + Ngrok Tunnel Startup Script (Windows)
REM This script starts the development server and creates a public tunnel

echo 🚀 Starting AnyManga Server with Ngrok Tunnel...
echo.

REM Start the server in background
echo 📡 Starting development server...
start /B npm run dev

REM Wait for server to start
echo ⏳ Waiting for server to initialize...
timeout /t 3 /nobreak > nul

REM Start ngrok tunnel
echo 🌐 Creating public tunnel with Ngrok...
echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   Copy the HTTPS URL from Ngrok below
echo   and use it in ApiConfig.kt
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

ngrok http 3000
