package com.kakarote.finance.common;

import cn.hutool.core.collection.CollUtil;
import com.kakarote.common.entity.UserInfo;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.finance.entity.PO.FinanceAccountSet;
import com.kakarote.finance.entity.PO.FinanceAccountUser;
import com.kakarote.finance.service.IFinanceAccountSetService;
import com.kakarote.finance.service.IFinanceAccountUserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author zhangzhiwei
 * user注入切面
 */
@Aspect
@Component
@Slf4j
@Order(10)
public class AccountSetAspect {

    @Autowired
    private IFinanceAccountSetService setService;

    @Autowired
    private IFinanceAccountUserService accountUserService;


    @Around("execution(* com.kakarote.finance.controller..*.*(..)) && !execution(@(com.kakarote.core.common.ParamAspect) * *(..)) ")
    public Object before(ProceedingJoinPoint point) throws Throwable {
        try {
            UserInfo user = UserUtil.getUser();
            //查看当前登录用户是否有默认账套
            FinanceAccountUser accountUser = new FinanceAccountUser();
            List<FinanceAccountUser> accountUserList = accountUserService.lambdaQuery().eq(FinanceAccountUser::getUserId, user.getUserId()).eq(FinanceAccountUser::getIsDefault, 1).isNotNull(FinanceAccountUser::getRoleId).groupBy(FinanceAccountUser::getAccountId).list();
            if (CollUtil.isEmpty(accountUserList)) {
                //查询是否有全部账套
                List<FinanceAccountUser> userList = accountUserService.lambdaQuery().eq(FinanceAccountUser::getUserId, user.getUserId()).eq(FinanceAccountUser::getIsDefault, 0).isNotNull(FinanceAccountUser::getRoleId).list();
                if (userList.size() > 0) {
                    //取第一个
                    accountUser = userList.get(0);
                    //并设置默认
                    accountUserService.lambdaUpdate().eq(FinanceAccountUser::getId, accountUser.getId()).set(FinanceAccountUser::getIsDefault, 1).update();
                }
            } else if (accountUserList.size() > 1) {
                //默认取第一个
                accountUser = accountUserList.get(0);
                //并设置默认
                accountUserService.lambdaUpdate().ne(FinanceAccountUser::getId, accountUser.getId()).set(FinanceAccountUser::getIsDefault, 0).update();
            } else {
                //默认取第一个
                accountUser = accountUserList.get(0);
            }
            FinanceAccountSet accountSet = setService.getById(accountUser.getAccountId());
            if (accountSet == null) {
                accountSet = new FinanceAccountSet();
            }
            AccountSet.setAccountSet(accountSet);
            return point.proceed();
        } finally {
            AccountSet.remove();
        }
    }
}
