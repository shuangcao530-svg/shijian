package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.Category;
import com.shijian.vo.PostVO;
import com.shijian.vo.StatsVO;
import com.shijian.vo.UserVO;

public interface AdminService {
    PageResult<UserVO> listUsers(String keyword, Integer page, Integer size);
    Result<Void> toggleUserStatus(Long userId);
    PageResult<PostVO> listPendingPosts(Integer page, Integer size);
    Result<Void> auditPost(Long postId, Integer status);
    Result<Category> addCategory(Category category);
    Result<Category> updateCategory(Long id, Category category);
    Result<Void> deleteCategory(Long id);
    Result<Void> handleReport(Long adminId, Long reportId, String result, String handleResult);
    PageResult<?> listReports(Integer page, Integer size);
    StatsVO getStats();
}
