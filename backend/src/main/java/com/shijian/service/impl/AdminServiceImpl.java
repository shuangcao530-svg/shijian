package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.*;
import com.shijian.mapper.*;
import com.shijian.service.AdminService;
import com.shijian.vo.PostVO;
import com.shijian.vo.StatsVO;
import com.shijian.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private ReportMapper reportMapper;
    @Resource
    private PostImageMapper postImageMapper;

    @Override
    public PageResult<UserVO> listUsers(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or()
                    .like(User::getNickname, keyword).or()
                    .like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> records = userPage.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setPhone(u.getPhone());
            vo.setEmail(u.getEmail());
            vo.setAvatar(u.getAvatar());
            vo.setGender(u.getGender());
            vo.setBio(u.getBio());
            vo.setRole(u.getRole());
            vo.setStatus(u.getStatus());
            vo.setCreateTime(u.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(userPage.getTotal(), page, size, records);
    }

    @Override
    public Result<Void> toggleUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");
        if ("ADMIN".equals(user.getRole())) return Result.error("不能禁用管理员");
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
        return Result.success("操作成功", null);
    }

    @Override
    public PageResult<PostVO> listPendingPosts(Integer page, Integer size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 0).eq(Post::getIsDeleted, 0).orderByDesc(Post::getCreateTime);
        Page<Post> postPage = postMapper.selectPage(new Page<>(page, size), wrapper);
        List<PostVO> records = postPage.getRecords().stream().map(p -> {
            PostVO vo = new PostVO();
            vo.setId(p.getId());
            vo.setUserId(p.getUserId());
            vo.setTitle(p.getTitle());
            vo.setContent(p.getContent());
            vo.setCreateTime(p.getCreateTime());
            User user = userMapper.selectById(p.getUserId());
            if (user != null) vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(postPage.getTotal(), page, size, records);
    }

    @Override
    public Result<Void> auditPost(Long postId, Integer status) {
        Post post = postMapper.selectById(postId);
        if (post == null) return Result.error("帖子不存在");
        post.setStatus(status);
        postMapper.updateById(post);
        return Result.success(status == 1 ? "审核通过" : "已驳回", null);
    }

    @Override
    public Result<Category> addCategory(Category category) {
        categoryMapper.insert(category);
        return Result.success("添加成功", category);
    }

    @Override
    public Result<Category> updateCategory(Long id, Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success("修改成功", category);
    }

    @Override
    public Result<Void> deleteCategory(Long id) {
        categoryMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Override
    @Transactional
    public Result<Void> handleReport(Long adminId, Long reportId, String result, String handleResult) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) return Result.error("举报不存在");
        report.setStatus(result);
        report.setHandleResult(handleResult);
        report.setHandlerId(adminId);
        reportMapper.updateById(report);
        return Result.success("处理成功", null);
    }

    @Override
    public PageResult<?> listReports(Integer page, Integer size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Report::getCreateTime);
        Page<Report> reportPage = reportMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(reportPage.getTotal(), page, size, reportPage.getRecords());
    }

    @Override
    public StatsVO getStats() {
        StatsVO vo = new StatsVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setPostCount(postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getIsDeleted, 0)));
        vo.setCommentCount(commentMapper.selectCount(null));
        // Today stats
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayNewUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));
        vo.setTodayNewPosts(postMapper.selectCount(new LambdaQueryWrapper<Post>().ge(Post::getCreateTime, todayStart).eq(Post::getIsDeleted, 0)));
        vo.setPendingReports(reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING")));
        vo.setPendingPosts(postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 0).eq(Post::getIsDeleted, 0)));
        return vo;
    }
}
