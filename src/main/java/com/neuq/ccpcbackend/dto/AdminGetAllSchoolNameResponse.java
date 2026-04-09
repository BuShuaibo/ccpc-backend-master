package com.neuq.ccpcbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllSchoolNameResponse {
    @Data
    public static class School{
        String Name;
        String Id;
    }
    List<School> schoolList;
}
