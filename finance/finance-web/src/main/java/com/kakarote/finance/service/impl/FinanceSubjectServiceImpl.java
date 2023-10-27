package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.upload.entity.UploadEntity;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.common.MultipartFileUtil;
import com.kakarote.core.common.cache.AdminCacheKey;
import com.kakarote.core.common.enums.FieldEnum;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.core.utils.ExcelParseUtil;
import com.kakarote.core.utils.RecursionUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.ExcelImportUtils;
import com.kakarote.finance.common.FinanceExcelParseUtil;
import com.kakarote.finance.constant.FinanceBalanceDirectionEnum;
import com.kakarote.finance.constant.FinanceCategoryEnum;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.entity.PO.*;
import com.kakarote.finance.entity.VO.FinanceSubjectIdsVO;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import com.kakarote.finance.mapper.FinanceSubjectMapper;
import com.kakarote.finance.service.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 科目 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Service
public class FinanceSubjectServiceImpl extends BaseServiceImpl<FinanceSubjectMapper, FinanceSubject> implements IFinanceSubjectService {

    @Autowired
    private IFinanceSubjectAdjuvantService subjectAdjuvantService;

    @Autowired
    private IFinanceCertificateDetailService certificateDetailService;

    @Autowired
    private IFinanceInitialAssistService financeInitialAssistService;

    @Autowired
    private IFinanceAssistService financeAssistService;

    @Autowired
    private IFinanceAdjuvantService adjuvantService;

    @Autowired
    private IFinanceInitialService initialService;

    @Autowired
    private IFinanceCertificateService certificateService;

    @Autowired
    private IFinanceParameterService parameterService;

    @Autowired
    private IFinanceSubjectService subjectService;

    @Autowired
    private IFinanceAssistAdjuvantService assistAdjuvantService;

    @Autowired
    private IFinanceSubjectCurrencyService subjectCurrencyService;

    @Autowired
    private AdminFileService fileService;

    private static final int TWO = 2;

    private static final int THREE = 3;

    private static final int FOUR = 4;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationLog saveAndUpdate(FinanceSubjectBO subjectBO) {
        FinanceSubject subject = BeanUtil.copyProperties(subjectBO, FinanceSubject.class);
        if (subject.getParentId() != null && subject.getParentId() != 0) {
            FinanceSubject pSubject = getById(subject.getParentId());
            if (!ObjectUtil.equal(subject.getCategory(), pSubject.getCategory()) || !ObjectUtil.equal(subject.getType(), pSubject.getType())) {
                throw new CrmException(FinanceCodeEnum.FINANCE_SUBJECT_NUMBER_ERROR);
            }
        }

        OperationLog operationLog = new OperationLog();
        if (ObjectUtil.isNull(subject.getSubjectId())) {
            List<FinanceSubject> subjects = lambdaQuery().eq(FinanceSubject::getNumber, subject.getNumber())
                    .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).list();
            if (!subjects.isEmpty()) {
                throw new CrmException(FinanceCodeEnum.FINANCE_SUBJECT_NUMBER_ERROR);
            }
            subject.setCreateUserId(UserUtil.getUserId());
            subject.setCreateTime(LocalDateTime.now());
            subject.setAccountId(AccountSet.getAccountSetId());
            save(subject);

            operationLog.setOperationObject(subject.getNumber());
            operationLog.setOperationInfo("新建科目：" + subject.getNumber());

            if (ObjectUtil.isNotEmpty(subject.getParentId())) {
                //根据上级科目ID查询所有凭证
                List<FinanceCertificateDetail> detailList = certificateDetailService.lambdaQuery().eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId()).eq(FinanceCertificateDetail::getSubjectId, subject.getParentId()).list();
                if (CollUtil.isNotEmpty(detailList)) {

                    detailList.forEach(data -> {
                        data.setSubjectId(subject.getSubjectId()).setSubjectNumber(subject.getNumber());
                        FinanceSubjectVO subjectVO = JSON.parseObject(data.getSubjectContent(), FinanceSubjectVO.class);
                        String name = subjectVO.getSubjectName();

                        //根据科目id获取辅助核算信息
                        if (ObjectUtil.isNotEmpty(subjectVO.getSubjectId())) {
                            subjectVO.setSubjectName(name + "_" + subject.getSubjectName());
                            JSONObject object = new JSONObject(BeanUtil.beanToMap(subjectVO));
                            object.put("parentId", subject.getParentId().toString());
                            data.setSubjectContent(object.toJSONString());
                        }

                    });
                    certificateDetailService.updateBatchById(detailList);
                }
            }

            // 子科目，复制父科目的辅助核算数据
            if (ObjectUtil.isNotNull(subject.getParentId()) && ObjectUtil.notEqual(0L, subject.getParentId())) {
                // 父科目关联的辅助项
                boolean count = subjectAdjuvantService.lambdaQuery()
                        .eq(FinanceSubjectAdjuvant::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceSubjectAdjuvant::getSubjectId, subject.getParentId()).exists();
                boolean childSubjectCount = subjectService.lambdaQuery()
                        .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                        .ne(FinanceSubject::getSubjectId, subject.getSubjectId())
                        .eq(FinanceSubject::getParentId, subject.getParentId()).exists();
                // 删除父科目关联的辅助核算
                subjectAdjuvantService.lambdaUpdate()
                        .eq(FinanceSubjectAdjuvant::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceSubjectAdjuvant::getSubjectId, subject.getParentId())
                        .remove();
                FinanceSubject parentSubject = getById(subject.getParentId());
                // 获取父科目的初始余额
                FinanceInitial parentInitial = initialService.lambdaQuery()
                        .eq(FinanceInitial::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceInitial::getSubjectId, subject.getParentId())
                        .one();
                // 新建子科目的初始余额
                FinanceInitial initial = BeanUtil.copyProperties(parentInitial, FinanceInitial.class);
                initial.setInitialId(null);
                initial.setSubjectId(subject.getSubjectId());
                initial.setCreateTime(LocalDateTime.now());
                initial.setCreateUserId(UserUtil.getUserId());
                initial.setAccountId(AccountSet.getAccountSetId());
                if (!count && childSubjectCount) {
                    initial.setInitialBalance(BigDecimal.ZERO);
                    initial.setBeginningBalance(BigDecimal.ZERO);
                    initial.setAddUpCreditBalance(BigDecimal.ZERO);
                    initial.setAddUpDebtorBalance(BigDecimal.ZERO);
                    initial.setProfitBalance(BigDecimal.ZERO);
                }
                if (ObjectUtil.notEqual(parentSubject.getBalanceDirection(), subject.getBalanceDirection())) {
                    initial.setInitialBalance(Optional.ofNullable(initial.getInitialBalance()).orElse(BigDecimal.ZERO).negate());
                    initial.setBeginningBalance(Optional.ofNullable(initial.getBeginningBalance()).orElse(BigDecimal.ZERO).negate());
                    initial.setAddUpCreditBalance(Optional.ofNullable(initial.getAddUpCreditBalance()).orElse(BigDecimal.ZERO).negate());
                    initial.setAddUpDebtorBalance(Optional.ofNullable(initial.getAddUpDebtorBalance()).orElse(BigDecimal.ZERO).negate());
                    initial.setProfitBalance(Optional.ofNullable(initial.getProfitBalance()).orElse(BigDecimal.ZERO).negate());
                }
                initialService.save(initial);
                // 获取父科目关联的所有辅助数据
                List<FinanceAssist> financeAssists = financeAssistService.lambdaQuery()
                        .eq(FinanceAssist::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceAssist::getSubjectId, subject.getParentId())
                        .list();
                // 获取父科目关联的所有初始余额辅助核算
                List<FinanceInitialAssist> initialAssists = financeInitialAssistService.lambdaQuery()
                        .eq(FinanceInitialAssist::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceInitialAssist::getSubjectId, subject.getParentId())
                        .list();
                // 替换科目id 为子科目的id，然后重新保存
                if (CollUtil.isNotEmpty(financeAssists)) {
                    for (FinanceAssist financeAssist : financeAssists) {
                        financeAssist.setSubjectId(subject.getSubjectId());
                    }
                    financeAssistService.saveOrUpdateBatch(financeAssists);
                }
                if (CollUtil.isNotEmpty(initialAssists)) {
                    for (FinanceInitialAssist initialAssist : initialAssists) {
                        initialAssist.setSubjectId(subject.getSubjectId());
                    }
                    financeInitialAssistService.saveOrUpdateBatch(initialAssists);
                }
            }
        } else {

            operationLog.setOperationObject(subject.getNumber());
            operationLog.setOperationInfo("编辑科目：" + subject.getNumber());

            updateById(subject);
        }
        // 获取科目关联的辅助核算
        List<FinanceSubjectAdjuvant> subjectAdjuvants = subjectAdjuvantService.lambdaQuery()
                .eq(FinanceSubjectAdjuvant::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceSubjectAdjuvant::getSubjectId, subject.getSubjectId()).list();
        // 当前的辅助核算
        List<Long> currentAdjuvantIds = subjectAdjuvants.stream().map(FinanceSubjectAdjuvant::getAdjuvantId).collect(Collectors.toList());

        List<FinanceAdjuvant> adjuvantList = adjuvantService.lambdaQuery()
                .eq(FinanceAdjuvant::getAccountId, AccountSet.getAccountSetId()).list();
        Map<Long, FinanceAdjuvant> adjuvantMap = adjuvantList.stream()
                .collect(Collectors.toMap(FinanceAdjuvant::getAdjuvantId, Function.identity()));
        // 待删除的辅助核算
        List<Long> toRemoveAdjuvantIds = currentAdjuvantIds;
        // 获取科目的辅助核算信息
        List<Long> assistIds = null;
        if (CollUtil.isNotEmpty(subjectBO.getAdjuvantIds())) {
            // 待删除的辅助核算
            toRemoveAdjuvantIds = currentAdjuvantIds.stream()
                    .filter(id -> !CollUtil.contains(subjectBO.getAdjuvantIds(), id))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(toRemoveAdjuvantIds)) {
                List<JSONObject> adjuvantInfo = adjuvantService.queryAdjuvantInfoBySubjectId(subject.getSubjectId());
                List<Long> finalToRemoveAdjuvantIds = toRemoveAdjuvantIds;
                List<JSONObject> toRemoveInfo = adjuvantInfo.stream().filter(o -> ObjectUtil.isNotNull(o.getLong("adjuvantId")) && finalToRemoveAdjuvantIds.contains(o.getLong("adjuvantId")))
                        .collect(Collectors.toList());
                assistIds = toRemoveInfo.stream()
                        .filter(o -> ObjectUtil.isNotNull(o.getLong("assistId")))
                        .map(o -> o.getLong("assistId")).collect(Collectors.toList());
                boolean integer = assistAdjuvantService.lambdaQuery()
                        .in(FinanceAssistAdjuvant::getAssistId, assistIds)
                        .eq(FinanceAssistAdjuvant::getAccountId, AccountSet.getAccountSetId())
                        .exists();
                if (integer) {
                    throw new CrmException();
                }
            }
            // 待添加的辅助核算
            List<Long> toAddAdjuvantIds = subjectBO.getAdjuvantIds().stream()
                    .filter(id -> !CollUtil.contains(currentAdjuvantIds, id))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(toAddAdjuvantIds)) {
                for (Long adjuvantId : toAddAdjuvantIds) {
                    FinanceSubjectAdjuvant subjectAdjuvant = new FinanceSubjectAdjuvant();
                    FinanceAdjuvant adjuvant = adjuvantMap.get(adjuvantId);
                    if (ObjectUtil.isEmpty(adjuvant)) {
                        continue;
                    }
                    subjectAdjuvant.setAdjuvantId(adjuvant.getAdjuvantId());
                    subjectAdjuvant.setSubjectId(subject.getSubjectId());
                    subjectAdjuvant.setLabel(adjuvant.getLabel());
                    subjectAdjuvant.setAccountId(AccountSet.getAccountSetId());
                    subjectAdjuvant.setLabelName(adjuvant.getAdjuvantName());
                    subjectAdjuvantService.save(subjectAdjuvant);
                }
                //判断当前添加辅助核算的科目下是否存在凭证信息，如果存在则需要同步更新凭证信息
                certificateService.moveFinanceCertificate(subject, subjectBO.getAssociationMap());
            }
        }
        if (CollUtil.isNotEmpty(toRemoveAdjuvantIds)) {
            LambdaQueryWrapper<FinanceSubjectAdjuvant> adjuvantLambdaQueryWrapper = new LambdaQueryWrapper<>();
            adjuvantLambdaQueryWrapper
                    .eq(FinanceSubjectAdjuvant::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceSubjectAdjuvant::getSubjectId, subject.getSubjectId())
                    .in(FinanceSubjectAdjuvant::getAdjuvantId, toRemoveAdjuvantIds);
            subjectAdjuvantService.remove(adjuvantLambdaQueryWrapper);
            // 获取科目的辅助核算信息
            if (CollUtil.isNotEmpty(assistIds)) {
                financeAssistService.lambdaUpdate()
                        .eq(FinanceAssist::getSubjectId, subject.getSubjectId())
                        .eq(FinanceAssist::getAccountId, AccountSet.getAccountSetId())
                        .in(FinanceAssist::getAssistId, assistIds)
                        .remove();
                assistAdjuvantService.lambdaUpdate()
                        .in(FinanceAssistAdjuvant::getAssistId, assistIds)
                        .eq(FinanceAssistAdjuvant::getAccountId, AccountSet.getAccountSetId())
                        .remove();
                List<FinanceInitialAssist> initialAssists = financeInitialAssistService.lambdaQuery()
                        .eq(FinanceInitialAssist::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceInitialAssist::getSubjectId, subject.getSubjectId())
                        .in(FinanceInitialAssist::getFinanceAssistId, assistIds).list();
                List<Long> initialIds = initialAssists.stream().map(FinanceInitialAssist::getInitialId)
                        .collect(Collectors.toList());
                financeInitialAssistService.lambdaUpdate()
                        .eq(FinanceInitialAssist::getAccountId, AccountSet.getAccountSetId())
                        .eq(FinanceInitialAssist::getSubjectId, subject.getSubjectId())
                        .in(FinanceInitialAssist::getFinanceAssistId, assistIds).remove();
                if (CollUtil.isNotEmpty(initialIds)) {
                    initialService.lambdaUpdate()
                            .eq(FinanceInitial::getInitialId, initialIds)
                            .eq(FinanceInitial::getSubjectId, subject.getSubjectId())
                            .eq(FinanceInitial::getIsAssist, true)
                            .eq(FinanceInitial::getAccountId, AccountSet.getAccountSetId()).remove();
                }
            }
        }
        return operationLog;
    }

    @Override
    public List<FinanceSubjectVO> queryListByType(Integer type, Integer isTree, Integer returnType, String certificateTime) {
        int five = 5;
        if (isTree != null && isTree == 1) {
            List<FinanceSubjectVO> subjectVOS = getBaseMapper().queryListByType(new ArrayList<>(), type, AccountSet.getAccountSetId(), null);
            for (FinanceSubjectVO subjectVO : subjectVOS) {
                HashMap<String, String> keyMap = new HashMap<>();
                keyMap.put("subjectName_resourceKey", "finance.subject." + subjectVO.getSubjectName());
                subjectVO.setLanguageKeyMap(keyMap);
            }
            //根据科目id获取辅助核算信息
            setAdjuvantList(subjectVOS);
            return RecursionUtil.getChildListTree(subjectVOS, "parentId", 0L, "subjectId", "children", FinanceSubjectVO.class);
        } else if (isTree != null && isTree == TWO) {
            List<FinanceSubject> subjectList = lambdaQuery().eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).eq(type != null, FinanceSubject::getType, type).list();
            //筛选状态为启用的科目
            List<FinanceSubject> list = subjectList.stream().filter(subject -> Objects.equals(1, subject.getStatus())).collect(Collectors.toList());
            List<Long> subjectIds = new ArrayList<>();
            for (FinanceSubject subject : list) {
                if (querySubjectIdsByParentId(list, subject.getSubjectId())) {
                    subjectIds.add(subject.getSubjectId());
                }
            }
            if (subjectIds.size() == 0) {
                subjectIds.add(0L);
            }
            List<FinanceSubjectVO> subjectVOS = getBaseMapper().queryListByType(subjectIds, type, AccountSet.getAccountSetId(), null);
            if (returnType != null && returnType == 1) {
                for (FinanceSubjectVO subjectVO : subjectVOS) {
                    disposeName(subjectList, subjectVO, new ArrayList<>());
                }
            }
            for (FinanceSubjectVO subjectVO : subjectVOS) {
                HashMap<String, String> keyMap = new HashMap<>();
                keyMap.put("subjectName_resourceKey", "finance.subject." + subjectVO.getSubjectName());
                subjectVO.setLanguageKeyMap(keyMap);
            }
            //根据科目id获取辅助核算信息
            setAdjuvantList(subjectVOS);
            return subjectVOS;
        } else if (isTree != null && isTree == THREE) {
            List<FinanceSubjectVO> subjectVOS = getBaseMapper().queryListByType(new ArrayList<>(), type, AccountSet.getAccountSetId(), null);
            LambdaQueryChainWrapper<FinanceSubject> wrapper = lambdaQuery()
                    .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId());
            if (type != null) {
                wrapper.eq(FinanceSubject::getType, type);
            }
            List<FinanceSubject> subjectList = wrapper.list();
            if (returnType != null && returnType == 1) {
                for (FinanceSubjectVO subjectVO : subjectVOS) {
                    disposeName(subjectList, subjectVO, new ArrayList<>());
                    for (FinanceSubject financeSubject : subjectList) {
                        if (financeSubject.getSubjectId().equals(subjectVO.getSubjectId())) {
                            subjectVO.setParentId(financeSubject.getParentId());
                        }
                    }
                }

            }
            for (FinanceSubjectVO subjectVO : subjectVOS) {
                HashMap<String, String> keyMap = new HashMap<>();
                keyMap.put("subjectName_resourceKey", "finance.subject." + subjectVO.getSubjectName());
                subjectVO.setLanguageKeyMap(keyMap);
            }
            //根据科目id获取辅助核算信息
            setAdjuvantList(subjectVOS);
            return subjectVOS;
        } else if (isTree != null && isTree == FOUR) {
            LambdaQueryChainWrapper<FinanceSubject> subjects = lambdaQuery().eq(FinanceSubject::getStatus, 1)
                    .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId());
            if (type != null) {
                subjects.eq(FinanceSubject::getType, type);
            }
            List<FinanceSubject> list = subjects.list();
            for (FinanceSubject subject : list) {
                HashMap<String, String> keyMap = new HashMap<>();
                keyMap.put("subjectName_resourceKey", "finance.subject." + subject.getSubjectName());
                subject.setLanguageKeyMap(keyMap);
            }
            return RecursionUtil.getChildListTree(list, "parentId", 0L, "subjectId", "children", FinanceSubjectVO.class);
        } else if (isTree != null && isTree == five) {
            List<FinanceSubjectVO> subjectVOS = getBaseMapper().queryListByType(new ArrayList<>(), type, AccountSet.getAccountSetId(), 1);
            if (StrUtil.isNotEmpty(certificateTime)) {
                FinanceSubjectBalanceQueryBO accountBO = new FinanceSubjectBalanceQueryBO();
                accountBO.setStartTime(certificateTime);
                accountBO.setEndTime(certificateTime);
                accountBO.setAccountId(AccountSet.getAccountSetId());
                accountBO.setIsSubject(1);
                accountBO.setMinLevel(1);
                accountBO.setMaxLevel(9);
                List<JSONObject> jsonObjects = certificateService.querySubjectBalance(accountBO);
                JSONObject json = new JSONObject();
                for (JSONObject jsonObject : jsonObjects) {
                    json.put(jsonObject.getString("subjectId"), jsonObject);
                }
                for (FinanceSubjectVO vo : subjectVOS) {
                    JSONObject jsonObject = json.getJSONObject(String.valueOf(vo.getSubjectId()));
                    if (jsonObject != null) {
                        if (vo.getBalanceDirection() == 1) {
                            vo.setBalance(jsonObject.getBigDecimal("debtorEndBalance"));
                        } else {
                            vo.setBalance(jsonObject.getBigDecimal("creditEndBalance"));
                        }
                    }
                }
            }
            for (FinanceSubjectVO subjectVO : subjectVOS) {
                HashMap<String, String> keyMap = new HashMap<>();
                keyMap.put("subjectName_resourceKey", "finance.subject." + subjectVO.getSubjectName());
                subjectVO.setLanguageKeyMap(keyMap);
            }
            //根据科目id获取辅助核算信息
            setAdjuvantList(subjectVOS);
            return subjectVOS;
        } else {
            List<FinanceSubjectVO> subjectVOS = getBaseMapper().queryListByType(new ArrayList<>(), type, AccountSet.getAccountSetId(), null);
            for (FinanceSubjectVO subjectVO : subjectVOS) {
                HashMap<String, String> keyMap = new HashMap<>();
                keyMap.put("subjectName_resourceKey", "finance.subject." + subjectVO.getSubjectName());
                subjectVO.setLanguageKeyMap(keyMap);
            }
            //根据科目id获取辅助核算信息
            setAdjuvantList(subjectVOS);
            return subjectVOS;
        }
    }

    /**
     * 为科目设置辅助核算信息
     *
     * @param subjectVOS    科目列表
     */
    private void setAdjuvantList(List<FinanceSubjectVO> subjectVOS) {
        // 辅助核算
        List<FinanceSubjectAdjuvant> adjuvants = subjectAdjuvantService.lambdaQuery()
                .select(FinanceSubjectAdjuvant::getSubjectId, FinanceSubjectAdjuvant::getAdjuvantId,
                        FinanceSubjectAdjuvant::getLabel, FinanceSubjectAdjuvant::getLabelName)
                .eq(FinanceSubjectAdjuvant::getAccountId, AccountSet.getAccountSetId())
                .orderByAsc(FinanceSubjectAdjuvant::getLabel)
                .list();
        List<JSONObject> jsonObjects = adjuvants
                .stream()
                .map(BeanUtil::beanToMap)
                .map(MapUtil::removeNullValue)
                .map(s -> BeanUtil.toBean(s, JSONObject.class))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(subjectVOS) && CollUtil.isNotEmpty(jsonObjects)) {
            for (FinanceSubjectVO subjectVO : subjectVOS) {
                List<JSONObject> jsonObjectList = jsonObjects.stream()
                        .filter(o -> ObjectUtil.equal(subjectVO.getSubjectId(), o.getLong("subjectId")))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(jsonObjectList)) {
                    subjectVO.setAdjuvantList(jsonObjectList);
                }
            }
        }
    }

    /**
     * 处理名称数据
     *
     * @param subjects   科目列表
     * @param subjectVO  科目对象
     * @param subjectIds 上级Ids
     */
    private void disposeName(List<FinanceSubject> subjects, FinanceSubjectVO subjectVO, List<Long> subjectIds) {
        for (FinanceSubject s : subjects) {
            if (s.getSubjectId().equals(subjectVO.getParentId()) && !subjectIds.contains(s.getSubjectId())) {
                String subjectName = s.getSubjectName() + "_" + subjectVO.getSubjectName();
                subjectVO.setSubjectName(subjectName);
                subjectIds.add(s.getSubjectId());
                subjectVO.setParentId(s.getSubjectId());
                disposeName(subjects, subjectVO, subjectIds);
            }
        }
    }


    private Boolean querySubjectIdsByParentId(List<FinanceSubject> subjects, Long subjectId) {
        for (FinanceSubject data : subjects) {
            if (Objects.equals(subjectId, data.getParentId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<FinanceSubjectVO> queryList() {
        // 获取所有的科目
        List<FinanceSubject> subjects = queryAll();
        List<FinanceSubjectVO> subjectVOS = BeanUtil.copyToList(subjects, FinanceSubjectVO.class);
        // 为科目设置辅助核算信息
        setAdjuvantList(subjectVOS);
        return subjectVOS;
    }

    @Override
    public List<OperationLog> deleteByIds(List<Long> ids) {
        List<FinanceCertificateDetail> certificateDetails = certificateDetailService.lambdaQuery()
                .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId())
                .in(FinanceCertificateDetail::getSubjectId, ids).list();
        if (!certificateDetails.isEmpty()) {
            throw new CrmException(FinanceCodeEnum.FINANCE_TYPE_ERROR);
        }
        List<FinanceInitialAssist> assistList = financeInitialAssistService.lambdaQuery()
                .in(FinanceInitialAssist::getSubjectId, ids).list();
        if (!assistList.isEmpty()) {
            throw new CrmException(FinanceCodeEnum.FINANCE_SUBJECT_DELETE_ERROR);
        }
        List<FinanceSubject> financeSubjects = listByIds(ids);

        List<OperationLog> operationLogList = new ArrayList<>();
        for (FinanceSubject financeSubject : financeSubjects) {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationObject(financeSubject.getNumber());
            operationLog.setOperationInfo("删除科目：" + financeSubject.getNumber());
            operationLogList.add(operationLog);
        }
        removeByIds(ids);
        return operationLogList;
    }

    @Override
    public List<OperationLog> updateStatus(List<Long> ids, Integer status) {
        List<FinanceSubject> subjects = lambdaQuery().ne(FinanceSubject::getStatus, 3)
                .list();
        List<Long> subjectIds = new ArrayList<>();
        for (Long id : ids) {
            queryIdsByParentId(subjects, id, subjectIds);
        }
        ids.addAll(subjectIds);
        if (status == THREE) {
            List<FinanceCertificateDetail> certificateDetails = certificateDetailService.lambdaQuery()
                    .eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId())
                    .in(FinanceCertificateDetail::getSubjectId, ids).list();
            if (!certificateDetails.isEmpty()) {
                throw new CrmException(FinanceCodeEnum.FINANCE_TYPE_ERROR);
            }
            List<FinanceInitialAssist> assistList = financeInitialAssistService.lambdaQuery()
                    .in(FinanceInitialAssist::getSubjectId, ids).list();
            if (!assistList.isEmpty()) {
                throw new CrmException(FinanceCodeEnum.FINANCE_SUBJECT_DELETE_ERROR);
            }
        }


        List<FinanceSubject> financeSubjects = listByIds(ids);
        List<OperationLog> operationLogList = new ArrayList<>();
        for (FinanceSubject financeSubject : financeSubjects) {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationObject(financeSubject.getNumber());
            operationLog.setBehavior(status == 1 ? BehaviorEnum.START : BehaviorEnum.FORBID);
            operationLog.setOperationInfo(operationLog.getBehavior().getName() + "科目：" + financeSubject.getNumber());
            operationLogList.add(operationLog);
        }

        lambdaUpdate().set(FinanceSubject::getStatus, status).in(FinanceSubject::getSubjectId, ids).update();
        return operationLogList;
    }

    @Override
    public List<JSONObject> queryListCollect(FinanceCollectCertificateBO certificateBO) {
        //按照条件查询科目
        List<JSONObject> list = getBaseMapper().querySubjectIds(certificateBO);
        List<FinanceSubject> subjects = lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .list();
        List<JSONObject> subjectList = new ArrayList<>();
        list.forEach(json -> {
            List<Long> ids = new ArrayList<>();
            queryIdsByParentId(subjects, json.getLong("subjectId"), ids);
            ids.add(json.getLong("subjectId"));
            json.put("subjectIds", ids);
            if (!Collections.disjoint(ids, certificateBO.getSubjectIds())) {
                subjectList.add(json);
            }
        });
        return subjectList;
    }

    @Override
    public List<Long> queryIds(Long id, Integer minLevel, Integer maxLevel) {
        List<FinanceSubject> subjects = lambdaQuery().ne(FinanceSubject::getStatus, 3)
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .list();
        List<Long> ids = new ArrayList<>();
        queryIdsByParentId(subjects, id, ids);
        ids.add(id);
        return ids;
    }


    @Override
    public List<JSONObject> queryIds(FinanceDetailAccountBO accountBO) {
        List<FinanceSubject> subjects = lambdaQuery().ne(FinanceSubject::getStatus, 3)
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .orderByAsc(FinanceSubject::getNumber)
                .list();
        List<JSONObject> list = getBaseMapper().querySubjectByIds(accountBO);

        list.forEach(json -> {
            List<Long> ids = new ArrayList<>();
            queryIdsByParentId(subjects, json.getLong("subjectId"), ids);
            ids.add(json.getLong("subjectId"));
            json.put("subjectIds", ids);
        });
        return list;
    }

    @Override
    public FinanceSubjectIdsVO queryIdsByNumber(String number) {
        FinanceSubjectIdsVO idsVO = new FinanceSubjectIdsVO();
        List<Long> ids = new ArrayList<>();
        FinanceSubject subject = lambdaQuery().eq(FinanceSubject::getNumber, number)
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).one();
        if (subject == null) {
            ids.add(0L);
            idsVO.setBalanceDirection(0);
            idsVO.setIds(ids);
            idsVO.setSubjectId(0L);
            return idsVO;
        }
        List<FinanceSubject> subjects = lambdaQuery().ne(FinanceSubject::getStatus, 3)
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .list();
        queryIdsByParentId(subjects, subject.getSubjectId(), ids);
        ids.add(subject.getSubjectId());
        idsVO.setIds(ids);
        idsVO.setBalanceDirection(subject.getBalanceDirection());
        idsVO.setSubjectId(subject.getSubjectId());
        return idsVO;
    }

    @Override
    public FinanceSubjectIdsVO queryIdsById(Long id) {
        FinanceSubjectIdsVO idsVO = new FinanceSubjectIdsVO();
        if (id == null) {
            List<Long> ids = getBaseMapper().queryIds(new ArrayList<>(), null, null, AccountSet.getAccountSetId());
            idsVO.setBalanceDirection(0);
            idsVO.setIds(ids);
            return idsVO;
        }
        List<Long> ids = new ArrayList<>();
        FinanceSubject subject = lambdaQuery().eq(FinanceSubject::getSubjectId, id)
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).one();
        if (subject == null) {
            ids.add(0L);
            idsVO.setBalanceDirection(0);
            idsVO.setIds(ids);
            return idsVO;
        }
        List<FinanceSubject> subjects = lambdaQuery().ne(FinanceSubject::getStatus, 3)
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .list();
        queryIdsByParentId(subjects, subject.getSubjectId(), ids);
        ids.add(subject.getSubjectId());
        idsVO.setIds(ids);
        idsVO.setBalanceDirection(subject.getBalanceDirection());
        idsVO.setSubjectId(id);
        return idsVO;
    }

    @Override
    public FinanceSubjectIdsVO queryIdsByType(String certificateTime) {
        FinanceSubjectIdsVO idsVO = new FinanceSubjectIdsVO();
        List<Long> ids = getBaseMapper().queryIdsByType(certificateTime, AccountSet.getAccountSetId());
        if (ids.size() == 0) {
            ids.add(0L);
        }
        idsVO.setIds(ids);
        return idsVO;
    }

    @Override
    public void downloadExcel(HttpServletResponse response) throws IOException {
        List<JSONObject> list = queryField();
        ExcelParseUtil.importExcel(new ExcelParseUtil.ExcelParseService() {
            @Override
            public String getExcelName() {
                return "科目";
            }

            @Override
            public int addCell(ExcelWriter writer, Integer x, Integer y, String fieldName) {
                if (writer == null) {
                    return 0;
                }
                String subjectType = "subjectType";
                if (subjectType.equals(fieldName)) {
                    Workbook wb = writer.getWorkbook();
                    Sheet sheet = writer.getSheet();
                    for (int i = 0; i < TWO; i++) {
                        writer.setColumnWidth(x + i, 20);
                    }
                    Cell cell1 = writer.getOrCreateCell(x, y);
                    cell1.setCellValue("科目类型");
                    Sheet hideSheet = wb.createSheet(fieldName);
                    wb.setSheetHidden(wb.getSheetIndex(hideSheet), true);
                    int rowId = 0;
                    // 设置第一行，科目类型的信息
                    Row provinceRow = hideSheet.createRow(rowId++);
                    provinceRow.createCell(0).setCellValue("科目类型");
                    int line = 0;
                    List<String> provinces = new ArrayList<>();
                    for (FinanceCategoryEnum financeCategoryEnum : FinanceCategoryEnum.values()) {
                        if (financeCategoryEnum != FinanceCategoryEnum.NULL && financeCategoryEnum != FinanceCategoryEnum.CATEGORY_JOINTLY) {
                            Cell provinceCell = provinceRow.createCell(line + 1);
                            provinceCell.setCellValue(financeCategoryEnum.getName());
                            line++;
                            provinces.add(financeCategoryEnum.getName());
                        }
                    }
                    String[] provinceList = provinces.toArray(new String[]{});
                    CellRangeAddressList provRangeAddressList = new CellRangeAddressList(2, 10004, x, x);
                    DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                    DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(provinceList);
                    //设置下拉框数据
                    DataValidation dataValidation = validationHelper.createValidation(constraint, provRangeAddressList);
                    dataValidation.createErrorBox("error", "请选择正确的科目类型");
                    sheet.addValidationData(dataValidation);
                    return 0;
                }
                String balanceDirection = "balanceDirection";
                if (balanceDirection.equals(fieldName)) {
                    Workbook wb = writer.getWorkbook();
                    Sheet sheet = writer.getSheet();
                    for (int i = 0; i < TWO; i++) {
                        writer.setColumnWidth(x + i, 20);
                    }
                    Cell cell1 = writer.getOrCreateCell(x, y);
                    cell1.setCellValue("余额方向");
                    Sheet hideSheet = wb.createSheet(fieldName);
                    wb.setSheetHidden(wb.getSheetIndex(hideSheet), true);
                    int rowId = 0;
                    // 设置第一行，科目类型的信息
                    Row provinceRow = hideSheet.createRow(rowId++);
                    provinceRow.createCell(0).setCellValue("余额方向");
                    int line = 2;
                    List<String> provinces = new ArrayList<>();
                    Cell provinceCell = provinceRow.createCell(line);
                    provinceCell.setCellValue("借");
                    provinceCell.setCellValue("贷");
                    provinces.add("借");
                    provinces.add("贷");
                    String[] provinceList = provinces.toArray(new String[]{});
                    CellRangeAddressList provRangeAddressList = new CellRangeAddressList(2, 10004, x, x);
                    DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                    DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(provinceList);
                    //设置下拉框数据
                    DataValidation dataValidation = validationHelper.createValidation(constraint, provRangeAddressList);
                    dataValidation.createErrorBox("error", "请选择正确的余额方向");
                    sheet.addValidationData(dataValidation);
                    return 0;
                }

                return 0;
            }

            @Override
            public String getMergeContent(String module) {
                return "注意事项：\n" +
                        "1、表头标“*”的红色字体为必填项\n" +
                        "2、若导入的为一级科目，则上级科目编号填‘0’\n" +
                        "3、多辅助核算以“/”隔开";
            }
        }, list, response, "subject");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject excelImport(MultipartFile file) {
        List<List<Object>> errList = new ArrayList<>();
        String filePath = ExcelImportUtils.getFilePath(file);
        List<JSONObject> list = queryField();
        Map<String, Integer> map = new HashMap<>();
        AtomicReference<Integer> num = new AtomicReference<>(0);
        List<FinanceSubject> subjects = new ArrayList<>();
        Map<String, FinanceSubject> numberSubjectMap = new HashMap<>();
        //获取当前登陆人的系统参数
        JSONObject parameter = parameterService.queryParameter();
        ExcelUtil.readBySax(filePath, 0, (int sheetIndex, long rowIndex, List<Object> rowList) -> {
            if (rowIndex > 1) {
                num.getAndSet(num.get() + 1);
                //1.获取上级科目编码
                String parentNum = rowList.get(map.get("上级科目编码")).toString();
                if (ObjectUtil.notEqual(parentNum, "0")) {
                    //判断上级科目编码是否存在
                    FinanceSubject parentSubject = lambdaQuery().eq(FinanceSubject::getNumber, parentNum.trim()).eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).one();
                    if (ObjectUtil.isEmpty(parentSubject)) {
                        rowList.add(0, "上级科目编码不存在");
                        errList.add(rowList);
                        return;
                    }
                    //如果上级科目存在，查询上级科目是否有余额
                    Integer detailCount = parentSubjectBalance(parentSubject.getSubjectId());
                    if (detailCount > 0) {
                        rowList.add(0, "该科目上级科目已有数据，导入失败");
                        errList.add(rowList);
                        return;
                    }
                    //如果上级科目存在查询上级科目是否有初始余额
                    boolean initMoney = parentSubjectInitMoney(parentSubject.getSubjectId());
                    if (initMoney) {
                        rowList.add(0, "其母级科目有初始余额，导入失败");
                        errList.add(rowList);
                        return;
                    }
                }
                //辅助核算
                String adjuvantName = "";
                if (rowList.size() == 6) {
                    //保存科目辅助核算关联表
                    adjuvantName = rowList.get(map.getOrDefault("辅助核算", 0)).toString();
                }
                //2.获取导入科目编码
                String subjectNum = rowList.get(map.getOrDefault("科目编码", 0)).toString();
                //判断科目编码是否存在
                FinanceSubject originalSubject = lambdaQuery().eq(FinanceSubject::getNumber, subjectNum.trim()).eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).one();
                if (ObjectUtil.isNotEmpty(originalSubject)) {
                    String subjectName = rowList.get(map.get("科目名称")).toString();
                    //如果存在，查询是否有余额，有余额不能覆盖，没有余额可以覆盖
                    Integer detailCount = subjectBalance(originalSubject.getSubjectId());
                    if (detailCount > 0) {
                        rowList.add(0, subjectNum.trim() + subjectName + "科目上有余额，科目导入失败");
                        errList.add(rowList);
                        return;
                    }
                    FinanceInitial initial = initialService.lambdaQuery().eq(FinanceInitial::getSubjectId, originalSubject.getSubjectId()).eq(FinanceInitial::getAccountId, AccountSet.getAccountSetId()).one();
                    if (ObjectUtil.isNotEmpty(initial)) {
                        if (initial.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
                            rowList.add(0, subjectNum.trim() + subjectName + "科目上有财务初始余额，科目导入失败");
                            errList.add(rowList);
                            return;
                        }
                    }
                    //如果科目存在，查询子集科目是否有余额
                    Integer count = childSubjectBalance(originalSubject.getSubjectId());
                    if (count > 0) {
                        rowList.add(0, "其子集科目已有数据，导入失败。");
                        errList.add(rowList);
                        return;
                    }
                    if (ObjectUtil.isNotEmpty(adjuvantName)) {
                        //查询是否有下级科目
                        boolean childCount = lambdaQuery().in(FinanceSubject::getParentId, originalSubject.getSubjectId()).exists();
                        if (childCount) {
                            rowList.add(0, subjectNum.trim() + subjectName + "不是末级科目，辅助核算导入失败。");
                            errList.add(rowList);
                            return;
                        }
                    }
                }
                //如果有辅助核算信息，判断是不是末级科目
                if (ObjectUtil.isNotEmpty(adjuvantName)) {
                    //校验所填的辅助核算类别是否被包含在【设置---辅助核算】里
                    List<String> nameList = StrUtil.split(adjuvantName.trim(), "/");
                    for (String name : nameList) {
                        boolean adjuvantCount = adjuvantService.lambdaQuery().eq(FinanceAdjuvant::getAdjuvantName, name).eq(FinanceAdjuvant::getAccountId, AccountSet.getAccountSetId()).exists();
                        if (!adjuvantCount) {
                            rowList.add(0, "辅助核算类别错误。");
                            errList.add(rowList);
                            return;
                        }
                    }
                }
                //3.获取科目编码长度
                String rule = parameter.get("rule").toString();
                List<Integer> ruleList = Arrays.stream(rule.split("-")).map(Integer::parseInt).collect(Collectors.toList());

                if (ObjectUtil.equal(parentNum, "0")) {
                    //上级科目编码为0则为一级科目
                    //4.获取科目编码第一级
                    Integer one = ruleList.get(0);
                    //5.判断导入科目编码长度与科目编码长度是否相等
                    if (ObjectUtil.notEqual(one, subjectNum.length())) {
                        rowList.add(0, "第" + Convert.toInt(NumberUtil.add(rowIndex, 1)) + "行科目长度错误，未满足科目编码规则定义, 科目编码结构=[" + rule + "]");
                        errList.add(rowList);
                        return;
                    }
                } else {
                    //6.判断上级科目编码
                    int i = 0;
                    int j = 0;
                    for (int ru : ruleList) {
                        j++;
                        i = i + ru;
                        //判断上级科目编码长度，处理导入科目编码
                        if (ObjectUtil.equal(i, parentNum.length())) {
                            if (j <= ruleList.size()) {
                                //获取下一级次的长度
                                int level = ruleList.get(j) + i;
                                //7.判断导入科目编码长度与下一级次长度是否相同
                                if (ObjectUtil.notEqual(level, subjectNum.length())) {
                                    rowList.add(0, "第" + Convert.toInt(NumberUtil.add(rowIndex, 1)) + "行科目长度错误，未满足科目编码规则定义, 科目编码结构=[" + rule + "]");
                                    errList.add(rowList);
                                    return;
                                }
                            } else {
                                rowList.add(0, "第" + Convert.toInt(NumberUtil.add(rowIndex, 1)) + "行科目长度错误，未满足科目编码规则定义, 科目编码结构=[" + rule + "]");
                                errList.add(rowList);
                                return;
                            }
                        }
                    }
                }

                JSONObject json = new JSONObject();
                for (JSONObject jsonObject : list) {
                    String name = jsonObject.getString("name");
                    Integer index = map.get(name);
                    if (ObjectUtil.equal("辅助核算", name)) {
                        if (rowList.size() < 6) {
                            continue;
                        }
                    }
                    Object object = rowList.get(index);
                    if (jsonObject.getInteger("isNull") == 1 && object == null) {
                        rowList.add(0, name + "不能为空");
                        errList.add(rowList);
                        return;
                    }
                    if (Objects.equals("余额方向", name)) {
                        json.put(jsonObject.getString("fieldName"), FinanceBalanceDirectionEnum.parseName(object.toString()).getType());
                    } else {
                        json.put(jsonObject.getString("fieldName"), object);
                    }
                }
                FinanceSubject subject = json.toJavaObject(FinanceSubject.class);
                FinanceCategoryEnum categoryEnum = FinanceCategoryEnum.parseName(json.getString("subjectType"));
                subject.setType(categoryEnum.getType());

                subject.setCategory(categoryEnum.getCategory());
                subject.setIsCash(0);
                subject.setStatus(1);
                subject.setCreateTime(LocalDateTime.now());
                subject.setCreateUserId(UserUtil.getUserId());
                subject.setGrade(1);
                subject.setAccountId(AccountSet.getAccountSetId());
                //查询父级科目
                List<FinanceSubject> financeSubject = lambdaQuery().eq(FinanceSubject::getNumber, json.getString("pNumber"))
                        .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                        .ne(FinanceSubject::getStatus, 3).list();
                if (!financeSubject.isEmpty()) {
                    subject.setParentId(financeSubject.get(0).getSubjectId());
                    subject.setGrade(financeSubject.get(0).getGrade() + 1);
                }
                if (ObjectUtil.isNotEmpty(originalSubject)) {
                    //删除现有科目，财务初始余额表科目,科目和辅助核算关联表，科目和币种关联表
                    lambdaUpdate().eq(FinanceSubject::getSubjectId, originalSubject.getSubjectId()).remove();
                    initialService.lambdaUpdate().eq(FinanceInitial::getSubjectId, originalSubject.getSubjectId()).remove();
                    subjectAdjuvantService.lambdaUpdate().eq(FinanceSubjectAdjuvant::getSubjectId, originalSubject.getSubjectId()).remove();
                    subjectCurrencyService.lambdaUpdate().eq(FinanceSubjectCurrency::getSubjectId, originalSubject.getSubjectId()).remove();
                }

                save(subject);
                //保存科目辅助核算关联表
                if (ObjectUtil.isNotEmpty(adjuvantName)) {
                    saveSubjectAdjuvant(adjuvantName, subject.getSubjectId());
                }
                if (ObjectUtil.isNotEmpty(originalSubject)) {
                    //根据原始科目ID修改子集
                    lambdaUpdate().eq(FinanceSubject::getParentId, originalSubject.getSubjectId()).set(FinanceSubject::getParentId, subject.getSubjectId()).update();
                }
                subjects.add(subject);
            } else {
                if (rowIndex == 1) {
                    ExcelImportUtils.queryMapByList(list, rowList, map);
                    rowList.add(0, "错误信息");
                }
                errList.add(Convert.toInt(rowIndex), rowList);
            }
        });
        subjects.sort(Comparator.comparing(FinanceSubject::getNumber));
        for (FinanceSubject subject : subjects) {
            int length = subject.getNumber().length();
            if (length > 4) {
                int index = length - 2;
                while (index >= FOUR) {
                    FinanceSubject pSubject = numberSubjectMap.get(StrUtil.sub(subject.getNumber(), 0, index));
                    if (ObjectUtil.isNotNull(pSubject)) {
                        subject.setParentId(pSubject.getSubjectId());
                        subject.setGrade(pSubject.getGrade() + 1);
                        break;
                    }
                    index -= 2;
                }
            }
            saveOrUpdate(subject);
            numberSubjectMap.put(subject.getNumber(), subject);
        }
        FileUtil.del(filePath);
        JSONObject result = new JSONObject().fluentPut("totalSize", num.get()).fluentPut("errSize", 0);
        if (errList.size() > TWO) {
            File errFile = FileUtil.file("excelData/" + IdUtil.simpleUUID() + ".xlsx");
            BigExcelWriter writer = ExcelUtil.getBigWriter(errFile);
            // 取消数据的黑色边框以及数据左对齐
            CellStyle cellStyle = writer.getCellStyle();
            cellStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
            cellStyle.setBorderTop(BorderStyle.NONE);
            cellStyle.setBorderBottom(BorderStyle.NONE);
            cellStyle.setBorderLeft(BorderStyle.NONE);
            cellStyle.setBorderRight(BorderStyle.NONE);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            Font defaultFont = writer.createFont();
            defaultFont.setFontHeightInPoints((short) 11);
            cellStyle.setFont(defaultFont);
            // 取消数字格式的数据的黑色边框以及数据左对齐
            CellStyle cellStyleForNumber = writer.getStyleSet().getCellStyleForNumber();
            cellStyleForNumber.setBorderTop(BorderStyle.NONE);
            cellStyleForNumber.setBorderBottom(BorderStyle.NONE);
            cellStyleForNumber.setBorderLeft(BorderStyle.NONE);
            cellStyleForNumber.setBorderRight(BorderStyle.NONE);
            cellStyleForNumber.setAlignment(HorizontalAlignment.LEFT);
            cellStyleForNumber.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
            cellStyleForNumber.setFont(defaultFont);

            CellStyle textStyle = writer.getWorkbook().createCellStyle();
            DataFormat format = writer.getWorkbook().createDataFormat();
            textStyle.setDataFormat(format.getFormat("@"));

            writer.merge(errList.get(1).size() + 1, errList.get(0).get(0).toString().trim(), true);
            writer.getHeadCellStyle().setAlignment(HorizontalAlignment.LEFT);
            writer.getHeadCellStyle().setWrapText(true);
            Font headFont = writer.createFont();
            headFont.setFontHeightInPoints((short) 11);
            writer.getHeadCellStyle().setFont(headFont);
            writer.getHeadCellStyle().setFillPattern(FillPatternType.NO_FILL);
            writer.getOrCreateRow(0).setHeightInPoints(120);
            writer.setRowHeight(-1, 20);

            //writer.merge(6, "系统用户导入模板(*)为必填项");
            for (int i = 0; i < errList.get(1).size(); i++) {
                writer.getSheet().setDefaultColumnStyle(i, textStyle);
            }
            errList.remove(0);
            writer.write(errList);
            writer.close();
            //将错误信息的excel保存七天 604800 七天的秒数
            MultipartFile multipartFile = MultipartFileUtil.getMultipartFile(errFile);
            UploadEntity uploadEntity = fileService.uploadTempFile(multipartFile, IdUtil.simpleUUID());
            BaseUtil.getRedis().setex(AdminCacheKey.UPLOAD_EXCEL_MESSAGE_PREFIX + "file:" + uploadEntity.getFileId(), 604800, uploadEntity.getPath());
            result.fluentPut("errSize", errList.size() - 1).fluentPut("token", uploadEntity.getFileId());

        }
        return result;
    }

    /**
     * 根据科目编码查询时候有余额
     *
     * @param subjectId
     * @return
     */
    private Integer subjectBalance(Long subjectId) {
        Long count = certificateDetailService.lambdaQuery().eq(FinanceCertificateDetail::getSubjectId, subjectId).eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId()).count();
        return count.intValue();
    }

    /**
     * 保存科目辅助核算关联
     *
     * @param adjuvantName
     * @param subjectId
     */
    private void saveSubjectAdjuvant(String adjuvantName, Long subjectId) {
        //查询辅助核算根据辅助核算名称
        List<String> nameList = StrUtil.split(adjuvantName.trim(), "/");
        nameList.forEach(name -> {
            FinanceAdjuvant financeAdjuvant = adjuvantService.lambdaQuery().eq(FinanceAdjuvant::getAdjuvantName, name).eq(FinanceAdjuvant::getAccountId, AccountSet.getAccountSetId()).one();
            FinanceSubjectAdjuvant subjectAdjuvant = new FinanceSubjectAdjuvant();
            subjectAdjuvant.setSubjectId(subjectId);
            subjectAdjuvant.setAdjuvantId(financeAdjuvant.getAdjuvantId());
            subjectAdjuvant.setAccountId(AccountSet.getAccountSetId());
            subjectAdjuvant.setLabel(financeAdjuvant.getLabel());
            subjectAdjuvant.setLabelName(financeAdjuvant.getAdjuvantName());
            subjectAdjuvantService.save(subjectAdjuvant);
        });
    }

    /**
     * 查询上级科目是否有余额
     *
     * @param subjectId
     * @return
     */
    private Integer parentSubjectBalance(Long subjectId) {
        Long count = certificateDetailService.lambdaQuery().eq(FinanceCertificateDetail::getSubjectId, subjectId).eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId()).count();
        if (count.intValue() == 0) {
            //如果没有余额，查询上级科目
            FinanceSubject parentSubject = lambdaQuery().eq(FinanceSubject::getSubjectId, subjectId).eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).one();
            if (ObjectUtil.isNotEmpty(parentSubject)) {
                if (ObjectUtil.equal(0L, parentSubject.getParentId())) {
                    subjectBalance(parentSubject.getSubjectId());
                } else {
                    parentSubjectBalance(parentSubject.getParentId());
                }
            }
        }
        return count.intValue();
    }

    private boolean parentSubjectInitMoney(Long subjectId) {
        boolean isMoney = false;
        FinanceInitial initial = initialService.lambdaQuery().eq(FinanceInitial::getSubjectId, subjectId).eq(FinanceInitial::getAccountId, AccountSet.getAccountSetId()).one();
        if (ObjectUtil.isNotEmpty(initial)) {
            if (initial.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
                isMoney = true;
            } else {
                //如果没有余额，查询上级科目
                FinanceSubject parentSubject = lambdaQuery().eq(FinanceSubject::getSubjectId, subjectId).eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId()).one();
                if (ObjectUtil.isNotEmpty(parentSubject)) {
                    parentSubjectInitMoney(parentSubject.getSubjectId());
                }
            }
        }
        return isMoney;
    }

    private Integer childSubjectBalance(Long subjectId) {
        //查询科目
        List<FinanceSubject> financeSubjects = lambdaQuery().in(FinanceSubject::getParentId, subjectId).list();
        Integer count = 0;
        for (FinanceSubject subject : financeSubjects) {
            count = childBalance(subject.getSubjectId());
            if (count > 0) {
                return count;
            }
        }
        return count;
    }

    private Integer childBalance(Long subjectId) {
        Long count = certificateDetailService.lambdaQuery().eq(FinanceCertificateDetail::getSubjectId, subjectId).eq(FinanceCertificateDetail::getAccountId, AccountSet.getAccountSetId()).count();
        if (count.intValue() == 0) {
            //如果没有余额，继续查询下级科目
            childSubjectBalance(subjectId);
        }
        return count.intValue();
    }

    private List<JSONObject> queryField() {
        List<JSONObject> list = new ArrayList<>();
        list.add(ExcelImportUtils.queryField("number", FieldEnum.TEXT.getFormType(), FieldEnum.TEXT.getType(), "科目编码", 1));
        list.add(ExcelImportUtils.queryField("subjectName", FieldEnum.TEXT.getFormType(), FieldEnum.TEXT.getType(), "科目名称", 1));
        list.add(ExcelImportUtils.queryField("pNumber", FieldEnum.TEXT.getFormType(), FieldEnum.TEXT.getType(), "上级科目编码", 1));
        list.add(ExcelImportUtils.queryField("balanceDirection", FieldEnum.DATE.getFormType(), FieldEnum.DATE.getType(), "余额方向", 1));
        list.add(ExcelImportUtils.queryField("subjectType", FieldEnum.TEXT.getFormType(), FieldEnum.TEXT.getType(), "类别", 1));
        list.add(ExcelImportUtils.queryField("adjuvant", FieldEnum.TEXT.getFormType(), FieldEnum.TEXT.getType(), "辅助核算", 0));
        return list;
    }

    /**
     * 根据科目id获取辅助核算信息
     *
     * @param subjectId
     * @return
     */
    @Override
    public List<Integer> getAdjuvantList(Long subjectId) {
        return subjectAdjuvantService.lambdaQuery().eq(FinanceSubjectAdjuvant::getSubjectId, subjectId).list().stream().map(FinanceSubjectAdjuvant::getLabel).collect(Collectors.toList());
    }

    @Override
    public List<Long> queryIds() {
        return baseMapper.queryIdsByUnit(AccountSet.getAccountSetId());
    }


    private void queryIdsByParentId(List<FinanceSubject> subjects, Long subjectId, List<Long> ids) {
        for (FinanceSubject data : subjects) {
            if (Objects.equals(subjectId, data.getParentId())) {
                ids.add(data.getSubjectId());
                queryIdsByParentId(subjects, data.getSubjectId(), ids);
            }
        }
    }

    @Override
    public List<FinanceSubject> queryAll() {
        return lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .orderByAsc(FinanceSubject::getNumber)
                .list();
    }

    @Override
    public List<FinanceSubject> queryFirstGradeByType(Integer type) {
        List<FinanceSubject> subjects = lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .and(i -> i.eq(FinanceSubject::getParentId, 0L).or().eq(FinanceSubject::getParentId, null))
                .eq(FinanceSubject::getType, type)
                .ne(FinanceSubject::getStatus, 3)
                .list();
        Integer firstGrade = subjects.stream().min(Comparator.comparing(FinanceSubject::getGrade)).get().getGrade();
        subjects = subjects.stream().filter(o -> ObjectUtil.equal(firstGrade, o.getGrade())).collect(Collectors.toList());
        return subjects;
    }

    /**
     * 导出科目
     *
     * @param exportBO
     */
    @Override
    public void exportListByType(FinanceExportBO exportBO) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollUtil.isNotEmpty(exportBO.getDataList())) {
            mapList = exportBO.getDataList();
        }
        for (Map map : mapList) {
            //类别
            if (ObjectUtil.isNotEmpty(map.get("type"))) {
                Integer type = Convert.toInt(map.get("type"));
                Integer category = Convert.toInt(map.get("category"));
                map.put("categoryName", FinanceCategoryEnum.parseCategory(type, category));
            }
            if (ObjectUtil.isNotEmpty(map.get("balanceDirection"))) {
                if (ObjectUtil.equal(1, map.get("balanceDirection"))) {
                    map.put("balanceDirection", "借");
                } else {
                    map.put("balanceDirection", "贷");
                }
            }
            if (ObjectUtil.isNotEmpty(map.get("isCash"))) {
                if (ObjectUtil.equal(1, map.get("isCash"))) {
                    map.put("isCash", "是");
                } else {
                    map.put("isCash", "否");
                }
            }
        }

        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("number", "编码"));
        dataList.add(ExcelParseUtil.toEntity("subjectName", "名称"));
        dataList.add(ExcelParseUtil.toEntity("categoryName", "类别"));
        dataList.add(ExcelParseUtil.toEntity("balanceDirection", "余额方向"));
        dataList.add(ExcelParseUtil.toEntity("labelName", "辅助核算"));
        dataList.add(ExcelParseUtil.toEntity("isAmount", "数量"));
        dataList.add(ExcelParseUtil.toEntity("isCash", "是否是现金项"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "科目设置";
            }

            @Override
            public String addCompany() {
                return "";
            }

            @Override
            public String addPeriod() {
                return null;
            }

            @Override
            public String addUnit() {
                return null;
            }
        }, dataList);
    }

}
