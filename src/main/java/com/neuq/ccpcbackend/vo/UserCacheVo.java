package com.neuq.ccpcbackend.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserCacheVo {
    String id;
    List<String> identities;
}
