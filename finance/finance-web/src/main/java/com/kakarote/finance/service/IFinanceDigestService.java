package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceDigest;

import java.util.List;

/**
 * <p>
 * 凭证摘要 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface IFinanceDigestService extends BaseService<FinanceDigest> {

    /**
     * 新建或编辑摘要
     */
    public void saveAndUpdate(FinanceDigest digest);

    /**
     * 列表查询摘要
     */
    public List<FinanceDigest> queryList();

    /**
     * 删除摘要
     */
    public void deleteById(Long id);

}
