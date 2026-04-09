package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */

@Data
@TableName("announcement")
public class Announcement {
    public String id;
    public String title;
    public String authorId;
    public String summary;
    public String coverPictureUrl;
    public String content;
    public Long createAt;
    public Long updateAt;
    public Integer status;
    public Long viewCount;
    public String season;
}
