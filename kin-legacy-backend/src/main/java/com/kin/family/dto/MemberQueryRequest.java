package com.kin.family.dto;

import lombok.Data;

@Data
public class MemberQueryRequest {
    private Integer page = 1;
    private Integer size = 20;
    private String name;
    private String gender;
    private String birthDateStart;
    private String birthDateEnd;
    private Long genealogyId;
    private String createTimeStart;
    private String createTimeEnd;
}
