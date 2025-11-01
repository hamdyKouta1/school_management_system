package com.canalprep.servlet;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class BulkStudentBatchServletTest {

    private static final String[] TEMPLATE_COLUMNS = new String[]{
            "student_name", "nid", "nationality", "religion", "current_address", "medical_status",
            "date_of_birth", "place_of_birth", "grade", "class", "medical_descriptions",
            "student_phones", "parents_info", "student_notes"
    };

    private Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    @Test
    public void testValidateHeaderValid() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(TEMPLATE_COLUMNS[i]);
            }
            boolean valid = (boolean) invokePrivate(servlet, "validateHeader", new Class<?>[]{Row.class}, header);
            assertTrue(valid);
        }
    }

    @Test
    public void testValidateHeaderInvalid() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue("wrong_" + i);
            }
            boolean valid = (boolean) invokePrivate(servlet, "validateHeader", new Class<?>[]{Row.class}, header);
            assertFalse(valid);
        }
    }

    @Test
    public void testValidateRequiredPresent() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        JSONObject json = new JSONObject();
        json.put("student_name", "John");
        json.put("nid", "123");
        json.put("nationality", "Egyptian");
        json.put("religion", "Muslim");
        json.put("current_address", "123 Street");
        json.put("medical_status", true);
        json.put("grade", "Grade 1");
        json.put("class", "Class A");

        // Should not throw
        invokePrivate(servlet, "validateRequired", new Class<?>[]{JSONObject.class, int.class}, json, 1);
    }

    @Test
    public void testValidateRequiredMissingGradeOrClass() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        JSONObject json = new JSONObject();
        json.put("student_name", "John");
        json.put("nid", "123");
        json.put("nationality", "Egyptian");
        json.put("religion", "Muslim");
        json.put("current_address", "123 Street");
        json.put("medical_status", true);
        json.put("grade", "Grade 1");
        // Missing class
        try {
            invokePrivate(servlet, "validateRequired", new Class<?>[]{JSONObject.class, int.class}, json, 2);
            fail("Expected IllegalArgumentException for missing class");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testBuildJsonFromRowParsesComplexFields() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
                header.createCell(i).setCellValue(TEMPLATE_COLUMNS[i]);
            }
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("John Doe");
            row.createCell(1).setCellValue("30009040300099");
            row.createCell(2).setCellValue("Egyptian");
            row.createCell(3).setCellValue("Muslim");
            row.createCell(4).setCellValue("123 Main St");
            row.createCell(5).setCellValue("true");
            row.createCell(6).setCellValue("2010-01-01");
            row.createCell(7).setCellValue("Cairo");
            row.createCell(8).setCellValue("Grade 1");
            row.createCell(9).setCellValue("Class A");
            row.createCell(10).setCellValue("None");
            row.createCell(11).setCellValue("01234567890,01234567891");
            row.createCell(12).setCellValue("[{\"parent_name\":\"Jane Doe\",\"relationship\":\"Mother\",\"parent_nid\":\"00000000000000001\",\"parent_nationality\":\"Egyptian\",\"parent_job\":\"Teacher\",\"parent_address\":\"123 Main St\",\"parent_social_status\":\"Married\",\"parent_phones\":[\"01234567891\"]}]");
            row.createCell(13).setCellValue("[{\"note_text\":\"Good student\",\"created_by\":\"Teacher\"}]");

            JSONObject json = (JSONObject) invokePrivate(servlet, "buildJsonFromRow", new Class<?>[]{Row.class}, row);

            assertEquals("John Doe", json.getString("student_name"));
            assertTrue(json.getBoolean("medical_status"));
            JSONArray phones = json.getJSONArray("student_phones");
            assertEquals(2, phones.length());
            assertEquals("01234567890", phones.getString(0));
            assertTrue(json.get("parents_info") instanceof JSONArray);
            assertTrue(json.get("student_notes") instanceof JSONArray);
            assertEquals("Grade 1", json.getString("grade"));
            assertEquals("Class A", json.getString("class"));
        }
    }

    @Test
    public void testGetCellStringDateFormatting() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row row = sheet.createRow(0);
            Cell dateCell = row.createCell(0);
            // Set as date
            CreationHelper createHelper = wb.getCreationHelper();
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
            dateCell.setCellValue(new java.util.Date(1262304000000L)); // 2010-01-01 UTC
            dateCell.setCellStyle(dateStyle);

            String dateStr = (String) invokePrivate(servlet, "getCellString", new Class<?>[]{Cell.class}, dateCell);
            assertEquals("2010-01-01", dateStr);
        }
    }
}