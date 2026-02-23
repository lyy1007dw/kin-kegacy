package com.kin.family.converter;

import com.kin.family.dto.FamilyDetailDTO;
import com.kin.family.entity.Family;
import org.springframework.stereotype.Component;

/**
 * 家谱转换器
 *
 * @author candong
 */
@Component
public class FamilyConverter {

    /**
     * Entity转DetailDTO
     */
    public FamilyDetailDTO toDetailDTO(Family family) {
        if (family == null) {
            return null;
        }
        FamilyDetailDTO dto = new FamilyDetailDTO();
        dto.setId(family.getId());
        dto.setName(family.getName());
        dto.setCode(family.getCode());
        dto.setAvatar(family.getAvatar());
        dto.setDescription(family.getDescription());
        dto.setCreatorId(family.getCreatorId());
        dto.setCreateTime(family.getCreateTime());
        return dto;
    }
}
