# BONE88 Social Manager

Android app and Cloudflare Worker for sending approved Telegram posts.

## Telegram Worker deployment

Worker source: [cloudflare/worker.js](cloudflare/worker.js)

1. In Cloudflare, open **Workers & Pages** and create a Worker named `bone88-social-manager`.
2. Replace the starter code with the contents of `cloudflare/worker.js`, then deploy.
3. Open the Worker **Settings → Variables and Secrets** and add these as **Secrets** (not plain text variables):
   - `TELEGRAM_BOT_TOKEN`: token from BotFather.
   - `TELEGRAM_CHAT_ID`: numeric ID of the destination chat/channel. Add the bot to the channel and grant permission to post.
   - `BONE88_API_KEY`: a long random secret key of your own. Use the exact same value in the Android app.
4. Copy the Worker URL (for example, `https://bone88-social-manager.<your-subdomain>.workers.dev`).
5. In BONE88 → Settings, save the Worker URL and the same API key.
6. Open `<WORKER_URL>/health`. It should return `ok: true` and `configured: true`.
7. Create a Telegram draft in BONE88 and press **Approve and send**. The message is sent only after manual approval.

## Security

Never put the Telegram bot token in the Android app or in this Git repository. Keep all three values in Cloudflare Worker Secrets. The app sends only the separate `BONE88_API_KEY` in the HTTPS request header. If that key is exposed, rotate it in Cloudflare and in the app.

## Current integration status

- Telegram: Worker endpoint implemented; requires the three Cloudflare secrets and a real end-to-end test.
- TikTok, Instagram and YouTube: not connected yet; each requires its own official API authorization and platform permissions.
- Drafts and settings are stored locally on the Android device.
