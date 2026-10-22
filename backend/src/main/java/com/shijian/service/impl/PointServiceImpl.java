package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.Checkin;
import com.shijian.entity.PointRecord;
import com.shijian.entity.User;
import com.shijian.mapper.CheckinMapper;
import com.shijian.mapper.PointRecordMapper;
import com.shijian.mapper.UserMapper;
import com.shijian.service.PointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PointServiceImpl implements PointService {
    @Resource
    private CheckinMapper checkinMapper;
    @Resource
    private PointRecordMapper pointRecordMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional
    public Result<Map<String, Object>> checkin(Long userId) {
        LocalDate today = LocalDate.now();
        if (hasCheckedInToday(userId)) {
            return Result.error("今日已签到");
        }

        User user = userMapper.selectById(userId);
        LocalDate lastCheckin = null;
        try {
            // 查询最近一次签到
            LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Checkin::getUserId, userId)
                    .orderByDesc(Checkin::getCheckinDate)
                    .last("LIMIT 1");
            Checkin last = checkinMapper.selectList(wrapper).stream().findFirst().orElse(null);
            if (last != null) lastCheckin = last.getCheckinDate();
        } catch (Exception ignored) {}

        // 计算连续天数
        int consecutiveDays = 1;
        if (lastCheckin != null && lastCheckin.equals(today.minusDays(1))) {
            consecutiveDays = (user.getConsecutiveCheckinDays() != null ? user.getConsecutiveCheckinDays() : 0) + 1;
        }

        // 计算积分（基础5分 + 连续奖励）
        int pointsAwarded = 5 + Math.min(consecutiveDays - 1, 7) * 2; // 最多额外14分

        // 插入签到记录
        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setConsecutiveDays(consecutiveDays);
        checkin.setPointsAwarded(pointsAwarded);
        checkinMapper.insert(checkin);

        // 更新用户积分和签到信息
        int newPoints = (user.getPoints() != null ? user.getPoints() : 0) + pointsAwarded;
        user.setPoints(newPoints);
        user.setConsecutiveCheckinDays(consecutiveDays);
        user.setLastCheckinDate(today);
        user.setLevel(calculateLevel(newPoints));
        userMapper.updateById(user);

        // 插入积分记录
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(pointsAwarded);
        record.setReason("CHECKIN");
        record.setTargetType("CHECKIN");
        record.setTargetId(checkin.getId());
        pointRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("pointsAwarded", pointsAwarded);
        result.put("totalPoints", newPoints);
        result.put("consecutiveDays", consecutiveDays);
        result.put("level", user.getLevel());
        result.put("badge", getBadgeName(user.getLevel()));
        return Result.success("签到成功！+" + pointsAwarded + "积分", result);
    }

    @Override
    public boolean hasCheckedInToday(Long userId) {
        return checkinMapper.selectCount(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .eq(Checkin::getCheckinDate, LocalDate.now())) > 0;
    }

    @Override
    @Transactional
    public void addPoints(Long userId, int points, String reason, String targetType, Long targetId) {
        User user = userMapper.selectById(userId);
        if (user == null) return;
        int newPoints = (user.getPoints() != null ? user.getPoints() : 0) + points;
        user.setPoints(newPoints);
        user.setLevel(calculateLevel(newPoints));
        userMapper.updateById(user);

        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setReason(reason);
        record.setTargetType(targetType);
        record.setTargetId(targetId);
        pointRecordMapper.insert(record);
    }

    @Override
    public PageResult<PointRecord> getPointRecords(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<PointRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointRecord::getUserId, userId).orderByDesc(PointRecord::getCreateTime);
        Page<PointRecord> p = pointRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(p.getTotal(), page, size, p.getRecords());
    }

    @Override
    public Map<String, Object> getUserPointsAndLevel(Long userId) {
        User user = userMapper.selectById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("points", user.getPoints() != null ? user.getPoints() : 0);
        result.put("level", user.getLevel() != null ? user.getLevel() : 1);
        result.put("badge", getBadgeName(user.getLevel() != null ? user.getLevel() : 1));
        result.put("consecutiveCheckinDays", user.getConsecutiveCheckinDays() != null ? user.getConsecutiveCheckinDays() : 0);
        result.put("hasCheckedInToday", hasCheckedInToday(userId));
        result.put("nextLevelPoints", getNextLevelPoints(user.getLevel() != null ? user.getLevel() : 1));
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> getPointsRank(Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getPoints);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<Map<String, Object>> records = p.getRecords().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            m.put("avatar", u.getAvatar());
            m.put("points", u.getPoints() != null ? u.getPoints() : 0);
            m.put("level", u.getLevel() != null ? u.getLevel() : 1);
            m.put("badge", getBadgeName(u.getLevel() != null ? u.getLevel() : 1));
            return m;
        }).collect(Collectors.toList());
        return PageResult.of(p.getTotal(), page, size, records);
    }

    @Override
    public int getConsecutiveCheckinDays(Long userId) {
        User user = userMapper.selectById(userId);
        return user.getConsecutiveCheckinDays() != null ? user.getConsecutiveCheckinDays() : 0;
    }

    @Override
    public int calculateLevel(int points) {
        if (points >= 10000) return 10;
        if (points >= 5000) return 9;
        if (points >= 3000) return 8;
        if (points >= 2000) return 7;
        if (points >= 1000) return 6;
        if (points >= 500) return 5;
        if (points >= 200) return 4;
        if (points >= 100) return 3;
        if (points >= 50) return 2;
        return 1;
    }

    @Override
    public String getBadgeName(int level) {
        switch (level) {
            case 10: return "校园传奇";
            case 9: return "校园领袖";
            case 8: return "校园达人";
            case 7: return "资深校友";
            case 6: return "活跃之星";
            case 5: return "人气新星";
            case 4: return "小有名气";
            case 3: return "初露锋芒";
            case 2: return "新生报到";
            default: return "校园萌新";
        }
    }

    private int getNextLevelPoints(int currentLevel) {
        switch (currentLevel) {
            case 1: return 50;
            case 2: return 100;
            case 3: return 200;
            case 4: return 500;
            case 5: return 1000;
            case 6: return 2000;
            case 7: return 3000;
            case 8: return 5000;
            case 9: return 10000;
            default: return 0;
        }
    }
}
