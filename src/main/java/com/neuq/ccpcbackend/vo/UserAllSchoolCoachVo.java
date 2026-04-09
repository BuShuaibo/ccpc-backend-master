package com.neuq.ccpcbackend.vo;

import com.neuq.ccpcbackend.entity.User;
import lombok.Data;

import java.util.List;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */

@Data
public class UserAllSchoolCoachVo {
    private List<User> coachList;
    private Long pageTotal;     //总条目数
    private Long pages;     // 总页数
}
