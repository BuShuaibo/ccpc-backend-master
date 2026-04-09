package com.neuq.ccpcbackend.controller;

import com.neuq.ccpcbackend.entity.CcpcojAccount;
import com.neuq.ccpcbackend.entity.CcpcojArea;
import com.neuq.ccpcbackend.service.CcpcojService;
import com.neuq.ccpcbackend.utils.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ccpcoj")
public class CcpcojController {

    @Resource
    CcpcojService ccpcojService;

    @PostMapping("/setAreas")
    @PreAuthorize("hasAnyAuthority('admin', 'contestAdmin')")
    public Response setAreas(@RequestParam String competitionId, @RequestBody List<CcpcojArea> ccpcojAreas) {
        return ccpcojService.setAreas(competitionId, ccpcojAreas);
    }

    @PostMapping("/assignAccounts")
    @PreAuthorize("hasAnyAuthority('admin', 'contestAdmin')")
    public Response assignAccounts(@RequestParam String competitionId) {
        return ccpcojService.assignAccounts(competitionId);
    }

    @PostMapping("/setAccounts")
    @PreAuthorize("hasAnyAuthority('admin', 'contestAdmin')")
    public Response setAccounts(@RequestParam String competitionId, @RequestBody Map<String, List<CcpcojAccount>> ccpcojAccounts) {
        return ccpcojService.setAccounts(competitionId, ccpcojAccounts);
    }

    @GetMapping("/exportCcpcojAccounts")
    @PreAuthorize("hasAnyAuthority('admin', 'contestAdmin')")
    public void exportCcpcojAccounts(@RequestParam String competitionId, HttpServletResponse response) {
        ccpcojService.exportCcpcojAccounts(competitionId, response);
    }
}
