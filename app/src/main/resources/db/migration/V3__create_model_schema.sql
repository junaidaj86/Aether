CREATE TABLE models
(
    id                  UUID         NOT NULL,
    provider_id         UUID         NOT NULL,
    name                VARCHAR(100) NOT NULL,
    provider_model_id   VARCHAR(255) NOT NULL,
    status              VARCHAR(50)  NOT NULL,
    context_window      INTEGER,
    max_output_tokens   INTEGER,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_models
        PRIMARY KEY (id),

    CONSTRAINT fk_model_provider
        FOREIGN KEY (provider_id)
        REFERENCES providers (id),

    CONSTRAINT uk_model_provider_model_id
        UNIQUE (provider_id, provider_model_id),

    CONSTRAINT chk_model_context_window
        CHECK (
            context_window IS NULL
            OR context_window > 0
        ),

    CONSTRAINT chk_model_max_output_tokens
        CHECK (
            max_output_tokens IS NULL
            OR max_output_tokens > 0
        ),

    CONSTRAINT chk_model_token_limits
        CHECK (
            context_window IS NULL
            OR max_output_tokens IS NULL
            OR max_output_tokens <= context_window
        )
);


CREATE INDEX idx_models_provider_id
    ON models (provider_id);


CREATE TABLE model_capabilities
(
    model_id      UUID        NOT NULL,
    capability    VARCHAR(50) NOT NULL,

    CONSTRAINT pk_model_capabilities
        PRIMARY KEY (model_id, capability),

    CONSTRAINT fk_model_capability_model
        FOREIGN KEY (model_id)
        REFERENCES models (id)
        ON DELETE CASCADE
);