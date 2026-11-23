CREATE TABLE match_idempotency (
    principal_id TEXT NOT NULL,
    operation VARCHAR(80) NOT NULL,
    operation_key UUID NOT NULL,
    request_fingerprint CHAR(64) NOT NULL,
    state VARCHAR(16) NOT NULL DEFAULT 'IN_PROGRESS',
    match_id BIGINT,
    response_body JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '24 hours'),

    PRIMARY KEY (principal_id, operation, operation_key),
    CONSTRAINT ck_match_idempotency_state CHECK (
        (state = 'IN_PROGRESS' AND match_id IS NULL AND response_body IS NULL)
        OR (
            state = 'COMPLETED'
            AND match_id IS NOT NULL
            AND response_body IS NOT NULL
            AND jsonb_typeof(response_body) = 'object'
        )
    )
);