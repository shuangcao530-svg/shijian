package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.*;
import com.shijian.mapper.*;
import com.shijian.service.FriendService;
import com.shijian.service.NotificationService;
import com.shijian.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {
    @Resource
    private FriendRequestMapper friendRequestMapper;
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private BlacklistMapper blacklistMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NotificationService notificationService;

    @Override
    public Result<Void> sendRequest(Long fromUserId, Long toUserId, String message) {
        if (fromUserId.equals(toUserId)) {
            return Result.error("不能添加自己为好友");
        }
        // Check blacklist
        LambdaQueryWrapper<Blacklist> blWrapper = new LambdaQueryWrapper<>();
        blWrapper.eq(Blacklist::getUserId, toUserId).eq(Blacklist::getBlockedUserId, fromUserId);
        if (blacklistMapper.selectCount(blWrapper) > 0) {
            return Result.error("对方已将你拉黑");
        }
        // Check existing request
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getFromUserId, fromUserId)
                .eq(FriendRequest::getToUserId, toUserId)
                .eq(FriendRequest::getStatus, "PENDING");
        if (friendRequestMapper.selectCount(wrapper) > 0) {
            return Result.error("已发送过好友申请");
        }
        // Check if already friends
        if (isFriend(fromUserId, toUserId)) {
            return Result.error("已经是好友");
        }
        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus("PENDING");
        friendRequestMapper.insert(request);
        // 通知对方有新的好友申请
        notificationService.create(toUserId, fromUserId, "FRIEND_REQUEST", "FRIEND_REQUEST",
                request.getId(), "请求添加你为好友");
        return Result.success("好友申请已发送", null);
    }

    @Override
    @Transactional
    public Result<Void> handleRequest(Long userId, Long requestId, String action) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            return Result.error("请求不存在");
        }
        if (!"PENDING".equals(request.getStatus())) {
            return Result.error("该请求已处理");
        }
        if ("ACCEPT".equals(action)) {
            request.setStatus("ACCEPTED");
            friendRequestMapper.updateById(request);
            // Add bidirectional friend relationship
            Friend f1 = new Friend();
            f1.setUserId(request.getFromUserId());
            f1.setFriendId(request.getToUserId());
            friendMapper.insert(f1);
            Friend f2 = new Friend();
            f2.setUserId(request.getToUserId());
            f2.setFriendId(request.getFromUserId());
            friendMapper.insert(f2);
            return Result.success("已接受好友申请", null);
        } else if ("REJECT".equals(action)) {
            request.setStatus("REJECTED");
            friendRequestMapper.updateById(request);
            return Result.success("已拒绝好友申请", null);
        }
        return Result.error("无效操作");
    }

    @Override
    @Transactional
    public Result<Void> deleteFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        friendMapper.delete(wrapper);
        LambdaQueryWrapper<Friend> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId);
        friendMapper.delete(wrapper2);
        return Result.success("删除好友成功", null);
    }

    @Override
    public PageResult<UserVO> getFriends(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId);
        Page<Friend> friendPage = friendMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> records = friendPage.getRecords().stream().map(f -> {
            User user = userMapper.selectById(f.getFriendId());
            if (user == null) return null;
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setBio(user.getBio());
            vo.setGender(user.getGender());
            vo.setIsFriend(true);
            return vo;
        }).filter(u -> u != null).collect(Collectors.toList());
        return PageResult.of(friendPage.getTotal(), page, size, records);
    }

    @Override
    public List<UserVO> getFriendRequests(Long userId) {
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, "PENDING")
                .orderByDesc(FriendRequest::getCreateTime);
        List<FriendRequest> requests = friendRequestMapper.selectList(wrapper);
        return requests.stream().map(r -> {
            User user = userMapper.selectById(r.getFromUserId());
            UserVO vo = new UserVO();
            if (user != null) {
                vo.setId(user.getId());
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            vo.setRequestId(r.getId());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isFriend(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, targetUserId);
        return friendMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Result<Void> addToBlacklist(Long userId, Long blockedUserId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId).eq(Blacklist::getBlockedUserId, blockedUserId);
        if (blacklistMapper.selectCount(wrapper) > 0) {
            return Result.success("已拉黑", null);
        }
        Blacklist bl = new Blacklist();
        bl.setUserId(userId);
        bl.setBlockedUserId(blockedUserId);
        blacklistMapper.insert(bl);
        // Remove friend if exists
        deleteFriend(userId, blockedUserId);
        return Result.success("已拉黑", null);
    }

    @Override
    public Result<Void> removeFromBlacklist(Long userId, Long blockedUserId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId).eq(Blacklist::getBlockedUserId, blockedUserId);
        blacklistMapper.delete(wrapper);
        return Result.success("已取消拉黑", null);
    }

    @Override
    public PageResult<UserVO> getBlacklist(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId);
        Page<Blacklist> blPage = blacklistMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> records = blPage.getRecords().stream().map(bl -> {
            User user = userMapper.selectById(bl.getBlockedUserId());
            if (user == null) return null;
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            return vo;
        }).filter(u -> u != null).collect(Collectors.toList());
        return PageResult.of(blPage.getTotal(), page, size, records);
    }
}
