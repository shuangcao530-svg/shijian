package com.shijian.vo;

import lombok.Data;

@Data
public class RankVO {
    private Long postId;
    private String title;
    private String categoryName;
    private String nickname;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer hotScore;
}
