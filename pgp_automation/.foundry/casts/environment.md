# environment contract

- all required configuration keys must be listed in an example file.
  - example env file path: `src/main/resources/profiles/<profile>/localconfig.properties` (one per Maven profile: ite, qa5, qa7, qa8, qa11, qa12, qa14, staging, hotfix, sandbox, automation, dev2, mix-hotfix, merchantStaging, local)
- local dev must not require production secrets.
- if the system depends on external services (db, cache, queues, etc.), provide one of:
  - a single command that starts dependencies locally (containerized or otherwise), or
  - clear setup instructions in the runbook.

| variable | required | description | example |
|----------|----------|-------------|---------|
| PGP_HOST | yes | PGP service base URL | `https://pgp-ite.paytm.in` |
| ENV_NAME | yes | Target environment name | `ITE` |
| PROFILE | yes | Active Maven profile name | `ite` |
| PGP_DB_CONNECTION_URL | yes | PGP database JDBC connection string | `jdbc:mysql://host:3306/PGPDB?user=...&password=...` |
| AUTH_HOST | yes | Auth service base URL | `https://accounts-staging.paytm.in` |
| WALLET_HOST | no | Wallet service base URL | `https://wallet-integration.paytmbank.com` |
| MOCK_HOST | no | Mock server base URL | `https://automation-pg-ext.paytm.in` |
| KAFKA_SERVER | no | Kafka broker address | `host:9092` |
| MONGO_DB_URI | no | MongoDB connection URI | `mongodb://user:pass@host:27017/dbname` |

## rules

- never hardcode secrets. never commit `.env`.
- if a required env var is missing, fail loudly -- do not silently default.
