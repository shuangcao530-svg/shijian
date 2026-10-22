package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.*;
import com.shijian.mapper.*;
import com.shijian.service.FavoriteService;
import com.shijian.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Resource
    private FavoriteMapper favoriteMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private PostImageMapper postImageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Result<Void> add(Long userId, Long postId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getPostId, postId);
        if (favoriteMapper.selectCount(wrapper) > 0) {
            return Result.success("已收藏", null);
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);
        favoriteMapper.insert(favorite);
        // Update post favorite count
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setFavoriteCount(post.getFavoriteCount() + 1);
            postMapper.updateById(post);
        }
        return Result.success("收藏成功", null);
    }

    @Override
    @Transactional
    public Result<Void> remove(Long userId, Long postId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getPostId, postId);
        favoriteMapper.delete(wrapper);
        // Update post favorite count
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setFavoriteCount(Math.max(0, post.getFavoriteCount() - 1));
            postMapper.updateById(post);
        }
        return Result.success("取消收藏", null);
    }

    @Override
    public PageResult<PostVO> list(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        Page<Favorite> favPage = favoriteMapper.selectPage(new Page<>(page, size), wrapper);
        List<PostVO> records = favPage.getRecords().stream().map(f -> {
            Post post = postMapper.selectById(f.getPostId());
            if (post == null || post.getIsDeleted() == 1) return null;
            return toSimplePostVO(post);
        }).filter(p -> p != null).collect(Collectors.toList());
        return PageResult.of(favPage.getTotal(), page, size, records);
    }

    @Override
    public boolean isFavorited(Long userId, Long postId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getPostId, postId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    private static final String[] LEVEL_TEXTS = {"", "1星", "2星", "3星", "4星", "5星"};

    private PostVO toSimplePostVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setCategoryId(post.getCategoryId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setLevel(post.getLevel());
        if (post.getLevel() >= 1 && post.getLevel() <= 5) {
            vo.setLevelText(LEVEL_TEXTS[post.getLevel()]);
        }
        vo.setVisibility(post.getVisibility());
        vo.setLocation(post.getLocation());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setStatus(post.getStatus());
        vo.setIsFavorited(true);
        vo.setCreateTime(post.getCreateTime());
        vo.setUpdateTime(post.getUpdateTime());
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) vo.setCategoryName(category.getName());
        LambdaQueryWrapper<PostImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.eq(PostImage::getPostId, post.getId()).orderByAsc(PostImage::getSort);
        List<PostImage> images = postImageMapper.selectList(imgWrapper);
        vo.setImages(images.stream()
                .map(img -> "/api/image/" + img.getId())
                .collect(Collectors.toList()));
        return vo;
    }
}
