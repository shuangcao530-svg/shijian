package com.shijian.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.PostDTO;
import com.shijian.dto.PostQueryDTO;
import com.shijian.entity.*;
import com.shijian.mapper.*;
import com.shijian.service.LikeService;
import com.shijian.service.FavoriteService;
import com.shijian.service.FriendService;
import com.shijian.service.PostService;
import com.shijian.service.TopicService;
import com.shijian.service.ContentModerationService;
import com.shijian.service.NotificationService;
import com.shijian.service.PointService;
import com.shijian.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    @Resource
    private PostMapper postMapper;
    @Resource
    private PostImageMapper postImageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private LikeService likeService;
    @Resource
    private FavoriteService favoriteService;
    @Resource
    private FriendService friendService;
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private TopicService topicService;
    @Resource
    private ContentModerationService moderationService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private PointService pointService;
    @Resource
    private PostTopicMapper postTopicMapper;
    @Resource
    private TopicMapper topicMapper;

    private static final String[] LEVEL_TEXTS = {"", "1星", "2星", "3星", "4星", "5星"};

    @Override
    @Transactional
    public Result<PostVO> create(Long userId, PostDTO dto) {
        // AI内容审核
        ContentModerationService.ModerationResult modResult = moderationService.moderateText(
                (dto.getTitle() != null ? dto.getTitle() : "") + " " +
                (dto.getContent() != null ? dto.getContent() : ""));

        Post post = new Post();
        post.setUserId(userId);
        post.setCategoryId(dto.getCategoryId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setLevel(dto.getLevel());
        post.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : "PUBLIC");
        post.setLocation(dto.getLocation());

        // AI审核结果
        post.setStatus("REJECT".equals(modResult.getSuggestion()) ? 0 : 1);
        post.setAiScore(modResult.getScore());
        try {
            post.setAiTags(cn.hutool.json.JSONUtil.toJsonStr(modResult.getFlags()));
        } catch (Exception ignored) {}

        postMapper.insert(post);

        // Link uploaded images to this post
        linkImages(post.getId(), dto.getImages());

        // 提取并关联话题
        List<String> topicNames = topicService.extractTopicNames(dto.getContent());
        if (!topicNames.isEmpty()) {
            topicService.linkTopics(post.getId(), topicNames);
        }

        // 发帖积分奖励
        pointService.addPoints(userId, 3, "POST", "POST", post.getId());

        // 如果AI拒绝，提示用户
        if ("REJECT".equals(modResult.getSuggestion())) {
            return Result.success("内容已提交，因包含敏感内容进入审核队列", getById(post.getId(), userId).getData());
        }

        return Result.success("发布成功", getById(post.getId(), userId).getData());
    }

    @Override
    public Result<PostVO> getById(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.error("帖子不存在");
        }
        // Check visibility
        if ("PRIVATE".equals(post.getVisibility())) {
            if (currentUserId == null || !currentUserId.equals(post.getUserId())) {
                return Result.error("该内容仅作者可见");
            }
        }
        if ("FRIENDS".equals(post.getVisibility())) {
            if (currentUserId == null || (!currentUserId.equals(post.getUserId())
                    && !friendService.isFriend(currentUserId, post.getUserId()))) {
                return Result.error("该内容仅好友可见");
            }
        }
        // Increment view count
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);
        return Result.success(toPostVO(post, currentUserId));
    }

    @Override
    @Transactional
    public Result<PostVO> update(Long userId, Long postId, PostDTO dto) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.error("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return Result.error("无权限修改");
        }
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setLevel(dto.getLevel());
        post.setCategoryId(dto.getCategoryId());
        if (dto.getVisibility() != null) {
            post.setVisibility(dto.getVisibility());
        }
        if (dto.getLocation() != null) {
            post.setLocation(dto.getLocation());
        }
        postMapper.updateById(post);
        // Unlink all old images from this post
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<PostImage> unlinkWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        unlinkWrapper.eq(PostImage::getPostId, postId).set(PostImage::getPostId, null);
        postImageMapper.update(null, unlinkWrapper);
        // Link the specified images
        linkImages(postId, dto.getImages());
        return Result.success("修改成功", getById(postId, userId).getData());
    }

    @Override
    @Transactional
    public Result<Void> delete(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.error("帖子不存在");
        }
        User user = userMapper.selectById(userId);
        if (!post.getUserId().equals(userId) && (user == null || !"ADMIN".equals(user.getRole()))) {
            return Result.error("无权限删除");
        }
        postMapper.deleteById(postId);
        postImageMapper.delete(new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, postId));
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
        return Result.success("删除成功", null);
    }

    @Override
    public PageResult<PostVO> page(PostQueryDTO query, Long currentUserId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0);
        wrapper.eq(Post::getStatus, 1);

        // Only show PUBLIC posts and own posts in listing
        if (currentUserId != null) {
            wrapper.and(w -> w.eq(Post::getVisibility, "PUBLIC")
                    .or().eq(Post::getUserId, currentUserId)
                    .or(q2 -> q2.eq(Post::getVisibility, "FRIENDS")
                            .inSql(Post::getUserId,
                                    "SELECT friend_id FROM friend WHERE user_id = " + currentUserId)));
        } else {
            wrapper.eq(Post::getVisibility, "PUBLIC");
        }

        if (query.getCategoryId() != null) {
            wrapper.eq(Post::getCategoryId, query.getCategoryId());
        }
        if (query.getLevel() != null) {
            wrapper.eq(Post::getLevel, query.getLevel());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, query.getKeyword())
                    .or().like(Post::getContent, query.getKeyword()));
        }

        String sort = query.getSort() != null ? query.getSort() : "latest";
        switch (sort) {
            case "hot":
                wrapper.orderByDesc(Post::getLikeCount, Post::getCommentCount);
                break;
            case "popular":
                wrapper.orderByDesc(Post::getViewCount);
                break;
            default:
                wrapper.orderByDesc(Post::getCreateTime);
        }

        Page<Post> page = postMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        List<PostVO> records = page.getRecords().stream()
                .map(p -> toPostVO(p, currentUserId))
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), records);
    }

    @Override
    public PageResult<PostVO> getMyPosts(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId).eq(Post::getIsDeleted, 0)
                .orderByDesc(Post::getCreateTime);
        Page<Post> postPage = postMapper.selectPage(new Page<>(page, size), wrapper);
        List<PostVO> records = postPage.getRecords().stream()
                .map(p -> toPostVO(p, userId)).collect(Collectors.toList());
        return PageResult.of(postPage.getTotal(), page, size, records);
    }

    @Override
    public PageResult<PostVO> getUserPosts(Long currentUserId, Long targetUserId, Integer page, Integer size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, targetUserId).eq(Post::getIsDeleted, 0).eq(Post::getStatus, 1);

        boolean isFriend = currentUserId != null && friendService.isFriend(currentUserId, targetUserId);
        boolean isSelf = currentUserId != null && currentUserId.equals(targetUserId);

        if (isSelf) {
            // Show all
        } else if (isFriend) {
            wrapper.and(w -> w.eq(Post::getVisibility, "PUBLIC").or().eq(Post::getVisibility, "FRIENDS"));
        } else {
            wrapper.eq(Post::getVisibility, "PUBLIC");
        }

        wrapper.orderByDesc(Post::getCreateTime);
        Page<Post> postPage = postMapper.selectPage(new Page<>(page, size), wrapper);
        List<PostVO> records = postPage.getRecords().stream()
                .map(p -> toPostVO(p, currentUserId)).collect(Collectors.toList());
        return PageResult.of(postPage.getTotal(), page, size, records);
    }

    private PostVO toPostVO(Post post, Long currentUserId) {
        PostVO vo = new PostVO();
        BeanUtil.copyProperties(post, vo);
        // Level text
        if (post.getLevel() >= 1 && post.getLevel() <= 5) {
            vo.setLevelText(LEVEL_TEXTS[post.getLevel()]);
        }
        // User info
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        // Category name
        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        // Images
        LambdaQueryWrapper<PostImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.eq(PostImage::getPostId, post.getId()).orderByAsc(PostImage::getSort);
        List<PostImage> images = postImageMapper.selectList(imgWrapper);
        vo.setImages(images.stream()
                .map(img -> "/api/image/" + img.getId())
                .collect(Collectors.toList()));
        // Like & favorite status
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

    /** Parse /api/image/123 → 123 and link image records to post */
    private void linkImages(Long postId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            if (url == null || !url.contains("/api/image/")) continue;
            try {
                Long imgId = Long.parseLong(url.substring(url.lastIndexOf("/") + 1));
                PostImage img = postImageMapper.selectById(imgId);
                if (img != null) {
                    img.setPostId(postId);
                    img.setSort(i);
                    postImageMapper.updateById(img);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    @Override
    public PageResult<PostVO> getFriendPosts(Long userId, Integer page, Integer size) {
        // 获取所有好友ID
        LambdaQueryWrapper<Friend> fw = new LambdaQueryWrapper<>();
        fw.eq(Friend::getUserId, userId).select(Friend::getFriendId);
        List<Long> friendIds = friendMapper.selectList(fw).stream()
                .map(Friend::getFriendId).collect(Collectors.toList());
        if (friendIds.isEmpty()) {
            return PageResult.of(0, page, size, new ArrayList<>());
        }
        // 查好友的公开和好友可见帖子
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<>();
        w.in(Post::getUserId, friendIds)
         .eq(Post::getIsDeleted, 0).eq(Post::getStatus, 1)
         .and(wr -> wr.eq(Post::getVisibility, "PUBLIC").or().eq(Post::getVisibility, "FRIENDS"))
         .orderByDesc(Post::getCreateTime);
        Page<Post> postPage = postMapper.selectPage(new Page<>(page, size), w);
        List<PostVO> records = postPage.getRecords().stream()
                .map(p -> toPostVO(p, userId)).collect(Collectors.toList());
        return PageResult.of(postPage.getTotal(), page, size, records);
    }
}
