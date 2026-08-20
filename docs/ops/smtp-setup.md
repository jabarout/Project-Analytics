# SMTP setup (Gmail)

Transactional mail powers **signup email confirmation** and **password reset**.  
Without SMTP, local/dev only logs the link when `PASSWORD_RESET_MAIL_ENABLED=false`.

Contact mailbox: `projectanalytics.contact@gmail.com`

---

## 1. Create a Google App Password

1. Sign in to Google as `projectanalytics.contact@gmail.com`
2. Enable **2-Step Verification** (required for App Passwords)
3. Google Account → **Security** → **App passwords**
4. Create an app password (e.g. “Project Analytics”)
5. Copy the 16-character password (spaces optional)

Never commit this password. Put it only in your private `.env` / `prod.env`.

---

## 2. Local `.env` (repo root)

```bash
PASSWORD_RESET_MAIL_ENABLED=true
PASSWORD_RESET_MAIL_FROM=projectanalytics.contact@gmail.com
PASSWORD_RESET_FRONTEND_BASE_URL=http://localhost:4200

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=projectanalytics.contact@gmail.com
MAIL_PASSWORD='xxxx xxxx xxxx xxxx'
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_SMTP_STARTTLS_REQUIRED=true
```

**Quote** `MAIL_PASSWORD` if it contains spaces (required when sourcing `.env` in bash).

Restart the backend with `./scripts/run-backend.sh` after saving.

---

## 3. Verify

**Password reset**

1. UI → Forgot password → enter a real inbox you control (or the contact Gmail)
2. Check that mailbox for “Project Analytics” reset mail
3. Open the link and set a new password

**Signup confirmation**

1. Register a new account with a real email
2. Confirm via the link in the mail before login

If mail is enabled but `MAIL_HOST` is missing, the API fails with a clear system error (JavaMailSender not configured).

---

## 4. Production (VPS)

Same variables in `prod.env` on the server. Set:

- `PASSWORD_RESET_FRONTEND_BASE_URL=https://YOUR_DOMAIN`
- `CORS_ALLOWED_ORIGINS=https://YOUR_DOMAIN`

See `docs/ops/vps-deploy.md`, `docs/ops/DEPLOY.md`, and `security-checklist.md`.
