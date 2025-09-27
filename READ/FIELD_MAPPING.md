# Field Mapping Documentation

## API Endpoints for Student Creation

There are two endpoints for creating students:

1. **Basic Student Creation**: `POST /api/protected/students`
   - Supports only basic student information
   - Limited fields (name, nid, nationality, religion, address, medical status)

2. **Full Student Creation**: `POST /api/protected/insertStudent`
   - Supports complete student information
   - All fields including parents info, student notes, phones, etc.
   - **Use this endpoint for the complete student data**

## Student Model Field Mapping

The Student model supports multiple field naming conventions for backward compatibility. This document outlines the mappings between different field names that can be used in API requests.

### Primary Fields

| JSON Request Field | Alternative Field | Model Property |
|-------------------|-------------------|----------------|
| `student_name` | `firstName` | `studentName` |
| `current_address` | `address` | `currentAddress` |
| `date_of_birth` | `dateOfBirth` | `dateOfBirth` |
| `class` | `className` | `className` |

### Example Requests

Below are examples of valid request formats for both endpoints. Make sure to use the appropriate endpoint based on the data you need to send:

#### Basic Student Creation - Snake Case (POST /api/protected/students)

```json
{
    "student_name": "John Doe",
    "nid": "30009040300099",
    "nationality": "Egyptian",
    "religion": "Muslim",
    "current_address": "123 Main St",
    "medical_status": true
}
```

#### Full Student Creation - Snake Case (POST /api/protected/insertStudent)

```json
{
    "student_name": "John Doe",
    "nid": "30009040300099",
    "nationality": "Egyptian",
    "religion": "Muslim",
    "current_address": "123 Main St",
    "medical_status": true,
    "date_of_birth": "2010-01-01",
    "place_of_birth": "Cairo",
    "grade": "Grade 1",
    "class": "Class A",
    "medical_descriptions": "None",
    "student_phones": ["01234567890"],
    "parents_info": [
        {
            "parent_name": "Jane Doe",
            "relationship": "Mother",
            "parent_nid": "00000000000000001",
            "parent_nationality": "Egyptian",
            "parent_job": "Teacher",
            "parent_address": "123 Main St",
            "parent_social_status": "Married",
            "parent_phones": ["01234567891"]
        }
    ],
    "student_notes": [
        {
            "note_text": "Good student",
            "created_by": "Teacher"
        }
    ]
}
```

#### Basic Student Creation - Camel Case (POST /api/protected/students)

```json
{
    "firstName": "John Doe",
    "nid": "30009040300099",
    "nationality": "Egyptian",
    "religion": "Muslim",
    "address": "123 Main St",
    "medicalStatus": true
}
```

#### Full Student Creation - Camel Case (POST /api/protected/insertStudent)

```json
{
    "studentName": "John Doe",
    "nid": "30009040300099",
    "nationalityName": "Egyptian",
    "religionName": "Muslim",
    "currentAddress": "123 Main St",
    "medicalStatus": true,
    "dateOfBirth": "2010-01-01",
    "placeOfBirth": "Cairo",
    "gradeName": "Grade 1",
    "className": "Class A",
    "medicalDescriptions": "None",
    "studentPhones": ["01234567890"],
    "parentsInfo": [
        {
            "parent_name": "Jane Doe",
            "relationship": "Mother",
            "parent_nid": "00000000000000001",
            "parent_nationality": "Egyptian",
            "parent_job": "Teacher",
            "parent_address": "123 Main St",
            "parent_social_status": "Married",
            "parent_phones": ["01234567891"]
        }
    ],
    "studentNotes": [
        {
            "note_text": "Good student",
            "created_by": "Teacher"
        }
    ]
}
```

## Implementation Details

### Field Mapping

The field mapping is implemented using Jackson's `@JsonAlias` annotation in the Student model. This allows the API to accept multiple field names for the same property.

```java
@JsonAlias({"firstName", "student_name"})
private String studentName;

@JsonAlias({"address", "current_address"})
private String currentAddress;
```

### Endpoint Implementation

- The basic student endpoint (`/api/protected/students`) is implemented in `StudentServlet.java` and uses the `addStudent` method in `StudentDAO.java`.
- The full student endpoint (`/api/protected/insertStudent`) is implemented in `InsertFullStudentServlet.java` and uses the `insertFullStudent` method in `StudentDAO.java`.

### Important Note

When adding a student with complete information (including parents, notes, etc.), make sure to use the `/api/protected/insertStudent` endpoint. Using the basic endpoint with a full student object will result in an error.

This approach ensures backward compatibility with existing clients while allowing new clients to use the more consistent snake_case naming convention.