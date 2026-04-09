package com.neuq.ccpcbackend.dto;

import lombok.Data;

@Data
public class UserGetAddressInfoResponse {
    String id;
    String address;
    String addressee;
    String addressPhone;
}
