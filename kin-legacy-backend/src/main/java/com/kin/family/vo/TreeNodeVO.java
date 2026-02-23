package com.kin.family.vo;

import com.kin.family.constant.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 树节点视图对象
 *
 * @author candong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeNodeVO {
    private Long id;
    private String name;
    private GenderEnum gender;
    private String avatar;
    private LocalDate birthDate;
    private String bio;
    private Integer isCreator;

    private TreeNodeVO spouse;

    @Builder.Default
    private List<TreeNodeVO> children = new ArrayList<>();
}
