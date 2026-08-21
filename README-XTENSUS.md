# How to run Project Analytics (Xtensus)

Simple steps to open the app on your computer and connect OpenProject.

**Production / customer deploy:** start at [`docs/ops/DEPLOY.md`](docs/ops/DEPLOY.md)  
(prod Compose overlay, env template, backup, security checklist, demo walkthrough).

---

## What you need installed

1. **Docker Desktop** (must be running)
2. **Java 21** and **Maven**
3. **Node.js** (version 20 or 22 is fine)

On **Windows**: install **WSL2** (Ubuntu) and run the commands **inside WSL**, not in plain PowerShell.  
On **Mac or Linux**: use the normal Terminal.

---

## Folder

Open a terminal in the project folder (the one that contains `scripts`, `backend`, `frontend`, `docker`).

---

## Step 1 — Configuration file (`.env`)

GitHub **does not** include `.env` (secrets are not committed). Create one from the example:

```bash
cp .env.example .env
```

You do **not** need to put an OpenProject URL or API key in `.env`. Those are entered later on the **Connections** screen (OAuth client id/secret, or an API key as an alternative).

The example file already has working local defaults (database, JWT, OAuth redirect to `http://localhost:8080/...`). Save `.env` at the **root** of the project (same level as `README.md`).

---

## Step 2 — Start the database

```bash
cd docker
docker compose --env-file ../.env -f docker-compose.yml up -d postgres redis
cd ..
```

Wait a few seconds. Docker must stay running.

---

## Step 3 — Start the backend

```bash
./scripts/run-backend.sh
```

- Leave this window **open** (the app backend is this process).
- Wait until you see something like “Started ProjectAnalyticsApplication”.

**Do not** start the backend with only `mvn spring-boot:run` (that ignores `.env`).

---

## Step 4 — Start the frontend (new terminal)

Open a **second** terminal in the project folder:

```bash
cd frontend
npm install
npm start
```

When it is ready, open a browser:

**http://localhost:4200**

---

## Step 5 — Sign up

On the auth page, use **Sign up** (email + password). That creates a Project Analytics account only — it does **not** connect OpenProject yet.

You must **confirm your email** before you can sign in. With the default local `.env` (`PASSWORD_RESET_MAIL_ENABLED=false`), the confirmation link is printed in the **backend terminal**. Open that link, then sign in.

After login you land on **Connections**. Analytics access requires connecting OpenProject (next step) and passing eligibility / grants.

---

## Step 6 — Connect OpenProject and sync

1. Go to **Connections** in the menu.
2. Enter your OpenProject **Base URL** (same as in the browser, e.g. `https://….openproject.com`).
3. **Preferred:** paste the OAuth **Client ID** and **Client secret** from OpenProject  
   (**Administration → Authentication → OAuth applications**). Use the redirect URI shown on the Connections screen.
4. Click **Connect with OpenProject OAuth**, then **Open OpenProject sign-in** and authorize.
5. After a successful connect, click **Synchronize** (this copies OpenProject data locally and **drops** projects/work packages that were deleted in OpenProject).
6. When sync succeeds, open **Home** or **Explorer**.

You can also **Use API key instead** on the same screen if you prefer an API token. Credentials are stored **server-side**, not in `.env`.

**Home → Recalculate** only refreshes scores from data already stored locally. After deleting something in OpenProject, run **Synchronize** again — Recalculate alone will not remove it.

---

## If something goes wrong

| What you see | What to do |
|--------------|------------|
| Backend does not start | Use `./scripts/run-backend.sh` and check that `.env` exists at the project root |
| OAuth / connect fails | Check Base URL, client id/secret, and that the OpenProject OAuth app uses the redirect URI shown on Connections |
| Sync fails with 401 | Reconnect the workspace (OAuth or API key) on Connections, then Synchronize again |
| Cannot open the page | Is Step 4 still running? Is the URL http://localhost:4200 ? |
| Port already in use | Close the old backend/frontend and start again |

---

## Optional reading

- Full test checklist: `docs/ops/demo-happy-path.md`
- Known limits of this version: `docs/ops/known-limitations.md`
