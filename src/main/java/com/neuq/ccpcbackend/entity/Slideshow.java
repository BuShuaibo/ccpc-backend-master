package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("slideshow")
public class Slideshow {
    private String id;
    private String coverUrl;
    private String contentUrl;
    @TableField("`order`")
    private Integer order;
    @TableField("`show`")
    private Boolean show;
    private String title;
}
