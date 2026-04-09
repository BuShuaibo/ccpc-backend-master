package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("season")
public class Season {
    String id;
    Boolean isCurrentSeason;
}
