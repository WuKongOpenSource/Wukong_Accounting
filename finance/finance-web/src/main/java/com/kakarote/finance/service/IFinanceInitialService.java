package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceAddInitialBO;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceInitialPageBO;
import com.kakarote.finance.entity.PO.FinanceInitial;
import com.kakarote.finance.entity.PO.FinanceInitialAssist;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 财务初始余额 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface IFinanceInitialService extends BaseService<FinanceInitial> {


    /**
     * 添加辅助核算初始余额
     *
     * @param bos
     */
    void addInitial(List<FinanceAddInitialBO> bos);

    /**
     * 更新期初余额
     *
     * @param initials
     */
    void updateInitialValue(List<FinanceInitial> initials);

    /**
     * 查询财务初始余额列表
     */
    public List<JSONObject> queryListBySubjectType(Integer subjectType);

    /**
     * 查询财务初始余额列表
     */
    public BasePage<JSONObject> queryListBySubjectType(FinanceInitialPageBO bo);

    /**
     * 试算平衡
     */
    public JSONObject queryTrialBalance();

    /**
     * 删除辅助核算
     */
    public void deleteById(Long initialId);

    /**
     * 删除辅助核算
     */
    public void deleteByAssistId(Long assistId);

    /**
     * 根据科目id，辅助核算，查询余额
     */
    public FinanceInitial queryByAdjuvant(FinanceInitialAssist assist);

    /**
     * 获取账套的所有初始余额
     *
     * @param accountId
     * @return
     */
    List<FinanceInitial> queryAll(Long accountId);

    /**
     * 获取账套的所有初始余额,返回辅助核算ID
     *
     * @param accountId
     * @return
     */
    List<JSONObject> queryAllWithAssistId(Long accountId);

    /**
     * 获取账套的核算初始余额,
     *
     * @return
     */
    List<JSONObject> queryAdjuvantInitial(FinanceDetailAccountBO accountBO);

    /**
     * 初始余额列表转科目 id和 entity map
     *
     * @param initials
     * @return
     */
    Map<Long, FinanceInitial> listToMap(List<FinanceInitial> initials);
}
