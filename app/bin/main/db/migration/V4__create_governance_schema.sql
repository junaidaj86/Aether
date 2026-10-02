CREATE TABLE provider_credentials (
    id UUID NOT NULL,
    provider_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    secret_ref VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    last_used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_provider_credentials PRIMARY KEY (id),
    CONSTRAINT uk_provider_credentials_provider_name UNIQUE (provider_id, name),
    CONSTRAINT fk_provider_credentials_provider FOREIGN KEY (provider_id)
        REFERENCES providers (id) ON DELETE CASCADE
);

CREATE INDEX idx_provider_credentials_provider_id ON provider_credentials (provider_id);

CREATE TABLE routing_policies (
    id UUID NOT NULL,
    agent_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    strategy VARCHAR(50) NOT NULL,
    max_latency_ms INTEGER,
    max_cost_per_request NUMERIC(19, 8),
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_routing_policies PRIMARY KEY (id),
    CONSTRAINT uk_routing_policy_agent_name UNIQUE (agent_id, name),
    CONSTRAINT fk_routing_policy_agent FOREIGN KEY (agent_id)
        REFERENCES agents (id) ON DELETE CASCADE,
    CONSTRAINT chk_routing_policy_latency CHECK (max_latency_ms IS NULL OR max_latency_ms > 0),
    CONSTRAINT chk_routing_policy_cost CHECK (max_cost_per_request IS NULL OR max_cost_per_request >= 0)
);

CREATE INDEX idx_routing_policies_agent_id ON routing_policies (agent_id);

CREATE TABLE usage_records (
    id UUID NOT NULL,
    trace_id VARCHAR(128),
    correlation_id VARCHAR(128) NOT NULL,
    execution_id VARCHAR(128),
    agent_id UUID,
    provider_id UUID,
    model_id UUID,
    team VARCHAR(100),
    environment VARCHAR(50),
    input_tokens BIGINT,
    output_tokens BIGINT,
    cached_tokens BIGINT,
    latency_ms BIGINT,
    time_to_first_token_ms BIGINT,
    status VARCHAR(30) NOT NULL,
    estimated_cost NUMERIC(19, 8),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_usage_records PRIMARY KEY (id)
);

CREATE INDEX idx_usage_records_correlation_id ON usage_records (correlation_id);
CREATE INDEX idx_usage_records_created_at ON usage_records (created_at);
CREATE INDEX idx_usage_records_agent_id ON usage_records (agent_id);

CREATE TABLE budgets (
    id UUID NOT NULL,
    scope VARCHAR(30) NOT NULL,
    scope_key VARCHAR(255) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    limit_amount NUMERIC(19, 8) NOT NULL,
    consumed_amount NUMERIC(19, 8) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_budgets PRIMARY KEY (id),
    CONSTRAINT uk_budget_scope_period UNIQUE (scope, scope_key, period_start, period_end),
    CONSTRAINT chk_budget_period CHECK (period_end >= period_start),
    CONSTRAINT chk_budget_limit CHECK (limit_amount >= 0),
    CONSTRAINT chk_budget_consumed CHECK (consumed_amount >= 0)
);

CREATE TABLE audit_events (
    id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    trace_id VARCHAR(128),
    correlation_id VARCHAR(128),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_audit_events PRIMARY KEY (id)
);

CREATE INDEX idx_audit_events_correlation_id ON audit_events (correlation_id);
CREATE INDEX idx_audit_events_created_at ON audit_events (created_at);
CREATE INDEX idx_audit_events_resource ON audit_events (resource_type, resource_id);
