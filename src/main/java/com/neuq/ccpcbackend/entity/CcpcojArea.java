package com.neuq.ccpcbackend.entity;

import lombok.Data;

import java.util.List;

@Data
public class CcpcojArea {
    String areaName;
    String remark;
    Integer teamNumber;
    List<String> primarySchoolIds;
}
