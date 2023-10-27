package com.kakarote.core.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.feign.admin.service.AdminService;
import com.kakarote.core.feign.crm.entity.BiParams;
import com.kakarote.core.servlet.ApplicationContextHolder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * @author zhangzhiwei
 */
public class BiTimeUtil {

    public static BiTimeEntity analyzeTypeOa(BiParams biParams) {
        BiTimeEntity biTimeEntity = analyzeTime(biParams);
        List<Long> userIdList = new ArrayList<>();
        Long deptId = biParams.getDeptId();
        Long userId = biParams.getUserId();
        Integer isUser = biParams.getIsUser();
        if (isUser == 0) {
            if (deptId == null) {
                deptId = Long.valueOf(0);
            }
            AdminService adminService = ApplicationContextHolder.getBean(AdminService.class);
            List<Long> deptIdList = adminService.queryChildDeptId(deptId).getData();
            deptIdList.add(deptId);
            userIdList = adminService.queryUserByDeptIds(deptIdList).getData();
        } else {
            if (userId == null) {
                userIdList = ApplicationContextHolder.getBean(AdminService.class).queryUserList(1).getData();
            } else {
                userIdList.add(userId);
            }
        }
        biTimeEntity.setUserIds(userIdList);
        return biTimeEntity;
    }

    public static BiTimeEntity analyzeType(BiParams biParams) {
        BiTimeEntity biTimeEntity;
        if (biParams.getYear() != null){
            biTimeEntity = analyzeTimeByYear(biParams);
        }else {
            biTimeEntity = analyzeTime(biParams);
        }
        biTimeEntity.setUserIds(analyzeAuth(biParams));
        return biTimeEntity;
    }

    public static BiTimeEntity analyzeTypeTime(BiParams biParams) {
        BiTimeEntity biTimeEntity;

            biTimeEntity = analyzeTimeByYear1(biParams);
        return biTimeEntity;
    }


    public static List<Long> analyzeAuth(BiParams biParams) {
        List<Long> userIdList = new ArrayList<>();
        Long deptId = biParams.getDeptId();
        Long userId = biParams.getUserId();
        Integer isUser = biParams.getIsUser();
        if (isUser == 0) {
            if (deptId == null) {
                deptId = UserUtil.getUser().getDeptId();
            }
            AdminService adminService = ApplicationContextHolder.getBean(AdminService.class);
            List<Long> deptIdList = adminService.queryChildDeptId(deptId).getData();
            deptIdList.add(deptId);
            userIdList.addAll(adminService.queryUserByDeptIds(deptIdList).getData());
        } else {
            if (userId == null) {
                userIdList.addAll(ApplicationContextHolder.getBean(AdminService.class).queryChildUserId(UserUtil.getUserId()).getData());
                userIdList.add(UserUtil.getUserId());
            } else {
                userIdList.add(userId);
            }
        }
        Long menuId = biParams.getMenuId();
        //获取数据权限范围内的userId，取交集
        dataFilter(menuId, userIdList);
        return userIdList;
    }

    /**
     * 数据权限过滤
     *
     * @param menuId     菜单ID
     * @param userIdList 用户ID
     * @return data
     */
    public static void dataFilter(Long menuId, List<Long> userIdList) {
        if (!UserUtil.isAdmin()) {
            AdminService adminService = ApplicationContextHolder.getBean(AdminService.class);
            List<Long> authUserIdList = new ArrayList<>();
            Integer dataType = adminService.queryDataType(UserUtil.getUserId(), menuId).getData();
            int adminDataTypeFive=5;
            int adminDataTypeTwo=2;
            int adminDataTypeThree=3;
            int adminDataTypeFour=4;
            if (dataType == null) {
                userIdList.clear();
            } else if (dataType != adminDataTypeFive) {
                if (dataType == 1) {
                    authUserIdList.add(UserUtil.getUserId());
                } else if (dataType == adminDataTypeTwo) {
                    authUserIdList = adminService.queryChildUserId(UserUtil.getUserId()).getData();
                    authUserIdList.add(UserUtil.getUserId());
                } else if (dataType == adminDataTypeThree) {
                    authUserIdList = adminService.queryUserByDeptIds(Collections.singletonList(UserUtil.getUser().getDeptId())).getData();
                } else if (dataType == adminDataTypeFour) {
                    List<Long> deptIdList = adminService.queryChildDeptId(UserUtil.getUser().getDeptId()).getData();
                    deptIdList.add(UserUtil.getUser().getDeptId());
                    authUserIdList = adminService.queryUserByDeptIds(deptIdList).getData();
                }
                userIdList.retainAll(authUserIdList);
            }
        }
    }

    public static BiTimeEntity analyzeTime(BiParams biParams) {
        Date beginDate = DateUtil.date();
        Date endDate = DateUtil.date();
        int cycleNum = 12;
        String sqlDateFormat = "%Y%m";
        String dateFormat = "yyyyMM";
        String type = biParams.getType();
        String startTime = biParams.getStartTime();
        String endTime = biParams.getEndTime();
        if (StrUtil.isNotEmpty(type)) {
            String regex="^[\\d]{8}$";
            if (type.matches(regex)){
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
            }
            switch (type) {
                case "year":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    break;
                case "lastYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.offsetMonth(DateUtil.date(), -12));
                    endDate = DateUtil.endOfYear(DateUtil.offsetMonth(DateUtil.date(), -12));
                    break;
                case "quarter":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.date());
                    endDate = DateUtil.endOfQuarter(DateUtil.date());
                    cycleNum = 3;
                    break;
                case "lastQuarter":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -3));
                    endDate = DateUtil.endOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -3));
                    cycleNum = 3;
                    break;
                case "month":
                    beginDate = DateUtil.beginOfMonth(DateUtil.date());
                    endDate = DateUtil.endOfMonth(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "lastMonth":
                    beginDate = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    endDate = DateUtil.endOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "week":
                    beginDate = DateUtil.beginOfWeek(DateUtil.date());
                    endDate = DateUtil.endOfWeek(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "lastWeek":
                    beginDate = DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    endDate = DateUtil.endOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "today":
                    beginDate = DateUtil.beginOfDay(DateUtil.date());
                    endDate = DateUtil.endOfDay(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "yesterday":
                    beginDate = DateUtil.beginOfDay(new Date(System.currentTimeMillis() - 86400000));
                    endDate = DateUtil.endOfDay(new Date(System.currentTimeMillis() - 86400000));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "nextYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    beginDate =  DateUtil.offset(beginDate, DateField.YEAR,1);
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    endDate = DateUtil.offset(endDate, DateField.YEAR,1);
                    break;
                case "firstHalfYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    endDate = DateUtil.offsetMonth(DateUtil.endOfYear(DateUtil.date()),-6);
                    break;
                case "nextHalfYear":
                    beginDate = DateUtil.offsetMonth(DateUtil.beginOfYear(DateUtil.date()),6);
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    break;
                case "nextQuarter":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.date().offset(DateField.MONTH,3));
                    endDate = DateUtil.endOfQuarter(DateUtil.date().offset(DateField.MONTH,3));
                    break;
                case "nextMonth":
                    beginDate = DateUtil.beginOfMonth(DateUtil.date().offset(DateField.MONTH,1));
                    endDate = DateUtil.beginOfMonth(DateUtil.date().offset(DateField.MONTH,1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "nextWeek":
                    beginDate = DateUtil.beginOfWeek(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    endDate = DateUtil.endOfWeek(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "tomorrow":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "previous7day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-7));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-1));
                    break;
                case "previous30day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-30));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-1));
                    break;
                case "future7day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    break;
                case "future30day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,30));
                    break;
                default:
                    break;
            }
        } else if (StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime)) {
            Date start;
            Date end;
            int timeLength=6;
            if (startTime.length() == timeLength) {
                start = DateUtil.parse(startTime, "yyyyMM");
                end = DateUtil.endOfMonth(DateUtil.parse(endTime, "yyyyMM"));
            } else {
                start = DateUtil.parse(startTime);
                end = DateUtil.parse(endTime);
            }
            Integer startMonth = Integer.valueOf(DateUtil.format(start, "yyyyMM"));
            Integer endMonth = Integer.valueOf(DateUtil.format(end, "yyyyMM"));
            if (startMonth.equals(endMonth)) {
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                Long diffDay = DateUtil.between(start, end, DateUnit.DAY);
                cycleNum = diffDay.intValue() + 1;
            } else {
                sqlDateFormat = "%Y%m";
                dateFormat = "yyyyMM";
                int diffYear = Integer.parseInt(endMonth.toString().substring(0, 4)) - Integer.parseInt(startMonth.toString().substring(0, 4));
                int diffMonth = endMonth % 100 - startMonth % 100 + 1;
                cycleNum = diffYear * 12 + diffMonth;
            }
            beginDate = start;
            endDate = end;
        }
        Integer beginTime = Integer.valueOf(DateUtil.format(beginDate, dateFormat));
        Integer finalTime = Integer.valueOf(DateUtil.format(endDate, dateFormat));
        Date resBeginTime= beginDate;
        List<Map<String,Object>> resDate = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            Map<String,Object> map= new HashMap<>();
            if (StrUtil.isNotEmpty(type)) {
                switch (type) {
                    case "year":
                        if(i!=1){
                            resBeginTime=getBeginTime(resBeginTime);
                        }

                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfMonth(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "lastYear":
                        if(i!=1){
                            resBeginTime=getBeginTime(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfMonth(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "quarter":
                        if(i!=1){
                            resBeginTime=getBeginTime(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfMonth(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "lastQuarter":
                        if(i!=1){
                            resBeginTime=getBeginTime(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfMonth(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "month":
                        if(i!=1){
                            resBeginTime=getBeginDay(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfDay(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "lastMonth":
                        if(i!=1){
                            resBeginTime=getBeginDay(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfDay(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "week":
                        if(i!=1){
                            resBeginTime=getBeginDay(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfDay(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "lastWeek":
                        if(i!=1){
                            resBeginTime=getBeginDay(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfDay(resBeginTime).toString());
                        resDate.add(map);
                        break;
                    case "today":
                        map.put("beginTime",beginDate);
                        map.put("endTime",endDate);
                        resDate.add(map);
                        break;
                    case "yesterday":
                        map.put("beginTime",beginDate);
                        map.put("endTime",endDate);
                        resDate.add(map);
                        break;
                    default:
                        break;
                }
            } else if (StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime)) {
                Date start;
                Date end;
                if (startTime.length() == 6) {
                    start = DateUtil.parse(startTime, "yyyyMM");
                    end = DateUtil.endOfMonth(DateUtil.parse(endTime, "yyyyMM"));
                } else {
                    start = DateUtil.parse(startTime);
                    end = DateUtil.parse(endTime);
                }
                Integer startMonth = Integer.valueOf(DateUtil.format(start, "yyyyMM"));
                Integer endMonth = Integer.valueOf(DateUtil.format(end, "yyyyMM"));

                    if (startMonth.equals(endMonth)) {
                        if(i!=1) {
                            resBeginTime=getBeginDay(resBeginTime);
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfDay(resBeginTime).toString());
                    } else {
                        if(i!=1) {
                            resBeginTime=getBeginTime(resBeginTime);
                        }else{
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(resBeginTime);
                            calendar.set(Calendar.DAY_OF_MONTH,1);
                            resBeginTime=calendar.getTime();
                        }
                        map.put("beginTime",DateUtil.formatDateTime(resBeginTime));
                        map.put("endTime",DateUtil.endOfMonth(resBeginTime).toString());
                    }

                resDate.add(map);
            }
        }
        return new BiTimeEntity(sqlDateFormat, dateFormat, beginDate, endDate, cycleNum, beginTime, finalTime, new ArrayList<>(),resDate);
    }

    private static Date getBeginTime(Date resBeginTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(resBeginTime);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private static Date getBeginDay(Date resBeginTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(resBeginTime);
        int day = calendar.get(Calendar.DATE);
        calendar.set(Calendar.DATE, day + 1);
        return calendar.getTime();
    }

    public static BiTimeEntity analyzeTimeByYear(BiParams biParams) {
        Date date = DateUtil.parse(biParams.getYear().toString(), "yyyy");
        Date beginDate = DateUtil.beginOfYear(date);
        Date endDate = DateUtil.endOfYear(date);
        int cycleNum = 12;
        String sqlDateFormat = "%Y%m";
        String dateFormat = "yyyyMM";
        String type = biParams.getType();
        String startTime = biParams.getStartTime();
        String endTime = biParams.getEndTime();
        if (StrUtil.isNotEmpty(type)) {
            String regex="^[\\d]{8}$";
            if (type.matches(regex)){
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
            }
            switch (type) {
                case "year":
                    beginDate = DateUtil.beginOfYear(date);
                    endDate = DateUtil.endOfYear(date);
                    break;
                case "lastYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.offsetMonth(date, -12));
                    endDate = DateUtil.endOfYear(DateUtil.offsetMonth(date, -12));
                    break;
                case "quarter":
                    beginDate = DateUtil.beginOfQuarter(date);
                    endDate = DateUtil.endOfQuarter(date);
                    cycleNum = 3;
                    break;
                case "lastQuarter":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.offsetMonth(date, -3));
                    endDate = DateUtil.endOfQuarter(DateUtil.offsetMonth(date, -3));
                    cycleNum = 3;
                    break;
                case "month":
                    beginDate = DateUtil.beginOfMonth(DateUtil.date());
                    endDate = DateUtil.endOfMonth(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "lastMonth":
                    beginDate = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    endDate = DateUtil.endOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "week":
                    beginDate = DateUtil.beginOfWeek(DateUtil.date());
                    endDate = DateUtil.endOfWeek(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "lastWeek":
                    beginDate = DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    endDate = DateUtil.endOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "today":
                    beginDate = DateUtil.beginOfDay(DateUtil.date());
                    endDate = DateUtil.endOfDay(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "yesterday":
                    beginDate = DateUtil.beginOfDay(new Date(System.currentTimeMillis() - 86400000));
                    endDate = DateUtil.endOfDay(new Date(System.currentTimeMillis() - 86400000));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "nextYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    beginDate =  DateUtil.offset(beginDate, DateField.YEAR,1);
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    endDate = DateUtil.offset(endDate, DateField.YEAR,1);
                    break;
                case "firstHalfYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    endDate = DateUtil.offsetMonth(DateUtil.endOfYear(DateUtil.date()),-6);
                    break;
                case "nextHalfYear":
                    beginDate = DateUtil.offsetMonth(DateUtil.beginOfYear(DateUtil.date()),6);
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    break;
                case "nextQuarter":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.date().offset(DateField.MONTH,3));
                    endDate = DateUtil.endOfQuarter(DateUtil.date().offset(DateField.MONTH,3));
                    break;
                case "nextMonth":
                    beginDate = DateUtil.beginOfMonth(DateUtil.date().offset(DateField.MONTH,1));
                    endDate = DateUtil.beginOfMonth(DateUtil.date().offset(DateField.MONTH,1));
                    break;
                case "nextWeek":
                    beginDate = DateUtil.beginOfWeek(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    endDate = DateUtil.endOfWeek(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    break;
                case "tomorrow":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    break;
                case "previous7day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-7));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-1));
                    break;
                case "previous30day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-30));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-1));
                    break;
                case "future7day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    break;
                case "future30day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,30));
                    break;
                default:
                    break;
            }
        } else if (StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime)) {
            Date start;
            Date end;
            int timeLength=6;
            if (startTime.length() == timeLength) {
                start = DateUtil.parse(startTime, "yyyyMM");
                end = DateUtil.endOfMonth(DateUtil.parse(endTime, "yyyyMM"));
            } else {
                start = DateUtil.parse(startTime);
                end = DateUtil.parse(endTime);
            }
            Integer startMonth = Integer.valueOf(DateUtil.format(start, "yyyyMM"));
            Integer endMonth = Integer.valueOf(DateUtil.format(end, "yyyyMM"));
            if (startMonth.equals(endMonth)) {
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                Long diffDay = DateUtil.between(start, end, DateUnit.DAY);
                cycleNum = diffDay.intValue() + 1;
            } else {
                sqlDateFormat = "%Y%m";
                dateFormat = "yyyyMM";
                int diffYear = Integer.valueOf(endMonth.toString().substring(0, 4)) - Integer.valueOf(startMonth.toString().substring(0, 4));
                int diffMonth = endMonth % 100 - startMonth % 100 + 1;
                cycleNum = diffYear * 12 + diffMonth;
            }
            beginDate = start;
            endDate = end;
        }
        Integer beginTime = Integer.valueOf(DateUtil.format(beginDate, dateFormat));
        Integer finalTime = Integer.valueOf(DateUtil.format(endDate, dateFormat));
        return new BiTimeEntity(sqlDateFormat, dateFormat, beginDate, endDate, cycleNum, beginTime, finalTime, new ArrayList<>(),null);
    }
    public static BiTimeEntity analyzeTimeByYear1(BiParams biParams) {
        Date date = DateUtil.date();
        Date beginDate = DateUtil.date();
        Date endDate = DateUtil.date();
        int cycleNum = 12;
        String sqlDateFormat = "%Y%m";
        String dateFormat = "yyyyMM";
        String type = biParams.getType();
        String startTime = biParams.getStartTime();
        String endTime = biParams.getEndTime();
        if (StrUtil.isNotEmpty(type)) {
            String regex="^[\\d]{8}$";
            if (type.matches(regex)){
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
            }
            switch (type) {
                case "year":
                    beginDate = DateUtil.beginOfYear(date);
                    endDate = DateUtil.endOfYear(date);
                    break;
                case "lastYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.offsetMonth(date, -12));
                    endDate = DateUtil.endOfYear(DateUtil.offsetMonth(date, -12));
                    break;
                case "quarter":
                    beginDate = DateUtil.beginOfQuarter(date);
                    endDate = DateUtil.endOfQuarter(date);
                    cycleNum = 3;
                    break;
                case "lastQuarter":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.offsetMonth(date, -3));
                    endDate = DateUtil.endOfQuarter(DateUtil.offsetMonth(date, -3));
                    cycleNum = 3;
                    break;
                case "month":
                    beginDate = DateUtil.beginOfMonth(DateUtil.date());
                    endDate = DateUtil.endOfMonth(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "lastMonth":
                    beginDate = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    endDate = DateUtil.endOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
                    break;
                case "week":
                    beginDate = DateUtil.beginOfWeek(DateUtil.date());
                    endDate = DateUtil.endOfWeek(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "lastWeek":
                    beginDate = DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    endDate = DateUtil.endOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 7;
                    break;
                case "today":
                    beginDate = DateUtil.beginOfDay(DateUtil.date());
                    endDate = DateUtil.endOfDay(DateUtil.date());
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "yesterday":
                    beginDate = DateUtil.beginOfDay(new Date(System.currentTimeMillis() - 86400000));
                    endDate = DateUtil.endOfDay(new Date(System.currentTimeMillis() - 86400000));
                    sqlDateFormat = "%Y%m%d";
                    dateFormat = "yyyyMMdd";
                    cycleNum = 1;
                    break;
                case "nextYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    beginDate =  DateUtil.offset(beginDate, DateField.YEAR,1);
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    endDate = DateUtil.offset(endDate, DateField.YEAR,1);
                    break;
                case "firstHalfYear":
                    beginDate = DateUtil.beginOfYear(DateUtil.date());
                    endDate = DateUtil.offsetMonth(DateUtil.endOfYear(DateUtil.date()),-6);
                    break;
                case "nextHalfYear":
                    beginDate = DateUtil.offsetMonth(DateUtil.beginOfYear(DateUtil.date()),6);
                    endDate = DateUtil.endOfYear(DateUtil.date());
                    break;
                case "nextQuarter":
                    beginDate = DateUtil.beginOfQuarter(DateUtil.date().offset(DateField.MONTH,3));
                    endDate = DateUtil.endOfQuarter(DateUtil.date().offset(DateField.MONTH,3));
                    break;
                case "nextMonth":
                    beginDate = DateUtil.beginOfMonth(DateUtil.date().offset(DateField.MONTH,1));
                    endDate = DateUtil.beginOfMonth(DateUtil.date().offset(DateField.MONTH,1));
                    break;
                case "nextWeek":
                    beginDate = DateUtil.beginOfWeek(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    endDate = DateUtil.endOfWeek(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    break;
                case "tomorrow":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    break;
                case "previous7day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-7));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-1));
                    break;
                case "previous30day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-30));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,-1));
                    break;
                case "future7day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,7));
                    break;
                case "future30day":
                    beginDate = DateUtil.beginOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,1));
                    endDate = DateUtil.endOfDay(DateUtil.date().offset(DateField.DAY_OF_YEAR,30));
                    break;
                default:
                    break;
            }
        } else if (StrUtil.isNotEmpty(startTime) && StrUtil.isNotEmpty(endTime)) {
            Date start;
            Date end;
            int timeLength=6;
            if (startTime.length() == timeLength) {
                start = DateUtil.parse(startTime, "yyyyMM");
                end = DateUtil.endOfMonth(DateUtil.parse(endTime, "yyyyMM"));
            } else {
                start = DateUtil.parse(startTime);
                end = DateUtil.parse(endTime);
            }
            Integer startMonth = Integer.valueOf(DateUtil.format(start, "yyyyMM"));
            Integer endMonth = Integer.valueOf(DateUtil.format(end, "yyyyMM"));
                sqlDateFormat = "%Y%m";
                dateFormat = "yyyyMM";
                int diffYear = Integer.valueOf(endMonth.toString().substring(0, 4)) - Integer.valueOf(startMonth.toString().substring(0, 4));
                int diffMonth = endMonth % 100 - startMonth % 100 + 1;
                cycleNum = diffYear * 12 + diffMonth;
            beginDate = start;
            endDate = end;
        }
        Integer beginTime = Integer.valueOf(DateUtil.format(beginDate, dateFormat));
        Integer finalTime = Integer.valueOf(DateUtil.format(endDate, dateFormat));
        return new BiTimeEntity(sqlDateFormat, dateFormat, beginDate, endDate, cycleNum, beginTime, finalTime, new ArrayList<>(),null);
    }


    public static Integer estimateTime(Integer beginTime) {
        if (beginTime < 1000000 && beginTime % 100 == 12) {
            beginTime = beginTime + 89;
        } else if (beginTime > 1000000) {
            int month = DateUtil.month(DateUtil.parse(beginTime.toString(), "yyyyMMdd")) + 1;
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10) {
                if (beginTime % 100 == 31) {
                    beginTime = beginTime + 70;
                } else {
                    beginTime++;
                }
            } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                if (beginTime % 100 == 30) {
                    beginTime = beginTime + 71;
                } else {
                    beginTime++;
                }
            } else if (month == 2) {
                int year = DateUtil.year(DateUtil.parse(beginTime.toString(), "yyyyMMdd"));
                if (DateUtil.isLeapYear(year) && beginTime % 100 == 29) {
                    beginTime = beginTime + 72;
                } else if (!DateUtil.isLeapYear(year) && beginTime % 100 == 28) {
                    beginTime = beginTime + 73;
                } else {
                    beginTime++;
                }
            } else if (month == 12) {
                if (beginTime % 100 == 31) {
                    beginTime = beginTime + 8870;
                } else {
                    beginTime++;
                }
            }
        } else {
            beginTime++;
        }
        return beginTime;
    }

    public static Integer analyzeType(String type) {
        int status;
        if (type == null) {
            return 11;
        }
        switch (type) {
            case "year":
                status = 9;
                break;
            case "lastYear":
                status = 10;
                break;
            case "quarter":
                status = 7;
                break;
            case "lastQuarter":
                status = 8;
                break;
            case "month":
                status = 5;
                break;
            case "lastMonth":
                status = 6;
                break;
            case "week":
                status = 3;
                break;
            case "lastWeek":
                status = 4;
                break;
            case "today":
                status = 1;
                break;
            case "yesterday":
                status = 2;
                break;
            default:
                status = 11;
                break;
        }
        return status;
    }
    /**
     * 计算与上次的比较
     *
     * @param record 类型
     */
    public static BiTimeEntity prevAnalyzeType(BiParams record) {
        Date beginDate = DateUtil.date();
        Date endDate = DateUtil.date();
        int cycleNum = 12;
        String sqlDateFormat = "%Y%m";
        String dateFormat = "yyyyMM";
        Long deptId = record.getDeptId();
        Long userId = record.getUserId();
        String type = record.getType();
        if (StrUtil.isNotEmpty(type)) {
            if ("year".equals(type)) {
                beginDate = DateUtil.beginOfYear(DateUtil.offsetMonth(DateUtil.date(), -12));
                endDate = DateUtil.endOfYear(DateUtil.offsetMonth(DateUtil.date(), -12));
            } else if ("lastYear".equals(type)) {
                beginDate = DateUtil.beginOfYear(DateUtil.offsetMonth(DateUtil.date(), -24));
                endDate = DateUtil.endOfYear(DateUtil.offsetMonth(DateUtil.date(), -24));
            } else if ("quarter".equals(type)) {
                beginDate = DateUtil.beginOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -3));
                endDate = DateUtil.endOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -3));
                cycleNum = 3;
            } else if ("lastQuarter".equals(type)) {
                beginDate = DateUtil.beginOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -6));
                endDate = DateUtil.endOfQuarter(DateUtil.offsetMonth(DateUtil.date(), -6));
                cycleNum = 3;
            } else if ("month".equals(type)) {
                beginDate = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                endDate = DateUtil.endOfMonth(DateUtil.offsetMonth(DateUtil.date(), -1));
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
            } else if ("lastMonth".equals(type)) {
                beginDate = DateUtil.beginOfMonth(DateUtil.offsetMonth(DateUtil.date(), -2));
                endDate = DateUtil.endOfMonth(DateUtil.offsetMonth(DateUtil.date(), -2));
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = (int) DateUtil.between(beginDate, endDate, DateUnit.DAY) + 1;
            } else if ("week".equals(type)) {
                beginDate = DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                endDate = DateUtil.endOfWeek(DateUtil.offsetWeek(DateUtil.date(), -1));
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 7;
            } else if ("lastWeek".equals(type)) {
                beginDate = DateUtil.beginOfWeek(DateUtil.offsetWeek(DateUtil.date(), -2));
                endDate = DateUtil.endOfWeek(DateUtil.offsetWeek(DateUtil.date(), -2));
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 7;
            } else if ("today".equals(type)) {
                beginDate = DateUtil.offsetDay(DateUtil.date(), -1);
                endDate = DateUtil.offsetDay(DateUtil.date(), -1);
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 1;
            } else if ("yesterday".equals(type)) {
                beginDate = DateUtil.offsetDay(DateUtil.date(), -2);
                endDate = DateUtil.offsetDay(DateUtil.date(), -2);
                sqlDateFormat = "%Y%m%d";
                dateFormat = "yyyyMMdd";
                cycleNum = 1;
            }
        }
        Integer beginTime = Integer.valueOf(DateUtil.format(beginDate, dateFormat));
        Integer finalTime = Integer.valueOf(DateUtil.format(endDate, dateFormat));
        BiTimeEntity timeEntity = new BiTimeEntity(sqlDateFormat, dateFormat, beginDate, endDate, cycleNum, beginTime, finalTime, new ArrayList<>(),null);
        if (userId != null) {
            timeEntity.getUserIds().add(userId);
        } else if (deptId != null) {
            AdminService adminService = ApplicationContextHolder.getBean(AdminService.class);
            List<Long> data = adminService.queryChildDeptId(deptId).getData();
            timeEntity.getUserIds().addAll(adminService.queryUserByDeptIds(data).getData());
        }
        return timeEntity;
    }


    @Data
    @Accessors(chain = true)
    public static class BiTimeEntity {
        /**
         * sql日期格式化
         */
        private String sqlDateFormat;

        /**
         * 日期格式化
         */
        private String dateFormat;

        /**
         * 开始时间
         */
        private Date beginDate;

        /**
         * 结束时间
         */
        private Date endDate;

        /**
         * 周期
         */
        private Integer cycleNum;

        /**
         * 开始时间 字符串格式 如20200101
         */
        private Integer beginTime;

        /**
         * 结束时间 字符串格式 如20200101
         */
        private Integer finalTime;

        /**
         * user列表
         */
        private List<Long> userIds = new ArrayList<>();


        private Long companyId;

        private List<Map<String,Object>> resDate = new ArrayList<>();

        public BiTimeEntity(String sqlDateFormat, String dateFormat, Date beginDate, Date endDate, Integer cycleNum, Integer beginTime, Integer finalTime, List<Long> userIds,List<Map<String,Object>> resDate) {
            this.sqlDateFormat = sqlDateFormat;
            this.dateFormat = dateFormat;
            this.beginDate = beginDate;
            this.endDate = endDate;
            this.cycleNum = cycleNum;
            this.beginTime = beginTime;
            this.finalTime = finalTime;
            this.userIds = userIds;
            this.resDate = resDate;
        }

        public BiTimeEntity() {
        }

        private Integer page;

        private Integer limit;

        public Map<String, Object> toMap() {
            Map<String, Object> map = BeanUtil.beanToMap(this);
            return map;
        }
    }
}
