package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.constant.AdjuvantTypeEnum;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.entity.PO.FinanceAdjuvant;
import com.kakarote.finance.entity.PO.FinanceAdjuvantCarte;
import com.kakarote.finance.entity.PO.FinanceSubjectAdjuvant;
import com.kakarote.finance.mapper.FinanceAdjuvantMapper;
import com.kakarote.finance.service.IFinanceAdjuvantCarteService;
import com.kakarote.finance.service.IFinanceAdjuvantService;
import com.kakarote.finance.service.IFinanceSubjectAdjuvantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 辅助核算表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Service
public class FinanceAdjuvantServiceImpl extends BaseServiceImpl<FinanceAdjuvantMapper, FinanceAdjuvant> implements IFinanceAdjuvantService {

    @Autowired
    private IFinanceAdjuvantCarteService carteService;

    @Autowired
    private IFinanceSubjectAdjuvantService subjectAdjuvantService;

    /**
     * 辅助核算新增修改
     *
     * @param adjuvant
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationLog saveAndUpdate(FinanceAdjuvant adjuvant) {
        String adjuvantName = adjuvant.getAdjuvantName();
        FinanceAdjuvant financeAdjuvant = lambdaQuery().eq(FinanceAdjuvant::getAdjuvantName, adjuvantName).one();
        if (ObjectUtil.isNotNull(financeAdjuvant)) {
            if (ObjectUtil.notEqual(adjuvant.getAdjuvantId(), financeAdjuvant.getAdjuvantId())) {
                throw new CrmException(400, "辅助核算名称已存在");
            }
        }
        OperationLog operationLog = new OperationLog();

        if (ObjectUtil.isEmpty(adjuvant.getAdjuvantId())) {
            adjuvant.setLabel(AdjuvantTypeEnum.CUSTOM.getType());
            adjuvant.setAccountId(AccountSet.getAccountSetId());
            adjuvant.setAdjuvantType(0);
            save(adjuvant);
            operationLog.setOperationObject(adjuvantName);
            operationLog.setOperationInfo("新建辅助核算分类：" + adjuvantName);
        } else {
            updateById(adjuvant);
            operationLog.setOperationObject(adjuvantName);
            operationLog.setOperationInfo("编辑辅助核算分类：" + adjuvantName);
        }
        return operationLog;
    }

    /**
     * 查询辅助核算列表
     *
     * @return
     */
    @Override
    public List<FinanceAdjuvant> queryAllList() {
        if (ObjectUtil.isEmpty(AccountSet.getAccountSetId())) {
            return new ArrayList<>();
        }
        //查询是否有辅助核算数据
        List<FinanceAdjuvant> adjuvantList = lambdaQuery().eq(FinanceAdjuvant::getAccountId, AccountSet.getAccountSetId()).list();
        if (adjuvantList.size() < 1) {
            //新增原始固定数据
            for (AdjuvantTypeEnum typeEnum : AdjuvantTypeEnum.values()) {
                if (typeEnum.equals(AdjuvantTypeEnum.CUSTOM) || typeEnum.equals(AdjuvantTypeEnum.NULL)) {
                    continue;
                }
                FinanceAdjuvant adjuvant = new FinanceAdjuvant();
                adjuvant.setAdjuvantName(typeEnum.getRemarks());
                adjuvant.setAdjuvantType(1);
                adjuvant.setAccountId(AccountSet.getAccountSetId());
                adjuvant.setLabel(typeEnum.getType());
                adjuvantList.add(adjuvant);
            }
            saveBatch(adjuvantList);

            adjuvantList = queryAllList();
        }
        if (CollectionUtil.isNotEmpty(adjuvantList)) {
            for (FinanceAdjuvant financeAdjuvant : adjuvantList) {
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("adjuvantName_resourceKey", "finance.adjuvant" + "." + financeAdjuvant.getAdjuvantName());
                financeAdjuvant.setLanguageKeyMap(keyMap);
            }
        }
        return adjuvantList;
    }

    /**
     * 查询所有自定义列表
     *
     * @return
     */
    @Override
    public List<FinanceAdjuvant> queryCustomList() {
        return lambdaQuery().eq(FinanceAdjuvant::getAdjuvantType, 0).eq(FinanceAdjuvant::getAccountId, AccountSet.getAccountSetId()).list();
    }

    /**
     * 删除辅助核算
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationLog deleteById(Long id) {
        //查询是否被科目关联，如果关联不能删除
        boolean exists = subjectAdjuvantService.lambdaQuery().eq(FinanceSubjectAdjuvant::getAdjuvantId, id).exists();
        if (exists) {
            throw new CrmException(FinanceCodeEnum.FINANCE_ADJUVANT_SUBJECT_DELETE_ERROR);
        }
        //判断是否有关联的明细，如果有就不能删除
        List<FinanceAdjuvantCarte> carteList = carteService.lambdaQuery().eq(FinanceAdjuvantCarte::getAdjuvantId, id).list();
        if (!carteList.isEmpty()) {
            throw new CrmException(FinanceCodeEnum.FINANCE_ADJUVANT_DELETE_ERROR);
        }

        FinanceAdjuvant byId = getById(id);
        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject(byId.getAdjuvantName());
        operationLog.setOperationInfo("删除辅助核算分类：" + byId.getAdjuvantName());

        removeById(id);
        return operationLog;
    }

    @Override
    public List<JSONObject> queryAdjuvantInfoBySubjectId(Long subjectId) {
        return baseMapper.queryAdjuvantInfoBySubjectId(subjectId);
    }
}
