-- No hay datos reales que conservar: se vacía para poder cambiar los tipos.
TRUNCATE TABLE reminders, subscriptions, users;

-- 1. Quitar las claves foráneas que dependen de las columnas que cambian.
ALTER TABLE reminders     DROP CONSTRAINT reminders_subscription_id_fkey;
ALTER TABLE subscriptions DROP CONSTRAINT subscriptions_user_id_fkey;

-- 2. Quitar la generación automática numérica.
ALTER TABLE users         ALTER COLUMN id DROP IDENTITY;
ALTER TABLE subscriptions ALTER COLUMN id DROP IDENTITY;

-- 3. Cambiar los tipos. Con las tablas vacías, USING solo tiene que
--    producir un valor del tipo nuevo: no hay filas a las que aplicarlo.
ALTER TABLE users         ALTER COLUMN id              TYPE uuid USING gen_random_uuid();
ALTER TABLE subscriptions ALTER COLUMN id              TYPE uuid USING gen_random_uuid();
ALTER TABLE subscriptions ALTER COLUMN user_id         TYPE uuid USING gen_random_uuid();
ALTER TABLE reminders     ALTER COLUMN subscription_id TYPE uuid USING gen_random_uuid();

-- 4. La base de datos genera el UUID si nadie lo da (red de seguridad).
ALTER TABLE users         ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE subscriptions ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- 5. Volver a crear las claves foráneas.
ALTER TABLE subscriptions
    ADD CONSTRAINT subscriptions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE reminders
    ADD CONSTRAINT reminders_subscription_id_fkey
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE;