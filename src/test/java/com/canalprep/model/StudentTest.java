package com.canalprep.model;

import static org.junit.Assert.assertEquals;


import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StudentTest {

    @Test
    public void testDeserializeWithFirstName() throws IOException {
        // Given
        String json = "{\"firstName\": \"John Doe\", \"dateOfBirth\": \"2010-01-01\", \"address\": \"123 Main St\", \"className\": \"Class A\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        
        // When
        Student student = objectMapper.readValue(json, Student.class);
        
        // Then
        assertEquals("John Doe", student.getStudentName());
        assertEquals("123 Main St", student.getCurrentAddress());
        assertEquals("Class A", student.getClassName());
    }
    
    @Test
    public void testDeserializeWithStudentName() throws IOException {
        // Given
        String json = "{\"student_name\": \"John Doe\", \"date_of_birth\": \"2010-01-01\", \"current_address\": \"123 Main St\", \"class\": \"Class A\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        
        // When
        Student student = objectMapper.readValue(json, Student.class);
        
        // Then
        assertEquals("John Doe", student.getStudentName());
        assertEquals("123 Main St", student.getCurrentAddress());
        assertEquals("Class A", student.getClassName());
    }
}