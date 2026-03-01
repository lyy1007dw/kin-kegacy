package com.kin.family.service;

import com.kin.family.dto.*;
import com.kin.family.vo.TreeNodeVO;

import java.util.List;

/**
 * 成员服务接口
 *
 * @author candong
 */
public interface MemberService {
    List<MemberDetailDTO> getMembers(Long familyId);
    MemberDetailDTO getMemberById(Long familyId, Long memberId);
    MemberDetailDTO getMemberDetail(Long familyId, Long memberId);
    MemberDetailDTO addMember(Long familyId, MemberCreateDTO request, Long userId);
    MemberDetailDTO addChildMember(Long familyId, Long parentId, MemberCreateDTO request, Long userId);
    MemberDetailDTO addParentMember(Long familyId, Long childId, MemberCreateDTO request, Long userId);
    void applyEditMember(Long familyId, Long memberId, MemberEditDTO request, Long userId);
    List<TreeNodeVO> getFamilyTree(Long familyId, Long currentUserId);
    MemberDetailDTO updateMember(Long familyId, Long memberId, MemberCreateDTO request, Long userId);
    void deleteMember(Long familyId, Long memberId, Long userId);
    List<MemberDetailDTO> getAllMembers();
    PageResult<MemberDetailDTO> getMembersPaged(Integer page, Integer size);
    MemberDetailDTO addMemberByUser(Long familyId, Long userId, MemberCreateByAdminDTO request);
    
    PageResult<MemberVO> queryMembers(MemberQueryRequest query);
    MemberDetailDTO updateMemberByAdmin(Long memberId, MemberEditByAdminDTO request);
    MemberTransferCheckDTO checkMemberTransfer(Long memberId, Long targetGenealogyId);
}
