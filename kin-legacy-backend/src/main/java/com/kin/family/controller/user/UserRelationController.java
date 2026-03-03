package com.kin.family.controller.user;

import com.kin.family.annotation.RequireLogin;
import com.kin.family.annotation.OperationLogger;
import com.kin.family.dto.RelationCreateDTO;
import com.kin.family.dto.RelationVO;
import com.kin.family.dto.Result;
import com.kin.family.service.MemberRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family/{familyId}/relation")
@RequiredArgsConstructor
public class UserRelationController {

    private final MemberRelationService memberRelationService;

    @PostMapping
    @RequireLogin
    @OperationLogger(module = "关系管理", operation = "添加关系")
    public Result<RelationVO> addRelation(
            @PathVariable Long familyId,
            @Valid @RequestBody RelationCreateDTO dto) {
        RelationVO vo = memberRelationService.addRelation(familyId, dto);
        return Result.success(vo);
    }

    @GetMapping("/member/{memberId}")
    @RequireLogin
    @OperationLogger(module = "关系管理", operation = "查询成员关系")
    public Result<List<RelationVO>> getMemberRelations(
            @PathVariable Long familyId,
            @PathVariable Long memberId) {
        List<RelationVO> relations = memberRelationService.getMemberRelations(familyId, memberId);
        return Result.success(relations);
    }

    @DeleteMapping("/{relationId}")
    @RequireLogin
    @OperationLogger(module = "关系管理", operation = "删除关系")
    public Result<Void> deleteRelation(
            @PathVariable Long familyId,
            @PathVariable Long relationId) {
        memberRelationService.deleteRelation(familyId, relationId);
        return Result.success();
    }
}
