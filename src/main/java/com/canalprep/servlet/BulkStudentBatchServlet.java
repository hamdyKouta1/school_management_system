package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;
import com.canalprep.model.Student;
import com.canalprep.util.StudentJsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Bulk student batch API:
 * - GET /api/protected/bulk-action/student-batch -> downloads Excel template
 * - POST /api/protected/bulk-action/student-batch -> upload Excel to insert multiple students
 */
@MultipartConfig
public class BulkStudentBatchServlet extends HttpServlet {

    private static final String[] TEMPLATE_COLUMNS = new String[]{
            "student_name", "nid", "nationality", "religion", "current_address", "medical_status",
            "date_of_birth", "place_of_birth", "grade", "class", "medical_descriptions",
            // phones: comma-separated values
            "student_phones",
            // parents_info: JSON array string per row
            "parents_info",
            // student_notes: JSON array string per row
            "student_notes"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Return an Excel template with headers and one example row
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=student_batch_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");
            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(TEMPLATE_COLUMNS[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Add example row with formats
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("John Doe");
            example.createCell(1).setCellValue("30009040300099");
            example.createCell(2).setCellValue("Egyptian");
            example.createCell(3).setCellValue("Muslim");
            example.createCell(4).setCellValue("123 Main St");
            example.createCell(5).setCellValue("true"); // medical_status as boolean string
            example.createCell(6).setCellValue("2010-01-01"); // date_of_birth YYYY-MM-DD
            example.createCell(7).setCellValue("Cairo");
            example.createCell(8).setCellValue("Grade 1");
            example.createCell(9).setCellValue("Class A");
            example.createCell(10).setCellValue("None");
            example.createCell(11).setCellValue("01234567890,01234567891");
            example.createCell(12).setCellValue("[{\"parent_name\":\"Jane Doe\",\"relationship\":\"Mother\",\"parent_nid\":\"00000000000000001\",\"parent_nationality\":\"Egyptian\",\"parent_job\":\"Teacher\",\"parent_address\":\"123 Main St\",\"parent_social_status\":\"Married\",\"parent_phones\":[\"01234567891\"]}]");
            example.createCell(13).setCellValue("[{\"note_text\":\"Good student\",\"created_by\":\"Teacher\"}]");

            // Write workbook to response
            try (OutputStream os = resp.getOutputStream()) {
                workbook.write(os);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Failed to generate template: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        JSONArray results = new JSONArray();
        List<Integer> insertedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"No file uploaded. Use form field name 'file'.\"}");
                return;
            }
            if (!filePart.getSubmittedFileName().endsWith(".xlsx")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid file type. Only .xlsx files are accepted.\"}");
                return;
            }

            try (InputStream is = filePart.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
                Sheet sheet = workbook.getSheetAt(0);
                // Validate header row
                Row header = sheet.getRow(0);
                if (!validateHeader(header)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Invalid template headers. Please download the latest template via GET endpoint.\"}");
                    return;
                }

                StudentDAO dao = new StudentDAO();
                // Iterate over data rows
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue; // skip empty row
                    try {
                        JSONObject json = buildJsonFromRow(row);
                        // validate minimal required fields
                        validateRequired(json, r);
                        Student student = StudentJsonBuilder.buildStudentFromJson(json);
                        int id = dao.insertFullStudent(student);
                        insertedIds.add(id);
                        JSONObject ok = new JSONObject();
                        ok.put("row", r);
                        ok.put("status", "inserted");
                        ok.put("student_id", id);
                        results.put(ok);
                    } catch (Exception ex) {
                        JSONObject err = new JSONObject();
                        err.put("row", r);
                        err.put("status", "error");
                        err.put("message", ex.getMessage());
                        results.put(err);
                        errors.add("Row " + r + ": " + ex.getMessage());
                    }
                }
            }

            JSONObject response = new JSONObject();
            response.put("success_count", insertedIds.size());
            response.put("error_count", errors.size());
            response.put("results", results);
            resp.getWriter().write(response.toString());
        } catch (IllegalStateException ise) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Request must be multipart/form-data.\"}");
        } catch (ServletException se) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Servlet error: " + se.getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Unexpected error: " + e.getMessage() + "\"}");
        }
    }

    private boolean validateHeader(Row header) {
        if (header == null) return false;
        for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
            Cell c = header.getCell(i);
            String v = c != null ? c.getStringCellValue() : null;
            if (v == null || !v.trim().equalsIgnoreCase(TEMPLATE_COLUMNS[i])) {
                return false;
            }
        }
        return true;
    }

    private void validateRequired(JSONObject json, int rowIndex) {
        String[] required = new String[]{"student_name", "nid", "nationality", "religion", "current_address", "medical_status", "grade", "class"};
        for (String key : required) {
            if (!json.has(key)) {
                throw new IllegalArgumentException("Missing required field '" + key + "' in row " + rowIndex);
            }
        }
    }

    private JSONObject buildJsonFromRow(Row row) {
        JSONObject json = new JSONObject();
        for (int i = 0; i < TEMPLATE_COLUMNS.length; i++) {
            String key = TEMPLATE_COLUMNS[i];
            Cell cell = row.getCell(i);
            String value = getCellString(cell);
            if (value == null || value.isEmpty()) continue;

            switch (key) {
                case "medical_status":
                    json.put(key, Boolean.parseBoolean(value.trim()));
                    break;
                case "student_phones":
                    // comma-separated
                    String[] phones = value.split(",");
                    JSONArray phonesArr = new JSONArray();
                    for (String p : phones) {
                        String s = p.trim();
                        if (!s.isEmpty()) phonesArr.put(s);
                    }
                    json.put(key, phonesArr);
                    break;
                case "parents_info":
                    // expecting JSON array string
                    try {
                        JSONArray parents = new JSONArray(value);
                        json.put(key, parents);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid parents_info JSON array format");
                    }
                    break;
                case "student_notes":
                    // expecting JSON array string
                    try {
                        JSONArray notes = new JSONArray(value);
                        json.put(key, notes);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid student_notes JSON array format");
                    }
                    break;
                default:
                    json.put(key, value);
            }
        }
        return json;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                // convert date to YYYY-MM-DD
                java.util.Date d = cell.getDateCellValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                return String.format("%04d-%02d-%02d", year, month, day);
            } else {
                // Use DataFormatter to preserve displayed value and avoid scientific notation
                DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            }
        }
        if (cell.getCellType() == CellType.BOOLEAN) return Boolean.toString(cell.getBooleanCellValue());
        if (cell.getCellType() == CellType.BLANK) return "";
        // Fallback to string representation
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }
}