# Student QR Code Generation API Documentation

## 📋 **Overview**

The Student QR Code Generation API allows authorized users (ADMIN and DEVELOPER roles only) to generate QR codes for all students in the system and download them as a ZIP archive.

## 🔗 **Endpoint Details**

### **Generate Student QR Codes**
- **Method**: `GET`
- **Path**: `/api/protected/students/qrcodes`
- **Authentication**: Required (JWT Token)
- **Authorization**: ADMIN or DEVELOPER role only
- **Response**: ZIP file containing PNG QR codes

## 🔐 **Security Requirements**

### **Authentication**
```http
Authorization: Bearer <JWT_TOKEN>
```

### **Role-Based Access Control**
- ✅ **ADMIN**: Full access
- ✅ **DEVELOPER**: Full access  
- ❌ **USER**: Access denied (403 Forbidden)
- ❌ **Unauthenticated**: Access denied (401 Unauthorized)

## 📤 **Response Format**

### **Success Response (200 OK)**
```http
Content-Type: application/zip
Content-Disposition: attachment; filename="student_qrcodes.zip"
Cache-Control: no-cache, no-store, must-revalidate
Pragma: no-cache
Expires: 0

[Binary ZIP file content]
```

### **Error Responses**

#### **401 Unauthorized**
```json
{
  "error": "Authorization token required",
  "status": 401
}
```

#### **403 Forbidden**
```json
{
  "error": "Access denied. ADMIN or DEVELOPER role required",
  "status": 403
}
```

#### **404 Not Found**
```json
{
  "error": "No students found in the system",
  "status": 404
}
```

#### **500 Internal Server Error**
```json
{
  "error": "Internal server error: [error details]",
  "status": 500
}
```

## 📁 **ZIP File Contents**

### **File Structure**
The ZIP file is organized hierarchically by grade and then by class:
```
student_qrcodes.zip
├── Grade_1/
│   ├── Class_A/
│   │   ├── John_Doe.png
│   │   ├── Jane_Smith.png
│   │   └── ...
│   ├── Class_B/
│   │   ├── Ahmed_Mohamed.png
│   │   ├── Sarah_Wilson.png
│   │   └── ...
│   └── ...
├── Grade_2/
│   ├── Class_A/
│   │   ├── Michael_Brown.png
│   │   ├── Emily_Davis.png
│   │   └── ...
│   ├── Class_B/
│   │   ├── David_Johnson.png
│   │   └── ...
│   └── ...
├── Grade_3/
│   └── ...
└── ...
```

### **Directory Structure Rules**
- **Primary Grouping**: Students are grouped by their grade name
- **Secondary Grouping**: Within each grade, students are grouped by class name
- **File Placement**: Individual QR code PNG files are placed in their respective class directories
- **Scalability**: The structure automatically accommodates new grades, classes, and students
- **Empty Handling**: Grades or classes with no students are omitted from the ZIP file

### **File Naming Convention**
- **Format**: `${sanitized_student_name}.png`
- **Sanitization**: Invalid characters (`\/:*?"<>|`) replaced with `_`
- **Length Limit**: Maximum 100 characters
- **Fallback**: If name is empty/invalid, uses `student_${timestamp}`

### **QR Code Specifications**
- **Format**: PNG images
- **Size**: 300x300 pixels
- **Error Correction**: Medium level
- **Character Set**: UTF-8
- **Margin**: 1 pixel

## 📊 **QR Code Content**

Each QR code contains a JSON object with student information:

```json
{
  "student_name": "John Doe",
  "id": "123",
  "phone_number": "01234567890"
}
```

### **Field Descriptions**
| Field | Type | Description |
|-------|------|-------------|
| `student_name` | String | Full name of the student |
| `id` | String | Unique student ID |
| `phone_number` | String | Primary phone number (first in list, empty if none) |

## 🚀 **Usage Examples**

### **cURL Example**
```bash
curl -X GET \
  "http://localhost:8081/api/protected/students/qrcodes" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o "student_qrcodes.zip"
```

### **JavaScript/Fetch Example**
```javascript
const response = await fetch('/api/protected/students/qrcodes', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

if (response.ok) {
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'student_qrcodes.zip';
  a.click();
} else {
  console.error('Failed to download QR codes:', response.status);
}
```

### **Postman Usage**
1. Set `Authorization` header with Bearer token
2. Send GET request to `/api/protected/students/qrcodes`
3. Save response as file (Postman will auto-detect ZIP format)

## ⚡ **Performance Features**

### **Caching System**
- **Server-side caching**: Generated QR codes are cached in memory
- **Cache key**: Based on student ID, name hash, and phone hash
- **Cache benefits**: Faster regeneration for unchanged student data
- **Cache management**: Automatic cleanup and monitoring

### **Batch Processing**
- **Synchronous generation**: All QR codes generated in single request
- **Error handling**: Individual student failures don't stop entire process
- **Progress logging**: Detailed logs for success/error counts
- **Memory efficient**: Temporary files cleaned up automatically

### **File Management**
- **Temporary directories**: Unique temp folders for each request
- **Automatic cleanup**: Files deleted after ZIP creation
- **Error recovery**: Cleanup occurs even if errors happen
- **Security**: Temp files isolated per request

## 🛡️ **Security Features**

### **Input Validation**
- **Student data validation**: Checks for required fields
- **Filename sanitization**: Prevents directory traversal attacks
- **Token validation**: JWT signature and expiration verification
- **Role verification**: Strict ADMIN/DEVELOPER access control

### **Audit Logging**
- **Access attempts**: All requests logged with IP addresses
- **Security events**: Failed authentication/authorization logged
- **Operation tracking**: Success/failure metrics recorded
- **User identification**: Role and user info in security logs

### **Error Handling**
- **Graceful degradation**: Partial failures don't crash system
- **Detailed logging**: Comprehensive error information for debugging
- **User-friendly messages**: Clean error responses without sensitive data
- **Resource cleanup**: Guaranteed cleanup even on errors

## 📈 **Monitoring & Metrics**

### **Success Metrics**
- Total students processed
- QR codes generated successfully
- Cache hit/miss ratios
- Response times and file sizes

### **Error Metrics**
- Failed QR code generations
- Authentication/authorization failures
- File system errors
- Network/timeout issues

### **Log Locations**
- **Application logs**: `logs/application/`
- **Security logs**: `logs/security/`
- **Error logs**: `logs/error/`
- **Access logs**: `logs/access/`

## 🔧 **Configuration**

### **Environment Variables**
```properties
# QR Code settings (optional)
qr.code.size=300
qr.code.format=PNG
qr.code.error_correction=M

# Temporary file settings
temp.dir.prefix=student_qrcodes_
temp.cleanup.enabled=true

# Cache settings
qr.cache.enabled=true
qr.cache.max_size=1000
```

### **System Requirements**
- **Java**: 11 or higher
- **Memory**: Minimum 512MB heap for large student datasets
- **Disk**: Temporary space for ZIP file creation
- **Dependencies**: ZXing library for QR code generation

## 🐛 **Troubleshooting**

### **Common Issues**

#### **403 Forbidden Error**
- **Cause**: User doesn't have ADMIN or DEVELOPER role
- **Solution**: Ensure user has proper role assignment
- **Check**: Verify JWT token contains correct role claim

#### **Empty ZIP File**
- **Cause**: No students in database or all students have invalid data
- **Solution**: Check student data completeness
- **Debug**: Review application logs for specific errors

#### **Memory Issues**
- **Cause**: Large number of students causing memory pressure
- **Solution**: Increase JVM heap size
- **Monitoring**: Check memory usage during generation

#### **File Permission Errors**
- **Cause**: Insufficient permissions for temporary directory
- **Solution**: Ensure application has write access to temp directory
- **Alternative**: Configure custom temp directory path

### **Debug Steps**
1. **Check Authentication**: Verify JWT token is valid and not expired
2. **Verify Role**: Ensure user has ADMIN or DEVELOPER role
3. **Review Logs**: Check application and error logs for details
4. **Test with Small Dataset**: Try with fewer students to isolate issues
5. **Monitor Resources**: Check memory and disk space availability

## 📞 **Support**

For technical support or questions about the QR Code API:

1. **Check Logs**: Review application logs for error details
2. **Verify Configuration**: Ensure all dependencies are properly configured
3. **Test Permissions**: Verify user roles and authentication
4. **Monitor Resources**: Check system resources and performance

---

**API Version**: 1.0.0  
**Last Updated**: November 2024  
**Dependencies**: ZXing 3.5.2, Java 11+