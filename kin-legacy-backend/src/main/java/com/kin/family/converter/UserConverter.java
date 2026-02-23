package com.kin.family.converter;

import com.kin.family.dto.UserDetailDTO;
import com.kin.family.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用户转换器
 *
 * @author candong
 */
@Component
public class UserConverter {

    /**
     * Entity转DetailDTO
     */
    public UserDetailDTO toDetailDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setPhone(user.getPhone());
        dto.setName(user.getName());
        dto.setGlobalRole(user.getGlobalRole());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
