package com.wms.vo;

import lombok.Data;

/**
 * 登录返回 VO
 */
@Data
public class LoginVO {
    private String token;
    private String role;
    private String username;
    private String realName;
    private Long userId;
}
