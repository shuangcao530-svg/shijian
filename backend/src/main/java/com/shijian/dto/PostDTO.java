package com.shijian.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PostDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    @NotNull(message = "严重程度不能为空")
    private Integer level;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    private String visibility = "PUBLIC";
    private String location;
    private List<String> images;
}
