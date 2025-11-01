package com.canalprep.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.canalprep.utilities.LoggerUtil;
import com.google.gson.Gson;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

/**
 * Service for generating QR codes with caching capabilities
 * Provides thread-safe QR code generation for student data
 */
public class QRCodeService {
    
    private static final int QR_CODE_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";
    private static final Gson gson = new Gson();
    
    // Cache for generated QR codes to improve performance
    private static final Map<String, byte[]> qrCodeCache = new ConcurrentHashMap<>();
    
    // QR code generation settings
    private static final Map<EncodeHintType, Object> hints = new HashMap<>();
    
    static {
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
    }
    
    /**
     * Student data structure for QR code content
     */
    public static class StudentQRData {
        private final String student_name;
        private final String id;
        private final String phone_number;
        
        public StudentQRData(String studentName, String id, String phoneNumber) {
            this.student_name = studentName;
            this.id = id;
            this.phone_number = phoneNumber != null ? phoneNumber : "";
        }
        
        public String getStudentName() {
            return student_name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getPhoneNumber() {
            return phone_number;
        }
    }
    
    /**
     * Generate QR code as byte array with caching
     * @param studentData Student data to encode in QR code
     * @return QR code as PNG byte array
     * @throws QRCodeGenerationException if generation fails
     */
    public byte[] generateQRCodeBytes(StudentQRData studentData) throws QRCodeGenerationException {
        if (studentData == null || studentData.getStudentName() == null || studentData.getId() == null) {
            throw new QRCodeGenerationException("Student data is incomplete");
        }
        
        // Create cache key based on student data
        String cacheKey = createCacheKey(studentData);
        
        // Check cache first
        byte[] cachedQRCode = qrCodeCache.get(cacheKey);
        if (cachedQRCode != null) {
            LoggerUtil.logInfo("QRCodeService", "QR code retrieved from cache for student: " + studentData.getStudentName());
            return cachedQRCode;
        }
        
        try {
            // Convert student data to JSON
            String qrContent = gson.toJson(studentData);
            
            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);
            
            // Convert to BufferedImage
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, IMAGE_FORMAT, baos);
            byte[] qrCodeBytes = baos.toByteArray();
            
            // Cache the generated QR code
            qrCodeCache.put(cacheKey, qrCodeBytes);
            
            LoggerUtil.logInfo("QRCodeService", "QR code generated and cached for student: " + studentData.getStudentName());
            return qrCodeBytes;
            
        } catch (WriterException e) {
            LoggerUtil.logError("QRCodeService", "Failed to encode QR code for student: " + studentData.getStudentName(), e);
            throw new QRCodeGenerationException("Failed to encode QR code: " + e.getMessage(), e);
        } catch (IOException e) {
            LoggerUtil.logError("QRCodeService", "Failed to convert QR code to bytes for student: " + studentData.getStudentName(), e);
            throw new QRCodeGenerationException("Failed to convert QR code to bytes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate QR code and save to file
     * @param studentData Student data to encode
     * @param outputPath Path where to save the QR code file
     * @throws QRCodeGenerationException if generation or file writing fails
     */
    public void generateQRCodeFile(StudentQRData studentData, Path outputPath) throws QRCodeGenerationException {
        byte[] qrCodeBytes = generateQRCodeBytes(studentData);
        
        try {
            java.nio.file.Files.write(outputPath, qrCodeBytes);
            LoggerUtil.logInfo("QRCodeService", "QR code file saved: " + outputPath.toString());
        } catch (IOException e) {
            LoggerUtil.logError("QRCodeService", "Failed to save QR code file: " + outputPath.toString(), e);
            throw new QRCodeGenerationException("Failed to save QR code file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a cache key for the student data
     * @param studentData Student data
     * @return Cache key string
     */
    private String createCacheKey(StudentQRData studentData) {
        return "qr_" + studentData.getId() + "_" + studentData.getStudentName().hashCode() + "_" + studentData.getPhoneNumber().hashCode();
    }
    
    /**
     * Clear the QR code cache
     */
    public void clearCache() {
        qrCodeCache.clear();
        LoggerUtil.logInfo("QRCodeService", "QR code cache cleared");
    }
    
    /**
     * Get cache size for monitoring
     * @return Number of cached QR codes
     */
    public int getCacheSize() {
        return qrCodeCache.size();
    }
    
    /**
     * Sanitize filename to prevent directory traversal and invalid characters
     * @param filename Original filename
     * @return Sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unknown";
        }
        
        // Remove or replace invalid characters
        String sanitized = filename.trim()
            .replaceAll("[\\\\/:*?\"<>|]", "_")  // Replace invalid characters with underscore
            .replaceAll("\\.\\.+", "_")          // Replace multiple dots with underscore
            .replaceAll("^\\.", "_")             // Replace leading dot
            .replaceAll("\\.$", "_");            // Replace trailing dot
        
        // Limit length to prevent issues
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        
        // Ensure it's not empty after sanitization
        if (sanitized.isEmpty()) {
            sanitized = "student_" + System.currentTimeMillis();
        }
        
        return sanitized;
    }
    
    /**
     * Custom exception for QR code generation errors
     */
    public static class QRCodeGenerationException extends Exception {
        public QRCodeGenerationException(String message) {
            super(message);
        }
        
        public QRCodeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}