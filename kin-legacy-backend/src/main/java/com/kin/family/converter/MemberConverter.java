package com.kin.family.converter;

import com.kin.family.constant.GenderEnum;
import com.kin.family.dto.MemberDetailDTO;
import com.kin.family.entity.FamilyMember;
import com.kin.family.vo.TreeNodeVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 成员转换器
 *
 * @author candong
 */
@Component
public class MemberConverter {

    /**
     * Entity转DetailDTO
     */
    public MemberDetailDTO toDetailDTO(FamilyMember member) {
        if (member == null) {
            return null;
        }
        MemberDetailDTO dto = new MemberDetailDTO();
        dto.setId(member.getId());
        dto.setFamilyId(member.getFamilyId());
        dto.setUserId(member.getUserId());
        dto.setName(member.getName());
        dto.setGender(member.getGender());
        dto.setAvatar(member.getAvatar());
        dto.setBirthDate(member.getBirthDate());
        dto.setBio(member.getBio());
        dto.setIsCreator(member.getIsCreator());
        dto.setCreateTime(member.getCreateTime());
        return dto;
    }

    /**
     * Entity转TreeNodeVO
     */
    public TreeNodeVO toTreeNodeVO(FamilyMember member) {
        if (member == null) {
            return null;
        }
        return TreeNodeVO.builder()
                .id(member.getId())
                .name(member.getName())
                .gender(member.getGender())
                .avatar(member.getAvatar())
                .birthDate(member.getBirthDate())
                .bio(member.getBio())
                .isCreator(member.getIsCreator())
                .children(new ArrayList<>())
                .build();
    }
}
