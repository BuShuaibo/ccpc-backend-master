package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("competition_type")
public class CompetitionType {
    String id;
    String typeName;
}
