package com.neuq.ccpcbackend.vo;

import com.neuq.ccpcbackend.entity.Sponsor;
import lombok.Data;

import java.util.List;

/**
 * @Author: lambertyu233
 * @Description:
 * @Version: 1.0
 */
@Data
public class IndexSponsorVo {
    //白金赞助商
    private List<Sponsor> platinumSponsors;
    //普通赞助商
    private List<Sponsor> normalSponsors;
    //支持院校
    private List<Sponsor> schools;
}
