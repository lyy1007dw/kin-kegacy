package com.kin.family.service;

import com.kin.family.dto.FamilyCreateDTO;
import com.kin.family.dto.FamilyDetailDTO;
import com.kin.family.dto.FamilyJoinDTO;
import com.kin.family.dto.PageResult;

import java.util.List;

/**
 * 家谱服务接口
 *
 * @author candong
 */
public interface FamilyService {
    FamilyDetailDTO createFamily(FamilyCreateDTO request, Long userId);
    void joinFamily(FamilyJoinDTO request, Long userId);
    FamilyDetailDTO getFamilyById(Long id);
    List<FamilyDetailDTO> getMyFamilies(Long userId);
    FamilyDetailDTO getFamilyByCode(String code);
    List<FamilyDetailDTO> getAllFamilies();
    PageResult<FamilyDetailDTO> getFamiliesPaged(Integer page, Integer size);
    FamilyDetailDTO updateFamily(Long id, FamilyCreateDTO request, Long userId);
    void deleteFamily(Long id, Long userId);
}
