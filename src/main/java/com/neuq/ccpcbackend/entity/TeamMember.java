package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("team_member")
public class TeamMember {
    String id;
    String teamId;
    String memberId;
}
