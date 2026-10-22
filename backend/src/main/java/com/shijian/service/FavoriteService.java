package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.vo.PostVO;

public interface FavoriteService {
    Result<Void> add(Long userId, Long postId);
    Result<Void> remove(Long userId, Long postId);
    PageResult<PostVO> list(Long userId, Integer page, Integer size);
    boolean isFavorited(Long userId, Long postId);
}
