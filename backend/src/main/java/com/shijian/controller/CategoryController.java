package com.shijian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijian.common.Result;
import com.shijian.entity.Category;
import com.shijian.mapper.CategoryMapper;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Resource
    private CategoryMapper categoryMapper;

    @GetMapping
    public Result<List<Category>> list() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1).orderByAsc(Category::getSort);
        return Result.success(categoryMapper.selectList(wrapper));
    }
}
