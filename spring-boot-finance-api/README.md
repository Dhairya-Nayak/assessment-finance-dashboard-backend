# Finance Dashboard API

A comprehensive Spring Boot backend API for a finance dashboard application with role-based access control, financial record management, and analytics.

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.4**
- **Spring Security with JWT**
- **Spring Data JPA**
- **MySQL 8.x**
- **Lombok**
- **Caffeine Cache**

## Project Structure

```
src/main/java/com/financedashboard/
├── FinanceDashboardApplication.java     # Main application class
├── config/                               # Configuration classes
│   ├── SecurityConfig.java              # Security and JWT configuration
│   ├── AuditConfig.java                 # JPA auditing configuration
│   ├── CacheConfig.java                 # Caching configuration
│   ├── AsyncConfig.java                 # Async execution configuration
│   └── JacksonConfig.java               # JSON serialization config
├── controller/                           # REST controllers
│   ├── AuthController.java              # Authentication endpoints
│   ├── UserController.java              # User management endpoints
│   ├── CategoryController.java          # Category management endpoints
│   ├── FinancialRecordController.java   # Financial records endpoints
│   ├── DashboardController.java         # Dashboard and analytics endpoints
│   └── AuditController.java             # Audit log endpoints
├── dto/                                  # Data Transfer Objects
│   ├── request/                         # Request DTOs
│   └── response/                        # Response DTOs
├── entity/                               # JPA entities
│   ├── User.java
│   ├── Role.java
│   ├── Category.java
│   ├── FinancialRecord.java
│   ├── RefreshToken.java
│   └── AuditLog.java
├── exception/                            # Custom exceptions and handlers
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── ...
├── repository/                           # JPA repositories
├── security/                             # Security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserPrincipal.java
│   └── CustomUserDetailsService.java
├── service/                              # Business logic services
│   ├── AuthService.java
│   ├── UserService.java
│   ├── CategoryService.java
│   ├── FinancialRecordService.java
│   ├── DashboardService.java
│   └── AuditService.java
└── specification/                        # JPA Specifications
    └── FinancialRecordSpecification.java
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- MySQL 8.x

### Database Setup

1. Create the database by running the schema script:

```bash
mysql -u root -p < database/schema.sql
```

2. (Optional) Load sample data:

```bash
mysql -u root -p finance_dashboard < database/sample_data.sql
```

### Configuration

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/finance_dashboard
    username: your_username
    password: your_password
```

Update the JWT secret key for production:

```yaml
jwt:
  secret: YOUR_SECURE_SECRET_KEY_AT_LEAST_256_BITS
```

### Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/login` | User login | Public |
| POST | `/api/auth/register` | User registration | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| POST | `/api/auth/logout` | Logout user | Authenticated |
| GET | `/api/auth/me` | Get current user | Authenticated |

### Users

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users` | List all users | Admin |
| GET | `/api/users/{id}` | Get user by ID | Admin |
| POST | `/api/users` | Create new user | Admin |
| PUT | `/api/users/{id}` | Update user | Admin |
| DELETE | `/api/users/{id}` | Delete user | Admin |
| GET | `/api/users/profile` | Get current profile | Authenticated |
| PUT | `/api/users/profile` | Update profile | Authenticated |
| POST | `/api/users/change-password` | Change password | Authenticated |

### Categories

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/categories` | List all categories | Authenticated |
| GET | `/api/categories/{id}` | Get category by ID | Authenticated |
| GET | `/api/categories/type/{type}` | Get by type | Authenticated |
| POST | `/api/categories` | Create category | Admin |
| PUT | `/api/categories/{id}` | Update category | Admin |
| DELETE | `/api/categories/{id}` | Delete category | Admin |

### Financial Records

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/records` | List records with filters | Authenticated |
| GET | `/api/records/{id}` | Get record by ID | Authenticated |
| GET | `/api/records/my-records` | Get current user's records | Authenticated |
| GET | `/api/records/search` | Search records | Authenticated |
| GET | `/api/records/recent` | Get recent transactions | Authenticated |
| POST | `/api/records` | Create record | Admin |
| PUT | `/api/records/{id}` | Update record | Admin |
| DELETE | `/api/records/{id}` | Delete record | Admin |

### Dashboard

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/dashboard/summary` | Get dashboard summary | Authenticated |
| GET | `/api/dashboard/quick` | Quick summary | Authenticated |
| GET | `/api/dashboard/analytics/category-breakdown` | Category analysis | Analyst, Admin |
| GET | `/api/dashboard/analytics/monthly-trends` | Monthly trends | Analyst, Admin |
| GET | `/api/dashboard/analytics/weekly-trends` | Weekly trends | Analyst, Admin |
| GET | `/api/dashboard/analytics/daily-summary` | Daily summary | Analyst, Admin |
| GET | `/api/dashboard/analytics/top-categories` | Top categories | Analyst, Admin |

### Audit Logs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/audit` | Get audit logs | Admin |
| GET | `/api/audit/entity/{type}/{id}` | Logs by entity | Admin |
| GET | `/api/audit/user/{userId}` | Logs by user | Admin |
| GET | `/api/audit/stats` | Audit statistics | Admin |

## Roles and Permissions

| Role | Description | Permissions |
|------|-------------|-------------|
| ROLE_VIEWER | Basic user | Read-only access to own records and dashboard |
| ROLE_ANALYST | Data analyst | Viewer permissions + analytics features |
| ROLE_ADMIN | Administrator | Full CRUD access, user management, audit logs |

## Default Admin User

After running the schema script, a default admin user is created:

- **Username:** admin
- **Password:** Admin@123
- **Email:** admin@financedashboard.com

## Security Features

- JWT-based stateless authentication
- Password encryption with BCrypt (strength 12)
- Role-based access control with @PreAuthorize
- Refresh token support with revocation
- Request validation with Bean Validation
- SQL injection prevention with parameterized queries
- CORS configuration

## Caching

Dashboard data is cached using Caffeine cache with a 5-10 minute TTL to improve performance. Cache is automatically evicted when records are created, updated, or deleted.

## Optimistic Locking

The application uses JPA's @Version annotation for optimistic locking to handle concurrent updates safely. If a conflict is detected, an HTTP 409 Conflict response is returned.

## Audit Logging

All CRUD operations and authentication events are automatically logged to the audit_logs table with:
- Entity type and ID
- Action performed
- User who performed the action
- Old and new values (for updates)
- IP address and user agent
- Timestamp

## Error Handling

The application provides consistent error responses in the following format:

```json
{
    "timestamp": "2024-06-15T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid input data",
    "path": "/api/records",
    "fieldErrors": [
        {
            "field": "amount",
            "message": "Amount must be greater than 0",
            "rejectedValue": -100
        }
    ]
}
```

## Building for Production

```bash
mvn clean package -DskipTests
java -jar target/finance-dashboard-api-1.0.0.jar
```

## Environment Variables

For production, configure these environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://production-host:3306/finance_dashboard
SPRING_DATASOURCE_USERNAME=app_user
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=your_production_secret_key
```

## License

This project is licensed under the MIT License.
