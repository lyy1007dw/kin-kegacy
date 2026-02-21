package com.kin.family.enums;

public enum RequestStatus {
    pending("待审批"),
    approved("已同意"),
    rejected("已拒绝");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
