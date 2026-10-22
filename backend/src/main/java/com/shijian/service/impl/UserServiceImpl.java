package com.shijian.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.*;
import com.shijian.entity.User;
import com.shijian.mapper.UserMapper;
import com.shijian.service.FriendService;
import com.shijian.service.UserService;
import com.shijian.utils.JwtUtils;
import com.shijian.utils.MD5Utils;
import com.shijian.vo.LoginVO;
import com.shijian.vo.UserVO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private FriendService friendService;

    @Override
    public Result<Void> register(RegisterDTO dto) {
        // Check username
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        // Check phone if provided
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, dto.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                return Result.error("手机号已被注册");
            }
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(MD5Utils.md5(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        userMapper.insert(user);
        return Result.success("注册成功", null);
    }

    @Override
    public Result<LoginVO> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getAccount())
                .or().eq(User::getPhone, dto.getAccount());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("账号不存在");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        if (!user.getPassword().equals(MD5Utils.md5(dto.getPassword()))) {
            return Result.error("密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(toUserVO(user));
        return Result.success(vo);
    }

    @Override
    public Result<UserVO> getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(toUserVO(user));
    }

    @Override
    public Result<UserVO> getUserById(Long currentUserId, Long targetUserId) {
        User user = userMapper.selectById(targetUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserVO vo = toUserVO(user);
        if (currentUserId != null) {
            vo.setIsFriend(friendService.isFriend(currentUserId, targetUserId));
        }
        return Result.success(vo);
    }

    @Override
    public Result<Void> updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
        return Result.success("修改成功", null);
    }

    @Override
    public Result<Void> updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (!user.getPassword().equals(MD5Utils.md5(dto.getOldPassword()))) {
            return Result.error("旧密码错误");
        }
        user.setPassword(MD5Utils.md5(dto.getNewPassword()));
        userMapper.updateById(user);
        return Result.success("密码修改成功", null);
    }

    @Override
    public PageResult<UserVO> searchUsers(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> records = userPage.getRecords().stream()
                .map(this::toUserVO).collect(Collectors.toList());
        return PageResult.of(userPage.getTotal(), page, size, records);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
