package com.kin.family.service;

import com.kin.family.dto.CreateFamilyRequest;
import com.kin.family.dto.FamilyDetailResponse;
import com.kin.family.dto.JoinFamilyRequest;
import com.kin.family.dto.PageResult;

import java.util.List;

public interface FamilyService {
    FamilyDetailResponse createFamily(CreateFamilyRequest request, Long userId);
    void joinFamily(JoinFamilyRequest request, Long userId);
    FamilyDetailResponse getFamilyById(Long id);
    List<FamilyDetailResponse> getMyFamilies(Long userId);
    FamilyDetailResponse getFamilyByCode(String code);
    List<FamilyDetailResponse> getAllFamilies();
    PageResult<FamilyDetailResponse> getFamiliesPaged(Integer page, Integer size);
    FamilyDetailResponse updateFamily(Long id, CreateFamilyRequest request, Long userId);
    void deleteFamily(Long id, Long userId);
}
