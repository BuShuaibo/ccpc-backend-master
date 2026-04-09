package com.neuq.ccpcbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.neuq.ccpcbackend.dto.*;
import com.neuq.ccpcbackend.entity.*;
import com.neuq.ccpcbackend.mapper.*;
import com.neuq.ccpcbackend.service.CompetitionService;
import com.neuq.ccpcbackend.utils.ExcelUtil;
import com.neuq.ccpcbackend.utils.ImageUtil;
import com.neuq.ccpcbackend.utils.RoleUtil;
import com.neuq.ccpcbackend.utils.TimeUtil;
import com.neuq.ccpcbackend.utils.exception.BizException;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.vo.CompetingSchoolsExportVo;
import com.neuq.ccpcbackend.vo.SchoolInvoiceInfoVO;
import com.neuq.ccpcbackend.vo.TeamExportVo;
import com.neuq.ccpcbackend.vo.TeamVo;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.neuq.ccpcbackend.config.GlobalExceptionHandler.log;

@Service
public class CompetitionServiceImpl implements CompetitionService {

    @Resource
    private CompetitionMapper competitionMapper;
    @Resource
    private SignupMapper signupMapper;
    @Resource
    private SchoolMapper schoolMapper;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SignupMemberMapper signupMemberMapper;
    @Resource
    private QuotaDistributionMapper quotaDistributionMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompetitionInfo(CompetitionUpdateInfoRequest request) {
        if (request.getId() == null || request.getId().isEmpty()) {
            throw new BizException(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }

        // 检查比赛是否存在
        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Competition::getId, request.getId());
        Competition competition = competitionMapper.selectOne(queryWrapper);
        if (competition == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }

        // 创建新的competition对象并复制属性
        Competition updatedCompetition = new Competition();
        BeanUtils.copyProperties(request, updatedCompetition);

        // 设置更新时间
        updatedCompetition.setUpdatedAt(TimeUtil.getCurrentTimestamp());

        // 更新数据库
        int result = competitionMapper.updateById(updatedCompetition);
        if (result != 1) {
            throw new BizException(ErrorCode.UPDATE_FAILED.getErrCode(), "更新比赛信息失败");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response exportCompetingSchools(String competitionId, HttpServletResponse response) {
        List<CompetingSchoolsExportVo> schools = schoolMapper.getIdByCompetitionId(competitionId);
        List<List<String>> excelData = new ArrayList<>();
        excelData.add(List.of(
                "中文校名", "英文校名", "通信地址",
                "单位名称", "管理员姓名", "管理员电话号"
        ));

        schools.forEach(school ->
                excelData.add(List.of(
                        safe(school.getName()),
                        safe(school.getNameEn()),
                        safe(school.getMailingAddress()),
                        safe(school.getInstitution()),
                        safe(school.getSchoolAdminName()),
                        safe(school.getSchoolAdminPhone())
                ))
        );
        String fileName = getFileName(competitionId, "参赛学校");
        exportExcel(response, fileName, excelData);
        return Response.success();
    }

    @Override
    public Response exportCompetingSchoolsEmblem(String competitionId, HttpServletResponse response) {
        List<ExportSchoolsEmblem> schoolsEmblem = schoolMapper.selectCompetingSchoolsEmblem(competitionId);
        if (schoolsEmblem.isEmpty()) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "无学校徽章");
        }
        List<String> schoolBadgeUrls = schoolsEmblem.stream().map(ExportSchoolsEmblem::getSchoolBadgeUrl).toList();
        List<String> schoolNames = schoolsEmblem.stream().map(ExportSchoolsEmblem::getName).toList();
        try {
            ImageUtil.imagesToZip("参赛学校校徽", schoolBadgeUrls, schoolNames, response);
        } catch (Exception e) {
            return Response.error(ErrorCode.DOWNLOAD_FAILED.getErrCode(), "导出失败");
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response exportCoaches(String competitionId, HttpServletResponse response) {
        if (competitionId == null || competitionId.isEmpty()) {
            return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }
        //获取coachId集合
        List<Signup> signups = signupMapper.selectList(new LambdaQueryWrapper<Signup>()
                .eq(Signup::getCompetitionId, competitionId));
        //用HashSet存储教练ID实现去重
        HashSet<String> coachIdSet = new HashSet<>();
        signups.forEach(signup -> coachIdSet.add(signup.getCoachId()));
        if (coachIdSet.isEmpty()) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "无教练");
        }
        ArrayList<String> coaches = new ArrayList<>(coachIdSet);
        //根据coachId在User表中找到人员数据
        List<User> users = userMapper.selectByIds(coaches);
        List<List<String>> excelData = new ArrayList<>();
        excelData.add(List.of(
                "姓名", "英文名", "性别", "所属学校", "电话",
                "微信", "邮箱", "联系地址", "T-shirt尺寸"
        ));
        users.forEach(user ->
                excelData.add(List.of(
                        safe(user.getFirstName() + user.getLastName()),
                        safe(user.getFirstNameEn() + user.getLastNameEn()),
                        Objects.isNull(user.getSex()) ? "" : (user.getSex() ? "女" : "男"),
                        safe(schoolMapper.selectOne(new LambdaQueryWrapper<School>()
                                .eq(School::getId, user.getSchoolId())).getName()),
                        safe(user.getPhone()),
                        safe(user.getWxid()),
                        safe(user.getEmail()),
                        safe(user.getAddress()),
                        safe(user.getClothSize())
                ))
        );

        String fileName = getFileName(competitionId, "参赛教练");
        try {
            exportExcel(response, fileName, excelData);
        } catch (Exception e) {
            return Response.error(ErrorCode.DOWNLOAD_FAILED.getErrCode(),
                    ErrorCode.DOWNLOAD_FAILED.getErrDesc());
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response exportTeam(String competitionId, HttpServletResponse response) {

        List<TeamExportVo> teams = teamMapper.getIdByCompetitionId(competitionId);
        List<List<String>> excelData = new ArrayList<>();
        excelData.add(List.of(
                "中文队名", "英文队名", "所属学校",
                "队员1姓名", "队员1姓名拼音", "队员2姓名", "队员2姓名拼音", "队员3姓名", "队员3姓名拼音",
                "占用名额类型", "是否女队", "证书教练姓名", "证书教练姓名拼音","证书教练联系方式"
        ));
        teams.forEach(team ->
                excelData.add(List.of(
                        safe(team.getName()),
                        safe(team.getNameEn()),
                        safe(team.getSchoolName()),
                        safe(team.getMemberName1()),
                        safe(team.getMemberNameEn1()),
                        safe(team.getMemberName2()),
                        safe(team.getMemberNameEn2()),
                        safe(team.getMemberName3()),
                        safe(team.getMemberNameEn3()),
                        safe(team.getOccupationQuotaType()),
                        safe(team.getIsFemaleTeam()),
                        safe(team.getPrizeCoachName()),
                        safe(team.getPrizeCoachNameEn()),
                        safe(team.getPrizeCoachPhone())
                ))
        );

        String fileName = getFileName(competitionId, "参赛队伍");
        exportExcel(response, fileName, excelData);
        return Response.success();
    }

    private String safe(Object value) {
        return Objects.isNull(value) ? "" : value.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response exportTeamMembers(String competitionId, HttpServletResponse response) {
        LambdaQueryWrapper<Signup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Signup::getCompetitionId, competitionId);
        List<Signup> signups = signupMapper.selectList(queryWrapper);
        List<SignupMember> signupMembers = new ArrayList<>();
        for (Signup signup : signups) {
            LambdaQueryWrapper<SignupMember> queryWrapper1 = new LambdaQueryWrapper<>();
            String signupId = signup.getId();
            queryWrapper1.eq(SignupMember::getSignupId, signupId);
            List<SignupMember> signupMembers1 = signupMemberMapper.selectList(queryWrapper1);
            signupMembers.addAll(signupMembers1);
        }
        LambdaQueryWrapper<User> queryWrapper2 = new LambdaQueryWrapper<>();
        List<String> MemberIds = signupMembers.stream().map(SignupMember::getMemberId).toList();
        queryWrapper2.in(User::getId, MemberIds);
        List<User> users = userMapper.selectList(queryWrapper2);
        List<List<String>> excelData = new ArrayList<>();
        excelData.add(List.of(
                "姓名", "性别", "所属学校", "电话",
                "邮箱", "学号", "T-shirt尺寸"
        ));
        try {
            for (User user : users) {
                String schoolName = schoolMapper.selectById(user.getSchoolId()).getName();
                if(schoolName==null)
                    schoolName="未找到学校";
                excelData.add(List.of(
                        safe(user.getFirstName()) + safe(user.getLastName()),
                        user.getSex() == null ? " " : (user.getSex() ? "女" : "男"),
                        safe(schoolName),
                        safe(user.getPhone()),
                        safe(user.getEmail()),
                        safe(user.getStudentNumber()),
                        safe(user.getClothSize())
                ));
            }
        } catch (Exception e) {
            return Response.error(ErrorCode.DOWNLOAD_FAILED.getErrCode(),
                    ErrorCode.DOWNLOAD_FAILED.getErrDesc());
        }
        exportExcel(response, "参赛人员名单.xlsx", excelData);
        return Response.success();
    }

    @Override
    public Response exportInvoiceInfo(String competitionId, HttpServletResponse response) {
        List<SchoolInvoiceInfoVO> schoolInvoiceInfoVOS = schoolMapper.selectInvoiceInfoByCompetitionId(competitionId);
        if (schoolInvoiceInfoVOS == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(),
                    ErrorCode.DATA_NOT_EXIST.getErrDesc());
        }
        List<List<String>> excelData = new ArrayList<>();
        excelData.add(List.of(
                "邮箱", "中文校名", "英文校名", "教练中文名", "教练英文名",
                "单位名称", "纳税人识别号", "邮寄地址", "邮寄收件人电话", "邮寄收件人", "银行账号", "银行名称"
        ));

        schoolInvoiceInfoVOS.forEach(infoVO ->
                excelData.add(List.of(
                        safe(infoVO.getEmail()),
                        safe(infoVO.getSchoolName()),
                        safe(infoVO.getSchoolNameEn()),
                        safe(infoVO.getCoachName()),
                        safe(infoVO.getCoachNameEn()),
                        safe(infoVO.getInstitution()),
                        safe(infoVO.getTaxpayerCode()),
                        safe(infoVO.getMailingAddress()),
                        safe(infoVO.getMailingAcceptorPhone()),
                        safe(infoVO.getMailingAcceptorName()),
                        safe(infoVO.getBankCardCode()),
                        safe(infoVO.getBankName())
                ))
        );
        String fileName = getFileName(competitionId, "发票信息");
        exportExcel(response, fileName, excelData);
        return Response.success();
    }

    private void exportExcel(HttpServletResponse response, String fileName, List<List<String>> data) {
        try {
            byte[] bytes = ExcelUtil.generateExcel(data);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setContentType("application/octet-stream;character=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentLength(bytes.length);

            // 禁止缓存
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                outputStream.write(bytes);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new BizException(ErrorCode.DOWNLOAD_FAILED.getErrCode(), "下载文件失败");
        }

    }

    private String getFileName(String competitionId, String content) {
        // 先查询比赛信息，获取比赛名称
        Competition competition = competitionMapper.selectById(competitionId);

        long timestamp = System.currentTimeMillis();

        // 1. 将时间戳转换为格式化字符串
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Shanghai"));

        // 定义文件名友好的格式（避免冒号等特殊字符）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timeString = zonedDateTime.format(formatter);
        // 2. 拼接其他字符串生成文件名
        String prefix = content + "-" + competition.getSeason() + "年" + competition.getName();
        String extension = ".xlsx";
        String fileName = String.format("%s-%s%s", prefix, timeString, extension);

        return fileName;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response getAllCompetition(int pageNow, int pageSize, String keyword, String type, String season) {

        // todo 加season过滤条件

        Page<Competition> page = new Page<>(pageNow, pageSize);
        LambdaQueryWrapper<Competition> queryWrapper = new LambdaQueryWrapper<>();

        //按比赛类型筛选
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Competition::getType, type);
        }

        //按关键词筛选
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Competition::getName, keyword);
        }

        if (season != null) {
            queryWrapper.eq(Competition::getSeason, season);
        }

        Page<Competition> competitionPage = competitionMapper.selectPage(page, queryWrapper);
        List<Competition> records = competitionPage.getRecords();

        List<CompetitionGetInfoResponse> ans = new ArrayList<>();
        for (Competition competition : records) {
            CompetitionGetInfoResponse competitionGetInfoResponse = new CompetitionGetInfoResponse();
            BeanUtils.copyProperties(competition, competitionGetInfoResponse);
            String schoolId = competition.getSchoolId();
            if (schoolId != null && !schoolId.isEmpty()) {
                School school = schoolMapper.selectById(schoolId);
                if (school != null) {
                    competitionGetInfoResponse.setSchool(school.getName());
                }
            }
            String competitionAdminId = competition.getCompetitionAdminId();
            if (competitionAdminId != null && !competitionAdminId.isEmpty()) {
                User user = userMapper.selectById(competitionAdminId);
                if (user != null) {
                    competitionGetInfoResponse.setCompetitionAdminFirstName(user.getFirstName());
                    competitionGetInfoResponse.setCompetitionAdminLastName(user.getLastName());
                }
            }
            ans.add(competitionGetInfoResponse);
        }


        CompetitionListResponse competitionListResponse = new CompetitionListResponse();
        competitionListResponse.setTotalCount(ans.size());
        competitionListResponse.setCompetitions(ans);

        return Response.of(competitionListResponse);
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {

        String fileName = "导入模板.xlsx";
        // 用 classpath 方式读取资源文件（无论本地跑还是打包后都能用）
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("public/quotationTemplate.xlsx")) {
            if (inputStream == null) {
                throw new FileNotFoundException("模板文件未找到！");
            }

            // URL 编码文件名，防止中文乱码
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            // 设置响应头
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // 把模板内容写到响应里
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192]; // 8KB 缓冲区
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response uploadExcel(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
            List<List<String>> dataList = parseSheetToList(sheet);
            List<AdminUploadQuotationExcelResponse> ans = new ArrayList<>();
            if (dataList.size() > 1) {
                for (int i = 1; i < dataList.size(); i++) {
                    AdminUploadQuotationExcelResponse adminUploadQuotationExcelResponse = new AdminUploadQuotationExcelResponse();
                    List<String> rowData = dataList.get(i);
                    String school = rowData.get(0);
                    String quotaSimple = rowData.get(1);
                    String quotaGirl = rowData.get(2);
                    String quotaAddition = rowData.get(3);
                    String quotaUnofficial = rowData.get(4);
                    adminUploadQuotationExcelResponse.setQuotaAddition(quotaAddition);
                    adminUploadQuotationExcelResponse.setQuotaGirl(quotaGirl);
                    adminUploadQuotationExcelResponse.setQuotaSimple(quotaSimple);
                    adminUploadQuotationExcelResponse.setQuotaUnofficial(quotaUnofficial);
                    adminUploadQuotationExcelResponse.setSchoolName(school);
                    LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(School::getName, school);
                    School school1 = schoolMapper.selectOne(queryWrapper);
                    if (school1 == null) {
                        adminUploadQuotationExcelResponse.setMessage("学校不存在");
                        adminUploadQuotationExcelResponse.setIsSuccess(0);
                    } else {
                        adminUploadQuotationExcelResponse.setIsSuccess(1);
                        adminUploadQuotationExcelResponse.setMessage("成功");
                    }
                    ans.add(adminUploadQuotationExcelResponse);
                }
            }
            return Response.of(ans);
        } catch (Exception e) {
            log.error("上传文件失败", e);
        }
        return Response.error();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response updateQuotationRequest(AdminUpdateQuotationRequest request) {
        List<AdminUpdateQuotationRequest.Quotation> quotations = request.getQuotations();
        for (AdminUpdateQuotationRequest.Quotation quotation : quotations) {
            QuotaDistribution quotaDistribution = new QuotaDistribution();
            BeanUtils.copyProperties(quotation, quotaDistribution);
            quotaDistribution.setQuotaGirlRest(quotation.getQuotaGirl());
            quotaDistribution.setQuotaSimpleRest(quotation.getQuotaSimple());
            quotaDistribution.setQuotaAdditionRest(quotation.getQuotaAddition());
            quotaDistribution.setQuotaUnofficialRest(quotation.getQuotaUnofficial());
            LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(School::getName, quotation.getSchoolName());
            School school = schoolMapper.selectOne(queryWrapper);
            quotaDistribution.setSchoolId(school.getId());
            quotaDistributionMapper.insert(quotaDistribution);
        }
        return Response.success();
    }

    public List<List<String>> parseSheetToList(Sheet sheet) {
        List<List<String>> result = new ArrayList<>();

        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            for (Cell cell : row) {
                rowData.add(getCellValue(cell));
            }
            result.add(rowData);
        }

        return result;
    }

    // 辅助方法：处理不同类型的单元格
    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response getCompetitionInfo(String competitionId) {
        // 检查 competitionId 是否为空
        if (competitionId == null || competitionId.isEmpty()) {
            return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }
        // 查询比赛信息
        Competition competition = competitionMapper.selectById(competitionId);
        // 检查 competition 是否存在
        if (competition == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }

        CompetitionGetSignUpInfoResponse competitionInfoRes = null;

        try {
            // 包装响应类
            competitionInfoRes = new CompetitionGetSignUpInfoResponse();
            BeanUtils.copyProperties(competition, competitionInfoRes);

            // 查询承办学校名称和承办人姓名
            competitionInfoRes.setSchoolName(schoolMapper.selectById(competition.getSchoolId()).getName());

            User user = userMapper.selectById(competition.getCompetitionAdminId());
            competitionInfoRes.setCompetitionAdminLastName(user.getLastName());
            competitionInfoRes.setCompetitionAdminFirstName(user.getFirstName());
        } catch (BeansException e) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "获取比赛信息失败");
        }
        return Response.of(competitionInfoRes);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response getCoachTeam(String competitionId) {
        if (competitionId == null || competitionId.isEmpty()) {
            return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }
        // 获得当前教练id
        String coachId = RoleUtil.getCurrentUserId();
        List<Team> teamsNotSigned = null;

        try {
            // 根据教练和比赛id查到教练麾下参与到这场比赛的队伍
            List<Signup> signups = signupMapper.selectList(new LambdaQueryWrapper<Signup>()
                    .eq(Signup::getCompetitionId, competitionId)
                    .eq(Signup::getCoachId, coachId));
            // 提取每个队伍的teamId
            List<String> signedTeamsOfCoachId = signups.stream().map(Signup::getTeamId).toList();

            // 找到教练麾下未报名的比赛的队伍
            teamsNotSigned = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                    .notIn(Team::getId, signedTeamsOfCoachId));
        } catch (Exception e) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "队伍查询失败");
        }
        List<String> teamIds = teamsNotSigned.stream().map(Team::getId).toList();
        List<TeamVo> teamVos = new ArrayList<>();
        try {
            teamIds.forEach(teamId -> {
                //能够直接看懂的数据
                TeamVo teamVo = teamMapper.getTeamInfo(teamId);
                //队伍的抽象数据
                Team team = teamMapper.selectById(teamId);
                if (team.getCoachId() != null && !team.getCoachId().isEmpty()) {
                    teamVo.setCoachName(teamVo.getCoachFirstName() + teamVo.getCoachLastName());
                }
                if (team.getPrizeCoachId() != null && !team.getPrizeCoachId().isEmpty()) {
                    teamVo.setPrizeCoachName(teamVo.getPrizeCoachFirstName() + teamVo.getPrizeCoachLastName());
                    teamVo.setPrizeCoachNameEn(teamVo.getPrizeCoachFirstNameEn() + teamVo.getPrizeCoachLastNameEn());
                }
                teamVo.setMembers(teamMapper.getMember(teamId));
                teamVo.setTeam(team);
                teamVos.add(teamVo);
            });
        } catch (Exception e) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "队伍查询失败");
        }
        return Response.of(teamVos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response getCoachStudent(String competitionId) {
        if (competitionId == null || competitionId.isEmpty()) {
            return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }
        String coachId = RoleUtil.getCurrentUserId();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getCoachId, coachId)
                .eq(User::getStatus, 1));
        ArrayList<String> userNames = new ArrayList<>();
        users.forEach(user -> userNames.add(user.getLastName() + user.getFirstName()));
        //todo 是否需要考虑用户身份问题
        return Response.of(userNames);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response getSchoolInvoiceInfo(String competitionId) {
        if (competitionId == null || competitionId.isEmpty()) {
            return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }
        String coachId = RoleUtil.getCurrentUserId();
        List<Signup> signups = null;
        try {
            signups = signupMapper.selectList(new LambdaQueryWrapper<Signup>()
                    .eq(Signup::getCompetitionId, competitionId)
                    .eq(Signup::getSchoolId, userMapper.selectById(coachId).getSchoolId()));
        } catch (Exception e) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "获取发票信息失败");
        }

        List<SchoolInvoiceInfoResult> schoolInvoiceInfos = new ArrayList<>();
        signups.forEach(signup -> {
            SchoolInvoiceInfoResult schoolInvoiceInfo = new SchoolInvoiceInfoResult();
            BeanUtils.copyProperties(signup, schoolInvoiceInfo);
            schoolInvoiceInfos.add(schoolInvoiceInfo);
        });
        return Response.of(schoolInvoiceInfos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response getSchoolCompetitionQuotaRest(String competitionId) {
        if (competitionId == null || competitionId.isEmpty()) {
            return Response.error(ErrorCode.ILLEGAL_ARGUMENT.getErrCode(), "比赛ID不能为空");
        }
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            return Response.error(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }
        QuotaDistribution quotaDistribution = null;
        try {
            quotaDistribution = quotaDistributionMapper.selectOne(new LambdaQueryWrapper<QuotaDistribution>()
                    .eq(QuotaDistribution::getCompetitionId, competitionId)
                    .eq(QuotaDistribution::getSchoolId, userMapper.selectById(RoleUtil.getCurrentUserId()).getSchoolId()));
        } catch (Exception e) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "获取剩余配额信息失败");
        }
        try {
            QuotaRestResponse quotaRestResponse = QuotaRestResponse.builder()
                    .id(quotaDistribution.getId())
                    .competitionId(quotaDistribution.getCompetitionId())
                    .schoolId(quotaDistribution.getSchoolId())
                    .quotaSimpleRest(quotaDistribution.getQuotaSimpleRest().toString())
                    .quotaGirlRest(quotaDistribution.getQuotaGirlRest().toString())
                    .quotaAdditionRest(quotaDistribution.getQuotaAdditionRest().toString())
                    .quotaUnofficialRest(quotaDistribution.getQuotaUnofficialRest().toString())
                    .build();
            return Response.of(quotaRestResponse);
        } catch (BeansException e) {
            return Response.error(ErrorCode.QUERY_FAILED.getErrCode(), "参数存在空值");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exportClothSizeStats(String competitionId, HttpServletResponse response) {
        // 查询比赛是否存在
        Competition competition = competitionMapper.selectById(competitionId);
        if (competition == null) {
            throw new BizException(ErrorCode.DATA_NOT_EXIST.getErrCode(), "比赛不存在");
        }

        // 从signup表中获取该比赛所有报名记录
        List<Signup> signups = signupMapper.selectList(new LambdaQueryWrapper<Signup>()
                .eq(Signup::getCompetitionId, competitionId));

        // 统计各类尺寸数量
        // 教练衣服尺寸统计
        Map<String, Integer> coachSizeStats = new HashMap<>();
        // 学生衣服尺寸统计
        Map<String, Integer> studentSizeStats = new HashMap<>();

        // 处理教练衣服尺寸
        List<String> coachIds = signups.stream()
                .map(Signup::getCoachId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (!coachIds.isEmpty()) {
            List<User> coaches = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getCoachId, coachIds));

            for (User coach : coaches) {
                String clothSize = coach.getClothSize();
                if (clothSize != null && !clothSize.isEmpty()) {
                    coachSizeStats.put(clothSize, coachSizeStats.getOrDefault(clothSize, 0) + 1);
                }
            }
        }

        // 处理学生衣服尺寸
        Set<String> memberIds = new HashSet<>();
        for (Signup signup : signups) {
            List<SignupMember> members = signupMemberMapper.selectList(new LambdaQueryWrapper<SignupMember>()
                    .eq(SignupMember::getSignupId, signup.getId()));

            for (SignupMember member : members) {
                memberIds.add(member.getMemberId());
            }

        }

        if (!memberIds.isEmpty()) {
            List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .in(User::getId, new ArrayList<>(memberIds)));

            for (User student : students) {
                String clothSize = student.getClothSize();
                if (clothSize != null && !clothSize.isEmpty()) {
                    studentSizeStats.put(clothSize, studentSizeStats.getOrDefault(clothSize, 0) + 1);
                }
            }
        }

        // 准备导出数据
        List<List<String>> excelData = new ArrayList<>();
        // 添加表头
        excelData.add(List.of("类型", "教练", "学生"));

        // 合并所有尺寸类型
        Set<String> allSizes = new HashSet<>();
        allSizes.addAll(coachSizeStats.keySet());
        allSizes.addAll(studentSizeStats.keySet());

        // 按规格添加数据行
        for (String size : allSizes) {
            excelData.add(List.of(
                    size,
                    String.valueOf(coachSizeStats.getOrDefault(size, 0)),
                    String.valueOf(studentSizeStats.getOrDefault(size, 0))
            ));
        }

        // 导出Excel
        String fileName = getFileName(competitionId, "衣服尺寸统计");
        exportExcel(response, fileName, excelData);
    }


}
