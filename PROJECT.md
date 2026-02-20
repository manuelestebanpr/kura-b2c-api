# KURA B2C API — Project Architecture & Status

## Role
Patient-facing API. High traffic, read-heavy. Handles: search, share links, and read-only catalog browsing.

> **Note (Sprint 0 Refactor):** Auth, Orders, Commerce, and Payment logic were moved to `kura-enterprise-api` to enforce the Dual-Backend architecture. The B2C API is now a lean, read-only gateway for patient traffic.

## Architecture
- **Spring Boot 4.0** / Java 25 / PostgreSQL 16+ / Redis
- **Port:** 8081
- **Shares DB** with `kura-enterprise-api` (no Flyway here — enterprise owns all migrations)
- **Stateless:** No JVM sessions. HttpOnly cookie SSO (root-domain scoped)
- **Package:** `co.com.kura.b2c`
- **Jackson:** Jackson 3 (`tools.jackson.databind`) — Spring Boot 4.0 default
- **Cache Serializer:** `GenericJacksonJsonRedisSerializer` (Jackson 3 builder pattern)

## Current Endpoints (Post-Refactor)
| Module | Base Path | Description | Status |
|--------|-----------|-------------|--------|
| Search | `/api/v1/search` | pg_trgm fuzzy search by service name, Redis cached | ✅ Implemented |
| Share  | `/api/v1/share/{uuid}` | Public 48h expiring result links | ✅ Implemented |
| Health | `/api/v1/ping` | Healthcheck | ✅ Implemented |

## Endpoints Moved to Enterprise API
| Module | Enterprise Path | Reason |
|--------|-----------------|--------|
| Auth   | `/api/v1/auth/*` | OTP, register, login, password reset — single auth source |
| Commerce | `/api/v1/orders/*` | Order creation, walk-in tickets, payments |

## Entity Mapping (B2C — Read-Only Subset)
| Entity | Table | Notes |
|--------|-------|-------|
| `MasterService` | `master_services` | Catalog browsing |
| `BundleItem` | `bundle_items` | Bundle composition |
| `LabOffering` | `lab_offerings` | PoS-specific pricing |
| `PointOfService` | `points_of_service` | Lab locations |
| `PatientResult` | `patient_results` | `result_data` is JSONB |
| `ShareLink` | `share_links` | 48h expiring UUID links |

## Configuration
```yaml
# application.yml
server.port: 8081
spring.jpa.hibernate.ddl-auto: validate  # Enterprise owns migrations
spring.flyway.enabled: false
spring.data.redis.host: localhost:6379
kura.cors.allowed-origins: http://localhost:4200,http://localhost:4201
```

## Mocked Integrations (Moved to Enterprise API)
All mock integrations now live in `kura-enterprise-api`:
1. **AWS SES** → `MockSesEmailProvider` (logs to console)
2. **RNEC** → `MockRnecProvider` (always returns MATCH)
3. **MercadoPago** → `MockMercadoPagoProvider` (fake preference ID)
4. **AWS S3** → Audio saved to local `/tmp`

## Standards Enforced
- DRY/SOLID, Clean Architecture, `@ControllerAdvice` global exception handler
- Soft delete (`deleted_at`), immutable audit logging
- BCrypt passwords, parameterized queries (JPA)
- `@Cacheable` for search results (15 min TTL), `Cache-Control` edge headers
- Colombia compliance: Ley 1581 consent tracking, locked cedula/name post-registration
- 15-year data retention (soft delete + future Glacier archive)

## Frontend
Consumed by `kura-b2c-web` (Angular 20, port 4200)

---

## Full Ecosystem — 4 Repositories

### 1. `kura-b2c-web` (Angular 20 — Port 4200)
Patient commerce portal & B2B landing page.
- i18n: `@ngx-translate/core` v17 (ES/EN), files in `src/assets/i18n/`
- Pages: Home, Search, Checkout, Orders, Login, Register, Share
- Design: Tailwind CSS, Mobile-first, Trust Medical Blue (#0ea5e9)
- Lab Portal link in footer → `kura-workspace-web`

### 2. `kura-workspace-web` (Angular 20 — Port 4201)
B2B Marketplace & Lab Backoffice portal.
- i18n: `@ngx-translate/core` v17 (ES/EN), files in `public/assets/i18n/`
- Pages: Dashboard, Catalog, Inventory, Orders, Results, Patient Import
- Sidebar layout with navigation

### 3. `kura-b2c-api` (Spring Boot 4.0 — Port 8081) ← THIS REPO
Patient-facing read-only API. See above.

### 4. `kura-enterprise-api` (Spring Boot 4.0 — Port 8080)
Lab/Admin traffic API. Owns all Flyway migrations.
- Auth: OTP → Redis (5 min TTL), Mock SES email, RNEC mock registration
- Catalog: CRUD for master services, bundles, lab offerings, test dependencies
- Commerce: Orders, walk-in tickets (15-day expiry), Mock MercadoPago
- Results: CRUD, SAMPLE_TAKEN → auto stock deduction via BOM
- Import: CSV patient import with PostgreSQL UPSERT
- Share: 48h expiring result links

### Database (Shared — PostgreSQL 16+)
All 4 Flyway migrations in `kura-enterprise-api`:
| Migration | Content |
|-----------|---------|
| V1 | `users`, `laboratories`, `points_of_service`, `warehouse_inventory`, `audit_logs` |
| V2 | `master_services`, `bundle_items`, `lab_offerings`, `test_dependencies` |
| V3 | `orders`, `order_items`, `walkin_tickets`, `payments` |
| V4 | `patient_results`, `share_links` |
| V5 | Seed data: 1 lab, 2 PoS, 2 users, 12 services, offerings, inventory |

### SSO Architecture
- Unified `users` table shared by both APIs
- Root-domain scoped HttpOnly cookie (`Domain=.kura.com.co; SameSite=Lax`)
- Single login works across B2C (4200) and Workspace (4201)

---

## How to Test

### Prerequisites
```bash
# PostgreSQL 16+ running with database 'kura'
# Redis running on localhost:6379
```

### Start APIs
```bash
# Terminal 1: Enterprise API (runs Flyway migrations + seeds data)
cd kura-enterprise-api && mvn spring-boot:run  # Port 8080

# Terminal 2: B2C API
cd kura-b2c-api && mvn spring-boot:run  # Port 8081
```

### Start Frontends
```bash
# Terminal 3: B2C Web
cd kura-b2c-web && npm install && ng serve --port 4200

# Terminal 4: Workspace Web
cd kura-workspace-web && npm install && ng serve --port 4201
```

### Test Credentials (from V5 seed migration)
| Role | Email | Password |
|------|-------|----------|
| Super Admin | `admin@kura.com.co` | `Admin123!` |
| Patient | `paciente@test.co` | `Admin123!` |

### Test Scenarios
1. **Search (B2C):** Go to `http://localhost:4200/search`, type "hemograma" or "perfil" — should return services from seed data via pg_trgm
2. **Auth (Enterprise):** POST `http://localhost:8080/api/v1/auth/login` with admin credentials — should return JWT/cookie
3. **SSO:** Login on B2C (4200), then visit Workspace (4201) — should be authenticated
4. **Catalog (Workspace):** Go to `http://localhost:4201/catalog` — should show 12 seeded services
5. **Orders (Workspace):** Go to `http://localhost:4201/orders` — order management
6. **Share Link:** Create a result + share link via Enterprise API, then access via `http://localhost:4200/share/{uuid}`
