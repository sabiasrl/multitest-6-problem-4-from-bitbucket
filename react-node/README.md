# School Management System

## 🐳 Docker Compose

```bash
# Start all services
docker-compose up -d --build

# Stop all services
docker-compose down

# Stop and remove volumes (fresh start)
docker-compose down -v

# View logs
docker-compose logs -f
```

## 🧪 Test Student Endpoints

```bash
./test-student-endpoints.sh
```

Tests all student CRUD endpoints and automatically manages Docker Compose.

## 🚀 Run Locally

### Backend
```bash
cd backend
npm install
npm start
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend: http://localhost:5173 | Backend: http://localhost:5007

## 🧪 Unit Tests

### Backend
```bash
cd backend
npm test              # Run all tests
npm run test:watch    # Watch mode
npm run test:coverage # With coverage
```

### Frontend
```bash
cd frontend
npm test
```

## 📝 Demo Credentials

- Email: `admin@school-admin.com`
- Password: `3OU4zn3q6Zh9`

