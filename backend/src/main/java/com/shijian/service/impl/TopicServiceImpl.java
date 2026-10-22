package com.shijian.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.PostQueryDTO;
import com.shijian.entity.*;
import com.shijian.mapper.*;
import com.shijian.service.FavoriteService;
import com.shijian.service.LikeService;
import com.shijian.service.TopicService;
import com.shijian.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl implements TopicService {
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private PostTopicMapper postTopicMapper;
    @Resource
    private TopicFollowMapper topicFollowMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private PostImageMapper postImageMapper;
    @Resource
    private LikeService likeService;
    @Resource
    private FavoriteService favoriteService;

    private static final Pattern TOPIC_PATTERN = Pattern.compile("#([^#\\s]{1,50})");

    @Override
    public List<Topic> listAll() {
        return topicMapper.selectList(
                new LambdaQueryWrapper<Topic>().eq(Topic::getStatus, 1)
                        .orderByDesc(Topic::getPostCount));
    }

    @Override
    public List<Topic> listHot(int limit) {
        return topicMapper.selectList(
                new LambdaQueryWrapper<Topic>().eq(Topic::getStatus, 1)
                        .eq(Topic::getIsHot, 1)
                        .orderByDesc(Topic::getPostCount)
                        .last("LIMIT " + limit));
    }

    @Override
    @Transactional
    public Result<Void> follow(Long userId, Long topicId) {
        if (isFollowed(userId, topicId)) {
            return Result.error("已关注该话题");
        }
        TopicFollow tf = new TopicFollow();
        tf.setUserId(userId);
        tf.setTopicId(topicId);
        topicFollowMapper.insert(tf);
        // 更新话题关注数
        Topic topic = topicMapper.selectById(topicId);
        if (topic != null) {
            topic.setFollowCount((topic.getFollowCount() == null ? 0 : topic.getFollowCount()) + 1);
            topicMapper.updateById(topic);
        }
        return Result.success("关注成功", null);
    }

    @Override
    @Transactional
    public Result<Void> unfollow(Long userId, Long topicId) {
        topicFollowMapper.delete(
                new LambdaQueryWrapper<TopicFollow>()
                        .eq(TopicFollow::getUserId, userId)
                        .eq(TopicFollow::getTopicId, topicId));
        Topic topic = topicMapper.selectById(topicId);
        if (topic != null && topic.getFollowCount() != null && topic.getFollowCount() > 0) {
            topic.setFollowCount(topic.getFollowCount() - 1);
            topicMapper.updateById(topic);
        }
        return Result.success("已取消关注", null);
    }

    @Override
    public List<Topic> getUserFollowedTopics(Long userId) {
        List<TopicFollow> follows = topicFollowMapper.selectList(
                new LambdaQueryWrapper<TopicFollow>().eq(TopicFollow::getUserId, userId));
        if (follows.isEmpty()) return Collections.emptyList();
        List<Long> topicIds = follows.stream().map(TopicFollow::getTopicId).collect(Collectors.toList());
        return topicMapper.selectBatchIds(topicIds);
    }

    @Override
    public boolean isFollowed(Long userId, Long topicId) {
        return topicFollowMapper.selectCount(
                new LambdaQueryWrapper<TopicFollow>()
                        .eq(TopicFollow::getUserId, userId)
                        .eq(TopicFollow::getTopicId, topicId)) > 0;
    }

    @Override
    public PageResult<PostVO> getTopicPosts(Long topicId, PostQueryDTO query, Long currentUserId) {
        // 先查关联表获取帖子ID
        LambdaQueryWrapper<PostTopic> ptWrapper = new LambdaQueryWrapper<>();
        ptWrapper.eq(PostTopic::getTopicId, topicId).orderByDesc(PostTopic::getCreateTime);
        Page<PostTopic> ptPage = postTopicMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), ptWrapper);
        List<Long> postIds = ptPage.getRecords().stream().map(PostTopic::getPostId).collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return PageResult.of(0, query.getPage(), query.getSize(), Collections.emptyList());
        }
        // 再查帖子
        List<Post> posts = postMapper.selectBatchIds(postIds);
        // 保持顺序
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        List<PostVO> records = postIds.stream()
                .filter(postMap::containsKey)
                .map(id -> toPostVO(postMap.get(id), currentUserId))
                .collect(Collectors.toList());
        return PageResult.of(ptPage.getTotal(), query.getPage(), query.getSize(), records);
    }

    @Override
    public List<String> extractTopicNames(String content) {
        if (content == null || content.isEmpty()) return Collections.emptyList();
        Matcher matcher = TOPIC_PATTERN.matcher(content);
        Set<String> names = new LinkedHashSet<>();
        while (matcher.find()) {
            names.add("#" + matcher.group(1));
        }
        return new ArrayList<>(names);
    }

    @Override
    @Transactional
    public void linkTopics(Long postId, List<String> topicNames) {
        if (topicNames == null || topicNames.isEmpty()) return;
        for (String name : topicNames) {
            // 查找或创建话题
            Topic topic = topicMapper.selectOne(
                    new LambdaQueryWrapper<Topic>().eq(Topic::getName, name));
            if (topic == null) {
                topic = new Topic();
                topic.setName(name);
                topic.setPostCount(0);
                topic.setFollowCount(0);
                topic.setIsHot(0);
                topic.setStatus(1);
                topicMapper.insert(topic);
            }
            // 创建关联
            PostTopic pt = new PostTopic();
            pt.setPostId(postId);
            pt.setTopicId(topic.getId());
            postTopicMapper.insert(pt);
            // 更新话题帖子数
            topic.setPostCount((topic.getPostCount() == null ? 0 : topic.getPostCount()) + 1);
            topicMapper.updateById(topic);
        }
    }

    @Override
    public void updateTopicPostCount(Long topicId) {
        int count = postTopicMapper.selectCount(
                new LambdaQueryWrapper<PostTopic>().eq(PostTopic::getTopicId, topicId)).intValue();
        Topic topic = topicMapper.selectById(topicId);
        if (topic != null) {
            topic.setPostCount(count);
            topicMapper.updateById(topic);
        }
    }

    private PostVO toPostVO(Post post, Long currentUserId) {
        PostVO vo = new PostVO();
        BeanUtil.copyProperties(post, vo);
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) vo.setCategoryName(category.getName());
        // Images
        List<PostImage> images = postImageMapper.selectList(
                new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, post.getId()).orderByAsc(PostImage::getSort));
        vo.setImages(images.stream().map(img -> "/api/image/" + img.getId()).collect(Collectors.toList()));
        if (currentUserId != null) {
            vo.setIsLiked(likeService.isLikedPost(currentUserId, post.getId()));
            vo.setIsFavorited(favoriteService.isFavorited(currentUserId, post.getId()));
        }
        // Topics
        List<PostTopic> pts = postTopicMapper.selectList(
                new LambdaQueryWrapper<PostTopic>().eq(PostTopic::getPostId, post.getId()));
        if (!pts.isEmpty()) {
            List<Long> topicIds = pts.stream().map(PostTopic::getTopicId).collect(Collectors.toList());
            List<Topic> topics = topicMapper.selectBatchIds(topicIds);
            vo.setTopics(topics.stream().map(Topic::getName).collect(Collectors.toList()));
        }
        return vo;
    }
}
