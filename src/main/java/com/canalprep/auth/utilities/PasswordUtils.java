package com.canalprep.auth.utilities;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 10;
    private static final int MEMORY = 65536;
    private static final int PARALLELISM = 4;
    
    // Generate a secure random salt
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    // Hash password with Argon2
    public static String hashPassword(String password, String salt) {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(saltBytes)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY)
                .withParallelism(PARALLELISM);
        
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());
        
        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(password.toCharArray(), hash);
        
        return Base64.getEncoder().encodeToString(hash);
    }
    
    // Verify password
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        String computedHash = hashPassword(password, salt);
        System.out.println("i'am: new comp     "+computedHash);
        System.out.println("at function        "+computedHash.equals(storedHash));
        return computedHash.equals(storedHash);
    }
}