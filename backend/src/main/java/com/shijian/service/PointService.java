package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.PointRecord;
import java.util.Map;

public interface PointService {
    /** 用户签到 */
    Result<Map<String, Object>> checkin(Long userId);

    /** 今日是否已签到 */
    boolean hasCheckedInToday(Long userId);

    /** 积分变更 */
    void addPoints(Long userId, int points, String reason, String targetType, Long targetId);

    /** 用户积分记录 */
    PageResult<PointRecord> getPointRecords(Long userId, Integer page, Integer size);

    /** 用户积分和等级 */
    Map<String, Object> getUserPointsAndLevel(Long userId);

    /** 积分排行榜 */
    PageResult<Map<String, Object>> getPointsRank(Integer page, Integer size);

    /** 获取用户连续签到天数 */
    int getConsecutiveCheckinDays(Long userId);

    /** 根据积分计算等级 */
    int calculateLevel(int points);

    /** 根据等级获取徽章名称 */
    String getBadgeName(int level);
}
