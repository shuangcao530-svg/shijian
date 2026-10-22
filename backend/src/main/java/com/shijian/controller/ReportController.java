package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.dto.ReportDTO;
import com.shijian.service.ReportService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Resource
    private ReportService reportService;

    @PostMapping
    public Result<Void> submit(@Valid @RequestBody ReportDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return reportService.submit(userId, dto);
    }
}
