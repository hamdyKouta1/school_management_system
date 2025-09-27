#!/bin/bash

# This script helps test the correct endpoint for adding a student
# Usage: ./test-student-endpoint.sh <token>

TOKEN=$1

if [ -z "$TOKEN" ]; then
  echo "Error: JWT token is required"
  echo "Usage: ./test-student-endpoint.sh <token>"
  exit 1
fi

echo "Testing Full Student Creation Endpoint..."
echo "Sending request to /api/protected/insertStudent"

curl -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
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
}' \
  http://localhost:8080/api/protected/insertStudent

echo "\n\nFor comparison, testing Basic Student Endpoint..."
echo "Sending request to /api/protected/students with only basic fields"

curl -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
  "studentName": "ahmed ahmed ahmed",
  "nid": "1122334455667788",
  "nationalityName": "مصري",
  "religionName": "مسلم",
  "currentAddress": "بورسعيد - الشرق",
  "medicalStatus": true
}' \
  http://localhost:8080/api/protected/students