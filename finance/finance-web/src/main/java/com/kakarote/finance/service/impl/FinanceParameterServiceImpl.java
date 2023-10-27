package com.kakarote.finance.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.common.Const;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.PeriodUtils;
import com.kakarote.finance.entity.PO.FinanceParameter;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.mapper.FinanceParameterMapper;
import com.kakarote.finance.service.IFinanceParameterService;
import com.kakarote.finance.service.IFinanceSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统参数设置 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@Service
public class FinanceParameterServiceImpl extends BaseServiceImpl<FinanceParameterMapper, FinanceParameter> implements IFinanceParameterService {

    @Autowired
    private IFinanceSubjectService subjectService;

    @Override
    public JSONObject queryParameter() {
        JSONObject json = getBaseMapper().queryParameter(AccountSet.getAccountSetId());
        return json;
    }

    @Override
    public FinanceParameter getParameter() {
        return lambdaQuery()
                .eq(FinanceParameter::getAccountId, AccountSet.getAccountSetId())
                .one();
    }

    @Override
    public OperationLog updateParameter(FinanceParameter parameter) {

        FinanceParameter financeParameter = lambdaQuery().eq(FinanceParameter::getAccountId, AccountSet.getAccountSetId()).one();
        if (financeParameter == null) {

            parameter.setAccountId(AccountSet.getAccountSetId());
            save(parameter);
        } else {
            parameter.setParameterId(financeParameter.getParameterId());
            updateById(parameter);
            //修改科目编码方法
            updateSubjectNum(parameter.getRule(), financeParameter.getRule());
        }
        OperationLog operationLog = new OperationLog();
        operationLog.setOperationInfo("编辑系统参数");
        operationLog.setOperationObject("系统参数");
        return operationLog;
    }

    /**
     * 修改科目编码
     *
     * @param newRule
     * @param oldRule
     */
    private void updateSubjectNum(String newRule, String oldRule) {
        //判断是否有修改
        if (!ObjectUtil.equal(newRule, oldRule)) {
            //分割编码规则
            //旧规则
            List<Integer> oldRuleList = Arrays.asList(oldRule.split("-")).stream().map(Integer::parseInt).collect(Collectors.toList());
            //新规则
            List<Integer> ruleList = Arrays.asList(newRule.split("-")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (oldRuleList.size() == 0 || ruleList.size() == 0) {
                return;
            }
            //定义加0位置
            int sum = 0;
            for (int i = 0; i < oldRuleList.size(); i++) {
                //计算加0位置
                sum = sum + ruleList.get(i);
                //判断新规则与旧规则是否相同
                if (!ObjectUtil.equal(oldRuleList.get(i), ruleList.get(i))) {
                    //定义第一级（如果是第一级则在第一位后面加0，如果不是第一级则在前面级别相加之后加0）
                    int finalI = i;
                    //计算需要加0的位置减去当前级别
                    int finalSum = sum - ruleList.get(i);
                    //计算需要加0的个数
                    int newNum = ruleList.get(i) - oldRuleList.get(i);
                    int j = 0;
                    //需要加的0
                    StringBuilder resZero = new StringBuilder();
                    while (j < newNum) {
                        resZero.append("0");
                        j++;
                    }
                    //将所有级别都修改
                    for (int h = i; h < ruleList.size(); h++) {
                        //查询当前等级的所有科目
                        List<FinanceSubject> updateList = subjectService.lambdaQuery().eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).eq(FinanceSubject::getGrade, h + 1).ne(FinanceSubject::getStatus, 3).list();
                        updateList.forEach(update -> {
                            //修改子集
                            if (finalI == 0) {
                                //如果是第一级则在编号第二位加0
                                StringBuilder sb = new StringBuilder(update.getNumber());
                                update.setNumber(sb.insert(1, resZero).toString());
                            } else {
                                StringBuilder sb = new StringBuilder(update.getNumber());
                                update.setNumber(sb.insert(finalSum, resZero).toString());
                            }
                        });
                        //批量修改科目表科目编码
                        if (!updateList.isEmpty()) {
                            subjectService.updateBatchById(updateList, Const.BATCH_SAVE_SIZE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getYearInitialPeriod(String period) {
        FinanceParameter financeParameter = lambdaQuery()
                .eq(FinanceParameter::getAccountId, AccountSet.getAccountSetId()).one();
        LocalDateTime startTime = financeParameter.getStartTime();
        int startYear = startTime.getYear();
        Date periodDate = DateUtil.parse(period, DatePattern.SIMPLE_MONTH_PATTERN);
        int periodYear = DateUtil.year(periodDate);
        if (startYear == periodYear) {
            return LocalDateTimeUtil.format(startTime, DatePattern.SIMPLE_MONTH_PATTERN);
        }
        if (startYear < periodYear) {
            return PeriodUtils.parsePeriod(DateUtil.beginOfYear(periodDate));
        } else {
            return null;
        }
    }

    @Override
    public String getStartPeriod() {
        FinanceParameter parameter = this.getParameter();
        if (ObjectUtil.isEmpty(parameter)) {
            return LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.SIMPLE_MONTH_PATTERN);
        }
        return LocalDateTimeUtil.format(parameter.getStartTime(), DatePattern.SIMPLE_MONTH_PATTERN);
    }
}
