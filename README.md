# Client Request System

Веб-приложение для работы с заявками. В одном `docker-compose.yml` поднимаются:

- PostgreSQL с постоянным volume для данных
- Spring Boot backend с автоматическим запуском Flyway-миграций
- Vue frontend, который отдаётся через Nginx

## Быстрый старт

### 1. Подготовьте окружение

Скопируйте шаблон переменных окружения:

```sh
cp .env.example .env
```

Для локального запуска можно оставить значения по умолчанию. В этом проекте они уже подходят для compose-конфига:

- `POSTGRES_DB=client_request_system`
- `POSTGRES_USER=postgres`
- `POSTGRES_PASSWORD=postgres`
- `BACKEND_PORT=8081`
- `FRONTEND_PORT=8082`
- `APP_JWT_SECRET=change-me-change-me-change-me-change-me-1234567890`

Если меняете `BACKEND_PORT`, проверьте `VITE_API_BASE_URL`, чтобы frontend смотрел на правильный адрес backend.

### 2. Запустите приложение

```sh
docker compose up --build -d
```

### 3. Проверьте, что сервисы поднялись

```sh
docker compose ps
```

После старта откройте:

- Frontend: http://localhost:8082
- Backend API: http://localhost:8081/api
- PostgreSQL: localhost:5432

### 4. Как проверить работу приложения

1. Откройте страницу входа и убедитесь, что фронтенд загружается без ошибок.
2. Авторизуйтесь с тестовой учетной записью, которая создаётся Flyway-миграциями. В базе есть, как минимум, такие пользователи:
	- `admin@example.com`
	- `operator@example.com`
	- `client@example.com`
3. После входа проверьте, что доступны основные разделы:
	- список заявок `/tickets`
	- создание заявки `/tickets/create`
	- карточка заявки `/tickets/:id`
	- отчёты `/reports` для роли `ADMIN`
4. При необходимости проверьте backend напрямую, например через браузер или API-клиент, по адресу `http://localhost:8081/api`.

## Остановка

Остановить сервисы:

```sh
docker compose down
```

Чтобы удалить ещё и данные PostgreSQL:

```sh
docker compose down -v
```

## Полезные заметки

- Схема базы и тестовые данные находятся в `backend/app/src/main/resources/db/migration`.
- Backend использует context path `/api`.
- Frontend получает адрес API на этапе сборки через `VITE_API_BASE_URL`.
- Для продакшена обязательно замените `APP_JWT_SECRET` на собственный секрет.
