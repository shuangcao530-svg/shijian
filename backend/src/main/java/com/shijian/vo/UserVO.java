package com.shijian.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private String bio;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private Boolean isFriend;
    private Long requestId;
}
