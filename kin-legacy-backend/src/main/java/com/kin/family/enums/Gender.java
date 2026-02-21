package com.kin.family.enums;

public enum Gender {
    male("男"),
    female("女");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
