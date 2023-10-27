package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceAdjuvant;

import java.util.List;

/**
 * <p>
 * 辅助核算表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface IFinanceAdjuvantService extends BaseService<FinanceAdjuvant> {

    /**
     * 新建编辑辅助核算
     */

    public OperationLog saveAndUpdate(FinanceAdjuvant adjuvant);

    /**
     * 列表查询辅助核算
     */
    public List<FinanceAdjuvant> queryAllList();

    /**
     * 查询左右固定辅助核算
     */
    public List<FinanceAdjuvant> queryCustomList();

    /**
     * 删除辅助核算
     *
     * @return
     */
    public OperationLog deleteById(Long id);

    /**
     * 获取科目的辅助核算信息
     *
     * @param subjectId
     * @return
     */
    List<JSONObject> queryAdjuvantInfoBySubjectId(Long subjectId);

}
