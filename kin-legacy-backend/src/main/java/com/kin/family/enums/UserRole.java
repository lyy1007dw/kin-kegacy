package com.kin.family.enums;

public enum UserRole {
    admin("管理员"),
    user("普通用户");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
