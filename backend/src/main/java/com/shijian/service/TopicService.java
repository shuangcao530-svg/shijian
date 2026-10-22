package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.PostQueryDTO;
import com.shijian.entity.Topic;
import com.shijian.vo.PostVO;
import java.util.List;

public interface TopicService {
    /** 所有话题（按热度排序） */
    List<Topic> listAll();

    /** 热门话题 */
    List<Topic> listHot(int limit);

    /** 关注话题 */
    Result<Void> follow(Long userId, Long topicId);

    /** 取消关注 */
    Result<Void> unfollow(Long userId, Long topicId);

    /** 用户关注的话题 */
    List<Topic> getUserFollowedTopics(Long userId);

    /** 是否已关注 */
    boolean isFollowed(Long userId, Long topicId);

    /** 话题下的帖子 */
    PageResult<PostVO> getTopicPosts(Long topicId, PostQueryDTO query, Long currentUserId);

    /** 解析帖子的#话题标签 */
    List<String> extractTopicNames(String content);

    /** 给帖子关联话题 */
    void linkTopics(Long postId, List<String> topicNames);

    /** 更新话题帖子数 */
    void updateTopicPostCount(Long topicId);
}
