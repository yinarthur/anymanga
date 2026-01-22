# 🚀 Ngrok Setup Guide - AnyManga Server

## Quick Start

### 1. Start the Server with Ngrok Tunnel

**Windows:**

```bash
cd anyserver
start-with-tunnel.bat
```

**Linux/Mac:**

```bash
cd anyserver
chmod +x start-with-tunnel.sh
./start-with-tunnel.sh
```

### 2. Copy the Ngrok URL

Ngrok will display something like:

```
Forwarding  https://abc123.ngrok.io -> http://localhost:3000
```

Copy the **HTTPS URL** (e.g., `https://abc123.ngrok.io`)

### 3. Update the App

Open the AnyManga app on your phone:

1. Go to **Settings**
2. Find **Server URL** setting (we'll add this)
3. Paste: `https://abc123.ngrok.io/api/`
4. Enable **Use Local Server**

---

## Alternative: Manual Setup

If you prefer to run them separately:

```bash
# Terminal 1 - Start the server
cd anyserver
npm run dev

# Terminal 2 - Start Ngrok
ngrok http 3000
```

---

## Important Notes

- **Free Ngrok URLs change** every time you restart Ngrok
- For a **permanent URL**, sign up for a free Ngrok account
- The server now listens on `0.0.0.0:3000` to accept external connections
- Make sure your firewall allows connections on port 3000

---

## Next Steps: Deploy to Render/Railway

Once everything works with Ngrok, we'll deploy to a permanent cloud server:

- **Render.com** - Free tier available
- **Railway.app** - Free tier with $5 credit/month
- No need to keep your computer running!
