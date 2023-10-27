package com.kakarote.finance.service;

import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceAccountAuthSaveBO;
import com.kakarote.finance.entity.BO.FinanceAccountSetBO;
import com.kakarote.finance.entity.BO.FinanceNewAccountSetBO;
import com.kakarote.finance.entity.PO.AdminRole;
import com.kakarote.finance.entity.PO.FinanceAccountSet;
import com.kakarote.finance.entity.VO.FinanceAccountListVO;
import com.kakarote.finance.entity.VO.FinanceAccountVO;

import java.util.List;

/**
 * <p>
 * 账套表 服务类
 * </p>
 *
 * @author dsc
 * @since 2021-08-28
 */
public interface IFinanceAccountSetService extends BaseService<FinanceAccountSet> {

    List<FinanceAccountSet> queryPageList();

    FinanceAccountSet getAccountSetById(Long accountId);

    OperationLog saveAndUpdate(FinanceAccountSet accountSet);

    FinanceAccountVO getUserByAccountId(Long accountId);

    List<OperationLog> saveAccountAuth(FinanceAccountAuthSaveBO authSaveBO);

    void deleteAccountUser(FinanceAccountSetBO accountSetBO);

    void saveAccountSet(FinanceNewAccountSetBO accountSet);

    List<FinanceAccountListVO> getAccountSetList();

    void switchAccountSet(Long accountId);

    List<AdminRole> getFinanceRoleByType(Integer type);

    void initFinanceData();

}
