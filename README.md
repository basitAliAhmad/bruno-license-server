# Bruno License Server

A self-hosted license server for [Bruno](https://www.usebruno.com/) built with Spring Boot. It implements the license activation and verification API that Bruno clients communicate with, allowing you to run your own license backend.

> **⚠️ DISCLAIMER**: This project is for **development and educational purposes only**. If you like Bruno and use it professionally, please [purchase a legitimate license](https://www.usebruno.com/pricing) to support the developers.


## Tech Stack

- **Java 21**
- **Spring Boot 4.0.6**
- **Lombok**
- **Maven**
- **Docker**

## API Endpoints

All endpoints are served under the `/api` context path.

### Activate License

```
POST /api/v2/license/activate
Content-Type: application/json
```

**Request body:**
```json
{
  "licenseKey": "YOUR-LICENSE-KEY",
  "deviceId": "device-uuid",
  "deviceName": "My Machine",
  "email": "user@example.com",
  "licenseServerUrl": "http://localhost:9090"
}
```

**Response:**
```json
{
  "status": "ACTIVATED",
  "licenseKey": "YOUR-LICENSE-KEY",
  "deviceId": "device-uuid",
  "deviceName": "My Machine",
  "email": "user@example.com",
  "activationId": "<uuid>",
  "activatedAt": "<timestamp>"
}
```

---

### Complete Activation with OTP

```
POST /api/v1/license/activate/{activationId}
Content-Type: application/json
```

**Request body:**
```json
{
  "otp": 123456
}
```

**Response:**
```json
{
  "licenseToken": "<jwt>"
}
```

The returned JWT encodes a license payload with plan `ULTIMATE_EDITION` and type `personal`.

---

### Verify License

```
POST /api/v2/license/verify
```

**Response:**
```json
{
  "verified": true,
  "subscription": {
    "plan": "ULTIMATE_EDITION"
  }
}
```

---

## Running Locally

### Prerequisites

- Java 21+
- Maven 3.9+

### Steps

```bash
./mvnw spring-boot:run
```

The server starts on port `9090`. Base URL: `http://localhost:9090/api`

---

## Running with Docker

### Build and run

```bash
docker build -t bruno-license-server .
docker run -p 9090:9090 bruno-license-server
```

The Dockerfile uses a multi-stage build: Maven compiles the app in the builder stage, and only the JRE and the final JAR are included in the runtime image.

---

## Configuration

Default configuration in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: bruno-license-server

server:
  port: 9090
  servlet:
    context-path: /api
```

Override the port at runtime:

```bash
java -jar target/bruno-license-server-0.0.1-SNAPSHOT.jar --server.port=8080
```

---

## License Plans & Types

| Plan | Value |
|------|-------|
| Pro Edition | `PRO_EDITION` |
| Golden Edition | `GOLDEN_EDITION` |
| Ultimate Edition | `ULTIMATE_EDITION` |

| Type | Value |
|------|-------|
| Personal | `personal` |
| Team | `team` |
| Enterprise | `enterprise` |

---

## Error Handling

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Invalid or expired `activationId` | `404 Not Found` | `{"error": "Invalid activationId"}` |
