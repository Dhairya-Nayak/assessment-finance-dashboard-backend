---

# Finance Dashboard API

A Spring Boot backend API for a finance dashboard application with role-based access control, financial record management, dashboard summaries, analytics, and audit logging.

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.4**
- **Spring Security with JWT**
- **Spring Data JPA**
- **MySQL 8.x**
- **Lombok**
- **Caffeine Cache**
- **Swagger / OpenAPI**

---

## Project Structure

```text
src/main/java/com/financedashboard/
├── FinanceDashboardApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── AuditConfig.java
│   ├── CacheConfig.java
│   ├── AsyncConfig.java
│   └── JacksonConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── CategoryController.java
│   ├── FinancialRecordController.java
│   ├── DashboardController.java
│   └── AuditController.java
├── dto/
│   ├── request/
│   └── response/
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── Category.java
│   ├── FinancialRecord.java
│   ├── RefreshToken.java
│   └── AuditLog.java
├── exception/
├── repository/
├── security/
├── service/
└── specification/
```

---

## Features Quck Walkthrough

- JWT-based authentication and refresh token support
- Role-based access control (`VIEWER`, `ANALYST`, `ADMIN`)
- User management APIs
- Category management APIs
- Financial record CRUD and filtering
- Dashboard summary APIs
- Analytics APIs for trends and category breakdown
- Audit log APIs
- Validation and global exception handling
- Caching for selected dashboard/category data
- Optimistic locking for concurrent updates

---
# Features Description 

## 1. User and Role Management
The system provides complete backend support for managing users and controlling access through clearly defined roles.

### Included capabilities
- User registration and authentication
- Admin-driven user creation and management
- Role assignment for `VIEWER`, `ANALYST`, and `ADMIN`
- User status management such as:
  - `ACTIVE`
  - `INACTIVE`
  - `SUSPENDED`
  - `DELETED`
- Role-aware access restrictions across all APIs
- Profile management and password change for authenticated users
- Admin-only user activation, deactivation, and soft deletion

### Role model implemented
- **Viewer**
  - Can view dashboard summary and read permitted data
- **Analyst**
  - Can view records and access analytical insights
- **Admin**
  - Full access to user management, financial records, categories, dashboards, and audit logs

This ensures role-based behavior is explicit, enforceable, and easy to understand.

---

## 2. Financial Records Management
The backend supports structured management of finance records such as incomes and expenses.

### Record fields supported
- Amount
- Record type (`INCOME` / `EXPENSE`)
- Category
- Transaction date
- Description
- Notes
- Tags
- Reference number
- Recurring flag and recurring frequency
- Attachment URL
- Record status (`PENDING`, `CONFIRMED`, `CANCELLED`, `DELETED`)

### Operations implemented
- Create records
- View records
- Update records
- Delete records using soft delete
- Cancel records
- Retrieve recent transactions
- Search records
- Filter records using:
  - date range
  - category
  - type
  - status
  - amount range
  - search keyword

This goes beyond basic CRUD and demonstrates structured record lifecycle handling.

---

## 3. Dashboard Summary and Analytics APIs
The project includes dedicated dashboard endpoints designed for frontend reporting and analytics, not just raw data retrieval.

### Summary metrics implemented
- Total income
- Total expenses
- Net balance
- Savings rate
- Total transactions
- Recent transactions

### Aggregated analytics implemented
- Category-wise breakdown
- Category contribution percentages
- Monthly trends
- Weekly trends
- Daily summaries
- Top categories
- User-specific dashboard summary for admin use

These APIs are designed to support real dashboard visualization and summary-driven reporting.

---

## 4. Access Control Logic
Access control is enforced at the backend using Spring Security and role-based authorization rules.

### Security implementation highlights
- JWT-based stateless authentication
- Role-based restrictions using Spring Security configuration and method-level authorization
- Different API permissions enforced for viewers, analysts, and admins
- Authenticated-only access for protected resources
- Admin-only access for sensitive operations such as:
  - user management
  - category management
  - financial record creation/modification
  - audit log access

### Role-based behavior enforced
- Viewer cannot create or modify financial records
- Viewer cannot access admin or audit APIs
- Analyst can access analytics but not admin management APIs
- Admin has full system access

This demonstrates clear backend authorization logic aligned with the requirement.

---

## 5. Validation and Error Handling
The application includes structured validation and meaningful error responses to simulate real-world backend behavior.

### Validation support
- Request body validation using Bean Validation annotations
- Field-level validation for:
  - email format
  - password strength
  - required fields
  - amount boundaries
  - date validity
  - color format
  - enum constraints

### Error handling support
- Global exception handling with consistent JSON responses
- Proper HTTP status codes such as:
  - `400 Bad Request`
  - `401 Unauthorized`
  - `403 Forbidden`
  - `404 Not Found`
  - `409 Conflict`
  - `500 Internal Server Error`
- Business rule protection such as:
  - category type mismatch
  - duplicate resources
  - invalid status changes
  - invalid recurring record input
  - delete restrictions for protected/system entities

This demonstrates attention to correctness, reliability, and user-friendly API behavior.

---

## 6. Data Persistence and Modeling
The project uses a relational persistence model built with MySQL and JPA/Hibernate.

### Persistence design highlights
- MySQL-based relational data storage
- JPA entity modeling with proper relationships
- Indexed columns for frequent query patterns
- Many-to-many mapping for users and roles
- One-to-many and many-to-one mappings for records and categories
- Self-referencing category hierarchy
- Version fields for optimistic locking
- Soft delete patterns for users and records
- Audit-ready timestamp and user tracking fields

This satisfies the persistence requirement with a production-style relational model.

---

# Additional Enhancements Implemented

## Authentication and Session Security
- JWT access tokens
- Refresh token support
- Logout with token revocation
- Password encryption using BCrypt

## Pagination and Search
- Pagination for users, records, and audit logs
- Search support for:
  - users
  - records

## Soft Delete Support
- User soft delete via status
- Record soft delete via status
- Category deactivation instead of hard delete

## Optimistic Locking
- `@Version` based concurrency handling
- Prevents silent overwrite during concurrent updates

## Audit Logging
- Tracks important operations and authentication events
- Supports querying logs by:
  - entity
  - user
  - date range
  - action statistics

## Caching
- Category and dashboard responses cached with Caffeine
- Cache eviction on relevant data changes

## API Documentation
- Swagger / OpenAPI integration for interactive API exploration and testing

## Roles and Permissions

| Role | Description | Permissions |
|------|-------------|-------------|
| `ROLE_VIEWER` | Basic user | View dashboard summary, categories, own/basic records, manage own profile/password |
| `ROLE_ANALYST` | Analyst user | Viewer permissions + analytics endpoints |
| `ROLE_ADMIN` | Administrator | Full user/category/record management + audit access |

---

## Test User Credentials

Use these seeded sample users after running the database scripts:

| Role | Username | Password | Email |
|------|----------|----------|-------|
| Admin | `testadmin1` | `Test@1234` | `testadmin1@example.com` |
| Viewer | `viewer1` | `Test@1234` | `viewer1@example.com` |
| Analyst | `analyst1` | `Test@1234` | `analyst1@example.com` |

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- MySQL 8.x
- Postman or Swagger UI for API testing

---

## Database Setup

### 1. Create schema
Run the schema script:

```bash
mysql -u root -p < database/schema.sql
```
### Preferable way is just copy the whole script from file and got MySQL workbench and open new SQL tab and paste it and execute the whole querry at one go 


### 2. Load sample data
Run the data script:

```bash
mysql -u root -p finance_dashboard < database/sample_data.sql
```
### Preferable way is just copy the whole script from file and got MySQL workbench and open new SQL tab and paste it and execute the whole querry at one go 

---

## Configuration

Update `src/main/resources/application.properties` with your local database and JWT settings.

### Example `application.properties`

```properties
spring.application.name=finance-dashboard-api

spring.datasource.url=jdbc:mysql://localhost:3306/finance_dashboard?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username={YOUR_USERNAME}
spring.datasource.password={YOUR_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1200000

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.open-in-view=false

spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m

jwt.secret=VGhpc0lzQVN1cGVyU2VjdXJlQmFzZTY0RW5jb2RlZEtleUZvckpXVFRva2VuR2VuZXJhdGlvbjEyMzQ1Ng==
jwt.expiration=86400000
jwt.refresh-expiration=604800000

server.port=8080

logging.level.root=INFO
logging.level.com.financedashboard=DEBUG
logging.level.org.springframework.security=INFO
```

### Important JWT Note
`jwt.secret` must be a **Base64-encoded** key because the application decodes it before signing JWTs.

---

## Running the Application

### From IDE
Run:
- `FinanceDashboardApplication`

### From Maven
```bash
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## API Endpoints

## Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/login` | User login | Public |
| POST | `/api/auth/register` | User registration | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| POST | `/api/auth/logout` | Logout user | Authenticated |
| GET | `/api/auth/me` | Get current user | Authenticated |

## Users

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users` | List all users | Admin |
| GET | `/api/users/{id}` | Get user by ID | Admin / Self |
| GET | `/api/users/username/{username}` | Get by username | Admin |
| GET | `/api/users/role/{roleName}` | Get users by role | Admin |
| GET | `/api/users/profile` | Current profile | Authenticated |
| POST | `/api/users` | Create user | Admin |
| PUT | `/api/users/{id}` | Update user | Admin |
| PUT | `/api/users/profile` | Update own profile | Authenticated |
| POST | `/api/users/change-password` | Change password | Authenticated |
| POST | `/api/users/{id}/activate` | Activate user | Admin |
| POST | `/api/users/{id}/deactivate` | Deactivate user | Admin |
| DELETE | `/api/users/{id}` | Soft delete user | Admin |

## Categories

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/categories` | List all categories | Authenticated |
| GET | `/api/categories/{id}` | Get category by ID | Authenticated |
| GET | `/api/categories/type/{type}` | Categories by type | Authenticated |
| GET | `/api/categories/for-record/{type}` | Valid categories for record type | Authenticated |
| GET | `/api/categories/hierarchy` | Category hierarchy | Authenticated |
| GET | `/api/categories/system` | System categories | Authenticated |
| GET | `/api/categories/custom` | Custom categories | Authenticated |
| POST | `/api/categories` | Create category | Admin |
| PUT | `/api/categories/{id}` | Update category | Admin |
| DELETE | `/api/categories/{id}` | Soft delete category | Admin |

## Financial Records

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/records` | List records with filters | Authenticated |
| GET | `/api/records/{id}` | Get record by ID | Authenticated |
| GET | `/api/records/my-records` | Current user's records | Authenticated |
| GET | `/api/records/user/{userId}` | Records by user | Admin / Self |
| GET | `/api/records/date-range` | Records by date range | Authenticated |
| GET | `/api/records/search` | Search records | Authenticated |
| GET | `/api/records/recent` | Recent transactions | Authenticated |
| POST | `/api/records` | Create record | Admin |
| PUT | `/api/records/{id}` | Update record | Admin |
| DELETE | `/api/records/{id}` | Soft delete record | Admin |
| POST | `/api/records/{id}/cancel` | Cancel record | Admin |

## Dashboard

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/dashboard/summary` | Dashboard summary | Authenticated |
| GET | `/api/dashboard/quick` | Quick summary | Authenticated |
| GET | `/api/dashboard/analytics/category-breakdown` | Category breakdown | Analyst, Admin |
| GET | `/api/dashboard/analytics/monthly-trends` | Monthly trends | Analyst, Admin |
| GET | `/api/dashboard/analytics/weekly-trends` | Weekly trends | Analyst, Admin |
| GET | `/api/dashboard/analytics/daily-summary` | Daily summary | Analyst, Admin |
| GET | `/api/dashboard/analytics/top-categories` | Top categories | Analyst, Admin |
| GET | `/api/dashboard/user/{userId}/summary` | Summary for a specific user | Admin |

## Audit Logs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/audit` | Get audit logs | Admin |
| GET | `/api/audit/entity/{type}/{id}` | Logs by entity | Admin |
| GET | `/api/audit/user/{userId}` | Logs by user | Admin |
| GET | `/api/audit/user/{userId}/login-history` | Login history | Admin |
| GET | `/api/audit/date-range` | Logs by date range | Admin |
| GET | `/api/audit/stats` | Audit statistics | Admin |

---

# API Testing Flow

This section is written so a reviewer can test the application step by step even without deep technical knowledge.

## Before Testing
1. Start the application
2. Open Swagger UI(preferable to get overall view of api and schemas) or Postman(Preferable for testing)
3. Ensure database scripts have already been executed
4. Keep the test user credentials table handy

---

## Step 1 - Login and collect tokens

You need to log in with each role and save the returned access token.(MAKE SURE IN POSTMAN AUTHORIZATION IS SET ON NO AUTH WHILE LOGIN)

### Admin Login
**POST** `/api/auth/login`

Body:
```json
{
  "usernameOrEmail": "testadmin1",
  "password": "Test@1234"
}
```

Save:
- `accessToken` as **Admin Token**
- `refreshToken` as **Admin Refresh Token**

### Viewer Login
**POST** `/api/auth/login`

Body:
```json
{
  "usernameOrEmail": "viewer1",
  "password": "Viewer@1234"
}
```

Save:
- `accessToken` as **Viewer Token**

### Analyst Login
**POST** `/api/auth/login`

Body:
```json
{
  "usernameOrEmail": "analyst1",
  "password": "Test@1234"
}
```

Save:
- `accessToken` as **Analyst Token**

---

## Step 2 - How to use tokens

For protected endpoints, add this header:

```text
Authorization: Bearer <ACCESS_TOKEN>
```

Examples:
- Admin APIs → use **Admin Token**
- Viewer tests → use **Viewer Token**
- Analyst tests → use **Analyst Token**

---

# Admin Testing Flow

Use **Admin Token** for every request in this section. (GO TO AUTHORIZATION TAB IN POSTMAN SELECT Bearer Token and paste the Access Token of Admin which you saved and remember this step for other roles testing too use viewer acces token while testing viewer and same for analyst)

## A1. Verify admin identity
- `GET /api/auth/me`
- `GET /api/users/profile`

Expected:
- `200 OK`
- role includes `ROLE_ADMIN`

## A2. User management
Test:
- `GET /api/users`
- `GET /api/users/search?query=analyst&page=0&size=10`
- `GET /api/users/role/VIEWER`
- `GET /api/users/role/ANALYST`

### Create user
**POST** `/api/users`

```json
{
  "username": "viewer3",
  "email": "viewer3@example.com",
  "password": "Test@1234",
  "firstName": "Viewer",
  "lastName": "Three",
  "roles": ["VIEWER"]
}
```

### Update user
**PUT** `/api/users/{id}`

```json
{
  "email": "viewer3updated@example.com",
  "firstName": "ViewerUpdated",
  "lastName": "ThreeUpdated",
  "status": "ACTIVE",
  "roles": ["VIEWER"]
}
```

### Deactivate / Activate
- `POST /api/users/{id}/deactivate`
- `POST /api/users/{id}/activate`

## A3. Categories
Test:
- `GET /api/categories`
- `GET /api/categories/type/INCOME`
- `GET /api/categories/hierarchy`

### Create category
**POST** `/api/categories`

```json
{
  "name": "Office Supplies",
  "description": "Stationery and office expenses",
  "type": "EXPENSE",
  "color": "#2563EB",
  "icon": "briefcase",
  "parentId": null
}
```

### Update category
**PUT** `/api/categories/{id}`

```json
{
  "name": "Office Expense",
  "description": "Updated office-related expenses",
  "type": "EXPENSE",
  "color": "#1D4ED8",
  "icon": "briefcase",
  "parentId": null
}
```

### Delete category
- `DELETE /api/categories/{id}`

## A4. Financial records
### Create income record
**POST** `/api/records`

```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "categoryId": 1,
  "description": "Salary Payment",
  "referenceNumber": "INC-1001",
  "transactionDate": "2026-04-04",
  "notes": "Monthly salary credit",
  "tags": "salary,monthly",
  "isRecurring": true,
  "recurringFrequency": "MONTHLY",
  "attachmentUrl": null
}
```

### Create expense record
**POST** `/api/records`

```json
{
  "amount": 1200.00,
  "type": "EXPENSE",
  "categoryId": 6,
  "description": "Rent Payment",
  "referenceNumber": "EXP-1001",
  "transactionDate": "2026-04-04",
  "notes": "Monthly rent",
  "tags": "rent,housing",
  "isRecurring": true,
  "recurringFrequency": "MONTHLY",
  "attachmentUrl": null
}
```

### Read/filter/search
Test:
- `GET /api/records`
- `GET /api/records?type=EXPENSE`
- `GET /api/records/search?query=rent&page=0&size=10`
- `GET /api/records/recent?limit=10`
- `GET /api/records/date-range?startDate=2026-04-01&endDate=2026-04-30`

### Update record
**PUT** `/api/records/{id}`

```json
{
  "amount": 1300.00,
  "type": "EXPENSE",
  "categoryId": 6,
  "description": "Updated Rent Payment",
  "referenceNumber": "EXP-1001",
  "transactionDate": "2026-04-04",
  "notes": "Updated monthly rent",
  "tags": "rent,housing,updated",
  "isRecurring": true,
  "recurringFrequency": "MONTHLY",
  "attachmentUrl": null
}
```

### Cancel / Delete
- `POST /api/records/{id}/cancel`
- `DELETE /api/records/{id}`

## A5. Dashboard and audit
Test:
- `GET /api/dashboard/summary`
- `GET /api/dashboard/quick`
- `GET /api/dashboard/analytics/monthly-trends?months=12`
- `GET /api/dashboard/analytics/category-breakdown?type=EXPENSE&startDate=2026-04-01&endDate=2026-04-30`
- `GET /api/dashboard/user/{userId}/summary`

Audit:
- `GET /api/audit?page=0&size=20`
- `GET /api/audit/stats?hoursBack=24`

---

# Viewer Testing Flow

Use **Viewer Token** for every request in this section.

## V1. Verify viewer identity
- `GET /api/auth/me`
- `GET /api/users/profile`

Expected:
- `200 OK`
- role includes `ROLE_VIEWER`

## V2. Update profile and password
### Update profile
**PUT** `/api/users/profile`

```json
{
  "email": "viewer1updated@example.com",
  "firstName": "ViewerUpdated",
  "lastName": "OnlyUpdated"
}
```

### Change password
**POST** `/api/users/change-password`

```json
{
  "currentPassword": "Viewer@1234",
  "newPassword": "Viewer@1234",
  "confirmPassword": "Viewer@1234"
}
```

## V3. Allowed viewer APIs
Test:
- `GET /api/categories`
- `GET /api/categories/type/EXPENSE`
- `GET /api/dashboard/summary`
- `GET /api/dashboard/quick`
- `GET /api/records`
- `GET /api/records/my-records`
- `GET /api/records/search?query=rent&page=0&size=10`
- `GET /api/records/recent?limit=10`

## V4. Viewer restricted APIs
These should be blocked:
- `GET /api/users`
- `POST /api/categories`
- `POST /api/records`
- `GET /api/dashboard/analytics/monthly-trends?months=12`
- `GET /api/audit`

---

# Analyst Testing Flow

Use **Analyst Token** for every request in this section.

## N1. Verify analyst identity
- `GET /api/auth/me`
- `GET /api/users/profile`

Expected:
- `200 OK`
- role includes `ROLE_ANALYST`

## N2. Allowed analyst APIs
Test:
- `GET /api/categories`
- `GET /api/dashboard/summary`
- `GET /api/dashboard/quick`
- `GET /api/dashboard/analytics/monthly-trends?months=12`
- `GET /api/dashboard/analytics/category-breakdown?type=EXPENSE&startDate=2026-04-01&endDate=2026-04-30`
- `GET /api/records`
- `GET /api/records/my-records`
- `GET /api/records/search?query=rent&page=0&size=10`

## N3. Analyst restricted APIs
These should be blocked:
- `POST /api/records`
- `POST /api/categories`
- `GET /api/users`
- `GET /api/audit`

---

# Validation and Error Testing

## Register with invalid email
**POST** `/api/auth/register`

```json
{
  "username": "baduser",
  "email": "bad-email",
  "password": "Test@1234",
  "firstName": "Bad",
  "lastName": "User"
}
```

Expected:
- `400`

## Create record with invalid amount
**POST** `/api/records`

```json
{
  "amount": -100,
  "type": "EXPENSE",
  "categoryId": 6,
  "description": "Invalid",
  "transactionDate": "2026-04-04"
}
```

Expected:
- `400`

## Create record with future date
Expected:
- `400`

## Create record with wrong category type
Example:
- use expense category with income mismatch
Expected:
- `400`

---

# Security Notes

## JWT Security
- Stateless authentication using JWT
- BCrypt password hashing
- Refresh token support with revocation


---

# Caching

Dashboard and category data is cached using Caffeine. Cache is cleared automatically when relevant records are created, updated, or deleted.

---

# Optimistic Locking

Entities use `@Version` for optimistic locking. Concurrent modification conflicts return HTTP `409 Conflict`.

---

# Audit Logging

Audit logs capture:
- entity type and id
- action
- acting user
- old/new values
- IP and user agent
- timestamp

---

# Error Response Format

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

---

# Build for Production

```bash
mvn clean package -DskipTests
java -jar target/finance-dashboard-api-1.0.0.jar
```

---

# Environment Variables

For production:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://production-host:3306/finance_dashboard
SPRING_DATASOURCE_USERNAME=app_user
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=your_base64_secret
```

---
# Requirement Fulfillment Summary
## All asked requirements in assignment are included along with some additional/optional features

| Requirement Area | Fulfilled In Project |
|------------------|----------------------|
| User and Role Management | Yes |
| Role-Based Access Control | Yes |
| Financial Records CRUD | Yes |
| Filtering and Search | Yes |
| Dashboard Summary APIs | Yes |
| Analytics Endpoints | Yes |
| Validation and Error Handling | Yes |
| Data Persistence with MySQL | Yes |
| Authentication with JWT | Yes |
| Pagination | Yes |
| Soft Delete | Yes |
| API Documentation | Yes |
| Audit Logging | Yes |
| Concurrency Handling | Yes |