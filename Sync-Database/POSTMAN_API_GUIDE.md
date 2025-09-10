# Postman API Testing Guide - Dual Database Sync with Failover

## Overview
This guide provides comprehensive instructions for testing the dual database synchronization system using Postman. All data is managed through REST API endpoints - no hardcoded data is used.

## Base URL
```
http://localhost:8082
```

## API Endpoints for Postman Testing

### 1. Health Check
**GET** `/api/v2/health`

**Description:** Check if the API is running and get database status

**Example Response:**
```json
{
    "success": true,
    "status": "API is running",
    "database_status": "MySQL (Primary)",
    "timestamp": 1699123456789
}
```

### 2. Get Database Status
**GET** `/api/v2/database-status`

**Description:** Get current database status and user count

**Example Response:**
```json
{
    "success": true,
    "database_status": "MySQL (Primary)",
    "total_users": 0,
    "timestamp": 1699123456789
}
```

### 3. Get All Users
**GET** `/api/v2/users`

**Description:** Retrieve all users with automatic failover support

**Example Response:**
```json
{
    "success": true,
    "data": [],
    "count": 0,
    "database_status": "MySQL (Primary)",
    "message": "Users retrieved successfully"
}
```

### 4. Create New User
**POST** `/api/v2/users`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
    "name": "John Doe",
    "email": "john.doe@example.com"
}
```

**Example Response:**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@example.com"
    },
    "database_status": "MySQL (Primary)",
    "message": "User created successfully"
}
```

**Validation Rules:**
- `name`: Required, 2-100 characters
- `email`: Required, valid email format, unique, max 150 characters

### 5. Get User by ID
**GET** `/api/v2/users/{id}`

**Example:** `/api/v2/users/1`

**Example Response:**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@example.com"
    },
    "database_status": "MySQL (Primary)",
    "message": "User found successfully"
}
```

### 6. Update User
**PUT** `/api/v2/users/{id}`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
    "name": "John Smith",
    "email": "john.smith@example.com"
}
```

**Example Response:**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "name": "John Smith",
        "email": "john.smith@example.com"
    },
    "database_status": "MySQL (Primary)",
    "message": "User updated successfully"
}
```

### 7. Delete User
**DELETE** `/api/v2/users/{id}`

**Example:** `/api/v2/users/1`

**Example Response:**
```json
{
    "success": true,
    "message": "User deleted successfully",
    "database_status": "MySQL (Primary)",
    "deleted_user": {
        "id": 1,
        "name": "John Smith",
        "email": "john.smith@example.com"
    }
}
```

### 8. Trigger Manual Sync
**POST** `/api/v2/trigger-sync`

**Description:** Manually trigger synchronization between databases

**Example Response:**
```json
{
    "success": true,
    "message": "Synchronization triggered successfully",
    "database_status": "MySQL (Primary)",
    "timestamp": 1699123456789
}
```

## Postman Testing Scenarios

### Scenario 1: Basic CRUD Operations
1. **Check Health:** `GET /api/v2/health`
2. **Get Empty Users:** `GET /api/v2/users` (should return empty array)
3. **Create User 1:**
   ```json
   POST /api/v2/users
   {
       "name": "Alice Johnson",
       "email": "alice@example.com"
   }
   ```
4. **Create User 2:**
   ```json
   POST /api/v2/users
   {
       "name": "Bob Wilson",
       "email": "bob@example.com"
   }
   ```
5. **Get All Users:** `GET /api/v2/users` (should return 2 users)
6. **Get User by ID:** `GET /api/v2/users/1`
7. **Update User:**
   ```json
   PUT /api/v2/users/1
   {
       "name": "Alice Smith",
       "email": "alice.smith@example.com"
   }
   ```
8. **Delete User:** `DELETE /api/v2/users/2`
9. **Verify Final State:** `GET /api/v2/users`

### Scenario 2: Validation Testing
1. **Invalid Email:**
   ```json
   POST /api/v2/users
   {
       "name": "Test User",
       "email": "invalid-email"
   }
   ```
   Expected: 400 Bad Request with validation errors

2. **Missing Name:**
   ```json
   POST /api/v2/users
   {
       "email": "test@example.com"
   }
   ```
   Expected: 400 Bad Request

3. **Duplicate Email:**
   - Create a user first
   - Try to create another user with the same email
   Expected: 500 Internal Server Error (unique constraint violation)

### Scenario 3: Failover Testing
1. **Create Users with MySQL Running:**
   ```json
   POST /api/v2/users
   {
       "name": "MySQL User",
       "email": "mysql@example.com"
   }
   ```

2. **Check Database Status:** `GET /api/v2/database-status`
   Expected: "MySQL (Primary)"

3. **Stop MySQL Service** (manually stop MySQL)

4. **Check Database Status Again:** `GET /api/v2/database-status`
   Expected: "PostgreSQL (Failover)"

5. **Create User During Failover:**
   ```json
   POST /api/v2/users
   {
       "name": "PostgreSQL User",
       "email": "postgres@example.com"
   }
   ```

6. **Restart MySQL Service**

7. **Check Database Status:** `GET /api/v2/database-status`
   Expected: "MySQL (Primary)" (should automatically switch back)

8. **Trigger Manual Sync:** `POST /api/v2/trigger-sync`

9. **Verify All Users:** `GET /api/v2/users`

## Error Response Format
All error responses follow this format:
```json
{
    "success": false,
    "error": "Detailed error message",
    "message": "User-friendly message"
}
```

## Postman Collection Setup

### Environment Variables
Create a Postman environment with:
- `base_url`: `http://localhost:8082`
- `user_id`: (set dynamically from responses)

### Pre-request Scripts
For dynamic user ID extraction:
```javascript
// For POST requests, extract ID from response
pm.test("Extract user ID", function () {
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data && jsonData.data.id) {
        pm.environment.set("user_id", jsonData.data.id);
    }
});
```

### Tests Scripts
Basic success test:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has success field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('success');
    pm.expect(jsonData.success).to.be.true;
});
```

## Database Monitoring
Monitor the automatic synchronization by:
1. Creating users when both databases are healthy
2. Checking logs for sync operations every 30 seconds
3. Monitoring health checks every 10 seconds
4. Testing failover scenarios by stopping/starting database services

## Notes
- All tables are created automatically using JPA/Hibernate
- No manual database setup or SQL scripts needed
- The system handles database creation and schema management
- Email field has unique constraint across both databases
- Automatic bidirectional sync occurs every 30 seconds when both databases are healthy
