package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("competition_group_category")
public class CompetitionGroupCategory {
    String id;
    String competitionId;
    String name;
}
