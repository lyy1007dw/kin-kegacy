package com.kin.family.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户姓名更新DTO
 *
 * @author candong
 */
@Data
public class UserNameUpdateDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(min = 1, max = 50, message = "姓名长度必须在1-50个字符之间")
    private String name;
}
