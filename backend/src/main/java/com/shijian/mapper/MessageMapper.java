package com.shijian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijian.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
