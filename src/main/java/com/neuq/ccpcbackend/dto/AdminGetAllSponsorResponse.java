package com.neuq.ccpcbackend.dto;

import com.neuq.ccpcbackend.entity.Sponsor;
import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllSponsorResponse {
    long count;
    List<Sponsor> sponsors;
}
