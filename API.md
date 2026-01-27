# API Documentation

## Base URL
```
http://localhost:8080
```

## 1. Authentication & Security

All protected endpoints require the following header:
```
Authorization: Bearer <jwt_token>
```

### Login
**Endpoint:** `POST /auth/login`  
**Description:** Authenticate user and receive JWT token  
**Authentication:** None required  

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (401 Unauthorized):**
```json
"Invalid credentials"
```

### Logout
**Endpoint:** `POST /auth/logout`  
**Description:** Invalidate JWT token  
**Authentication:** Required  

**Response (200 OK):**
```
✅ Logged out
```

### Get Profile
**Endpoint:** `GET /auth/me`  
**Description:** Get current user's profile information  
**Authentication:** Required  

**Response (200 OK):**
```json
{
  "id": 1,
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "STUDENT",
  "status": "ACTIVE",
  "profile": {
    "grade": "10th",
    "parentName": "Jane Doe"
  }
}
```

### Update Profile
**Endpoint:** `PUT /auth/me`  
**Description:** Update current user's profile  
**Authentication:** Required  

**Request Body:**
```json
{
  "fullName": "John Updated",
  "mobileNumber": "+1234567890",
  "courseEnrolled": "Mathematics",
  "parentName": "Jane Updated",
  "experienceYears": 5,
  "qualification": "PhD"
}
```

## 2. Admin User Management

All admin endpoints require `ADMIN` role.

### Onboard User
**Endpoint:** `POST /admin/onboard`  
**Description:** Create a new user account with profile transactionally  
**Authentication:** Required (ADMIN role)  

**Student Onboarding Example:**
```json
{
  "fullName": "Alice Student",
  "email": "alice@example.com",
  "mobileNumber": "+1234567890",
  "role": "STUDENT",
  "initialPassword": "tempPassword123",
  "grade": "12th",
  "parentName": "Bob Student",
  "enrollmentNumber": "STU2024001",
  "courseEnrolled": "Science"
}
```

**Teacher Onboarding Example:**
```json
{
  "fullName": "Dr. Smith",
  "email": "smith@example.com",
  "mobileNumber": "+1234567891",
  "role": "TEACHER",
  "initialPassword": "tempPassword123",
  "subjectSpecialization": "Mathematics",
  "qualification": "PhD in Mathematics",
  "salary": 75000.00,
  "experienceYears": 10
}
```

**Response (201 Created):**
```json
{
  "message": "User onboarded successfully"
}
```

### List Users
**Endpoint:** `GET /admin/users`  
**Description:** Get paginated list of users  
**Authentication:** Required (ADMIN role)  

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | integer | 0 | Page number (0-based) |
| size | integer | 10 | Number of users per page |
| role | string | null | Filter by role (ADMIN/TEACHER/STUDENT) |

**Example Request:**
```
GET /admin/users?page=0&size=5&role=STUDENT
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "fullName": "Alice Student",
    "email": "alice@example.com",
    "role": "STUDENT",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

### Bulk Upload
**Endpoint:** `POST /admin/users/bulk`  
**Description:** Upload CSV file for bulk user creation (async processing)  
**Authentication:** Required (ADMIN role)  
**Content-Type:** `multipart/form-data`  

**Form Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| file | File | Yes | CSV file containing user data |

**CSV Format Example:**
```csv
fullName,email,role,grade,parentName,subjectSpecialization,salary
Alice Student,alice@example.com,STUDENT,10th,Bob Student,,
Dr. Smith,smith@example.com,TEACHER,,,Mathematics,75000
```

**Response (202 Accepted):**
```json
{
  "uploadId": 12345,
  "message": "Bulk import initiated"
}
```

### Get Bulk Upload Status
**Endpoint:** `GET /admin/uploads/{id}/status`  
**Description:** Check status of bulk upload operation  
**Authentication:** Required (ADMIN role)  

**Response (200 OK):**
```json
{
  "id": 12345,
  "status": "COMPLETED",
  "totalRecords": 100,
  "successCount": 95,
  "failureCount": 5,
  "createdAt": "2024-01-15T10:30:00",
  "completedAt": "2024-01-15T10:35:00"
}
```

### Change User Status
**Endpoint:** `PUT /admin/users/{userId}/status`  
**Description:** Activate or deactivate a user  
**Authentication:** Required (ADMIN role)  

**Request Body:**
```json
{
  "status": "INACTIVE"
}
```

**Response (200 OK):**
```json
{
  "message": "User status updated successfully"
}
```

### Delete User
**Endpoint:** `DELETE /admin/users/{userId}`  
**Description:** Soft delete a user  
**Authentication:** Required (ADMIN role)  

**Response (200 OK):**
```json
{
  "message": "User deleted successfully"
}
```

## 3. KYC & AI Verification

### Submit KYC
**Endpoint:** `POST /kyc/submit`  
**Description:** Submit KYC document for AI-powered verification  
**Authentication:** Required (All roles)  
**Content-Type:** `multipart/form-data`  

**Form Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| file | File | Yes | ID document image (JPG, PNG, PDF) |
| address | string | Yes | User's address |
| dateOfBirth | string | Yes | Date in YYYY-MM-DD format |
| govtIdType | string | Yes | PAN, AADHAAR, or PASSPORT |
| govtIdNumber | string | Yes | Government ID number |

**Example using cURL:**
```bash
curl -X POST http://localhost:8080/kyc/submit \
  -H "Authorization: Bearer <jwt_token>" \
  -F "file=@id_card.jpg" \
  -F "address=123 Main St, City" \
  -F "dateOfBirth=1990-01-15" \
  -F "govtIdType=AADHAAR" \
  -F "govtIdNumber=123456789012"
```

**Response (201 Created):**
```json
{
  "message": "KYC submitted successfully"
}
```

**Note:** The response is immediate while AI processing happens asynchronously in the background.

### Get KYC Status
**Endpoint:** `GET /kyc/status`  
**Description:** Get current user's KYC submission status  
**Authentication:** Required (All roles)  

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "SUBMITTED",
  "govtIdType": "AADHAAR",
  "govtIdNumber": "123456789012",
  "address": "123 Main St, City",
  "dateOfBirth": "1990-01-15",
  "submittedAt": "2024-01-15T10:30:00",
  "aiAnalysisResult": {
    "confidenceScore": 0.95,
    "aiRecommendation": "APPROVE",
    "aiRiskFlags": []
  }
}
```

### Admin KYC Review
**Endpoint:** `GET /admin/kyc/review`  
**Description:** Get paginated list of KYC submissions pending review  
**Authentication:** Required (ADMIN role)  

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | integer | 0 | Page number (0-based) |
| size | integer | 10 | Number of records per page |

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "user": {
      "id": 5,
      "fullName": "John Doe",
      "email": "john@example.com"
    },
    "status": "SUBMITTED",
    "govtIdType": "AADHAAR",
    "documentPath": "user_5_kyc.jpg",
    "aiAnalysisResult": {
      "confidenceScore": 0.92,
      "aiRecommendation": "APPROVE",
      "aiRiskFlags": ["MINOR_BLUR_DETECTED"]
    },
    "submittedAt": "2024-01-15T10:30:00"
  }
]
```

### Review KYC Submission
**Endpoint:** `PATCH /admin/kyc/{id}/review`  
**Description:** Approve or reject a KYC submission  
**Authentication:** Required (ADMIN role)  

**Request Body:**
```json
{
  "status": "APPROVED",
  "adminRemarks": "Document verified successfully"
}
```

**Response (200 OK):**
```json
{
  "message": "KYC Updated"
}
```

### Get KYC Details
**Endpoint:** `GET /admin/kyc/{id}`  
**Description:** Get detailed information about a specific KYC submission  
**Authentication:** Required (ADMIN role)  

**Response (200 OK):**
```json
{
  "id": 1,
  "user": {
    "id": 5,
    "fullName": "John Doe",
    "email": "john@example.com"
  },
  "status": "APPROVED",
  "govtIdType": "AADHAAR",
  "govtIdNumber": "123456789012",
  "address": "123 Main St, City",
  "dateOfBirth": "1990-01-15",
  "documentPath": "user_5_kyc.jpg",
  "aiAnalysisResult": {
    "confidenceScore": 0.95,
    "aiRecommendation": "APPROVE",
    "aiRiskFlags": [],
    "extractedData": {
      "name": "JOHN DOE",
      "idNumber": "123456789012"
    }
  },
  "adminRemarks": "Document verified successfully",
  "submittedAt": "2024-01-15T10:30:00",
  "reviewedAt": "2024-01-15T11:00:00"
}
```

## 4. Common Status Codes

| Status Code | Description | Common Scenarios |
|-------------|-------------|------------------|
| **200 OK** | Request successful | GET requests, successful updates |
| **201 Created** | Resource created successfully | User onboarding, KYC submission |
| **202 Accepted** | Request accepted for processing | Bulk upload initiation |
| **400 Bad Request** | Invalid request data | Missing fields, invalid JSON, wrong file type |
| **401 Unauthorized** | Authentication required or invalid | Missing/expired JWT token, wrong credentials |
| **403 Forbidden** | Insufficient permissions | Non-admin accessing admin endpoints |
| **404 Not Found** | Resource not found | Invalid user ID, KYC record not found |
| **500 Internal Server Error** | Server error | Database connection issues, AI service failures |

## 5. Error Response Format

All error responses follow this format:

```json
{
  "error": "Detailed error message"
}
```

Or for simple errors:
```
"Error message string"
```

## 6. Data Types & Enums

### Role Enum
- `ADMIN` - Full system access
- `TEACHER` - Educational content management  
- `STUDENT` - Learning platform access

### KYC Status Enum
- `PENDING` - Initial state
- `SUBMITTED` - Document uploaded and under AI analysis
- `APPROVED` - Verified and approved
- `REJECTED` - Rejected by admin

### Government ID Types
- `PAN` - Permanent Account Number
- `AADHAAR` - Aadhaar Card
- `PASSPORT` - Passport

### User Status
- `ACTIVE` - User can access the system
- `INACTIVE` - User access suspended

## 7. Rate Limiting & File Constraints

### File Upload Limits
- **KYC Documents:** Max 10MB, formats: JPG, PNG, PDF
- **Bulk CSV:** Max 5MB, format: CSV only

### AI Processing
- KYC documents are processed asynchronously
- Processing typically takes 30-60 seconds
- Check status using `/kyc/status` endpoint
- AI confidence scores range from 0.0 to 1.0 (higher is better)

## 8. Security Notes

- JWT tokens expire after 24 hours (configurable)
- All file uploads are validated for type and size
- Sensitive data is not logged in application logs
- Admin actions are audited automatically
- KYC documents are stored securely with restricted access