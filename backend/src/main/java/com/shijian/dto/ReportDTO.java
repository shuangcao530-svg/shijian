package com.shijian.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReportDTO {
    @NotBlank(message = "举报类型不能为空")
    private String targetType;
    @NotNull(message = "举报目标不能为空")
    private Long targetId;
    @NotBlank(message = "举报原因不能为空")
    private String reason;
}
