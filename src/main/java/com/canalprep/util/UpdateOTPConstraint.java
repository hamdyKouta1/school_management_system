package com.canalprep.util;

import com.canalprep.dao.DBConnection;
import java.sql.Connection;
import java.sql.Statement;

public class UpdateOTPConstraint {
    public static void main(String[] args) {
        try {
            updateOTPConstraint();
            System.out.println("✅ OTP constraint updated successfully!");
        } catch (Exception e) {
            System.err.println("❌ Failed to update OTP constraint: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void updateOTPConstraint() throws Exception {
        String[] sqlStatements = {
            "ALTER TABLE otps DROP CONSTRAINT IF EXISTS chk_otp_operation;",
            "ALTER TABLE otps ADD CONSTRAINT chk_otp_operation CHECK (operation IN ('RENEW', 'REMOVE', 'ADMIN_CREATION', 'PASSWORD_RESET'));"
        };
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            for (String sql : sqlStatements) {
                System.out.println("Executing: " + sql);
                stmt.execute(sql);
            }
        }
    }
}