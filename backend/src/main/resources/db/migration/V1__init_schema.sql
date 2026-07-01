-- Core schema for the Rental Vehicle Management System

CREATE TABLE branches (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL UNIQUE,
    address         VARCHAR(255) NOT NULL,
    phone           VARCHAR(30)  NOT NULL,
    manager_name    VARCHAR(120),
    opening_hours   VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    phone           VARCHAR(30),
    role            VARCHAR(30)  NOT NULL,
    branch_id       BIGINT REFERENCES branches(id),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE vehicles (
    id              BIGSERIAL PRIMARY KEY,
    plate_number    VARCHAR(20) NOT NULL UNIQUE,
    vin             VARCHAR(17) NOT NULL UNIQUE,
    type            VARCHAR(20) NOT NULL,
    brand           VARCHAR(60) NOT NULL,
    model           VARCHAR(60) NOT NULL,
    year            INT         NOT NULL,
    mileage         INT         NOT NULL DEFAULT 0,
    fuel_type       VARCHAR(20) NOT NULL,
    transmission    VARCHAR(20) NOT NULL,
    daily_rate      NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    branch_id       BIGINT NOT NULL REFERENCES branches(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);
CREATE INDEX idx_vehicles_branch ON vehicles(branch_id);
CREATE INDEX idx_vehicles_status ON vehicles(status);

CREATE TABLE customers (
    id                      BIGSERIAL PRIMARY KEY,
    full_name               VARCHAR(150) NOT NULL,
    email                   VARCHAR(150) NOT NULL UNIQUE,
    phone                   VARCHAR(30)  NOT NULL,
    driver_license_number   VARCHAR(50)  NOT NULL UNIQUE,
    address                 VARCHAR(255),
    date_of_birth           DATE,
    blacklisted             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ
);
CREATE INDEX idx_customers_name ON customers(full_name);

CREATE TABLE reservations (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    vehicle_id      BIGINT NOT NULL REFERENCES vehicles(id),
    branch_id       BIGINT NOT NULL REFERENCES branches(id),
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    estimated_total NUMERIC(10,2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_vehicle ON reservations(vehicle_id);

CREATE TABLE rentals (
    id                  BIGSERIAL PRIMARY KEY,
    reservation_id      BIGINT REFERENCES reservations(id),
    customer_id         BIGINT NOT NULL REFERENCES customers(id),
    vehicle_id          BIGINT NOT NULL REFERENCES vehicles(id),
    branch_id           BIGINT NOT NULL REFERENCES branches(id),
    start_date          DATE NOT NULL,
    planned_end_date    DATE NOT NULL,
    actual_return_date  DATE,
    daily_rate          NUMERIC(10,2) NOT NULL,
    total_amount        NUMERIC(10,2),
    late_fee            NUMERIC(10,2) NOT NULL DEFAULT 0,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ
);
CREATE INDEX idx_rentals_status ON rentals(status);
CREATE INDEX idx_rentals_branch ON rentals(branch_id);
CREATE INDEX idx_rentals_planned_end ON rentals(planned_end_date);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    rental_id       BIGINT NOT NULL REFERENCES rentals(id),
    amount          NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    method          VARCHAR(30),
    transaction_ref VARCHAR(100),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_rental ON payments(rental_id);
CREATE INDEX idx_payments_status ON payments(status);

CREATE TABLE maintenance_records (
    id              BIGSERIAL PRIMARY KEY,
    vehicle_id      BIGINT NOT NULL REFERENCES vehicles(id),
    branch_id       BIGINT NOT NULL REFERENCES branches(id),
    description     VARCHAR(500) NOT NULL,
    cost            NUMERIC(10,2),
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_date  DATE NOT NULL,
    completed_date  DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_maintenance_vehicle ON maintenance_records(vehicle_id);
CREATE INDEX idx_maintenance_status ON maintenance_records(status);
