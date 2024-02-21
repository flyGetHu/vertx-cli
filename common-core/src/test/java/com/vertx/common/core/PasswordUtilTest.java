package com.vertx.common.core;

import com.vertx.common.core.utils.PasswordUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

class PasswordUtilTest {

    @Test
    void testHashPassword() throws NoSuchAlgorithmException {
        String password = "password123";
        String hashedPasswordWithSalt = PasswordUtil.hashPassword(password);
        String hashedPasswordWithSalt2 = PasswordUtil.hashPassword(password);
        String hashedPasswordWithSalt3 = PasswordUtil.hashPassword(password);
        System.out.println(hashedPasswordWithSalt);
        System.out.println(hashedPasswordWithSalt2);
        System.out.println(hashedPasswordWithSalt3);
        Assertions.assertNotNull(hashedPasswordWithSalt);
        Assertions.assertTrue(hashedPasswordWithSalt.contains("$"));
    }

    @Test
    void testVerifyPassword() throws NoSuchAlgorithmException {
        String password = "password123";
        String hashedPasswordWithSalt = PasswordUtil.hashPassword(password);

        Assertions.assertTrue(PasswordUtil.verifyPassword(password, hashedPasswordWithSalt));
    }
}