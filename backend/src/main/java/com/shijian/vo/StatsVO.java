package com.shijian.vo;

import lombok.Data;

@Data
public class StatsVO {
    private Long userCount;
    private Long postCount;
    private Long commentCount;
    private Long todayNewUsers;
    private Long todayNewPosts;
    private Long pendingReports;
    private Long pendingPosts;
}
