# Role-Based Learning Platform Backend

A high-performance, reactive backend service built with **Vert.x** and **Java 17**, designed for educational platforms requiring role-based access control and AI-powered identity verification. The system leverages non-blocking, asynchronous architecture using RxJava3 for optimal scalability and performance.

## 🛠️ Tech Stack

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Vert.x](https://img.shields.io/badge/Vert.x-5.0.7-purple?style=flat-square&logo=eclipse-vert.x)
![Ebean ORM](https://img.shields.io/badge/Ebean-15.5.0-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue?style=flat-square&logo=mysql)
![RxJava3](https://img.shields.io/badge/RxJava3-3.x-red?style=flat-square&logo=reactivex)
![Google Gemini AI](https://img.shields.io/badge/Google%20Gemini-AI-green?style=flat-square&logo=google)

## ✨ Key Features

### 🔐 Authentication & Authorization
- **JWT-based Authentication** with secure token management
- **Role-Based Access Control (RBAC)** supporting three roles:
  - `ADMIN` - Full system access and user management
  - `TEACHER` - Educational content management
  - `STUDENT` - Learning platform access

### 👥 User Management
- **Admin-only User Onboarding** with role assignment
- **Soft Delete Functionality** for data integrity
- **Profile Management** with role-specific attributes

### 📊 Async Bulk Operations
- **CSV Processing** for bulk user creation
- **Reactive Streams** implementation for non-blocking file processing
- **Background Job Processing** with status tracking

### 🤖 AI-Powered KYC Verification
- **Automated Identity Verification** using Google Gemini 2.0 Flash Vision API
- **ID Card Image Analysis** for fraud detection and data validation
- **Real-time Document Processing** with confidence scoring

### 📝 Audit & Logging
- **Asynchronous Audit Logging** for all admin actions
- **Comprehensive Activity Tracking** with timestamps and user context

## 🏗️ Architecture

The application follows a **Layered Architecture** pattern ensuring separation of concerns and maintainability:

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Router    │───▶│   Handler   │───▶│   Service   │───▶│ Repository  │───▶│  Database   │
│  (Routes)   │    │ (HTTP Logic)│    │(Business    │    │(Data Access)│    │   (MySQL)   │
│             │    │             │    │ Logic)      │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

- **Router Layer**: HTTP request routing and middleware application
- **Handler Layer**: Request/response processing and validation
- **Service Layer**: Business logic and orchestration
- **Repository Layer**: Data access and ORM operations
- **Database Layer**: MySQL data persistence

## 🚀 Setup & Installation

### Prerequisites
- **JDK 17** or higher
- **MySQL 8.0+** 
- **Maven 3.6+**

### Database Configuration

1. Create a MySQL database:
```sql
CREATE DATABASE role_db;
```

2. Update `src/main/resources/application.properties`:
```properties
datasource.db.url=jdbc:mysql://localhost:3306/role_db?useSSL=false&allowPublicKeyRetrieval=true
datasource.db.username=your_username
datasource.db.password=your_password
```

### Build & Run

1. **Clone and build the project:**
```bash
git clone <repository-url>
cd backendassn
mvn clean install
```

2. **Run the application:**
```bash
java -jar target/backendassn-1.0-SNAPSHOT.jar
```

The server will start on `http://localhost:8080`

## 📚 API Documentation

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/auth/login` | POST | User authentication | ❌ |
| `/admin/onboard` | POST | Create new user account | ✅ (Admin) |
| `/admin/users/bulk` | POST | Bulk user upload via CSV | ✅ (Admin) |
| `/kyc/submit` | POST | Submit KYC document (multipart) | ✅ (All roles) |
| `/auth/logout` | POST | Invalidate JWT token | ✅ (All roles) |
| `/admin/users` | GET | List all users | ✅ (Admin) |

### Example Requests

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@example.com", "password": "password123"}'
```

**KYC Upload:**
```bash
curl -X POST http://localhost:8080/kyc/submit \
  -H "Authorization: Bearer <jwt-token>" \
  -F "document=@id_card.jpg"
```

## ⚙️ Environment Variables

Configure the following properties in `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
datasource.db.url=jdbc:mysql://localhost:3306/role_db
datasource.db.username=your_db_username
datasource.db.password=your_db_password

# JWT Configuration
jwt.secret=your-super-secret-jwt-key-minimum-256-bits
jwt.expiryMs=86400000

# Google Gemini AI Configuration
ai.apiKey=your-google-ai-api-key
ai.model=gemini-3-flash-preview
```

## 🔧 Troubleshooting

### Common Issues

**AI Service Failures:**
- Ensure **Data Retention** is enabled in your Google AI/OpenRouter console
- Verify your API key has proper permissions for Vision API
- Check API quota limits

**Database Connection Issues:**
- Confirm MySQL service is running: `systemctl status mysql`
- Verify database credentials and connection URL
- Check firewall settings for port 3306

**Application Startup Issues:**
- Ensure JDK 17+ is installed: `java -version`
- Verify Maven build completed successfully
- Check application logs for detailed error messages

### Logs Location
Application logs are output to console. For production deployment, configure logging to files using logback configuration.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request