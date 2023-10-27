package com.kakarote.finance.service.impl;

import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.constant.FinanceTemplateEnum;
import com.kakarote.finance.entity.PO.FinanceAccountSet;
import com.kakarote.finance.service.IFinanceAccountSetService;
import com.kakarote.finance.service.IFinanceCommonService;
import com.kakarote.finance.service.IFinanceInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class FinanceCommonServiceImpl implements IFinanceCommonService {

    @Autowired
    private IFinanceAccountSetService accountSetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void init() {
        for (FinanceTemplateEnum nameEnum : FinanceTemplateEnum.values()) {
            if (!Objects.equals(FinanceTemplateEnum.NULL, nameEnum)) {
                IFinanceInitService initService = ApplicationContextHolder.getBean(nameEnum.getServiceName());
                initService.init();
            }
        }
    }

    @Override
    public void initBalanceSheet() {
        List<FinanceAccountSet> accountSetList = accountSetService.lambdaQuery().list();
        for (FinanceAccountSet accountSet : accountSetList) {
            try {
                AccountSet.setAccountSet(accountSet);
                FinanceBalanceSheetConfigServiceImpl balanceSheetConfigService = ApplicationContextHolder.getBean(FinanceBalanceSheetConfigServiceImpl.class);
                balanceSheetConfigService.init();
            } finally {
                AccountSet.remove();
            }
        }
    }
}
