CREATE TABLE users (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   email VARCHAR(255) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   locale VARCHAR(10) NOT NULL DEFAULT 'es',
   default_reminder_days INT NOT NULL DEFAULT 3,
   calendar_token VARCHAR(64) NOT NULL UNIQUE,
   created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE subscriptions (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
   name VARCHAR(120) NOT NULL,
   price NUMERIC(10,2) NOT NULL,
   currency CHAR(3) NOT NULL DEFAULT 'EUR',
   billing_cycle VARCHAR(20) NOT NULL,
   next_renewal DATE NOT NULL,
   category VARCHAR(60),
   status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
   trial_ends_on DATE,
   notes TEXT,
   created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_subscriptions_user ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_renewal ON subscriptions(next_renewal);

CREATE TABLE reminders (
   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
   send_on DATE NOT NULL,
   sent_at TIMESTAMPTZ,
   UNIQUE (subscription_id, send_on)
);