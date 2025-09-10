package com.example.harbor_total.feature.commute.dto.req;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommuteListReqDto {
    private LocalDateTime month;
}
