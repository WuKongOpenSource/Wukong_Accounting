package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.entity.UserInfo;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.BO.FinanceAddInitialBO;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceInitialPageBO;
import com.kakarote.finance.entity.PO.*;
import com.kakarote.finance.mapper.FinanceInitialMapper;
import com.kakarote.finance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 财务初始余额 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@Service
public class FinanceInitialServiceImpl extends BaseServiceImpl<FinanceInitialMapper, FinanceInitial> implements IFinanceInitialService {

    @Autowired
    private IFinanceInitialAssistService financeInitialAssistService;

    @Autowired
    private IFinanceInitialAssistAdjuvantService financeInitialAssistAdjuvantService;

    @Autowired
    private IFinanceSubjectAdjuvantService financeSubjectAdjuvantService;

    @Autowired
    private IFinanceAssistService financeAssistService;

    @Autowired
    private IFinanceAssistAdjuvantService assistAdjuvantService;

    @Autowired
    private IFinanceAdjuvantService adjuvantService;

    @Autowired
    private IFinanceInitialService initialService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInitial(List<FinanceAddInitialBO> bos) {
        Long accountId = AccountSet.getAccountSetId();
        for (FinanceAddInitialBO addInitialBO : bos) {
            // 参数检查
            if (!ObjectUtil.isAllNotEmpty(addInitialBO, addInitialBO.getAdjuvantId(), addInitialBO.getSubjectId(), addInitialBO.getCarteIdList())) {
                continue;
            }
            FinanceAdjuvant adjuvant = adjuvantService.getById(addInitialBO.getAdjuvantId());
            List<JSONObject> assistAdjuvantList = baseMapper.queryInitialAssistAdjuvant(addInitialBO.getSubjectId(), AccountSet.getAccountSetId());
            Set<Long> carteIds = assistAdjuvantList.stream().map(o -> o.getLong("relationId")).collect(Collectors.toSet());
            for (Long carteId : addInitialBO.getCarteIdList()) {
                if (CollUtil.contains(carteIds, carteId)) {
                    continue;
                }
                UserInfo userInfo = UserUtil.getUser();
                // 初始余额
                FinanceInitial initial = new FinanceInitial();
                initial.setSubjectId(addInitialBO.getSubjectId());
                initial.setCreateTime(LocalDateTime.now());
                initial.setCreateUserId(userInfo.getUserId());
                initial.setAccountId(accountId);
                initial.setIsAssist(true);
                initialService.save(initial);
                // 辅助数据
                FinanceAssist assist = new FinanceAssist();
                assist.setSubjectId(addInitialBO.getSubjectId());
                assist.setCreateTime(LocalDateTime.now());
                assist.setCreateUserId(userInfo.getUserId());
                assist.setAccountId(accountId);
                financeAssistService.save(assist);
                // 初始余额辅助核算
                FinanceInitialAssist initialAssist = new FinanceInitialAssist();
                initialAssist.setInitialId(initial.getInitialId());
                initialAssist.setFinanceAssistId(assist.getAssistId());
                initialAssist.setSubjectId(addInitialBO.getSubjectId());
                initialAssist.setCreateTime(LocalDateTime.now());
                initialAssist.setCreateUserId(userInfo.getUserId());
                initialAssist.setAccountId(accountId);
                financeInitialAssistService.save(initialAssist);
                // 初始余额管理辅助核算
                FinanceInitialAssistAdjuvant initialAssistAdjuvant = new FinanceInitialAssistAdjuvant();
                initialAssistAdjuvant.setAssistId(initialAssist.getAssistId());
                initialAssistAdjuvant.setAdjuvantId(addInitialBO.getAdjuvantId());
                initialAssistAdjuvant.setRelationId(carteId);
                initialAssistAdjuvant.setLabel(adjuvant.getLabel());
                initialAssistAdjuvant.setLabelName(adjuvant.getAdjuvantName());
                initialAssistAdjuvant.setCreateTime(LocalDateTime.now());
                initialAssistAdjuvant.setCreateUserId(userInfo.getUserId());
                initialAssistAdjuvant.setAccountId(accountId);
                financeInitialAssistAdjuvantService.save(initialAssistAdjuvant);
                // 辅助核算关联
                FinanceAssistAdjuvant assistAdjuvant = new FinanceAssistAdjuvant();
                assistAdjuvant.setLabel(initialAssistAdjuvant.getLabel());
                assistAdjuvant.setRelationId(initialAssistAdjuvant.getRelationId());
                assistAdjuvant.setAdjuvantId(initialAssistAdjuvant.getAdjuvantId());
                assistAdjuvant.setAssistId(assist.getAssistId());
                assistAdjuvant.setAccountId(accountId);
                assistAdjuvant.setLabelName(initialAssistAdjuvant.getLabelName());
                assistAdjuvantService.save(assistAdjuvant);

            }
        }
    }

    @Override
    public void updateInitialValue(List<FinanceInitial> initials) {
        List<FinanceInitial> currentInitials = lambdaQuery().eq(FinanceInitial::getAccountId, AccountSet.getAccountSetId()).list();
        Map<Long, FinanceInitial> initialMap = currentInitials.stream().collect(Collectors.toMap(FinanceInitial::getInitialId, Function.identity()));
        List<FinanceInitial> toUpdateData = new ArrayList<>();
        for (FinanceInitial initial : initials) {
            FinanceInitial financeInitial = initialMap.get(initial.getInitialId());
            if (ObjectUtil.isNotNull(financeInitial)) {
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
            saveOrUpdateBatch(toUpdateData);
        }
    }

    @Override
    public List<JSONObject> queryListBySubjectType(Integer subjectType) {
        List<JSONObject> list = getBaseMapper().queryListBySubjectType(subjectType, AccountSet.getAccountSetId());
        for (JSONObject json : list) {
            if (json.getInteger("isAssist") == 1) {
                List<FinanceInitialAssistAdjuvant> assistAdjuvantList = baseMapper.queryAssistAdjuvant(json.getLong("assistId"), AccountSet.getAccountSetId());
                json.put("assistAdjuvants", assistAdjuvantList);
            }
            if (json.getInteger("isAdjuvant") == 1) {
                List<FinanceSubjectAdjuvant> subjectAdjuvants = financeSubjectAdjuvantService.lambdaQuery()
                        .eq(FinanceSubjectAdjuvant::getSubjectId, json.getLong("subjectId")).list();
                json.put("subjectAdjuvants", subjectAdjuvants);
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BasePage<JSONObject> queryListBySubjectType(FinanceInitialPageBO bo) {
        BasePage<FinanceInitialPageBO> page = new BasePage<>(bo.getPage(), bo.getLimit());
        bo.setAccountId(AccountSet.getAccountSetId());
        List<JSONObject> initials = baseMapper.queryAllInitial(AccountSet.getAccountSetId());
        // 待生成初始化余额的数据
        List<JSONObject> toInit = initials.stream().filter(o -> ObjectUtil.isNull(o.getLong("initialId"))).collect(Collectors.toList());
        UserInfo userInfo = UserUtil.getUser();
        Long accountId = AccountSet.getAccountSetId();
        if (CollUtil.isNotEmpty(toInit)) {
            List<FinanceInitial> initialList = new ArrayList<>();
            for (JSONObject o : toInit) {
                // 初始余额
                FinanceInitial initial = new FinanceInitial();
                initial.setSubjectId(o.getLong("subjectId"));
                initial.setCreateTime(LocalDateTime.now());
                initial.setCreateUserId(userInfo.getUserId());
                initial.setAccountId(accountId);
                initialList.add(initial);
            }
            saveBatch(initialList);
        }
        // 当前账套所有科目和辅助核算关系
        List<FinanceSubjectAdjuvant> subjectAdjuvants = financeSubjectAdjuvantService.lambdaQuery()
                .eq(FinanceSubjectAdjuvant::getAccountId, AccountSet.getAccountSetId()).list();
        Map<Long, List<FinanceSubjectAdjuvant>> subjectAdjuvantMap = subjectAdjuvants.stream().collect(Collectors.groupingBy(FinanceSubjectAdjuvant::getSubjectId));
        BasePage<JSONObject> basePage = baseMapper.queryPageListBySubjectType(page, bo);
        List<String> list = Arrays.asList("initialNum", "initialBalance", "addUpDebtorNum", "addUpDebtorBalance", "addUpCreditNum", "addUpCreditBalance", "beginningNum", "beginningBalance", "profitBalance");
        // 处理有辅助核算信息的初始余额科目

        for (JSONObject object : basePage.getList()) {
            // 非辅助核的初始余额的科目，返回关联的辅助核算
            Long assistId = object.getLong("assistId");
            if (ObjectUtil.isNotNull(assistId)) {
                continue;
            }
            Long subjectId = object.getLong("subjectId");
            List<FinanceSubjectAdjuvant> subjectAdjuvantList = subjectAdjuvantMap.get(subjectId);
            if (CollUtil.isNotEmpty(subjectAdjuvantList)) {
                object.put("subjectAdjuvantList", subjectAdjuvantList);
            }
            for (String s : list) {
                if (ObjectUtil.isNull(object.get(s))) {
                    object.put(s, BigDecimal.ZERO);
                }
            }
        }
        return basePage;
    }

    @Override
    public JSONObject queryTrialBalance() {
        return getBaseMapper().queryTrialBalance(AccountSet.getAccountSetId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long initialId) {
        FinanceInitial initial = getById(initialId);
        List<FinanceInitialAssist> assistList = financeInitialAssistService.lambdaQuery().eq(FinanceInitialAssist::getInitialId, initial).list();
        for (FinanceInitialAssist assist : assistList) {
            financeInitialAssistAdjuvantService.lambdaUpdate().eq(FinanceInitialAssistAdjuvant::getAssistId, assist.getAssistId()).remove();
            financeInitialAssistService.removeById(assist.getAssistId());
        }
        removeById(initialId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAssistId(Long assistId) {
        FinanceInitialAssist assist = financeInitialAssistService.getById(assistId);
        financeInitialAssistAdjuvantService.lambdaUpdate().eq(FinanceInitialAssistAdjuvant::getAssistId, assistId).remove();
        financeInitialAssistService.removeById(assistId);
        financeAssistService.removeById(assist.getFinanceAssistId());
        assistAdjuvantService.lambdaUpdate().eq(FinanceAssistAdjuvant::getAssistId, assist.getFinanceAssistId()).remove();
        removeById(assist.getInitialId());
    }

    @Override
    public FinanceInitial queryByAdjuvant(FinanceInitialAssist assist) {
        List<FinanceInitialAssist> assistList = financeInitialAssistService.lambdaQuery().eq(FinanceInitialAssist::getSubjectId, assist.getSubjectId()).list();
        for (FinanceInitialAssist initialAssist : assistList) {
            List<FinanceInitialAssistAdjuvant> assistAdjuvants = financeInitialAssistAdjuvantService.lambdaQuery().eq(FinanceInitialAssistAdjuvant::getAssistId, initialAssist.getAssistId()).list();
            if (assist.getAssistAdjuvantList().size() == assistAdjuvants.size()) {
                Integer count = 0;
                for (FinanceInitialAssistAdjuvant assistAdjuvant : assistAdjuvants) {
                    for (FinanceInitialAssistAdjuvant adjuvant : assist.getAssistAdjuvantList()) {
                        if (ObjectUtil.equal(assistAdjuvant.getAdjuvantId(), adjuvant.getAdjuvantId()) && ObjectUtil.equal(assistAdjuvant.getRelationId(), adjuvant.getRelationId())) {
                            count = count + 1;
                        }
                    }
                }
                if (count == assistAdjuvants.size()) {
                    return getById(initialAssist.getInitialId());
                }
            }
        }
        return new FinanceInitial();
    }

    @Override
    public List<FinanceInitial> queryAll(Long accountId) {
        List<FinanceInitial> initials = lambdaQuery().eq(FinanceInitial::getAccountId, accountId).list();
        return initials;
    }

    @Override
    public List<JSONObject> queryAllWithAssistId(Long accountId) {
        return baseMapper.queryAllInitial(accountId);
    }

    @Override
    public List<JSONObject> queryAdjuvantInitial(FinanceDetailAccountBO accountBO) {
        return baseMapper.queryAdjuvantInitial(accountBO);
    }

    @Override
    public Map<Long, FinanceInitial> listToMap(List<FinanceInitial> initials) {
        return initials.stream().collect(Collectors.toMap(FinanceInitial::getSubjectId, Function.identity()));
    }
}
