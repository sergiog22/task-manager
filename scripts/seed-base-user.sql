CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    seed_username text := 'task-admin';
    seed_password text := 'CHANGE_THIS_TO_A_LONG_UNIQUE_PASSWORD';
    seed_user_id bigint;
BEGIN
    IF char_length(trim(seed_username)) NOT BETWEEN 1 AND 100 THEN
        RAISE EXCEPTION 'Username must contain between 1 and 100 characters';
    END IF;

    IF seed_password = 'CHANGE_THIS_TO_A_LONG_UNIQUE_PASSWORD'
        OR char_length(seed_password) < 12 THEN
        RAISE EXCEPTION 'Set a unique password of at least 12 characters before running this script';
    END IF;

    INSERT INTO users (username, password, enabled)
    VALUES (seed_username, crypt(seed_password, gen_salt('bf', 12)), TRUE)
    ON CONFLICT (username) DO NOTHING
    RETURNING id INTO seed_user_id;

    IF seed_user_id IS NULL THEN
        SELECT id INTO seed_user_id
        FROM users
        WHERE username = seed_username;
    END IF;

    IF seed_user_id IS NULL THEN
        RAISE EXCEPTION 'Could not find or create user %', seed_username;
    END IF;

    INSERT INTO user_roles (user_id, role)
    SELECT seed_user_id, requested_roles.role
    FROM (VALUES ('USER'), ('ADMIN')) AS requested_roles(role)
    WHERE NOT EXISTS (
        SELECT 1
        FROM user_roles AS existing_role
        WHERE existing_role.user_id = seed_user_id
            AND existing_role.role = requested_roles.role
    );
END;
$$;