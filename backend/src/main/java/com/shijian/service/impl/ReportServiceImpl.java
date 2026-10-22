package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.ReportDTO;
import com.shijian.entity.Report;
import com.shijian.mapper.ReportMapper;
import com.shijian.service.ReportService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class ReportServiceImpl implements ReportService {
    @Resource
    private ReportMapper reportMapper;

    @Override
    public Result<Void> submit(Long userId, ReportDTO dto) {
        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setStatus("PENDING");
        reportMapper.insert(report);
        return Result.success("举报已提交", null);
    }

    @Override
    public PageResult<Report> listPending(Integer page, Integer size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, "PENDING").orderByDesc(Report::getCreateTime);
        Page<Report> reportPage = reportMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(reportPage.getTotal(), page, size, reportPage.getRecords());
    }

    @Override
    public Result<Void> handle(Long adminId, Long reportId, String result, String handleResult) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            return Result.error("举报不存在");
        }
        report.setStatus(result);
        report.setHandleResult(handleResult);
        report.setHandlerId(adminId);
        reportMapper.updateById(report);
        return Result.success("处理成功", null);
    }
}
