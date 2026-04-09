package com.neuq.ccpcbackend.service;

import com.neuq.ccpcbackend.entity.CcpcojAccount;
import com.neuq.ccpcbackend.entity.CcpcojArea;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

public interface CcpcojService {
    Response setAreas(String competitionId, List<CcpcojArea> ccpcojAreas);
    Response assignAccounts(String competitionId);
    Response setAccounts(String competitionId, Map<String, List<CcpcojAccount>> ccpcojAccounts);
    void exportCcpcojAccounts(String competitionId, HttpServletResponse response);
}
