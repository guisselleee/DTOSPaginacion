-- Demo users for the administrative dashboard and local testing.
INSERT INTO weill_users (
    email,
    role,
    display_name,
    education_level,
    school_center,
    municipality,
    department,
    birth_date,
    currently_studying
)
VALUES
    (
        'maria.lopez@example.com',
        'EVALUADO',
        'María López',
        'Primaria',
        'Colegio Rubén Darío',
        'Managua',
        'Managua',
        DATE '2015-03-14',
        TRUE
    ),
    (
        'carlos.martinez@example.com',
        'EVALUADO',
        'Carlos Martínez',
        'Secundaria',
        'Instituto Nacional Central',
        'León',
        'León',
        DATE '2011-09-22',
        TRUE
    ),
    (
        'ana.garcia@example.com',
        'EVALUADO',
        'Ana García',
        'Universidad',
        'Universidad Nacional',
        'Masaya',
        'Masaya',
        DATE '2004-01-08',
        TRUE
    ),
    (
        'jose.ramirez@example.com',
        'EVALUADO',
        'José Ramírez',
        'Secundaria completa',
        NULL,
        'Estelí',
        'Estelí',
        DATE '1988-07-19',
        FALSE
    )
ON CONFLICT (email) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    education_level = EXCLUDED.education_level,
    school_center = EXCLUDED.school_center,
    municipality = EXCLUDED.municipality,
    department = EXCLUDED.department,
    birth_date = EXCLUDED.birth_date,
    currently_studying = EXCLUDED.currently_studying;

-- Completed and in-progress evaluations. The timestamps identify each seed
-- attempt and make repeated application startup idempotent.
INSERT INTO weill_test_attempts (
    user_id,
    status,
    started_at,
    completed_at,
    total_score,
    max_score
)
SELECT
    user_record.id,
    seed_attempt.status,
    seed_attempt.started_at,
    seed_attempt.completed_at,
    seed_attempt.total_score,
    60
FROM (
    VALUES
        (
            'maria.lopez@example.com',
            'COMPLETED',
            TIMESTAMPTZ '2026-05-12 09:00:00-06',
            TIMESTAMPTZ '2026-05-12 09:28:42-06',
            50
        ),
        (
            'carlos.martinez@example.com',
            'COMPLETED',
            TIMESTAMPTZ '2026-05-18 14:15:00-06',
            TIMESTAMPTZ '2026-05-18 14:51:16-06',
            43
        ),
        (
            'ana.garcia@example.com',
            'COMPLETED',
            TIMESTAMPTZ '2026-05-26 10:05:00-06',
            TIMESTAMPTZ '2026-05-26 10:29:08-06',
            57
        ),
        (
            'jose.ramirez@example.com',
            'IN_PROGRESS',
            TIMESTAMPTZ '2026-06-05 16:30:00-06',
            NULL,
            0
        )
) AS seed_attempt(email, status, started_at, completed_at, total_score)
JOIN weill_users user_record ON user_record.email = seed_attempt.email
WHERE NOT EXISTS (
    SELECT 1
    FROM weill_test_attempts existing
    WHERE existing.user_id = user_record.id
      AND existing.started_at = seed_attempt.started_at
);

-- Generate responses using the same 12-answer key used by WeillTestCatalog.
-- Completed attempts receive 60 answers; the in-progress attempt receives 18.
WITH seeded_attempts AS (
    SELECT
        attempt.id,
        attempt.started_at,
        attempt.total_score,
        CASE WHEN attempt.status = 'COMPLETED' THEN 60 ELSE 18 END AS answer_count
    FROM weill_test_attempts attempt
    WHERE attempt.started_at IN (
        TIMESTAMPTZ '2026-05-12 09:00:00-06',
        TIMESTAMPTZ '2026-05-18 14:15:00-06',
        TIMESTAMPTZ '2026-05-26 10:05:00-06',
        TIMESTAMPTZ '2026-06-05 16:30:00-06'
    )
),
generated_answers AS (
    SELECT
        attempt.id AS attempt_id,
        answer_number,
        ((answer_number - 1) / 12 + 1)::SMALLINT AS page_number,
        (((answer_number - 1) % 12) / 3 + 1)::SMALLINT AS row_number,
        (((answer_number - 1) % 3) + 1)::SMALLINT AS item_number,
        (
            ARRAY[
                2, 8, 7,
                4, 5, 6,
                8, 4, 4,
                7, 3, 2
            ]
        )[((answer_number - 1) % 12) + 1]::SMALLINT AS correct_option,
        attempt.total_score,
        attempt.started_at
    FROM seeded_attempts attempt
    CROSS JOIN LATERAL generate_series(1, attempt.answer_count) AS answer_number
)
INSERT INTO weill_item_responses (
    attempt_id,
    page_number,
    row_number,
    item_number,
    selected_option,
    correct_option,
    is_correct,
    score,
    answered_at
)
SELECT
    answer.attempt_id,
    answer.page_number,
    answer.row_number,
    answer.item_number,
    CASE
        WHEN answer.answer_number <= answer.total_score
            THEN answer.correct_option
        ELSE (answer.correct_option % 8 + 1)::SMALLINT
    END,
    answer.correct_option,
    answer.answer_number <= answer.total_score,
    CASE WHEN answer.answer_number <= answer.total_score THEN 1 ELSE 0 END,
    answer.started_at + make_interval(secs => answer.answer_number * 25)
FROM generated_answers answer
ON CONFLICT (attempt_id, page_number, row_number, item_number) DO NOTHING;

-- Representative audit history for exercising audit queries and database data.
INSERT INTO weill_audit_logs (
    event_type,
    actor_user_id,
    actor_email,
    actor_role,
    attempt_id,
    session_id,
    ip_address,
    details,
    occurred_at
)
SELECT
    event.event_type,
    user_record.id,
    user_record.email,
    'EVALUADO',
    attempt.id,
    event.session_id,
    '127.0.0.1',
    event.details,
    event.occurred_at
FROM (
    VALUES
        (
            'maria.lopez@example.com',
            TIMESTAMPTZ '2026-05-12 09:00:00-06',
            'LOGIN',
            'seed-maria',
            'Inicio de sesión de usuario.',
            TIMESTAMPTZ '2026-05-12 08:59:45-06'
        ),
        (
            'maria.lopez@example.com',
            TIMESTAMPTZ '2026-05-12 09:00:00-06',
            'EVALUATION_STARTED',
            'seed-maria',
            'Evaluación iniciada.',
            TIMESTAMPTZ '2026-05-12 09:00:00-06'
        ),
        (
            'maria.lopez@example.com',
            TIMESTAMPTZ '2026-05-12 09:00:00-06',
            'EVALUATION_COMPLETED',
            'seed-maria',
            'Evaluación finalizada con 50/60 aciertos.',
            TIMESTAMPTZ '2026-05-12 09:28:42-06'
        ),
        (
            'carlos.martinez@example.com',
            TIMESTAMPTZ '2026-05-18 14:15:00-06',
            'RESULT_VIEWED',
            'seed-carlos',
            'El participante consultó sus resultados.',
            TIMESTAMPTZ '2026-05-18 14:52:03-06'
        ),
        (
            'jose.ramirez@example.com',
            TIMESTAMPTZ '2026-06-05 16:30:00-06',
            'SESSION_TIMEOUT',
            'seed-jose',
            'Sesión cerrada por inactividad.',
            TIMESTAMPTZ '2026-06-05 17:12:00-06'
        )
) AS event(email, attempt_started_at, event_type, session_id, details, occurred_at)
JOIN weill_users user_record ON user_record.email = event.email
JOIN weill_test_attempts attempt
    ON attempt.user_id = user_record.id
   AND attempt.started_at = event.attempt_started_at
WHERE NOT EXISTS (
    SELECT 1
    FROM weill_audit_logs existing
    WHERE existing.session_id = event.session_id
      AND existing.event_type = event.event_type
      AND existing.occurred_at = event.occurred_at
);
