package com.kin.family.service;

import com.kin.family.dto.AddMemberByUserRequest;
import com.kin.family.dto.AddMemberRequest;
import com.kin.family.dto.EditMemberRequest;
import com.kin.family.dto.MemberResponse;
import com.kin.family.dto.PageResult;
import com.kin.family.dto.TreeNodeVO;

import java.util.List;

public interface MemberService {
    List<MemberResponse> getMembers(Long familyId);
    MemberResponse getMemberById(Long familyId, Long memberId);
    MemberResponse addMember(Long familyId, AddMemberRequest request, Long userId);
    void applyEditMember(Long familyId, Long memberId, EditMemberRequest request, Long userId);
    List<TreeNodeVO> getFamilyTree(Long familyId);
    MemberResponse updateMember(Long familyId, Long memberId, AddMemberRequest request, Long userId);
    void deleteMember(Long familyId, Long memberId, Long userId);
    List<MemberResponse> getAllMembers();
    PageResult<MemberResponse> getMembersPaged(Integer page, Integer size);
    MemberResponse addMemberByUser(Long familyId, Long userId, AddMemberByUserRequest request);
}
