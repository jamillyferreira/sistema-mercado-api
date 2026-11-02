-- V1__create_tables.sql — Sistema de Mercado (PostgreSQL)

--   * Nada é apagado: todas as FKs usam ON DELETE RESTRICT.

-- users (admin e funcionário)
CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    phone         VARCHAR(20),
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Aceitam NULL só aqui: o primeiro admin não tem quem o criou
    created_by    UUID REFERENCES users (id) ON DELETE RESTRICT,
    updated_by    UUID REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'EMPLOYEE'))
);

-- E-mail único sem diferenciar maiúsculas de minúsculas
CREATE UNIQUE INDEX ux_users_email_lower ON users (lower(email));

-- business (dados do comércio; um único registro)
CREATE TABLE business
(
    id       UUID PRIMARY KEY,
    name     VARCHAR(150) NOT NULL,
    owner_id UUID         NOT NULL UNIQUE, -- UNIQUE => relação um para um com users
    CONSTRAINT fk_business_owner_id_users
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE RESTRICT
);

-- categories
CREATE TABLE categories
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(150),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by  UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT
);

-- clients
CREATE TABLE clients
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(150)   NOT NULL,
    phone        VARCHAR(20)    NOT NULL,
    address      VARCHAR(150),
    credit_limit DECIMAL(10, 2) NOT NULL DEFAULT 0, -- 0 = sem fiado até o admin liberar
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by   UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by   UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_clients_credit_limit CHECK (credit_limit >= 0)
);

-- products
CREATE TABLE products
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(150)   NOT NULL,
    category_id   UUID           NOT NULL,
    price         DECIMAL(10, 2) NOT NULL,
    stock         INTEGER        NOT NULL DEFAULT 0, -- Só muda por movimentação de estoque (tabela stock_movement)
    stock_minimum INTEGER,                           -- Gera alerta quando stock < stock_minimum
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by    UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by    UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_products_category_id_categories
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT ck_products_price CHECK (price > 0),
    CONSTRAINT ck_products_stock CHECK (stock >= 0),
    CONSTRAINT ck_products_stock_minimum CHECK (stock_minimum IS NULL OR stock_minimum >= 0)
);

CREATE INDEX idx_products_category_id ON products (category_id);

-- sales
CREATE TABLE sales
(
    id                 UUID PRIMARY KEY,
    client_id          UUID           NOT NULL,
    sale_type          VARCHAR(70)    NOT NULL,                                          -- IMMEDIATE | STORE_CREDIT (sem DEFAULT: o service sempre informa)
    total_value        DECIMAL(10, 2) NOT NULL DEFAULT 0,
    due_date           DATE,                                                             -- Obrigatória em STORE_CREDIT, nula em IMMEDIATE
    status             VARCHAR(70)    NOT NULL DEFAULT 'ACTIVE',                         -- ACTIVE | CANCELED
    -- Os quatro campos abaixo só são preenchidos quando a venda é cancelada
    cancel_reason      VARCHAR(255),
    canceled_by        UUID,
    canceled_at        TIMESTAMPTZ,
    canceled_with_code BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by         UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT, -- created_by = quem registrou a venda
    updated_by         UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_client_id_clients
        FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_canceled_by_users
        FOREIGN KEY (canceled_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_sales_sale_type CHECK (sale_type IN ('IMMEDIATE', 'STORE_CREDIT')),
    CONSTRAINT ck_sales_status CHECK (status IN ('ACTIVE', 'CANCELED')),
    CONSTRAINT ck_sales_total_value CHECK (total_value >= 0),
    CONSTRAINT ck_sales_due_date CHECK ((sale_type = 'STORE_CREDIT') = (due_date IS NOT NULL)),
    CONSTRAINT ck_sales_cancel_data CHECK (
        (status = 'CANCELED') = (canceled_at IS NOT NULL)
            AND (status = 'CANCELED') = (canceled_by IS NOT NULL)
            AND (status = 'CANCELED') = (cancel_reason IS NOT NULL)
        ),
    CONSTRAINT ck_sales_canceled_with_code CHECK (NOT canceled_with_code OR status = 'CANCELED')
);

CREATE INDEX idx_sales_client_id ON sales (client_id);
CREATE INDEX idx_sales_created_by ON sales (created_by);
CREATE INDEX idx_sales_status_due_date ON sales (status, due_date);

-- sale_item
CREATE TABLE sale_item
(
    id         UUID PRIMARY KEY,
    sale_id    UUID           NOT NULL,
    product_id UUID           NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL, -- Preço do produto no momento da venda
    quantity   INTEGER        NOT NULL,
    CONSTRAINT fk_sale_item_sale_id_sales
        FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sale_item_product_id_products
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT ck_sale_item_unit_price CHECK (unit_price > 0),
    CONSTRAINT ck_sale_item_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_sale_item_sale_id ON sale_item (sale_id);
CREATE INDEX idx_sale_item_product_id ON sale_item (product_id);

-- payments
CREATE TABLE payments
(
    id              UUID PRIMARY KEY,
    sale_id         UUID           NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL, -- > 0 e <= saldo devedor da venda (a segunda parte é validada no service)
    method          VARCHAR(150)   NOT NULL, -- CASH | PIX | DEBIT_CARD | CREDIT_CARD (sem DEFAULT: o service sempre informa)
    -- Os três campos abaixo são preenchidos juntos quando o pagamento é estornado
    reversed_at     TIMESTAMPTZ,
    reversed_by     UUID,
    reversal_reason VARCHAR(255),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    -- created_by = quem recebeu o dinheiro
    created_by      UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by      UUID           NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_sale_id_sales
        FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_reversed_by_users
        FOREIGN KEY (reversed_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_payments_amount CHECK (amount > 0),
    CONSTRAINT ck_payments_method CHECK (method IN ('CASH', 'PIX', 'DEBIT_CARD', 'CREDIT_CARD')),
    CONSTRAINT ck_payments_reversal_data CHECK (
        (reversed_at IS NULL) = (reversed_by IS NULL)
            AND (reversed_at IS NULL) = (reversal_reason IS NULL)
        )
);

CREATE INDEX idx_payments_sale_id ON payments (sale_id);

-- stock_movement (histórico de estoque)
CREATE TABLE stock_movement
(
    id            UUID PRIMARY KEY,
    product_id    UUID        NOT NULL,
    type          VARCHAR(50) NOT NULL, -- STOCK_IN | SALE | SALE_CANCELED | ADJUSTMENT
    -- Com sinal: positivo entra, negativo sai
    quantity      INTEGER     NOT NULL,
    stock_after   INTEGER     NOT NULL, -- Estoque do produto depois da movimentação
    sale_id       UUID,
    -- Obrigatório em ADJUSTMENT
    reason        VARCHAR(255),
    registered_by UUID        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_stock_movement_product_id_products
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_movement_sale_id_sales
        FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_movement_registered_by_users
        FOREIGN KEY (registered_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_stock_movement_type
        CHECK (type IN ('STOCK_IN', 'SALE', 'SALE_CANCELED', 'ADJUSTMENT')),
    CONSTRAINT ck_stock_movement_quantity CHECK (quantity <> 0),
    CONSTRAINT ck_stock_movement_stock_after CHECK (stock_after >= 0),
    -- STOCK_IN e SALE_CANCELED somam, SALE subtrai, ADJUSTMENT pode ser qualquer sinal
    CONSTRAINT ck_stock_movement_sign CHECK (
        (type IN ('STOCK_IN', 'SALE_CANCELED') AND quantity > 0)
            OR (type = 'SALE' AND quantity < 0)
            OR type = 'ADJUSTMENT'
        ),
    -- sale_id só nas movimentações ligadas a uma venda
    CONSTRAINT ck_stock_movement_sale CHECK (
        (type IN ('SALE', 'SALE_CANCELED')) = (sale_id IS NOT NULL)
        ),
    CONSTRAINT ck_stock_movement_reason CHECK (type <> 'ADJUSTMENT' OR reason IS NOT NULL)
);

CREATE INDEX idx_stock_movement_product_created ON stock_movement (product_id, created_at DESC);
CREATE INDEX idx_stock_movement_sale_id ON stock_movement (sale_id);

-- cancel_code (PIN de cancelamento; um único registro)
CREATE TABLE cancel_code
(
    id              UUID PRIMARY KEY,
    code_hash       VARCHAR(255) NOT NULL, -- Hash BCrypt do PIN de 6 dígitos. Nunca em texto
    -- Tentativas erradas seguidas. Volta a 0 ao acertar
    failed_attempts INTEGER      NOT NULL DEFAULT 0,
    -- Bloqueio temporário após muitas tentativas erradas
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    updated_by      UUID         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_cancel_code_failed_attempts CHECK (failed_attempts >= 0)
);

-- Garante no máximo um registro na tabela
CREATE UNIQUE INDEX ux_cancel_code_single ON cancel_code ((TRUE));

-- audit_log
CREATE TABLE audit_log
(
    id          UUID PRIMARY KEY,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    action      VARCHAR(255) NOT NULL, -- Ex: PRODUCT_PRICE_CHANGED, SALE_CANCELED
    user_id     UUID         NOT NULL, -- Quem executou
    user_role   VARCHAR(70)  NOT NULL,

    entity_type VARCHAR(255) NOT NULL, -- Ex: 'Sale', 'Product'
    entity_id   UUID         NOT NULL,
    ip_address  VARCHAR(45),           -- IP da requisição, quando houver (VARCHAR(45) cabe IPv6)
    details     JSONB        NOT NULL, -- Valores antes e depois, motivo etc.
    CONSTRAINT fk_audit_log_user_id_users
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_audit_log_user_role CHECK (user_role IN ('ADMIN', 'EMPLOYEE'))
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at DESC);