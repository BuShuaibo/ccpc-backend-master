package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quota_distribution")
public class QuotaDistribution {
    public String id;
    public String competitionId;
    public String schoolId;

    public Long quotaSimple;
    public Long quotaGirl;
    public Long quotaAddition;
    public Long quotaUnofficial;

    public Long quotaSimpleRest;
    public Long quotaGirlRest;
    public Long quotaAdditionRest;
    public Long quotaUnofficialRest;
}
