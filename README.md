# Auth App Backend

Professional, production-ready authentication microservice built with Spring Boot.

## Overview

This repository contains the backend for a full-stack authentication module. It provides:

- Local username/password authentication with JWT access + refresh tokens
- OAuth2 login (Google & GitHub) using Spring Security OAuth2 client
- Refresh token lifecycle with secure, revocable refresh tokens persisted in the database
- Cookie-based refresh token support and CSRF-safe endpoints
- REST endpoints for user CRUD and auth (login, register, refresh, logout)

## Technology

- Java 17
- Spring Boot (3.x)
- Spring Security (oauth2-client, security)
- Spring Data JPA (MySQL)
- jjwt for JWT handling
- ModelMapper
- Lombok (compile-time)
- Maven build (wrapper included)

## Key features

- Access tokens (short-lived) and refresh tokens (long-lived) with server-side revocation
- OAuth2 Authorization Code flow for Google and GitHub
- Stateless JWT validation for API requests and optional session for OAuth2 flows
- Secure refresh cookie with configurable SameSite and HttpOnly flags

## Repository layout (important files)

- `src/main/java/com/app/auth/config/SecurityConfig.java` — Security setup (HTTP rules, OAuth2 config)
- `src/main/java/com/app/auth/Security/JwtService.java` — JWT creation and parsing
- `src/main/java/com/app/auth/Security/JwtAuthenticationFilter.java` — Bearer token validation filter
- `src/main/java/com/app/auth/controllers/AuthController.java` — Login, register, refresh, logout
- `src/main/java/com/app/auth/controllers/UserController.java` — User CRUD endpoints
- `src/main/resources/application-dev.yaml` — Development configuration (DB, OAuth2 client registration)

## Requirements

- Java 17 (or compatible JDK)
- Maven (or use the included Maven wrapper `mvnw` / `mvnw.cmd`)
- MySQL (or change datasource to your DB)

## Environment & Configuration

This service reads configuration from `src/main/resources/application-{profile}.yaml` and environment variables. Sensitive values should be provided through environment variables or a secrets manager.

Important environment variables (recommended):

- `MYSQL_URL` — JDBC URL for the database (or set in `application-*.yaml`)
- `MYSQL_USER`, `MYSQL_PASSWORD`
- `JWT_SECRET` — secret used to sign JWTs (REQUIRED in production)
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` — Google OAuth2 credentials
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` — GitHub OAuth2 credentials

Note: Do not commit secrets to the repository. The example `application-dev.yaml` in this project may contain placeholder defaults — override them via env vars.

### OAuth2 Redirect URIs

When registering OAuth2 clients (Google/GitHub) use the redirect URI:

```
http://localhost:8082/login/oauth2/code/{registrationId}
```

For local testing with Google set `{registrationId}` to `google`.

## Build & Run (local)

Use the included Maven wrapper to build and run locally.

Windows (PowerShell):

```powershell
cd "d:\Auth App\auth-app-backend"
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run
```

Or using the packaged jar:

```powershell
java -jar target/auth-app-backend-0.0.1-SNAPSHOT.jar
```

## Database

By default the development profile expects a MySQL database. Configure database connection in `application-dev.yaml` or provide env vars. Example:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db
    username: root
    password: root
```

## Exposed Endpoints

- `POST /api/v1/auth/register` — Register a new user (body: UserDto)
- `POST /api/v1/auth/login` — Login with email/password (returns access + refresh token; refresh cookie attached)
- `POST /api/v1/auth/refresh` — Exchange refresh token for new tokens
- `POST /api/v1/auth/logout` — Revoke refresh token and clear cookie
- `POST /api/v1/users` — Create a user (User CRUD)
- `GET /api/v1/users` — List users
- `GET /api/v1/users/{id}` — Get user by ID
- OAuth2 login: `GET /oauth2/authorization/google` (or `/oauth2/authorization/github`) — start OAuth2 flow

Success and failure redirects for oauth are configured in `application-dev.yaml` under `app.auth.frontend`.

## OAuth2 Notes

- The app uses the Authorization Code flow via Spring Security's OAuth2 client. Sessions are required for the authorization flow; the app config supports that for OAuth2 while still using JWTs for API auth.
- Ensure the OAuth2 client credentials and redirect URIs are correctly configured in the provider console (Google Cloud Console, GitHub App settings).

## Security

- Access tokens are short-lived and validated via `JwtAuthenticationFilter` (expects `Authorization: Bearer <token>`)
- Refresh tokens are stored in DB (`RefreshToken` entity) and can be revoked or rotated
- Refresh tokens are optionally stored in an HttpOnly cookie by the `CookieService`

## Development tips

- Use environment profiles (e.g., `--spring.profiles.active=dev`) to separate dev and prod settings.
- Do not store secrets in source control — use env vars or secret managers.
- If you update the Java version in `pom.xml`, ensure your local JDK matches the `java.version` property.

## Testing

- Unit tests are present under `src/test/java` using Spring Boot Test.
- Use `mvnw.cmd test` to run tests locally.

## Contributing

- Fork → feature branch → open PR to `auth-backend` branch
- Include unit tests for new features and keep changes focused

## Troubleshooting

- 401 / InsufficientAuthenticationException during OAuth2 callback: confirm `sessionCreationPolicy` allows sessions for OAuth2 and your redirect URI is registered correctly.
- Invalid client authentication at token exchange: verify `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` (set via env vars) and `redirect-uri` exactly matches the provider registration.

## License

This project does not include an explicit license in the repository. Add one if you intend to make this public.

---

If you want, I can also:

- Add a `.env.example` with the required environment variables
- Create a Dockerfile + `docker-compose.yml` for local DB + app
- Add CI workflow to run tests and build artifacts

