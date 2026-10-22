package com.shijian.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijian.common.Result;
import com.shijian.dto.CommentDTO;
import com.shijian.entity.Comment;
import com.shijian.entity.Post;
import com.shijian.entity.User;
import com.shijian.mapper.CommentMapper;
import com.shijian.mapper.LikeRecordMapper;
import com.shijian.mapper.PostMapper;
import com.shijian.mapper.UserMapper;
import com.shijian.service.CommentService;
import com.shijian.service.LikeService;
import com.shijian.service.NotificationService;
import com.shijian.service.PointService;
import com.shijian.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private LikeService likeService;
    @Resource
    private LikeRecordMapper likeRecordMapper;
    @Resource
    private NotificationService notificationService;
    @Resource
    private PointService pointService;

    @Override
    @Transactional
    public Result<CommentVO> create(Long userId, CommentDTO dto) {
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            return Result.error("帖子不存在");
        }
        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setImage(dto.getImage());
        comment.setParentId(dto.getParentId());
        commentMapper.insert(comment);
        // Update post comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);
        // 通知帖子作者
        if (!post.getUserId().equals(userId)) {
            notificationService.create(post.getUserId(), userId, "COMMENT", "POST", post.getId(),
                    "评论了你的帖子");
        }
        // 如果是回复，通知被回复者
        if (dto.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(dto.getParentId());
            if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                notificationService.create(parentComment.getUserId(), userId, "REPLY", "COMMENT", parentComment.getId(),
                        "回复了你的评论");
            }
        }
        // 评论积分
        pointService.addPoints(userId, 2, "COMMENT", "POST", post.getId());
        return Result.success("评论成功", toCommentVO(comment, userId));
    }

    @Override
    @Transactional
    public Result<Void> delete(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        User loginUser = userMapper.selectById(userId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        if (!comment.getUserId().equals(userId) && (loginUser == null || !"ADMIN".equals(loginUser.getRole()))) {
            return Result.error("无权限删除");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
        // Update post comment count
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postMapper.updateById(post);
        }
        return Result.success("删除成功", null);
    }

    @Override
    public Result<Void> like(Long userId, Long commentId) {
        return likeService.likeComment(userId, commentId);
    }

    @Override
    public Result<Void> unlike(Long userId, Long commentId) {
        return likeService.unlikeComment(userId, commentId);
    }

    @Override
    public List<CommentVO> getByPostId(Long postId, Long currentUserId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId).eq(Comment::getStatus, 1)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreateTime);
        List<Comment> comments = commentMapper.selectList(wrapper);
        return comments.stream().map(c -> {
            CommentVO vo = toCommentVO(c, currentUserId);
            // Load replies
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getParentId, c.getId())
                    .eq(Comment::getStatus, 1)
                    .orderByAsc(Comment::getCreateTime);
            List<Comment> replies = commentMapper.selectList(replyWrapper);
            vo.setReplies(replies.stream()
                    .map(r -> toCommentVO(r, currentUserId))
                    .collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    private CommentVO toCommentVO(Comment comment, Long currentUserId) {
        CommentVO vo = new CommentVO();
        BeanUtil.copyProperties(comment, vo);
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        if (currentUserId != null) {
            vo.setIsLiked(checkLiked(currentUserId, comment.getId()));
        }
        return vo;
    }

    private boolean checkLiked(Long userId, Long commentId) {
        LambdaQueryWrapper<com.shijian.entity.LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.shijian.entity.LikeRecord::getUserId, userId)
                .eq(com.shijian.entity.LikeRecord::getTargetType, "COMMENT")
                .eq(com.shijian.entity.LikeRecord::getTargetId, commentId)
                .eq(com.shijian.entity.LikeRecord::getIsLike, 1);
        return likeRecordMapper.selectCount(wrapper) > 0;
    }
}
