package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.vo.UserVO;

import java.util.List;

public interface FriendService {
    Result<Void> sendRequest(Long fromUserId, Long toUserId, String message);
    Result<Void> handleRequest(Long userId, Long requestId, String action);
    Result<Void> deleteFriend(Long userId, Long friendId);
    PageResult<UserVO> getFriends(Long userId, Integer page, Integer size);
    List<UserVO> getFriendRequests(Long userId);
    boolean isFriend(Long userId, Long targetUserId);
    Result<Void> addToBlacklist(Long userId, Long blockedUserId);
    Result<Void> removeFromBlacklist(Long userId, Long blockedUserId);
    PageResult<UserVO> getBlacklist(Long userId, Integer page, Integer size);
}
