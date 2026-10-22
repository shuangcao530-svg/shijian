package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.PostDTO;
import com.shijian.dto.PostQueryDTO;
import com.shijian.vo.PostVO;

public interface PostService {
    Result<PostVO> create(Long userId, PostDTO dto);
    Result<PostVO> getById(Long postId, Long currentUserId);
    Result<PostVO> update(Long userId, Long postId, PostDTO dto);
    Result<Void> delete(Long userId, Long postId);
    PageResult<PostVO> page(PostQueryDTO query, Long currentUserId);
    PageResult<PostVO> getMyPosts(Long userId, Integer page, Integer size);
    PageResult<PostVO> getUserPosts(Long currentUserId, Long targetUserId, Integer page, Integer size);
    PageResult<PostVO> getFriendPosts(Long userId, Integer page, Integer size);
}
