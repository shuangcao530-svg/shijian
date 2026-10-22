package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijian.common.Result;
import com.shijian.entity.Comment;
import com.shijian.entity.LikeRecord;
import com.shijian.entity.Post;
import com.shijian.entity.User;
import com.shijian.mapper.CommentMapper;
import com.shijian.mapper.LikeRecordMapper;
import com.shijian.mapper.PostMapper;
import com.shijian.mapper.UserMapper;
import com.shijian.service.LikeService;
import com.shijian.service.NotificationService;
import com.shijian.service.PointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

@Service
public class LikeServiceImpl implements LikeService {
    @Resource
    private LikeRecordMapper likeRecordMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private NotificationService notificationService;
    @Resource
    private PointService pointService;

    @Override
    @Transactional
    public Result<Void> likePost(Long userId, Long postId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, "POST")
                .eq(LikeRecord::getTargetId, postId);
        LikeRecord record = likeRecordMapper.selectOne(wrapper);
        if (record != null && record.getIsLike() == 1) {
            return Result.success("已点赞", null);
        }
        if (record != null) {
            record.setIsLike(1);
            likeRecordMapper.updateById(record);
        } else {
            record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetType("POST");
            record.setTargetId(postId);
            record.setIsLike(1);
            likeRecordMapper.insert(record);
        }
        Post post = postMapper.selectById(postId);
        post.setLikeCount(post.getLikeCount() + 1);
        postMapper.updateById(post);
        // 通知作者
        notificationService.create(post.getUserId(), userId, "LIKE_POST", "POST", postId,
                "赞了你的帖子");
        // 积分奖励
        pointService.addPoints(userId, 1, "LIKE_POST", "POST", postId);
        return Result.success("点赞成功", null);
    }

    @Override
    @Transactional
    public Result<Void> unlikePost(Long userId, Long postId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, "POST")
                .eq(LikeRecord::getTargetId, postId)
                .eq(LikeRecord::getIsLike, 1);
        LikeRecord record = likeRecordMapper.selectOne(wrapper);
        if (record != null) {
            record.setIsLike(0);
            likeRecordMapper.updateById(record);
            Post post = postMapper.selectById(postId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(post);
        }
        return Result.success("取消点赞", null);
    }

    @Override
    public boolean isLikedPost(Long userId, Long postId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, "POST")
                .eq(LikeRecord::getTargetId, postId)
                .eq(LikeRecord::getIsLike, 1);
        return likeRecordMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional
    public Result<Void> likeComment(Long userId, Long commentId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, "COMMENT")
                .eq(LikeRecord::getTargetId, commentId);
        LikeRecord record = likeRecordMapper.selectOne(wrapper);
        if (record != null && record.getIsLike() == 1) {
            return Result.success("已点赞", null);
        }
        if (record != null) {
            record.setIsLike(1);
            likeRecordMapper.updateById(record);
        } else {
            record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetType("COMMENT");
            record.setTargetId(commentId);
            record.setIsLike(1);
            likeRecordMapper.insert(record);
        }
        Comment comment = commentMapper.selectById(commentId);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentMapper.updateById(comment);
        // 通知评论作者
        notificationService.create(comment.getUserId(), userId, "LIKE_COMMENT", "COMMENT", commentId,
                "赞了你的评论");
        return Result.success("点赞成功", null);
    }

    @Override
    @Transactional
    public Result<Void> unlikeComment(Long userId, Long commentId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, "COMMENT")
                .eq(LikeRecord::getTargetId, commentId)
                .eq(LikeRecord::getIsLike, 1);
        LikeRecord record = likeRecordMapper.selectOne(wrapper);
        if (record != null) {
            record.setIsLike(0);
            likeRecordMapper.updateById(record);
            Comment comment = commentMapper.selectById(commentId);
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentMapper.updateById(comment);
        }
        return Result.success("取消点赞", null);
    }
}
