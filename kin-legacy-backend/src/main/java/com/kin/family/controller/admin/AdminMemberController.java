package com.kin.family.controller.admin;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.RequireRole;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.dto.*;
import com.kin.family.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员成员控制器
 *
 * @author candong
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    @GetMapping("/member/list")
    @RequireLogin
    @RequireRole("SUPER_ADMIN")
    @OperationLogger(module = "成员管理", operation = "查询所有成员")
    public Result<List<MemberDetailDTO>> getAllMembers() {
        return Result.success(memberService.getAllMembers());
    }

    @GetMapping("/member/list/paged")
    @RequireLogin
    @RequireRole("SUPER_ADMIN")
    @OperationLogger(module = "成员管理", operation = "分页查询成员")
    public Result<PageResult<MemberDetailDTO>> getMembersPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(memberService.getMembersPaged(page, size));
    }

    @PostMapping("/member")
    @RequireLogin
    @RequireRole("SUPER_ADMIN")
    @OperationLogger(module = "成员管理", operation = "管理员添加成员")
    public Result<MemberDetailDTO> addMember(@RequestBody MemberCreateByAdminDTO request) {
        return Result.success(memberService.addMemberByUser(
                request.getFamilyId(),
                request.getUserId(),
                request
        ));
    }
}
