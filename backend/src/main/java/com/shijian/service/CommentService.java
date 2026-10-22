package com.shijian.service;

import com.shijian.common.Result;
import com.shijian.dto.CommentDTO;
import com.shijian.vo.CommentVO;
import java.util.List;

public interface CommentService {
    Result<CommentVO> create(Long userId, CommentDTO dto);
    Result<Void> delete(Long userId, Long commentId);
    Result<Void> like(Long userId, Long commentId);
    Result<Void> unlike(Long userId, Long commentId);
    List<CommentVO> getByPostId(Long postId, Long currentUserId);
}
