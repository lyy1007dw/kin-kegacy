package com.kin.family.enums;

public enum RelationType {
    father_son("父子"),
    mother_son("母女"),
    husband_wife("夫妻"),
    sibling("兄弟姐妹");

    private final String description;

    RelationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
