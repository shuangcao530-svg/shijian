package com.shijian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post_topic")
public class PostTopic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long topicId;
    private LocalDateTime createTime;
}
