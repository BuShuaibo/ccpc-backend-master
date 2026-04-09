package com.neuq.ccpcbackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ccpcoj")
public class Ccpcoj {
    String id;
    String competitionId;

    /*
    格式
    Array
        Dict
            key-areaName-String
            key-remark-String
            key-teamNumber-String
            key-primarySchoolIds-Array
                schoolId-String

    [
        {
            area_name: "",
            remark: "",
            team_number: 1,
            primary_school_ids: ["", ""]
        },
        {
            area_name: "",
            remark: "",
            team_number: 1,
            primary_school_ids: ["", ""]
        }
    ]
    */
    String ccpcojAreas;

    /*
    格式
    Dict
        key-"areaName"-List
            Dict
                key-signupId-String
                key-account-String
                key-password-String

    {
        "A": [
            {
                signup_id: "",
                account: "A01",
                password: "123456",
            },
            {
                signup_id: "",
                account: "A01",
                password: "123456",
            }
        ],
        "B": []
    }
    */
    String ccpcojAccounts;
}
