# Local Keycloak

This directory provides a disposable Keycloak instance for local Aether development.

## Start

From the repository root:

```bash
docker compose -f infra/keycloak/docker-compose.yml up -d
```

Verify the configured issuer:

```bash
curl http://localhost:8180/realms/aether/.well-known/openid-configuration
```

The gateway configuration already points to:

```text
http://localhost:8180/realms/aether
```

The local administrator is `admin` / `admin` unless overridden with
`KEYCLOAK_ADMIN_USERNAME` and `KEYCLOAK_ADMIN_PASSWORD`.

The imported realm defines the `aether-gateway-client` confidential client and
the roles used by the gateway. The client secret is intentionally local-only:
`aether-gateway-secret`.

The client-credentials token includes the `aether-api` audience required by the
gateway. To authorize the client for API calls, open the Keycloak admin console,
select `aether` → `Clients` → `aether-gateway-client` → `Service account roles`,
and assign the minimum required realm role, such as `aether.registry.read`.

You can request a token with:

```bash
curl -X POST http://localhost:8180/realms/aether/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=client_credentials' \
  --data-urlencode 'client_id=aether-gateway-client' \
  --data-urlencode 'client_secret=aether-gateway-secret'
```

To stop Keycloak:

```bash
docker compose -f infra/keycloak/docker-compose.yml down
```

To remove the local Keycloak data as well:

```bash
docker compose -f infra/keycloak/docker-compose.yml down -v
```

This uses Keycloak development mode and must not be used as a production deployment.
