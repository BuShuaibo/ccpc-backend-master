package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
@TableName("sponsor")
public class Sponsor {
    private String id;
    private String name;
    private String pictureUrl;
    private String officialWebsiteUrl;
    private Integer type;
    @TableField("`show`")
    private Boolean show;
}
