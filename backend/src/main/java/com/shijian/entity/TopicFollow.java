package com.shijian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("topic_follow")
public class TopicFollow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long topicId;
    private LocalDateTime createTime;
}
