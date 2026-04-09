package com.neuq.ccpcbackend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuq.ccpcbackend.dto.CcpcojAssignAccountsResponse;
import com.neuq.ccpcbackend.entity.*;
import com.neuq.ccpcbackend.mapper.*;
import com.neuq.ccpcbackend.service.CcpcojService;
import com.neuq.ccpcbackend.utils.ExcelUtil;
import com.neuq.ccpcbackend.utils.PasswordUtil;
import com.neuq.ccpcbackend.utils.RoleUtil;
import com.neuq.ccpcbackend.utils.exception.BizException;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.vo.CcpcojAccountsVo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class CcpcojServiceImpl implements CcpcojService {

    @Resource
    private CcpcojMapper ccpcojMapper;

    @Resource
    private SignupMapper signupMapper;

    @Resource
    private CompetitionMapper competitionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response setAreas(String competitionId, List<CcpcojArea> ccpcojAreas) {
        // 检查身份
        if (!RoleUtil.hasRole("admin")) {
            String id = RoleUtil.getCurrentUserId();
            LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Competition::getId, competitionId).select(Competition::getId, Competition::getCompetitionAdminId);
            Competition competition = competitionMapper.selectOne(queryWrapper);
            if (competition == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
            }
            if (!id.equals(competition.getCompetitionAdminId())) {
                throw new BizException(ErrorCode.ACCESS_DENIED.getErrCode(), "您无权管理该比赛");
            }
        }

        LambdaQueryWrapper<Ccpcoj> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ccpcoj::getCompetitionId, competitionId).select(Ccpcoj::getId);
        Ccpcoj ccpcoj = ccpcojMapper.selectOne(wrapper);
        if (ccpcoj == null) {
            Ccpcoj newCcpcoj = new Ccpcoj();
            newCcpcoj.setCompetitionId(competitionId);
            newCcpcoj.setId(UUID.randomUUID().toString());
            String ccpcojAreasString = JSON.toJSONString(ccpcojAreas);
            newCcpcoj.setCcpcojAreas(ccpcojAreasString);
            int res = ccpcojMapper.insert(newCcpcoj);
            if (res == 1) {
                return Response.success();
            } else {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "设置比赛区域失败");
            }
        } else {
            String ccpcojAreasString = JSON.toJSONString(ccpcojAreas);
            ccpcoj.setCcpcojAreas(ccpcojAreasString);
            // 更新分区就要清空账号分配
            String ccpcojAccountsString = JSON.toJSONString(new ArrayList<>());
            ccpcoj.setCcpcojAccounts(ccpcojAccountsString);
            int res = ccpcojMapper.updateById(ccpcoj);
            if (res == 1) {
                return Response.success();
            } else {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "设置比赛区域失败");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response assignAccounts(String competitionId) {
        // 检查身份
        if (!RoleUtil.hasRole("admin")) {
            String id = RoleUtil.getCurrentUserId();
            LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Competition::getId, competitionId).select(Competition::getId, Competition::getCompetitionAdminId);
            Competition competition = competitionMapper.selectOne(queryWrapper);
            if (competition == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
            }
            if (!id.equals(competition.getCompetitionAdminId())) {
                throw new BizException(ErrorCode.ACCESS_DENIED.getErrCode(), "您无权管理该比赛");
            }
        }

        LambdaQueryWrapper<Ccpcoj> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ccpcoj::getCompetitionId, competitionId).select(Ccpcoj::getId, Ccpcoj::getCcpcojAreas);
        Ccpcoj ccpcoj = ccpcojMapper.selectOne(wrapper);
        if (ccpcoj == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在或者还未分配区域");
        }
        if (ccpcoj.getCcpcojAreas() == null || ccpcoj.getCcpcojAreas().isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "该比赛还未分配区域");
        }

        // 获取区域分配
        List<CcpcojArea> ccpcojAreas = JSON.parseArray(ccpcoj.getCcpcojAreas(), CcpcojArea.class);

        // 获取所有报名成功，通过审核的队伍
        LambdaQueryWrapper<Signup> queryWrapperSignup = new LambdaQueryWrapper<>();
        queryWrapperSignup.eq(Signup::getCompetitionId, competitionId)
                .ge(Signup::getProcessStatus, 2)
                .select(Signup::getId, Signup::getSchoolId)
                .orderByAsc(Signup::getSchoolId);
        List<Signup> signups = signupMapper.selectList(queryWrapperSignup);

        // 分配座位
        Map<String, List<Signup>> accounts = new HashMap<>();  // 记录座位分配情况
        Map<String, Boolean> assigned = new HashMap<>();  // 记录某个signupId是否已经分配的
        // 优先分配并初始化结构
        for (CcpcojArea ccpcojArea : ccpcojAreas) {
            String areaName = ccpcojArea.getAreaName();
            accounts.put(areaName, new ArrayList<>());
            Integer teamNumber = ccpcojArea.getTeamNumber();
            List<String> primarySchoolIds = ccpcojArea.getPrimarySchoolIds();
            for (String primarySchoolId : primarySchoolIds) {
                // 获取所有优先分配并且还没有分配的signup
                List<Signup> signupsBySchool = signups.stream()
                        .filter(signup -> primarySchoolId.equals(signup.getSchoolId()))
                        .filter(signup -> !assigned.containsKey(signup.getId()) || !assigned.get(signup.getId()))
                        .toList();

                // 计算还能添加的数量
                int remainingSlots = teamNumber - accounts.get(areaName).size();
                List<Signup> signupsToAdd = signupsBySchool.size() <= remainingSlots
                        ? signupsBySchool
                        : signupsBySchool.subList(0, remainingSlots);

                // 添加
                accounts.get(areaName).addAll(signupsToAdd);

                // 标记已分配
                for (Signup signup : signupsToAdd) {
                    assigned.put(signup.getId(), true);
                }

                if (accounts.get(areaName).size() >= teamNumber) {
                    break;
                }
            }
        }

        // 分配其他signup
        for (Signup signup : signups) {
            if (!assigned.containsKey(signup.getId()) || !assigned.get(signup.getId())) {
                // 分配到一个还有多余名额的区域
                for (CcpcojArea ccpcojArea : ccpcojAreas) {
                    String areaName = ccpcojArea.getAreaName();
                    Integer teamNumber = ccpcojArea.getTeamNumber();
                    int remainingSlots = teamNumber - accounts.get(areaName).size();
                    if (remainingSlots - 1 >= 0) {
                        accounts.get(areaName).add(signup);
                        assigned.put(signup.getId(), true);
                        break;
                    }
                }
            }
        }

        // 区域总名额小于报名成功总队伍数，将剩余signup分配到notAssigned
        accounts.put("notAssigned", new ArrayList<>());
        for (Signup signup : signups) {
            if (!assigned.containsKey(signup.getId()) || !assigned.get(signup.getId())) {
                accounts.get("notAssigned").add(signup);
            }
        }

        // 同区域内同校队伍不相连
        for (CcpcojArea ccpcojArea : ccpcojAreas) {
            String areaName = ccpcojArea.getAreaName();
            List<Signup> signupsSameArea = accounts.get(areaName);
            signupsSameArea.sort(Comparator.comparing(Signup::getSchoolId));

            List<Signup> signupsNewOrder = new ArrayList<>();
            Map<String, Boolean> sorted = new HashMap<>();
            int preSize = 0;
            while(signupsNewOrder.size() != signupsSameArea.size()) {
                String preSchoolId = "";
                for (Signup signup : signupsSameArea) {
                    if (!sorted.containsKey(signup.getId()) || !sorted.get(signup.getId())) {
                        if (!signup.getSchoolId().equals(preSchoolId)) {
                            signupsNewOrder.add(signup);
                            preSchoolId = signup.getSchoolId();
                            sorted.put(signup.getId(), true);
                        }
                    }
                }
                if (signupsNewOrder.size() == preSize) {
                    break;
                }
                preSize = signupsNewOrder.size();
            }
            for (Signup signup : signupsSameArea) {
                if (!sorted.containsKey(signup.getId()) || !sorted.get(signup.getId())) {
                    signupsNewOrder.add(signup);
                    sorted.put(signup.getId(), true);
                }
            }
            accounts.put(areaName, signupsNewOrder);
        }

        // 写入数据库
        Map<String, List<CcpcojAccount>> res = new HashMap<>();
        for (String areaName : accounts.keySet()) {
            List<Signup> signupss = accounts.get(areaName);
            res.put(areaName, Arrays.stream(IntStream.range(0, signupss.size())
                    .mapToObj(i -> {
                        Signup signup = signupss.get(i);
                        CcpcojAccount ccpcojAccount = new CcpcojAccount();
                        ccpcojAccount.setSignupId(signup.getId());
                        ccpcojAccount.setAccount(areaName + String.format("%02d", i+1));
                        ccpcojAccount.setPassword(PasswordUtil.generatePassword(6, areaName + String.format("%02d", i) + "|" + competitionId));
                        return ccpcojAccount;
                    })
                    .toArray(CcpcojAccount[]::new)).toList());
        }
        ccpcoj.setCcpcojAccounts(JSON.toJSONString(res));
        int result = ccpcojMapper.updateById(ccpcoj);
        if (result != 1) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "分配座位失败");
        }

        // 返回信息
        Map<String, List<CcpcojAssignAccountsResponse>> ans = new HashMap<>();
        for (String areaName : accounts.keySet()) {
            List<CcpcojAccount> ccpcojAccounts = res.get(areaName);
            ans.put(areaName, Arrays.stream(ccpcojAccounts.stream().map(account -> {
                        CcpcojAccountsVo ccpcojAccountVo = ccpcojMapper.getCcpcojAccount(account, areaName);
                        CcpcojAssignAccountsResponse ccpcojAssignAccountsResponse = new CcpcojAssignAccountsResponse();
                        BeanUtils.copyProperties(ccpcojAccountVo, ccpcojAssignAccountsResponse);
                        ccpcojAssignAccountsResponse.setSignupId(account.getSignupId());
                        return ccpcojAssignAccountsResponse;
                    })
                    .toArray(CcpcojAssignAccountsResponse[]::new)).toList());
        }
        return Response.of(ans);
    }

    @Override
    public Response setAccounts(String competitionId, Map<String, List<CcpcojAccount>> ccpcojAccounts) {
        // 检查身份
        if (!RoleUtil.hasRole("admin")) {
            String id = RoleUtil.getCurrentUserId();
            LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Competition::getId, competitionId).select(Competition::getId, Competition::getCompetitionAdminId);
            Competition competition = competitionMapper.selectOne(queryWrapper);
            if (competition == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
            }
            if (!id.equals(competition.getCompetitionAdminId())) {
                throw new BizException(ErrorCode.ACCESS_DENIED.getErrCode(), "您无权管理该比赛");
            }
        }

        // 查询数据
        LambdaQueryWrapper<Ccpcoj> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ccpcoj::getCompetitionId, competitionId).select(Ccpcoj::getId, Ccpcoj::getCcpcojAreas);
        Ccpcoj ccpcoj = ccpcojMapper.selectOne(wrapper);
        if (ccpcoj == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在或者还未分配区域");
        }
        if (ccpcoj.getCcpcojAreas() == null || ccpcoj.getCcpcojAreas().isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "该比赛还未分配区域");
        }

        // 检查分配的账号是否满足区域分配
        List<CcpcojArea> ccpcojAreas = JSON.parseArray(ccpcoj.getCcpcojAreas(), CcpcojArea.class);
        Map<String, Integer> quota = new HashMap<>();
        for (CcpcojArea area : ccpcojAreas) {
            quota.put(area.getAreaName(), area.getTeamNumber());
        }
        for (String key : ccpcojAccounts.keySet()) {
            if (key.equals("notAssigned")) {
                continue;
            }
            if (!quota.containsKey(key)) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "区域" + key + "不存在");
            }
            if (ccpcojAccounts.get(key).size() > quota.get(key)) {
                throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "区域" + key + "分配的队伍数量超过区域限额");
            }
            for (CcpcojAccount account : ccpcojAccounts.get(key)) {
                account.setPassword(PasswordUtil.generatePassword(6, account.getAccount() + "|" + competitionId));
            }
        }

        ccpcoj.setCcpcojAccounts(JSON.toJSONString(ccpcojAccounts));
        int result = ccpcojMapper.updateById(ccpcoj);
        if (result == 1) {
            return Response.success();
        } else {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "设置账号分配失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exportCcpcojAccounts(String competitionId, HttpServletResponse response) {
        // 检查身份
        if (!RoleUtil.hasRole("admin")) {
            String id = RoleUtil.getCurrentUserId();
            LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Competition::getId, competitionId).select(Competition::getId, Competition::getCompetitionAdminId);
            Competition competition = competitionMapper.selectOne(queryWrapper);
            if (competition == null) {
                throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
            }
            if (!id.equals(competition.getCompetitionAdminId())) {
                throw new BizException(ErrorCode.ACCESS_DENIED.getErrCode(), "您无权管理该比赛");
            }
        }

        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Competition::getId, competitionId).select(Competition::getId, Competition::getName);
        Competition competition = competitionMapper.selectOne(queryWrapper);
        if (competition == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }

        Ccpcoj ccpcoj = ccpcojMapper.selectOne(new LambdaQueryWrapper<Ccpcoj>().eq(Ccpcoj::getCompetitionId, competitionId));
        if (ccpcoj == null || ccpcoj.getCcpcojAreas() == null || ccpcoj.getCcpcojAreas().isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛还未分配区域");
        }
        if (ccpcoj.getCcpcojAccounts() == null || ccpcoj.getCcpcojAccounts().isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "该比赛还未分配账号");
        }

        // 添加数据
        List<List<String>> ans = new ArrayList<>();
        List<String> header = new ArrayList<>();
        header.add("账号");
        header.add("队伍名");
        header.add("学校");
        header.add("队员");
        header.add("证书老师");
        header.add("区域");
        header.add("队伍类型");
        header.add("密码");
        ans.add(header);
        Map<String, List<CcpcojAccount>> res = JSON.parseObject(ccpcoj.getCcpcojAccounts(), new TypeReference<Map<String, List<CcpcojAccount>>>() {});
        for (String key : res.keySet()) {
            for (CcpcojAccount account : res.get(key)) {
                CcpcojAccountsVo ccpcojAccount = ccpcojMapper.getCcpcojAccount(account, key);
                List<String> row = new ArrayList<>();
                row.add(ccpcojAccount.getAccount());
                row.add(ccpcojAccount.getTeamName());
                row.add(ccpcojAccount.getSchool());
                row.add(ccpcojAccount.getTeamMembers());
                row.add(ccpcojAccount.getPrizeCoach());
                row.add(ccpcojAccount.getArea());
                // 需要和ccpcoj的对应类型的值进行对应
                row.add(String.valueOf(ccpcojAccount.getType() == 2 ? 0 : ccpcojAccount.getType() == 3 ? 2 : ccpcojAccount.getType()));
                row.add(ccpcojAccount.getPassword());
                ans.add(row);
            }
        }

        try {
            byte[] bytes = ExcelUtil.generateExcel(ans);

            String fileName = competition.getName() + "ccpcoj选手账号.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setContentType("application/octet-stream;character=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentLength(bytes.length);

            // 禁止缓存
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // 将二进制文件写入输出流
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(bytes);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new BizException(ErrorCode.DOWNLOAD_FAILED.getErrCode(), "下载文件失败");
        }
    }
}
