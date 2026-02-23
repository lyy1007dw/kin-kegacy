package com.kin.family.util;

/**
 * 用户上下文
 *
 * @author candong
 */
public class UserContextUtil {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> GLOBAL_ROLE = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void setGlobalRole(String globalRole) {
        GLOBAL_ROLE.set(globalRole);
    }

    public static String getGlobalRole() {
        return GLOBAL_ROLE.get();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        GLOBAL_ROLE.remove();
    }
}
