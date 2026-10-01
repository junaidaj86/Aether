#!/usr/bin/env bash

set -e

BASE_URL="http://localhost:8080/api/v1/agent"

TIMESTAMP=$(date +%s)

AGENT_NAME="test-agent-$TIMESTAMP"
EXTERNAL_PRINCIPAL_ID="test-principal-$TIMESTAMP"
IDENTITY_PROVIDER="scania-entra"

echo "========================================="
echo " Aether Agent API Test"
echo "========================================="
echo


# ---------------------------------------------------------
# 1. REGISTER AGENT
# ---------------------------------------------------------

echo "1. Registering agent..."

CREATE_RESPONSE=$(curl -s -f \
  -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$AGENT_NAME\",
    \"description\": \"Test agent created by API test script\",
    \"owner\": \"junaid\",
    \"team\": \"platform-engineering\",
    \"environment\": \"DEVELOPMENT\",
    \"riskLevel\": \"MEDIUM\",
    \"identityProvider\": \"$IDENTITY_PROVIDER\",
    \"externalPrincipalId\": \"$EXTERNAL_PRINCIPAL_ID\"
  }")

echo "$CREATE_RESPONSE"
echo


# Extract UUID without requiring jq.
AGENT_ID=$(echo "$CREATE_RESPONSE" \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')

if [ -z "$AGENT_ID" ]; then
    echo "ERROR: Could not extract agent ID."
    exit 1
fi

echo "Created Agent ID: $AGENT_ID"
echo


# ---------------------------------------------------------
# 2. GET ALL AGENTS
# ---------------------------------------------------------

echo "2. Getting all agents..."

curl -s -f \
  -X GET "$BASE_URL"

echo
echo


# ---------------------------------------------------------
# 3. GET AGENT BY NAME
# ---------------------------------------------------------

echo "3. Getting agent by name..."

curl -s -f \
  -X GET "$BASE_URL/name?name=$AGENT_NAME"

echo
echo


# ---------------------------------------------------------
# 4. UPDATE AGENT
# ---------------------------------------------------------

echo "4. Updating agent..."

UPDATE_RESPONSE=$(curl -s -f \
  -X PUT "$BASE_URL?id=$AGENT_ID" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$AGENT_NAME\",
    \"description\": \"Updated by automated API test\",
    \"owner\": \"junaid\",
    \"team\": \"ai-platform\",
    \"environment\": \"STAGING\",
    \"riskLevel\": \"HIGH\",
    \"identityProvider\": \"$IDENTITY_PROVIDER\",
    \"externalPrincipalId\": \"$EXTERNAL_PRINCIPAL_ID\"
  }")

echo "$UPDATE_RESPONSE"
echo


# ---------------------------------------------------------
# 5. VERIFY UPDATE
# ---------------------------------------------------------

echo "5. Verifying update..."

curl -s -f \
  -X GET "$BASE_URL/name?name=$AGENT_NAME"

echo
echo


# ---------------------------------------------------------
# 6. DELETE AGENT
# ---------------------------------------------------------

echo "6. Deleting agent..."

curl -s -f \
  -X DELETE "$BASE_URL?id=$AGENT_ID"

echo
echo "Agent deleted."
echo


# ---------------------------------------------------------
# 7. GET ALL AFTER DELETE
# ---------------------------------------------------------

echo "7. Getting all agents after deletion..."

curl -s -f \
  -X GET "$BASE_URL"

echo
echo


echo "========================================="
echo " All Agent API tests completed successfully"
echo "========================================="