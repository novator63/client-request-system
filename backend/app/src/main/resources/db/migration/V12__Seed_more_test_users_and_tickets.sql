INSERT INTO users (
    email,
    password_hash,
    full_name,
    role,
    is_active,
    created_at,
    updated_at
) VALUES
    (
        'client2@example.com',
        '$2y$12$sdnwVJWaGVHM9xv1lvNFyOjJFjiJdr8OabiJ0bnLsRlvViirFR7yu',
        'Alice Smirnova',
        'CLIENT',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'client3@example.com',
        '$2y$12$sdnwVJWaGVHM9xv1lvNFyOjJFjiJdr8OabiJ0bnLsRlvViirFR7yu',
        'Boris Petrov',
        'CLIENT',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'client4@example.com',
        '$2y$12$sdnwVJWaGVHM9xv1lvNFyOjJFjiJdr8OabiJ0bnLsRlvViirFR7yu',
        'Daria Volkova',
        'CLIENT',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'client5@example.com',
        '$2y$12$sdnwVJWaGVHM9xv1lvNFyOjJFjiJdr8OabiJ0bnLsRlvViirFR7yu',
        'Egor Ivanov',
        'CLIENT',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'operator2@example.com',
        '$2y$12$F.oG172MI/gKKmG2enBq4OPH6.MU2E2ISm18F/Vsv5rKyaIvtkMES',
        'Olga Operator',
        'OPERATOR',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'operator3@example.com',
        '$2y$12$F.oG172MI/gKKmG2enBq4OPH6.MU2E2ISm18F/Vsv5rKyaIvtkMES',
        'Pavel Operator',
        'OPERATOR',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO NOTHING;

INSERT INTO tickets (
    title,
    description,
    status,
    priority,
    author_id,
    assignee_id,
    category_id,
    created_at,
    updated_at,
    closed_at,
    due_at
)
SELECT
    seed.title,
    seed.description,
    seed.status,
    seed.priority,
    seed.author_id,
    seed.assignee_id,
    seed.category_id,
    seed.created_at,
    seed.updated_at,
    seed.closed_at,
    seed.due_at
FROM (
    VALUES
        (
            'Не открывается страница оплаты для client2',
            'При переходе к оплате отображается пустая страница в браузере.',
            'NEW',
            'HIGH',
            (SELECT id FROM users WHERE email = 'client2@example.com'),
            (SELECT id FROM users WHERE email = 'operator2@example.com'),
            (SELECT id FROM categories WHERE name = 'Оплата'),
            CURRENT_TIMESTAMP - INTERVAL '4 hours',
            CURRENT_TIMESTAMP - INTERVAL '4 hours',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP + INTERVAL '8 hours'
        ),
        (
            'Не удается обновить профиль для client2',
            'Ошибка валидации при сохранении телефона в профиле.',
            'IN_PROGRESS',
            'MEDIUM',
            (SELECT id FROM users WHERE email = 'client2@example.com'),
            (SELECT id FROM users WHERE email = 'operator3@example.com'),
            (SELECT id FROM categories WHERE name = 'Техническая проблема'),
            CURRENT_TIMESTAMP - INTERVAL '1 day',
            CURRENT_TIMESTAMP - INTERVAL '8 hours',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP + INTERVAL '12 hours'
        ),
        (
            'Неверный расчет стоимости доставки для client3',
            'Система добавляет двойную стоимость доставки в корзине.',
            'IN_PROGRESS',
            'HIGH',
            (SELECT id FROM users WHERE email = 'client3@example.com'),
            (SELECT id FROM users WHERE email = 'operator2@example.com'),
            (SELECT id FROM categories WHERE name = 'Доставка'),
            CURRENT_TIMESTAMP - INTERVAL '18 hours',
            CURRENT_TIMESTAMP - INTERVAL '2 hours',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP - INTERVAL '1 hour'
        ),
        (
            'Вопрос по возврату заказа client3',
            'Клиент уточняет сроки возврата денежных средств после отмены.',
            'RESOLVED',
            'LOW',
            (SELECT id FROM users WHERE email = 'client3@example.com'),
            (SELECT id FROM users WHERE email = 'operator3@example.com'),
            (SELECT id FROM categories WHERE name = 'Возврат'),
            CURRENT_TIMESTAMP - INTERVAL '2 days',
            CURRENT_TIMESTAMP - INTERVAL '1 day',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP + INTERVAL '1 day'
        ),
        (
            'Проблема с авторизацией mobile app для client4',
            'После обновления приложения вход выполняется только со второй попытки.',
            'NEW',
            'MEDIUM',
            (SELECT id FROM users WHERE email = 'client4@example.com'),
            NULL,
            (SELECT id FROM categories WHERE name = 'Техническая проблема'),
            CURRENT_TIMESTAMP - INTERVAL '5 hours',
            CURRENT_TIMESTAMP - INTERVAL '5 hours',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP + INTERVAL '19 hours'
        ),
        (
            'Запрос на изменение адреса доставки client4',
            'Нужно заменить адрес доставки в уже собранном заказе.',
            'IN_PROGRESS',
            'LOW',
            (SELECT id FROM users WHERE email = 'client4@example.com'),
            (SELECT id FROM users WHERE email = 'operator2@example.com'),
            (SELECT id FROM categories WHERE name = 'Доставка'),
            CURRENT_TIMESTAMP - INTERVAL '12 hours',
            CURRENT_TIMESTAMP - INTERVAL '3 hours',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP + INTERVAL '36 hours'
        ),
        (
            'Частичный возврат по заказу client5',
            'Клиент просит возврат за один товар из комплекта.',
            'IN_PROGRESS',
            'MEDIUM',
            (SELECT id FROM users WHERE email = 'client5@example.com'),
            (SELECT id FROM users WHERE email = 'operator3@example.com'),
            (SELECT id FROM categories WHERE name = 'Возврат'),
            CURRENT_TIMESTAMP - INTERVAL '22 hours',
            CURRENT_TIMESTAMP - INTERVAL '10 hours',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP + INTERVAL '2 hours'
        ),
        (
            'Сбой при создании заказа client5',
            'Оформление заказа завершается ошибкой 502.',
            'NEW',
            'HIGH',
            (SELECT id FROM users WHERE email = 'client5@example.com'),
            NULL,
            (SELECT id FROM categories WHERE name = 'Техническая проблема'),
            CURRENT_TIMESTAMP - INTERVAL '40 minutes',
            CURRENT_TIMESTAMP - INTERVAL '40 minutes',
            CAST(NULL AS TIMESTAMP),
            CURRENT_TIMESTAMP + INTERVAL '23 hours'
        )
) AS seed(
    title,
    description,
    status,
    priority,
    author_id,
    assignee_id,
    category_id,
    created_at,
    updated_at,
    closed_at,
    due_at
)
WHERE NOT EXISTS (
    SELECT 1
    FROM tickets t
    WHERE t.title = seed.title
);
