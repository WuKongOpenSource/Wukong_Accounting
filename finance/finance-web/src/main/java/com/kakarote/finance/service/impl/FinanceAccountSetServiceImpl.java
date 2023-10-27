package com.kakarote.finance.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.common.Const;
import com.kakarote.core.common.enums.SystemCodeEnum;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.common.PeriodUtils;
import com.kakarote.finance.common.admin.AdminRoleTypeEnum;
import com.kakarote.finance.constant.AdjuvantTypeEnum;
import com.kakarote.finance.constant.FinanceDatabaseEnum;
import com.kakarote.finance.entity.BO.FinanceAccountAuthSaveBO;
import com.kakarote.finance.entity.BO.FinanceAccountSetBO;
import com.kakarote.finance.entity.BO.FinanceNewAccountSetBO;
import com.kakarote.finance.entity.PO.*;
import com.kakarote.finance.entity.VO.FinanceAccountListVO;
import com.kakarote.finance.entity.VO.FinanceAccountVO;
import com.kakarote.finance.mapper.FinanceAccountSetMapper;
import com.kakarote.finance.service.*;
import com.kakarote.ids.provider.utils.UserCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 账套表 服务实现类
 * </p>
 *
 * @author dsc
 * @since 2021-08-28
 */
@Service
@Slf4j
public class FinanceAccountSetServiceImpl extends BaseServiceImpl<FinanceAccountSetMapper, FinanceAccountSet> implements IFinanceAccountSetService {

    private static final String INIT_AUTH_URL = "/adminConfig/moduleInitData";

    @Autowired
    private IFinanceAccountUserService accountUserService;

    @Autowired
    private IFinanceParameterService parameterService;

    @Autowired
    private IFinanceCommonService commonService;

    @Autowired
    private IFinanceAdjuvantService adjuvantService;

    @Autowired
    private IAdminRoleService adminRoleService;

    @Autowired
    private DataSource dataSource;

    /**
     * 查询账套管理页面数据
     *
     * @return
     */
    @Override
    public List<FinanceAccountSet> queryPageList() {
        return lambdaQuery().list();
    }

    /**
     * 根据id查询账套详情
     *
     * @param accountId
     * @return
     */
    @Override
    public FinanceAccountSet getAccountSetById(Long accountId) {
        return getById(accountId);
    }

    /**
     * 账套保存与修改
     *
     * @param accountSet
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationLog saveAndUpdate(FinanceAccountSet accountSet) {
        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject(accountSet.getCompanyName());
        if (ObjectUtil.isNotEmpty(accountSet.getAccountId())) {
            accountSet.setUpdateUserId(UserUtil.getUserId());
            operationLog.setOperationInfo("编辑账套：" + accountSet.getCompanyName());
            updateById(accountSet);
        } else {
            accountSet.setCreateUserId(UserUtil.getUserId());
            operationLog.setOperationInfo("新建账套：" + accountSet.getCompanyName());
            save(accountSet);
            //将所有财务角色查询出来
            AdminRoleTypeEnum roleTypeEnum = AdminRoleTypeEnum.parse(4);
            List<AdminRole> roleList = adminRoleService.getRoleByType(roleTypeEnum);
            List<FinanceAccountUser> userList = new ArrayList<>();
            if (roleList.size() > 0) {
                for (AdminRole adminRole : roleList) {
                    FinanceAccountUser accountUser = new FinanceAccountUser();
                    accountUser.setUserId(UserUtil.getUserId());
                    accountUser.setAccountId(accountSet.getAccountId());
                    accountUser.setRoleId(adminRole.getRoleId());
                    accountUser.setIsDefault(0);
                    accountUser.setIsFounder(1);
                    userList.add(accountUser);
                }
                accountUserService.saveBatch(userList, Const.BATCH_SAVE_SIZE);
            } else {
                //如果没有角色,将当前创建人添加到授权员工中
                FinanceAccountUser accountUser = new FinanceAccountUser();
                accountUser.setUserId(UserUtil.getUserId());
                accountUser.setAccountId(accountSet.getAccountId());
                accountUser.setIsDefault(0);
                accountUser.setIsFounder(1);
                accountUserService.save(accountUser);
            }

            try {
                //调用初始币种方法
                initMethod(accountSet.getAccountId());
            } catch (SQLException | FileNotFoundException e) {
                log.error("初始化币种sql", e);
                throw new CrmException(SystemCodeEnum.SYSTEM_ERROR);
            }
            try {
                AccountSet.setAccountSet(accountSet);
                commonService.init();
            } finally {
                CashFlowStatementReportHolder.remove();
                AccountSet.remove();
            }

        }
        return operationLog;
    }

    public static final List<String> REGISTER_SQL_LIST = IoUtil.readLines(ClassLoaderUtil.getClassLoader().getResourceAsStream("init.sql"), Charset.defaultCharset(), new ArrayList<>());

    private void initMethod(Long accountId) throws SQLException, FileNotFoundException {
        Map<String, Object> formatMap = new HashMap<>(3);
        formatMap.put("userId", UserUtil.getUserId());
        formatMap.put("accountId", Convert.toLong(accountId));
        formatMap.put("createTime", LocalDateTime.now());
        Connection connection = dataSource.getConnection();
        //保存当前自动提交模式
        boolean autoCommit = connection.getAutoCommit();
        try {
            Statement statement = connection.createStatement();
            //关闭自动提交
            connection.setAutoCommit(false);
            for (String line : REGISTER_SQL_LIST) {
                if (line.startsWith("--") || StrUtil.isEmpty(line)) {
                    continue;
                }
                formatMap.put("autoId", BaseUtil.getNextId());
                String sql = StrUtil.format(line, formatMap);
                statement.addBatch(sql);
            }
            //同时提交所有的sql语句
            statement.executeBatch();
            //提交修改
            connection.commit();
        } catch (Exception e) {
            log.error("初始化币种失败", e);
            connection.rollback();
        } finally {
            connection.setAutoCommit(autoCommit);
            connection.close();
        }
    }

    /**
     * 根据账套id查询授权员工及角色
     *
     * @param accountId
     * @return
     */
    @Override
    public FinanceAccountVO getUserByAccountId(Long accountId) {
        FinanceAccountVO accountVO = new FinanceAccountVO();
        accountVO.setCompanyName(lambdaQuery().eq(FinanceAccountSet::getAccountId, accountId).one().getCompanyName());
        //1、根据账套id查询关联的员工
        List<FinanceAccountSetBO> accountSetBOS = baseMapper.getUserIdList(accountId);
        //根据员工id查询所有角色
        List<Map<String, Object>> dataMap = new ArrayList<>();
        accountSetBOS.forEach(setBO -> {
            Map<String, Object> maps = new HashMap<>(16);
            maps.put("userId", setBO.getUserId());
            maps.put("isFounder", setBO.getIsFounder());
            maps.put("realname", UserCacheUtil.getUserName(setBO.getUserId()));
            List<Long> roleList = baseMapper.getRoleIdList(accountId, setBO.getUserId());
            roleList.removeAll(Collections.singleton(null));
            if (roleList.size() > 0) {
                roleList.forEach(ro -> {
                    maps.put(Convert.toStr(ro), true);
                });
            }
            dataMap.add(maps);
        });
        accountVO.setUserList(dataMap);
        return accountVO;
    }

    /**
     * 保存账套授权员工
     *
     * @param authSaveBO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OperationLog> saveAccountAuth(FinanceAccountAuthSaveBO authSaveBO) {
        //保存账套授权员工表
        List<FinanceAccountUser> accountUserList = new ArrayList<>();
        List<FinanceAccountUser> list = accountUserService.lambdaQuery().eq(FinanceAccountUser::getAccountId, authSaveBO.getAccountId()).list();
        List<Long> userIds = list.stream().map(FinanceAccountUser::getUserId).distinct().collect(Collectors.toList());
        List<OperationLog> operationLogList = new ArrayList<>();
        FinanceAccountSet accountSet = getById(authSaveBO.getAccountId());

        userIds.remove(accountSet.getCreateUserId());

        authSaveBO.getUserList().forEach(auth -> {
            if (!userIds.contains(auth.getUserId())) {
                OperationLog operationLog = new OperationLog();
                operationLog.setOperationObject(accountSet.getCompanyName());
                operationLog.setOperationInfo("授权：" + UserCacheUtil.getUserName(auth.getUserId()));
                operationLog.setBehavior(BehaviorEnum.AUTHORIZATION);
                operationLogList.add(operationLog);
            } else {
                userIds.remove(auth.getUserId());
            }

            //查询用户是否有账套
            List<FinanceAccountUser> userList = accountUserService.lambdaQuery().eq(FinanceAccountUser::getUserId, auth.getUserId()).list();
            Integer isDefault = 0;
            if (userList.isEmpty()) {
                isDefault = 1;
            }
            //判断是否有角色
            if (!auth.getRoleIdList().isEmpty()) {
                Integer finalIsDefault = isDefault;
                auth.getRoleIdList().forEach(role -> {
                    FinanceAccountUser user = new FinanceAccountUser();
                    user.setUserId(auth.getUserId());
                    user.setAccountId(authSaveBO.getAccountId());
                    user.setRoleId(role);
                    user.setIsDefault(finalIsDefault);
                    accountUserList.add(user);
                });
            } else {
                FinanceAccountUser user = new FinanceAccountUser();
                user.setUserId(auth.getUserId());
                user.setAccountId(authSaveBO.getAccountId());
                user.setIsDefault(isDefault);
                accountUserList.add(user);
            }
        });


        for (Long userId : userIds) {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationObject(accountSet.getCompanyName());
            operationLog.setOperationInfo("取消授权：" + UserCacheUtil.getUserName(userId));
            operationLog.setBehavior(BehaviorEnum.CANCEL_AUTHORIZATION);
            operationLogList.add(operationLog);
        }

        //先删除账套员工根据账套id,删除不是创始人的
        accountUserService.lambdaUpdate()
                .eq(FinanceAccountUser::getAccountId, authSaveBO.getAccountId())
                .eq(FinanceAccountUser::getIsFounder, 0)
                .remove();
        //保存账套员工
        accountUserService.saveBatch(accountUserList, Const.BATCH_SAVE_SIZE);
        return operationLogList;
    }

    /**
     * 账套员工单条删除
     *
     * @param accountSetBO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccountUser(FinanceAccountSetBO accountSetBO) {
        //删除账套员工表根据账套id员工id公司id
        accountUserService.lambdaUpdate()
                .eq(FinanceAccountUser::getAccountId, accountSetBO.getAccountId())
                .eq(FinanceAccountUser::getUserId, accountSetBO.getUserId())
                .remove();
        //删除员工角色表
        //service.delUserRole(accountSetBO.getUserId());

    }

    /**
     * 创建账套
     *
     * @param accountSet
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAccountSet(FinanceNewAccountSetBO accountSet) {
        FinanceAccountSet setData = lambdaQuery().eq(FinanceAccountSet::getAccountId, accountSet.getAccountId()).one();
        if (ObjectUtil.isNotEmpty(setData)) {
            setData.setUpdateUserId(UserUtil.getUserId());
            setData.setStatus(1);
            setData.setBookkeeperId(accountSet.getBookkeeperId());
            setData.setCurrencyId(accountSet.getCurrencyId());
            setData.setStartTime(LocalDateTimeUtil.parse(accountSet.getStartTime(), DatePattern.NORM_DATE_PATTERN));
            //修改账套
            updateById(setData);

            //保存系统参数表
            FinanceParameter parameter = new FinanceParameter();
            parameter.setCompanyName(setData.getCompanyName());
            parameter.setCurrencyId(accountSet.getCurrencyId());
            parameter.setStartTime(LocalDateTimeUtil.parse(accountSet.getStartTime(), DatePattern.NORM_DATE_PATTERN));
            parameter.setBookkeeperId(accountSet.getBookkeeperId());
            parameter.setAccountId(setData.getAccountId());
            parameter.setLevel(4);
            parameter.setRule("4-2-2-2");
            parameterService.save(parameter);
            //保存threadlocal
            AccountSet.remove();
            AccountSet.setAccountSet(setData);
            //保存初始化辅助核算数据
            //saveAdjuvant(setData.getAccountId());
        }
    }

    private void saveAdjuvant(Long accountId) {
        //保存初始化辅助核算数据
        List<FinanceAdjuvant> adjuvantList = new ArrayList<>();
        for (AdjuvantTypeEnum typeEnum : AdjuvantTypeEnum.values()) {
            if (Arrays.asList(0, 11).contains(typeEnum.getType())) {
                continue;
            }
            FinanceAdjuvant adjuvant = new FinanceAdjuvant();
            adjuvant.setAdjuvantName(typeEnum.getRemarks());
            adjuvant.setAdjuvantType(1);
            adjuvant.setAccountId(accountId);
            adjuvant.setLabel(typeEnum.getType());
            adjuvantList.add(adjuvant);
        }
        adjuvantService.saveBatch(adjuvantList, Const.BATCH_SAVE_SIZE);
    }

    /**
     * 获取账套列表
     *
     * @return
     */
    @Override
    public List<FinanceAccountListVO> getAccountSetList() {
        //获取账套列表根据用户id
        List<FinanceAccountListVO> dataList = baseMapper.getAccountSetList(UserUtil.getUserId());
        if (UserUtil.isAdmin()) {
            //判断是否是超级管理员
            List<FinanceAccountListVO> setList = baseMapper.getAccountSetList(null);
            setList.forEach(set -> {
                //判断当前账套是否有当前员工
                boolean exists = accountUserService.lambdaQuery()
                        .eq(FinanceAccountUser::getAccountId, set.getAccountId())
                        .eq(FinanceAccountUser::getUserId, UserUtil.getUserId()).exists();
                if (!exists) {
                    List<FinanceAccountUser> userList = new ArrayList<>();
                    //将所有财务角色查询出来
                    AdminRoleTypeEnum roleTypeEnum = AdminRoleTypeEnum.parse(4);
                    List<AdminRole> roleList = adminRoleService.getRoleByType(roleTypeEnum);
                    if (roleList.size() > 0) {
                        for (int i = 0; i < roleList.size(); i++) {
                            FinanceAccountUser accountUser = new FinanceAccountUser();
                            accountUser.setUserId(UserUtil.getUserId());
                            accountUser.setAccountId(set.getAccountId());
                            accountUser.setRoleId(roleList.get(i).getRoleId());
                            accountUser.setIsDefault(0);
                            accountUser.setIsFounder(0);
                            userList.add(accountUser);
                        }
                        accountUserService.saveBatch(userList, Const.BATCH_SAVE_SIZE);
                    } else {
                        //如果没有角色,将当前创建人添加到授权员工中
                        FinanceAccountUser accountUser = new FinanceAccountUser();
                        accountUser.setUserId(UserUtil.getUserId());
                        accountUser.setAccountId(set.getAccountId());
                        accountUser.setIsDefault(0);
                        accountUser.setIsFounder(0);
                        accountUserService.save(accountUser);
                    }
                }
            });
            dataList = baseMapper.getAccountSetList(UserUtil.getUserId());
        }
        dataList.forEach(data -> {
            //根据账套id查询结账结账清单表是否有数据,有数据取最新的结账时间
            String settleTime = baseMapper.getSettleTime(data.getAccountId());
            if (ObjectUtil.isNotEmpty(settleTime)) {
                data.setStartTime(DateUtil.offsetMonth(PeriodUtils.parseDate(settleTime), 1));
            }
        });
        return dataList;
    }

    /**
     * 切换账套
     *
     * @param accountId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchAccountSet(Long accountId) {
        //修改账套员工表
        accountUserService.lambdaUpdate().set(FinanceAccountUser::getIsDefault, 0)
                .eq(FinanceAccountUser::getUserId, UserUtil.getUserId())
                .update();

        accountUserService.lambdaUpdate().set(FinanceAccountUser::getIsDefault, 1)
                .eq(FinanceAccountUser::getAccountId, accountId)
                .eq(FinanceAccountUser::getUserId, UserUtil.getUserId())
                .update();
        //删除threadlocal
        AccountSet.remove();
        //重新保存threadlocal
        AccountSet.setAccountSet(getById(accountId));
    }

    /**
     * 查询财务管理角色
     *
     * @param type
     * @return
     */
    @Override
    public List<AdminRole> getFinanceRoleByType(Integer type) {
        AdminRoleTypeEnum roleTypeEnum = AdminRoleTypeEnum.parse(type);
        return adminRoleService.getRoleByType(roleTypeEnum);
    }

    /**
     * 初始化财务数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initFinanceData() {
        if (!UserUtil.isAdmin()) {
            if (this.verifyInitAuth()) {
                throw new CrmException(SystemCodeEnum.SYSTEM_NO_AUTH);
            }
        }
        FinanceDatabaseEnum[] databaseEnum = FinanceDatabaseEnum.values();
        for (FinanceDatabaseEnum enums : databaseEnum) {
            baseMapper.removeAllData(enums.name().toLowerCase());
        }
    }



    private boolean verifyInitAuth() {
        boolean isNoAuth = false;
        Long userId = UserUtil.getUserId();
        String key = userId.toString();
        List<String> noAuthMenuUrls = BaseUtil.getRedis().get(key);
        if (noAuthMenuUrls != null && noAuthMenuUrls.contains(INIT_AUTH_URL)) {
            isNoAuth = true;
        }
        return isNoAuth;
    }


}
