package com.shijian.dto;

import lombok.Data;

@Data
public class PostQueryDTO {
    private Long categoryId;
    private String keyword;
    private Integer level;
    private String sort = "latest"; // latest, hot, popular
    private Integer page = 1;
    private Integer size = 10;
}
