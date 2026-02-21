package com.kin.family.controller;

import com.kin.family.dto.*;
import com.kin.family.service.MemberService;
import com.kin.family.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family/{familyId}")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members")
    public Result<List<MemberResponse>> getMembers(@PathVariable Long familyId) {
        return Result.success(memberService.getMembers(familyId));
    }

    @GetMapping("/tree")
    public Result<List<TreeNodeVO>> getFamilyTree(@PathVariable Long familyId) {
        return Result.success(memberService.getFamilyTree(familyId));
    }

    @GetMapping("/member/{id}")
    public Result<MemberResponse> getMemberById(
            @PathVariable Long familyId,
            @PathVariable Long id) {
        return Result.success(memberService.getMemberById(familyId, id));
    }

    @PostMapping("/member")
    public Result<MemberResponse> addMember(
            @PathVariable Long familyId,
            @RequestBody AddMemberRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(memberService.addMember(familyId, request, userId));
    }

    @PostMapping("/member/{id}/edit-request")
    public Result<Void> applyEditMember(
            @PathVariable Long familyId,
            @PathVariable Long id,
            @RequestBody EditMemberRequest request) {
        Long userId = UserContext.getUserId();
        memberService.applyEditMember(familyId, id, request, userId);
        return Result.success();
    }

    @PutMapping("/member/{id}")
    public Result<MemberResponse> updateMember(
            @PathVariable Long familyId,
            @PathVariable Long id,
            @RequestBody AddMemberRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(memberService.updateMember(familyId, id, request, userId));
    }

    @DeleteMapping("/member/{id}")
    public Result<Void> deleteMember(
            @PathVariable Long familyId,
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        memberService.deleteMember(familyId, id, userId);
        return Result.success();
    }
}
