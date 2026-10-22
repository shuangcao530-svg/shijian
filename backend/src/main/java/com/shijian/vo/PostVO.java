package com.shijian.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String nickname;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String content;
    private Integer level;
    private String levelText;
    private String visibility;
    private String location;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer status;
    private List<String> images;
    private List<String> topics;
    private Boolean isLiked;
    private Boolean isFavorited;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
