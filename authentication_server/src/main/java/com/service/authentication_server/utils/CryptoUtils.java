package com.service.authentication_server.utils;

import com.service.authentication_server.exception.GenericException.GenericException;
import lombok.extern.log4j.Log4j2;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import static com.service.authentication_server.exception.GenericException.GenericExceptionEntityType.PBKDF2_OP;
import static com.service.authentication_server.exception.GenericException.GenericExceptionType.INVALID_PARAMETER_EXCEPTION;

@Log4j2
public class CryptoUtils {

    public static final int SALT_LENGTH = 16;
    public static final int PBKDF_ITERATION_COUNT = 65536;
    public static final int PBKDF_KEY_LENGTH = 128;

    public static byte[] generateSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    public static byte[] generatePBKDF2(String password, byte[] salt) throws GenericException {

        byte[] hashResult;

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF_ITERATION_COUNT, PBKDF_KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hashResult = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            if(log.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw GenericException.throwException(PBKDF2_OP, INVALID_PARAMETER_EXCEPTION, "Invalid hash parameter or key spec");
        }
        return hashResult;
    }

    public static boolean checkPasswordMatch(String providedPassword, byte[] storedPassword, byte[] storedSalt){

        byte[] passwordHash = new byte[0];
        // hash the password
        try {
            passwordHash = generatePBKDF2(providedPassword, storedSalt);
        } catch (GenericException e) {
            e.printStackTrace();
        }

        return Arrays.equals(storedPassword, passwordHash);
    }

    public static void generateJWT(){

    }

}
