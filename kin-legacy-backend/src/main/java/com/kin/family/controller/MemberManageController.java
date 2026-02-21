package com.kin.family.controller;

import com.kin.family.dto.*;
import com.kin.family.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class MemberManageController {

    private final MemberService memberService;

    @GetMapping("/member/list")
    public Result<List<MemberResponse>> getAllMembers() {
        return Result.success(memberService.getAllMembers());
    }

    @GetMapping("/member/list/paged")
    public Result<PageResult<MemberResponse>> getMembersPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(memberService.getMembersPaged(page, size));
    }

    @PostMapping("/member")
    public Result<MemberResponse> addMember(@RequestBody AddMemberByUserRequest request) {
        return Result.success(memberService.addMemberByUser(
                request.getFamilyId(), 
                request.getUserId(), 
                request
        ));
    }
}
