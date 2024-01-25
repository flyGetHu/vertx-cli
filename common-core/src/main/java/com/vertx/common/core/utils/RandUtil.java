package com.vertx.common.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class RandUtil {
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
