package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.*;
import com.shijian.vo.LoginVO;
import com.shijian.vo.UserVO;

public interface UserService {
    Result<Void> register(RegisterDTO dto);
    Result<LoginVO> login(LoginDTO dto);
    Result<UserVO> getProfile(Long userId);
    Result<UserVO> getUserById(Long currentUserId, Long targetUserId);
    Result<Void> updateProfile(Long userId, UpdateProfileDTO dto);
    Result<Void> updatePassword(Long userId, UpdatePasswordDTO dto);
    PageResult<UserVO> searchUsers(String keyword, Integer page, Integer size);
}
