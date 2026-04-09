package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("system_info")
public class SystemInfo {
    private String id;
    private Integer seasonNumber;
    private Integer schoolNumber;
    private Integer contestNumber;
    private Integer currentSeasonContestNumber;
}
