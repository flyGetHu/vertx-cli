package com.vertx.common.core;

import com.vertx.common.core.utils.RandUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

class RandUtilTest {

    @Test
    void testHashPassword() throws NoSuchAlgorithmException {
        String password = "password123";
        String hashedPasswordWithSalt = RandUtil.hashPassword(password);

        Assertions.assertNotNull(hashedPasswordWithSalt);
        Assertions.assertTrue(hashedPasswordWithSalt.contains("$"));
    }

    @Test
    void testVerifyPassword() throws NoSuchAlgorithmException {
        String password = "password123";
        String hashedPasswordWithSalt = RandUtil.hashPassword(password);

        Assertions.assertTrue(RandUtil.verifyPassword(password, hashedPasswordWithSalt));
    }
}