### 1. Настройка окружения

Создайте файл `.env` в корне проекта:

```bash
# Database
POSTGRES_USER=admin
POSTGRES_PASSWORD=secure_password
POSTGRES_DB=bankdb

# Spring
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}
SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}

# JWT
JWT_SECRET=your-256-bit-base64-secret
JWT_EXPIRATION_MS=3600000
```

2. Запуск системы
```bash
docker-compose build --no-cache
docker-compose up

```
Доступ по адресу:
```bash
http://localhost:8080
```
