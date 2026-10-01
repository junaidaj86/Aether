CREATE TABLE providers (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_providers
        PRIMARY KEY (id),

    CONSTRAINT uk_provider_name_environment
        UNIQUE (name, environment)
);