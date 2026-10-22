package com.shijian.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String nickname;
    private String phone;
    private String email;
    private Integer gender;
    private String bio;
    private String avatar;
}
