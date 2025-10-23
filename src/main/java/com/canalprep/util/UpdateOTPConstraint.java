package com.canalprep.util;

import com.canalprep.dao.DBConnection;
import java.sql.Connection;
import java.sql.Statement;

public class UpdateOTPConstraint {
    public static void main(String[] args) {
        try {
            updateOTPConstraint();

        } catch (Exception e) {

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
    
                stmt.execute(sql);
            }
        }
    }
}