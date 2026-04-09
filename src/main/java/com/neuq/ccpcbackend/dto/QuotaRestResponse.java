package com.neuq.ccpcbackend.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author MachiskComic
 * @ClassName QuotaRestResponse
 * @date 2025-04-24 19:25
 */
@Data
@Builder
public class QuotaRestResponse {
    public String id;
    public String competitionId;
    public String schoolId;

    public String quotaSimpleRest;
    public String quotaGirlRest;
    public String quotaAdditionRest;
    public String quotaUnofficialRest;
}
