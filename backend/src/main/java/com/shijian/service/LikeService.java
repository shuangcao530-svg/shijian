package com.shijian.service;

import com.shijian.common.Result;

public interface LikeService {
    Result<Void> likePost(Long userId, Long postId);
    Result<Void> unlikePost(Long userId, Long postId);
    boolean isLikedPost(Long userId, Long postId);
    Result<Void> likeComment(Long userId, Long commentId);
    Result<Void> unlikeComment(Long userId, Long commentId);
}
