-- V1__create_tables.sql — Sistema de Mercado (PostgreSQL)
-- Schema consolidado da v1 (multi-tenant por business_id).

-- business
-- o schema permite NULL só nesse caso via CHECK em users.
CREATE TABLE business
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(150),          -- opcional; preenchido depois no perfil
    business_type VARCHAR(100) NOT NULL, -- informado no signup; texto livre
    city          VARCHAR(100),
    state         VARCHAR(2),
    address       VARCHAR(255),
    phone         VARCHAR(11),
    cnpj          VARCHAR(14),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    UUID,                  -- NULL apenas no signup (bootstrap)
    updated_by    UUID,                  -- NULL apenas no signup (bootstrap)

    CONSTRAINT ck_business_state_format CHECK (state IS NULL OR state ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_business_phone_format CHECK (phone IS NULL OR phone ~ '^[0-9]{10,11}$'),
    CONSTRAINT ck_business_cnpj_format CHECK (cnpj IS NULL OR cnpj ~ '^[0-9]{14}$'),
    CONSTRAINT ck_business_name_not_blank CHECK (name IS NULL OR btrim(name) <> '')
);

-- FKs para users são adicionadas depois da criação de users (ver final do arquivo).
CREATE INDEX idx_business_active ON business (active) WHERE active = TRUE;

-- users (admin e funcionário numa única tabela)
CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    business_id   UUID         NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    name          VARCHAR(150) NOT NULL,
    username      VARCHAR(50),                                   -- obrigatório para EMPLOYEE, opcional para ADMIN
    email         VARCHAR(150),                                  -- obrigatório para ADMIN, opcional para EMPLOYEE
    phone         VARCHAR(11),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    version       BIGINT       NOT NULL DEFAULT 0,               -- controle otimista (@Version)
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    UUID REFERENCES users (id) ON DELETE RESTRICT, -- NULL = signup público
    updated_by    UUID REFERENCES users (id) ON DELETE RESTRICT,

    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'EMPLOYEE')),
    CONSTRAINT ck_users_username_format CHECK (username IS NULL OR username ~ '^[a-zA-Z0-9._-]{3,50}$'),
    CONSTRAINT ck_users_phone_format CHECK (phone IS NULL OR phone ~ '^[0-9]{10,11}$'),
    -- ADMIN deve ter email; EMPLOYEE deve ter username
    CONSTRAINT ck_users_admin_needs_email CHECK (role <> 'ADMIN' OR email IS NOT NULL),
    CONSTRAINT ck_users_employee_needs_username CHECK (role <> 'EMPLOYEE' OR username IS NOT NULL)
);

-- username único no sistema (login não informa o mercado)
CREATE UNIQUE INDEX ux_users_username ON users (lower(username)) WHERE username IS NOT NULL;
-- email único no sistema (case-insensitive)
CREATE UNIQUE INDEX ux_users_email ON users (lower(email)) WHERE email IS NOT NULL;
-- garante um único ADMIN por mercado
CREATE UNIQUE INDEX ux_users_one_admin_per_business ON users (business_id) WHERE role = 'ADMIN';
CREATE INDEX idx_users_business_id ON users (business_id);

-- Adiciona FKs de business.created_by / updated_by agora que users existe
ALTER TABLE business
    ADD CONSTRAINT fk_business_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_business_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT;


-- categories
CREATE TABLE categories
(
    id          UUID PRIMARY KEY,
    business_id UUID         NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by  UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT
);

-- nome único por mercado
CREATE UNIQUE INDEX ux_categories_business_name ON categories (business_id, lower(name));
CREATE INDEX idx_categories_business_id ON categories (business_id);

-- products
CREATE TABLE products
(
    id            UUID PRIMARY KEY,
    business_id   UUID           NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    internal_code VARCHAR(50),                       -- SKU interno; único por mercado (índice abaixo)
    name          VARCHAR(150)   NOT NULL,
    category_id   UUID           NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    unit          VARCHAR(10)    NOT NULL DEFAULT 'UN',
    price         DECIMAL(10, 2) NOT NULL,
    cost_price    DECIMAL(10, 2),                    -- opcional; nem todo mercado controla custo
    stock         INTEGER        NOT NULL DEFAULT 0, -- atualizado APENAS via stock_movement
    stock_minimum INTEGER,                           -- gera alerta mas não impede venda
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    version       BIGINT         NOT NULL DEFAULT 0, -- controle otimista (@Version)
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by    UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by    UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,

    CONSTRAINT ck_products_unit CHECK (unit IN ('UN', 'KG', 'L', 'CX', 'PCT')),
    CONSTRAINT ck_products_price CHECK (price > 0),
    CONSTRAINT ck_products_cost_price CHECK (cost_price IS NULL OR cost_price >= 0),
    CONSTRAINT ck_products_stock CHECK (stock >= 0),
    CONSTRAINT ck_products_stock_minimum CHECK (stock_minimum IS NULL OR stock_minimum >= 0)
);

-- SKU interno único por mercado
CREATE UNIQUE INDEX ux_products_business_internal_code
    ON products (business_id, internal_code) WHERE internal_code IS NOT NULL;
CREATE INDEX idx_products_business_id ON products (business_id);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_active ON products (business_id, active) WHERE active = TRUE;


-- clients
CREATE TABLE clients
(
    id           UUID PRIMARY KEY,
    business_id  UUID           NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    name         VARCHAR(150)   NOT NULL,
    phone        VARCHAR(11)    NOT NULL,
    email        VARCHAR(150),
    address      VARCHAR(255),
    cep          VARCHAR(8),
    document     VARCHAR(14),
    note         VARCHAR(500),
    credit_limit DECIMAL(10, 2) NOT NULL DEFAULT 0,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    version      BIGINT         NOT NULL DEFAULT 0, -- controle otimista (@Version)
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by   UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by   UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,

    CONSTRAINT ck_clients_phone_format CHECK (phone ~ '^[0-9]{10,11}$'),
    CONSTRAINT ck_clients_cep_format CHECK (cep IS NULL OR cep ~ '^[0-9]{8}$'),
    CONSTRAINT ck_clients_document_format CHECK (document IS NULL OR document ~ '^[0-9]{11,14}$'),
    CONSTRAINT ck_clients_credit_limit CHECK (credit_limit >= 0)
);

-- document único por mercado (dois mercados podem ter cliente com mesmo CPF)
CREATE UNIQUE INDEX ux_clients_business_document
    ON clients (business_id, document) WHERE document IS NOT NULL;
CREATE INDEX idx_clients_business_id ON clients (business_id);
CREATE INDEX idx_clients_active ON clients (business_id, active) WHERE active = TRUE;


-- sales
CREATE TABLE sales
(
    id                 UUID PRIMARY KEY,
    business_id        UUID           NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    idempotency_key    VARCHAR(36)    NOT NULL, -- UUID enviado no header Idempotency-Key
    client_id          UUID REFERENCES clients (id) ON DELETE RESTRICT,
    sale_type          VARCHAR(20)    NOT NULL, -- IMMEDIATE | STORE_CREDIT
    total_value        DECIMAL(10, 2) NOT NULL DEFAULT 0,
    due_date           DATE,                    -- obrigatória em STORE_CREDIT
    status             VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    cancel_reason      VARCHAR(255),
    canceled_by        UUID REFERENCES users (id) ON DELETE RESTRICT,
    canceled_at        TIMESTAMPTZ,
    canceled_with_code BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by         UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by         UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,

    -- idempotência: mesma chave no mesmo mercado = mesma venda
    CONSTRAINT uq_sales_idempotency UNIQUE (business_id, idempotency_key),

    CONSTRAINT ck_sales_sale_type CHECK (sale_type IN ('IMMEDIATE', 'STORE_CREDIT')),
    CONSTRAINT ck_sales_status CHECK (status IN ('ACTIVE', 'CANCELED')),
    CONSTRAINT ck_sales_total_value CHECK (total_value >= 0),
    -- STORE_CREDIT exige client_id e due_date; IMMEDIATE proíbe ambos
    CONSTRAINT ck_sales_credit_needs_client CHECK ((sale_type = 'STORE_CREDIT') = (client_id IS NOT NULL)),
    CONSTRAINT ck_sales_credit_needs_due_date CHECK ((sale_type = 'STORE_CREDIT') = (due_date IS NOT NULL)),
    -- campos de cancelamento: todos presentes ou todos ausentes
    CONSTRAINT ck_sales_cancel_data CHECK (
        (canceled_at IS NULL) = (canceled_by IS NULL) AND
        (canceled_at IS NULL) = (cancel_reason IS NULL)
        ),
    -- canceled_with_code só pode ser true em venda cancelada
    CONSTRAINT ck_sales_canceled_with_code CHECK (NOT canceled_with_code OR status = 'CANCELED')
);

CREATE INDEX idx_sales_business_id ON sales (business_id);
CREATE INDEX idx_sales_client_id ON sales (client_id);
CREATE INDEX idx_sales_created_by ON sales (created_by);
-- relatórios e consulta de inadimplência
CREATE INDEX idx_sales_status_due_date ON sales (business_id, status, due_date);


-- sale_items
-- Auditoria herdada de sales (mesma transação, mesmo instante, mesmo autor).
CREATE TABLE sale_items
(
    id          UUID PRIMARY KEY,
    business_id UUID           NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    sale_id     UUID           NOT NULL REFERENCES sales (id) ON DELETE RESTRICT,
    product_id  UUID           NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    unit_price  DECIMAL(10, 2) NOT NULL, -- preço congelado no momento da venda
    unit_cost   DECIMAL(10, 2),          -- custo congelado no momento (opcional)
    quantity    INTEGER        NOT NULL,

    CONSTRAINT ck_sale_items_unit_price CHECK (unit_price > 0),
    CONSTRAINT ck_sale_items_unit_cost CHECK (unit_cost IS NULL OR unit_cost >= 0),
    CONSTRAINT ck_sale_items_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_sale_items_business_id ON sale_items (business_id);
CREATE INDEX idx_sale_items_sale_id ON sale_items (sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items (product_id);


-- stock_movements
CREATE TABLE stock_movements
(
    id                UUID PRIMARY KEY,
    business_id       UUID        NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    product_id        UUID        NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    type              VARCHAR(20) NOT NULL, -- STOCK_IN | SALE | SALE_CANCELED | ADJUSTMENT
    quantity          INTEGER     NOT NULL, -- com sinal; nunca zero
    stock_after       INTEGER     NOT NULL, -- estoque após a movimentação
    unit_cost         DECIMAL(10, 2),       -- custo unitário no momento (opcional; útil em STOCK_IN)
    sale_id           UUID REFERENCES sales (id) ON DELETE RESTRICT,
    adjustment_reason VARCHAR(20),          -- obrigatório em ADJUSTMENT
    reason            VARCHAR(255),         -- texto livre; obrigatório em ADJUSTMENT
    created_by        UUID        NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_stock_movements_type CHECK (type IN ('STOCK_IN', 'SALE', 'SALE_CANCELED', 'ADJUSTMENT')),
    CONSTRAINT ck_stock_movements_quantity CHECK (quantity <> 0),
    CONSTRAINT ck_stock_movements_stock_after CHECK (stock_after >= 0),
    CONSTRAINT ck_stock_movements_unit_cost CHECK (unit_cost IS NULL OR unit_cost >= 0),
    -- STOCK_IN e SALE_CANCELED sempre positivos; SALE sempre negativo; ADJUSTMENT qualquer ≠ 0
    CONSTRAINT ck_stock_movements_sign CHECK (
        (type IN ('STOCK_IN', 'SALE_CANCELED') AND quantity > 0) OR
        (type = 'SALE' AND quantity < 0) OR
        (type = 'ADJUSTMENT')
        ),
    -- sale_id obrigatório somente em SALE e SALE_CANCELED
    CONSTRAINT ck_stock_movements_sale_id CHECK (
        (type IN ('SALE', 'SALE_CANCELED')) = (sale_id IS NOT NULL)
        ),
    -- ADJUSTMENT exige reason e adjustment_reason
    CONSTRAINT ck_stock_movements_adj_reason_required CHECK (
        type <> 'ADJUSTMENT' OR adjustment_reason IS NOT NULL
        ),
    CONSTRAINT ck_stock_movements_adj_reason_values CHECK (
        adjustment_reason IS NULL OR
        adjustment_reason IN ('LOSS', 'BREAKAGE', 'EXPIRY', 'COUNT', 'OTHER')
        ),
    CONSTRAINT ck_stock_movements_reason_required CHECK (
        type <> 'ADJUSTMENT' OR reason IS NOT NULL
        )
);

CREATE INDEX idx_stock_movements_business_id ON stock_movements (business_id);
CREATE INDEX idx_stock_movements_product_created ON stock_movements (business_id, product_id, created_at DESC);
CREATE INDEX idx_stock_movements_sale_id ON stock_movements (sale_id);
CREATE INDEX idx_stock_movements_type ON stock_movements (business_id, type);

-- payments
CREATE TABLE payments
(
    id              UUID PRIMARY KEY,
    business_id     UUID           NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    idempotency_key VARCHAR(36),             -- NULL só no pagamento automático de IMMEDIATE
    sale_id         UUID           NOT NULL REFERENCES sales (id) ON DELETE RESTRICT,
    received_by     UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    amount          DECIMAL(10, 2) NOT NULL,
    method          VARCHAR(20)    NOT NULL, -- CASH | PIX | DEBIT_CARD | CREDIT_CARD
    reversed_at     TIMESTAMPTZ,
    reversed_by     UUID REFERENCES users (id) ON DELETE RESTRICT,
    reversal_reason VARCHAR(255),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by      UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,

    -- idempotência: mesma chave no mesmo mercado = mesmo pagamento
    CONSTRAINT uq_payments_idempotency UNIQUE (business_id, idempotency_key),

    CONSTRAINT ck_payments_amount CHECK (amount > 0),
    CONSTRAINT ck_payments_method CHECK (method IN ('CASH', 'PIX', 'DEBIT_CARD', 'CREDIT_CARD')),
    -- campos de estorno: todos presentes ou todos ausentes
    CONSTRAINT ck_payments_reversal_data CHECK (
        (reversed_at IS NULL) = (reversed_by IS NULL) AND
        (reversed_at IS NULL) = (reversal_reason IS NULL)
        )
);

CREATE INDEX idx_payments_business_id ON payments (business_id);
CREATE INDEX idx_payments_sale_id ON payments (sale_id);
-- relatório de pagamentos recebidos por período
CREATE INDEX idx_payments_created_at ON payments (business_id, created_at DESC);


-- cancel_codes  (PIN de cancelamento; 1 por mercado)
CREATE TABLE cancel_codes
(
    id              UUID PRIMARY KEY,
    business_id     UUID         NOT NULL UNIQUE REFERENCES business (id) ON DELETE RESTRICT,
    code_hash       VARCHAR(255) NOT NULL, -- BCrypt do PIN de 6 dígitos; nunca em texto
    failed_attempts INTEGER      NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,           -- bloqueio temporário após 5 erros seguidos
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by      UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,

    CONSTRAINT ck_cancel_codes_failed_attempts CHECK (failed_attempts >= 0)
);


-- audit_log
CREATE TABLE audit_log
(
    id          UUID PRIMARY KEY,
    business_id UUID         NOT NULL REFERENCES business (id) ON DELETE RESTRICT,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    action      VARCHAR(255) NOT NULL, -- AuditActionEnum: ex. SALE_CANCELED, PRODUCT_PRICE_CHANGED
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    user_role   VARCHAR(20)  NOT NULL, -- snapshot do papel no momento da ação
    entity_type VARCHAR(255) NOT NULL, -- ex. "Sale", "Product"
    entity_id   UUID         NOT NULL,
    ip_address  VARCHAR(45),
    details     JSONB        NOT NULL, -- valores antes/depois, motivo etc.

    CONSTRAINT ck_audit_log_user_role CHECK (user_role IN ('ADMIN', 'EMPLOYEE'))
);

CREATE INDEX idx_audit_log_business_id ON audit_log (business_id);
CREATE INDEX idx_audit_log_occurred_at ON audit_log (business_id, occurred_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);