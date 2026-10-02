CREATE TABLE agents (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    owner VARCHAR(100) NOT NULL,
    team VARCHAR(100) NOT NULL,
    environment VARCHAR(255) NOT NULL,
    risk_level VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    identity_provider VARCHAR(100) NOT NULL,
    external_principal_id VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_agents PRIMARY KEY (id),
    CONSTRAINT uk_agent_name_environment UNIQUE (name, environment),
    CONSTRAINT uk_agent_external_identity UNIQUE (identity_provider, external_principal_id)
);

CREATE TABLE agent_credentials (
    id UUID NOT NULL,
    agent_id UUID NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    secret_hash VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_agent_credentials PRIMARY KEY (id),
    CONSTRAINT uk_agent_credentials_client_id UNIQUE (client_id),
    CONSTRAINT fk_agent_credentials_agent FOREIGN KEY (agent_id)
        REFERENCES agents (id) ON DELETE CASCADE
);
