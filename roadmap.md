

## Phase 0 — Stabilize the foundation

Goal: make the existing registry production-ready enough to support inference.

Build:

- Complete agent CRUD
- Provider and model entities
- Versioned database migrations
- Stable error schema
- OpenAPI documentation
- Request correlation IDs
- Configuration validation
- Integration tests with PostgreSQL
- Health and readiness endpoints

Add these core entities:

```text
Provider
Model
ProviderCredential
RoutingPolicy
UsageRecord
Budget
AuditEvent
```

Done when:

- The application starts reliably in every profile
- Database schema is migration-controlled
- Agent, provider, and model configuration can be tested through APIs
- API contracts are documented

## Phase 1 — Single-provider inference

Goal: deliver the first useful gateway path.

Build:

```text
POST /api/v1/responses
```

Flow:

```text
request
  → resolve agent
  → resolve model
  → resolve provider credential
  → invoke provider adapter
  → normalize response
  → record usage
  → return response
```

Features:

- Synchronous text generation
- Request validation
- Provider adapter interface
- Provider timeout
- Normalized provider errors
- Token usage recording
- Basic cost estimation
- Request and response IDs
- Idempotency support
- Redacted logging

Keep the adapter interface provider-neutral:

```java
interface AiProvider {
    AiResponse generate(AiRequest request);
    Flux<AiEvent> stream(AiRequest request);
}
```

Done when:

- A client can call Aether without knowing the provider
- Provider-specific errors are converted into stable gateway errors
- Every request produces a usage record
- Failed provider calls do not leave inconsistent state

## Phase 2 — Streaming and observability

Goal: make the gateway usable for real applications.

Build:

- Server-sent event streaming
- Time-to-first-token measurement
- Total response latency
- Provider-call tracing
- Token and cost metrics
- Structured logs
- OpenTelemetry integration
- Request cancellation handling
- Maximum response duration
- Maximum output token enforcement

Track:

- `provider`
- `model`
- `agent`
- `team`
- `environment`
- `input_tokens`
- `output_tokens`
- `cached_tokens`
- `latency_ms`
- `time_to_first_token_ms`
- `status`
- `estimated_cost`

OpenTelemetry’s GenAI conventions already define standard concepts for model, token usage, messages, and data sources. [OpenTelemetry GenAI conventions](https://opentelemetry.io/docs/specs/semconv/registry/attributes/gen-ai/)

Done when:

- Streaming works reliably
- Operators can identify slow providers and expensive agents
- Prompt and response content is not logged accidentally

## Phase 3 — Multi-provider routing

Goal: make Aether valuable as a gateway rather than a proxy.

Build:

- Multiple provider adapters
- Model aliases such as `fast`, `balanced`, and `reasoning`
- Provider capability registry
- Routing policies
- Provider health checks
- Retry policies
- Exponential backoff
- Circuit breakers
- Fallback providers
- Load shedding
- Rate-limit-aware routing

Example routing policy:

```yaml
agent: jira-agent
environment: production
requirements:
  max_latency_ms: 3000
  max_cost_per_request: 0.05
  capabilities:
    - tool_calling
providers:
  - provider: primary
    model: balanced
  - provider: fallback
    model: fast
```

Do not retry blindly. Only retry safe transient failures, especially when tool calls or external side effects are involved. Provider rate limits commonly involve both request and token dimensions. [OpenAI rate limits](https://developers.openai.com/api/docs/guides/rate-limits), [Anthropic rate limits](https://platform.claude.com/docs/en/api/rate-limits)

Done when:

- A provider outage does not automatically become an application outage
- Routing can be changed through configuration
- The same client contract works across providers

## Phase 4 — Security and governance

Goal: make the gateway safe for internal or external production use.

Build:

- Spring Security authentication
- Agent-level authorization
- Tenant isolation
- Role-based access control
- Model allowlists
- Provider allowlists
- API key or workload identity support
- Credential rotation
- Vault/KMS integration
- Audit events
- Prompt/response redaction
- Data retention policies
- Data residency controls
- Per-agent and per-team permissions

This phase should happen before exposing the gateway outside a trusted network.

Also add safety policies for:

- Prompt injection
- Sensitive information disclosure
- Excessive tool permissions
- Unsafe output handling
- Unbounded token consumption

These risks are highlighted in the OWASP Top 10 for LLM Applications. [OWASP LLM Top 10](https://genai.owasp.org/resource/owasp-top-10-for-llm-applications-2025/)

Done when:

- Every request has an authenticated principal
- Every action is authorized
- Provider secrets never appear in application logs
- Audit records explain who used which model and why

## Phase 5 — Budgets, quotas, and chargeback

Goal: control cost and prevent accidental or abusive consumption.

Build:

- Per-request token limits
- Per-agent quotas
- Per-team quotas
- Monthly budgets
- Daily spend limits
- Rate limits by request count
- Rate limits by token count
- Cost dashboards
- Budget alerts
- Hard and soft budget thresholds
- Chargeback reports

Example:

```text
organization budget
  → team budget
      → agent budget
          → request limit
```

Done when:

- A runaway agent cannot consume unlimited budget
- Costs can be attributed to agents and teams
- Requests can be rejected before reaching a provider when budget is exhausted

## Phase 6 — Structured output and tools

Goal: support agent workflows rather than simple text generation.

Build:

- JSON Schema response formats
- Function calling
- Tool registry
- Tool ownership
- Tool authorization
- Tool argument validation
- Tool timeouts
- Tool result size limits
- Maximum tool-call count
- Human approval for destructive tools
- Tool-call audit events

Later add MCP support. Current provider APIs support custom functions, hosted tools, and MCP integrations, but these require strong permission and data controls. [OpenAI tools](https://developers.openai.com/api/docs/guides/tools), [Anthropic tool use](https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview)

Done when:

- Agents can call approved tools safely
- Invalid tool arguments are rejected before execution
- Destructive actions require explicit approval
- Tool calls appear in traces and audit logs

## Phase 7 — Multimodal and asynchronous workloads

Goal: expand beyond synchronous text requests.

Build:

- Image inputs
- File inputs
- Audio transcription
- Text-to-speech
- Background jobs
- Job status endpoints
- Webhook delivery
- Batch processing
- Large-file storage
- Payload size limits

Use asynchronous execution for long-running operations rather than keeping HTTP connections open indefinitely.

Done when:

- Large or slow requests do not exhaust web threads
- Clients can safely poll or receive completion events
- Files have explicit retention and access policies

## Phase 8 — Evaluation and release management

Goal: improve quality systematically.

Build:

- Golden test datasets
- Prompt versioning
- Model comparison
- Automated evaluators
- Human feedback
- Regression detection
- Shadow traffic
- Canary routing
- Rollback support
- Quality and cost scorecards

A model change should be evaluated across:

```text
quality
cost
latency
safety
tool success rate
failure rate
```

Done when:

- Model changes can be tested before production
- Routing changes can be rolled back
- Quality regressions are detected automatically

## Recommended implementation order

For your repository, I would use this sequence:

1. Provider/model configuration
2. Canonical inference request and response types
3. One provider adapter
4. `POST /api/v1/responses`
5. Usage and cost recording
6. Streaming
7. OpenTelemetry
8. Timeouts and retries
9. Second provider
10. Routing and fallback
11. Authentication and authorization
12. Budgets and quotas
13. Tools and structured output
14. Evaluation and canary releases

## Features to postpone

Do not start with:

- Multi-agent orchestration
- Your own vector database
- Complex prompt marketplace
- Full dashboard
- Autonomous tool execution
- Fine-tuning management
- Custom semantic cache
- Voice/realtime support

Those features are useful later, but the gateway’s first proof of value should be:

> One stable API, multiple providers, reliable routing, controlled cost, and complete observability.