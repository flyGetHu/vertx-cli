package com.vertx.common.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating random values.
 */
public class RandUtil {
    /**
     * 使用SHA-256算法对给定的密码进行哈希处理，并返回盐和哈希密码，
     * 编码为Base64字符串。
     *
     * @param password 需要进行哈希处理的密码
     * @return 盐和哈希密码编码为Base64字符串
     * @throws NoSuchAlgorithmException 如果SHA-256算法不可用
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());

        // 返回盐和哈希密码的Base64编码字符串
        return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hashedPassword);
    }

    /**
     * 验证给定的密码是否与带盐的哈希密码匹配。
     *
     * @param password               需要验证的密码。
     * @param hashedPasswordWithSalt 带盐的哈希密码。
     * @return 如果密码与带盐的哈希密码匹配，则返回true，否则返回false。
     * @throws NoSuchAlgorithmException 如果SHA-256算法不可用。
     */
    public static boolean verifyPassword(String password, String hashedPasswordWithSalt) throws NoSuchAlgorithmException {
        String[] parts = hashedPasswordWithSalt.split("\\$");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String hashedPassword = parts[1];

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] computedHash = md.digest(password.getBytes());

        // 比较计算出的哈希密码和存储的哈希密码
        return hashedPassword.equals(Base64.getEncoder().encodeToString(computedHash));
    }
}
