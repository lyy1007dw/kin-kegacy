package com.kin.family.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTransferCheckDTO {
    private Boolean canTransfer;
    private List<String> warnings;
    private Integer affectedRelations;
}
