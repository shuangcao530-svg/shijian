package com.shijian.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userAvatar;
    private String nickname;
    private Long parentId;
    private String content;
    private String image;
    private Integer likeCount;
    private Boolean isLiked;
    private List<CommentVO> replies;
    private LocalDateTime createTime;
}
