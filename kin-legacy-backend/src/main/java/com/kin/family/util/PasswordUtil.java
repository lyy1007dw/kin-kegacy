package com.kin.family.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 *
 * @author candong
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密原始密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword       原始密码
     * @param encodedPassword   加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 判断是否需要升级加密
     *
     * @param encodedPassword 加密后的密码
     * @return 是否需要升级
     */
    public static boolean upgradeEncoding(String encodedPassword) {
        return ENCODER.upgradeEncoding(encodedPassword);
    }

    /**
     * 判断密码是否已加密（BCrypt密文以$2a$开头）
     *
     * @param password 密码
     * @return 是否已加密
     */
    public static boolean isEncoded(String password) {
        return password != null && password.startsWith("$2a$");
    }

    private PasswordUtil() {
    }
}
