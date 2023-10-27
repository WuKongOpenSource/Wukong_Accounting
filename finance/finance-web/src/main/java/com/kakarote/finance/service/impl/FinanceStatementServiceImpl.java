package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.common.enums.SystemCodeEnum;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.common.PeriodUtils;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.entity.PO.*;
import com.kakarote.finance.entity.VO.FinanceStatementVO;
import com.kakarote.finance.entity.VO.FinanceSubjectIdsVO;
import com.kakarote.finance.mapper.FinanceStatementMapper;
import com.kakarote.finance.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 结账表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
@Slf4j
@Service("statementService")
public class FinanceStatementServiceImpl extends BaseServiceImpl<FinanceStatementMapper, FinanceStatement>
        implements IFinanceStatementService, IFinanceInitService {

    @Autowired
    private IFinanceStatementSubjectService statementSubjectService;

    @Autowired
    private IFinanceStatementCertificateService financeStatementCertificateService;

    @Autowired
    private IFinanceStatementSettleService statementSettleService;

    @Autowired
    private IFinanceParameterService financeParameterService;

    @Autowired
    private IFinanceCertificateService financeCertificateService;

    @Autowired
    private IFinanceSubjectService financeSubjectService;

    @Autowired
    private IFinanceVoucherService financeVoucherService;

    @Autowired
    private IFinanceIncomeStatementReportService financeIncomeStatementService;

    @Autowired
    private IFinanceCertificateDetailService financeCertificateDetailService;

    @Autowired
    private IFinanceIncomeStatementReportService incomeStatementService;

    @Autowired
    private IFinanceBalanceSheetReportService balanceSheetService;

    @Autowired
    private IFinanceInitialService financeInitialService;

    private static final int TWO = 2;

    private static final String ZERO = "0";

    private static final String VOUCHER_ID = "voucherId";

    private static final int THREE = 3;

    private static final int FOUR = 4;

    private static final int FIVE = 5;

    @Override
    public OperationLog saveAndUpdate(FinanceStatementSaveBO statementSaveBO) {

        OperationLog operationLog = new OperationLog();

        FinanceStatement statement = BeanUtil.copyProperties(statementSaveBO, FinanceStatement.class);
        if (statement.getStatementId() == null) {
            statement.setCreateUserId(UserUtil.getUserId());
            statement.setCreateTime(LocalDateTime.now());
            statement.setAccountId(AccountSet.getAccountSetId());
            save(statement);

            operationLog.setOperationInfo("新建结账 ：" + statement.getStatementName());
            operationLog.setOperationObject(statement.getStatementName());
        } else {
            updateById(statement);

            operationLog.setOperationInfo("编辑结账 ：" + statement.getStatementName());
            operationLog.setOperationObject(statement.getStatementName());
        }
        LambdaQueryWrapper<FinanceStatementSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceStatementSubject::getStatementId, statement.getStatementId())
                .eq(FinanceStatementSubject::getAccountId, AccountSet.getAccountSetId());
        statementSubjectService.remove(wrapper);
        if (statementSaveBO.getSubjectList() != null && statementSaveBO.getSubjectList().size() > 0) {
            for (FinanceStatementSubject statementSubject : statementSaveBO.getSubjectList()) {
                statementSubject.setStatementId(statement.getStatementId());
                statementSubject.setAccountId(AccountSet.getAccountSetId());
                statementSubjectService.save(statementSubject);
            }
        }
        return operationLog;
    }

    @Override
    public FinanceStatementVO queryStatement() {
        CashFlowStatementReportHolder.remove();
        FinanceStatementVO statementVO = new FinanceStatementVO();
        List<FinanceStatementSettle> statementSettles = statementSettleService.lambdaQuery()
                .eq(FinanceStatementSettle::getAccountId, AccountSet.getAccountSetId())
                .orderByDesc(FinanceStatementSettle::getSettleTime).list();
        FinanceStatementSettle statementSettle = null;
        if (statementSettles.size() > 0) {
            statementSettle = statementSettles.get(0);
        }
        if (statementSettle != null) {
            statementVO.setSettleTime(LocalDateTimeUtil.offset(statementSettle.getSettleTime(), 1, ChronoUnit.MONTHS));
        } else {
            FinanceParameter parameter = financeParameterService.lambdaQuery()
                    .eq(FinanceParameter::getAccountId, AccountSet.getAccountSetId()).one();
            if (parameter == null) {
                statementVO.setSettleTime(LocalDateTimeUtil.now());
            } else {
                statementVO.setSettleTime(parameter.getStartTime());
            }
        }
        List<JSONObject> list = getBaseMapper().queryList(DateUtil.format(statementVO.getSettleTime(), "yyyyMM")
                , AccountSet.getAccountSetId());
        for (JSONObject jsonObject : list) {
            //添加多语言
            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("statementName_resourceKey", "finance.statement." + jsonObject.getString("statementName"));
            jsonObject.put("languageKeyMap", keyMap);
            List<JSONObject> objectList = getBaseMapper().querySubject(jsonObject.getLong("statementId"),
                    AccountSet.getAccountSetId());
            if (CollectionUtil.isNotEmpty(objectList)) {
                for (JSONObject subObj : objectList) {
                    //添加多语言
                    Map<String, String> subMap = new HashMap<>();
                    subMap.put("subjectName_resourceKey", "finance.subject." + subObj.getString("subjectName"));
                    subMap.put("digestContent_resourceKey", "finance.statement.subject." + subObj.getString("digestContent"));
                    subObj.put("languageKeyMap", subMap);
                }
            }
            jsonObject.put("subject", objectList);
            List<JSONObject> certificates = getBaseMapper().queryCertificate(
                    jsonObject.getLong("statementId"),
                    DateUtil.format(statementVO.getSettleTime(), "yyyyMM"),
                    AccountSet.getAccountSetId());
            jsonObject.put("certificates", certificates);
            if (jsonObject.getInteger("statementType") == 1) {
                if (certificates.size() > 0) {
                    JSONObject json = getBaseMapper().queryBalance(jsonObject.getLong("statementId"),
                            DateUtil.format(statementVO.getSettleTime(), "yyyyMM"),
                            AccountSet.getAccountSetId());
                    jsonObject.put("balance", json.getBigDecimal("balance"));
                } else {
                    FinanceStatement statement = BeanUtil.toBean(jsonObject, FinanceStatement.class);
                    String format = DateUtil.format(statementVO.getSettleTime(), "yyyyMMdd");
                    BigDecimal balance = commonStatement(statement, format, 0);
                    jsonObject.put("balance", balance);
                }
            } else if (jsonObject.getInteger("statementType") == 2) {
                if (certificates.size() == 0) {
                    FinanceStatement statement = BeanUtil.toBean(jsonObject, FinanceStatement.class);
                    String format = DateUtil.format(statementVO.getSettleTime(), "yyyyMMdd");
                    BigDecimal balance = incomeStatement(statement, format, 0);
                    jsonObject.put("balance", balance);
                } else {
                    String format = DateUtil.format(statementVO.getSettleTime(), "yyyyMM");
                    JSONObject json = getBaseMapper().queryProfitBalance(jsonObject.getLong("statementId"), format,
                            AccountSet.getAccountSetId());
                    jsonObject.put("balance", json.getBigDecimal("balance"));
                }
            } else if (jsonObject.getInteger("statementType") == 3) {
                if (certificates.size() == 0) {
                    FinanceStatement statement = BeanUtil.toBean(jsonObject, FinanceStatement.class);
                    String format = DateUtil.format(statementVO.getSettleTime(), "yyyyMMdd");
                    BigDecimal balance = appreciationStatement(statement, format, 0);
                    jsonObject.put("balance", balance);
                } else {
                    JSONObject json = getBaseMapper().queryBalance(jsonObject.getLong("statementId"),
                            DateUtil.format(statementVO.getSettleTime(), "yyyyMM"),
                            AccountSet.getAccountSetId());
                    jsonObject.put("balance", json.getBigDecimal("balance"));
                }
            } else if (jsonObject.getInteger("statementType") == 4) {
                if (certificates.size() == 0) {
                    FinanceStatement statement = BeanUtil.toBean(jsonObject, FinanceStatement.class);
                    String format = DateUtil.format(statementVO.getSettleTime(), "yyyyMMdd");

                    BigDecimal balance = touchStatement(statement, format, 0);
                    jsonObject.put("balance", balance);
                } else {
                    JSONObject json = getBaseMapper().queryBalance(jsonObject.getLong("statementId"),
                            DateUtil.format(statementVO.getSettleTime(), "yyyyMM"),
                            AccountSet.getAccountSetId());
                    jsonObject.put("balance", json.getBigDecimal("balance"));
                }
            } else {
                if (certificates.size() == 0) {
                    FinanceStatement statement = BeanUtil.toBean(jsonObject, FinanceStatement.class);
                    String format = DateUtil.format(statementVO.getSettleTime(), "yyyyMMdd");
                    BigDecimal balance = gainsStatement(statement, format, 0);
                    jsonObject.put("balance", balance);
                } else {
                    JSONObject json = getBaseMapper().queryBalance(jsonObject.getLong("statementId"),
                            DateUtil.format(statementVO.getSettleTime(), "yyyyMM"),
                            AccountSet.getAccountSetId());
                    jsonObject.put("balance", json.getBigDecimal("balance"));
                }
            }

        }
        CashFlowStatementReportHolder.removeDetailBalanceSheet();
        statementVO.setStatements(list);
        statementVO.setAccountId(AccountSet.getAccountSetId());
        Integer certificateCount = getBaseMapper().queryByTime(statementVO);
        statementVO.setNumber(certificateCount);
        return statementVO;
    }

    @Override
    public List<OperationLog> statementCertificate(FinanceStatementCertificateBO statementCertificateBO) {
        statementCertificateBO.setAccountId(AccountSet.getAccountSetId());
        List<FinanceStatement> financeStatements = lambdaQuery()
                .eq(FinanceStatement::getAccountId, AccountSet.getAccountSetId())
                .in(FinanceStatement::getStatementId, statementCertificateBO.getStatementIds())
                .list();
        int six = 6;
        if (statementCertificateBO.getCertificateTime().length() == six) {
            Date start = DateUtil.parse(statementCertificateBO.getCertificateTime(), "yyyyMM");
            String format = DateUtil.format(DateUtil.endOfMonth(start), "yyyyMMdd");
            statementCertificateBO.setCertificateTime(format);
        } else {
            Date start = DateUtil.parse(statementCertificateBO.getCertificateTime());
            String format = DateUtil.format(DateUtil.endOfMonth(start), "yyyyMMdd");
            statementCertificateBO.setCertificateTime(format);
        }

        List<OperationLog> operationLogList = new ArrayList<>();

        for (FinanceStatement statement : financeStatements) {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationInfo("生成凭证");
            operationLog.setOperationObject(statement.getStatementName());
            operationLogList.add(operationLog);
            if (statement.getStatementType() == 1) {
                commonStatement(statement, statementCertificateBO.getCertificateTime(), 1);
            } else if (statement.getStatementType() == 2) {
                statementCertificateBO.setAccountId(AccountSet.getAccountSetId());
                List<JSONObject> list = getBaseMapper().queryListByIds(statementCertificateBO);
                if (!list.isEmpty()) {
                    incomeStatement(statement, statementCertificateBO.getCertificateTime(), 1);
                }
            } else if (statement.getStatementType() == 3) {
                appreciationStatement(statement, statementCertificateBO.getCertificateTime(), 1);
            } else if (statement.getStatementType() == 4) {
                touchStatement(statement, statementCertificateBO.getCertificateTime(), 1);
            } else if (statement.getStatementType() == 5) {
                gainsStatement(statement, statementCertificateBO.getCertificateTime(), 1);
            }
        }
        CashFlowStatementReportHolder.removeDetailBalanceSheet();
        return operationLogList;
    }

    @Override
    public void statement(FinanceStatementCertificateBO bo) {
        if (bo.getType() == 1) {
            JSONObject paramnter = financeParameterService.queryParameter();
            String voucherExamine = "voucherExamine";
            if (paramnter != null && paramnter.getInteger(voucherExamine) == 1) {
                Integer count = getBaseMapper().queryCertificateCheckStatusCount(bo.getCertificateTime(), AccountSet.getAccountSetId());
                if (count > 0) {
                    throw new CrmException(FinanceCodeEnum.FINANCE_CERTIFICATE_STATUS_ERROR);
                }
            }
            bo.setAccountId(AccountSet.getAccountSetId());
            List<JSONObject> list = getBaseMapper().queryListByIds(bo);
            if (list.size() > 0) {
                Integer count = getBaseMapper().queryProfitSubjectCount(bo.getCertificateTime(), AccountSet.getAccountSetId());
                if (count == 0) {
                    throw new CrmException(FinanceCodeEnum.FINANCE_STATEMENT_ERROR);
                }
            }
            JSONObject json = financeInitialService.queryTrialBalance();
            String trialResult = "trialResult";
            if (json.getBigDecimal(trialResult).compareTo(BigDecimal.ZERO) == 0) {
                throw new CrmException(FinanceCodeEnum.FINANCE_STATEMENT_INITIAL_NOT_BALANCE);
            }
            json = baseMapper.queryIsPayments(bo.getCertificateTime(), AccountSet.getAccountSetId());
            String debtorBalance = "debtorBalance";
            String creditBalance = "creditBalance";
            if (json.getBigDecimal(debtorBalance).compareTo(json.getBigDecimal(creditBalance)) != 0) {
                throw new CrmException(FinanceCodeEnum.FINANCE_STATEMENT_NOT_BALANCE);
            }
            List<FinanceVoucher> vouchers = financeVoucherService.queryList();
            for (FinanceVoucher voucher : vouchers) {
                List<Integer> certificateNums = baseMapper.queryCertificateNumsByVoucherId(bo.getCertificateTime(), AccountSet.getAccountSetId(), voucher.getVoucherId());
                if (certificateNums.size() > 0 && certificateNums.size() != certificateNums.get(certificateNums.size() - 1)) {
                    throw new CrmException(FinanceCodeEnum.FINANCE_STATEMENT_IS_FRACTURE);
                }
            }
            FinanceReportRequestBO requestBO = new FinanceReportRequestBO();
            requestBO.setAccountId(AccountSet.getAccountSetId());
            requestBO.setType(1);
            requestBO.setFromPeriod(DateUtil.format(DateUtil.parse(bo.getCertificateTime()), "yyyyMM"));
            requestBO.setToPeriod(DateUtil.format(DateUtil.parse(bo.getCertificateTime()), "yyyyMM"));
            JSONObject jsonObject = incomeStatementService.balanceCheck(requestBO);
            String balanced = "balanced";
            if (!jsonObject.getBoolean(balanced)) {
                throw new CrmException(FinanceCodeEnum.FINANCE_PROFIT_NOT_BALANCE);
            }
            jsonObject = balanceSheetService.balanceCheck(requestBO);
            if (!jsonObject.getBoolean(balanced)) {
                throw new CrmException(FinanceCodeEnum.FINANCE_SHEET_NOT_BALANCE);
            }
            FinanceStatementSettle settle = new FinanceStatementSettle();
            settle.setSettleTime(LocalDateTimeUtil.parse(bo.getCertificateTime(), DatePattern.NORM_DATE_PATTERN));
            settle.setCreateUserId(UserUtil.getUserId());
            settle.setCreateTime(LocalDateTime.now());
            settle.setAccountId(AccountSet.getAccountSetId());
            statementSettleService.save(settle);
            // 查询利润表
//            FinanceReportRequestBO incomeStatementRequest = new FinanceReportRequestBO();
//            List<JSONObject> s = financeIncomeStatementService.report(incomeStatementRequest);
        } else {
            List<FinanceStatementSettle> statementSettles = statementSettleService.lambdaQuery()
                    .eq(FinanceStatementSettle::getAccountId, AccountSet.getAccountSetId())
                    .orderByDesc(FinanceStatementSettle::getSettleTime).list();
            if (statementSettles.size() > 0) {
                statementSettleService.removeById(statementSettles.get(0).getSettleId());
            }
        }
    }

    /**
     * 处理普通结账
     */
    private BigDecimal commonStatement(FinanceStatement statement, String certificateTime, Integer type) {
        BigDecimal debtorBalance = new BigDecimal("0");
        BigDecimal creditBalance = new BigDecimal("0");
        BigDecimal returnBalance = new BigDecimal("0");
        if (statement.getIsEndOver() == 1 && statement.getSubjectId() != null) {
            BigDecimal balance1 = new BigDecimal("0");

            FinanceCertificateBO certificateBO = new FinanceCertificateBO();
            if (type == 1) {
                String format = DateUtil.format(DateUtil.parse(certificateTime), "yyyyMM");
                List<JSONObject> certificates = getBaseMapper().queryCertificate(
                        statement.getStatementId(), format, AccountSet.getAccountSetId());
                FinanceVoucher financeVoucher = financeVoucherService.lambdaQuery()
                        .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceVoucher::getIsDefault, 1).one();
                if (certificates.size() > 0) {
                    String voucherId = "voucherId";
                    if (financeVoucher.getVoucherId().equals(certificates.get(0).getLong(voucherId))) {
                        // certificateBO.setCertificateId(certificates.get(0).getInteger("certificateId"));
                        certificateBO.setVoucherId(certificates.get(0).getLong("voucherId"));
                        certificateBO.setCertificateNum(certificates.get(0).getInteger("num"));
                    }
                    LambdaQueryWrapper<FinanceStatementCertificate> wrapper1 = new LambdaQueryWrapper<>();
                    wrapper1.eq(FinanceStatementCertificate::getCertificateId, certificates.get(0).getLong("certificateId"))
                            .eq(FinanceStatementCertificate::getStatementId, statement.getStatementId());
                    financeStatementCertificateService.remove(wrapper1);
                    financeCertificateService.removeById(certificates.get(0).getLong("certificateId"));

                    LambdaQueryWrapper<FinanceCertificateDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(FinanceCertificateDetail::getCertificateId, certificateBO.getCertificateId())
                            .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId());
                    financeCertificateDetailService.remove(wrapper);
                }
                if (financeVoucher != null && certificateBO.getVoucherId() == null) {
                    certificateBO.setVoucherId(financeVoucher.getVoucherId());
                }
            }

            JSONObject json = CashFlowStatementReportHolder.getDetailBalance(statement.getSubjectId(), certificateTime);
            if (json == null) {
                return BigDecimal.ZERO;
            }
            FinanceSubject subject = financeSubjectService.getById(statement.getSubjectId());
            if (statement.getGainRule() == 1) {
                if (statement.getTimeType() == 1) {
                    if (subject.getBalanceDirection() == 1) {
                        balance1 = json.getBigDecimal("debtorEndBalance");
                    } else {
                        balance1 = json.getBigDecimal("creditEndBalance");
                    }
                } else if (statement.getTimeType() == TWO) {
                    if (subject.getBalanceDirection() == 1) {
                        balance1 = json.getBigDecimal("debtorInitialBalance");
                    } else {
                        balance1 = json.getBigDecimal("creditInitialBalance");
                    }
                } else {
                    if (subject.getBalanceDirection() == 1) {
                        balance1 = json.getBigDecimal("debtorInitialYearBalance");
                    } else {
                        balance1 = json.getBigDecimal("creditInitialYearBalance");
                    }
                }
            } else if (statement.getGainRule() == TWO) {
                if (statement.getTimeType() == 1) {
                    balance1 = json.getBigDecimal("debtorSumEndBalance");
                } else if (statement.getTimeType() == TWO) {
                    balance1 = json.getBigDecimal("debtorSumInitialBalance");
                } else {
                    balance1 = json.getBigDecimal("debtorSumInitialYearBalance");
                }
            } else if (statement.getGainRule() == THREE) {
                if (statement.getTimeType() == 1) {
                    balance1 = json.getBigDecimal("creditSumEndBalance");
                } else if (statement.getTimeType() == TWO) {
                    balance1 = json.getBigDecimal("creditSumInitialBalance");
                } else {
                    balance1 = json.getBigDecimal("creditSumInitialYearBalance");
                }
            } else if (statement.getGainRule() == FOUR) {
                if (statement.getTimeType() == 1) {
                    balance1 = json.getBigDecimal("debtorCurrentBalance");
                } else if (statement.getTimeType() == TWO) {
                    balance1 = json.getBigDecimal("debtorYearBalance");
                } else if (statement.getTimeType() == THREE) {
                    balance1 = json.getBigDecimal("debtorLastMonthBalance");
                } else if (statement.getTimeType() == FOUR) {
                    balance1 = json.getBigDecimal("debtorLastYearMonthBalance");
                } else if (statement.getTimeType() == FIVE) {
                    balance1 = json.getBigDecimal("debtorLastYearBalance");
                }
            } else if (statement.getGainRule() == FIVE) {
                if (statement.getTimeType() == 1) {
                    balance1 = json.getBigDecimal("creditCurrentBalance");
                } else if (statement.getTimeType() == TWO) {
                    balance1 = json.getBigDecimal("creditYearBalance");
                } else if (statement.getTimeType() == THREE) {
                    balance1 = json.getBigDecimal("creditLastMonthBalance");
                } else if (statement.getTimeType() == FOUR) {
                    balance1 = json.getBigDecimal("creditLastYearMonthBalance");
                } else if (statement.getTimeType() == FIVE) {
                    balance1 = json.getBigDecimal("creditLastYearBalance");
                }
            } else {
                FinanceSubjectIdsVO idsVO = financeSubjectService.queryIdsByType(certificateTime);
                FinanceDetailAccountBO accountBO = new FinanceDetailAccountBO();
                accountBO.setSubjectIds(idsVO.getIds());
                accountBO.setStartTime(certificateTime);
                accountBO.setEndTime(certificateTime);
                accountBO.setAccountId(AccountSet.getAccountSetId());
                json = getBaseMapper().queryDetailBalanceAccount(accountBO);
                if (statement.getTimeType() == 1) {
                    balance1 = json.getBigDecimal("yearPeriod");
                }
            }
            if (balance1.compareTo(BigDecimal.ZERO) < 1) {
                return BigDecimal.ZERO;
            }
            List<FinanceStatementSubject> statementSubjects = statementSubjectService.lambdaQuery()
                    .eq(FinanceStatementSubject::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceStatementSubject::getStatementId, statement.getStatementId()).list();
            List<FinanceCertificateDetail> details = new ArrayList<>();

            for (FinanceStatementSubject statementSubject : statementSubjects) {
                FinanceCertificateDetail certificateDetail = new FinanceCertificateDetail();
                certificateDetail.setDigestContent(statementSubject.getDigestContent());
                BigDecimal balance4 = balance1.multiply(new BigDecimal(statementSubject.getMoneyRatio())).divide(new BigDecimal("100"));
                if (statementSubject.getIsLend() == 1) {
                    debtorBalance = debtorBalance.add(balance4);
                    certificateDetail.setDebtorBalance(balance4);
                    returnBalance = returnBalance.add(balance4);
                } else {
                    certificateDetail.setCreditBalance(balance4);
                    creditBalance = creditBalance.add(balance4);
                    returnBalance = returnBalance.add(balance4);
                }
                subject = financeSubjectService.getById(statementSubject.getSubjectId());
                certificateDetail.setSubjectId(statementSubject.getSubjectId());
                certificateDetail.setSubjectNumber(subject.getNumber());
                certificateDetail.setSubjectContent(JSONObject.toJSONString(subject));
                details.add(certificateDetail);
            }
            certificateBO.setDebtorBalance(debtorBalance);
            certificateBO.setCreditBalance(creditBalance);
            certificateBO.setCertificateDetails(details);
            certificateBO.setCertificateTime(certificateTime);
            if (type == 1) {
                FinanceCertificate certificate = financeCertificateService.saveAndUpdate(certificateBO);
                if (certificateBO.getCertificateId() == null) {
                    FinanceStatementCertificate statementCertificate = new FinanceStatementCertificate();
                    statementCertificate.setCertificateId(certificate.getCertificateId());
                    statementCertificate.setCertificateTime(LocalDateTimeUtil.parse(certificateTime, DatePattern.PURE_DATE_PATTERN));
                    statementCertificate.setStatementId(statement.getStatementId());
                    statementCertificate.setAccountId(AccountSet.getAccountSetId());
                    financeStatementCertificateService.save(statementCertificate);
                }
            }
        }

        return returnBalance;
    }

    /**
     * 处理转出未交增值税
     */
    private BigDecimal appreciationStatement(FinanceStatement statement, String certificateTime, Integer type) {
        BigDecimal balance = new BigDecimal("0");
        //先判断是否符合生成凭证条件
        FinanceSubjectIdsVO idsVO = financeSubjectService.queryIdsByNumber("222101");
        JSONObject json = CashFlowStatementReportHolder.getDetailBalance(idsVO.getSubjectId(), certificateTime);

        if (json == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance2;
        if (idsVO.getBalanceDirection() == 1) {
            balance2 = json.getBigDecimal("debtorEndBalance");
        } else {
            balance2 = json.getBigDecimal("creditEndBalance");
        }
        if (balance2.compareTo(new BigDecimal(ZERO)) > 0) {
            FinanceCertificateBO certificateBO = new FinanceCertificateBO();
            if (type == 1) {
                String format = DateUtil.format(DateUtil.parse(certificateTime), "yyyyMM");
                List<JSONObject> certificates = getBaseMapper().queryCertificate(
                        statement.getStatementId(), format, AccountSet.getAccountSetId());
                FinanceVoucher financeVoucher = financeVoucherService.lambdaQuery()
                        .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceVoucher::getIsDefault, 1).one();
                if (certificates.size() > 0) {
                    if (financeVoucher.getVoucherId().equals(certificates.get(0).getLong(VOUCHER_ID))) {
                        certificateBO.setVoucherId(certificates.get(0).getLong("voucherId"));
                        certificateBO.setCertificateNum(certificates.get(0).getInteger("num"));
                    }
                    LambdaQueryWrapper<FinanceStatementCertificate> wrapper1 = new LambdaQueryWrapper<>();
                    wrapper1.eq(FinanceStatementCertificate::getCertificateId, certificates.get(0).getLong("certificateId"))
                            .eq(FinanceStatementCertificate::getStatementId, statement.getStatementId());
                    financeStatementCertificateService.remove(wrapper1);
                    financeCertificateService.removeById(certificates.get(0).getLong("certificateId"));

                    LambdaQueryWrapper<FinanceCertificateDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(FinanceCertificateDetail::getCertificateId, certificateBO.getCertificateId())
                            .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId());
                    financeCertificateDetailService.remove(wrapper);
                }
                if (financeVoucher != null && certificateBO.getVoucherId() == null) {
                    certificateBO.setVoucherId(financeVoucher.getVoucherId());
                }
            }
            //满足条件，生成凭证
            List<FinanceStatementSubject> statementSubjects = statementSubjectService.lambdaQuery()
                    .eq(FinanceStatementSubject::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceStatementSubject::getStatementId, statement.getStatementId()).list();
            certificateBO.setCertificateTime(certificateTime);
            BigDecimal debtorBalance = new BigDecimal("0");
            BigDecimal creditBalance = new BigDecimal("0");
            List<FinanceCertificateDetail> details = new ArrayList<>();
            for (FinanceStatementSubject statementSubject : statementSubjects) {
                FinanceCertificateDetail certificateDetail = new FinanceCertificateDetail();
                certificateDetail.setDigestContent(statementSubject.getDigestContent());
                BigDecimal balance4 = balance2.multiply(new BigDecimal(statementSubject.getMoneyRatio())).divide(new BigDecimal("100"));
                if (statementSubject.getIsLend() == 1) {
                    debtorBalance.add(balance4);
                    certificateDetail.setDebtorBalance(balance4);
                } else {
                    certificateDetail.setCreditBalance(balance4);
                    creditBalance.add(balance4);
                }
                balance = balance.add(balance4);
                FinanceSubject subject = financeSubjectService.getById(statementSubject.getSubjectId());
                certificateDetail.setSubjectId(statementSubject.getSubjectId());
                certificateDetail.setSubjectNumber(subject.getNumber());
                certificateDetail.setSubjectContent(JSONObject.toJSONString(subject));
                details.add(certificateDetail);
            }
            certificateBO.setDebtorBalance(debtorBalance);
            certificateBO.setCreditBalance(creditBalance);
            certificateBO.setCertificateDetails(details);
            certificateBO.setCertificateTime(certificateTime);
            if (type == 1) {
                FinanceCertificate certificate = financeCertificateService.saveAndUpdate(certificateBO);
                if (certificateBO.getCertificateId() == null) {
                    FinanceStatementCertificate statementCertificate = new FinanceStatementCertificate();
                    statementCertificate.setCertificateId(certificate.getCertificateId());
                    statementCertificate.setCertificateTime(LocalDateTimeUtil.parse(certificateTime, DatePattern.PURE_DATE_PATTERN));
                    statementCertificate.setStatementId(statement.getStatementId());
                    statementCertificate.setAccountId(AccountSet.getAccountSetId());
                    financeStatementCertificateService.save(statementCertificate);
                }
            }
        }

        return balance.divide(new BigDecimal(2));

    }

    /**
     * 处理计提地税
     */
    private BigDecimal touchStatement(FinanceStatement statement, String certificateTime, Integer type) {
        BigDecimal balance = new BigDecimal("0");
        FinanceDetailAccountBO accountBO = new FinanceDetailAccountBO();
        accountBO.setAccountId(AccountSet.getAccountSetId());
        BigDecimal balance3 = new BigDecimal("0");
        FinanceSubjectIdsVO idsVO = financeSubjectService.queryIdsByNumber("222101");
        JSONObject json = CashFlowStatementReportHolder.getDetailBalance(idsVO.getSubjectId(), certificateTime);
        if (json == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance2;
        if (idsVO.getBalanceDirection() == 1) {
            balance2 = json.getBigDecimal("debtorEndBalance");
        } else {
            balance2 = json.getBigDecimal("creditEndBalance");
        }
        BigDecimal appreciationBalance = new BigDecimal("0");
        idsVO = financeSubjectService.queryIdsByNumber("222121");
        accountBO.setSubjectIds(idsVO.getIds());
        json = getBaseMapper().queryDetailBalanceAccount(accountBO);
        if (idsVO.getBalanceDirection() == 1) {
            appreciationBalance = json.getBigDecimal("debtorEndBalance");
        } else {
            appreciationBalance = json.getBigDecimal("creditEndBalance");
        }
        appreciationBalance = balance2.add(appreciationBalance);
        if (appreciationBalance.compareTo(new BigDecimal(ZERO)) == 1) {
            FinanceCertificateBO certificateBO = new FinanceCertificateBO();
            if (type == 1) {
                String format = DateUtil.format(DateUtil.parse(certificateTime), "yyyyMM");
                List<JSONObject> certificates = getBaseMapper().queryCertificate(
                        statement.getStatementId(), format, AccountSet.getAccountSetId());
                FinanceVoucher financeVoucher = financeVoucherService.lambdaQuery()
                        .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceVoucher::getIsDefault, 1).one();
                if (certificates.size() > 0) {
                    if (financeVoucher.getVoucherId().equals(certificates.get(0).getLong(VOUCHER_ID))) {
                        // certificateBO.setCertificateId(certificates.get(0).getInteger("certificateId"));
                        certificateBO.setVoucherId(certificates.get(0).getLong("voucherId"));
                        certificateBO.setCertificateNum(certificates.get(0).getInteger("num"));
                    }
                    LambdaQueryWrapper<FinanceStatementCertificate> wrapper1 = new LambdaQueryWrapper<>();
                    wrapper1.eq(FinanceStatementCertificate::getCertificateId, certificates.get(0).getLong("certificateId"))
                            .eq(FinanceStatementCertificate::getStatementId, statement.getStatementId());
                    financeStatementCertificateService.remove(wrapper1);
                    financeCertificateService.removeById(certificates.get(0).getLong("certificateId"));
                    LambdaQueryWrapper<FinanceCertificateDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(FinanceCertificateDetail::getCertificateId, certificateBO.getCertificateId())
                            .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId());
                    financeCertificateDetailService.remove(wrapper);
                }
                if (financeVoucher != null && certificateBO.getVoucherId() == null) {
                    certificateBO.setVoucherId(financeVoucher.getVoucherId());
                }
            }
            //满足条件，生成凭证
            balance3 = balance2;
            List<FinanceStatementSubject> statementSubjects = statementSubjectService.lambdaQuery()
                    .eq(FinanceStatementSubject::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceStatementSubject::getStatementId, statement.getStatementId()).list();
            certificateBO.setCertificateTime(certificateTime);
            BigDecimal debtorBalance = new BigDecimal("0");
            BigDecimal creditBalance = new BigDecimal("0");
            List<FinanceCertificateDetail> details = new ArrayList<>();
            Integer sort = 1;
            for (FinanceStatementSubject statementSubject : statementSubjects) {
                FinanceCertificateDetail certificateDetail = new FinanceCertificateDetail();
                certificateDetail.setDigestContent(statementSubject.getDigestContent());
                BigDecimal balance4 = balance3.multiply(new BigDecimal(statementSubject.getMoneyRatio())).divide(new BigDecimal("100"));
                if (statementSubject.getIsLend() == 1) {
                    debtorBalance.add(balance4);
                    certificateDetail.setDebtorBalance(balance4);
                    certificateDetail.setSort(sort);
                    sort = sort + 1;
                } else {
                    certificateDetail.setCreditBalance(balance4);
                    creditBalance.add(balance4);
                    certificateDetail.setSort(statementSubjects.size() + sort);
                }
                balance = balance.add(balance4);
                FinanceSubject subject = financeSubjectService.getById(statementSubject.getSubjectId());
                certificateDetail.setSubjectId(statementSubject.getSubjectId());
                certificateDetail.setSubjectNumber(subject.getNumber());
                certificateDetail.setSubjectContent(JSONObject.toJSONString(subject));
                details.add(certificateDetail);
            }
            certificateBO.setDebtorBalance(debtorBalance);
            certificateBO.setCreditBalance(creditBalance);
            certificateBO.setCertificateDetails(details);
            certificateBO.setCertificateTime(certificateTime);
            if (type == 1) {
                FinanceCertificate certificate = financeCertificateService.saveAndUpdate(certificateBO);
                FinanceStatementCertificate statementCertificate = new FinanceStatementCertificate();
                statementCertificate.setCertificateId(certificate.getCertificateId());
                statementCertificate.setCertificateTime(LocalDateTimeUtil.parse(certificateTime, DatePattern.PURE_DATE_PATTERN));
                statementCertificate.setStatementId(statement.getStatementId());
                statementCertificate.setAccountId(AccountSet.getAccountSetId());
                financeStatementCertificateService.save(statementCertificate);
            }
        }
        return balance.divide(new BigDecimal(2));
    }

    /**
     * 处理计提所得税
     */
    private BigDecimal gainsStatement(FinanceStatement statement, String certificateTime, Integer type) {
        CashFlowStatementReportHolder.remove();
        BigDecimal balance = new BigDecimal("0");
        FinanceReportRequestBO bo = new FinanceReportRequestBO();
        bo.setType(1);
        int eight = 8;
        if (certificateTime.length() == eight) {
            bo.setFromPeriod(PeriodUtils.parsePeriod(DateUtil.parse(certificateTime, "yyyyMMdd")));
            bo.setToPeriod(PeriodUtils.parsePeriod(DateUtil.parse(certificateTime, "yyyyMMdd")));
        } else {
            bo.setFromPeriod(certificateTime);
            bo.setToPeriod(certificateTime);
        }
        bo.setAccountId(statement.getAccountId());
        List<JSONObject> list = financeIncomeStatementService.report(bo);
        if (list.isEmpty()) {
            throw new CrmException(FinanceCodeEnum.FINANCE_INCOME_STATEMENT_NOT_EXIST_ERROR);
        }
        log.info("计提所得税数据：{}", list);
        JSONObject json = financeIncomeStatementService.filterReport(list, 30);
        if (json == null) {
            return new BigDecimal("0");
        }
        BigDecimal balance1 = json.getBigDecimal("yearValue");
        json = financeIncomeStatementService.filterReport(list, 31);
        if (json == null) {
            return new BigDecimal("0");
        }
        BigDecimal balance2 = json.getBigDecimal("yearValue");
        FinanceStatement financeStatement = lambdaQuery().eq(FinanceStatement::getStatementType, 2)
                .eq(FinanceStatement::getAccountId, AccountSet.getAccountSetId()).one();
        BigDecimal balance3 = new BigDecimal("0");
        if (financeStatement != null) {
            balance3 = incomeStatement(financeStatement, certificateTime, 0);
        }
        String decimal = "0.25";
        if (balance1.multiply(new BigDecimal(decimal)).subtract(balance2).compareTo(new BigDecimal(ZERO)) > 0 && balance3.compareTo(new BigDecimal("0")) > 0) {
            FinanceCertificateBO certificateBO = new FinanceCertificateBO();
            if (type == 1) {
                String format = DateUtil.format(DateUtil.parse(certificateTime), "yyyyMM");
                List<JSONObject> certificates = getBaseMapper().queryCertificate(
                        statement.getStatementId(), format, AccountSet.getAccountSetId());
                FinanceVoucher financeVoucher = financeVoucherService.lambdaQuery()
                        .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceVoucher::getIsDefault, 1).one();
                if (certificates.size() > 0) {
                    if (financeVoucher.getVoucherId().equals(certificates.get(0).getLong(VOUCHER_ID))) {
                        certificateBO.setVoucherId(certificates.get(0).getLong("voucherId"));
                        certificateBO.setCertificateNum(certificates.get(0).getInteger("num"));
                    }
                    LambdaQueryWrapper<FinanceStatementCertificate> wrapper1 = new LambdaQueryWrapper<>();
                    wrapper1.eq(FinanceStatementCertificate::getCertificateId, certificates.get(0).getLong("certificateId"))
                            .eq(FinanceStatementCertificate::getStatementId, statement.getStatementId());
                    financeStatementCertificateService.remove(wrapper1);
                    financeCertificateService.removeById(certificates.get(0).getLong("certificateId"));
                    LambdaQueryWrapper<FinanceCertificateDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(FinanceCertificateDetail::getCertificateId, certificateBO.getCertificateId())
                            .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId());
                    financeCertificateDetailService.remove(wrapper);
                }
                if (financeVoucher != null && certificateBO.getVoucherId() == null) {
                    certificateBO.setVoucherId(financeVoucher.getVoucherId());
                }
            }
            List<FinanceStatementSubject> statementSubjects = statementSubjectService.lambdaQuery()
                    .eq(FinanceStatementSubject::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceStatementSubject::getStatementId, statement.getStatementId()).list();
            certificateBO.setCertificateTime(certificateTime);
            BigDecimal debtorBalance = new BigDecimal("0");
            BigDecimal creditBalance = new BigDecimal("0");
            List<FinanceCertificateDetail> details = new ArrayList<>();
            for (FinanceStatementSubject statementSubject : statementSubjects) {
                FinanceCertificateDetail certificateDetail = new FinanceCertificateDetail();
                certificateDetail.setDigestContent(statementSubject.getDigestContent());
                BigDecimal balance4 = balance1.multiply(new BigDecimal(statementSubject.getMoneyRatio())).divide(new BigDecimal("100"));
                if (statementSubject.getIsLend() == 1) {
                    debtorBalance.add(balance4);
                    certificateDetail.setDebtorBalance(balance4);
                } else {
                    certificateDetail.setCreditBalance(balance4);
                    creditBalance.add(balance4);
                }
                balance = balance.add(balance4);
                FinanceSubject subject = financeSubjectService.getById(statementSubject.getSubjectId());
                certificateDetail.setSubjectId(statementSubject.getSubjectId());
                certificateDetail.setSubjectNumber(subject.getNumber());
                certificateDetail.setSubjectContent(JSONObject.toJSONString(subject));
                details.add(certificateDetail);
            }
            certificateBO.setDebtorBalance(debtorBalance);
            certificateBO.setCreditBalance(creditBalance);
            certificateBO.setCertificateDetails(details);
            certificateBO.setCertificateTime(certificateTime);
            if (type == 1) {
                FinanceCertificate certificate = financeCertificateService.saveAndUpdate(certificateBO);
                if (certificateBO.getCertificateId() == null) {
                    FinanceStatementCertificate statementCertificate = new FinanceStatementCertificate();
                    statementCertificate.setCertificateId(certificate.getCertificateId());
                    statementCertificate.setCertificateTime(LocalDateTimeUtil.parse(certificateTime, DatePattern.PURE_DATE_PATTERN));
                    statementCertificate.setStatementId(statement.getStatementId());
                    statementCertificate.setAccountId(AccountSet.getAccountSetId());
                    financeStatementCertificateService.save(statementCertificate);
                }
            }
        }
        return balance.divide(new BigDecimal(2));

    }

    /**
     * 处理结转损益
     */
    private BigDecimal incomeStatement(FinanceStatement statement, String certificateTime, Integer type) {
        BigDecimal balance = new BigDecimal("0");
        FinanceSubjectIdsVO idsVO = financeSubjectService.queryIdsById(statement.getAdjustSubjectId());
        FinanceDetailAccountBO accountBO = new FinanceDetailAccountBO();
        accountBO.setSubjectIds(idsVO.getIds());
        accountBO.setStartTime(certificateTime);
        accountBO.setEndTime(certificateTime);
        accountBO.setAccountId(AccountSet.getAccountSetId());
        JSONObject json = getBaseMapper().queryDetailBalanceAccount(accountBO);
        BigDecimal balance1;
        if (idsVO.getBalanceDirection() == 1) {
            balance1 = json.getBigDecimal("debtorEndBalance");
        } else {
            balance1 = json.getBigDecimal("creditEndBalance");
        }
        if (balance1.compareTo(new BigDecimal(ZERO)) == 1) {
            FinanceCertificateBO certificateBO = new FinanceCertificateBO();
            List<FinanceCertificateDetail> details = new ArrayList<>();
            certificateBO.setVoucherId(statement.getVoucherId());
            FinanceCertificateDetail certificateDetail1 = new FinanceCertificateDetail();
            certificateDetail1.setDigestContent(statement.getDigestContent());
            if (idsVO.getBalanceDirection() == 1) {
                certificateDetail1.setDebtorBalance(balance1);
            } else {
                certificateDetail1.setCreditBalance(balance1);
            }
            FinanceSubject subject = financeSubjectService.getById(statement.getAdjustSubjectId());
            certificateDetail1.setSubjectId(subject.getSubjectId());
            certificateDetail1.setSubjectNumber(subject.getNumber());
            certificateDetail1.setSubjectContent(JSONObject.toJSONString(subject));
            details.add(certificateDetail1);
            FinanceCertificateDetail certificateDetail2 = new FinanceCertificateDetail();
            certificateDetail2.setDigestContent(statement.getDigestContent());
            subject = financeSubjectService.getById(statement.getEndSubjectId());
            if (subject.getBalanceDirection() == 1) {
                certificateDetail2.setDebtorBalance(balance1);
            } else {
                certificateDetail2.setCreditBalance(balance1);
            }
            certificateDetail2.setSubjectId(subject.getSubjectId());
            certificateDetail2.setSubjectNumber(subject.getNumber());
            certificateDetail2.setSubjectContent(JSONObject.toJSONString(subject));
            details.add(certificateDetail2);
            certificateBO.setDebtorBalance(balance1);
            certificateBO.setCreditBalance(balance1);
            certificateBO.setCertificateDetails(details);
            certificateBO.setCertificateTime(certificateTime);
            if (type == 1) {
                FinanceCertificate certificate = financeCertificateService.saveAndUpdate(certificateBO);
            }
        }
        idsVO = financeSubjectService.queryIdsByType(certificateTime);
        accountBO.setSubjectIds(idsVO.getIds());
        String format = DateUtil.format(DateUtil.parse(certificateTime), "yyyyMM");
        accountBO.setStartTime(format);
        accountBO.setEndTime(format);
        accountBO.setIsFlat(1);
        FinanceCertificateBO certificateBO = new FinanceCertificateBO();
        if (type == 1) {
            List<JSONObject> certificates = getBaseMapper().queryCertificate(
                    statement.getStatementId(), format, AccountSet.getAccountSetId());
            if (certificates.size() > 0) {
                if (statement.getVoucherId().equals(certificates.get(0).getLong(VOUCHER_ID))) {
                    //    certificateBO.setCertificateId(certificates.get(0).getInteger("certificateId"));
                    certificateBO.setVoucherId(certificates.get(0).getLong("voucherId"));
                    certificateBO.setCertificateNum(certificates.get(0).getInteger("num"));
                }
                LambdaQueryWrapper<FinanceStatementCertificate> wrapper1 = new LambdaQueryWrapper<>();
                wrapper1.eq(FinanceStatementCertificate::getCertificateId, certificates.get(0).getLong("certificateId"))
                        .eq(FinanceStatementCertificate::getStatementId, statement.getStatementId());
                financeStatementCertificateService.remove(wrapper1);
                financeCertificateService.removeById(certificates.get(0).getLong("certificateId"));
                LambdaQueryWrapper<FinanceCertificateDetail> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(FinanceCertificateDetail::getCertificateId, certificateBO.getCertificateId())
                        .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId());
                financeCertificateDetailService.remove(wrapper);
            }
        }
        List<JSONObject> list = financeCertificateService.queryListDetailBalanceAccount(accountBO);
        BigDecimal debtorBalance = new BigDecimal("0");
        BigDecimal creditBalance = new BigDecimal("0");
        if (list.size() > 0) {
            List<FinanceCertificateDetail> details = new ArrayList<>();
            certificateBO.setVoucherId(statement.getVoucherId());
            for (JSONObject jsonObject : list) {
                FinanceCertificateDetail certificateDetail = new FinanceCertificateDetail();
                certificateDetail.setDigestContent(statement.getDigestContent());
                if (statement.getEndWay() == 0) {
                    if (jsonObject.getInteger("balanceDirection") == 1) {
                        BigDecimal creditBalance1 = jsonObject.getBigDecimal("debtorCurrentEndBalance");
                        certificateDetail.setCreditBalance(creditBalance1);
                        creditBalance = creditBalance.add(creditBalance1);
                        balance = balance.add(creditBalance1);
                    } else {
                        BigDecimal debtorBalance1 = jsonObject.getBigDecimal("creditCurrentEndBalance");
                        certificateDetail.setDebtorBalance(debtorBalance1);
                        debtorBalance = debtorBalance.add(debtorBalance1);
                        balance = balance.add(debtorBalance1);
                    }
                } else {
                    log.info("按照余额方向结转");
                    BigDecimal creditBalance1 = jsonObject.getBigDecimal("creditCurrentBalance");
                    BigDecimal debtorBalance1 = jsonObject.getBigDecimal("debtorCurrentBalance");
                    log.info("借方发生额：" + debtorBalance1);
                    log.info("贷方发生额：" + creditBalance1);
                    if (creditBalance1.compareTo(new BigDecimal(0)) != 0 && debtorBalance1.compareTo(new BigDecimal(0)) != 0) {
                        log.info("借方贷方都有发生额");
                        if (creditBalance1.compareTo(debtorBalance1) >= 0) {
                            log.info("贷方发生额大于借方发生额 进行计算");
                            BigDecimal currentBalance = creditBalance1.subtract(debtorBalance1);
                            log.info("贷方余额" + currentBalance);
                            certificateDetail.setDebtorBalance(currentBalance);
                            debtorBalance = debtorBalance.add(currentBalance);
                            balance = balance.add(currentBalance);
                        } else {
                            log.info("借方发生额大于贷方发生额 进行计算");
                            BigDecimal currentBalance = debtorBalance1.subtract(creditBalance1);
                            log.info("借方余额" + currentBalance);
                            certificateDetail.setCreditBalance(currentBalance);
                            creditBalance = creditBalance.add(currentBalance);
                            balance = balance.add(currentBalance);
                        }
                    } else {
                        log.info("只有一方有发生额");
                        if (jsonObject.getBigDecimal("creditCurrentBalance").compareTo(new BigDecimal(0)) != 0) {
                            log.info("贷方有发生额，进行计算");
                            certificateDetail.setDebtorBalance(creditBalance1);
                            debtorBalance = debtorBalance.add(creditBalance1);
                            balance = balance.add(creditBalance1);
                        } else {
                            log.info("借方有发生额，进行计算");
                            certificateDetail.setCreditBalance(debtorBalance1);
                            creditBalance = creditBalance.add(debtorBalance1);
                            balance = balance.add(debtorBalance1);
                        }
                    }
                }
                certificateDetail.setSubjectId(jsonObject.getLong("subjectId"));
                certificateDetail.setSubjectNumber(jsonObject.getString("number"));
                FinanceSubject subject = financeSubjectService.getById(jsonObject.getLong("subjectId"));
                certificateDetail.setSubjectContent(JSONObject.toJSONString(subject));
                details.add(certificateDetail);
            }
            FinanceSubject subject = financeSubjectService.getById(statement.getRestSubjectId());
            FinanceCertificateDetail certificateDetail = new FinanceCertificateDetail();
            certificateDetail.setDigestContent(statement.getDigestContent());

            if (debtorBalance.compareTo(creditBalance) == 1) {
                certificateDetail.setCreditBalance(debtorBalance.subtract(creditBalance));
            } else {
                certificateDetail.setDebtorBalance(creditBalance.subtract(debtorBalance));
            }

            if (type == 1) {
                certificateDetail.setSubjectId(subject.getSubjectId());
                certificateDetail.setSubjectNumber(subject.getNumber());
                certificateDetail.setSubjectContent(JSONObject.toJSONString(subject));
                details.add(certificateDetail);
                certificateBO.setDebtorBalance(debtorBalance);
                certificateBO.setCreditBalance(creditBalance);
                certificateBO.setCertificateDetails(details);
                certificateBO.setCertificateTime(certificateTime);
                FinanceCertificate certificate = financeCertificateService.saveAndUpdate(certificateBO);

                if (certificateBO.getCertificateId() == null) {
                    FinanceStatementCertificate statementCertificate = new FinanceStatementCertificate();
                    statementCertificate.setCertificateId(certificate.getCertificateId());
                    statementCertificate.setCertificateTime(LocalDateTimeUtil.parse(certificateTime, DatePattern.PURE_DATE_PATTERN));
                    statementCertificate.setStatementId(statement.getStatementId());
                    statementCertificate.setAccountId(AccountSet.getAccountSetId());
                    financeStatementCertificateService.save(statementCertificate);
                }
            }
        }
        FinanceSubject subject = financeSubjectService.getById(statement.getRestSubjectId());
        if (subject == null) {
            return BigDecimal.ZERO;
        }
        if (subject.getBalanceDirection() == 1) {
            return creditBalance.subtract(debtorBalance);
        } else {
            return debtorBalance.subtract(creditBalance);
        }
    }

    @Override
    public void importTemplate(Sheet sheet) {

    }

    @Override
    public void init() {
        String str;
        ClassPathResource classPathResource = new ClassPathResource("excelTemplates/statementSave.json");
        try {
            InputStream inputStream = classPathResource.getInputStream();
            str = IoUtil.read(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            log.error("初始化json错误", e);
            throw new CrmException(SystemCodeEnum.SYSTEM_ERROR);
        }
        JSONArray array = JSON.parseArray(str);
        Map<String, FinanceSubject> subjectMap = CashFlowStatementReportHolder.getSubjects().stream().collect(Collectors.toMap(FinanceSubject::getNumber, Function.identity(), (k1, k2) -> k1));
        List<FinanceStatement> statements = new ArrayList<>();
        List<FinanceStatementSubject> statementSubjects = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            if (json.getInteger("statementType") == 2) {
                FinanceStatement statement = json.toJavaObject(FinanceStatement.class);
                FinanceSubject subject = subjectMap.get(json.getString("adjustNumber"));
                statement.setAdjustSubjectId(subject.getSubjectId());
                subject = subjectMap.get(json.getString("endNumber"));
                statement.setEndSubjectId(subject.getSubjectId());
                subject = subjectMap.get(json.getString("restNumber"));
                statement.setRestSubjectId(subject.getSubjectId());
                statement.setCreateUserId(UserUtil.getUserId());
                statement.setCreateTime(LocalDateTime.now());
                statement.setAccountId(AccountSet.getAccountSetId());
                if (statement.getVoucherId() == null) {
                    FinanceVoucher financeVoucher = financeVoucherService.lambdaQuery()
                            .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                            .eq(FinanceVoucher::getIsDefault, 1).one();
                    if (financeVoucher != null) {
                        statement.setVoucherId(financeVoucher.getVoucherId());
                    }
                }
                statement.setDigestContent("结转本期损益");
                statement.setStatementId(BaseUtil.getNextId());
                statements.add(statement);
            } else {
                FinanceStatementSaveBO statementSaveBO = json.toJavaObject(FinanceStatementSaveBO.class);
                FinanceStatement statement = BeanUtil.copyProperties(statementSaveBO, FinanceStatement.class);
                statement.setCreateUserId(UserUtil.getUserId());
                statement.setCreateTime(LocalDateTime.now());
                statement.setAccountId(AccountSet.getAccountSetId());
                statement.setStatementId(BaseUtil.getNextId());
                statements.add(statement);
                for (FinanceStatementSubject statementSubject : statementSaveBO.getSubjectList()) {
                    FinanceSubject subject = subjectMap.get(statementSubject.getNumber());
                    if (subject != null) {
                        statementSubject.setSubjectId(subject.getSubjectId());
                        statementSubject.setStatementId(statement.getStatementId());
                        statementSubject.setAccountId(AccountSet.getAccountSetId());
                        statementSubject.setStatementSubjectId(BaseUtil.getNextId());
                        statementSubjects.add(statementSubject);
                    }
                }

            }
        }
        saveBatch(statements);
        statementSubjectService.saveBatch(statementSubjects);
    }

    @Override
    public List<JSONObject> queryLossDetailByPeriod(String period) {
        return getBaseMapper().queryLossDetailByPeriod(period, AccountSet.getAccountSetId());
    }
}
