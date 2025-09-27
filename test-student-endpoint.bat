@echo off
setlocal enabledelayedexpansion

REM This script helps test the correct endpoint for adding a student
REM Usage: test-student-endpoint.bat <token>

set TOKEN=%1

if "%TOKEN%"=="" (
  echo Error: JWT token is required
  echo Usage: test-student-endpoint.bat ^<token^>
  exit /b 1
)

echo Testing Full Student Creation Endpoint...
echo Sending request to /api/protected/insertStudent

REM Save the JSON to a temporary file to avoid command line length limitations
set "TEMP_JSON=%TEMP%\full_student.json"

echo {
  "studentName": "ahmed ahmed ahmed",
  "nid": "1122334455667788",
  "nationalityName": "مصري",
  "religionName": "مسلم",
  "currentAddress": "بورسعيد - الشرق",
  "medicalStatus": true,
  "dateOfBirth": "2000-09-04",
  "placeOfBirth": "بورسعيد",
  "gradeName": "الصف الاول الاعدادي",
  "className": "1أ",
  "medicalDescriptions": "حساسية من الغبار; نظارة طبية",
  "studentPhones": ["011111115055"],
  "parentsInfo": [
    {
      "parent_name": "omahmed mahjoby",
      "relationship": "أم",
      "parent_nid": "0000012121212",
      "parent_nationality": "مصري",
      "parent_job": "طبيبة",
      "parent_address": "القاهرة - المعادي",
      "parent_social_status": "متزوج",
      "parent_phones": ["088889999555"]
    },
    {
      "parent_name": "ahmed ahmed",
      "relationship": "أب",
      "parent_nid": "0000000003333",
      "parent_nationality": "مصري",
      "parent_job": "مهندس",
      "parent_address": "الجيزة - الدقي",
      "parent_social_status": "متزوج",
      "parent_phones": ["000333666999965"]
    }
  ],
  "studentNotes": [
    {
      "note_text": "الطالب متفوق في الرياضيات",
      "created_by": "معلمة الرياضيات"
    },
    {
      "note_text": "شطورة",
      "created_by": "تيست"
    },
    {
      "note_text": "يحتاج إلى متابعة في اللغة الإنجليزية",
      "created_by": "معلمة اللغة"
    }
  ]
} > "%TEMP_JSON%"

curl -X POST ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d @"%TEMP_JSON%" ^
  http://localhost:8080/api/protected/insertStudent

echo.
echo.
echo For comparison, testing Basic Student Endpoint...
echo Sending request to /api/protected/students with only basic fields

set "TEMP_JSON=%TEMP%\basic_student.json"

echo {
  "studentName": "ahmed ahmed ahmed",
  "nid": "1122334455667788",
  "nationalityName": "مصري",
  "religionName": "مسلم",
  "currentAddress": "بورسعيد - الشرق",
  "medicalStatus": true
} > "%TEMP_JSON%"

curl -X POST ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d @"%TEMP_JSON%" ^
  http://localhost:8080/api/protected/students

del "%TEMP%\full_student.json" 2>nul
del "%TEMP%\basic_student.json" 2>nul