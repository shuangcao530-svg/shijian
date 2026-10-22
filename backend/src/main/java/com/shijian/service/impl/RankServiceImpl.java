package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijian.entity.*;
import com.shijian.mapper.*;
import com.shijian.service.RankService;
import com.shijian.vo.PostVO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankServiceImpl implements RankService {
    @Resource
    private PostMapper postMapper;
    @Resource
    private PostImageMapper postImageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<PostVO> getHotPosts(Integer limit) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0).eq(Post::getStatus, 1)
                .eq(Post::getVisibility, "PUBLIC")
                .orderByDesc(Post::getLikeCount, Post::getCommentCount, Post::getViewCount)
                .last("LIMIT " + (limit != null ? limit : 10));
        List<Post> posts = postMapper.selectList(wrapper);
        return posts.stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    @Override
    public List<PostVO> getWeeklyRanking(Integer limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0).eq(Post::getStatus, 1)
                .eq(Post::getVisibility, "PUBLIC")
                .ge(Post::getCreateTime, weekAgo)
                .orderByDesc(Post::getLikeCount, Post::getCommentCount)
                .last("LIMIT " + (limit != null ? limit : 10));
        return postMapper.selectList(wrapper).stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    @Override
    public List<PostVO> getMonthlyRanking(Integer limit) {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0).eq(Post::getStatus, 1)
                .eq(Post::getVisibility, "PUBLIC")
                .ge(Post::getCreateTime, monthAgo)
                .orderByDesc(Post::getLikeCount, Post::getCommentCount)
                .last("LIMIT " + (limit != null ? limit : 10));
        return postMapper.selectList(wrapper).stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    @Override
    public List<PostVO> getCategoryRanking(Long categoryId, Integer limit) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0).eq(Post::getStatus, 1)
                .eq(Post::getVisibility, "PUBLIC")
                .eq(Post::getCategoryId, categoryId)
                .orderByDesc(Post::getLikeCount, Post::getCommentCount)
                .last("LIMIT " + (limit != null ? limit : 10));
        return postMapper.selectList(wrapper).stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    private PostVO toSimpleVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setViewCount(post.getViewCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setCreateTime(post.getCreateTime());
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }
        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) vo.setCategoryName(category.getName());
        LambdaQueryWrapper<PostImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.eq(PostImage::getPostId, post.getId()).orderByAsc(PostImage::getSort);
        vo.setImages(postImageMapper.selectList(imgWrapper).stream().map(PostImage::getUrl).collect(Collectors.toList()));
        return vo;
    }
}
