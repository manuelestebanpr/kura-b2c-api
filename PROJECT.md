# KURA B2C API — Project Instructions

## Role
Patient-facing API. High traffic, read-heavy. Handles: search, auth, checkout, orders, share links.

## Architecture
- **Spring Boot 4.0** / Java 25 / PostgreSQL 16+ / Redis
- **Port:** 8081
- **Shares DB** with `kura-enterprise-api` (no Flyway here — enterprise owns migrations)
- **Stateless:** No JVM sessions. JWT placeholder (Phase 2: HttpOnly cookie SSO)
- **Package:** `co.com.kura.b2c`

## Endpoints
| Module | Base Path | Description |
|--------|-----------|-------------|
| Auth | `/api/v1/auth` | OTP, register (RNEC mock), login, password reset |
| Search | `/api/v1/search` | pg_trgm fuzzy search, Redis cached |
| Commerce | `/api/v1/orders` | Create order, walk-in ticket (15 days), mock MercadoPago |
| Share | `/api/v1/share` | Public 48h expiring result links |
| Health | `/api/v1/ping` | Healthcheck |

## Mocked Integrations
1. AWS SES → `MockSesEmailProvider` (logs to console)
2. RNEC → `MockRnecProvider` (always MATCH)
3. MercadoPago → `MockMercadoPagoProvider` (fake preference ID)

## Standards
- DRY/SOLID, Clean Architecture
- Soft delete (`deleted_at`), audit logging
- BCrypt passwords, parameterized queries
- `@Cacheable` for search, `Cache-Control` headers
- Colombia compliance: Ley 1581 consent, locked cedula/name

## Frontend
Consumed by `kura-b2c-web` (Angular 20, port 4200)
