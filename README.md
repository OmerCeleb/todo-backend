# 📝 Modern Todo Application - Backend

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Test Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen.svg)](https://github.com/OmerCeleb/todo-backend)

> A production-ready RESTful API for todo management with JWT authentication, built with Spring Boot and PostgreSQL.

[🌐 Live Demo](#) | [📚 API Documentation](http://localhost:8080/swagger-ui.html) | [💻 Frontend Repository](https://github.com/OmerCeleb/todo-app-frontend)

---

## ✨ Features

### 🔐 Authentication & Security
- JWT-based authentication with refresh tokens
- BCrypt password hashing
- Role-based access control (USER, ADMIN)
- Secure token validation and expiration

### ✅ Todo Management
- Complete CRUD operations
- Priority levels (LOW, MEDIUM, HIGH)
- Category organization
- Due date tracking with overdue detection
- Search and advanced filtering
- Bulk operations (delete, update)

### 📊 Statistics & Analytics
- Real-time todo statistics
- Completion tracking
- Overdue todo monitoring
- Category-based analytics

### 🛠️ Technical Highlights
- **Global Exception Handling**: Consistent error responses across the API
- **OpenAPI/Swagger Documentation**: Interactive API documentation
- **Comprehensive Testing**: 29 unit and integration tests (100% passing)
- **Clean Architecture**: Layered architecture with clear separation of concerns
- **Database Migrations**: Automatic schema management with Hibernate

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│              Client (React Frontend)            │
└──────────────────┬──────────────────────────────┘
                   │ HTTP/REST + JWT
┌──────────────────▼──────────────────────────────┐
│         Controller Layer (REST API)             │
│  ┌────────────────────────────────────────┐     │
│  │  TodoController  │  AuthController     │     │
│  └────────────┬───────────────┬───────────┘     │
├───────────────▼───────────────▼─────────────────┤
│              Service Layer                      │
│  ┌────────────────────────────────────────┐     │
│  │  TodoService    │   UserService        │     │
│  └────────────┬───────────────┬───────────┘     │
├───────────────▼───────────────▼─────────────────┤
│           Repository Layer (JPA)                │
│  ┌────────────────────────────────────────┐     │
│  │  TodoRepository │  UserRepository      │     │
│  └────────────┬───────────────┬───────────┘     │
├───────────────▼───────────────▼─────────────────┤
│          Database (PostgreSQL)                  │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 21+**
- **Maven 3.6+**
- **PostgreSQL 15+**
- **Git**

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/OmerCeleb/todo-backend.git
cd todo-backend
```

2. **Set up PostgreSQL**
```bash
# Create database
createdb todo_db

# Or using psql
psql -U postgres
CREATE DATABASE todo_db;
\q
```

3. **Configure environment variables**
```bash
# Create .env file (or set as environment variables)
cp .env.example .env

# Edit .env with your configurations
DB_URL=jdbc:postgresql://localhost:5432/todo_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your-super-secret-key-min-64-characters-for-hs512-algorithm
```

4. **Build and run**
```bash
# Install dependencies and run tests
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

---

## 📚 API Documentation

### Swagger UI (Interactive)
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification (JSON)
```
http://localhost:8080/api-docs
```

### Quick API Overview

#### Authentication Endpoints
```http
POST   /api/auth/register    # Register new user
POST   /api/auth/login        # Login user
POST   /api/auth/refresh      # Refresh JWT token
```

#### Todo Endpoints (Requires Authentication)
```http
GET    /api/todos             # Get all todos (with filters)
POST   /api/todos             # Create new todo
GET    /api/todos/{id}        # Get todo by ID
PUT    /api/todos/{id}        # Update todo
DELETE /api/todos/{id}        # Delete todo
PATCH  /api/todos/{id}        # Toggle todo completion
GET    /api/todos/stats       # Get todo statistics
```

### Example Request

**Create a Todo**
```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive README",
    "priority": "HIGH",
    "category": "Documentation",
    "dueDate": "2025-10-15T23:59:59"
  }'
```

**Response**
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "completed": false,
  "priority": "HIGH",
  "category": "Documentation",
  "dueDate": "2025-10-15T23:59:59",
  "createdAt": "2025-10-09T14:00:00",
  "updatedAt": "2025-10-09T14:00:00"
}
```

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TodoServiceTest

# Generate test coverage report
mvn test jacoco:report
```

**Test Results:** ✅ 29/29 tests passing
- ✅ 11 Service layer tests
- ✅ 10 Todo Controller integration tests
- ✅ 8 Auth Controller integration tests

---

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/todoapp/
│   │   ├── config/              # Configuration classes
│   │   │   ├── OpenAPIConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/          # REST Controllers
│   │   │   ├── AuthController.java
│   │   │   └── TodoController.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── TodoRequestDTO.java
│   │   │   ├── TodoResponseDTO.java
│   │   │   ├── ErrorResponse.java
│   │   │   └── auth/
│   │   ├── entity/              # JPA Entities
│   │   │   ├── Todo.java
│   │   │   └── User.java
│   │   ├── exception/           # Custom Exceptions
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── TodoNotFoundException.java
│   │   │   └── ...
│   │   ├── repository/          # JPA Repositories
│   │   │   ├── TodoRepository.java
│   │   │   └── UserRepository.java
│   │   ├── security/            # Security Components
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── service/             # Business Logic
│   │   │   ├── TodoService.java
│   │   │   └── UserService.java
│   │   └── util/                # Utility Classes
│   │       └── JwtUtil.java
│   └── resources/
│       └── application.yml      # Application Configuration
└── test/                        # Unit & Integration Tests
```

---

## 🛠️ Tech Stack

### Core
- **Java 21** - Modern Java with latest features
- **Spring Boot 3.3.0** - Enterprise-grade application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **PostgreSQL 15** - Production-ready relational database

### Libraries & Tools
- **JWT (jjwt 0.12.3)** - JSON Web Token implementation
- **SpringDoc OpenAPI 2.3.0** - API documentation
- **Hibernate** - ORM for database operations
- **Bean Validation** - Input validation
- **Lombok** - Reduce boilerplate code
- **JUnit 5 & Mockito** - Testing framework

### Development Tools
- **Maven** - Dependency management
- **Spring DevTools** - Hot reload for development
- **H2 Database** - In-memory database for testing

---

## 🔒 Security

- **JWT Authentication**: Stateless authentication with access and refresh tokens
- **BCrypt Password Hashing**: Secure password storage
- **CORS Configuration**: Controlled cross-origin resource sharing
- **SQL Injection Prevention**: Parameterized queries via JPA
- **XSS Protection**: Content security headers
- **HTTPS Support**: Ready for SSL/TLS deployment

---

## 🌍 Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_URL` | PostgreSQL connection URL | - | ✅ |
| `DB_USERNAME` | Database username | - | ✅ |
| `DB_PASSWORD` | Database password | - | ✅ |
| `JWT_SECRET` | JWT signing key (min 64 chars) | - | ✅ |
| `JWT_EXPIRATION` | Token expiration (milliseconds) | 86400000 (24h) | ❌ |
| `SERVER_PORT` | Application port | 8080 | ❌ |

---

## 🚀 Deployment

### Docker (Coming Soon)

```bash
docker-compose up
```

### Manual Deployment

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/todo-backend-0.0.1-SNAPSHOT.jar
```

---

## 📈 Future Enhancements

- [ ] Redis caching for improved performance
- [ ] Rate limiting for API endpoints
- [ ] Email notifications for due date reminders
- [ ] File attachments for todos
- [ ] Todo sharing and collaboration features
- [ ] WebSocket support for real-time updates
- [ ] Docker containerization
- [ ] CI/CD pipeline with GitHub Actions
- [ ] Kubernetes deployment configuration

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Ömer Çelebi**

- LinkedIn: [@omercelebii](https://www.linkedin.com/in/omercelebii/)
- Email: omer534@outlook.com
- GitHub: [@OmerCeleb](https://github.com/OmerCeleb)

---

## 🙏 Acknowledgments

- Spring Boot Team for the excellent framework
- PostgreSQL Community for the robust database
- All contributors and supporters of this project

---

<div align="center">

**⭐ If you found this project helpful, please consider giving it a star!**

[Report Bug](https://github.com/OmerCeleb/todo-backend/issues) · [Request Feature](https://github.com/OmerCeleb/todo-backend/issues)

Made with ❤️ by Ömer Çelebi

</div>
