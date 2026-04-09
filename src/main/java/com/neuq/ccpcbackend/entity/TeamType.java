package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("team_type")
public class TeamType {
    private String id;
    private String typeName;
    private Integer quota;
}
