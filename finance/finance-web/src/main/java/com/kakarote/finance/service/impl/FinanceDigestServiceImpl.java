package com.kakarote.finance.service.impl;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceDigest;
import com.kakarote.finance.mapper.FinanceDigestMapper;
import com.kakarote.finance.service.IFinanceDigestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 凭证摘要 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@Service
public class FinanceDigestServiceImpl extends BaseServiceImpl<FinanceDigestMapper, FinanceDigest> implements IFinanceDigestService {

    @Override
    public void saveAndUpdate(FinanceDigest digest) {
        if (digest.getDigestId() == null) {
            digest.setCreateTime(LocalDateTime.now());
            digest.setCreateUserId(UserUtil.getUserId());
            digest.setAccountId(AccountSet.getAccountSetId());
            save(digest);
        } else {
            updateById(digest);
        }
    }

    @Override
    public List<FinanceDigest> queryList() {
        return lambdaQuery().eq(FinanceDigest::getAccountId, AccountSet.getAccountSetId()).list();
    }

    @Override
    public void deleteById(Long id) {
        removeById(id);
    }
}
