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
            'Не приходит письмо подтверждения',
            'После регистрации не приходит письмо подтверждения на почту.',
            'NEW',
            'MEDIUM',
            (SELECT id FROM users WHERE email = 'client@example.com'),
            NULL,
            (SELECT id FROM categories WHERE name = 'Техническая проблема'),
            CURRENT_TIMESTAMP - INTERVAL '3 hours',
            CURRENT_TIMESTAMP - INTERVAL '3 hours',
            NULL,
            CURRENT_TIMESTAMP + INTERVAL '21 hours'
        ),
        (
            'Ошибка при оплате картой',
            'Оплата завершается ошибкой 500, заказ не создается.',
            'IN_PROGRESS',
            'HIGH',
            (SELECT id FROM users WHERE email = 'client@example.com'),
            (SELECT id FROM users WHERE email = 'operator@example.com'),
            (SELECT id FROM categories WHERE name = 'Оплата'),
            CURRENT_TIMESTAMP - INTERVAL '6 hours',
            CURRENT_TIMESTAMP - INTERVAL '2 hours',
            NULL,
            CURRENT_TIMESTAMP + INTERVAL '2 hours'
        ),
        (
            'Перенести адрес доставки',
            'Нужно изменить адрес доставки в уже оформленном заказе.',
            'RESOLVED',
            'LOW',
            (SELECT id FROM users WHERE email = 'client@example.com'),
            (SELECT id FROM users WHERE email = 'operator@example.com'),
            (SELECT id FROM categories WHERE name = 'Доставка'),
            CURRENT_TIMESTAMP - INTERVAL '2 days',
            CURRENT_TIMESTAMP - INTERVAL '1 day',
            NULL,
            CURRENT_TIMESTAMP + INTERVAL '1 day'
        ),
        (
            'Возврат средств по отмененному заказу',
            'Заказ отменен, но возврат средств не поступил.',
            'CLOSED',
            'MEDIUM',
            (SELECT id FROM users WHERE email = 'client@example.com'),
            (SELECT id FROM users WHERE email = 'operator@example.com'),
            (SELECT id FROM categories WHERE name = 'Возврат'),
            CURRENT_TIMESTAMP - INTERVAL '5 days',
            CURRENT_TIMESTAMP - INTERVAL '3 days',
            CURRENT_TIMESTAMP - INTERVAL '3 days',
            CURRENT_TIMESTAMP - INTERVAL '4 days'
        ),
        (
            'Тестовая заявка от администратора',
            'Проверка видимости заявок для разных ролей в системе.',
            'IN_PROGRESS',
            'HIGH',
            (SELECT id FROM users WHERE email = 'admin@example.com'),
            (SELECT id FROM users WHERE email = 'operator@example.com'),
            (SELECT id FROM categories WHERE name = 'Другое'),
            CURRENT_TIMESTAMP - INTERVAL '10 hours',
            CURRENT_TIMESTAMP - INTERVAL '30 minutes',
            NULL,
            CURRENT_TIMESTAMP + INTERVAL '6 hours'
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
