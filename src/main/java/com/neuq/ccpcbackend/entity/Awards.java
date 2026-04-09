package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("awards")
public class Awards {
    String id;
    String competitionId;
    String awardType;
    String recipient;
    String competitionGroupCategoryId;
    String awardName;
    String awardNameEn;
}
