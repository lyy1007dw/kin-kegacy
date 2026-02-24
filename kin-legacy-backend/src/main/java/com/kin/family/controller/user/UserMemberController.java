package com.kin.family.controller.user;

import com.kin.family.annotation.RequireAdmin;
import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.dto.*;
import com.kin.family.service.MemberService;
import com.kin.family.util.UserContextUtil;
import com.kin.family.vo.TreeNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户成员控制器
 *
 * @author candong
 */
@RestController
@RequestMapping("/api/family/{familyId}")
@RequiredArgsConstructor
public class UserMemberController {

    private final MemberService memberService;

    @GetMapping("/members")
    @RequireLogin
    @OperationLogger(module = "成员管理", operation = "查询成员列表")
    public Result<List<MemberDetailDTO>> getMembers(@PathVariable Long familyId) {
        return Result.success(memberService.getMembers(familyId));
    }

    @GetMapping("/tree")
    @RequireLogin
    @OperationLogger(module = "成员管理", operation = "查看家谱树")
    public Result<List<TreeNodeVO>> getFamilyTree(@PathVariable Long familyId) {
        return Result.success(memberService.getFamilyTree(familyId));
    }

    @GetMapping("/member/{id}")
    @RequireLogin
    @OperationLogger(module = "成员管理", operation = "查看成员详情")
    public Result<MemberDetailDTO> getMemberById(
            @PathVariable Long familyId,
            @PathVariable Long id) {
        return Result.success(memberService.getMemberById(familyId, id));
    }

    @PostMapping("/member")
    @RequireLogin
    @RequireAdmin
    @OperationLogger(module = "成员管理", operation = "添加成员")
    public Result<MemberDetailDTO> addMember(
            @PathVariable Long familyId,
            @RequestBody MemberCreateDTO request) {
        Long userId = UserContextUtil.getUserId();
        return Result.success(memberService.addMember(familyId, request, userId));
    }

    @PostMapping("/member/{id}/edit-request")
    @RequireLogin
    @RequireAdmin
    @OperationLogger(module = "成员管理", operation = "申请编辑成员")
    public Result<Void> applyEditMember(
            @PathVariable Long familyId,
            @PathVariable Long id,
            @RequestBody MemberEditDTO request) {
        Long userId = UserContextUtil.getUserId();
        memberService.applyEditMember(familyId, id, request, userId);
        return Result.success();
    }

    @PutMapping("/member/{id}")
    @RequireLogin
    @RequireAdmin
    @OperationLogger(module = "成员管理", operation = "更新成员")
    public Result<MemberDetailDTO> updateMember(
            @PathVariable Long familyId,
            @PathVariable Long id,
            @RequestBody MemberCreateDTO request) {
        Long userId = UserContextUtil.getUserId();
        return Result.success(memberService.updateMember(familyId, id, request, userId));
    }

    @DeleteMapping("/member/{id}")
    @RequireLogin
    @RequireAdmin
    @OperationLogger(module = "成员管理", operation = "删除成员")
    public Result<Void> deleteMember(
            @PathVariable Long familyId,
            @PathVariable Long id) {
        Long userId = UserContextUtil.getUserId();
        memberService.deleteMember(familyId, id, userId);
        return Result.success();
    }
}
