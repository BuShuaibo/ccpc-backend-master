package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("rank")
public class Rank {
    String id;
    String competitionId;
    String competitionGroupCategoryId;

    /*
    * List<RankDetail> 类型转换为json字符串
    * String rankDetail = JSON.toJSONString(new List<RankDetail>());
    * List<RankDetail> rankDetailList = JSON.parseArray(rankDetail, RankDetail.class);
    * */
    String rankDetail;
}
