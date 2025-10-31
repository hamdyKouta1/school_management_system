# School Management System - API Documentation

## Overview

This document provides detailed information about the RESTful API endpoints available in the School Management System. The API allows you to manage students, attendance records, and other related data.

## Base URL

All API endpoints are relative to the base URL of your server:

```
http://localhost:8081/api
```

## Authentication

The API uses JWT (JSON Web Token) for authentication. Most endpoints require a valid token to be included in the request headers.

### Authentication Header

For protected endpoints, include the JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

### Obtaining a Token

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "your_username",
  "password": "your_password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "your_username",
  "email": "your_email@example.com",
  "role": "ADMIN"
}
```

## API Endpoints

### Authentication

#### Login

**Endpoint:** `POST /api/auth/login`

**Description:** Authenticates a user and returns a JWT token

**Request Body:**
```json
{
  "username": "your_username",
  "password": "your_password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "your_username",
  "email": "your_email@example.com",
  "role": "ADMIN"
}
```

**Status Codes:**
- 200 OK: Authentication successful
- 401 Unauthorized: Invalid credentials

#### Register

**Endpoint:** `POST /api/auth/register`

**Description:** Registers a new user

**Request Body:**
```json
{
  "username": "new_user",
  "email": "new_user@example.com",
  "password": "secure_password",
  "role": "USER"
}
```

**Response:**
```json
{
  "userId": 2,
  "username": "new_user",
  "email": "new_user@example.com",
  "role": "USER"
}
```

**Status Codes:**
- 201 Created: User created successfully
- 400 Bad Request: Invalid input or username/email already exists

### Student Management

#### Get All Students

**Endpoint:** `GET /api/protected/students`

**Description:** Retrieves a list of all students

**Authentication Required:** Yes

**Response:**
```json
[
  {
    "studentId": 1,
    "studentName": "John Doe",
    "nid": "1234567890",
    "nationalityId": 1,
    "currentAddress": "123 Main St",
    "religionId": 1,
    "medicalStatus": "Healthy"
  },
  {
    "studentId": 2,
    "studentName": "Jane Smith",
    "nid": "0987654321",
    "nationalityId": 2,
    "currentAddress": "456 Oak Ave",
    "religionId": 2,
    "medicalStatus": "Allergies"
  }
]
```

**Status Codes:**
- 200 OK: Students retrieved successfully
- 401 Unauthorized: Invalid or missing token
- 500 Internal Server Error: Server error

#### Get Student by ID

**Endpoint:** `GET /api/protected/students/{id}`

**Description:** Retrieves a specific student by ID

**Authentication Required:** Yes

**Path Parameters:**
- `id`: Student ID

**Response:**
```json
{
  "studentId": 1,
  "studentName": "John Doe",
  "nid": "1234567890",
  "nationalityId": 1,
  "currentAddress": "123 Main St",
  "religionId": 1,
  "medicalStatus": "Healthy"
}
```

**Status Codes:**
- 200 OK: Student retrieved successfully
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error

#### Create Student (Basic)

**Endpoint:** `POST /api/protected/students`

**Description:** Creates a new student with basic information only. For creating a student with complete information including parents, notes, etc., use the Full Student endpoint below.

**Authentication Required:** Yes

**Request Body:**
```json
{
  "student_name": "New Student",
  "nid": "1122334455",
  "nationality": "Egyptian",
  "religion": "Muslim",
  "current_address": "789 Pine St",
  "medical_status": true
}
```

**Note**: The API also supports field names like `studentName` instead of `student_name`, `nationalityId` instead of `nationality`, `religionId` instead of `religion`, and `medicalStatus` instead of `medical_status` for backward compatibility.

**Response:**
```json
{
  "studentId": 3,
  "student_name": "New Student",
  "nid": "1122334455",
  "nationality": "Egyptian",
  "current_address": "789 Pine St",
  "religion": "Muslim",
  "medical_status": true
}
```

**Status Codes:**
- 201 Created: Student created successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 500 Internal Server Error: Server error

#### Create Full Student

**Endpoint:** `POST /api/protected/insertStudent`

**Description:** Creates a new student with complete information including parents, notes, etc.

**Authentication Required:** Yes

**Request Body:**
```json
{
  "studentName": "New Student",
  "nid": "1122334455",
  "nationalityName": "Egyptian",
  "religionName": "Muslim",
  "currentAddress": "789 Pine St",
  "medicalStatus": true,
  "dateOfBirth": "2010-01-01",
  "placeOfBirth": "Cairo",
  "gradeName": "Grade 1",
  "className": "Class A",
  "medicalDescriptions": "Healthy",
  "studentPhones": ["01234567890"],
  "parentsInfo": [
    {
      "parent_name": "Jane Doe",
      "relationship": "Mother",
      "parent_nid": "00000000000000001",
      "parent_nationality": "Egyptian",
      "parent_job": "Teacher",
      "parent_address": "789 Pine St",
      "parent_social_status": "Married",
      "parent_phones": ["01234567891"]
    }
  ],
  "studentNotes": [
    {
      "note_text": "New student enrollment",
      "created_by": "Admin"
    }
  ]
}
```

**Response:**
```json
{
  "student_id": 3
}
```

**Status Codes:**
- 201 Created: Student created successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 500 Internal Server Error: Server error

#### Update Student

**Endpoint:** `PUT /api/protected/students/{id}`

**Description:** Updates an existing student

**Authentication Required:** Yes

**Path Parameters:**
- `id`: Student ID

**Request Body:**
```json
{
  "studentName": "Updated Name",
  "nid": "1234567890",
  "nationalityId": 2,
  "currentAddress": "Updated Address",
  "religionId": 2,
  "medicalStatus": "Updated Status"
}
```

**Response:**
```json
{
  "studentId": 1,
  "studentName": "Updated Name",
  "nid": "1234567890",
  "nationalityId": 2,
  "currentAddress": "Updated Address",
  "religionId": 2,
  "medicalStatus": "Updated Status"
}
```

**Status Codes:**
- 200 OK: Student updated successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error

#### Delete Student

**Endpoint:** `DELETE /api/protected/students/{id}`

**Description:** Deletes a student

**Authentication Required:** Yes

**Path Parameters:**
- `id`: Student ID

**Response:**
```json
{
  "message": "Student deleted successfully"
}
```

**Status Codes:**
- 200 OK: Student deleted successfully
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error

### Student Notes

#### Add Student Note

**Endpoint:** `POST /api/protected/students/notes`

**Description:** Adds a note to a student record

**Authentication Required:** Yes

**Request Body:**
```json
{
  "studentId": 1,
  "noteText": "This student has shown improvement in mathematics.",
  "createdBy": "Teacher Name"
}
```

**Response:**
```json
{
  "noteId": 1,
  "studentId": 1,
  "noteText": "This student has shown improvement in mathematics.",
  "createdBy": "Teacher Name"
}
```

**Status Codes:**
- 201 Created: Note added successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error

### Student Phone Numbers

#### Add Student Phone

**Endpoint:** `POST /api/protected/students/phones`

**Description:** Adds a phone number to a student record

**Authentication Required:** Yes

**Request Body:**
```json
{
  "studentId": 1,
  "phoneNumber": "+1234567890"
}
```

**Response:**
```json
{
  "studentId": 1,
  "phoneNumber": "+1234567890"
}
```

**Status Codes:**
- 201 Created: Phone number added successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error

### Medical History

#### Add Medical History

**Endpoint:** `POST /api/protected/students/medical`

**Description:** Adds medical history to a student record

**Authentication Required:** Yes

**Request Body:**
```json
{
  "studentId": 1,
  "description": "Mild asthma, requires inhaler during physical activities."
}
```

**Response:**
```json
{
  "studentId": 1,
  "description": "Mild asthma, requires inhaler during physical activities."
}
```

**Status Codes:**
- 201 Created: Medical history added successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error

### Qualifications

#### Add Qualification

**Endpoint:** `POST /api/protected/students/qualifications`

**Description:** Adds a qualification to a student record

**Authentication Required:** Yes

**Request Body:**
```json
{
  "studentId": 1,
  "description": "First place in regional science fair."
}
```

**Response:**
```json
{
  "qualificationId": 1,
  "studentId": 1,
  "description": "First place in regional science fair."
}
```

**Status Codes:**
- 201 Created: Qualification added successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error

### Dashboard

#### Get Student Count

**Endpoint:** `GET /api/protected/dashboard/studentCount`

**Description:** Retrieves the total number of students

**Authentication Required:** Yes

**Response:**
```json
{
  "count": 3
}
```

**Status Codes:**
- 200 OK: Count retrieved successfully
- 401 Unauthorized: Invalid or missing token
- 500 Internal Server Error: Server error

## Status Codes

The API uses standard HTTP status codes to indicate the success or failure of a request:

- **200 OK**: The request was successful
- **201 Created**: A new resource was successfully created
- **400 Bad Request**: The request was invalid or cannot be served
- **401 Unauthorized**: Authentication is required or failed
- **403 Forbidden**: The authenticated user doesn't have permission
- **404 Not Found**: The requested resource doesn't exist
- **500 Internal Server Error**: An error occurred on the server

## Error Responses

When an error occurs, the API returns a JSON object with an error message:

```json
{
  "error": "Error message describing what went wrong"
}
```

For validation errors, the response may include details about which fields failed validation:

```json
{
  "error": "Validation failed",
  "details": {
    "studentName": "Student name is required",
    "nid": "National ID must be a valid format"
  }
}
```

## Rate Limiting

The API may implement rate limiting to prevent abuse. If you exceed the rate limit, you'll receive a 429 Too Many Requests response with a Retry-After header indicating how long to wait before making another request.

## Testing with Postman

A Postman collection is available for testing the API. Import the following files into Postman:

1. `School_Management_System_Postman_Collection.json`: Contains pre-configured requests for all endpoints
2. `School_Management_System_Postman_Environment.json`: Contains environment variables for base URL and token

### Using the Postman Collection

1. Import both files into Postman
2. Select the imported environment
3. Run the "Login" request first to obtain a token
4. The token will be automatically set for subsequent requests
5. Test other endpoints as needed

---

This API documentation provides comprehensive information about the available endpoints, request/response formats, and authentication requirements for the School Management System API. For additional assistance, please contact the system administrator or development team.

### Bulk Student Batch

**Endpoint:** `GET /api/protected/bulk-action/student-batch`

**Description:** Downloads an Excel (.xlsx) template for bulk student insertion. The first row contains headers; an example row is included.

**Authentication Required:** Yes

**Template Columns:**
- student_name
- nid
- nationality
- religion
- current_address
- medical_status (boolean)
- date_of_birth (YYYY-MM-DD)
- place_of_birth
- grade
- class
- medical_descriptions
- student_phones (comma-separated)
- parents_info (JSON array string)
- student_notes (JSON array string)

---

**Endpoint:** `POST /api/protected/bulk-action/student-batch`

**Description:** Accepts an Excel (.xlsx) file adhering to the template. Each row represents one student to insert using the same rules as the Full Student endpoint.

**Authentication Required:** Yes

**Request:** multipart/form-data with form field `file` containing the `.xlsx` file.

**Response:**
```json
{
  "success_count": 10,
  "error_count": 2,
  "results": [
    {"row": 1, "status": "inserted", "student_id": 101},
    {"row": 2, "status": "error", "message": "Missing required field 'student_name' in row 2"}
  ]
}
```

**Validation Rules:**
- Headers must exactly match the template; download via GET to avoid mismatch.
- Required fields: student_name, nid, nationality, religion, current_address, medical_status.
- `student_phones`: comma-separated values.
- `parents_info` and `student_notes`: valid JSON array strings.
- Dates formatted as `YYYY-MM-DD`.

**Notes:**
- This endpoint internally validates and constructs the same JSON as `/api/protected/insertStudent` using existing `StudentJsonBuilder` and persists via `StudentDAO.insertFullStudent`.
- 201 Created: Student created successfully
- 400 Bad Request: Invalid input
- 401 Unauthorized: Invalid or missing token
- 404 Not Found: Student not found
- 500 Internal Server Error: Server error