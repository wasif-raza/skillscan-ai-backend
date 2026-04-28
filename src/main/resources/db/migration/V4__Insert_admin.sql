INSERT INTO users (id, first_name, last_name, email, password, role, created_at)
VALUES (
    gen_random_uuid(),
    'wasif',
    'raza',
    'wasifhzb0786@gmail.com',
    '$2a$12$apFJkgMUdCXmViDTbweIGe626MmlqpsSrXa7GNdqEOqp7jm0qiXrm',
    'ADMIN',
    now()
)
ON CONFLICT (email) DO NOTHING;