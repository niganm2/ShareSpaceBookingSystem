package com.seatbooking.context;

public class UserContext {
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        userIdHolder.set(userId);
    }

    public static Long getCurrentUserId() {
        return userIdHolder.get();
    }

    public static void setCurrentRole(String role) {
        roleHolder.set(role);
    }

    public static String getCurrentRole() {
        return roleHolder.get();
    }

    public static void setCurrentUsername(String username) {
        usernameHolder.set(username);
    }

    public static String getCurrentUsername() {
        return usernameHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
        usernameHolder.remove();
    }
}