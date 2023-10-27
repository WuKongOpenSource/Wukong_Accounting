package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.cell.CellUtil;
import cn.hutool.poi.excel.style.StyleUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.upload.entity.UploadEntity;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.common.Const;
import com.kakarote.core.common.MultipartFileUtil;
import com.kakarote.core.common.cache.AdminCacheKey;
import com.kakarote.core.common.cache.CrmCacheKey;
import com.kakarote.core.common.enums.SystemCodeEnum;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.feign.admin.entity.FileEntity;
import com.kakarote.core.feign.crm.entity.BiParams;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.*;
import com.kakarote.finance.common.*;
import com.kakarote.finance.constant.AdjuvantTypeEnum;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.constant.FinanceConst;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.entity.PO.*;
import com.kakarote.finance.entity.VO.FinanceCertificateVO;
import com.kakarote.finance.entity.VO.FinanceDiversificationVO;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import com.kakarote.finance.mapper.FinanceAdjuvantCarteMapper;
import com.kakarote.finance.mapper.FinanceCertificateMapper;
import com.kakarote.finance.mapper.FinanceInitialMapper;
import com.kakarote.finance.mapper.FinanceSubjectMapper;
import com.kakarote.finance.service.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.kakarote.ids.provider.utils.UserCacheUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 凭证表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@Slf4j
@Service
public class FinanceCertificateServiceImpl extends BaseServiceImpl<FinanceCertificateMapper, FinanceCertificate> implements IFinanceCertificateService {

    @Autowired
    private IFinanceCertificateDetailService financeCertificateDetailService;

    @Autowired
    private IFinanceSubjectService financeSubjectService;

    @Autowired
    private IFinanceVoucherService financeVoucherService;

    @Autowired
    private IFinanceCertificateAssociationService associationService;

    @Autowired
    private IFinanceParameterService parameterService;

    @Autowired
    private IFinanceStatementCertificateService financeStatementCertificateService;

    @Autowired
    private IFinanceStatementSettleService financeStatementSettleService;

    @Autowired
    private IFinanceAdjuvantCarteService carteService;

    @Autowired
    private IFinanceStatementService financeStatementService;

    @Autowired
    private IFinanceAssistService financeAssistService;

    @Autowired
    private IFinanceSubjectService subjectService;

    @Autowired
    private IFinanceInitialService initialService;

    @Autowired
    private FinanceInitialMapper initialMapper;

    @Autowired
    private IFinanceAdjuvantService adjuvantService;

    @Autowired
    private FinanceSubjectMapper subjectMapper;

    @Autowired
    private FinanceAdjuvantCarteMapper adjuvantCarteMapper;

    @Autowired
    private IFinanceAssistAdjuvantService financeAssistAdjuvantService;

    @Autowired
    private AdminFileService fileService;

    @Override
    @Synchronized
    public FinanceCertificate saveAndUpdate(FinanceCertificateBO financeCertificateBO) {
        FinanceCertificate certificate = BeanUtil.copyProperties(financeCertificateBO, FinanceCertificate.class);
        if (certificate.getCertificateId() == null) {
            if (certificate.getCertificateNum() == null) {
                FinanceCertificateSettleBO settleBO = new FinanceCertificateSettleBO();
                settleBO.setSettleTime(DateUtil.format(certificate.getCertificateTime(), "yyyyMM"));
                settleBO.setVoucherId(financeCertificateBO.getVoucherId());
                JSONObject json = queryCertificateNum(settleBO);
                certificate.setCertificateNum(json.getInteger("certificateNum"));
                if (Convert.toLong(settleBO.getSettleTime()) < Convert.toLong(financeStatementSettleService.getCurrentPeriod())) {
                    throw new CrmException(FinanceCodeEnum.FINANCE_CERTIFICATE_TIME_ERROR);
                }
            }
            String batchId = IdUtil.simpleUUID();
            FinanceCertificate financeCertificate = getBaseMapper().queryByTime(certificate);
            if (financeCertificate != null) {
                throw new CrmException(FinanceCodeEnum.FINANCE_CERTIFICATE_VOUCHER_REPEATING_ERROR);
            }
            certificate.setCreateUserId(UserUtil.getUserId());
            certificate.setCreateTime(LocalDateTime.now());
            certificate.setAccountId(AccountSet.getAccountSetId());
            certificate.setBatchId(batchId);
            save(certificate);
        } else {
            certificate.setAccountId(AccountSet.getAccountSetId());
            FinanceCertificate financeCertificate = getBaseMapper().queryByTime(certificate);
            if (financeCertificate != null) {
                throw new CrmException(FinanceCodeEnum.FINANCE_CERTIFICATE_VOUCHER_REPEATING_ERROR);
            }
            updateById(certificate);
        }
        LambdaQueryWrapper<FinanceCertificateDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceCertificateDetail::getCertificateId, certificate.getCertificateId())
                .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId());
        financeCertificateDetailService.remove(wrapper);
        if (financeCertificateBO.getCertificateDetails().size() > 0) {
            for (FinanceCertificateDetail certificateDetail : financeCertificateBO.getCertificateDetails()) {
                certificateDetail.setCertificateId(certificate.getCertificateId());
                certificateDetail.setAccountId(AccountSet.getAccountSetId());

                //根据凭证详情id删除凭证详情关联标签类型表数据
                associationService.lambdaUpdate().eq(FinanceCertificateAssociation::getDetailId, certificateDetail.getDetailId()).remove();
                //保存凭证详情关联标签类型表
                if (ObjectUtil.isNotNull(certificateDetail.getAssociationBOS()) && certificateDetail.getAssociationBOS().size() > 0) {
                    List<FinanceAssistAdjuvant> assistAdjuvants = new ArrayList<>();
                    for (FinanceCertificateAssociation association : certificateDetail.getAssociationBOS()) {
                        FinanceAssistAdjuvant adjuvant = new FinanceAssistAdjuvant();
                        adjuvant.setLabel(association.getLabel());
                        adjuvant.setRelationId(association.getRelationId());
                        adjuvant.setAdjuvantId(association.getAdjuvantId());
                        assistAdjuvants.add(adjuvant);
                    }
                    FinanceAssist assist = financeAssistService.saveAndUpdate(certificateDetail.getSubjectId(), assistAdjuvants);
                    certificateDetail.setAssistId(assist.getAssistId());
                }
                financeCertificateDetailService.save(certificateDetail);

                //保存凭证详情关联类型表
                if (ObjectUtil.isNotNull(certificateDetail.getAssociationBOS()) && certificateDetail.getAssociationBOS().size() > 0) {
                    saveAssociation(certificateDetail.getDetailId(), certificateDetail.getAssociationBOS());
                }
            }
        }
        return certificate;
    }

    /**
     * 保存凭证详情关联表
     *
     * @param detailId
     * @param associationBOS
     */
    private void saveAssociation(Long detailId, List<FinanceCertificateAssociation> associationBOS) {
        List<FinanceCertificateAssociation> list = new ArrayList<>();
        associationBOS.forEach(ass -> {
            FinanceCertificateAssociation tion = new FinanceCertificateAssociation();
            tion.setAccountId(AccountSet.getAccountSetId());
            tion.setLabel(ass.getLabel());
            tion.setLabelName(ass.getLabelName());
            tion.setRelationId(ass.getRelationId());
            tion.setDetailId(detailId);
            tion.setAdjuvantId(ass.getAdjuvantId());
            list.add(tion);
        });
        associationService.saveBatch(list, Const.BATCH_SAVE_SIZE);
    }

    @Override
    public BasePage<FinanceCertificateVO> queryPage(FinanceSearchCertificateBO searchCertificateBO) {
        BasePage<FinanceCertificateVO> page = new BasePage<>(searchCertificateBO.getPage(), searchCertificateBO.getLimit());
        searchCertificateBO.setAccountId(AccountSet.getAccountSetId());
        List<Long> certificateIds = financeCertificateDetailService.queryIdsByCondition(searchCertificateBO);
        if (certificateIds.size() == 0) {
            certificateIds.add(0L);
        }

        if (CollUtil.isEmpty(searchCertificateBO.getCertificateIds())) {
            searchCertificateBO.setCertificateIds(certificateIds);
        }
        BasePage<FinanceCertificateVO> certificatePage = getBaseMapper().pageCallRecordList(page, searchCertificateBO);
        AdminFileService fileService = ApplicationContextHolder.getBean(AdminFileService.class);
        certificatePage.getList().forEach(financeCertificateVO -> {
            List<FinanceCertificateDetail> details = baseMapper.queryCertificateDetailById(financeCertificateVO);
            BigDecimal total = new BigDecimal("0");
            for (FinanceCertificateDetail detail : details) {
                List<FinanceCertificateAssociation> associations = associationService.lambdaQuery()
                        .eq(FinanceCertificateAssociation::getDetailId, detail.getDetailId()).list();
                for (FinanceCertificateAssociation association : associations) {
                    FinanceAdjuvantCarte carte = carteService.getById(association.getRelationId());
                    if (ObjectUtil.isNotEmpty(carte)) {
                        association.setName(carte.getCarteName());
                        association.setLabelName(AdjuvantTypeEnum.parse(association.getLabel()).getRemarks());
                        association.setCarteNumber(carte.getCarteNumber());
                        association.setSpecification(carte.getSpecification());
                        association.setUnit(carte.getUnit());
                    }
                }
                detail.setAssociationBOS(associations);
                if (detail.getCreditBalance() != null) {
                    total = total.add(detail.getCreditBalance());
                }
            }
            if (StrUtil.isEmpty(financeCertificateVO.getTotal())) {
                financeCertificateVO.setTotal(total.toString());
            }
            financeCertificateVO.setCertificateDetails(details);
            financeCertificateVO.setCreateUserName(UserCacheUtil.getUserName(financeCertificateVO.getCreateUserId()));
            financeCertificateVO.setExamineUserName(UserCacheUtil.getUserName(financeCertificateVO.getExamineUserId()));
            if (financeCertificateVO.getBatchId() != null) {
                List<FileEntity> fileEntityList = fileService.queryFileList(financeCertificateVO.getBatchId());
                financeCertificateVO.setFileEntityList(fileEntityList);
            }
            if (financeCertificateVO.getExamineUserId() != null) {
                financeCertificateVO.setExamineUserName(UserCacheUtil.getUserName(financeCertificateVO.getExamineUserId()));
            }

        });
        return certificatePage;
    }

    @Override
    public List<OperationLog> updateCheckStatusByIds(List<Long> ids, Integer status) {
        Integer flag = getBaseMapper().queryBalanceByIds(ids, AccountSet.getAccountSetId());
        if (flag == 0) {
            throw new CrmException(FinanceCodeEnum.FINANCE_CERTIFICATE_UPDATE_STATUS_ERROR);
        }
        lambdaUpdate().set(FinanceCertificate::getCheckStatus, status).set(FinanceCertificate::getExamineUserId, UserUtil.getUserId())
                .in(FinanceCertificate::getCertificateId, ids).update();

        BehaviorEnum behaviorEnum = status == 1 ? BehaviorEnum.EXAMINE : BehaviorEnum.REVERSE_EXAMINE;

        List<OperationLog> operationLogs = new ArrayList<>();
        List<FinanceCertificate> financeCertificates = listByIds(ids);
        for (FinanceCertificate certificate : financeCertificates) {
            FinanceVoucher voucher = financeVoucherService.getById(certificate.getVoucherId());

            OperationLog operationLog = new OperationLog();
            operationLog.setOperationObject(LocalDateTimeUtil.format(certificate.getCertificateTime(), DatePattern.NORM_DATE_PATTERN) + " " + voucher.getVoucherName() + "_" + certificate.getCertificateNum());
            operationLog.setOperationInfo(StrUtil.format("{}凭证：{}", behaviorEnum.getName(), operationLog.getOperationObject()));
            operationLog.setBehavior(behaviorEnum);
            operationLogs.add(operationLog);
        }
        return operationLogs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OperationLog> deleteByIds(List<Long> ids) {
        List<FinanceCertificate> certificates = lambdaQuery().in(FinanceCertificate::getCertificateId, ids).list();
        if (!certificates.isEmpty()) {
            long count = certificates.stream().filter(certificate -> certificate.getCheckStatus() == 1).count();
            if (count > 0) {
                throw new CrmException(FinanceCodeEnum.FINANCE_CERTIFICATE_DELETE_ERROR);
            }
        }
        List<OperationLog> operationLogs = new ArrayList<>();
        for (FinanceCertificate certificate : certificates) {

            FinanceVoucher voucher = financeVoucherService.getById(certificate.getVoucherId());

            OperationLog operationLog = new OperationLog();
            operationLog.setOperationObject(LocalDateTimeUtil.format(certificate.getCertificateTime(), DatePattern.NORM_DATE_PATTERN) + " " + voucher.getVoucherName() + "_" + certificate.getCertificateNum());
            operationLog.setOperationInfo(StrUtil.format("{}凭证：{}", BehaviorEnum.DELETE.getName(), operationLog.getOperationObject()));
            operationLogs.add(operationLog);
        }

        removeByIds(ids);
        //删除凭证详情
        ids.forEach(id -> {
            List<FinanceCertificateDetail> detailList = financeCertificateDetailService.lambdaQuery()
                    .eq(FinanceCertificateDetail::getCertificateId, id).list();
            List<Long> detailIds = detailList.stream().map(FinanceCertificateDetail::getDetailId).collect(Collectors.toList());
            associationService.lambdaUpdate().in(FinanceCertificateAssociation::getDetailId, detailIds).remove();
            //删除凭证表详情根据凭证id
            financeCertificateDetailService.removeByIds(detailIds);
            List<Long> statementCertificateIds = financeStatementCertificateService.lambdaQuery()
                    .eq(FinanceStatementCertificate::getCertificateId, id).list()
                    .stream().map(FinanceStatementCertificate::getStatementCertificateId).collect(Collectors.toList());
            financeStatementCertificateService.removeByIds(statementCertificateIds);

            List<Long> assistIds = detailList.stream().map(FinanceCertificateDetail::getAssistId).filter(Objects::nonNull).collect(Collectors.toList());
            if(!assistIds.isEmpty()) {
                //删除辅助核算
                financeAssistService.lambdaUpdate().in(FinanceAssist::getAssistId, assistIds).remove();
                financeAssistAdjuvantService.lambdaUpdate().in(FinanceAssistAdjuvant::getAssistId, assistIds).remove();
            }
        });
        return operationLogs;
    }

    @Override
    public List<JSONObject> queryListCollect(FinanceCollectCertificateBO certificateBO) {
        //获取生成凭证的科目id
        certificateBO.setAccountId(AccountSet.getAccountSetId());
        List<Long> subjectIds = getBaseMapper().querySubjectIds2(certificateBO);
        if (subjectIds.size() == 0) {
            subjectIds.add(0L);
        }
        certificateBO.setSubjectIds(subjectIds);
        List<JSONObject> list = financeSubjectService.queryListCollect(certificateBO);
        list.forEach(jsonObject -> {
            List<Long> ids = jsonObject.getObject("subjectIds", List.class);
            JSONObject json = getBaseMapper().queryBalanceBySubjectIds(ids, subjectIds, AccountSet.getAccountSetId(), certificateBO);
            jsonObject.put("debtorBalances", json.get("debtorBalances"));
            jsonObject.put("creditBalances", json.get("creditBalances"));
        });
        return list;
    }

    @Override
    public FinanceCertificateVO queryById(Long certificateId) {
        FinanceCertificate certificate = getById(certificateId);
        FinanceCertificateVO certificateVO = BeanUtil.copyProperties(certificate, FinanceCertificateVO.class);
        FinanceStatementCertificate statementCertificate = financeStatementCertificateService.lambdaQuery()
                .eq(FinanceStatementCertificate::getCertificateId, certificate.getCertificateId())
                .one();
        if (statementCertificate != null) {
            FinanceStatement statement = financeStatementService.getById(statementCertificate.getStatementId());
            if (statement != null) {
                certificateVO.setStatementType(statement.getStatementType());
            }
        }
        List<FinanceCertificateDetail> details = financeCertificateDetailService.lambdaQuery()
                .eq(FinanceCertificateDetail::getCertificateId, certificate.getCertificateId())
                .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId())
                .orderByAsc(FinanceCertificateDetail::getSort).list();

        //查询凭证详情关联类型表根据详情id
        if (ObjectUtil.isNotEmpty(details) && details.size() > 0) {
            details.forEach(deta -> {
                List<FinanceCertificateAssociation> associations = associationService.lambdaQuery().eq(FinanceCertificateAssociation::getDetailId, deta.getDetailId()).list();
                if (ObjectUtil.isNotEmpty(associations) && associations.size() > 0) {
                    //查询相关数据名称
                    associations.forEach(ass -> {
                        FinanceAdjuvantCarte carte = carteService.getById(ass.getRelationId());
                        if (ObjectUtil.isNotEmpty(carte)) {
                            ass.setName(carte.getCarteName());
                            ass.setLabelName(AdjuvantTypeEnum.parse(ass.getLabel()).getRemarks());
                            ass.setCarteNumber(carte.getCarteNumber());
                            ass.setSpecification(carte.getSpecification());
                            ass.setUnit(carte.getUnit());
                        }
                    });
                    deta.setAssociationBOS(associations);
                }
            });
        }
        certificateVO.setCertificateDetails(details);
        certificateVO.setCreateUserName(UserCacheUtil.getUserName(certificate.getCreateUserId()));
        certificateVO.setExamineUserName(UserCacheUtil.getUserName(certificate.getExamineUserId()));
        FinanceStatementSettle statementSettle =
                financeStatementSettleService.getByPeriod(DateUtil.format(certificate.getCertificateTime(), "yyyyMM"), AccountSet.getAccountSetId());
        if (statementSettle != null) {
            certificateVO.setIsStatement(1);
        } else {
            certificateVO.setIsStatement(0);
        }
        return certificateVO;
    }

    @Override
    public List<JSONObject> queryDetailAccount(FinanceDetailAccountBO accountBO) {
        List<JSONObject> result = new ArrayList<>();
        if (ObjectUtil.isNull(accountBO.getSubjectId())) {
            return result;
        }
        FinanceSubject subject = financeSubjectService.getById(accountBO.getSubjectId());
        if (ObjectUtil.isNull(subject)) {
            throw new CrmException(FinanceCodeEnum.FINANCE_SUBJECT_NOT_FOUND_ERROR);
        }
        accountBO.setAccountId(AccountSet.getAccountSetId());
        accountBO.setBalanceDirection(subject.getBalanceDirection());
        accountBO.setIsBalanceDirection(subject.getBalanceDirection());
        //获取符合条件的科目id
        List<Long> subjectIds = financeSubjectService.queryIds(accountBO.getSubjectId(), accountBO.getMinLevel(), accountBO.getMaxLevel());
        BiParams biParams = new BiParams();
        biParams.setStartTime(accountBO.getStartTime());
        biParams.setEndTime(accountBO.getEndTime());
        BiTimeUtil.BiTimeEntity record = BiTimeUtil.analyzeTypeTime(biParams);
        accountBO.setStartTime(DateUtil.formatDate(record.getBeginDate()));
        accountBO.setEndTime(DateUtil.formatDate(record.getEndDate()));
        Integer beginTime = record.getBeginTime();
        Integer cycleNum = record.getCycleNum();
        List<Integer> timeList = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            timeList.add(beginTime);
            beginTime = BiTimeUtil.estimateTime(beginTime);
        }
        accountBO.setTimeList(timeList);
        // List<Integer> ids = financeInitialService.querySubjectIds();
        // subjectIds.addAll(ids);
        if (subjectIds.size() == 0) {
            subjectIds.add(0L);
        }
        accountBO.setSubjectIds(subjectIds);
        JSONObject paramnter = parameterService.queryParameter();
        if (paramnter != null) {
            accountBO.setAccountBookDirection(paramnter.getInteger("accountBookDirection"));
        } else {
            accountBO.setAccountBookDirection(1);
        }
        List<JSONObject> detailAccounts = getBaseMapper().queryDetailAccountBySubjectIds(accountBO);
        calculate(detailAccounts, accountBO);
        if (CollectionUtil.isNotEmpty(detailAccounts)) {
            for (JSONObject jsonObject : detailAccounts) {
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("digestContent_resourceKey", "finance.subject.digestContent." + jsonObject.get("digestContent"));
                keyMap.put("balanceDirection_resourceKey", "finance.subject.balanceDirection." + jsonObject.get("balanceDirection"));
                jsonObject.put("languageKeyMap", keyMap);
            }
        }
        return detailAccounts;
    }

    @Override
    public List<JSONObject> queryDetailUpAccount(FinanceDetailAccountBO accountBO) {
        FinanceCollectCertificateBO certificateBO = new FinanceCollectCertificateBO();
        certificateBO.setStartTime(accountBO.getStartTime());
        certificateBO.setEndTime(accountBO.getEndTime());
        certificateBO.setAccountId(AccountSet.getAccountSetId());
        accountBO.setAccountId(AccountSet.getAccountSetId());
        List<JSONObject> subjects = financeSubjectService.queryIds(accountBO);
        BiParams biParams = new BiParams();
        biParams.setStartTime(accountBO.getStartTime());
        biParams.setEndTime(accountBO.getEndTime());
        BiTimeUtil.BiTimeEntity record = BiTimeUtil.analyzeTypeTime(biParams);
        accountBO.setStartTime(DateUtil.formatDate(record.getBeginDate()));
        accountBO.setEndTime(DateUtil.formatDate(record.getEndDate()));
        Integer beginTime = record.getBeginTime();
        Integer cycleNum = record.getCycleNum();
        List<Integer> timeList = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            timeList.add(beginTime);
            beginTime = BiTimeUtil.estimateTime(beginTime);
        }
        accountBO.setTimeList(timeList);
        List<Long> subjectIdList = getBaseMapper().querySubjectIdsByTime(certificateBO);
        JSONObject paramnter = parameterService.queryParameter();
        if (paramnter != null) {
            accountBO.setAccountBookDirection(paramnter.getInteger("accountBookDirection"));
        } else {
            accountBO.setAccountBookDirection(1);
        }
        List<JSONObject> subjects1 = new ArrayList<>();
        for (JSONObject json : subjects) {
            List<Long> subjectIds = json.getJSONArray("subjectIds").toJavaList(Long.class);
            subjectIds.add(json.getLong("subjectId"));
            if (!Collections.disjoint(subjectIdList, subjectIds)) {
                accountBO.setSubjectIds(subjectIds);
                accountBO.setSubjectId(json.getLong("subjectId"));
                accountBO.setBalanceDirection(json.getInteger("balanceDirection"));
                List<JSONObject> accountUp = getBaseMapper().queryDetailUpAccount(accountBO);
                for (JSONObject jsonObject : accountUp) {
                    jsonObject.put("subjectName", json.get("subjectName"));
                    jsonObject.put("number", json.get("subjectNumber"));
                    jsonObject.put("subjectId", json.get("subjectId"));
                    Map<String, String> keyMap = new HashMap<>();
                    keyMap.put("subjectName_resourceKey", "finance.subject." + json.get("subjectName"));
                    keyMap.put("digestContent_resourceKey", "finance.subject.digestContent." + jsonObject.get("digestContent"));
                    keyMap.put("balanceDirection_resourceKey", "finance.subject.balanceDirection." + jsonObject.get("balanceDirection"));
                    jsonObject.put("languageKeyMap", keyMap);
                    subjects1.add(jsonObject);
                }
            }
        }
        return subjects1;
    }

    private void filterByNumber(List<JSONObject> result, List<JSONObject> reports, String minNumberStr, String maxNumberStr, String rule) {
        for (JSONObject report : reports) {
            String numberStr = report.getString("number");
            if (numberStr == null) {
                continue;
            }
            BigDecimal number = RuleUtils.fillUpNumber(numberStr, rule);
            Boolean flag = true;
            if (ObjectUtil.isNotNull(minNumberStr)) {
                if (number.compareTo(RuleUtils.fillUpNumber(minNumberStr, rule)) < 0) {
                    flag = false;
                    continue;
                }
            }
            if (ObjectUtil.isNotNull(maxNumberStr)) {
                if (number.compareTo(RuleUtils.fillUpMaxNumber(maxNumberStr, rule)) > 0) {
                    flag = false;
                    continue;
                }
            }
            JSONArray sub = report.getJSONArray("subjects");
            List<JSONObject> subResult = new ArrayList<>();
            if (CollUtil.isNotEmpty(sub)) {
                List<JSONObject> subList = sub.toJavaList(JSONObject.class);
                this.filterByNumber(subResult, subList, minNumberStr, maxNumberStr, rule);
                report.put("subjects", subResult);
            }
            if (flag) {
                result.add(report);
            } else {
                if (CollUtil.isNotEmpty(subResult)) {
                    result.addAll(subResult);
                }
            }
        }
    }

    private void filterByMinGrade(List<JSONObject> result, List<JSONObject> reports, Integer minLevel) {
        if (ObjectUtil.isNotNull(minLevel)) {
            for (JSONObject report : reports) {
                Integer grade = report.getInteger("grade");
                if (ObjectUtil.equal(grade, minLevel)) {
                    result.add(report);
                } else {
                    JSONArray sub = report.getJSONArray("subjects");
                    if (CollUtil.isNotEmpty(sub)) {
                        this.filterByMinGrade(result, sub.toJavaList(JSONObject.class), minLevel);
                    }
                }
            }
        } else {
            result.addAll(reports);
        }
    }

    private List<JSONObject> filterByMaxGrade(List<JSONObject> reports, Integer maxLevel) {
        List<JSONObject> result = new ArrayList<>();
        if (ObjectUtil.isNotNull(maxLevel)) {
            for (JSONObject report : reports) {
                this.filterByMaxGrade(report, maxLevel);
                if (ObjectUtil.isNotNull(report)) {
                    result.add(report);
                }
            }
        } else {
            result = reports;
        }
        return result;
    }

    private JSONObject filterByMaxGrade(JSONObject report, Integer maxLevel) {
        Integer grade = report.getInteger("grade");
        if (grade > maxLevel) {
            report = null;
        } else {
            JSONArray sub = report.getJSONArray("subjects");
            List<JSONObject> newSub = new ArrayList<>();
            if (CollUtil.isNotEmpty(sub)) {
                List<JSONObject> subList = sub.toJavaList(JSONObject.class);
                for (JSONObject o : subList) {
                    o = this.filterByMaxGrade(o, maxLevel);
                    if (ObjectUtil.isNotNull(o)) {
                        newSub.add(o);
                    }
                }
            }
            report.put("subjects", newSub);
        }
        return report;
    }

    @Override
    public List<JSONObject> querySubjectBalance(FinanceSubjectBalanceQueryBO queryBO) {
        // 所有科目
        List<FinanceSubject> subjects = subjectService.queryAll();
        if (CollUtil.isEmpty(subjects)) {
            return Collections.emptyList();
        }
        String fromPeriod = queryBO.getStartTime();
        String toPeriod = queryBO.getEndTime();
        // 账套初始账期
        String startPeriod = parameterService.getStartPeriod();
        // 年初始账期
        String yearInitialPeriod = parameterService.getYearInitialPeriod(queryBO.getStartTime());
        if (ObjectUtil.isNull(yearInitialPeriod)) {
            return Collections.emptyList();
        }
        Long accountSetId = AccountSet.getAccountSetId();
        // 辅助核算的科目列表
        List<JSONObject> subjectAssist = subjectMapper.querySubjectWithAdjuvantAndCart(accountSetId);
        // 截止到当前账期的所有凭证明细
        List<JSONObject> certificateDetailList = baseMapper.listByPeriod(accountSetId, startPeriod, toPeriod);
        // 当前账套的所有科目的初始余额
        List<JSONObject> initialWithAssist = initialMapper.listInitialWithAssist(accountSetId);
        // 当前账套的所有科目的初始余额-非辅助核算
        List<JSONObject> initialNotAssist = initialWithAssist.stream()
                .filter(i -> !i.getBoolean("isAssist")).collect(Collectors.toList());
        // 当前账套的所有科目的初始余额-辅助核算
        List<JSONObject> initialAssist = initialWithAssist.stream()
                .filter(i -> i.getBoolean("isAssist")).collect(Collectors.toList());
        try {
            CashFlowStatementReportHolder.setSubjects(subjects);
            // 处理非辅助核算科目
            List<JSONObject> balanceResult = this.calculateBalance(certificateDetailList, initialNotAssist, fromPeriod);
            // 处理辅助核算科目
            List<JSONObject> assistBalanceResult = this.calculateAssistBalance(certificateDetailList, initialAssist, subjectAssist, fromPeriod);
            this.buildSubjectBalanceResultWithAssist(balanceResult, assistBalanceResult);
            return this.filterSubjectBalance(balanceResult, queryBO);
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }

    private List<JSONObject> filterSubjectBalance(List<JSONObject> balanceResult,
            FinanceSubjectBalanceQueryBO queryBO) {
        FinanceSubject startSubject = null;
        FinanceSubject endSubject = null;
        if (ObjectUtil.isNotNull(queryBO.getStartSubjectId())) {
            startSubject = CashFlowStatementReportHolder.getSubjects(queryBO.getStartSubjectId());
        }
        if (ObjectUtil.isNotNull(queryBO.getStartSubjectId())) {
            endSubject = CashFlowStatementReportHolder.getSubjects(queryBO.getEndSubjectId());
        }
        List<JSONObject> filterResult = new ArrayList<>();
        this.filterByMinGrade(filterResult, balanceResult, queryBO.getMinLevel());
        filterResult = this.filterByMaxGrade(filterResult, queryBO.getMaxLevel());
        List<JSONObject> result = new ArrayList<>();
        JSONObject parameter = parameterService.queryParameter();
        String rule = parameter.getString("rule");
        this.filterByNumber(result, filterResult,
                Optional.ofNullable(startSubject).map(FinanceSubject::getNumber).orElse(null),
                Optional.ofNullable(endSubject).map(FinanceSubject::getNumber).orElse(null), rule);
        //设置多语言
        if (CollectionUtil.isNotEmpty(result)) {
            for (JSONObject jsonObject : result) {
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("subjectName_resourceKey", "finance.subject." + jsonObject.get("subjectName"));
                jsonObject.put("languageKeyMap", keyMap);
            }
        }
        return result;
    }

    /**
     * 计算辅助项科目余额结果
     *
     * @param certificateDetailList 截止到当前账期的所有凭证明细
     * @param initialAssist         当前账套的所有科目的初始余额-辅助核算
     * @param subjectAssist         辅助核算的科目列表
     * @param fromPeriod            起始账期
     * @return
     */
    private List<JSONObject> calculateAssistBalance(List<JSONObject> certificateDetailList,
                                                    List<JSONObject> initialAssist,
                                                    List<JSONObject> subjectAssist,
                                                    String fromPeriod
                                                    ) {
        // 当前账期的上个月
        String lastMonth = PeriodUtils.previousPeriod(fromPeriod, -1);
        // 筛选辅助核算所有凭证明细
        List<JSONObject> assistCertificateDetail = certificateDetailList.stream()
                .filter(o -> ObjectUtil.isNotNull(o.getLong("assistId"))).collect(Collectors.toList());
        Set<Long> assistIdList = certificateDetailList.stream().map(o -> o.getLong("assistId")).collect(Collectors.toSet());
        subjectAssist = subjectAssist.stream()
                .filter(o -> CollUtil.contains(assistIdList, o.getLong("assistId")))
                .collect(Collectors.toList());

        // region 期初 - 辅助项
        List<JSONObject> assistCertificateDetailBefore = assistCertificateDetail.stream()
                .filter(o -> o.getInteger("period") <= Integer.parseInt(lastMonth)).collect(Collectors.toList());
        List<JSONObject> assistInitialBalance = this.calculateAssistEndBalance(assistCertificateDetailBefore, initialAssist, subjectAssist);
        // endregion
        // region 本期 - 辅助项
        List<JSONObject> assistCertificateDetailCurrent = assistCertificateDetail.stream()
                .filter(o -> o.getInteger("period") >= Integer.parseInt(fromPeriod))
                .collect(Collectors.toList());
        List<JSONObject> assistCurrentBalance = this.calculateAssistCurrentBalance(assistCertificateDetailCurrent, subjectAssist);
        // endregion
        // region 本年累计- 辅助项
        List<JSONObject> assistYearBalance = this.calculateAssistYearBalance(assistCertificateDetail, initialAssist, subjectAssist);
        // endregion
        // region 期末- 辅助项
        List<JSONObject> assistEndBalance = this.calculateAssistEndBalance(assistCertificateDetail, initialAssist, subjectAssist);
        // endregion
        return this.buildAssistSubjectBalanceResult(subjectAssist, assistInitialBalance, assistCurrentBalance, assistYearBalance, assistEndBalance);
    }

    /**
     * 计算科目余额结果
     *
     * @param certificateDetailList 截止到当前账期的所有凭证明细
     * @param initialNotAssist      当前账套的所有科目的初始余额-非辅助核算
     * @param fromPeriod            起始账期
     * @return
     */
    private List<JSONObject> calculateBalance(List<JSONObject> certificateDetailList,
                                              List<JSONObject> initialNotAssist,
                                              String fromPeriod) {
        List<FinanceSubject> subjects = CashFlowStatementReportHolder.getSubjects();
        // 当前账期的上个月
        String lastMonth = PeriodUtils.previousPeriod(fromPeriod, -1);
        // region 期初 = 上期的期末
        // 筛选上个月之前的所有凭证明细
        List<JSONObject> certificateDetailBefore = certificateDetailList.stream()
                .filter(o -> o.getInteger("period") <= Integer.parseInt(lastMonth)).collect(Collectors.toList());
        List<JSONObject> initialBalance = this.calculateEndBalance(certificateDetailBefore, initialNotAssist);
        // endregion
        // region 本期 = 账期内所有发生额累计
        // 筛选所选择账期内的所有凭证明细
        List<JSONObject> certificateDetailCurrent = certificateDetailList.stream()
                .filter(o -> o.getInteger("period") >= Integer.parseInt(fromPeriod))
                .collect(Collectors.toList());
        List<JSONObject> currentBalance = this.calculateCurrentBalance(certificateDetailCurrent);
        // endregion
        // region 本年累计 = 截止到当前账期所有发生额累计 + 初始余额
        List<JSONObject> yearBalance = this.calculateYearBalance(certificateDetailList, initialNotAssist);
        // endregion
        // region 期末 = 期初+本期 || 初始 + 截止到当前账期所有发生额累计
        List<JSONObject> endBalance = this.calculateEndBalance(certificateDetailList, initialNotAssist);
        // endregion
        List<FinanceSubject> subjectsTree = FinanceSubject.listToTree(subjects);
        return this.buildSubjectBalanceResult(subjectsTree, initialBalance, currentBalance, yearBalance, endBalance);
    }

    /**
     * 计算辅助项本期发生额
     *
     * @param assistCertificateDetail 截止到当前账期的辅助科目凭证明细
     * @param subjectAssist           辅助项科目列表
     * @return data
     */
    private List<JSONObject> calculateAssistCurrentBalance(List<JSONObject> assistCertificateDetail,
                                                           List<JSONObject> subjectAssist) {
        Map<Long, List<JSONObject>> certificateDetailGroupByAssistId = assistCertificateDetail.stream()
                .collect(Collectors.groupingBy(o -> MapUtil.getLong(o, "assistId")));
        Map<Long, Long> assistIdSubjectIdMap = subjectAssist.stream()
                .collect(Collectors.toMap(o -> MapUtil.getLong(o, "assistId"), o -> MapUtil.getLong(o, "subjectId"), (o1, o2) -> o1));
        Map<Long, List<JSONObject>> assistIdSubjectListMap = subjectAssist.stream()
                .collect(Collectors.groupingBy(o -> o.getLong("assistId")));
        List<JSONObject> result = new ArrayList<>();
        for (Map.Entry<Long, List<JSONObject>> entry : assistIdSubjectListMap.entrySet()) {
            Long assistId = entry.getKey();
            // 辅助项的科目信息
            List<JSONObject> assistSubjectList = entry.getValue();
            Long subjectId = assistIdSubjectIdMap.get(assistId);
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("balanceDirection", subject.getBalanceDirection());
            object.put("grade", subject.getGrade());
            String subjectName = this.buildAssistNameNumber(assistSubjectList, "carteName");
            String number = this.buildAssistNameNumber(assistSubjectList, "carteNumber");
            subjectName = String.join("_", subject.getSubjectName(), subjectName);
            number = String.join("_", subject.getNumber(), number);
            object.put("subjectName", subjectName);
            object.put("number", number);
            object.put("assistId", assistId);

            BigDecimal debtorCurrentBalance = BigDecimal.ZERO;
            BigDecimal creditCurrentBalance = BigDecimal.ZERO;

            List<JSONObject> detailList = certificateDetailGroupByAssistId.get(assistId);
            if (CollUtil.isNotEmpty(detailList)) {
                debtorCurrentBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                creditCurrentBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            object.put("debtorBalance", debtorCurrentBalance);
            object.put("creditBalance", creditCurrentBalance);
            result.add(object);
        }
        return result;
    }

    /**
     * 计算辅助项的本年累计余额
     *
     * @param assistCertificateDetail 截止到当前账期的辅助科目凭证明细
     * @param initialAssist           当前账套的辅助科目的初始余额
     * @param subjectAssist           辅助项科目列表
     * @return data
     */
    private List<JSONObject> calculateAssistYearBalance(List<JSONObject> assistCertificateDetail,
                                                  List<JSONObject> initialAssist,
                                                  List<JSONObject> subjectAssist) {
        Map<Long, List<JSONObject>> certificateDetailGroupByAssistId = assistCertificateDetail.stream()
                .collect(Collectors.groupingBy(o -> MapUtil.getLong(o, "assistId")));
        Map<Long, Long> assistIdSubjectIdMap = subjectAssist.stream()
                .collect(Collectors.toMap(o -> MapUtil.getLong(o, "assistId"), o -> MapUtil.getLong(o, "subjectId"), (o1, o2) -> o1));
        Map<Long, List<JSONObject>> assistIdSubjectListMap = subjectAssist.stream()
                .collect(Collectors.groupingBy(o -> o.getLong("assistId")));
        List<JSONObject> result = new ArrayList<>();
        for (Map.Entry<Long, List<JSONObject>> entry : assistIdSubjectListMap.entrySet()) {
            Long assistId = entry.getKey();
            // 辅助项的科目信息
            List<JSONObject> assistSubjectList = entry.getValue();
            Long subjectId = assistIdSubjectIdMap.get(assistId);
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("balanceDirection", subject.getBalanceDirection());
            object.put("grade", subject.getGrade());
            String subjectName = this.buildAssistNameNumber(assistSubjectList, "carteName");
            String number = this.buildAssistNameNumber(assistSubjectList, "carteNumber");
            subjectName = String.join("_", subject.getSubjectName(), subjectName);
            number = String.join("_", subject.getNumber(), number);
            object.put("subjectName", subjectName);
            object.put("number", number);
            object.put("assistId", assistId);

            BigDecimal debtorYearBalance = BigDecimal.ZERO;
            BigDecimal creditYearBalance = BigDecimal.ZERO;

            // 获取辅助项的初始余额
            Set<Long> carteIdList = assistSubjectList.stream().map(s -> s.getLong("carteId")).collect(Collectors.toSet());
            List<JSONObject> initialBalanceList = initialAssist.stream()
                    .filter(i -> ObjectUtil.equal(subjectId, i.getLong("subjectId")))
                    .filter(i -> CollUtil.contains(carteIdList, i.getLong("carteId")))
                    .collect(Collectors.toList());


            // 初始本年累计借方
            BigDecimal addUpDebtorBalance = initialBalanceList.stream()
                    .map(i -> i.getBigDecimal("addUpDebtorBalance"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // 初始本年累计贷方
            BigDecimal addUpCreditBalance = initialBalanceList.stream()
                    .map(i -> i.getBigDecimal("addUpCreditBalance"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            debtorYearBalance = debtorYearBalance.add(addUpDebtorBalance);
            creditYearBalance = creditYearBalance.add(addUpCreditBalance);

            List<JSONObject> detailList = certificateDetailGroupByAssistId.get(assistId);
            if (CollUtil.isNotEmpty(detailList)) {
                BigDecimal debtorBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal creditBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                debtorYearBalance = debtorYearBalance.add(debtorBalance);
                creditYearBalance = creditYearBalance.add(creditBalance);
            }
            object.put("debtorBalance", debtorYearBalance);
            object.put("creditBalance", creditYearBalance);
            result.add(object);
        }
        return result;
    }

    /**
     * 计算辅助项的期末余额
     *
     * @param assistCertificateDetail 截止到当前账期的辅助科目凭证明细
     * @param initialAssist           当前账套的辅助科目的初始余额
     * @param subjectAssist           辅助项科目列表
     * @return data
     */
    private List<JSONObject> calculateAssistEndBalance(List<JSONObject> assistCertificateDetail,
                                                       List<JSONObject> initialAssist,
                                                       List<JSONObject> subjectAssist) {
        Map<Long, List<JSONObject>> certificateDetailGroupByAssistId = assistCertificateDetail.stream()
                .collect(Collectors.groupingBy(o -> MapUtil.getLong(o, "assistId")));
        Map<Long, Long> assistIdSubjectIdMap = subjectAssist.stream()
                .collect(Collectors.toMap(o -> MapUtil.getLong(o, "assistId"), o -> MapUtil.getLong(o, "subjectId"), (o1, o2) -> o1));
        Map<Long, List<JSONObject>> assistIdSubjectListMap = subjectAssist.stream()
                .collect(Collectors.groupingBy(o -> o.getLong("assistId")));
        List<JSONObject> result = new ArrayList<>();
        for (Map.Entry<Long, List<JSONObject>> entry : assistIdSubjectListMap.entrySet()) {
            Long assistId = entry.getKey();
            // 辅助项的科目信息
            List<JSONObject> assistSubjectList = entry.getValue();
            Long subjectId = assistIdSubjectIdMap.get(assistId);
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("balanceDirection", subject.getBalanceDirection());
            object.put("grade", subject.getGrade());
            String subjectName = this.buildAssistNameNumber(assistSubjectList, "carteName");
            String number = this.buildAssistNameNumber(assistSubjectList, "carteNumber");
            subjectName = String.join("_", subject.getSubjectName(), subjectName);
            number = String.join("_", subject.getNumber(), number);
            object.put("subjectName", subjectName);
            object.put("number", number);
            object.put("assistId", assistId);

            BigDecimal debtorEndBalance = BigDecimal.ZERO;
            BigDecimal creditEndBalance = BigDecimal.ZERO;

            List<JSONObject> detailList = certificateDetailGroupByAssistId.get(assistId);
            BigDecimal debtorBalance = BigDecimal.ZERO;
            BigDecimal creditBalance = BigDecimal.ZERO;
            if (CollUtil.isNotEmpty(detailList)) {
                debtorBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                creditBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            // 获取辅助项的初始余额
            Set<Long> carteIdList = assistSubjectList.stream().map(s -> s.getLong("carteId")).collect(Collectors.toSet());
            BigDecimal initialBalance = initialAssist.stream()
                    .filter(i -> ObjectUtil.equal(subjectId, i.getLong("subjectId")))
                    .filter(i -> CollUtil.contains(carteIdList, i.getLong("carteId")))
                    .map(i -> i.getBigDecimal("initialBalance"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // 科目方向为借方
            if (1 == subject.getBalanceDirection()) {
                // 借方期末余额
                debtorEndBalance = initialBalance.add(debtorBalance).subtract(creditBalance);
            } else {
                // 贷方期末余额
                creditEndBalance = initialBalance.add(creditBalance).subtract(debtorBalance);
            }
            object.put("debtorBalance", debtorEndBalance);
            object.put("creditBalance", creditEndBalance);
            result.add(object);
        }
        return result;
    }

    /**
     * 计算本期发生额
     * 账期内所有发生额累计
     *
     * @param certificateDetailList 当前账期内的所有凭证明细
     * @return data
     */
    private List<JSONObject> calculateCurrentBalance(List<JSONObject> certificateDetailList) {
        List<FinanceSubject> subjects = CashFlowStatementReportHolder.getSubjects();
        Map<Long, List<JSONObject>> certificateDetailGroupBySubjectId = certificateDetailList.stream()
                .collect(Collectors.groupingBy(o -> MapUtil.getLong(o, "subjectId")));
        List<JSONObject> result = new ArrayList<>();
        for (FinanceSubject subject : subjects) {
            Long subjectId = subject.getSubjectId();
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("subjectName", subject.getSubjectName());
            object.put("number", subject.getNumber());
            object.put("grade", subject.getGrade());
            object.put("balanceDirection", subject.getBalanceDirection());

            BigDecimal debtorCurrentBalance = BigDecimal.ZERO;
            BigDecimal creditCurrentBalance = BigDecimal.ZERO;

            List<JSONObject> detailList = certificateDetailGroupBySubjectId.get(subjectId);
            if (CollUtil.isNotEmpty(detailList)) {
                debtorCurrentBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                creditCurrentBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            object.put("debtorBalance", debtorCurrentBalance);
            object.put("creditBalance", creditCurrentBalance);
            result.add(object);
        }
        return result;
    }


    /**
     * 计算本年累计余额
     * 截止到当前账期所有发生额累计 + 初始余额
     *
     * @param certificateDetailList 本年截止到当前账期的所有凭证明细
     * @param initialWithAssist     当前账套的所有科目的初始余额
     * @return data
     */
    private List<JSONObject> calculateYearBalance(List<JSONObject> certificateDetailList,
                                                  List<JSONObject> initialWithAssist) {
        List<FinanceSubject> subjects = CashFlowStatementReportHolder.getSubjects();
        Map<Long, List<JSONObject>> certificateDetailGroupBySubjectId = certificateDetailList.stream()
                .collect(Collectors.groupingBy(o -> MapUtil.getLong(o, "subjectId")));
        // 非辅助核算科目的初始余额
        Map<Long, JSONObject> subjectIdInitialMap = initialWithAssist.stream()
                .filter(i -> !i.getBoolean("isAssist"))
                .collect(Collectors.toMap(i -> i.getLong("subjectId"), Function.identity()));
        List<JSONObject> result = new ArrayList<>();
        for (FinanceSubject subject : subjects) {
            Long subjectId = subject.getSubjectId();
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("subjectName", subject.getSubjectName());
            object.put("number", subject.getNumber());
            object.put("grade", subject.getGrade());
            object.put("balanceDirection", subject.getBalanceDirection());

            BigDecimal debtorYearBalance = BigDecimal.ZERO;
            BigDecimal creditYearBalance = BigDecimal.ZERO;

            // 初始本年累计借方
            BigDecimal addUpDebtorBalance = Optional.ofNullable(subjectIdInitialMap.get(subjectId))
                    .map(o -> o.getBigDecimal("addUpDebtorBalance"))
                    .orElse(BigDecimal.ZERO);
            // 初始本年累计贷方
            BigDecimal addUpCreditBalance = Optional.ofNullable(subjectIdInitialMap.get(subjectId))
                    .map(o -> o.getBigDecimal("addUpCreditBalance"))
                    .orElse(BigDecimal.ZERO);
            debtorYearBalance = debtorYearBalance.add(addUpDebtorBalance);
            creditYearBalance = creditYearBalance.add(addUpCreditBalance);

            List<JSONObject> detailList = certificateDetailGroupBySubjectId.get(subjectId);
            if (CollUtil.isNotEmpty(detailList)) {
                BigDecimal debtorBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal creditBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                debtorYearBalance = debtorYearBalance.add(debtorBalance);
                creditYearBalance = creditYearBalance.add(creditBalance);
            }
            object.put("debtorBalance", debtorYearBalance);
            object.put("creditBalance", creditYearBalance);
            result.add(object);
        }

        return result;
    }

    /**
     * 计算期末余额
     * 初始 + 截止到当前账期所有发生额累计
     *
     * @param certificateDetailList 截止到当前账期的所有凭证明细
     * @param initialWithAssist     当前账套的所有科目的初始余额
     * @return data
     */
    private List<JSONObject> calculateEndBalance(List<JSONObject> certificateDetailList,
                                                 List<JSONObject> initialWithAssist) {
        List<FinanceSubject> subjects = CashFlowStatementReportHolder.getSubjects();
        Map<Long, List<JSONObject>> certificateDetailGroupBySubjectId = certificateDetailList.stream()
                .collect(Collectors.groupingBy(o -> MapUtil.getLong(o, "subjectId")));
        // 非辅助核算科目的初始余额
        Map<Long, BigDecimal> subjectIdInitialBalanceMap = initialWithAssist.stream()
                .collect(Collectors.toMap(i -> i.getLong("subjectId"), i -> i.getBigDecimal("initialBalance")));
        List<JSONObject> result = new ArrayList<>();
        for (FinanceSubject subject : subjects) {
            Long subjectId = subject.getSubjectId();
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("subjectName", subject.getSubjectName());
            object.put("number", subject.getNumber());
            object.put("grade", subject.getGrade());
            object.put("balanceDirection", subject.getBalanceDirection());

            BigDecimal debtorEndBalance = BigDecimal.ZERO;
            BigDecimal creditEndBalance = BigDecimal.ZERO;

            BigDecimal initialBalance = Optional.ofNullable(subjectIdInitialBalanceMap.get(subjectId)).orElse(BigDecimal.ZERO);
            List<JSONObject> detailList = certificateDetailGroupBySubjectId.get(subjectId);
            BigDecimal debtorBalance = BigDecimal.ZERO;
            BigDecimal creditBalance = BigDecimal.ZERO;
            if (CollUtil.isNotEmpty(detailList)) {
                debtorBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                creditBalance = detailList.stream().map(o ->
                                Optional.ofNullable(o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            // 科目方向为借方
            if (1 == subject.getBalanceDirection()) {
                // 借方期末余额
                debtorEndBalance = initialBalance.add(debtorBalance).subtract(creditBalance);
            } else {
                // 贷方期末余额
                creditEndBalance = initialBalance.add(creditBalance).subtract(debtorBalance);
            }
            object.put("debtorBalance", debtorEndBalance);
            object.put("creditBalance", creditEndBalance);
            result.add(object);
        }
        return result;
    }

    /**
     * 构造辅助项余额结果集
     *
     * @param subjectAssist  辅助项的科目信息
     * @param initialBalance 科目期初余额列表
     * @param currentBalance 科目本期发生额列表
     * @param yearBalance    科目本年累计列表
     * @param endBalance     科目期末余额列表
     * @return data
     */
    private List<JSONObject> buildAssistSubjectBalanceResult(List<JSONObject> subjectAssist,
                                                             List<JSONObject> initialBalance,
                                                             List<JSONObject> currentBalance,
                                                             List<JSONObject> yearBalance,
                                                             List<JSONObject> endBalance
    ) {
        List<JSONObject> result = new ArrayList<>();
        Map<Long, Long> assistIdSubjectIdMap = subjectAssist.stream()
                .collect(Collectors.toMap(o -> MapUtil.getLong(o, "assistId"), o -> MapUtil.getLong(o, "subjectId"), (o1, o2) -> o1));
        Map<Long, List<JSONObject>> assistIdSubjectListMap = subjectAssist.stream()
                .collect(Collectors.groupingBy(o -> o.getLong("assistId")));
        for (Map.Entry<Long, List<JSONObject>> entry : assistIdSubjectListMap.entrySet()) {
            Long assistId = entry.getKey();
            // 辅助项的科目信息
            List<JSONObject> assistSubjectList = entry.getValue();
            Long subjectId = assistIdSubjectIdMap.get(assistId);
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("balanceDirection", subject.getBalanceDirection());
            object.put("grade", subject.getGrade());
            String subjectName = this.buildAssistNameNumber(assistSubjectList, "carteName");
            String number = this.buildAssistNameNumber(assistSubjectList, "carteNumber");
            subjectName = String.join("_", subject.getSubjectName(), subjectName);
            number = String.join("_", subject.getNumber(), number);
            object.put("subjectName", subjectName);
            object.put("number", number);

            // 计算子科目的科目余额
            JSONObject initial = this.filterBalanceBySubjectIdAndAssistId(initialBalance, subjectId, assistId);
            JSONObject current = this.filterBalanceBySubjectIdAndAssistId(currentBalance, subjectId, assistId);
            JSONObject year = this.filterBalanceBySubjectIdAndAssistId(yearBalance, subjectId, assistId);
            JSONObject end = this.filterBalanceBySubjectIdAndAssistId(endBalance, subjectId, assistId);

            BigDecimal debtorInitialBalance = Optional.ofNullable(initial).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
            BigDecimal creditInitialBalance = Optional.ofNullable(initial).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);
            BigDecimal debtorCurrentBalance = Optional.ofNullable(current).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
            BigDecimal creditCurrentBalance = Optional.ofNullable(current).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);
            BigDecimal debtorYearBalance = Optional.ofNullable(year).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
            BigDecimal creditYearBalance = Optional.ofNullable(year).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);
            BigDecimal debtorEndBalance = Optional.ofNullable(end).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
            BigDecimal creditEndBalance = Optional.ofNullable(end).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);

            // 过滤没有任何金额的科目余额
            if (debtorInitialBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditInitialBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    debtorCurrentBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditCurrentBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    debtorEndBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditEndBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    debtorYearBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditYearBalance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            object.put("debtorInitialBalance", debtorInitialBalance);
            object.put("creditInitialBalance", creditInitialBalance);
            object.put("debtorCurrentBalance", debtorCurrentBalance);
            object.put("creditCurrentBalance", creditCurrentBalance);
            object.put("debtorYearBalance", debtorYearBalance);
            object.put("creditYearBalance", creditYearBalance);
            object.put("debtorEndBalance", debtorEndBalance);
            object.put("creditEndBalance", creditEndBalance);
            result.add(object);
        }
        return result;
    }

    /**
     * 辅助项名称/编号构造
     *
     * @param assistSubjectList 辅助项的科目信息
     * @param fieldName         字段名称
     * @return data
     */
    private String buildAssistNameNumber(List<JSONObject> assistSubjectList, String fieldName) {
        return assistSubjectList.stream().map(o -> o.getString(fieldName)).collect(Collectors.joining("_"));
    }

    /**
     * 构造科目余额结果集-不含辅助项
     *
     * @param subjectTree    科目树列表
     * @param initialBalance 科目期初余额列表
     * @param currentBalance 科目本期发生额列表
     * @param yearBalance    科目本年累计列表
     * @param endBalance     科目期末余额列表
     * @return data
     */
    private List<JSONObject> buildSubjectBalanceResult(List<FinanceSubject> subjectTree,
                                                       List<JSONObject> initialBalance,
                                                       List<JSONObject> currentBalance,
                                                       List<JSONObject> yearBalance,
                                                       List<JSONObject> endBalance) {

        List<JSONObject> result = new ArrayList<>();
        for (FinanceSubject subject : subjectTree) {
            Long subjectId = subject.getSubjectId();
            Integer balanceDirection = subject.getBalanceDirection();
            JSONObject object = new JSONObject();
            object.put("subjectId", subjectId);
            object.put("subjectName", subject.getSubjectName());
            object.put("number", subject.getNumber());
            object.put("balanceDirection", balanceDirection);
            object.put("grade", subject.getGrade());

            BigDecimal debtorInitialBalance = BigDecimal.ZERO;
            BigDecimal creditInitialBalance = BigDecimal.ZERO;
            BigDecimal debtorCurrentBalance = BigDecimal.ZERO;
            BigDecimal creditCurrentBalance = BigDecimal.ZERO;
            BigDecimal debtorYearBalance = BigDecimal.ZERO;
            BigDecimal creditYearBalance = BigDecimal.ZERO;
            BigDecimal debtorEndBalance = BigDecimal.ZERO;
            BigDecimal creditEndBalance = BigDecimal.ZERO;

            List<FinanceSubject> children = subject.getChildren();
            // 计算父级科目的余额
            if (CollUtil.isNotEmpty(children)) {
                List<JSONObject> subResult = this.buildSubjectBalanceResult(children, initialBalance, currentBalance, yearBalance, endBalance);
                object.put("subjects", subResult);
                for (JSONObject sub : subResult) {
                    Integer subBalanceDirection = sub.getInteger("balanceDirection");
                    BigDecimal subDebtorInitialBalance = sub.getBigDecimal("debtorInitialBalance");
                    BigDecimal subCreditInitialBalance = sub.getBigDecimal("creditInitialBalance");
                    BigDecimal subDebtorCurrentBalance = sub.getBigDecimal("debtorCurrentBalance");
                    BigDecimal subCreditCurrentBalance = sub.getBigDecimal("creditCurrentBalance");
                    BigDecimal subDebtorYearBalance = sub.getBigDecimal("debtorYearBalance");
                    BigDecimal subCreditYearBalance = sub.getBigDecimal("creditYearBalance");
                    BigDecimal subDebtorEndBalance = sub.getBigDecimal("debtorEndBalance");
                    BigDecimal subCreditEndBalance = sub.getBigDecimal("creditEndBalance");
                    // 本期
                    debtorCurrentBalance = debtorCurrentBalance.add(subDebtorCurrentBalance);
                    creditCurrentBalance = creditCurrentBalance.add(subCreditCurrentBalance);
                    // 本年
                    debtorYearBalance = debtorYearBalance.add(subDebtorYearBalance);
                    creditYearBalance = creditYearBalance.add(subCreditYearBalance);
                    // 期初&&期末
                    if (ObjectUtil.equal(balanceDirection, subBalanceDirection)) {
                        debtorInitialBalance = debtorInitialBalance.add(subDebtorInitialBalance);
                        creditInitialBalance = creditInitialBalance.add(subCreditInitialBalance);

                        debtorEndBalance = debtorEndBalance.add(subDebtorEndBalance);
                        creditEndBalance = creditEndBalance.add(subCreditEndBalance);

                    } else {
                        debtorInitialBalance = debtorInitialBalance.subtract(subDebtorInitialBalance);
                        creditInitialBalance = creditInitialBalance.subtract(subCreditInitialBalance);

                        debtorEndBalance = debtorEndBalance.subtract(subDebtorEndBalance);
                        creditEndBalance = creditEndBalance.subtract(subCreditEndBalance);
                    }
                    // 借方
                    if (ObjectUtil.equal(1, balanceDirection)) {
                        debtorInitialBalance = debtorInitialBalance.add(creditInitialBalance);
                        creditInitialBalance = BigDecimal.ZERO;

                        debtorEndBalance = debtorEndBalance.add(creditEndBalance);
                        creditEndBalance = BigDecimal.ZERO;
                    } else {
                        creditInitialBalance = debtorInitialBalance.add(creditInitialBalance);
                        debtorInitialBalance = BigDecimal.ZERO;

                        creditEndBalance = debtorEndBalance.add(creditEndBalance);
                        debtorEndBalance = BigDecimal.ZERO;
                    }
                }
            } else {
                // 计算子科目的科目余额
                JSONObject initial = this.filterBalanceBySubjectId(initialBalance, subjectId);
                JSONObject current = this.filterBalanceBySubjectId(currentBalance, subjectId);
                JSONObject year = this.filterBalanceBySubjectId(yearBalance, subjectId);
                JSONObject end = this.filterBalanceBySubjectId(endBalance, subjectId);

                debtorInitialBalance = Optional.ofNullable(initial).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
                creditInitialBalance = Optional.ofNullable(initial).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);
                debtorCurrentBalance = Optional.ofNullable(current).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
                creditCurrentBalance = Optional.ofNullable(current).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);
                debtorYearBalance = Optional.ofNullable(year).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
                creditYearBalance = Optional.ofNullable(year).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);
                debtorEndBalance = Optional.ofNullable(end).map(o -> o.getBigDecimal("debtorBalance")).orElse(BigDecimal.ZERO);
                creditEndBalance = Optional.ofNullable(end).map(o -> o.getBigDecimal("creditBalance")).orElse(BigDecimal.ZERO);
            }
            // 过滤没有任何金额的科目余额
            if (debtorInitialBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditInitialBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    debtorCurrentBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditCurrentBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    debtorEndBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditEndBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    debtorYearBalance.compareTo(BigDecimal.ZERO) == 0 &&
                    creditYearBalance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            object.put("debtorInitialBalance", debtorInitialBalance);
            object.put("creditInitialBalance", creditInitialBalance);
            object.put("debtorCurrentBalance", debtorCurrentBalance);
            object.put("creditCurrentBalance", creditCurrentBalance);
            object.put("debtorYearBalance", debtorYearBalance);
            object.put("creditYearBalance", creditYearBalance);
            object.put("debtorEndBalance", debtorEndBalance);
            object.put("creditEndBalance", creditEndBalance);
            result.add(object);
        }
        return result;
    }

    /**
     * 构建科目余额表结果
     *
     * @param balanceResult       科目余额结果集
     * @param assistBalanceResult 辅助项科目余额
     * @return
     */
    private void buildSubjectBalanceResultWithAssist(List<JSONObject> balanceResult,
                                                     List<JSONObject> assistBalanceResult) {
        Map<Long, List<JSONObject>> assistBalanceGroupBySubjectIdMap = assistBalanceResult.stream().collect(Collectors.groupingBy(r -> r.getLong("subjectId")));
        for (JSONObject balance : balanceResult) {
            Long subjectId = balance.getLong("subjectId");
            if (assistBalanceGroupBySubjectIdMap.containsKey(subjectId)) {
                balance.put("subjects", assistBalanceGroupBySubjectIdMap.get(subjectId));
            }
        }
    }

    /**
     * 根据科目 ID 过滤科目余额
     *
     * @param balanceList 科目余额列表
     * @param subjectId   科目 ID
     * @return data
     */
    private JSONObject filterBalanceBySubjectId(List<JSONObject> balanceList, Long subjectId) {
        if (CollUtil.isNotEmpty(balanceList)) {
            return balanceList.stream()
                    .filter(o -> ObjectUtil.equal(subjectId, o.getLong("subjectId")))
                    .findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 根据科目 ID 过滤科目余额
     *
     * @param balanceList 科目余额列表
     * @param subjectId   科目 ID
     * @param assistId    辅助 ID
     * @return data
     */
    private JSONObject filterBalanceBySubjectIdAndAssistId(List<JSONObject> balanceList, Long subjectId, Long assistId) {
        if (CollUtil.isNotEmpty(balanceList)) {
            return balanceList.stream()
                    .filter(o -> ObjectUtil.equal(subjectId, o.getLong("subjectId")))
                    .filter(o -> ObjectUtil.equal(assistId, o.getLong("assistId")))
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public List<JSONObject> queryOldDetailBalanceAccount(FinanceDetailAccountBO accountBO) {
        JSONObject parameter = parameterService.queryParameter();
        Integer accountBookDirection = parameter.getInteger("accountBookDirection");
        accountBO.setAccountId(AccountSet.getAccountSetId());
        List<JSONObject> amount = baseMapper.queryFinalDetailBalanceAccount(accountBO);
        // 获取所有初始余额
        List<JSONObject> initials = initialService.queryAllWithAssistId(AccountSet.getAccountSetId());
        // 辅助核算的初始余额
        List<JSONObject> initialsAssist = initials.stream()
                .filter(o -> ObjectUtil.isNotNull(o.getBoolean("isAssist")) && o.getBoolean("isAssist"))
                .collect(Collectors.toList());
        // 非辅助核算的初始余额
        List<JSONObject> initialsNotAssist = initials.stream()
                .filter(o -> ObjectUtil.isNotNull(o.getBoolean("isAssist")) && !o.getBoolean("isAssist"))
                .collect(Collectors.toList());
        // 所有未删除的科目
        List<FinanceSubject> subjects = subjectService.lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .list();
        //处理新建账套初始化未完成没有获取到数据
        if (CollUtil.isEmpty(subjects)) {
            return new ArrayList<>();
        }
        Integer firstGrade = subjects.stream().min(Comparator.comparing(FinanceSubject::getGrade)).get().getGrade();
        // 年初始账期
        String yearInitialPeriod = parameterService.getYearInitialPeriod(accountBO.getStartTime());
        // 上个月
        String lastMonth = PeriodUtils.previousPeriod(accountBO.getStartTime(), -1);
        if (Integer.valueOf(lastMonth) < Integer.valueOf(yearInitialPeriod)) {
            lastMonth = yearInitialPeriod;
        }
        // 本月之前的科目余额
        List<JSONObject> currentBalance = baseMapper.queryCurrentBalance(AccountSet.getAccountSetId(), yearInitialPeriod, lastMonth);
        Map<Integer, List<JSONObject>> currentBalanceGroupBySubjectId = currentBalance.stream().collect(Collectors.groupingBy(o -> o.getInteger("subjectId")));
        List<JSONObject> subjectList = new ArrayList<>();
        try {
            CashFlowStatementReportHolder.setSubjects(subjects);
            for (JSONObject jsonObject : amount) {
                // 科目ID
                Long subjectId = jsonObject.getLong("subjectId");
                // 辅助核算ID
                Long assistId = jsonObject.getLong("assistId");
                // 是否是辅助核算
                Boolean isAssist = jsonObject.getBoolean("isAssist");
                // 科目方向
                Integer balanceDirection = jsonObject.getInteger("balanceDirection");
                // 借方期初始余额
                BigDecimal debtorInitialBalance = new BigDecimal(0);
                // 贷方期初始余额
                BigDecimal creditInitialBalance = new BigDecimal(0);
                // 本期借方发生额
                BigDecimal debtorCurrentBalance = new BigDecimal(0);
                // 本期贷方发生额
                BigDecimal creditCurrentBalance = new BigDecimal(0);
                // 本期年初借方发生额
                BigDecimal debtorLastYearCurrentBalance = new BigDecimal(0);
                // 本期年初贷方发生额
                BigDecimal creditLastYearCurrentBalance = new BigDecimal(0);
                // 借方期末余额
                BigDecimal debtorEndBalance = new BigDecimal(0);
                // 贷方期末余额
                BigDecimal creditEndBalance = new BigDecimal(0);
                // 借方本年累计
                BigDecimal debtorYearBalance = new BigDecimal(0);
                // 贷方本年累计
                BigDecimal creditYearBalance = new BigDecimal(0);
                // 借方年初余额
                BigDecimal debtorInitialYearBalance = new BigDecimal(0);
                // 贷方年初余额
                BigDecimal creditInitialYearBalance = new BigDecimal(0);

                // 借方期初发生额
                BigDecimal debtorLastCurrentBalance = jsonObject.getBigDecimal("debtorLastCurrentBalance");
                // 贷方年初余额
                BigDecimal creditLastCurrentBalance = jsonObject.getBigDecimal("creditLastCurrentBalance");

                JSONObject initial;
                // 获取初始余额
                if (isAssist) {
                    initial = initialsAssist.stream()
                            .filter(o -> ObjectUtil.equal(assistId, o.getLong("assistId"))).findFirst()
                            .orElse(null);
                } else {
                    initial = initialsNotAssist.stream()
                            .filter(o -> ObjectUtil.equal(subjectId, o.getLong("subjectId"))).findFirst()
                            .orElse(null);
                }
                // 初始余额
                BigDecimal initialBalance = new BigDecimal(0);
                if (ObjectUtil.isNotNull(initial)) {
                    initialBalance = Optional.ofNullable(initial.getBigDecimal("initialBalance")).orElse(BigDecimal.ZERO);
                }
                List<JSONObject> currentBalances = currentBalanceGroupBySubjectId.get(subjectId);

                if (balanceDirection == 1) {
                    debtorInitialBalance = debtorInitialBalance.add(initialBalance).add(debtorLastCurrentBalance);
                    debtorInitialYearBalance = debtorInitialYearBalance.add(initialBalance);
                    if (CollUtil.isNotEmpty(currentBalances)) {
                        debtorInitialBalance = debtorInitialBalance.add(currentBalances.stream()
                                .map(b -> Optional.ofNullable(b.getBigDecimal("debtorLastCurrentBalance")).orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                        debtorInitialYearBalance = debtorInitialYearBalance.add(currentBalances.stream()
                                .map(b -> Optional.ofNullable(b.getBigDecimal("debtorLastYearCurrentBalance")).orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                    }
                } else {
                    creditInitialBalance = creditInitialBalance.add(initialBalance).add(creditLastCurrentBalance);
                    creditInitialYearBalance = creditInitialYearBalance.add(initialBalance);
                    if (CollUtil.isNotEmpty(currentBalances)) {
                        creditInitialBalance = creditInitialBalance.add(currentBalances.stream()
                                .map(b -> Optional.ofNullable(b.getBigDecimal("creditLastCurrentBalance")).orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                        creditInitialYearBalance = creditInitialYearBalance.add(currentBalances.stream()
                                .map(b -> Optional.ofNullable(b.getBigDecimal("creditLastYearCurrentBalance")).orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                    }
                }
                debtorCurrentBalance = debtorCurrentBalance.add(jsonObject.getBigDecimal("debtorCurrentBalance"));
                creditCurrentBalance = creditCurrentBalance.add(jsonObject.getBigDecimal("creditCurrentBalance"));
                debtorLastYearCurrentBalance = debtorLastYearCurrentBalance.add(jsonObject.getBigDecimal("debtorLastYearCurrentBalance"));
                creditLastYearCurrentBalance = creditLastYearCurrentBalance.add(jsonObject.getBigDecimal("creditLastYearCurrentBalance"));
                // 本年累计计算需要加上初始余额的值
                debtorYearBalance = debtorYearBalance
                        .add(jsonObject.getBigDecimal("debtorYearBalance"));
                if (ObjectUtil.isNotNull(initial)) {
                    debtorYearBalance = debtorYearBalance
                            .add(Optional.ofNullable(initial.getBigDecimal("addUpDebtorBalance")).orElse(BigDecimal.ZERO));
                }
                creditYearBalance = creditYearBalance
                        .add(jsonObject.getBigDecimal("creditYearBalance"));
                if (ObjectUtil.isNotNull(initial)) {
                    creditYearBalance = creditYearBalance
                            .add(Optional.ofNullable(initial.getBigDecimal("addUpCreditBalance")).orElse(BigDecimal.ZERO));
                }

                if (balanceDirection == 1) {
                    debtorEndBalance = debtorInitialYearBalance.add(debtorYearBalance).subtract(creditYearBalance);
                } else {
                    creditEndBalance = creditInitialYearBalance.add(creditYearBalance).subtract(debtorYearBalance);
                }
                jsonObject.put("debtorCurrentBalance", debtorCurrentBalance);
                jsonObject.put("creditCurrentBalance", creditCurrentBalance);
                jsonObject.put("debtorLastYearCurrentBalance", debtorLastYearCurrentBalance);
                jsonObject.put("creditLastYearCurrentBalance", creditLastYearCurrentBalance);
                jsonObject.put("debtorYearBalance", debtorYearBalance);
                jsonObject.put("creditYearBalance", creditYearBalance);
                jsonObject.put("debtorEndBalance", debtorEndBalance);
                jsonObject.put("creditEndBalance", creditEndBalance);
                jsonObject.put("debtorInitialBalance", debtorInitialBalance);
                jsonObject.put("creditInitialBalance", creditInitialBalance);
                jsonObject.put("debtorInitialYearBalance", debtorInitialYearBalance);
                jsonObject.put("creditInitialYearBalance", creditInitialYearBalance);

                jsonObject.put("debtorSumEndBalance", debtorEndBalance);
                jsonObject.put("creditSumEndBalance", creditEndBalance);
                jsonObject.put("debtorSumInitialBalance", debtorInitialBalance);
                jsonObject.put("creditSumInitialBalance", creditInitialBalance);
                jsonObject.put("debtorSumInitialYearBalance", debtorInitialYearBalance);
                jsonObject.put("creditSumInitialYearBalance", creditInitialYearBalance);

            }
            if (accountBO.getIsSubject() != null) {
                //返回非辅助核算全部科目
                List<JSONObject> amountNotAssist = amount.stream()
                        .filter(o -> !o.getBoolean("isAssist"))
                        .collect(Collectors.toList());
                for (JSONObject object : amountNotAssist) {
                    calculateSubjectBalance(object, amount, accountBookDirection);
                    subjectList.add(object);
                }
                return subjectList;
            }
            // 非辅助核算一级科目
            List<JSONObject> amountNotAssist = amount.stream()
                    .filter(o -> !o.getBoolean("isAssist"))
                    .filter(o -> ObjectUtil.equal(firstGrade, o.getInteger("grade")))
                    .collect(Collectors.toList());
            for (JSONObject object : amountNotAssist) {
                calculateSubjectBalance(object, amount, accountBookDirection);
                if (subjectHasBalance(object)) {
                    subjectList.add(object);
                }
            }
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
        return subjectList;
    }

    /**
     * 根据系统参数-账簿余额方向与科目方向相同转换科目余额
     *
     * @param accountBookDirection
     * @param jsonObject
     */
    private void transDirect(Integer accountBookDirection, JSONObject jsonObject) {
        // 科目方向
        Integer balanceDirection = jsonObject.getInteger("balanceDirection");
        // 借方期初始余额
        BigDecimal debtorInitialBalance = jsonObject.getBigDecimal("debtorInitialBalance");
        // 贷方期初始余额
        BigDecimal creditInitialBalance = jsonObject.getBigDecimal("creditInitialBalance");
        // 借方期末余额
        BigDecimal debtorEndBalance = jsonObject.getBigDecimal("debtorEndBalance");
        // 贷方期末余额
        BigDecimal creditEndBalance = jsonObject.getBigDecimal("creditEndBalance");
        // 借方期末下级余额
        BigDecimal debtorSumEndBalance = jsonObject.getBigDecimal("debtorSumEndBalance");
        // 贷方期末下级余额
        BigDecimal creditSumEndBalance = jsonObject.getBigDecimal("creditSumEndBalance");
        // 借方期初下级余额
        BigDecimal debtorSumInitialBalance = jsonObject.getBigDecimal("debtorSumInitialBalance");
        // 贷方期初下级余额
        BigDecimal creditSumInitialBalance = jsonObject.getBigDecimal("creditSumInitialBalance");
        // 借方年初下级余额
        BigDecimal debtorSumInitialYearBalance = jsonObject.getBigDecimal("debtorSumInitialYearBalance");
        // 贷方年初下级余额
        BigDecimal creditSumInitialYearBalance = jsonObject.getBigDecimal("creditSumInitialYearBalance");
        if (balanceDirection == 1) {
            if (debtorSumEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("creditSumEndBalance", debtorSumEndBalance.abs());
            }
            if (debtorSumInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("creditSumInitialBalance", debtorSumInitialBalance.abs());
            }
            if (debtorSumInitialYearBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("creditSumInitialYearBalance", debtorSumInitialYearBalance.abs());
            }
        } else {
            if (creditSumEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("debtorSumEndBalance", creditSumEndBalance.abs());
            }
            if (creditSumInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("debtorSumInitialBalance", creditSumInitialBalance.abs());
            }
            if (creditSumInitialYearBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("debtorSumInitialYearBalance", creditSumInitialYearBalance.abs());
            }
        }
        int two = 2;
        if (ObjectUtil.equal(two, accountBookDirection)) {
            if (balanceDirection == 1) {
                if (debtorInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                    jsonObject.put("debtorInitialBalance", 0);
                    jsonObject.put("creditInitialBalance", debtorInitialBalance.abs());
                }
                if (debtorEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                    jsonObject.put("debtorEndBalance", 0);
                    jsonObject.put("creditEndBalance", debtorEndBalance.abs());
                }
            } else {
                if (creditInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                    jsonObject.put("debtorInitialBalance", creditInitialBalance.abs());
                    jsonObject.put("creditInitialBalance", 0);
                }
                if (creditEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                    jsonObject.put("debtorEndBalance", creditEndBalance.abs());
                    jsonObject.put("creditEndBalance", 0);
                }
            }
        }
    }

    /**
     * 根据系统参数-账簿余额方向与科目方向相同转换科目余额
     *
     * @param jsonObject
     */
    private void transDirect(JSONObject jsonObject) {

        Integer balanceDirection = jsonObject.getInteger("balanceDirection");
        // 借方期初始余额
        BigDecimal debtorInitialBalance = jsonObject.getBigDecimal("debtorInitialBalance");
        // 贷方期初始余额
        BigDecimal creditInitialBalance = jsonObject.getBigDecimal("creditInitialBalance");
        // 借方期末余额
        BigDecimal debtorEndBalance = jsonObject.getBigDecimal("debtorEndBalance");
        // 贷方期末余额
        BigDecimal creditEndBalance = jsonObject.getBigDecimal("creditEndBalance");
        if (balanceDirection == 1) {
            if (debtorInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("debtorInitialBalance", 0);
                jsonObject.put("creditInitialBalance", debtorInitialBalance.abs());
            }

            if (debtorEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("debtorEndBalance", 0);
                jsonObject.put("creditEndBalance", debtorEndBalance.abs());
            }
        } else {
            if (creditInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("debtorInitialBalance", creditInitialBalance.abs());
                jsonObject.put("creditInitialBalance", 0);
            }

            if (creditEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                jsonObject.put("debtorEndBalance", creditEndBalance.abs());
                jsonObject.put("creditEndBalance", 0);
            }
        }
    }

    /**
     * 科目余额表计算
     *
     * @param object
     * @param amount
     * @param accountBookDirection
     */
    private void calculateSubjectBalance(JSONObject object, List<JSONObject> amount, Integer accountBookDirection) {
        Long subjectId = object.getLong("subjectId");
        Boolean isAssist = object.getBoolean("isAssist");
        if (isAssist) {
            return;
        }
        List<Long> childSubjectIds = CashFlowStatementReportHolder.getFirstChildSubjectIds(subjectId);
        // 获取子科目
        List<JSONObject> subList = amount.stream()
                .filter(a -> (
                        (!a.getBoolean("isAssist") && childSubjectIds.contains(a.getLong("subjectId")))
                )).collect(Collectors.toList());
        // 无子科目，获取辅助项
        if (CollUtil.isEmpty(subList)) {
            subList = amount.stream()
                    .filter(a -> (
                            (a.getBoolean("isAssist") && ObjectUtil.equal(subjectId, a.getLong("subjectId")))
                    )).collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(subList)) {
            return;
        }
        // 科目方向
        Integer balanceDirection = object.getInteger("balanceDirection");
        // 借方期初始余额
        BigDecimal debtorInitialBalance = new BigDecimal(0);
        // 贷方期初始余额
        BigDecimal creditInitialBalance = new BigDecimal(0);
        // 本期借方发生额
        BigDecimal debtorCurrentBalance = new BigDecimal(0);
        // 本期贷方发生额
        BigDecimal creditCurrentBalance = new BigDecimal(0);
        // 本期年初借方发生额
        BigDecimal debtorLastYearCurrentBalance = new BigDecimal(0);
        // 本期年初贷方发生额
        BigDecimal creditLastYearCurrentBalance = new BigDecimal(0);
        // 借方期末余额
        BigDecimal debtorEndBalance = new BigDecimal(0);
        // 贷方期末余额
        BigDecimal creditEndBalance = new BigDecimal(0);
        // 借方本年累计
        BigDecimal debtorYearBalance = new BigDecimal(0);
        // 贷方本年累计
        BigDecimal creditYearBalance = new BigDecimal(0);
        // 借方年初余额
        BigDecimal debtorInitialYearBalance = new BigDecimal(0);
        // 贷方年初余额
        BigDecimal creditInitialYearBalance = new BigDecimal(0);

        // 借方下级期末余额
        BigDecimal debtorSumEndBalance = new BigDecimal(0);
        // 贷方下级期末余额
        BigDecimal creditSumEndBalance = new BigDecimal(0);

        // 借方下级期初始余额
        BigDecimal debtorSumInitialBalance = new BigDecimal(0);
        // 贷方下级期初始余额
        BigDecimal creditSumInitialBalance = new BigDecimal(0);

        // 借方下级年初余额
        BigDecimal debtorSumInitialYearBalance = new BigDecimal(0);
        // 贷方下级年初余额
        BigDecimal creditSumInitialYearBalance = new BigDecimal(0);

        for (JSONObject jsonObject : subList) {
            // 计算子科目的科目余额
            this.calculateSubjectBalance(jsonObject, amount, accountBookDirection);
            Integer subBalanceDirection = jsonObject.getInteger("balanceDirection");
            // 借方期初始余额
            BigDecimal subDebtorInitialBalance = jsonObject.getBigDecimal("debtorInitialBalance");
            // 贷方期初始余额
            BigDecimal subCreditInitialBalance = jsonObject.getBigDecimal("creditInitialBalance");
            // 借方年初始余额
            BigDecimal subDebtorInitialYearBalance = jsonObject.getBigDecimal("debtorInitialYearBalance");
            // 贷方年初始余额
            BigDecimal subCreditInitialYearBalance = jsonObject.getBigDecimal("creditInitialYearBalance");
            // 本期借方发生额
            BigDecimal subDebtorCurrentBalance = jsonObject.getBigDecimal("debtorCurrentBalance");
            // 本期贷方发生额
            BigDecimal subCreditCurrentBalance = jsonObject.getBigDecimal("creditCurrentBalance");
            // 本期借方发生额
            BigDecimal subDebtorLastYearCurrentBalance = jsonObject.getBigDecimal("debtorLastYearCurrentBalance");
            // 本期贷方发生额
            BigDecimal subCreditLastYearCurrentBalance = jsonObject.getBigDecimal("creditLastYearCurrentBalance");
            // 借方期末余额
            BigDecimal subDebtorEndBalance = jsonObject.getBigDecimal("debtorEndBalance");
            // 贷方期末余额
            BigDecimal subCreditEndBalance = jsonObject.getBigDecimal("creditEndBalance");
            // 借方本年累计
            BigDecimal subDebtorYearBalance = jsonObject.getBigDecimal("debtorYearBalance");
            // 贷方本年累计
            BigDecimal subCreditYearBalance = jsonObject.getBigDecimal("creditYearBalance");


            debtorCurrentBalance = debtorCurrentBalance.add(subDebtorCurrentBalance);
            creditCurrentBalance = creditCurrentBalance.add(subCreditCurrentBalance);
            debtorLastYearCurrentBalance = debtorLastYearCurrentBalance.add(subDebtorLastYearCurrentBalance);
            creditLastYearCurrentBalance = creditLastYearCurrentBalance.add(subCreditLastYearCurrentBalance);
            debtorYearBalance = debtorYearBalance.add(subDebtorYearBalance);
            creditYearBalance = creditYearBalance.add(subCreditYearBalance);
            if (ObjectUtil.equal(balanceDirection, subBalanceDirection)) {
                debtorInitialBalance = debtorInitialBalance.add(subDebtorInitialBalance);
                creditInitialBalance = creditInitialBalance.add(subCreditInitialBalance);

                debtorEndBalance = debtorEndBalance.add(subDebtorEndBalance);
                creditEndBalance = creditEndBalance.add(subCreditEndBalance);

            } else {
                debtorInitialBalance = debtorInitialBalance.subtract(subDebtorInitialBalance);
                creditInitialBalance = creditInitialBalance.subtract(subCreditInitialBalance);

                debtorInitialYearBalance = debtorInitialYearBalance.subtract(subDebtorInitialYearBalance);
                creditInitialYearBalance = creditInitialYearBalance.subtract(subCreditInitialYearBalance);

                debtorEndBalance = debtorEndBalance.subtract(subDebtorEndBalance);
                creditEndBalance = creditEndBalance.subtract(subCreditEndBalance);
            }
            this.transDirect(accountBookDirection, jsonObject);

            // 借方期末余额
            BigDecimal subDebtorSumEndBalance = jsonObject.getBigDecimal("debtorSumEndBalance");
            // 贷方期末余额
            BigDecimal subCreditSumEndBalance = jsonObject.getBigDecimal("creditSumEndBalance");
            // 借方本年累计
            BigDecimal subDebtorSumInitialBalance = jsonObject.getBigDecimal("debtorSumInitialBalance");
            // 贷方本年累计
            BigDecimal subCreditSumInitialBalance = jsonObject.getBigDecimal("creditSumInitialBalance");

            // 借方本年累计
            BigDecimal subDebtorSumInitialYearBalance = jsonObject.getBigDecimal("debtorSumInitialYearBalance");
            // 贷方本年累计
            BigDecimal subCreditSumInitialYearBalance = jsonObject.getBigDecimal("creditSumInitialYearBalance");


            if (subDebtorSumEndBalance.compareTo(BigDecimal.ZERO) == 1) {
                debtorSumEndBalance = debtorSumEndBalance.add(subDebtorSumEndBalance);
            }

            if (subCreditSumEndBalance.compareTo(BigDecimal.ZERO) == 1) {
                creditSumEndBalance = creditSumEndBalance.add(subCreditSumEndBalance);

            }

            if (subDebtorSumInitialBalance.compareTo(BigDecimal.ZERO) == 1) {
                debtorSumInitialBalance = debtorSumInitialBalance.add(subDebtorSumInitialBalance);
            }

            if (subCreditSumInitialBalance.compareTo(BigDecimal.ZERO) == 1) {
                creditSumInitialBalance = creditSumInitialBalance.add(subCreditSumInitialBalance);

            }

            if (subDebtorSumInitialYearBalance.compareTo(BigDecimal.ZERO) == 1) {
                debtorSumInitialYearBalance = debtorSumInitialYearBalance.add(subDebtorSumInitialYearBalance);
            }

            if (subCreditSumInitialYearBalance.compareTo(BigDecimal.ZERO) == 1) {
                creditSumInitialYearBalance = creditSumInitialYearBalance.add(subCreditSumInitialYearBalance);

            }
        }
        // 借
        if (ObjectUtil.equal(1, balanceDirection)) {
            debtorInitialBalance = debtorInitialBalance.add(creditInitialBalance);
            creditInitialBalance = BigDecimal.ZERO;

            debtorInitialYearBalance = debtorInitialYearBalance.add(creditInitialYearBalance);
            creditInitialYearBalance = BigDecimal.ZERO;

            debtorEndBalance = debtorEndBalance.add(creditEndBalance);
            creditEndBalance = BigDecimal.ZERO;
        } else {
            creditInitialBalance = debtorInitialBalance.add(creditInitialBalance);
            debtorInitialBalance = BigDecimal.ZERO;

            debtorInitialYearBalance = BigDecimal.ZERO;
            creditInitialYearBalance = creditInitialYearBalance.add(debtorInitialYearBalance);

            creditEndBalance = debtorEndBalance.add(creditEndBalance);
            debtorEndBalance = BigDecimal.ZERO;
        }

        object.put("debtorCurrentBalance", debtorCurrentBalance);
        object.put("creditCurrentBalance", creditCurrentBalance);
        object.put("debtorLastYearCurrentBalance", debtorLastYearCurrentBalance);
        object.put("creditLastYearCurrentBalance", creditLastYearCurrentBalance);
        object.put("debtorYearBalance", debtorYearBalance);
        object.put("creditYearBalance", creditYearBalance);
        object.put("debtorEndBalance", debtorEndBalance);
        object.put("creditEndBalance", creditEndBalance);
        object.put("debtorInitialBalance", debtorInitialBalance);
        object.put("creditInitialBalance", creditInitialBalance);
        object.put("debtorInitialYearBalance", debtorInitialYearBalance);
        object.put("creditInitialYearBalance", creditInitialYearBalance);
        object.put("debtorSumEndBalance", debtorSumEndBalance);
        object.put("creditSumEndBalance", creditSumEndBalance);
        object.put("debtorSumInitialBalance", debtorSumInitialBalance);
        object.put("creditSumInitialBalance", creditSumInitialBalance);
        object.put("debtorSumInitialYearBalance", debtorSumInitialYearBalance);
        object.put("creditSumInitialYearBalance", creditSumInitialYearBalance);
        subList = subList.stream().filter(this::subjectHasBalance).collect(Collectors.toList());
        object.put("subjects", subList);
        this.transDirect(accountBookDirection, object);
    }

    /**
     * 科目有余额
     *
     * @param object
     * @return
     */
    private Boolean subjectHasBalance(JSONObject object) {
        // 借方期初始余额
        BigDecimal debtorInitialBalance = object.getBigDecimal("debtorInitialBalance");
        // 贷方期初始余额
        BigDecimal creditInitialBalance = object.getBigDecimal("creditInitialBalance");
        // 本期借方发生额
        BigDecimal debtorCurrentBalance = object.getBigDecimal("debtorCurrentBalance");
        // 本期贷方发生额
        BigDecimal creditCurrentBalance = object.getBigDecimal("creditCurrentBalance");
        // 借方期末余额
        BigDecimal debtorEndBalance = object.getBigDecimal("debtorEndBalance");
        // 贷方期末余额
        BigDecimal creditEndBalance = object.getBigDecimal("creditEndBalance");
        // 借方本年累计
        BigDecimal debtorYearBalance = object.getBigDecimal("debtorYearBalance");
        // 贷方本年累计
        BigDecimal creditYearBalance = object.getBigDecimal("creditYearBalance");

        if (debtorInitialBalance.compareTo(BigDecimal.ZERO) == 0 &&
                creditInitialBalance.compareTo(BigDecimal.ZERO) == 0 &&
                debtorCurrentBalance.compareTo(BigDecimal.ZERO) == 0 &&
                creditCurrentBalance.compareTo(BigDecimal.ZERO) == 0 &&
                debtorEndBalance.compareTo(BigDecimal.ZERO) == 0 &&
                creditEndBalance.compareTo(BigDecimal.ZERO) == 0 &&
                debtorYearBalance.compareTo(BigDecimal.ZERO) == 0 &&
                creditYearBalance.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return true;
    }

    /**
     * 为科目余额设置本期发生额
     *
     * @param subjectAssist
     * @param balanceAssist
     */
    private void setBalanceForSubjectAssist(List<JSONObject> subjectAssist, List<JSONObject> balanceAssist) {
        for (JSONObject jsonObject : subjectAssist) {
            Long subjectId = jsonObject.getLong("subjectId");
            Long assistId = jsonObject.getLong("assistId");
            JSONObject object = balanceAssist.stream()
                    .filter(b -> ObjectUtil.equal(subjectId, b.getLong("subjectId"))
                            && ObjectUtil.equal(assistId, b.getLong("assistId"))).findFirst().orElse(null);
            if (ObjectUtil.isNotNull(object)) {
                jsonObject.put("debtorCurrentBalance", object.getBigDecimal("debtorCurrentBalance"));
                jsonObject.put("creditCurrentBalance", object.getBigDecimal("creditCurrentBalance"));
            } else {
                jsonObject.put("debtorCurrentBalance", BigDecimal.ZERO);
                jsonObject.put("creditCurrentBalance", BigDecimal.ZERO);
            }
        }
    }

    @Override
    public FinanceDiversificationVO queryDiversification(FinanceDetailAccountBO accountBO) {
        FinanceDiversificationVO vo = new FinanceDiversificationVO();
        if (accountBO.getSubjectId() == null) {
            return vo;
        }
        accountBO.setAccountId(AccountSet.getAccountSetId());
        FinanceSubject financeSubject = financeSubjectService.getById(accountBO.getSubjectId());
        accountBO.setBalanceDirection(financeSubject.getBalanceDirection());
        accountBO.setIsBalanceDirection(financeSubject.getBalanceDirection());
        //获取符合条件的科目id
        List<Long> subjectIds = financeSubjectService.queryIds(accountBO.getSubjectId(), accountBO.getMinLevel(), accountBO.getMaxLevel());
        if (subjectIds.size() == 0) {
            subjectIds.add(0L);
        }
        accountBO.setSubjectIds(subjectIds);
        //获取借方科目id
        List<Long> debtorSubjectIds = getBaseMapper().queryDebtorSubjectIds(accountBO);
        HashSet<Long> set = new HashSet<>(debtorSubjectIds);
        debtorSubjectIds.clear();
        debtorSubjectIds.addAll(set);
        accountBO.setDebtorSubjectIds(debtorSubjectIds);
        if (debtorSubjectIds.size() > 0) {
            List<FinanceSubject> subjects = financeSubjectService.lambdaQuery()
                    .in(FinanceSubject::getSubjectId, debtorSubjectIds).list();
            vo.setSubjects(subjects);
        }
        BiParams biParams = new BiParams();
        biParams.setStartTime(accountBO.getStartTime());
        biParams.setEndTime(accountBO.getEndTime());
        BiTimeUtil.BiTimeEntity record = BiTimeUtil.analyzeTypeTime(biParams);
        accountBO.setStartTime(DateUtil.formatDate(record.getBeginDate()));
        accountBO.setEndTime(DateUtil.formatDate(record.getEndDate()));
        Integer beginTime = record.getBeginTime();
        Integer cycleNum = record.getCycleNum();
        List<Integer> timeList = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            timeList.add(beginTime);
            beginTime = BiTimeUtil.estimateTime(beginTime);
        }
        accountBO.setTimeList(timeList);
        JSONObject paramnter = parameterService.queryParameter();
        if (paramnter != null) {
            accountBO.setAccountBookDirection(paramnter.getInteger("accountBookDirection"));
        } else {
            accountBO.setAccountBookDirection(1);
        }
        List<JSONObject> detailAccounts = getBaseMapper().queryDiversification(accountBO);
        calculate(detailAccounts, accountBO);
        vo.setJsonObjects(detailAccounts);
        return vo;
    }

    @Override
    public List<JSONObject> queryListDetailBalanceAccount(FinanceDetailAccountBO accountBO) {
        accountBO.setAccountId(AccountSet.getAccountSetId());
        return getBaseMapper().queryListDetailBalanceAccount(accountBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject voucherImport(MultipartFile file) throws IOException, IllegalAccessException {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
        List<Map<String, Object>> rowList = reader.readAll();
        List<VoucherImportBO> voucherList = FileImportExportUtil.parseData2Object(rowList);
        // region 数据校验
        // 1.数据分组 key：日期 + 字 + 号
        Map<String, List<VoucherImportBO>> voucherMap = voucherList.stream().collect(Collectors.groupingBy(b ->
                String.format("%s_%s_%s",
                        Optional.ofNullable(DateUtil.format(b.getCertificateTime(), DatePattern.PURE_DATE_PATTERN)).orElse("*"),
                        Optional.ofNullable(b.getVoucherName()).orElse("*"),
                        Optional.ofNullable(b.getVoucherNum()).map(String::valueOf).orElse("*")
                )
        ));
        // 2.分组内数据 字段为空校验；借方 贷方不可同时填写 且 ≠ 0
        FileImportExportUtil.checkVoucherEmptyField(voucherMap);
        FileImportExportUtil.checkVoucherNumber(voucherMap);
        // 3. 账期是否结账检查
        String currentPeriod = financeStatementSettleService.getCurrentPeriod();
        FileImportExportUtil.checkCertificateTime(voucherMap, currentPeriod);
        // 获取科目和科目的辅助核算信息
        List<FinanceSubjectImportBO> subjectAdjuvantList = subjectMapper.querySubjectWithAdjuvant(AccountSet.getAccountSetId());
        List<FinanceSubject> subjects = subjectService.queryAll();
        List<FinanceAdjuvantCarteImportBO> adjuvantCarteList = adjuvantCarteMapper.queryAdjuvantInfo(AccountSet.getAccountSetId());
        // 查询当前账套的凭证字
        List<FinanceVoucher> vouchers = financeVoucherService.queryList();
        // 4.添加了辅助核算的科目 辅助核算的科目类别、编码不可为空
        // 5.添加了辅助核算未开启数量核算的科目，数量和单价不可填写
        FileImportExportUtil.checkSubject(voucherMap, subjectAdjuvantList);
        FileImportExportUtil.checkAdjuvant(voucherMap, subjectAdjuvantList, adjuvantCarteList);
        // 6.分组内数据借贷平衡检查
        FileImportExportUtil.checkVoucherBalance(voucherMap);
        // 7.日期凭证号校验
        LambdaQueryWrapper<CertificateWithVoucherBO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CertificateWithVoucherBO::getAccountId, AccountSet.getAccountSetId());
        // 系统中已存在的凭证日期凭证号信息
        List<CertificateWithVoucherBO> certificateWithVoucherBOS = baseMapper.queryCertificateWithVoucher(queryWrapper);
        FileImportExportUtil.checkVoucherNameNum(voucherMap, certificateWithVoucherBOS);
        // endregion
        // region 导入
        saveData(voucherMap, vouchers, subjects, subjectAdjuvantList, adjuvantCarteList);
        // region
        // 生成错误文件
        JSONObject result = FileImportExportUtil.generateErrorFile(voucherMap, reader);
        result.fluentPut("totalSize", rowList.size());
        return result;
    }

    /**
     * 保存凭证数据
     *
     * @param voucherMap          凭证数据
     * @param vouchers            凭证字信息
     * @param subjects            科目信息
     * @param subjectAdjuvantList 科目和辅助核算关联信息
     * @param adjuvantCarteList   辅助核算数据
     */
    private void saveData(Map<String, List<VoucherImportBO>> voucherMap,
                          List<FinanceVoucher> vouchers,
                          List<FinanceSubject> subjects,
                          List<FinanceSubjectImportBO> subjectAdjuvantList,
                          List<FinanceAdjuvantCarteImportBO> adjuvantCarteList) {
        Map<String, Long> voucherNameIdMap = vouchers.stream().collect(Collectors.toMap(FinanceVoucher::getVoucherName, FinanceVoucher::getVoucherId));
        Map<String, FinanceSubject> subjectNumberEntityMap = subjects.stream().collect(Collectors.toMap(FinanceSubject::getNumber, Function.identity()));
        Map<Long, List<FinanceSubjectImportBO>> subjectIdEntityMap = subjectAdjuvantList.stream().collect(Collectors.groupingBy(FinanceSubjectImportBO::getSubjectId));
        Map<Long, List<FinanceAdjuvantCarteImportBO>> adjuvantIdMap = adjuvantCarteList.stream().collect(Collectors.groupingBy(FinanceAdjuvantCarteImportBO::getAdjuvantId));
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            VoucherImportBO anyError = list.stream().filter(d -> CollUtil.isNotEmpty(d.getErrors())).findFirst().orElse(null);
            if (ObjectUtil.isNull(anyError)) {
                VoucherImportBO bo = CollUtil.getFirst(list);
                Date certificateTime = bo.getCertificateTime();
                String voucherName = bo.getVoucherName();
                Integer voucherNum = bo.getVoucherNum();
                Integer fileNum = bo.getFileNum();
                String batchId = IdUtil.simpleUUID();
                BigDecimal total = list.stream().map(d -> Optional.ofNullable(d.getDebtorBalance()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);

                // 1.生成凭证
                FinanceCertificate certificate = new FinanceCertificate();
                certificate.setVoucherId(voucherNameIdMap.get(voucherName));
                certificate.setCertificateTime(LocalDateTimeUtil.of(certificateTime));
                certificate.setCertificateNum(voucherNum);
                certificate.setBatchId(batchId);
                certificate.setFileNum(fileNum);
                certificate.setTotal(total.toPlainString());
                certificate.setCreateUserId(UserUtil.getUserId());
                certificate.setCreateTime(LocalDateTime.now());
                certificate.setAccountId(AccountSet.getAccountSetId());
                save(certificate);

                // 2.生成凭证详情
                AtomicInteger sort = new AtomicInteger(1);
                for (VoucherImportBO importBO : list) {
                    FinanceCertificateDetail detail = new FinanceCertificateDetail();
                    detail.setDigestContent(importBO.getDigestContent());
                    detail.setQuantity(Optional.ofNullable(importBO.getQuality()).map(String::valueOf).orElse(null));
                    detail.setPrice(Optional.ofNullable(importBO.getPrice()).map(BigDecimal::valueOf).orElse(null));
                    detail.setDebtorBalance(importBO.getDebtorBalance());
                    detail.setCreditBalance(importBO.getCreditBalance());
                    detail.setCertificateId(certificate.getCertificateId());
                    FinanceSubject subject = subjectNumberEntityMap.get(importBO.getSubjectNumber());
                    detail.setSubjectId(subject.getSubjectId());
                    detail.setSubjectContent(JSON.toJSONString(subject));
                    detail.setSubjectNumber(subject.getNumber());
                    detail.setSubjectName(subject.getSubjectName());
                    detail.setSort(sort.getAndIncrement());
                    detail.setAccountId(AccountSet.getAccountSetId());
                    detail.setCreateUserId(UserUtil.getUserId());
                    detail.setCreateTime(LocalDateTime.now());
                    detail.setDetailId(BaseUtil.getNextId());


                    List<FinanceSubjectImportBO> adjuvantList = subjectIdEntityMap.get(subject.getSubjectId());
                    if (adjuvantList != null) {
                        List<FinanceAssistAdjuvant> assistAdjuvantList = new ArrayList<>();
                        List<FinanceCertificateAssociation> associationList = new ArrayList<>();
                        //保存辅助核算
                        FinanceAssist assist = new FinanceAssist();
                        assist.setAccountId(AccountSet.getAccountSetId());
                        assist.setSubjectId(subject.getSubjectId());
                        assist.setAssistId(BaseUtil.getNextId());
                        if (detail.getAssistId() == null) {
                            detail.setAssistId(assist.getAssistId());
                        }
                        financeAssistService.save(assist);
                        for (FinanceSubjectImportBO financeSubjectImport : adjuvantList) {
                            List<FinanceAdjuvantCarteImportBO> adjuvantCarteImport = adjuvantIdMap.get(financeSubjectImport.getAdjuvantId());
                            if (adjuvantCarteImport == null) {
                                continue;
                            }
                            String labelName = financeSubjectImport.getLabelName();
                            String carteNumber = MapUtil.getStr(importBO.getSourceMap(), labelName);
                            if (StrUtil.isNotEmpty(carteNumber)) {
                                Optional<FinanceAdjuvantCarteImportBO> optional = adjuvantCarteImport.stream().filter(carte -> Objects.equals(carteNumber, carte.getCarteNumber()) && Objects.equals(financeSubjectImport.getAdjuvantId(), carte.getAdjuvantId())).findFirst();
                                if (!optional.isPresent()) {
                                    continue;
                                }
                                //保存辅助核算关联模块表
                                FinanceAdjuvantCarteImportBO carte = optional.get();
                                FinanceAssistAdjuvant adjuvant = new FinanceAssistAdjuvant();
                                adjuvant.setAssistId(assist.getAssistId());
                                adjuvant.setLabel(financeSubjectImport.getLabel());
                                adjuvant.setAccountId(subject.getAccountId());
                                adjuvant.setRelationId(carte.getCarteId());
                                adjuvant.setLabelName(financeSubjectImport.getLabelName());
                                adjuvant.setAdjuvantId(financeSubjectImport.getAdjuvantId());
                                assistAdjuvantList.add(adjuvant);
                                //保存凭证详情关联标签类型表
                                FinanceCertificateAssociation tion = new FinanceCertificateAssociation();
                                tion.setDetailId(detail.getDetailId());
                                tion.setAccountId(subject.getAccountId());
                                tion.setRelationId(carte.getCarteId());
                                tion.setLabel(financeSubjectImport.getLabel());
                                tion.setAdjuvantId(financeSubjectImport.getAdjuvantId());
                                associationList.add(tion);
                            }
                        }
                        financeAssistAdjuvantService.saveBatch(assistAdjuvantList);
                        associationService.saveBatch(associationList);
                    }

                    financeCertificateDetailService.save(detail);
                }
            }
        }
    }


    @Override
    public void downloadVoucherImportTemplate(HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = null;
        try {
            // sheet 数据
            List<SheetBO> sheetBOS = new ArrayList<>();

            // region sheet 1
            // 查询当前账套的凭证字
            List<FinanceVoucher> vouchers = financeVoucherService.queryList();
            String[] voucherNames = vouchers.stream().map(FinanceVoucher::getVoucherName).toArray(String[]::new);
            Map<String, String[]> headerNameDefaultValue = new HashMap<>();
            headerNameDefaultValue.put("凭证字", voucherNames);
            // 查询企业的当前账套的自定义辅助核算
            List<FinanceAdjuvant> adjuvants = adjuvantService.queryCustomList();
            List<String> adjuvantNames = adjuvants.stream().map(FinanceAdjuvant::getAdjuvantName).collect(Collectors.toList());
            LinkedHashMap<String, String> headerNameComment = new LinkedHashMap<>();
            for (String adjuvantName : adjuvantNames) {
                headerNameComment.put(String.format("%s", adjuvantName), null);
            }
            headerNameComment.put("数量", "可以为空值；如不为空，应先在”系统设置“——”科目“中选择”数量金额核算”，否则凭证无法引入。");
            headerNameComment.put("单价", "可以为空值；如不为空，应先在”系统设置“——”科目“中选择”数量金额核算”，否则凭证无法引入。");
            SheetBO sheetBO1 = new SheetBO();
            sheetBO1.setName("凭证模板");
            sheetBO1.setIndex(0);
            sheetBO1.setHeaderNameComment(headerNameComment);
            sheetBO1.setHeaderNameDefaultValue(headerNameDefaultValue);
            sheetBOS.add(sheetBO1);
            // endregion
            // region sheet 2
            // 获取科目信息
            List<FinanceSubjectImportBO> subjectList = subjectMapper.querySubjectWithAdjuvant(AccountSet.getAccountSetId());
            SheetBO sheetBO2 = new SheetBO();
            sheetBO2.setIndex(1);
            sheetBO2.setName("科目数据");
            List<Map<String, Object>> rows2 = new ArrayList<>();
            Field[] fields = FinanceSubjectImportBO.class.getDeclaredFields();
            for (FinanceSubjectImportBO subject : subjectList) {
                Map<String, Object> row = new HashMap<>();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    field.setAccessible(true);
                    String annotationName = field.getAnnotation(ApiModelProperty.class).value();
                    Object o = field.get(subject);
                    if (StrUtil.equals("balanceDirection", fieldName)) {
                        if (ObjectUtil.equal(1, o)) {
                            o = "借";
                        } else {
                            o = "贷";
                        }
                    }
                    if (StrUtil.equals("isCash", fieldName)) {
                        if (ObjectUtil.equal(true, o)) {
                            o = "是";
                        } else {
                            o = "否";
                        }
                    }
                    row.put(annotationName, o);
                }
                rows2.add(row);
            }
            sheetBO2.setRows(rows2);
            sheetBOS.add(sheetBO2);
            // endregion
            // region sheet 3
            // 查询辅助核算数据
            List<FinanceAdjuvantCarteImportBO> adjuvantCarteList = adjuvantCarteMapper.queryAdjuvantInfo(AccountSet.getAccountSetId());
            SheetBO sheetBO3 = new SheetBO();
            sheetBO3.setIndex(2);
            sheetBO3.setName("辅助核算项目数据");
            List<Map<String, Object>> rows3 = new ArrayList<>();
            Field[] fields2 = FinanceAdjuvantCarteImportBO.class.getDeclaredFields();
            for (FinanceAdjuvantCarteImportBO carteBO : adjuvantCarteList) {
                Map<String, Object> row = new HashMap<>();
                for (Field field : fields2) {
                    String fieldName = field.getName();
                    field.setAccessible(true);
                    String annotationName = field.getAnnotation(ApiModelProperty.class).value();
                    Object o = field.get(carteBO);
                    row.put(annotationName, o);
                }
                rows3.add(row);
            }
            sheetBO3.setRows(rows3);
            sheetBOS.add(sheetBO3);
            // endregion
            ExcelWriter excelWriter = FileImportExportUtil.generateVoucherImportFile(sheetBOS);
            String fileName = URLEncoder.encode("凭证导入模板.xlsx", "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            outputStream = response.getOutputStream();
            excelWriter.flush(outputStream, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(outputStream);
        }
    }

    private void calculate(List<JSONObject> detailAccounts, FinanceDetailAccountBO accountBO) {
        //余额数量
        BigDecimal balanceQuantity = new BigDecimal("0");
        //本年累计借方金额
        BigDecimal debtorYearBalance = new BigDecimal("0");
        //本年累计贷方金额
        BigDecimal creditYearBalance = new BigDecimal("0");
        //余额单价
        BigDecimal balanceUnit = new BigDecimal("0");
        //本期累计借方金额
        BigDecimal debtorMonthBalance = new BigDecimal("0");
        //本期累计贷方金额
        BigDecimal creditMonthBalance = new BigDecimal("0");
        //期初余额
        BigDecimal initialBalance = new BigDecimal("0");
        //当前月份
        Integer monthTime = 0;
        Integer yearTime = 0;
        String balanceDirection = "平";
        for (JSONObject json : detailAccounts) {
            //格式化保留两位小数
            if (ObjectUtil.isNotEmpty(json.get("debtorAmountUnit"))) {
                json.put("debtorAmountUnit", Convert.toBigDecimal(json.get("debtorAmountUnit")).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            if (json.getInteger("type") == 1) {
                //期初余额
                initialBalance = json.getBigDecimal("balance");
                monthTime = json.getInteger("monthTime");
                yearTime = json.getInteger("yearTime");
                balanceDirection = json.getString("balanceDirection");
                if (initialBalance.compareTo(new BigDecimal("0")) == 0) {
                    json.put("balanceDirection", "平");
                }
                if (accountBO.getIsBalanceDirection() != null) {
                    if (Objects.equals(balanceDirection, "平")) {
                        balanceDirection = accountBO.getIsBalanceDirection() == 1 ? "借" : "贷";
                    }
                    if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                        balanceQuantity = json.getBigDecimal("balanceQuantity");
                        if (balanceQuantity.compareTo(BigDecimal.ZERO) != 0) {
                            json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                        }
                    }
                }
            } else if (json.getInteger("type") == 2) {
                if (!monthTime.equals(json.getInteger("monthTime"))) {
                    debtorMonthBalance = new BigDecimal(0);
                    creditMonthBalance = new BigDecimal(0);
                }
                if (!yearTime.equals(json.getInteger("yearTime"))) {
                    debtorYearBalance = new BigDecimal(0);
                    creditYearBalance = new BigDecimal(0);
                }
                if (accountBO.getIsBalanceDirection() != null) {
                    if (Objects.equals(balanceDirection, json.get("balanceDirection"))) {
                        if (Objects.equals("借", json.get("balanceDirection"))) {
                            initialBalance = initialBalance.add(json.getBigDecimal("debtorBalance"))
                                    .subtract(json.getBigDecimal("creditBalance"));
                            if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                balanceQuantity = balanceQuantity.add(json.getBigDecimal("debtorQuantity"))
                                        .subtract(json.getBigDecimal("creditQuantity"));
                                if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                        && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                        (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                    json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                } else {
                                    BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                    if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                        testBig = new BigDecimal("1");
                                    }
                                    json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                }
                                json.put("balanceQuantity", balanceQuantity);
                            }
                        } else {
                            initialBalance = initialBalance.add(json.getBigDecimal("creditBalance"))
                                    .subtract(json.getBigDecimal("debtorBalance"));
                            if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                balanceQuantity = balanceQuantity.add(json.getBigDecimal("debtorQuantity"))
                                        .subtract(json.getBigDecimal("creditQuantity"));
                                if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                        && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                        (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                    json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                } else {
                                    BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                    if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                        testBig = new BigDecimal("1");
                                    }
                                    json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                }
                                json.put("balanceQuantity", balanceQuantity);
                            }
                        }
                    } else {
                        if (Objects.equals("借", json.get("balanceDirection"))) {
                            initialBalance = initialBalance.add(json.getBigDecimal("creditBalance"))
                                    .subtract(json.getBigDecimal("debtorBalance"));
                            if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                balanceQuantity = balanceQuantity.add(json.getBigDecimal("creditQuantity"))
                                        .subtract(json.getBigDecimal("debtorQuantity"));
                                if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                        && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                        (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                    json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                } else {
                                    BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                    if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                        testBig = new BigDecimal("1");
                                    }
                                    json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                }
                                json.put("balanceQuantity", balanceQuantity);
                            }
                        } else {
                            initialBalance = initialBalance.add(json.getBigDecimal("debtorBalance"))
                                    .subtract(json.getBigDecimal("creditBalance"));
                            if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                balanceQuantity = balanceQuantity.add(json.getBigDecimal("debtorQuantity"))
                                        .subtract(json.getBigDecimal("creditQuantity"));
                                if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                        && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                        (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                    json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                } else {
                                    BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                    if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                        testBig = new BigDecimal("1");
                                    }
                                    json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                }
                                json.put("balanceQuantity", balanceQuantity);
                            }
                        }
                    }
                    json.put("balance", initialBalance);
                    json.put("balanceDirection", balanceDirection);
                    if (initialBalance.compareTo(BigDecimal.ZERO) == 0) {
                        json.put("balanceDirection", "平");
                    }
                } else {
                    if (accountBO.getAccountBookDirection() == 1) {
                        //账簿余额方向与科目方向相同 1.相同
                        if (Objects.equals(balanceDirection, json.get("balanceDirection"))) {
                            if (Objects.equals("借", json.get("balanceDirection"))) {
                                initialBalance = initialBalance.add(json.getBigDecimal("debtorBalance"))
                                        .subtract(json.getBigDecimal("creditBalance"));
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = balanceQuantity.add(json.getBigDecimal("debtorQuantity"))
                                            .subtract(json.getBigDecimal("creditQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                            } else {
                                initialBalance = initialBalance.add(json.getBigDecimal("creditBalance"))
                                        .subtract(json.getBigDecimal("debtorBalance"));
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = balanceQuantity.add(json.getBigDecimal("debtorQuantity"))
                                            .subtract(json.getBigDecimal("creditQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                            }
                        } else {
                            if (Objects.equals("借", json.get("balanceDirection"))) {
                                initialBalance = new BigDecimal("0").subtract(initialBalance).add(json.getBigDecimal("debtorBalance"))
                                        .subtract(json.getBigDecimal("creditBalance"));
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = new BigDecimal("0").subtract(balanceQuantity).add(json.getBigDecimal("debtorQuantity"))
                                            .subtract(json.getBigDecimal("creditQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                            } else {
                                initialBalance = new BigDecimal("0").subtract(initialBalance).add(json.getBigDecimal("creditBalance"))
                                        .subtract(json.getBigDecimal("debtorBalance"));
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = new BigDecimal("0").subtract(balanceQuantity).add(json.getBigDecimal("creditQuantity"))
                                            .subtract(json.getBigDecimal("debtorQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                            }
                        }
                        json.put("balance", initialBalance);
                        if (initialBalance.compareTo(new BigDecimal(0)) == 0) {
                            json.put("balanceDirection", "平");
                            if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                balanceQuantity = new BigDecimal("0").subtract(balanceQuantity).add(json.getBigDecimal("creditQuantity"))
                                        .subtract(json.getBigDecimal("debtorQuantity"));
                                json.put("balanceAmountUnit", 0);
                                json.put("balanceQuantity", balanceQuantity);
                            }
                        }
                    } else {
                        initialBalance = initialBalance.add(json.getBigDecimal("debtorBalance"))
                                .subtract(json.getBigDecimal("creditBalance"));
                        if (Objects.equals("平", balanceDirection) || Objects.equals("借", balanceDirection)) {
                            if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                                json.put("balance", new BigDecimal("0").subtract(initialBalance));
                                json.put("balanceDirection", "贷");
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = new BigDecimal("0").subtract(balanceQuantity).add(json.getBigDecimal("creditQuantity"))
                                            .subtract(json.getBigDecimal("debtorQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                            } else {
                                if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                    json.put("balanceDirection", "借");
                                } else {
                                    json.put("balanceDirection", "平");
                                }
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = balanceQuantity.add(json.getBigDecimal("debtorQuantity"))
                                            .subtract(json.getBigDecimal("creditQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                                json.put("balance", initialBalance);
                            }
                        } else {
                            if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                                json.put("balance", new BigDecimal("0").subtract(initialBalance));
                                json.put("balanceDirection", "借");
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = new BigDecimal("0").subtract(balanceQuantity).add(json.getBigDecimal("debtorQuantity"))
                                            .subtract(json.getBigDecimal("creditQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                            } else {
                                if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                    json.put("balanceDirection", "贷");
                                } else {
                                    json.put("balanceDirection", "平");
                                }
                                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                                    balanceQuantity = balanceQuantity.add(json.getBigDecimal("creditQuantity"))
                                            .subtract(json.getBigDecimal("debtorQuantity"));
                                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                                    } else {
                                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                                            testBig = new BigDecimal("1");
                                        }
                                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                                    }
                                    json.put("balanceQuantity", balanceQuantity);
                                }
                                json.put("balance", initialBalance);
                            }
                        }


                    }
                    balanceDirection = json.getString("balanceDirection");
                }

                if (monthTime.equals(json.getInteger("monthTime"))) {
                    debtorMonthBalance = debtorMonthBalance.add(json.getBigDecimal("debtorBalance"));
                    creditMonthBalance = creditMonthBalance.add(json.getBigDecimal("creditBalance"));
                }
                if (yearTime.equals(json.getInteger("yearTime"))) {
                    debtorYearBalance = debtorYearBalance.add(json.getBigDecimal("debtorBalance"));
                    creditYearBalance = creditYearBalance.add(json.getBigDecimal("creditBalance"));
                }
                monthTime = json.getInteger("monthTime");
                yearTime = json.getInteger("yearTime");
            } else if (json.getInteger("type") == 3) {
                if (accountBO.getAccountBookDirection() == 1) {
                    if (initialBalance.compareTo(new BigDecimal(0)) == 0) {
                        json.put("balanceDirection", "平");
                    } else {
                        if (accountBO.getBalanceDirection() == 1) {
                            json.put("balanceDirection", "借");
                        } else {
                            json.put("balanceDirection", "贷");
                        }
                    }
                    json.put("balance", initialBalance);
                } else {
                    if (Objects.equals("平", balanceDirection) || Objects.equals("借", balanceDirection)) {
                        if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                            json.put("balance", new BigDecimal("0").subtract(initialBalance));
                            json.put("balanceDirection", "贷");
                        } else {
                            if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                json.put("balanceDirection", "借");
                            } else {
                                json.put("balanceDirection", "平");
                            }
                            json.put("balance", initialBalance);
                        }
                    } else {
                        if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                            json.put("balance", new BigDecimal("0").subtract(initialBalance));
                            json.put("balanceDirection", "借");
                        } else {
                            if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                json.put("balanceDirection", "贷");
                            } else {
                                json.put("balanceDirection", "平");
                            }
                            json.put("balance", initialBalance);
                        }
                    }
                }
                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                    } else {
                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                            testBig = new BigDecimal("1");
                        }
                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                    }
                    json.put("balanceQuantity", balanceQuantity);
                }
            } else if (json.getInteger("type") == 4) {

                if (accountBO.getAccountBookDirection() == 1) {
                    if (initialBalance.compareTo(new BigDecimal(0)) == 0) {
                        json.put("balanceDirection", "平");
                    } else {
                        if (accountBO.getBalanceDirection() == 1) {
                            json.put("balanceDirection", "借");
                        } else {
                            json.put("balanceDirection", "贷");
                        }
                    }
                    json.put("balance", initialBalance);
                } else {
                    if (Objects.equals("平", balanceDirection) || Objects.equals("借", balanceDirection)) {
                        if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                            json.put("balance", new BigDecimal("0").subtract(initialBalance));
                            json.put("balanceDirection", "贷");
                        } else {
                            if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                json.put("balanceDirection", "借");
                            } else {
                                json.put("balanceDirection", "平");
                            }
                            json.put("balance", initialBalance);
                        }
                    } else {
                        if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                            json.put("balance", new BigDecimal("0").subtract(initialBalance));
                            json.put("balanceDirection", "借");
                        } else {
                            if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                json.put("balanceDirection", "贷");
                            } else {
                                json.put("balanceDirection", "平");
                            }
                            json.put("balance", initialBalance);
                        }
                    }


                }
                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                    } else {
                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                            testBig = new BigDecimal("1");
                        }
                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                    }
                    json.put("balanceQuantity", balanceQuantity);
                }

            } else if (json.getInteger("type") == 5) {

                if (accountBO.getAccountBookDirection() == 1) {
                    if (initialBalance.compareTo(new BigDecimal(0)) == 0) {
                        json.put("balanceDirection", "平");
                    } else {
                        if (accountBO.getBalanceDirection() == 1) {
                            json.put("balanceDirection", "借");
                        } else {
                            json.put("balanceDirection", "贷");
                        }
                    }
                    json.put("balance", initialBalance);
                } else {
                    if (Objects.equals("平", balanceDirection) || Objects.equals("借", balanceDirection)) {
                        if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                            json.put("balance", new BigDecimal("0").subtract(initialBalance));
                            json.put("balanceDirection", "贷");
                        } else {
                            if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                json.put("balanceDirection", "借");
                            } else {
                                json.put("balanceDirection", "平");
                            }
                            json.put("balance", initialBalance);
                        }
                    } else {
                        if (initialBalance.compareTo(new BigDecimal(0)) == -1) {
                            json.put("balance", new BigDecimal("0").subtract(initialBalance));
                            json.put("balanceDirection", "借");
                        } else {
                            if (initialBalance.compareTo(new BigDecimal(0)) == 1) {
                                json.put("balanceDirection", "贷");
                            } else {
                                json.put("balanceDirection", "平");
                            }
                            json.put("balance", initialBalance);
                        }
                    }


                }
                if (accountBO.getIsQuantity() != null && accountBO.getIsQuantity() == 1) {
                    if ((balanceQuantity.compareTo(new BigDecimal("0")) < 0
                            && initialBalance.compareTo(new BigDecimal(0)) < 0) ||
                            (balanceQuantity.compareTo(new BigDecimal("0")) > 0
                                    && initialBalance.compareTo(new BigDecimal(0)) > 0)) {
                        json.put("balanceAmountUnit", initialBalance.divide(balanceQuantity, BigDecimal.ROUND_CEILING).abs());
                    } else {
                        BigDecimal testBig = new BigDecimal("0").subtract(balanceQuantity);
                        if (testBig.compareTo(new BigDecimal("0")) == 0) {
                            testBig = new BigDecimal("1");
                        }
                        json.put("balanceAmountUnit", initialBalance.divide(testBig, BigDecimal.ROUND_CEILING).abs());
                    }
                    json.put("balanceQuantity", balanceQuantity);
                }

            } else {
                continue;
            }
        }
    }

    @Override
    public JSONObject queryNumByTime(FinanceCertificateBO financeCertificateBO) {
        FinanceCertificate certificate = BeanUtil.copyProperties(financeCertificateBO, FinanceCertificate.class);
        FinanceCertificate financeCertificate = getBaseMapper().queryByTime(certificate);
        JSONObject json = new JSONObject();
        if (financeCertificate == null) {
            json.put("certificateNum", 1);
        } else {
            json.put("certificateNum", financeCertificate.getCertificateNum() + 1);
        }
        return json;
    }

    @Override
    public void insertCertificate(FinanceCertificateInsertBO insertBO) {
        FinanceCertificate certificate = getBaseMapper().queryCertificate(insertBO);

        if (certificate != null) {
            certificate.setCertificateNum(insertBO.getInsertCertificateNum());
            updateById(certificate);
        }
    }

    @Override
    public JSONObject queryCertificateTime() {
        return getBaseMapper().queryCertificateTime(AccountSet.getAccountSetId());
    }

    @Override
    public void certificateSettle(FinanceCertificateSettleBO settleBO) {

        LambdaQueryWrapper<FinanceVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId());
        if (settleBO.getVoucherId() != null) {
            wrapper.eq(FinanceVoucher::getVoucherId, settleBO.getVoucherId());
        }
        List<FinanceVoucher> vouchers = financeVoucherService.list(wrapper);
        settleBO.setAccountId(AccountSet.getAccountSetId());
        for (FinanceVoucher voucher : vouchers) {
            settleBO.setVoucherId(voucher.getVoucherId());
            Integer certificateNum = settleBO.getCertificateNum();
            List<FinanceCertificate> certificates = getBaseMapper().queryListByNum(settleBO);
            for (FinanceCertificate certificate : certificates) {
                if (certificateNum == null) {
                    certificateNum = 1;
                    lambdaUpdate().set(FinanceCertificate::getCertificateNum, certificateNum).eq(FinanceCertificate::getCertificateId, certificate.getCertificateId()).update();
                    certificateNum = certificateNum + 1;
                } else {
                    lambdaUpdate().set(FinanceCertificate::getCertificateNum, certificateNum).eq(FinanceCertificate::getCertificateId, certificate.getCertificateId()).update();
                    certificateNum = certificateNum + 1;
                }
            }
        }
    }

    @Override
    public JSONObject queryCertificateNum(FinanceCertificateSettleBO settleBO) {
        settleBO.setAccountId(AccountSet.getAccountSetId());
        settleBO.setCertificateNum(1);
        settleBO.setType(1);
        JSONObject json = new JSONObject();
        List<FinanceCertificate> certificates = getBaseMapper().queryListByNum(settleBO);
        if (certificates.size() == 0) {
            json.put("certificateNum", 1);
        } else {
            json.put("certificateNum", certificates.get(certificates.size() - 1).getCertificateNum() + 1);
        }
        return json;
    }

    @Override
    public List<JSONObject> querySubjectBalance(Long accountId, String fromPeriod, String toPeriod) {
        FinanceParameter parameter = parameterService.getParameter();
        if (ObjectUtil.isEmpty(parameter)) {
            return new ArrayList<>();
        }
        // 初始账期
        LocalDateTime startTime = parameter.getStartTime();
        String startPeriod = LocalDateTimeUtil.format(startTime, DatePattern.SIMPLE_MONTH_PATTERN);
        Date startDate = DateUtil.parse(startPeriod, DatePattern.SIMPLE_MONTH_FORMAT);
        DateTime toPeriodDate = DateUtil.parse(toPeriod, PeriodUtils.PERIOD_FORMAT);
        List<JSONObject> result = new ArrayList<>();
        if (startDate.after(toPeriodDate)) {
            return result;
        }
        // 本年第一个月
        String firstMonth = PeriodUtils.getFirstMonth(toPeriod);
        // 是否是第一年
        boolean isFirstYear = DateUtil.year(startDate) == DateUtil.year(toPeriodDate);
        // 查询从初始到现在的发生额
        List<JSONObject> allBalance = baseMapper.queryCurrentBalanceNoSettle(accountId, startPeriod, toPeriod);
        // 本年的发生额
        List<JSONObject> currentYearBalance = allBalance.stream()
                .filter(b -> b.getInteger("period") >= Integer.parseInt(firstMonth)).collect(Collectors.toList());
        // 本年之前的发生额
        List<JSONObject> beforeYearBalance = allBalance.stream()
                .filter(b -> b.getInteger("period") < Integer.parseInt(firstMonth)).collect(Collectors.toList());
        Map<Long, List<JSONObject>> currentBalanceGroupBySubjectId = currentYearBalance.stream().collect(Collectors.groupingBy(o -> o.getLong("subjectId")));
        Map<Long, List<JSONObject>> beforeBalanceGroupBySubjectId = beforeYearBalance.stream().collect(Collectors.groupingBy(o -> o.getLong("subjectId")));

        // 查询当前账期的发生额
        List<JSONObject> amount = baseMapper.queryAccumulatedAmountNoSettle(accountId, fromPeriod, toPeriod);
        // 获取所有辅助核算的初始余额
        List<FinanceInitial> initials = initialService.queryAll(AccountSet.getAccountSetId());
        //所有非辅助核算的初始余额
        List<FinanceInitial> initialsNotAssist = initials.stream().filter(o -> !o.getIsAssist()).collect(Collectors.toList());
        Map<Long, FinanceInitial> subjectIdInitialMap = initialService.listToMap(initialsNotAssist);
        try {
            for (JSONObject o : amount) {
                calculateSubjectBalance(isFirstYear, o, amount, subjectIdInitialMap,
                        currentBalanceGroupBySubjectId, beforeBalanceGroupBySubjectId);
            }
            return amount;
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }

    private void calculateSubjectBalance(boolean isFirstYear,
                                         JSONObject object,
                                         List<JSONObject> amount,
                                         Map<Long, FinanceInitial> subjectIdInitialMap,
                                         Map<Long, List<JSONObject>> currentBalanceGroupBySubjectId,
                                         Map<Long, List<JSONObject>> beforeBalanceGroupBySubjectId) {
        Long subjectId = object.getLong("subjectId");
        // 科目方向
        Integer balanceDirection = object.getInteger("balanceDirection");

        List<Long> childSubjectIds = CashFlowStatementReportHolder.getFirstChildSubjectIds(subjectId);
        // 获取子科目
        List<JSONObject> subList = amount.stream()
                .filter(a -> (childSubjectIds.contains(a.getLong("subjectId")))).collect(Collectors.toList());

        // 年初余额
        BigDecimal beginningBalance = new BigDecimal(0);
        // 借方期初始余额
        BigDecimal debtorInitialBalance = new BigDecimal(0);
        // 贷方期初始余额
        BigDecimal creditInitialBalance = new BigDecimal(0);
        // 本期借方发生额
        BigDecimal debtorCurrentBalance = new BigDecimal(0);
        // 本期贷方发生额
        BigDecimal creditCurrentBalance = new BigDecimal(0);
        // 借方期末余额
        BigDecimal debtorEndBalance = new BigDecimal(0);
        // 贷方期末余额
        BigDecimal creditEndBalance = new BigDecimal(0);
        // 借方本年累计
        BigDecimal debtorYearBalance = new BigDecimal(0);
        // 贷方本年累计
        BigDecimal creditYearBalance = new BigDecimal(0);
        // 借方本年累计-初始
        BigDecimal initDebtorYearBalance = new BigDecimal(0);
        // 贷方本年累计-初始
        BigDecimal initCreditYearBalance = new BigDecimal(0);

        // 借方本年累计-实际发生额
        BigDecimal debtorYearRealBalance = new BigDecimal(0);
        // 贷方本年累计-实际发生额
        BigDecimal creditYearRealBalance = new BigDecimal(0);

        // 借方本年之前累计-实际发生额
        BigDecimal debtorYearRealBeforeBalance = new BigDecimal(0);
        // 贷方本年之前累计-实际发生额
        BigDecimal creditYearRealBeforeBalance = new BigDecimal(0);
        // 本年之前的发生额
        List<JSONObject> beforeBalances = beforeBalanceGroupBySubjectId.get(subjectId);
        // 科目的初始余额
        FinanceInitial initial = subjectIdInitialMap.get(subjectId);
        if (ObjectUtil.isNotNull(initial)) {
            if (CollUtil.isEmpty(subList)) {
                if (isFirstYear) {
                    beginningBalance = initial.getBeginningBalance();
                } else {
                    beginningBalance = initial.getInitialBalance();
                }
            }
            if (ObjectUtil.equal(1, balanceDirection)) {
                debtorInitialBalance = Optional.ofNullable(initial.getInitialBalance()).orElse(BigDecimal.ZERO);

            } else {
                creditInitialBalance = Optional.ofNullable(initial.getInitialBalance()).orElse(BigDecimal.ZERO);

            }
            initDebtorYearBalance = initial.getAddUpDebtorBalance();
            initCreditYearBalance = initial.getAddUpCreditBalance();
        }
        if (CollUtil.isNotEmpty(beforeBalances)) {
            debtorYearRealBeforeBalance = beforeBalances.stream()
                    .map(b -> Optional.ofNullable(b.getBigDecimal("debtorCurrentBalance")).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            creditYearRealBeforeBalance = beforeBalances.stream()
                    .map(b -> Optional.ofNullable(b.getBigDecimal("creditCurrentBalance")).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (balanceDirection == 1) {
                beginningBalance = beginningBalance.add(debtorYearRealBeforeBalance).subtract(creditYearRealBeforeBalance);
            } else {
                beginningBalance = beginningBalance.add(creditYearRealBeforeBalance).subtract(debtorYearRealBeforeBalance);
            }
        }
        if (CollUtil.isEmpty(subList)) {
            // 本期借方发生额
            debtorCurrentBalance = object.getBigDecimal("debtorCurrentBalance");
            // 本期贷方发生额
            creditCurrentBalance = object.getBigDecimal("creditCurrentBalance");

            // 第一年本年累计计算需要加上初始余额的值
            if (isFirstYear) {
                debtorYearBalance = debtorYearBalance.add(initDebtorYearBalance);
                creditYearBalance = creditYearBalance.add(initCreditYearBalance);
            }
            // 本年的发生额
            List<JSONObject> currentBalances = currentBalanceGroupBySubjectId.get(subjectId);
            if (CollUtil.isNotEmpty(currentBalances)) {
                debtorYearRealBalance = currentBalances.stream()
                        .map(b -> Optional.ofNullable(b.getBigDecimal("debtorCurrentBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                debtorYearBalance = debtorYearBalance.add(debtorYearRealBalance);

                creditYearRealBalance = currentBalances.stream()
                        .map(b -> Optional.ofNullable(b.getBigDecimal("creditCurrentBalance")).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                creditYearBalance = creditYearBalance.add(creditYearRealBalance);
            }
            if (balanceDirection == 1) {
                debtorEndBalance = debtorInitialBalance
                        .add(debtorYearRealBalance).subtract(creditYearRealBalance)
                        .add(debtorYearRealBeforeBalance).subtract(creditYearRealBeforeBalance);
            } else {
                creditEndBalance = creditInitialBalance
                        .add(creditYearRealBalance).subtract(debtorYearRealBalance)
                        .add(creditYearRealBeforeBalance).subtract(debtorYearRealBeforeBalance);
            }
        } else {
            for (JSONObject jsonObject : subList) {
                Integer subBalanceDirection = jsonObject.getInteger("balanceDirection");
                // 计算子科目的科目余额
                this.calculateSubjectBalance(isFirstYear, jsonObject, amount, subjectIdInitialMap,
                        currentBalanceGroupBySubjectId, beforeBalanceGroupBySubjectId);
                // 本期借方发生额
                BigDecimal subDebtorCurrentBalance = jsonObject.getBigDecimal("debtorCurrentBalance");
                // 本期贷方发生额
                BigDecimal subCreditCurrentBalance = jsonObject.getBigDecimal("creditCurrentBalance");

                // 借方期末余额
                BigDecimal subDebtorEndBalance = jsonObject.getBigDecimal("debtorEndBalance");
                // 贷方期末余额
                BigDecimal subCreditEndBalance = jsonObject.getBigDecimal("creditEndBalance");

                // 借方本年累计
                BigDecimal subDebtorYearBalance = jsonObject.getBigDecimal("debtorYearBalance");
                // 贷方本年累计
                BigDecimal subCreditYearBalance = jsonObject.getBigDecimal("creditYearBalance");
                // 年初余额
                BigDecimal subBeginningBalance = jsonObject.getBigDecimal("beginningBalance");


                debtorCurrentBalance = debtorCurrentBalance.add(subDebtorCurrentBalance);
                creditCurrentBalance = creditCurrentBalance.add(subCreditCurrentBalance);
                debtorYearBalance = debtorYearBalance.add(subDebtorYearBalance);
                creditYearBalance = creditYearBalance.add(subCreditYearBalance);
                beginningBalance = beginningBalance.add(subBeginningBalance);

                if (ObjectUtil.equal(balanceDirection, subBalanceDirection)) {
                    debtorEndBalance = debtorEndBalance.add(subDebtorEndBalance);
                    creditEndBalance = creditEndBalance.add(subCreditEndBalance);

                } else {
                    debtorEndBalance = debtorEndBalance.subtract(subDebtorEndBalance);
                    creditEndBalance = creditEndBalance.subtract(subCreditEndBalance);
                }
                // 借
                if (ObjectUtil.equal(1, balanceDirection)) {
                    debtorEndBalance = debtorEndBalance.add(creditEndBalance);
                    creditEndBalance = BigDecimal.ZERO;
                } else {
                    creditEndBalance = debtorEndBalance.add(creditEndBalance);
                    debtorEndBalance = BigDecimal.ZERO;
                }
            }
        }
        object.put("debtorCurrentBalance", debtorCurrentBalance);
        object.put("creditCurrentBalance", creditCurrentBalance);
        object.put("debtorYearBalance", debtorYearBalance);
        object.put("creditYearBalance", creditYearBalance);
        object.put("debtorEndBalance", debtorEndBalance);
        object.put("creditEndBalance", creditEndBalance);
        object.put("debtorInitialBalance", debtorInitialBalance);
        object.put("creditInitialBalance", creditInitialBalance);
        object.put("beginningBalance", beginningBalance);
        object.put("initDebtorYearBalance", initDebtorYearBalance);
        object.put("initCreditYearBalance", initCreditYearBalance);
    }

    @Override
    public List<JSONObject> queryAmountDetailAccount(FinanceDetailAccountBO accountBO) {
        if (accountBO.getSubjectId() == null) {
            return new ArrayList<>();
        }
        accountBO.setAccountId(AccountSet.getAccountSetId());
        FinanceSubject subject = financeSubjectService.getById(accountBO.getSubjectId());
        accountBO.setBalanceDirection(subject.getBalanceDirection());
        accountBO.setIsBalanceDirection(subject.getBalanceDirection());
        //获取符合条件的科目id
        List<Long> subjectIds = financeSubjectService.queryIds(accountBO.getSubjectId(), accountBO.getMinLevel(), accountBO.getMaxLevel());
        List<Long> ids = financeSubjectService.queryIds();
        subjectIds.retainAll(ids);
        if (subjectIds.size() == 0) {
            return new ArrayList<>();
        }
        accountBO.setSubjectIds(subjectIds);
        BiParams biParams = new BiParams();
        biParams.setStartTime(accountBO.getStartTime());
        biParams.setEndTime(accountBO.getEndTime());
        BiTimeUtil.BiTimeEntity record = BiTimeUtil.analyzeTypeTime(biParams);
        accountBO.setStartTime(DateUtil.formatDate(record.getBeginDate()));
        accountBO.setEndTime(DateUtil.formatDate(record.getEndDate()));
        Integer beginTime = record.getBeginTime();
        Integer cycleNum = record.getCycleNum();
        List<Integer> timeList = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            timeList.add(beginTime);
            beginTime = BiTimeUtil.estimateTime(beginTime);
        }
        accountBO.setTimeList(timeList);

        JSONObject paramnter = parameterService.queryParameter();
        if (paramnter != null) {
            accountBO.setAccountBookDirection(paramnter.getInteger("accountBookDirection"));
        } else {
            accountBO.setAccountBookDirection(1);
        }
        accountBO.setIsQuantity(1);
        List<JSONObject> detailAccounts = getBaseMapper().queryAmountDetailAccount(accountBO);
        calculate(detailAccounts, accountBO);
        return detailAccounts;
    }

    @Override
    public List<JSONObject> queryAmountDetailUpAccount(FinanceDetailAccountBO accountBO) {
        FinanceCollectCertificateBO certificateBO = new FinanceCollectCertificateBO();
        certificateBO.setStartTime(accountBO.getStartTime());
        certificateBO.setEndTime(accountBO.getEndTime());
        certificateBO.setAccountId(AccountSet.getAccountSetId());
        accountBO.setAccountId(AccountSet.getAccountSetId());
        List<JSONObject> subjects = financeSubjectService.queryIds(accountBO);
        BiParams biParams = new BiParams();
        biParams.setStartTime(accountBO.getStartTime());
        biParams.setEndTime(accountBO.getEndTime());
        BiTimeUtil.BiTimeEntity record = BiTimeUtil.analyzeTypeTime(biParams);
        accountBO.setStartTime(DateUtil.formatDate(record.getBeginDate()));
        accountBO.setEndTime(DateUtil.formatDate(record.getEndDate()));
        Integer beginTime = record.getBeginTime();
        Integer cycleNum = record.getCycleNum();
        List<Integer> timeList = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            timeList.add(beginTime);
            beginTime = BiTimeUtil.estimateTime(beginTime);
        }
        accountBO.setTimeList(timeList);
        List<Long> subjectIdList = getBaseMapper().querySubjectIdsByTime(certificateBO);
        List<Long> ids = financeSubjectService.queryIds();
        subjectIdList.retainAll(ids);
        JSONObject paramnter = parameterService.queryParameter();
        if (paramnter != null) {
            accountBO.setAccountBookDirection(paramnter.getInteger("accountBookDirection"));
        } else {
            accountBO.setAccountBookDirection(1);
        }
        List<JSONObject> subjects1 = new ArrayList<>();
        for (JSONObject json : subjects) {
            List<Long> subjectIds = json.getJSONArray("subjectIds").toJavaList(Long.class);
            subjectIds.add(json.getLong("subjectId"));
            if (!Collections.disjoint(subjectIdList, subjectIds)) {
                accountBO.setSubjectIds(subjectIds);
                accountBO.setSubjectId(json.getLong("subjectId"));
                accountBO.setBalanceDirection(json.getInteger("balanceDirection"));
                JSONObject accountUp = getBaseMapper().queryAmountDetailUpAccount(accountBO);
                if (accountUp != null) {
                    subjects1.add(accountUp);
                    Map<String, String> keyMap = new HashMap<>();
                    keyMap.put("subjectName_resourceKey", "finance.subject." + json.get("subjectName"));
                    keyMap.put("initialBalanceDirection_resourceKey", "finance.subject.balanceDirection." + accountUp.get("initialBalanceDirection"));
                    keyMap.put("endBalanceDirection_resourceKey", "finance.subject.balanceDirection." + accountUp.get("endBalanceDirection"));
                    keyMap.put("amountUnit_resourceKey", "finance.subject.amountUnit." + accountUp.get("amountUnit"));
                    accountUp.put("languageKeyMap", keyMap);
                }

            }
        }
        return subjects1;
    }

    /**
     * 核算项目余额表
     *
     * @param accountBO
     * @return
     */
    @Override
    public List<JSONObject> queryItemsDetailBalanceAccount(FinanceDetailAccountBO accountBO) {
        if (ObjectUtil.isEmpty(accountBO)) {
            return new ArrayList<>();
        }
        accountBO.setAccountId(AccountSet.getAccountSetId());
        if (accountBO.getSubjectId() != null) {
            List<Long> subjectIds = financeSubjectService.queryIds(accountBO.getSubjectId(), accountBO.getMinLevel(), accountBO.getMaxLevel());
            accountBO.setSubjectIds(subjectIds);
        }
        List<JSONObject> amount = baseMapper.queryAdjuvantDetailBalanceAccount(accountBO);
        // 获取所有初始余额
        accountBO.setDegree(1);
        List<JSONObject> initials = initialService.queryAdjuvantInitial(accountBO);
        // 辅助核算的初始余额
        List<JSONObject> initialsAssist = initials.stream()
                .filter(o -> ObjectUtil.isNotNull(o.getBoolean("isAssist")) && o.getBoolean("isAssist"))
                .collect(Collectors.toList());
        // 所有未删除的科目
        List<FinanceSubject> subjects = subjectService.lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .list();
        //处理新建账套初始化未完成没有获取到数据
        if (CollUtil.isEmpty(subjects)) {
            return new ArrayList<>();
        }
        // 年初始账期
        List<JSONObject> subjectList = new ArrayList<>();
        if (amount.size() == 0 && initials.size() > 0) {
            for (JSONObject initial : initials) {
                // 借方期初始余额
                BigDecimal debtorInitialBalance = new BigDecimal(0);
                // 贷方期初始余额
                BigDecimal creditInitialBalance = new BigDecimal(0);
                // 本期借方发生额
                BigDecimal debtorCurrentBalance = new BigDecimal(0);
                // 本期贷方发生额
                BigDecimal creditCurrentBalance = new BigDecimal(0);
                // 借方本年累计
                BigDecimal debtorYearBalance = initial.getBigDecimal("debtorYearBalance");
                // 贷方本年累计
                BigDecimal creditYearBalance = initial.getBigDecimal("creditYearBalance");
                // 借方期末余额
                BigDecimal debtorEndBalance = new BigDecimal(0);
                // 贷方期末余额
                BigDecimal creditEndBalance = new BigDecimal(0);
                if (Objects.equals(initial.getInteger("balanceDirection"), 1)) {
                    debtorInitialBalance = initial.getBigDecimal("initialBalance");
                    debtorEndBalance = initial.getBigDecimal("initialBalance");
                } else {
                    creditInitialBalance = initial.getBigDecimal("initialBalance");
                    creditEndBalance = initial.getBigDecimal("initialBalance");
                }
                initial.put("debtorCurrentBalance", debtorCurrentBalance);
                initial.put("creditCurrentBalance", creditCurrentBalance);
                initial.put("debtorYearBalance", debtorYearBalance);
                initial.put("creditYearBalance", creditYearBalance);
                initial.put("debtorEndBalance", debtorEndBalance);
                initial.put("creditEndBalance", creditEndBalance);
                initial.put("debtorInitialBalance", debtorInitialBalance);
                initial.put("creditInitialBalance", creditInitialBalance);
                subjectList.add(initial);
            }
            return subjectList;
        }
        try {
            CashFlowStatementReportHolder.setSubjects(subjects);
            for (JSONObject jsonObject : amount) {
                // 辅助核算ID
                Long relationId = jsonObject.getLong("relationId");
                // 科目方向
                Integer balanceDirection = jsonObject.getInteger("balanceDirection");
                // 借方期初始余额
                BigDecimal debtorInitialBalance = new BigDecimal(0);
                // 贷方期初始余额
                BigDecimal creditInitialBalance = new BigDecimal(0);
                // 本期借方发生额
                BigDecimal debtorCurrentBalance = new BigDecimal(0);
                // 本期贷方发生额
                BigDecimal creditCurrentBalance = new BigDecimal(0);
                // 本期年初借方发生额
                BigDecimal debtorLastYearCurrentBalance = new BigDecimal(0);
                // 本期年初贷方发生额
                BigDecimal creditLastYearCurrentBalance = new BigDecimal(0);
                // 借方期末余额
                BigDecimal debtorEndBalance = new BigDecimal(0);
                // 贷方期末余额
                BigDecimal creditEndBalance = new BigDecimal(0);
                // 借方本年累计
                BigDecimal debtorYearBalance = new BigDecimal(0);
                // 贷方本年累计
                BigDecimal creditYearBalance = new BigDecimal(0);
                // 借方年初余额
                BigDecimal debtorInitialYearBalance = new BigDecimal(0);
                // 贷方年初余额
                BigDecimal creditInitialYearBalance = new BigDecimal(0);

                // 借方期初发生额
                BigDecimal debtorLastCurrentBalance = jsonObject.getBigDecimal("debtorLastCurrentBalance");
                // 贷方年初余额
                BigDecimal creditLastCurrentBalance = jsonObject.getBigDecimal("creditLastCurrentBalance");

                JSONObject initial = initialsAssist.stream()
                        .filter(o -> ObjectUtil.equal(relationId, o.getLong("relationId"))).findFirst()
                        .orElse(null);
                // 初始余额
                BigDecimal initialBalance = new BigDecimal(0);
                if (ObjectUtil.isNotNull(initial)) {
                    initialBalance = Optional.ofNullable(initial.getBigDecimal("initialBalance")).orElse(BigDecimal.ZERO);
                    if (!Objects.equals(balanceDirection, initial.getInteger("balanceDirection"))) {
                        initialBalance = BigDecimal.ZERO.subtract(initialBalance);
                    }
                }
                if (balanceDirection == 1) {
                    debtorInitialBalance = debtorInitialBalance.add(initialBalance).add(debtorLastCurrentBalance).subtract(creditLastCurrentBalance);
                    debtorInitialYearBalance = debtorInitialYearBalance.add(initialBalance);
                } else {
                    creditInitialBalance = creditInitialBalance.add(initialBalance).add(creditLastCurrentBalance).subtract(debtorLastCurrentBalance);
                    creditInitialYearBalance = creditInitialYearBalance.add(initialBalance);
                }
                debtorCurrentBalance = debtorCurrentBalance.add(jsonObject.getBigDecimal("debtorCurrentBalance"));
                creditCurrentBalance = creditCurrentBalance.add(jsonObject.getBigDecimal("creditCurrentBalance"));
                debtorLastYearCurrentBalance = debtorLastYearCurrentBalance.add(jsonObject.getBigDecimal("debtorLastYearCurrentBalance"));
                creditLastYearCurrentBalance = creditLastYearCurrentBalance.add(jsonObject.getBigDecimal("creditLastYearCurrentBalance"));
                // 本年累计计算需要加上初始余额的值
                debtorYearBalance = debtorYearBalance
                        .add(jsonObject.getBigDecimal("debtorYearBalance"));
                if (ObjectUtil.isNotNull(initial)) {
                    debtorYearBalance = debtorYearBalance
                            .add(Optional.ofNullable(initial.getBigDecimal("debtorYearBalance")).orElse(BigDecimal.ZERO));
                }
                creditYearBalance = creditYearBalance
                        .add(jsonObject.getBigDecimal("creditYearBalance"));
                if (ObjectUtil.isNotNull(initial)) {
                    creditYearBalance = creditYearBalance
                            .add(Optional.ofNullable(initial.getBigDecimal("creditYearBalance")).orElse(BigDecimal.ZERO));
                }

                if (balanceDirection == 1) {
                    debtorEndBalance = debtorInitialBalance.add(debtorCurrentBalance).subtract(creditCurrentBalance);
                } else {
                    creditEndBalance = creditInitialBalance.add(creditCurrentBalance).subtract(debtorCurrentBalance);
                }
                jsonObject.put("debtorCurrentBalance", debtorCurrentBalance);
                jsonObject.put("creditCurrentBalance", creditCurrentBalance);
                jsonObject.put("debtorLastYearCurrentBalance", debtorLastYearCurrentBalance);
                jsonObject.put("creditLastYearCurrentBalance", creditLastYearCurrentBalance);
                jsonObject.put("debtorYearBalance", debtorYearBalance);
                jsonObject.put("creditYearBalance", creditYearBalance);
                jsonObject.put("debtorEndBalance", debtorEndBalance);
                jsonObject.put("creditEndBalance", creditEndBalance);
                jsonObject.put("debtorInitialBalance", debtorInitialBalance);
                jsonObject.put("creditInitialBalance", creditInitialBalance);
                jsonObject.put("debtorInitialYearBalance", debtorInitialYearBalance);
                jsonObject.put("creditInitialYearBalance", creditInitialYearBalance);

                jsonObject.put("debtorSumEndBalance", debtorEndBalance);
                jsonObject.put("creditSumEndBalance", creditEndBalance);
                jsonObject.put("debtorSumInitialBalance", debtorInitialBalance);
                jsonObject.put("creditSumInitialBalance", creditInitialBalance);
                jsonObject.put("debtorSumInitialYearBalance", debtorInitialYearBalance);
                jsonObject.put("creditSumInitialYearBalance", creditInitialYearBalance);

            }
            // 辅助核算科目
            for (JSONObject object : amount) {
                this.transDirect(object);
                if (subjectHasBalance(object)) {
                    subjectList.add(object);
                }
            }
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
        return subjectList;
    }


    /**
     * 核算获取辅助项目名称
     *
     * @param adjuvantId 辅助核算id
     * @return 辅助核算名称
     */
    @Override
    public List<JSONObject> queryLabelName(Long adjuvantId) {
        List<FinanceAdjuvantCarte> cartes = carteService.lambdaQuery().eq(FinanceAdjuvantCarte::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceAdjuvantCarte::getAdjuvantId, adjuvantId).list();
        List<JSONObject> jsonObjects = new ArrayList<>();
        cartes.forEach(js -> {
            JSONObject jsons = new JSONObject();
            jsons.put("name", js.getCarteName());
            jsons.put("relationId", js.getCarteId());
            jsons.put("carteNumber", js.getCarteNumber());
            jsonObjects.add(jsons);
        });
        return jsonObjects;
    }

    /**
     * 核算项目明细账
     *
     * @param accountBO
     * @return
     */
    @Override
    public List<JSONObject> queryItemsDetailAccount(FinanceDetailAccountBO accountBO) {
        if (accountBO.getRelationId() == null) {
            return new ArrayList<>();
        }
        accountBO.setAccountId(AccountSet.getAccountSetId());
        if (accountBO.getSubjectId() != null) {
            List<Long> subjectIds = financeSubjectService.queryIds(accountBO.getSubjectId(), accountBO.getMinLevel(), accountBO.getMaxLevel());
            accountBO.setSubjectIds(subjectIds);
        }
        // 获取所有初始余额
        List<JSONObject> initials = initialService.queryAdjuvantInitial(accountBO);
        // 获取开始到结束的所有时间
        BiParams biParams = new BiParams();
        biParams.setStartTime(accountBO.getStartTime());
        biParams.setEndTime(accountBO.getEndTime());
        BiTimeUtil.BiTimeEntity record = BiTimeUtil.analyzeTypeTime(biParams);
        accountBO.setStartTime(DateUtil.formatDate(record.getBeginDate()));
        accountBO.setEndTime(DateUtil.formatDate(record.getEndDate()));
        Integer beginTime = record.getBeginTime();
        Integer cycleNum = record.getCycleNum();
        List<Integer> timeList = new ArrayList<>();
        for (int i = 1; i <= cycleNum; i++) {
            timeList.add(beginTime);
            beginTime = BiTimeUtil.estimateTime(beginTime);
        }
        accountBO.setTimeList(timeList);
        List<JSONObject> detailAccounts = getBaseMapper().queryItemsDetailAccount(accountBO);
        // 处理数据
        itemsDetailAccountCalculate(detailAccounts, initials, accountBO);
        return detailAccounts;
    }

    /**
     * 核算获取存在凭证的辅助项目名称
     *
     * @param association 辅助核算id
     * @return 辅助核算名称
     */
    @Override
    public List<JSONObject> queryLabelNameByData(FinanceCertificateAssociationBO association) {
        association.setAccountId(AccountSet.getAccountSetId());
        association.setStartTime(DateUtil.parse(association.getStartTime(), DatePattern.SIMPLE_MONTH_PATTERN).toString());
        DateTime dateTime = DateUtil.endOfMonth(DateUtil.parse(association.getEndTime(), DatePattern.SIMPLE_MONTH_PATTERN));
        association.setEndTime(dateTime.toString());
        return getBaseMapper().queryLabelNameByData(association);
    }

    /**
     * 项目明细账树形结构
     *
     * @param startTime 当前期数
     * @return data
     */
    @Override
    public List<FinanceSubjectVO> itemsDetailTree(String startTime) {

        List<FinanceSubjectVO> list = getBaseMapper().queryExistDataSubject(startTime, AccountSet.getAccountSetId());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        //辅助核算信息
        Map<Long, List<JSONObject>> listMap = adjuvantCarteMapper.querySubjectAdjuvantByAccountId(AccountSet.getAccountSetId()).stream().collect(Collectors.groupingBy(data -> data.getLong("subjectId")));
        //补充详细信息
        list = list.stream().map(data -> {
            JSONObject object = JSON.parseObject(data.getSubjectName());
            FinanceSubjectVO vo = object.toJavaObject(FinanceSubjectVO.class);
            if (listMap.containsKey(vo.getSubjectId())) {
                vo.setAdjuvantList(listMap.get(vo.getSubjectId()));
            }
            return vo;
        }).collect(Collectors.toList());
        Map<Long, FinanceSubject> subjectMap = subjectService.queryAll().stream().collect(Collectors.toMap(FinanceSubject::getSubjectId, Function.identity()));
        Map<Long, List<FinanceSubjectVO>> collect = list.stream().sorted(Comparator.comparing(o -> new BigDecimal(o.getNumber()))).collect(Collectors.groupingBy(FinanceSubjectVO::getParentId));
        List<FinanceSubjectVO> dataList = new ArrayList<>();
        Map<Long, FinanceSubjectVO> subjectVOMap = new HashMap<>();
        for (Long parentId : collect.keySet()) {
            Long tempId = parentId;
            List<FinanceSubjectVO> subjectVOList = collect.get(parentId);
            while (true) {
                if (tempId == 0L) {
                    dataList.addAll(subjectVOList);
                    break;
                }
                FinanceSubjectVO subjectVO;
                if (subjectVOMap.containsKey(tempId)) {
                    subjectVO = subjectVOMap.get(tempId);
                    subjectVO.getChildren().addAll(subjectVOList);
                    subjectVO.getChildren().sort(Comparator.comparing(o -> Integer.valueOf(o.getNumber())));
                    subjectVOMap.put(tempId, subjectVO);
                    break;
                } else {
                    FinanceSubject financeSubject = subjectMap.get(tempId);
                    if (financeSubject == null) {
                        break;
                    }
                    subjectVO = BeanUtil.copyProperties(financeSubject, FinanceSubjectVO.class);
                    subjectVO.setChildren(new ArrayList<>(subjectVOList));
                    subjectVOMap.put(tempId, subjectVO);
                }
                subjectVOList = Collections.singletonList(subjectVO);
                tempId = subjectVO.getParentId();

            }
        }
        dataList.sort(Comparator.comparing(o -> new BigDecimal(o.getNumber())));
        return dataList;
    }

    /**
     * 导出凭证
     *
     * @param searchCertificateBO
     */
    @Override
    public void exportCertificate(FinanceSearchCertificateBO searchCertificateBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        searchCertificateBO.setPageType(0);
        //查询凭证数据
        List<FinanceCertificateVO> pageList = queryPage(searchCertificateBO).getList();
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollUtil.isNotEmpty(pageList)) {
            pageList.forEach(data -> {
                if (CollUtil.isNotEmpty(data.getCertificateDetails())) {
                    data.getCertificateDetails().forEach(tail -> {
                        Map<String, Object> map = BeanUtil.beanToMap(tail);
                        map.put("certificateTime", DateUtil.format(data.getCertificateTime(), DatePattern.NORM_DATE_PATTERN));
                        map.put("voucherName", data.getVoucherName());
                        map.put("voucherNum", data.getVoucherNum());
                        map.put("fileNum", data.getFileNum());
                        map.put("createUserName", data.getCreateUserName());
                        map.put("examineUserName", data.getExamineUserName());
                        if (ObjectUtil.isNull(tail.getDebtorBalance()) || tail.getDebtorBalance().compareTo(BigDecimal.ZERO) == 0) {
                            map.put("debtorBalance", "");
                        }
                        if (ObjectUtil.isNull(tail.getCreditBalance()) || tail.getCreditBalance().compareTo(BigDecimal.ZERO) == 0) {
                            map.put("creditBalance", "");
                        }
                        JSONObject jsonObject = JSONObject.parseObject(tail.getSubjectContent());
                        StringBuilder sb = new StringBuilder();
                        if (CollUtil.isNotEmpty(tail.getAssociationBOS())) {
                            tail.getAssociationBOS().forEach(ass -> {
                                sb.append("_" + ass.getCarteNumber() + ass.getName());
                            });
                        }
                        map.put("subjectName", jsonObject.get("subjectName") + sb.toString());
                        mapList.add(map);
                    });
                }
            });
        }
        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("certificateTime", "日期"));
        dataList.add(ExcelParseUtil.toEntity("voucherName", "凭证字"));
        dataList.add(ExcelParseUtil.toEntity("voucherNum", "凭证号"));
        dataList.add(ExcelParseUtil.toEntity("digestContent", "摘要"));
        dataList.add(ExcelParseUtil.toEntity("subjectNumber", "科目代码"));
        dataList.add(ExcelParseUtil.toEntity("subjectName", "科目名称"));
        dataList.add(ExcelParseUtil.toEntity("debtorBalance", "借方金额"));
        dataList.add(ExcelParseUtil.toEntity("creditBalance", "贷方金额"));
        dataList.add(ExcelParseUtil.toEntity("fileNum", "附件数"));
        dataList.add(ExcelParseUtil.toEntity("createUserName", "制单人"));
        dataList.add(ExcelParseUtil.toEntity("examineUserName", "审核人"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "凭证列表";
            }

            @Override
            public String addCompany() {
                return accountSet.getCompanyName();
            }

            @Override
            public String addPeriod() {
                FinanceDetailAccountBO accountBO = new FinanceDetailAccountBO();
                accountBO.setStartTime(searchCertificateBO.getStartTime());
                accountBO.setEndTime(searchCertificateBO.getEndTime());
                return formatFinanceStartTime(accountBO);
            }

            @Override
            public String addUnit() {
                return "";
            }
        }, dataList);
    }

    /**
     * 导出凭证汇总
     *
     * @param searchCertificateBO
     */
    @Override
    public void exportListByType(FinanceCollectCertificateBO searchCertificateBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        //查询凭证数据
        List<JSONObject> mapList = queryListCollect(searchCertificateBO);
        mapList.forEach(map -> {
            if (ObjectUtil.isNull(map.get("debtorBalances")) || Convert.toBigDecimal(map.get("debtorBalances")).compareTo(BigDecimal.ZERO) == 0) {
                map.put("debtorBalances", "");
            }
            if (ObjectUtil.isNull(map.get("creditBalances")) || Convert.toBigDecimal(map.get("creditBalances")).compareTo(BigDecimal.ZERO) == 0) {
                map.put("creditBalances", "");
            }
        });

        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("subjectNumber", "科目编码"));
        dataList.add(ExcelParseUtil.toEntity("subjectName", "科目名称"));
        dataList.add(ExcelParseUtil.toEntity("debtorBalances", "借方金额"));
        dataList.add(ExcelParseUtil.toEntity("creditBalances", "贷方金额"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "凭证汇总";
            }

            @Override
            public String addCompany() {
                return accountSet.getCompanyName();
            }

            @Override
            public String addPeriod() {
                FinanceDetailAccountBO accountBO = new FinanceDetailAccountBO();
                accountBO.setStartTime(searchCertificateBO.getStartTime());
                accountBO.setEndTime(searchCertificateBO.getEndTime());
                return formatFinanceStartTime(accountBO);
            }

            @Override
            public String addUnit() {
                return "";
            }
        }, dataList);
    }

    /**
     * 导出明细账
     *
     * @param accountBO
     */
    @Override
    public void exportDetailAccount(FinanceDetailAccountBO accountBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        //查询明细账数据
        List<JSONObject> mapList = queryDetailAccount(accountBO);
        //拼接科目名称
        subjectMethod(accountBO.getSubjectId(), mapList);
        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("subject", "科目"));
        dataList.add(ExcelParseUtil.toEntity("accountTime", "日期"));
        dataList.add(ExcelParseUtil.toEntity("certificateNum", "凭证字号"));
        dataList.add(ExcelParseUtil.toEntity("digestContent", "摘要"));
        dataList.add(ExcelParseUtil.toEntity("debtorBalance", "借方"));
        dataList.add(ExcelParseUtil.toEntity("creditBalance", "贷方"));
        dataList.add(ExcelParseUtil.toEntity("balanceDirection", "方向"));
        dataList.add(ExcelParseUtil.toEntity("balance", "余额"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "明细账";
            }

            @Override
            public String addCompany() {
                return accountSet.getCompanyName();
            }

            @Override
            public String addPeriod() {
                return formatFinanceStartTime(accountBO);
            }

            @Override
            public String addUnit() {
                return "";
            }
        }, dataList);
    }

    /**
     * 导出总账
     *
     * @param accountBO
     */
    @Override
    public void exportDetailUpAccount(FinanceDetailAccountBO accountBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        //查询总账数据
        List<JSONObject> mapList = queryDetailUpAccount(accountBO);
        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("number", "科目编码"));
        dataList.add(ExcelParseUtil.toEntity("subjectName", "科目名称"));
        dataList.add(ExcelParseUtil.toEntity("monthTime", "期间"));
        dataList.add(ExcelParseUtil.toEntity("digestContent", "摘要"));
        dataList.add(ExcelParseUtil.toEntity("debtorBalance", "借方"));
        dataList.add(ExcelParseUtil.toEntity("creditBalance", "贷方"));
        dataList.add(ExcelParseUtil.toEntity("balanceDirection", "方向"));
        dataList.add(ExcelParseUtil.toEntity("balance", "余额"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "总账";
            }

            @Override
            public String addCompany() {
                return accountSet.getCompanyName();
            }

            @Override
            public String addPeriod() {
                return formatFinanceStartTime(accountBO);
            }

            @Override
            public String addUnit() {
                return "";
            }
        }, dataList);
    }

    /**
     * 导出科目余额表
     *
     * @param queryBO
     */
    @Override
    public void exportDetailBalanceAccount(FinanceSubjectBalanceQueryBO queryBO) {
        //获取科目余额表数据
        List<JSONObject> mapList = querySubjectBalance(queryBO);
        List<JSONObject> resList = new ArrayList<>();
        String date = formatFinanceStartTime(queryBO);
        if (ObjectUtil.equal(queryBO.getIsLaunch(), 1)) {
            if (CollUtil.isNotEmpty(mapList)) {
                mapList.forEach(map -> {
                    resList.add(map);
                    getSubjects(map, resList);
                });
            }
        } else {
            resList.addAll(mapList);
        }
        exportMethod(resList, "科目余额表", 9, (writer, cellStyle, extraData) -> {
            writer.merge(2, 3, 0, 0, "科目编码", cellStyle);
            writer.merge(2, 3, 1, 1, "科目名称", cellStyle);
            writer.merge(2, 2, 2, 3, "期初余额", cellStyle);
            writer.merge(2, 2, 4, 5, "本期发生额", cellStyle);
            writer.merge(2, 2, 6, 7, "本年累计发生额", cellStyle);
            writer.merge(2, 2, 8, 9, "期末余额", cellStyle);
            writer.writeCellValue(2, 3, "借方").setStyle(cellStyle, 2, 3);
            writer.writeCellValue(3, 3, "贷方").setStyle(cellStyle, 3, 3);
            writer.writeCellValue(4, 3, "借方").setStyle(cellStyle, 4, 3);
            writer.writeCellValue(5, 3, "贷方").setStyle(cellStyle, 5, 3);
            writer.writeCellValue(6, 3, "借方").setStyle(cellStyle, 6, 3);
            writer.writeCellValue(7, 3, "贷方").setStyle(cellStyle, 7, 3);
            writer.writeCellValue(8, 3, "借方").setStyle(cellStyle, 8, 3);
            writer.writeCellValue(9, 3, "贷方").setStyle(cellStyle, 9, 3);
            writer.setCurrentRow(4);
            writer.addHeaderAlias("number", "科目编码");
            writer.addHeaderAlias("subjectName", "科目名称");
            writer.addHeaderAlias("debtorInitialBalance", "借方");
            writer.addHeaderAlias("creditInitialBalance", "贷方");
            writer.addHeaderAlias("debtorCurrentBalance", "借方2");
            writer.addHeaderAlias("creditCurrentBalance", "贷方2");
            writer.addHeaderAlias("debtorYearBalance", "借方3");
            writer.addHeaderAlias("creditYearBalance", "贷方3");
            writer.addHeaderAlias("debtorEndBalance", "借方4");
            writer.addHeaderAlias("creditEndBalance", "贷方4");
        }, date);
    }

    private void getSubjects(JSONObject object, List<JSONObject> res) {
        if (ObjectUtil.isEmpty(object.get("subjects"))) {
            return;
        }
        List<JSONObject> param = (List<JSONObject>) object.get("subjects");
        res.addAll(param);
        param.forEach(pa -> {
            getSubjects(pa, res);
        });
    }

    /**
     * 导出多栏账
     *
     * @param accountBO
     */
    @Override
    public void exportDiversification(FinanceDetailAccountBO accountBO) {
        //查询总账数据
        FinanceDiversificationVO diversificationVO = queryDiversification(accountBO);
        List<JSONObject> mapList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(diversificationVO)) {
            mapList = diversificationVO.getJsonObjects();
        }
        if (mapList == null) {
            mapList = new ArrayList<>();
        }
        //拼接科目名称
        subjectMethod(accountBO.getSubjectId(), mapList);
        int len = 7;
        if (accountBO.getHeadList().size() == 8) {
            if (CollUtil.isNotEmpty(Collections.singleton(accountBO.getHeadList().get(7).get("children")))) {
                List<Map<String, Object>> renMap = (List<Map<String, Object>>) accountBO.getHeadList().get(7).get("children");
                len = len + renMap.size();
            }
        }
        String date = formatFinanceStartTime(accountBO);
        exportMethod(mapList, "多栏账", len, (writer, cellStyle, extraData) -> {
            writer.merge(2, 3, 0, 0, "科目", cellStyle);
            writer.merge(2, 3, 1, 1, "日期", cellStyle);
            writer.merge(2, 3, 2, 2, "凭证字号", cellStyle);
            writer.merge(2, 3, 3, 3, "摘要", cellStyle);
            writer.merge(2, 3, 4, 4, "借方", cellStyle);
            writer.merge(2, 3, 5, 5, "贷方", cellStyle);
            writer.merge(2, 3, 6, 6, "方向", cellStyle);
            writer.merge(2, 3, 7, 7, "余额", cellStyle);
            writer.setCurrentRow(4);
            writer.addHeaderAlias("subject", "科目");
            writer.addHeaderAlias("accountTime", "日期");
            writer.addHeaderAlias("certificateNum", "凭证字号");
            writer.addHeaderAlias("digestContent", "摘要");
            writer.addHeaderAlias("debtorBalance", "借方");
            writer.addHeaderAlias("creditBalance", "贷方");
            writer.addHeaderAlias("balanceDirection", "方向");
            writer.addHeaderAlias("balance", "余额");
            if (accountBO.getHeadList().size() == 8) {
                if (CollUtil.isNotEmpty(Collections.singleton(accountBO.getHeadList().get(7).get("children")))) {
                    List<Map<String, Object>> renMap = (List<Map<String, Object>>) accountBO.getHeadList().get(7).get("children");
                    writer.merge(2, 2, 8, 7 + renMap.size(), accountBO.getHeadList().get(7).get("label"), cellStyle);
                    int i = 7;
                    for (Map<String, Object> map : renMap) {
                        i++;
                        writer.writeCellValue(i, 3, map.get("label")).setStyle(cellStyle, i, 3);
                        writer.addHeaderAlias(map.get("field").toString(), map.get("label").toString());
                    }
                }
            }
        }, date);
    }

    /**
     * 导出核算项目明细账
     *
     * @param accountBO
     */
    @Override
    public void exportItemsDetailAccount(FinanceDetailAccountBO accountBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        //查询核算项目明细账
        List<JSONObject> mapList = queryItemsDetailAccount(accountBO);
        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("accountTime", "日期"));
        dataList.add(ExcelParseUtil.toEntity("certificateNum", "凭证字号"));
        dataList.add(ExcelParseUtil.toEntity("digestContent", "摘要"));
        dataList.add(ExcelParseUtil.toEntity("debtorBalance", "借方"));
        dataList.add(ExcelParseUtil.toEntity("creditBalance", "贷方"));
        dataList.add(ExcelParseUtil.toEntity("balanceDirection", "方向"));
        dataList.add(ExcelParseUtil.toEntity("balance", "余额"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "核算项目明细账";
            }

            @Override
            public String addCompany() {
                return accountSet.getCompanyName();
            }

            @Override
            public String addPeriod() {
                return formatFinanceStartTime(accountBO);
            }

            @Override
            public String addUnit() {
                return "";
            }
        }, dataList);
    }

    /**
     * 导出核算项目余额表
     *
     * @param accountBO
     */
    @Override
    public void exportItemsDetailBalanceAccount(FinanceDetailAccountBO accountBO) {
        //查询核算项目余额表
        List<JSONObject> mapList = queryItemsDetailBalanceAccount(accountBO);
        //获取日期
        String date = formatFinanceStartTime(accountBO);
        exportMethod(mapList, "核算项目余额表", 9, (writer, cellStyle, extraData) -> {
            writer.merge(2, 3, 0, 0, "编码", cellStyle);
            writer.merge(2, 3, 1, 1, "项目名称", cellStyle);
            writer.merge(2, 2, 2, 3, "期初余额", cellStyle);
            writer.merge(2, 2, 4, 5, "本期发生额", cellStyle);
            writer.merge(2, 2, 6, 7, "本年累计发生额", cellStyle);
            writer.merge(2, 2, 8, 9, "期末余额", cellStyle);
            writer.writeCellValue(2, 3, "借方").setStyle(cellStyle, 2, 3);
            writer.writeCellValue(3, 3, "贷方").setStyle(cellStyle, 3, 3);
            writer.writeCellValue(4, 3, "借方").setStyle(cellStyle, 4, 3);
            writer.writeCellValue(5, 3, "贷方").setStyle(cellStyle, 5, 3);
            writer.writeCellValue(6, 3, "借方").setStyle(cellStyle, 6, 3);
            writer.writeCellValue(7, 3, "贷方").setStyle(cellStyle, 7, 3);
            writer.writeCellValue(8, 3, "借方").setStyle(cellStyle, 8, 3);
            writer.writeCellValue(9, 3, "贷方").setStyle(cellStyle, 9, 3);
            writer.setCurrentRow(4);
            writer.addHeaderAlias("carteNumber", "编码");
            writer.addHeaderAlias("name", "项目名称");
            writer.addHeaderAlias("debtorInitialBalance", "借方");
            writer.addHeaderAlias("creditInitialBalance", "贷方");
            writer.addHeaderAlias("debtorCurrentBalance", "借方2");
            writer.addHeaderAlias("creditCurrentBalance", "贷方2");
            writer.addHeaderAlias("debtorYearBalance", "借方3");
            writer.addHeaderAlias("creditYearBalance", "贷方3");
            writer.addHeaderAlias("debtorEndBalance", "借方4");
            writer.addHeaderAlias("creditEndBalance", "贷方4");
        }, date);
    }

    /**
     * 导出数量金额明细账
     *
     * @param accountBO
     */
    @Override
    public void exportAmountDetailAccount(FinanceDetailAccountBO accountBO) {
        //查询数量金额明细账
        List<JSONObject> mapList = queryAmountDetailAccount(accountBO);
        String date = formatFinanceStartTime(accountBO);
        exportMethod(mapList, "数量金额明细账", 12, (writer, cellStyle, extraData) -> {
            writer.merge(2, 3, 0, 0, "日期", cellStyle);
            writer.merge(2, 3, 1, 1, "凭证字号", cellStyle);
            writer.merge(2, 3, 2, 2, "摘要", cellStyle);
            writer.merge(2, 2, 3, 5, "借方发生额", cellStyle);
            writer.merge(2, 2, 6, 8, "贷方发生额", cellStyle);
            writer.merge(2, 2, 9, 12, "余额", cellStyle);
            writer.writeCellValue(3, 3, "数量").setStyle(cellStyle, 3, 3);
            writer.writeCellValue(4, 3, "单价").setStyle(cellStyle, 4, 3);
            writer.writeCellValue(5, 3, "金额").setStyle(cellStyle, 5, 3);
            writer.writeCellValue(6, 3, "数量").setStyle(cellStyle, 6, 3);
            writer.writeCellValue(7, 3, "单价").setStyle(cellStyle, 7, 3);
            writer.writeCellValue(8, 3, "金额").setStyle(cellStyle, 8, 3);
            writer.writeCellValue(9, 3, "方向").setStyle(cellStyle, 9, 3);
            writer.writeCellValue(10, 3, "数量").setStyle(cellStyle, 10, 3);
            writer.writeCellValue(11, 3, "单价").setStyle(cellStyle, 11, 3);
            writer.writeCellValue(12, 3, "金额").setStyle(cellStyle, 12, 3);
            writer.setCurrentRow(4);
            writer.addHeaderAlias("accountTime", "日期");
            writer.addHeaderAlias("certificateNum", "凭证字号");
            writer.addHeaderAlias("digestContent", "摘要");
            writer.addHeaderAlias("debtorQuantity", "数量");
            writer.addHeaderAlias("debtorAmountUnit", "单价");
            writer.addHeaderAlias("debtorBalance", "金额");
            writer.addHeaderAlias("creditQuantity", "数量1");
            writer.addHeaderAlias("creditAmountUnit", "单价1");
            writer.addHeaderAlias("balanceDirection", "方向");
            writer.addHeaderAlias("balanceQuantity", "数量2");
            writer.addHeaderAlias("balanceAmountUnit", "单价2");
            writer.addHeaderAlias("balance", "金额2");
        }, date);
    }

    /**
     * 导出数量总账
     *
     * @param accountBO
     */
    @Override
    public void exportAmountDetailUpAccount(FinanceDetailAccountBO accountBO) {
        //查询数量总账
        List<JSONObject> mapList = queryAmountDetailUpAccount(accountBO);
        String date = formatFinanceStartTime(accountBO);
        exportMethod(mapList, "数量金额总账", 18, (writer, cellStyle, extraData) -> {
            writer.merge(2, 3, 0, 0, "科目编码", cellStyle);
            writer.merge(2, 3, 1, 1, "科目名称", cellStyle);
            writer.merge(2, 3, 2, 2, "单位", cellStyle);
            writer.merge(2, 2, 3, 6, "期初余额", cellStyle);
            writer.merge(2, 2, 7, 8, "本期借方", cellStyle);
            writer.merge(2, 2, 9, 10, "本期贷方", cellStyle);
            writer.merge(2, 2, 11, 12, "本年累计借方", cellStyle);
            writer.merge(2, 2, 13, 14, "本年累计贷方", cellStyle);
            writer.merge(2, 2, 15, 18, "期末余额", cellStyle);
            writer.writeCellValue(3, 3, "方向").setStyle(cellStyle, 3, 3);
            writer.writeCellValue(4, 3, "数量").setStyle(cellStyle, 4, 3);
            writer.writeCellValue(5, 3, "单价").setStyle(cellStyle, 5, 3);
            writer.writeCellValue(6, 3, "金额").setStyle(cellStyle, 6, 3);
            writer.writeCellValue(7, 3, "数量").setStyle(cellStyle, 7, 3);
            writer.writeCellValue(8, 3, "金额").setStyle(cellStyle, 8, 3);
            writer.writeCellValue(9, 3, "数量").setStyle(cellStyle, 9, 3);
            writer.writeCellValue(10, 3, "金额").setStyle(cellStyle, 10, 3);
            writer.writeCellValue(11, 3, "数量").setStyle(cellStyle, 11, 3);
            writer.writeCellValue(12, 3, "金额").setStyle(cellStyle, 12, 3);
            writer.writeCellValue(13, 3, "数量").setStyle(cellStyle, 13, 3);
            writer.writeCellValue(14, 3, "金额").setStyle(cellStyle, 14, 3);
            writer.writeCellValue(15, 3, "方向").setStyle(cellStyle, 15, 3);
            writer.writeCellValue(16, 3, "数量").setStyle(cellStyle, 16, 3);
            writer.writeCellValue(17, 3, "单价").setStyle(cellStyle, 17, 3);
            writer.writeCellValue(18, 3, "金额").setStyle(cellStyle, 18, 3);
            writer.setCurrentRow(4);
            writer.addHeaderAlias("number", "科目编码");
            writer.addHeaderAlias("subjectName", "科目名称");
            writer.addHeaderAlias("amountUnit", "单位");
            writer.addHeaderAlias("initialBalanceDirection", "方向");
            writer.addHeaderAlias("initialQuantity", "数量");
            writer.addHeaderAlias("initialUnitPrice", "单价");
            writer.addHeaderAlias("initialBalance", "金额");
            writer.addHeaderAlias("debtorQuantity", "数量1");
            writer.addHeaderAlias("debtorBalance", "金额1");
            writer.addHeaderAlias("creditQuantity", "数量2");
            writer.addHeaderAlias("creditBalance", "金额2");
            writer.addHeaderAlias("debtorYearQuantity", "数量3");
            writer.addHeaderAlias("debtorYearBalance", "金额3");
            writer.addHeaderAlias("creditYearQuantity", "数量4");
            writer.addHeaderAlias("creditYearBalance", "金额4");
            writer.addHeaderAlias("endBalanceDirection", "方向5");
            writer.addHeaderAlias("endQuantity", "数量5");
            writer.addHeaderAlias("initialEndPrice", "单价5");
            writer.addHeaderAlias("endBalance", "金额5");
        }, date);
    }

    /**
     * 导出财务初始余额
     *
     * @param bo
     */
    @Override
    public void exportPageBySubjectType(FinanceInitialPageBO bo) {
        List<JSONObject> dataList = new ArrayList<>();
        //查询数量总账
        bo.setSubjectType(1);
        List<JSONObject> mapList = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(2);
        List<JSONObject> mapList1 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(3);
        List<JSONObject> mapList2 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(4);
        List<JSONObject> mapList3 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(5);
        List<JSONObject> mapList4 = initialService.queryListBySubjectType(bo).getList();
        dataList.addAll(mapList);
        dataList.addAll(mapList1);
        dataList.addAll(mapList2);
        dataList.addAll(mapList3);
        dataList.addAll(mapList4);
        for (JSONObject object : dataList) {
            if (ObjectUtil.isNotEmpty(object.get("balanceDirection"))) {
                if (ObjectUtil.equal(1, object.get("balanceDirection"))) {
                    object.put("balanceDirection", "借");
                } else {
                    object.put("balanceDirection", "贷");
                }
            }
            if (ObjectUtil.equals(object.get("initialNum"), 0)) {
                object.put("initialNum", "");
            }
            if (ObjectUtil.equals(object.get("addUpDebtorNum"), 0)) {
                object.put("addUpDebtorNum", "");
            }
            if (ObjectUtil.equals(object.get("addUpCreditNum"), 0)) {
                object.put("addUpCreditNum", "");
            }
            if (ObjectUtil.equals(object.get("beginningNum"), 0)) {
                object.put("beginningNum", "");
            }
            if (ObjectUtil.isNotEmpty(object.get("carteName"))) {
                object.put("subjectName", object.get("subjectName") + "_" + object.get("carteName"));
            }
            if (ObjectUtil.isNotEmpty(object.get("carteNumber"))) {
                object.put("number", object.get("number") + "_" + object.get("carteNumber"));
            }
        }
        exportMethod(dataList, "财务初始余额", 10, (writer, cellStyle, extraData) -> {
            writer.merge(2, 3, 0, 0, "科目编码", cellStyle);
            writer.merge(2, 3, 1, 1, "科目名称", cellStyle);
            writer.merge(2, 3, 2, 2, "方向", cellStyle);
            writer.merge(2, 2, 3, 4, "期初余额", cellStyle);
            writer.merge(2, 2, 5, 6, "本期累计借方", cellStyle);
            writer.merge(2, 2, 7, 8, "本期累计贷方", cellStyle);
            writer.merge(2, 2, 9, 10, "年初余额", cellStyle);
            writer.writeCellValue(3, 3, "数量").setStyle(cellStyle, 3, 3);
            writer.writeCellValue(4, 3, "金额").setStyle(cellStyle, 4, 3);
            writer.writeCellValue(5, 3, "数量").setStyle(cellStyle, 5, 3);
            writer.writeCellValue(6, 3, "金额").setStyle(cellStyle, 6, 3);
            writer.writeCellValue(7, 3, "数量").setStyle(cellStyle, 7, 3);
            writer.writeCellValue(8, 3, "金额").setStyle(cellStyle, 8, 3);
            writer.writeCellValue(9, 3, "数量").setStyle(cellStyle, 9, 3);
            writer.writeCellValue(10, 3, "金额").setStyle(cellStyle, 10, 3);
            writer.setCurrentRow(4);
            writer.addHeaderAlias("number", "科目编码");
            writer.addHeaderAlias("subjectName", "科目名称");
            writer.addHeaderAlias("balanceDirection", "方向");
            writer.addHeaderAlias("initialNum", "数量");
            writer.addHeaderAlias("initialBalance", "金额");
            writer.addHeaderAlias("addUpDebtorNum", "数量1");
            writer.addHeaderAlias("addUpDebtorBalance", "金额1");
            writer.addHeaderAlias("addUpCreditNum", "数量2");
            writer.addHeaderAlias("addUpCreditBalance", "金额2");
            writer.addHeaderAlias("beginningNum", "数量3");
            writer.addHeaderAlias("beginningBalance", "金额3");
        }, null);
    }

    /**
     * 下载财务初始余额模板
     *
     * @param bo accountBO
     */
    @Override
    public void downloadFinanceInitialExcel(FinanceInitialPageBO bo) {
        List<JSONObject> dataList = new ArrayList<>();
        //查询数量总账
        bo.setSubjectType(1);
        List<JSONObject> mapList = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(2);
        List<JSONObject> mapList1 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(3);
        List<JSONObject> mapList2 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(4);
        List<JSONObject> mapList3 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(5);
        List<JSONObject> mapList4 = initialService.queryListBySubjectType(bo).getList();
        dataList.addAll(mapList);
        dataList.addAll(mapList1);
        dataList.addAll(mapList2);
        dataList.addAll(mapList3);
        dataList.addAll(mapList4);
        AccountSet.getAccountSet().setCompanyName(null);
        JSONObject jsonObject = queryCertificateTime();
        if (!Objects.equals(jsonObject.getString("maxTime"), jsonObject.getString("minTime"))) {
            throw new CrmException(FinanceCodeEnum.FINANCE_IMPORT_EXCEL_DOWN_ERROR);
        }
        //一月仅可以编辑期初余额
        boolean isStart = DateUtil.parse(jsonObject.getString("maxTime"), DatePattern.NORM_MONTH_PATTERN).month() == 0;
        exportMethod(null, "财务初始余额导入模板", null, (writer, cellStyle, extraData) -> {
            //设置禁止编辑列
            writer.getSheet().protectSheet("id@aneRSDG982");
            writer.merge(0, 1, 0, 0, "科目编码", cellStyle);
            writer.merge(0, 1, 1, 1, "科目名称", cellStyle);
            writer.merge(0, 1, 2, 2, "方向", cellStyle);
            writer.merge(0, 1, 3, 3, "辅助核算项目", cellStyle);
            writer.merge(0, 0, 4, 5, "期初余额", cellStyle);
            if (!isStart) {
                writer.merge(0, 0, 6, 7, "本期累计借方", cellStyle);
                writer.merge(0, 0, 8, 9, "本期累计贷方", cellStyle);
                writer.merge(0, 0, 10, 11, "年初余额", cellStyle);
                writer.merge(0, 0, 12, 13, "实际损益发生额", cellStyle);
                for (int i = 4; i < 14; i++) {
                    if (i % 2 == 0) {
                        writer.writeCellValue(i, 1, "数量").setStyle(cellStyle, i, 1);
                    } else {
                        writer.writeCellValue(i, 1, "金额").setStyle(cellStyle, i, 1);
                    }
                }
            } else {
                for (int i = 4; i < 6; i++) {
                    if (i % 2 == 0) {
                        writer.writeCellValue(i, 1, "数量").setStyle(cellStyle, i, 1);
                    } else {
                        writer.writeCellValue(i, 1, "金额").setStyle(cellStyle, i, 1);
                    }
                }
            }
            writer.setColumnWidth(-1, 15);
            writer.setCurrentRow(2);
            List<String> list = ListUtil.list(true, "number", "subjectName", "balanceDirection", "subjectId", "initialNum", "initialBalance");
            if (!isStart) {
                list.addAll(Arrays.asList("addUpDebtorNum", "addUpDebtorBalance", "addUpCreditNum", "addUpCreditBalance", "beginningNum", "beginningBalance", "profitNum", "profitBalance"));
            }
            //使用stream获取一个map,key为本身，value为元素下标
            Map<String, Integer> cacheMap = IntStream.range(0, list.size()).boxed().collect(Collectors.toMap(list::get, Function.identity()));
            CellStyle defaultStyle = writer.getCellStyle();
            CellStyle alterableStyle = StyleUtil.cloneCellStyle(writer.getWorkbook(), defaultStyle);
            alterableStyle.setLocked(false);
            defaultStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            defaultStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            Map<Long, List<String>> subjectMap = new HashMap<>();
            //辅助核算信息
            Map<Long, List<Long>> adjuvantMap = new HashMap<>();
            //循环数据列表，寻找父级科目
            for (int i = 2; i < dataList.size() + 2; i++) {
                JSONObject object = dataList.get(i - 2);
                if (!Objects.equals(0L, object.get("parentId")) && ObjectUtil.isEmpty(object.get("assistId"))) {
                    subjectMap.computeIfAbsent(object.getLong("parentId"), k -> new ArrayList<>()).add(object.getString("number"));
                }
                adjuvantMap.computeIfAbsent(object.getLong("subjectId"), k -> new ArrayList<>()).add(1L);
            }
            for (int i = 2; i < dataList.size() + 2; i++) {
                JSONObject object = dataList.get(i - 2);

                String[] args = new String[]{"initialNum", "addUpDebtorNum", "addUpCreditNum"};
                //数量字段，如果不是所有的都为空，将空设置为0
                boolean allEmpty = ObjectUtil.isAllEmpty(Arrays.stream(args).map(object::get).toArray());
                if (!allEmpty) {
                    for (String arg : args) {
                        if (ObjectUtil.isEmpty(object.get(arg))) {
                            object.put(arg, BigDecimal.ZERO);
                        }
                    }
                }
                Row createRow = writer.getOrCreateRow(i);
                createRow.setHeightInPoints(30);
                //只有最底层的科目才可以编辑
                boolean isLowLevel = !subjectMap.containsKey(object.getLong("subjectId")) && (adjuvantMap.get(object.getLong("subjectId")).size() == 1 || !object.containsKey("subjectAdjuvantList"));
                for (String key : object.keySet()) {
                    if (!cacheMap.containsKey(key)) {
                        continue;
                    }

                    int k = cacheMap.get(key);
                    Cell cell = writer.getOrCreateCell(k, i);
                    Object value = object.get(key);
                    //金额类型字段默认为0
                    if (key.endsWith("Balance") && value == null) {
                        value = BigDecimal.ZERO;
                    }
                    CellStyle style = defaultStyle;
                    switch (key) {
                        case "balanceDirection":
                            value = Objects.equals(1, value) ? "借" : "贷";
                            break;
                        case "number":
                            value = object.get("number");
                            if (ObjectUtil.isNotEmpty(object.get("carteNumber"))) {
                                value = value + "_" + object.get("carteNumber");
                            }
                            break;
                        case "subjectName":
                            value = object.get("subjectName");
                            if (ObjectUtil.isNotEmpty(object.get("carteName"))) {
                                value = object.get("subjectName") + "_" + object.get("carteName");
                            }
                            break;
                        case "subjectId":
                            value = object.containsKey("subjectAdjuvantList") ? "是" : "否";
                            break;
                        case "profitBalance": {
                            if (object.getString("number").startsWith("5") || object.getString("number").startsWith("6")) {
                                if (!isLowLevel) {
                                    List<?> id = subjectMap.get(object.getLong("subjectId"));
                                    if (id == null) {
                                        id = new ArrayList<>(adjuvantMap.get(object.getLong("subjectId")));
                                        id.remove(0);
                                    }
                                    String cellFormula = calculateCellFormula(id, k + 1, i + 1);
                                    cell.setCellFormula(cellFormula);
                                    cell.setCellStyle(defaultStyle);
                                    style = null;
                                } else {
                                    style = alterableStyle;
                                }
                            } else {
                                value = "";
                            }
                            Cell prevCell = writer.getOrCreateCell(k - 1, i);
                            CellUtil.setCellValue(prevCell, "", defaultStyle);
                            break;
                        }
                        case "beginningNum":
                        case "beginningBalance": {
                            Integer balanceDirection = object.getInteger("balanceDirection");
                            String cellFormula;
                            //验证三个单元格是否都存在值
                            if (allEmpty && "beginningNum".equals(key)) {
                                break;
                            }
                            style = null;
                            if (!isLowLevel) {
                                List<?> id = subjectMap.get(object.getLong("subjectId"));
                                if (id == null) {
                                    id = new ArrayList<>(adjuvantMap.get(object.getLong("subjectId")));
                                    id.remove(0);
                                }
                                cellFormula = calculateCellFormula(id, k + 1, i + 1);
                            } else {
                                int num = i + 1;
                                if (Objects.equals(1, balanceDirection)) {
                                    cellFormula = "SUM(" + CrmExcelUtil.getCorrespondingLabel(k - 5) + num + "-" + CrmExcelUtil.getCorrespondingLabel(k - 3) + num + "+" + CrmExcelUtil.getCorrespondingLabel(k - 1) + num + ")";
                                } else {
                                    cellFormula = "SUM(" + CrmExcelUtil.getCorrespondingLabel(k - 5) + num + "+" + CrmExcelUtil.getCorrespondingLabel(k - 3) + num + "-" + CrmExcelUtil.getCorrespondingLabel(k - 1) + num + ")";
                                }
                            }
                            cell.setCellFormula(cellFormula);
                            cell.setCellStyle(defaultStyle);
                            break;
                        }
                        default:
                            if (!isLowLevel) {
                                List<?> id = subjectMap.get(object.getLong("subjectId"));
                                if (id == null) {
                                    id = new ArrayList<>(adjuvantMap.get(object.getLong("subjectId")));
                                    id.remove(0);
                                }
                                String cellFormula = calculateCellFormula(id, k + 1, i + 1);
                                cell.setCellFormula(cellFormula);
                                cell.setCellStyle(defaultStyle);
                                style = null;
                            } else {
                                if (ObjectUtil.isEmpty(value)) {
                                    value = BigDecimal.ZERO;
                                }
                                style = alterableStyle;
                            }
                            break;
                    }
                    if (style != null) {
                        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                        style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                        //非顶级客户
                        CellUtil.setCellValue(cell, value, style);
                    }

                }

            }
        }, null);
    }

    /**
     * 计算子级科目的数据
     *
     * @param childIds 下级列表
     * @param k        columnNum
     * @param i        rowNum
     * @return
     */
    private String calculateCellFormula(List<?> childIds, Integer k, Integer i) {
        String label = CrmExcelUtil.getCorrespondingLabel(k);
        StringBuilder builder = new StringBuilder("SUM(");
        for (int j = 1, f = childIds.size(); j <= f; j++) {
            builder.append(label).append(i + j);
            if (j != f) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 导入财务初始余额模板
     *
     * @param file file
     * @return 导入size
     */
    @Override
    public Integer financeInitialImport(MultipartFile file) {
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new CrmException(SystemCodeEnum.SYSTEM_ERROR);
        }
        JSONObject jsonObject = queryCertificateTime();
        if (!Objects.equals(jsonObject.getString("maxTime"), jsonObject.getString("minTime"))) {
            throw new CrmException(FinanceCodeEnum.FINANCE_IMPORT_EXCEL_DOWN_ERROR);
        }
        //一月仅可以编辑期初余额
        boolean isStart = DateUtil.parse(jsonObject.getString("maxTime"), DatePattern.NORM_MONTH_PATTERN).month() == 0;
        FinanceInitialPageBO bo = new FinanceInitialPageBO();
        bo.setAccountId(AccountSet.getAccountSetId());
        List<JSONObject> dataList = new ArrayList<>();
        //查询数量总账
        bo.setSubjectType(1);
        List<JSONObject> mapList = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(2);
        List<JSONObject> mapList1 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(3);
        List<JSONObject> mapList2 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(4);
        List<JSONObject> mapList3 = initialService.queryListBySubjectType(bo).getList();
        bo.setSubjectType(5);
        List<JSONObject> mapList4 = initialService.queryListBySubjectType(bo).getList();
        dataList.addAll(mapList);
        dataList.addAll(mapList1);
        dataList.addAll(mapList2);
        dataList.addAll(mapList3);
        dataList.addAll(mapList4);
        List<String> list = ListUtil.list(true, "number", "subjectName", "balanceDirection", "subjectId", "initialNum", "initialBalance");
        if (!isStart) {
            list.addAll(Arrays.asList("addUpDebtorNum", "addUpDebtorBalance", "addUpCreditNum", "addUpCreditBalance", "beginningNum", "beginningBalance", "profitNum", "profitBalance"));
        }
        List<FinanceInitial> initials = new ArrayList<>(200);
        ExcelUtil.readBySax(inputStream, 0, (sheetIndex, rowIndex, rowList) -> {
            if (rowIndex > 1) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < rowList.size(); i++) {
                    map.put(list.get(i), rowList.get(i));
                }
                map.remove("subjectId");
                JSONObject object = dataList.get((int) (rowIndex - 2));
                if (object == null) {
                    return;
                }
                map.put("subjectId", object.getLong("subjectId"));
                map.put("initialId", object.getLong("initialId"));
                initials.add(BeanUtil.toBean(map, FinanceInitial.class));
            } else {
                if (rowList.size() != list.size()) {
                    throw new CrmException(FinanceCodeEnum.FINANCE_IMPORT_EXCEL_ERROR);
                }
            }
        });

        if (initials.size() != dataList.size()) {
            throw new CrmException(FinanceCodeEnum.FINANCE_IMPORT_EXCEL_ERROR);
        }

        Map<Long, FinanceInitial> initialMap = initialService.lambdaQuery().eq(FinanceInitial::getAccountId, AccountSet.getAccountSetId()).list().stream().collect(Collectors.toMap(FinanceInitial::getInitialId, Function.identity(), (k1, k2) -> k1));
        List<FinanceInitial> toUpdateData = new ArrayList<>();
        for (FinanceInitial initial : initials) {
            FinanceInitial financeInitial;
            if (ObjectUtil.isNotEmpty(initial.getInitialId())) {
                financeInitial = initialMap.get(initial.getInitialId());
            } else {
                financeInitial = initialMap.values().stream().filter(obj -> obj.getSubjectId().equals(initial.getSubjectId())).findFirst().orElse(null);
            }
            if (financeInitial != null) {
                financeInitial.setInitialBalance(initial.getInitialBalance());
                financeInitial.setInitialNum(initial.getInitialNum());
                financeInitial.setAddUpCreditBalance(initial.getAddUpCreditBalance());
                financeInitial.setAddUpCreditNum(initial.getAddUpCreditNum());
                financeInitial.setAddUpDebtorBalance(initial.getAddUpDebtorBalance());
                financeInitial.setAddUpDebtorNum(initial.getAddUpDebtorNum());
                financeInitial.setBeginningBalance(initial.getBeginningBalance());
                financeInitial.setBeginningNum(initial.getBeginningNum());
                financeInitial.setProfitBalance(initial.getProfitBalance());
                financeInitial.setUpdateTime(LocalDateTime.now());
                financeInitial.setUpdateUserId(UserUtil.getUserId());
                toUpdateData.add(financeInitial);
            }
        }
        if (CollUtil.isNotEmpty(toUpdateData)) {
            initialService.updateBatchById(toUpdateData);
        }
        return initials.size();
    }

    //导出excel双层表头公共方法
    private void exportMethod(List<JSONObject> mapList, String excelName, Integer length, FinanceExcelParseUtil.ExcelMergeFunc func, String date) {
        try {
            FinanceAccountSet accountSet = AccountSet.getAccountSet();
            File file = FileUtil.file("excelData/" + IdUtil.simpleUUID() + ".xlsx");
            BigExcelWriter writer = ExcelUtil.getBigWriter(file);
            CellStyle style = writer.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            Font headFont = writer.createFont();
            headFont.setFontHeightInPoints((short) 22);
            style.setFont(headFont);
            if (length != null) {
                writer.merge(0, 0, 0, length, excelName, style);
            }
            if (ObjectUtil.isNotEmpty(accountSet.getCompanyName())) {
                Font rowFont = writer.createFont();
                rowFont.setFontHeightInPoints((short) 11);
                CellStyle rowCell = writer.createCellStyle();
                rowCell.setFont(rowFont);
                rowCell.setAlignment(HorizontalAlignment.LEFT);
                writer.writeCellValue(0, 1, accountSet.getCompanyName()).setStyle(rowCell, 0, 1);
                if (ObjectUtil.isNotEmpty(date)) {
                    CellStyle rightCell = writer.createCellStyle();
                    rightCell.setFont(rowFont);
                    rightCell.setAlignment(HorizontalAlignment.RIGHT);
                    if (length != null) {
                        writer.merge(1, 1, 1, length, date, rightCell);
                    }
                }
            }

            CellStyle orCreateCellStyle = writer.createCellStyle();
            orCreateCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            Font font = writer.createFont();
            font.setBold(true);
            orCreateCellStyle.setFont(font);
            orCreateCellStyle.setBorderTop(BorderStyle.THIN);
            orCreateCellStyle.setBorderBottom(BorderStyle.THIN);
            orCreateCellStyle.setBorderLeft(BorderStyle.THIN);
            orCreateCellStyle.setBorderRight(BorderStyle.THIN);
            orCreateCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            orCreateCellStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);
            // 取消数据的黑色边框以及数据左对齐
            CellStyle cellStyle = writer.getCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            Font defaultFont = writer.createFont();
            defaultFont.setFontHeightInPoints((short) 11);
            cellStyle.setFont(defaultFont);
            // 取消数字格式的数据的黑色边框以及数据左对齐
            CellStyle cellStyleForNumber = writer.getStyleSet().getCellStyleForNumber();
            cellStyleForNumber.setLocked(false);
            cellStyleForNumber.setBorderTop(BorderStyle.THIN);
            cellStyleForNumber.setBorderBottom(BorderStyle.THIN);
            cellStyleForNumber.setBorderLeft(BorderStyle.THIN);
            cellStyleForNumber.setBorderRight(BorderStyle.THIN);
            cellStyleForNumber.setAlignment(HorizontalAlignment.RIGHT);
            cellStyleForNumber.setFont(defaultFont);
            //设置数字格式单元格为货币类型
            DataFormat format = writer.getWorkbook().createDataFormat();
            cellStyleForNumber.setDataFormat(format.getFormat(BuiltinFormats.getBuiltinFormat(4)));
            //设置行高以及列宽
            writer.setRowHeight(-1, 30);
            writer.setColumnWidth(-1, 20);
            // 设置数据
            if (func != null) {
                func.call(writer, orCreateCellStyle, null);
            }
            //只保留别名中的字段
            writer.setOnlyAlias(true);
            if (mapList != null) {
                writer.write(mapList, false);
            }
            //自定义标题别名
            //response为HttpServletResponse对象
            HttpServletResponse response = BaseUtil.getResponse();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(excelName + "信息", "utf-8") + ".xlsx");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
            writer.close();
            out.close();
            MultipartFile multipartFile = MultipartFileUtil.getMultipartFile(file);
            UploadEntity uploadEntity = fileService.uploadTempFile(multipartFile, IdUtil.simpleUUID());
            BaseUtil.getRedis().setex(AdminCacheKey.UPLOAD_EXCEL_MESSAGE_PREFIX + "file:" + uploadEntity.getFileId(), 604800, uploadEntity.getPath());
            response.setHeader("exportSize", mapList.size() + "");
            response.setHeader("fileData", uploadEntity.getFileId());

        } catch (Exception e) {
            log.error("导出错误：", e);
        }
    }


    /**
     * 格式化会计区间
     */
    @Override
    public String formatFinanceStartTime(FinanceDetailAccountBO accountBO) {
        //判断区间是否相同
        StringBuilder sb = new StringBuilder();
        boolean isTrue = StrUtil.contains(accountBO.getStartTime(), "-");
        DateTime startTime;
        DateTime endTime;
        if (isTrue) {
            startTime = DateUtil.parse(accountBO.getStartTime(), DatePattern.NORM_DATE_PATTERN);
            endTime = DateUtil.parse(accountBO.getEndTime(), DatePattern.NORM_DATE_PATTERN);
        } else {
            startTime = DateUtil.parse(accountBO.getStartTime(), DatePattern.SIMPLE_MONTH_PATTERN);
            endTime = DateUtil.parse(accountBO.getEndTime(), DatePattern.SIMPLE_MONTH_PATTERN);
        }

        String st = DateUtil.format(startTime, DatePattern.SIMPLE_MONTH_PATTERN);
        String en = DateUtil.format(endTime, DatePattern.SIMPLE_MONTH_PATTERN);
        //获取会计区间年
        String year = DateUtil.format(startTime, "yyyy");
        //获取会计区间月
        String month = DateUtil.format(startTime, "MM");
        if (ObjectUtil.equal(st, en)) {
            sb.append(year).append("年第").append(month).append("期");
        } else {
            //获取会计区间年
            String endYear = DateUtil.format(endTime, "yyyy");
            //获取会计区间月
            String endMonth = DateUtil.format(endTime, "MM");
            sb.append(year).append("年第").append(month).append("期-").append(endYear).append("年第").append(endMonth).append("期");
        }
        return sb.toString();
    }

    /**
     * 获取科目名称及编码
     */
    private void subjectMethod(Long subjectId, List<JSONObject> mapList) {
        //查询科目信息根据科目id
        FinanceSubject subject = financeSubjectService.getById(subjectId);
        if (CollUtil.isNotEmpty(mapList)) {
            mapList.stream().map(ma -> ma.put("subject", subject.getNumber() + "-" + subject.getSubjectName())).collect(Collectors.toList());
        }
    }

    private void itemsDetailAccountCalculate(List<JSONObject> detailAccounts, List<JSONObject> initials, FinanceDetailAccountBO accountBO) {
        //初始余额
        BigDecimal initialBalance = new BigDecimal("0");
        //方向 默认借方
        Integer balanceDirection = 1;
        if (initials.size() > 0) {
            initialBalance = initials.get(0).getBigDecimal("initialBalance");
            balanceDirection = initials.get(0).getInteger("balanceDirection");
            if (initialBalance == null) {
                initialBalance = new BigDecimal("0");
                balanceDirection = 1;
            }
        }
        //处理数据
        for (JSONObject object : detailAccounts) {
            if (Objects.equals(1, object.getInteger("type"))) {
                if (Objects.equals(balanceDirection, 1)) {
                    initialBalance = initialBalance.add(object.getBigDecimal("debtorBalance")).subtract(object.getBigDecimal("creditBalance"));
                    object.put("balanceDirection", "借");
                } else {
                    initialBalance = initialBalance.add(object.getBigDecimal("creditBalance")).subtract(object.getBigDecimal("debtorBalance"));
                    object.put("balanceDirection", "贷");
                }
                object.put("balance", initialBalance);
                object.put("debtorBalance", null);
                object.put("creditBalance", null);
            }
        }
        accountBO.setBalanceDirection(balanceDirection);
        accountBO.setIsBalanceDirection(balanceDirection);
        JSONObject paramnter = parameterService.queryParameter();
        if (paramnter != null) {
            accountBO.setAccountBookDirection(paramnter.getInteger("accountBookDirection"));
        } else {
            accountBO.setAccountBookDirection(1);
        }
        calculate(detailAccounts, accountBO);
    }

    @Value("${spring.cloud.client.ip-address}")
    private String currentIp;

    /**
     * 财务凭证预览
     *
     * @param propertiesBO 打印参数
     * @return path
     */
    @Override
    public String preview(CrmPrintPropertiesBO propertiesBO) {
        String fileTypeP = "pdf";
        String fileTypeW = "word";
        if (!Arrays.asList(fileTypeP, fileTypeW).contains(propertiesBO.getType())) {
            throw new CrmException(FinanceCodeEnum.FINANCE_PRINT_PRE_VIEW_ERROR);
        }
        String slash = BaseUtil.isWindows() ? "\\" : "/";
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String folderPath = FileUtil.getTmpDirPath() + "print" + slash + date;
        String UUID = IdUtil.simpleUUID();
        String fileName = UUID + ".pdf";
        FileUtil.mkdir(folderPath + slash);
        String path = folderPath + slash + fileName;
        String htmlName = UUID + ".html";
        String htmlPath = folderPath + slash + htmlName;
        JSONObject object = new JSONObject();
        String html = "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                ".wk-watermark-dom {\n" +
                "-webkit-transform: rotate(-15deg) !important;\n" +
                (fileTypeW.equals(propertiesBO.getType()) ? "display: none !important;\n" : "") +
                "}\n" +
                ".wk-watermark-dom {\n" +
                "  -webkit-transform: rotate(-15deg) !important;\n" +
                "  word-wrap: break-word;\n" +
                "}" +
                "body {\n" +
                "    box-sizing: border-box;\n" +
                "}" +
                "</style>\n" +
                "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" +
                "</head>\n" +
                "<body>\n" +
                propertiesBO.getContent() +
                "</body>\n" +
                "</html>";
        html = html.replaceAll("<!-- pagebreak -->", "<div style=\"page-break-before: always;\"></div>");
        FileUtil.writeUtf8String(html, htmlPath);
        if (fileTypeW.equals(propertiesBO.getType())) {
            String filePath = folderPath + slash + UUID + ".doc";
            File file = FileUtil.file(filePath);
            try (
                    ByteArrayInputStream byteArrayInputStream = IoUtil.toUtf8Stream(html);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    POIFSFileSystem poifsFileSystem = new POIFSFileSystem();
            ) {
                DirectoryEntry directoryEntry = poifsFileSystem.getRoot();
                directoryEntry.createDocument("WordDocument", byteArrayInputStream);
                object.put("word", file.getAbsolutePath());
                poifsFileSystem.writeFilesystem(outputStream);
            } catch (Exception e) {
                log.error("打印预览转换word失败", e);
                throw new CrmException(SystemCodeEnum.SYSTEM_ERROR);
            }
        }
        writePDFContent(htmlPath, path, propertiesBO);
        String uuid = IdUtil.simpleUUID();
        object.put("pdf", path);
        object.put("currentIp", currentIp);
        BaseUtil.getRedis().setex(CrmCacheKey.CRM_PRINT_TEMPLATE_CACHE_KEY + uuid, 3600 * 24, object.toJSONString());
        return uuid;
    }

    @Override
    public String previewFinance(String content, String type, String orientation, String pageSize) {
        String fileTypeP = "pdf";
        String fileTypeW = "word";
        if (!Arrays.asList(fileTypeP, fileTypeW).contains(type)) {
            throw new CrmException(FinanceCodeEnum.FINANCE_PRINT_PRE_VIEW_ERROR);
        }
        String slash = BaseUtil.isWindows() ? "\\" : "/";
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        String folderPath = FileUtil.getTmpDirPath() + slash + "print" + slash + date;
        String UUID = IdUtil.simpleUUID();
        String fileName = UUID + ".pdf";
        FileUtil.mkdir(folderPath + slash);
        String path = folderPath + slash + fileName;
        String htmlName = UUID + ".html";
        String htmlPath = folderPath + slash + htmlName;
        JSONObject object = new JSONObject();
        String html = "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "</style>\n" +
                "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" +
                "</head>\n" +
                "<body>\n" +
                content +
                "</body>\n" +
                "</html>";
        FileUtil.writeUtf8String(html, htmlPath);
        writePDFContentUP(htmlPath, path, orientation, pageSize);
        String uuid = IdUtil.simpleUUID();
        object.put("pdf", path);
        BaseUtil.getRedis().setex(CrmCacheKey.CRM_PRINT_TEMPLATE_CACHE_KEY + uuid, 3600 * 24, object.toJSONString());
        return uuid;
    }

    /**
     * 移动财务凭证信息，用于当科目已录入凭证后又添加辅助核算，需要将原凭证移动到辅助核算下
     *
     * @param subject        当前科目信息
     * @param associationMap 辅助核算信息
     */
    @Override
    public void moveFinanceCertificate(FinanceSubject subject, Map<Long, JSONObject> associationMap) {
        if (associationMap == null || associationMap.isEmpty()) {
            return;
        }
        List<FinanceCertificateDetail> list = financeCertificateDetailService.lambdaQuery().eq(FinanceCertificateDetail::getSubjectId, subject.getSubjectId()).list();
        for (FinanceCertificateDetail certificateDetail : list) {
            //根据凭证详情id删除凭证详情关联标签类型表数据
            associationMap.forEach((key, value) -> {
                FinanceCertificateAssociation association = BeanUtil.copyProperties(value, FinanceCertificateAssociation.class);
                FinanceAssistAdjuvant adjuvant = new FinanceAssistAdjuvant();
                if (association.getLabel() == null) {
                    FinanceAdjuvant financeAdjuvant = adjuvantService.getById(association.getAdjuvantId());
                    association.setLabel(financeAdjuvant.getLabel());
                    association.setLabelName(financeAdjuvant.getAdjuvantName());
                }
                association.setRelationId(value.getLong("carteId"));
                adjuvant.setLabel(association.getLabel());
                adjuvant.setLabelName(association.getLabelName());
                adjuvant.setRelationId(value.getLong("carteId"));
                adjuvant.setAdjuvantId(association.getAdjuvantId());
                FinanceAssist assist = financeAssistService.saveAndUpdate(certificateDetail.getSubjectId(), Collections.singletonList(adjuvant));
                certificateDetail.setAssistId(assist.getAssistId());
                saveAssociation(certificateDetail.getDetailId(), Collections.singletonList(association));
            });
            financeCertificateDetailService.updateById(certificateDetail);
        }
    }

    /**
     * 检查科目是否已经录入凭证
     *
     * @param subjectId 科目id
     * @return true 已录入 false 未录入
     */
    @Override
    public boolean checkSubjectCertificate(Long subjectId) {
        boolean exists = financeCertificateDetailService.lambdaQuery().eq(FinanceCertificateDetail::getSubjectId, subjectId).exists();
        return exists;
    }

    private void writePDFContent(String content, String distPath, CrmPrintPropertiesBO propertiesBO) {
        String toPdfTool;
        if (BaseUtil.isWindows()) {
            toPdfTool = FinanceConst.WIN_TO_PDF_TOOL;
        } else {
            toPdfTool = FinanceConst.LINUX_TO_PDF_TOOL;
        }
        StringBuilder cmd = new StringBuilder();
        log.info("设置插件地址 ： " + toPdfTool);
        cmd.append(toPdfTool);
        cmd.append(" ");
        cmd.append(" --log-level none");
        cmd.append(" --page-height ").append(propertiesBO.getPageHeight());
        cmd.append(" --page-width ").append(propertiesBO.getPageWidth());


        if (StrUtil.isNotEmpty(propertiesBO.getMarginLeft()) && !"0mm".equalsIgnoreCase(propertiesBO.getMarginLeft())) {
            cmd.append(" --margin-left ").append(propertiesBO.getMarginLeft());
        }
        if (StrUtil.isNotEmpty(propertiesBO.getMarginTop()) && !"0mm".equalsIgnoreCase(propertiesBO.getMarginTop())) {
            cmd.append(" --margin-top ").append(propertiesBO.getMarginTop());
        }
        if (StrUtil.isNotEmpty(propertiesBO.getFooterHtml())) {
            cmd.append(" --footer-center ").append(propertiesBO.getFooterHtml());
            cmd.append(" --footer-spacing 10");
        }
        if (StrUtil.isNotEmpty(propertiesBO.getHeaderHtml())) {
            cmd.append(" --header-center ").append(propertiesBO.getHeaderHtml());
            cmd.append(" --header-spacing 10");
        }
        cmd.append(" --orientation ").append(propertiesBO.getOrientation());
        cmd.append(" --disable-smart-shrinking");
        cmd.append(" ");
        cmd.append(content);
        cmd.append(" ");
        cmd.append(distPath);
        try {
            Process process = Runtime.getRuntime().exec(cmd.toString());
            process.waitFor();
            log.info("转换成功 ");
            process.destroy();
        } catch (Exception e) {
            log.error("转换失败，失败原因" + e);
            log.error("cmd:{}", cmd.toString());
        }
    }

    private void writePDFContentUP(String content, String distPath, String orientation, String pageSize) {
        log.info("准备转换pdf");
        String toPdfTool;
        if (BaseUtil.isWindows()) {
            toPdfTool = FinanceConst.WIN_TO_PDF_TOOL;
        } else {
            toPdfTool = FinanceConst.LINUX_TO_PDF_TOOL;
        }
        StringBuilder cmd = new StringBuilder();
        log.info("设置插件地址 ： " + toPdfTool);
        cmd.append(toPdfTool);
        cmd.append(" ");
        cmd.append(" --log-level none");
        if (StrUtil.isNotEmpty(orientation)) {
            cmd.append(" --orientation ").append(orientation);// (设置方向为横向或纵向 )
        }
        if (StrUtil.isNotEmpty(pageSize)) {
            cmd.append(" --margin-top 20mm ");//设置页面上边距 (default 10mm)
            cmd.append(" --page-size ").append(pageSize);// (设置纸张大小: A4, Letter, etc. )
            //cmd.append(" --header-spacing 5 ");// (设置页眉和内容的距离,默认0)
            cmd.append(" --margin-left  35mm ");// (设置页眉和内容的距离,默认0)
            cmd.append(" --margin-right 20mm ");// (设置页眉和内容的距离,默认0)
        }
        cmd.append(" --footer-spacing 5 ");// (设置页脚和内容的距离)
        cmd.append(content);
        cmd.append(" ");
        cmd.append(distPath);
        try {
            log.info("进行转换,html地址 ： " + content);
            log.info("进行转换,转换后pdf地址 ： " + distPath);
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            proc.waitFor();
            log.info("转换成功 ");
            proc.destroy();
        } catch (Exception e) {
            log.info("转换失败，失败原因" + e);
        }
    }
}
