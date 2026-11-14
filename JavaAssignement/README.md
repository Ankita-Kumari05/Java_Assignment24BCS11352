# Student Management REST API

A production-ready **Student Management REST API** built with **Java 17**, **Spring Boot 3.5**, **Spring Data JPA**, and **MySQL**. It exposes CRUD operations plus course-based filtering, uses DTO validation, returns correct HTTP status codes, and handles errors globally with `@RestControllerAdvice`.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Run the Application](#run-the-application)
- [API Endpoints](#api-endpoints)
- [Postman Testing Guide](#postman-testing-guide)
- [Error Handling](#error-handling)
- [Running Tests](#running-tests)
- [Build for Production](#build-for-production)
- [Troubleshooting](#troubleshooting)

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.5.16 |
| Spring Data JPA (Hibernate) | Managed by Spring Boot |
| MySQL | 8.x |
| Maven | 3.9+ |
| Bean Validation | Jakarta Validation |
| Testing | JUnit 5, MockMvc, H2 (test profile) |

---

## Features

- Full CRUD for students (`POST`, `GET`, `GET by id`, `PUT`, `DELETE`).
- Filter students by course (case-insensitive).
- Bean Validation for `name`, `email`, `course`, and `age`.
- Unique email constraint with a clean `409 Conflict` response.
- Global exception handling with consistent JSON error bodies.
- Correct status codes: `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`, `409 Conflict`, `500 Internal Server Error`.
- `Location` header returned on resource creation.
- Constructor injection, service interface, transactional boundaries, and `open-in-view=false`.
- Integration tests that run against an in-memory H2 database (no MySQL required for `mvn test`).

---

## Project Structure

```
JavaAssignement/
├── pom.xml
├── README.md
├── .gitignore
├── .vscode/
│   └── extensions.json
├── database/
│   └── setup.sql
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/studentmanagement/
    │   │       ├── StudentManagementApplication.java
    │   │       ├── controller/
    │   │       │   └── StudentController.java
    │   │       ├── dto/
    │   │       │   ├── StudentRequest.java
    │   │       │   └── StudentResponse.java
    │   │       ├── entity/
    │   │       │   └── Student.java
    │   │       ├── exception/
    │   │       │   ├── DuplicateResourceException.java
    │   │       │   ├── ErrorResponse.java
    │   │       │   ├── GlobalExceptionHandler.java
    │   │       │   └── ResourceNotFoundException.java
    │   │       ├── repository/
    │   │       │   └── StudentRepository.java
    │   │       └── service/
    │   │           ├── StudentService.java
    │   │           └── impl/
    │   │               └── StudentServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        ├── java/
        │   └── com/example/studentmanagement/
        │       ├── StudentManagementApplicationTests.java
        │       └── controller/
        │           └── StudentControllerIntegrationTests.java
        └── resources/
            └── application.properties
```

---

## Prerequisites

1. **JDK 17 or newer** — verify with:
   ```bash
   java -version
   ```
2. **Maven 3.9+** — verify with:
   ```bash
   mvn -version
   ```
3. **MySQL 8.x** running locally on port `3306`.
4. **VS Code** with these extensions (also listed in `.vscode/extensions.json`):
   - Extension Pack for Java (`vscjava.vscode-java-pack`)
   - Spring Boot Extension Pack (`vmware.vscode-spring-boot`)
   - REST Client (`humao.rest-client`) — optional, useful alternative to Postman

---

## Database Setup

Create the database using MySQL Workbench, the MySQL CLI, or the provided script.

### Option A — MySQL CLI

```bash
mysql -u root -p
```

Then run:

```sql
CREATE DATABASE IF NOT EXISTS student_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

### Option B — Provided script

```bash
mysql -u root -p < database/setup.sql
```

The script creates the database, the `students` table, and a few sample rows.

> Hibernate can also create the table automatically because `spring.jpa.hibernate.ddl-auto=update`. Running the script is optional but useful for initial setup and sample data.

---

## Configuration

Open `src/main/resources/application.properties` and update your MySQL credentials:

```properties
spring.application.name=student-management-api
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/student_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

spring.jackson.serialization.write-dates-as-timestamps=false
```

**Important:** replace `your_password` with your actual MySQL root password.

---

## Run the Application

### From the VS Code terminal

```bash
mvn spring-boot:run
```

### From VS Code UI

1. Open the **Spring Boot Dashboard** (left sidebar).
2. Select `student-management-api`.
3. Click **Run** (or **Debug**).

When startup succeeds you will see:

```
Tomcat started on port 8080 (http)
Started StudentManagementApplication in ...
```

Base URL: **`http://localhost:8080`**

---

## API Endpoints

| Method | Endpoint | Description | Success Status |
|---|---|---|---|
| `POST` | `/api/students` | Create a new student | `201 Created` |
| `GET` | `/api/students` | Get all students | `200 OK` |
| `GET` | `/api/students/{id}` | Get a student by ID | `200 OK` |
| `PUT` | `/api/students/{id}` | Update a student | `200 OK` |
| `DELETE` | `/api/students/{id}` | Delete a student | `204 No Content` |
| `GET` | `/api/students/course/{course}` | Get students by course | `200 OK` |

### Student JSON model

```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "course": "Computer Science",
  "age": 20
}
```

### Validation rules

| Field | Rules |
|---|---|
| `name` | Required, 2–100 characters |
| `email` | Required, valid email, max 150 characters, unique |
| `course` | Required, max 100 characters |
| `age` | Required, integer between 1 and 150 |

---

## Postman Testing Guide

### Setup

1. Open Postman → **New** → **HTTP Request**.
2. Use base URL `http://localhost:8080`.
3. For every request with a body:
   - Go to the **Body** tab.
   - Select **raw**.
   - Choose **JSON** from the dropdown.
4. Make sure the app is running (`mvn spring-boot:run`).

---

### 1. Create a Student — `POST /api/students`

**Request**

```
POST http://localhost:8080/api/students
Content-Type: application/json
```

**Body**

```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "course": "Computer Science",
  "age": 20
}
```

**Expected response — `201 Created`**

Headers include:

```
Location: http://localhost:8080/api/students/1
```

Body:

```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "course": "Computer Science",
  "age": 20
}
```

**Duplicate email — `409 Conflict`**

Sending the same email again returns:

```json
{
  "timestamp": "2026-11-09T10:15:30.123",
  "status": 409,
  "error": "Conflict",
  "message": "A student with email 'alice@example.com' already exists",
  "path": "/api/students"
}
```

---

### 2. Get All Students — `GET /api/students`

**Request**

```
GET http://localhost:8080/api/students
```

**Expected response — `200 OK`**

```json
[
  {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "course": "Computer Science",
    "age": 20
  },
  {
    "id": 2,
    "name": "Bob Smith",
    "email": "bob@example.com",
    "course": "Mathematics",
    "age": 23
  }
]
```

An empty database returns `200 OK` with `[]`.

---

### 3. Get Student by ID — `GET /api/students/{id}`

**Request**

```
GET http://localhost:8080/api/students/1
```

**Expected response — `200 OK`**

```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "course": "Computer Science",
  "age": 20
}
```

**Missing ID — `404 Not Found`**

```
GET http://localhost:8080/api/students/9999
```

```json
{
  "timestamp": "2026-11-09T10:16:00.456",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: 9999",
  "path": "/api/students/9999"
}
```

---

### 4. Update a Student — `PUT /api/students/{id}`

**Request**

```
PUT http://localhost:8080/api/students/1
Content-Type: application/json
```

**Body**

```json
{
  "name": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "course": "Data Science",
  "age": 21
}
```

**Expected response — `200 OK`**

```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "course": "Data Science",
  "age": 21
}
```

**Missing ID — `404 Not Found`**

```
PUT http://localhost:8080/api/students/9999
```

Returns the same `404` error shape shown above.

---

### 5. Delete a Student — `DELETE /api/students/{id}`

**Request**

```
DELETE http://localhost:8080/api/students/1
```

**Expected response — `204 No Content`**

The response body is empty.

**Missing ID — `404 Not Found`**

```
DELETE http://localhost:8080/api/students/9999
```

Returns the `404` error shape shown above.

---

### 6. Get Students by Course — `GET /api/students/course/{course}`

**Request**

```
GET http://localhost:8080/api/students/course/Computer Science
```

> In Postman, spaces in the URL are handled automatically. The encoded form is:
> `http://localhost:8080/api/students/course/Computer%20Science`

**Expected response — `200 OK`**

```json
[
  {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "course": "Computer Science",
    "age": 20
  },
  {
    "id": 3,
    "name": "Carol White",
    "email": "carol@example.com",
    "course": "Computer Science",
    "age": 21
  }
]
```

Matching is **case-insensitive**, so `computer science` returns the same results. If no students match, the API returns `200 OK` with `[]`.

---

### Validation Error Example — `400 Bad Request`

**Request**

```
POST http://localhost:8080/api/students
Content-Type: application/json
```

**Body**

```json
{
  "name": "",
  "email": "not-an-email",
  "course": "",
  "age": 0
}
```

**Expected response — `400 Bad Request`**

```json
{
  "timestamp": "2026-11-09T10:20:00.789",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/students",
  "validationErrors": {
    "name": "Name is required",
    "email": "Email must be a valid email address",
    "course": "Course is required",
    "age": "Age must be greater than 0"
  }
}
```

---

### Postman Collection Tip

To save time, create a Postman **Collection** named `Student Management API` and add six requests using the table above. Set a collection variable:

| Variable | Value |
|---|---|
| `baseUrl` | `http://localhost:8080` |

Then use `{{baseUrl}}/api/students` in every request URL.

---

## Error Handling

All errors are returned by `GlobalExceptionHandler` (`@RestControllerAdvice`) in a consistent format:

```json
{
  "timestamp": "2026-11-09T10:25:00.000",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: 42",
  "path": "/api/students/42"
}
```

| Exception | HTTP Status |
|---|---|
| `ResourceNotFoundException` | `404 Not Found` |
| `DuplicateResourceException` | `409 Conflict` |
| `MethodArgumentNotValidException` | `400 Bad Request` (with field errors) |
| `MethodArgumentTypeMismatchException` | `400 Bad Request` |
| `HttpMessageNotReadableException` | `400 Bad Request` |
| `DataIntegrityViolationException` | `409 Conflict` |
| Any other `Exception` | `500 Internal Server Error` |

---

## Running Tests

The tests use an in-memory H2 database, so **MySQL does not need to be running** for tests.

```bash
mvn test
```

Test coverage includes:

- Context loading.
- Create student (`201` + `Location` header).
- Duplicate email (`409`).
- Validation errors (`400` + field errors).
- Get all / get by ID (`200` / `404`).
- Course filtering (case-insensitive).
- Update (`200`).
- Delete (`204` + subsequent `404`).

---

## Build for Production

```bash
mvn clean package
```

This runs tests and produces an executable JAR:

```
target/student-management-api-1.0.0.jar
```

Run it with:

```bash
java -jar target/student-management-api-1.0.0.jar
```

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `Communications link failure` | MySQL is not running. Start the MySQL service and verify port `3306`. |
| `Access denied for user 'root'@'localhost'` | Wrong username/password in `application.properties`. |
| `Unknown database 'student_db'` | Run `CREATE DATABASE student_db;` or execute `database/setup.sql`. |
| `Port 8080 was already in use` | Change `server.port` in `application.properties` or stop the process using port 8080. |
| `Table 'student_db.students' doesn't exist` | Ensure `spring.jpa.hibernate.ddl-auto=update` is set, then restart the app. |
| Postman returns `415 Unsupported Media Type` | Set the body type to **raw → JSON** and add `Content-Type: application/json`. |
| Postman returns `400` with `Malformed JSON request body` | Check for trailing commas or invalid JSON syntax. |
| VS Code does not recognize the project | Install the **Extension Pack for Java** and run **Java: Clean Java Language Server Workspace**. |
| Tests fail with a port conflict | Integration tests use `MockMvc` and do not start a real server; ensure no other test starts Tomcat on 8080. |

---

## License

This project is provided as-is for educational and assignment purposes.
