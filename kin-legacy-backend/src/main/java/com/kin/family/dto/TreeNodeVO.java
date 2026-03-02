package com.kin.family.dto;

import com.kin.family.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeNodeVO {
    private Long id;
    private String name;
    private Gender gender;
    private String avatar;
    private LocalDate birthDate;
    private String bio;
    private Integer isCreator;
    
    private TreeNodeVO spouse;
    
    @Builder.Default
    private List<TreeNodeVO> children = new ArrayList<>();
}
