# Aether


Yes. If you mean the commands to log into the Keycloak CLI (kcadm.sh) and retrieve the actual client secret, use these.

1. Log in to Keycloak CLI

Your container is aether-keycloak:

docker exec -it aether-keycloak \
  /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin

If your local Keycloak admin password isn’t admin, replace the last value.

2. Find aether-gateway-client

docker exec -it aether-keycloak \
  /opt/keycloak/bin/kcadm.sh get clients \
  -r aether \
  -q clientId=aether-gateway-client

You should see the client configuration.

3. Get its internal Keycloak UUID

CLIENT_UUID=$(docker exec aether-keycloak \
  /opt/keycloak/bin/kcadm.sh get clients \
  -r aether \
  -q clientId=aether-gateway-client \
  --fields id \
  --format csv \
  --noquotes | tail -1)
echo "$CLIENT_UUID"

For your current client, you previously got:

070d720b-ca56-4f5d-ab47-87e13718017d

But using the command is better than hardcoding it.

4. Retrieve the actual client secret

docker exec aether-keycloak \
  /opt/keycloak/bin/kcadm.sh get \
  "clients/$CLIENT_UUID/client-secret" \
  -r aether

It returns:

{
  "type": "secret",
  "value": "..."
}

Don’t paste that value into chat or commit it.

5. Load it directly into your shell

If you have jq, you can avoid displaying/copying the secret:

export AETHER_CLIENT_SECRET=$(
  docker exec aether-keycloak \
    /opt/keycloak/bin/kcadm.sh get \
    "clients/$CLIENT_UUID/client-secret" \
    -r aether |
  jq -r '.value'
)

Verify only the length:

echo ${#AETHER_CLIENT_SECRET}

6. Request an access token

TOKEN=$(curl -s -X POST \
  'http://localhost:8180/realms/aether/protocol/openid-connect/token' \
  -u "aether-gateway-client:$AETHER_CLIENT_SECRET" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials' \
  | jq -r '.access_token')

Verify you received one without printing it:

echo ${#TOKEN}

Then you can call Aether with:

curl -i \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/providers

So your complete local workflow is now:

kcadm login
    ↓
Find client UUID
    ↓
Retrieve actual secret
    ↓
AETHER_CLIENT_SECRET
    ↓
Request JWT
    ↓
TOKEN
    ↓
Call Aether

This also avoids the secret mismatch that caused your earlier 401 invalid_client_credentials.