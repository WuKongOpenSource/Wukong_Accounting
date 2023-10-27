package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceStatementCertificateBO;
import com.kakarote.finance.entity.BO.FinanceStatementSaveBO;
import com.kakarote.finance.entity.PO.FinanceStatement;
import com.kakarote.finance.entity.VO.FinanceStatementVO;

import java.util.List;

/**
 * <p>
 * 结账表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
public interface IFinanceStatementService extends BaseService<FinanceStatement> {

    /**
     * 新建或编辑结账
     */
    public OperationLog saveAndUpdate(FinanceStatementSaveBO statementSaveBO);

    /**
     * 列表查询结账
     */
    public FinanceStatementVO queryStatement();

    /**
     * 结账生成凭证
     *
     * @return
     */
    public List<OperationLog> statementCertificate(FinanceStatementCertificateBO statementCertificateBO);

    /**
     * 结账反结账
     */
    public void statement(FinanceStatementCertificateBO bo);

    /**
     * 根据账期查询损益类科目凭证详情
     *
     * @param period
     * @return
     */
    List<JSONObject> queryLossDetailByPeriod(String period);

}
