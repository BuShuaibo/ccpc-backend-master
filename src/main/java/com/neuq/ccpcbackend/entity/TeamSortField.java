package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * Enum for team sorting fields
 */
public enum TeamSortField {
    name(Team::getName),
    season(Team::getSeason);
    public final SFunction<Team, Object> field;

    TeamSortField(SFunction<Team, Object> field) {
        this.field = field;
    }
}