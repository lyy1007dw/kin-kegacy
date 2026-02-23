package com.kin.family.service;

import com.kin.family.dto.MemberCreateByAdminDTO;
import com.kin.family.dto.MemberCreateDTO;
import com.kin.family.dto.MemberDetailDTO;
import com.kin.family.dto.MemberEditDTO;
import com.kin.family.dto.PageResult;
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
    MemberDetailDTO addMember(Long familyId, MemberCreateDTO request, Long userId);
    void applyEditMember(Long familyId, Long memberId, MemberEditDTO request, Long userId);
    List<TreeNodeVO> getFamilyTree(Long familyId);
    MemberDetailDTO updateMember(Long familyId, Long memberId, MemberCreateDTO request, Long userId);
    void deleteMember(Long familyId, Long memberId, Long userId);
    List<MemberDetailDTO> getAllMembers();
    PageResult<MemberDetailDTO> getMembersPaged(Integer page, Integer size);
    MemberDetailDTO addMemberByUser(Long familyId, Long userId, MemberCreateByAdminDTO request);
}
