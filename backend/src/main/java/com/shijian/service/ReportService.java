package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.dto.ReportDTO;
import com.shijian.entity.Report;

public interface ReportService {
    Result<Void> submit(Long userId, ReportDTO dto);
    PageResult<Report> listPending(Integer page, Integer size);
    Result<Void> handle(Long adminId, Long reportId, String result, String handleResult);
}
