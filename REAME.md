# 📝 Todo App - Full-Stack Task Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?style=for-the-badge&logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?style=for-the-badge&logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?style=for-the-badge&logo=typescript)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

A modern, full-stack todo application with a Spring Boot backend and React frontend, featuring JWT authentication, drag-and-drop functionality, and real-time updates.

[Features](#-features) • [Tech Stack](#-tech-stack) • [Getting Started](#-getting-started) • [API Documentation](#-api-documentation) • [Contributing](#-contributing)

</div>

---

## 🎯 Features

### Backend
- ✅ **RESTful API** with Spring Boot 3.3
- 🔐 **JWT Authentication** with Spring Security
- 🗄️ **PostgreSQL Database** with JPA/Hibernate
- 📊 **Statistics & Analytics** endpoint
- 🔍 **Advanced Filtering** (status, priority, category, date)
- ✨ **Automatic Timestamps** for todos
- 🛡️ **Input Validation** with Bean Validation
- 🌐 **CORS Configuration** for frontend integration

### Frontend
- ⚛️ **React 18** with TypeScript
- 🎨 **Tailwind CSS** for modern UI
- 🌓 **Dark Mode** support
- 🎭 **Drag & Drop** todo reordering
- 📱 **Fully Responsive** design
- 🔄 **Real-time Updates** with state management
- 🎨 **Category Colors** & priority indicators
- 📊 **Statistics Dashboard**
- 🎯 **Bulk Actions** for todos
- 🔍 **Advanced Filters** & search

---

## 🛠️ Tech Stack

### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3.3.0
- **Security:** Spring Security + JWT
- **Database:** PostgreSQL 15
- **ORM:** Hibernate / Spring Data JPA
- **Build Tool:** Maven
- **Validation:** Bean Validation (JSR 380)

### Frontend
- **Framework:** React 18
- **Language:** TypeScript 5
- **Styling:** Tailwind CSS
- **State Management:** Zustand
- **HTTP Client:** Axios
- **Routing:** React Router
- **Icons:** Lucide React
- **Drag & Drop:** @dnd-kit

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 18+** ([Download](https://nodejs.org/))
- **Docker** (for PostgreSQL) ([Download](https://www.docker.com/))
- **Git** ([Download](https://git-scm.com/))

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/yourusername/todo-app.git
cd todo-app
```

### 2️⃣ Backend Setup

#### Start PostgreSQL with Docker

```bash
docker run --name todo-postgres \
  -e POSTGRES_USER=your_username \
  -e POSTGRES_PASSWORD=your_secure_password \
  -e POSTGRES_DB=todo_db \
  -p 5432:5432 \
  -d postgres:15
```

#### Configure Environment Variables

Create a `.env` file in the backend root directory:

```bash
cp .env.example .env
```

Update `.env` with your configuration:

```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/todo_db
DB_USERNAME=your_username
DB_PASSWORD=your_secure_password

# JWT Configuration (Generate a secure random string)
JWT_SECRET=your-very-long-and-secure-jwt-secret-key-minimum-256-bits
JWT_EXPIRATION=86400000

# Admin User (Created on first startup)
ADMIN_EMAIL=admin@todoapp.com
ADMIN_PASSWORD=your_admin_password
ADMIN_NAME=Admin User

# Server Configuration
SERVER_PORT=8080

# CORS Allowed Origins
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

#### Build and Run Backend

```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3️⃣ Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

The frontend will start on `http://localhost:3000`

---

## 📖 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### Todo Endpoints

All todo endpoints require authentication. Include JWT token in headers:
```
Authorization: Bearer <your_jwt_token>
```

#### Get All Todos
```http
GET /api/todos
```

**Query Parameters:**
- `completed` (boolean): Filter by completion status
- `priority` (string): LOW, MEDIUM, HIGH
- `category` (string): Filter by category
- `search` (string): Search in title and description

#### Create Todo
```http
POST /api/todos
Content-Type: application/json

{
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "priority": "HIGH",
  "category": "Documentation",
  "dueDate": "2025-01-25T23:59:59"
}
```

#### Update Todo
```http
PUT /api/todos/{id}
Content-Type: application/json

{
  "title": "Updated title",
  "priority": "MEDIUM"
}
```

#### Toggle Completion
```http
PATCH /api/todos/{id}
Content-Type: application/json

{
  "completed": true
}
```

#### Delete Todo
```http
DELETE /api/todos/{id}
```

#### Get Statistics
```http
GET /api/todos/stats
```

**Response:**
```json
{
  "total": 25,
  "completed": 10,
  "active": 15,
  "overdue": 3,
  "completionPercentage": 40.0
}
```

#### Additional Endpoints
- `GET /api/todos/categories` - Get all categories
- `GET /api/todos/overdue` - Get overdue todos
- `GET /api/todos/due-today` - Get todos due today
- `POST /api/todos/bulk-delete` - Bulk delete todos
- `POST /api/todos/reorder` - Reorder todos

---

## 🏗️ Project Structure

### Backend Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/todoapp/
│   │   │   ├── controller/          # REST endpoints
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── security/            # JWT & Security config
│   │   │   └── TodoAppApplication.java
│   │   └── resources/
│   │       └── application.yml      # Configuration
│   └── test/                        # Unit & Integration tests
├── .env                             # Environment variables (gitignored)
├── .env.example                     # Example environment variables
├── .gitignore
└── pom.xml                          # Maven dependencies
```

### Frontend Structure
```
frontend/
├── src/
│   ├── components/                  # React components
│   │   ├── TodoForm/
│   │   ├── TodoItem/
│   │   ├── TodoFilters/
│   │   ├── DragDropContext/
│   │   └── ui/                      # Reusable UI components
│   ├── contexts/                    # React contexts
│   ├── hooks/                       # Custom hooks
│   ├── services/                    # API services
│   ├── store/                       # State management
│   ├── utils/                       # Utility functions
│   └── App.tsx                      # Main app component
├── public/
├── .env                             # Environment variables (gitignored)
├── .env.example
└── package.json
```

---

## 🧪 Testing

### Backend Testing
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Frontend Testing
```bash
# Run tests
npm test

# Run with coverage
npm test -- --coverage

# E2E tests (if configured)
npm run test:e2e
```

---

## 🔒 Security

- ✅ **JWT Authentication** with secure token generation
- ✅ **Password Hashing** with BCrypt
- ✅ **CORS** properly configured
- ✅ **SQL Injection** protection via JPA
- ✅ **Input Validation** on all endpoints
- ✅ **Environment Variables** for sensitive data
- ⚠️ **HTTPS** recommended for production

---

## 🚢 Deployment

### Backend Deployment (Railway/Render/Heroku)

1. Create a PostgreSQL database
2. Set environment variables on the platform
3. Deploy from GitHub repository

### Frontend Deployment (Vercel/Netlify)

1. Connect your GitHub repository
2. Set build command: `npm run build`
3. Set publish directory: `build` or `dist`
4. Add environment variable: `REACT_APP_API_URL`

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Coding Standards
- Follow Java and TypeScript best practices
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Your Name**

- GitHub: [@yourusername](https://github.com/OmerCeleb)
- LinkedIn: [Your Name](https://linkedin.com/in/yourprofile)
- Email: your.email@example.com

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [React](https://react.dev/) - Frontend library
- [Tailwind CSS](https://tailwindcss.com/) - CSS framework
- [PostgreSQL](https://www.postgresql.org/) - Database

---

## 📊 Project Status

🚧 **Active Development** - This project is actively maintained and updated regularly.

### Roadmap
- [ ] Email notifications for due dates
- [ ] File attachments for todos
- [ ] Todo sharing & collaboration
- [ ] Mobile app (React Native)
- [ ] Advanced analytics dashboard
- [ ] Integration with calendar apps

---

<div align="center">

**⭐ Star this repo if you find it helpful!**

Made with ❤️ using Spring Boot & React

</div>