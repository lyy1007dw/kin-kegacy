package com.kin.family.dto;

import lombok.Data;

/**
 * 分页查询请求DTO
 *
 * @author candong
 */
@Data
public class PageQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
}
