package com.canalprep.servlet;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class BulkStudentBatchServletExtraTest {

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
    public void testBuildJsonFromRowInvalidParentsInfo() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
                header.createCell(i).setCellValue(TEMPLATE_COLUMNS[i]);
            }
            Row row = sheet.createRow(1);
            row.createCell(12).setCellValue("not a json array");
            try {
                invokePrivate(servlet, "buildJsonFromRow", new Class[]{Row.class}, row);
                fail("Expected IllegalArgumentException for invalid parents_info JSON");
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof IllegalArgumentException);
            }
        }
    }

    @Test
    public void testBuildJsonFromRowInvalidStudentNotes() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
                header.createCell(i).setCellValue(TEMPLATE_COLUMNS[i]);
            }
            Row row = sheet.createRow(1);
            row.createCell(13).setCellValue("not a json array");
            try {
                invokePrivate(servlet, "buildJsonFromRow", new Class[]{Row.class}, row);
                fail("Expected IllegalArgumentException for invalid student_notes JSON");
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof IllegalArgumentException);
            }
        }
    }

    @Test
    public void testGetCellStringNumericPreservesFormatting() throws Exception {
        BulkStudentBatchServlet servlet = new BulkStudentBatchServlet();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            Row row = sheet.createRow(0);
            Cell numCell = row.createCell(0);
            // Large numeric value to ensure no scientific notation
            numCell.setCellValue(1234567890123d);
            String val = (String) invokePrivate(servlet, "getCellString", new Class[]{Cell.class}, numCell);
            assertEquals("1234567890123", val);
        }
    }
}