# How to run Project Analytics (Xtensus)

Simple steps to open the app on your computer and connect OpenProject.

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

You need a file named **`.env`** at the **root** of the project (same level as `README.md`).

### Case A — You got the project as a **zip / email**

The `.env` file is usually **already there**.

1. Open `.env` in a text editor.
2. Set (or fix) these two lines for your OpenProject:

```text
OPENPROJECT_URL=https://your-openproject-address
OPENPROJECT_API_KEY=your-token-here
```

3. Save the file. Done — do not recreate it from the example unless the file is missing.

### Case B — You got the project from **GitHub** (clone / pull)

GitHub **does not** include `.env` (secrets are not committed). You must create it:

```bash
cp .env.example .env
```

Then edit `.env` and set at least:

```text
OPENPROJECT_URL=https://your-openproject-address
OPENPROJECT_API_KEY=your-token-here
```

### Remember

- The website **Connections** screen only stores the OpenProject **URL**.  
- The **API key** always comes from `.env` (loaded when you start the backend in Step 3).  
- After any change to `.env`, **restart** the backend script.

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
- If it says the API key is **not** configured → fix `.env` and run the script again.

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

## Step 5 — Sign up or log in

You can **Sign up** with email/password on the auth page (creates a Project Analytics account only — no OpenProject access yet).

For local admin/demo:

| Field    | Value        |
|----------|--------------|
| Username | `admin`      |
| Password | `Admin123!`  |

Login also accepts the admin **email** `admin@projectanalytics.local`.

After signup you land on **Connections**. Analytics access requires connecting OpenProject and passing eligibility (M14) / grants (M15).

---

## Step 6 — Connect OpenProject and sync

1. Go to **Connections** in the menu.
2. Connect a workspace.
3. Set the **Base URL** to your OpenProject address (same as in the browser, e.g. `https://….openproject.eu`).
4. Click **Synchronize** (this copies OpenProject into local data and **drops** projects/work packages that were deleted in OpenProject).
5. When sync succeeds, open **Home** or **Explorer**.

The **API key is not typed in the UI**. It only comes from the `.env` file (Step 1 + Step 3).

**Home → Recalculate** only refreshes scores from data already stored locally. After deleting something in OpenProject, run **Synchronize** again — Recalculate alone will not remove it.

---

## If something goes wrong

| What you see | What to do |
|--------------|------------|
| API key not configured | Use `./scripts/run-backend.sh` and check `.env` |
| Sync fails with 401 | API key wrong or expired — update `.env`, restart Step 3 |
| Cannot open the page | Is Step 4 still running? Is the URL http://localhost:4200 ? |
| Port already in use | Close the old backend/frontend and start again |

---

## Optional reading

- Full test checklist: `docs/ops/demo-happy-path.md`
- Known limits of this version: `docs/ops/known-limitations.md`
