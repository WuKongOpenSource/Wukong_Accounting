package com.kakarote.finance.common;

import com.kakarote.finance.entity.PO.FinanceAccountSet;

public class AccountSet {

    private static final ThreadLocal<FinanceAccountSet> accountSetThreadLocal = new ThreadLocal<>();


    public static FinanceAccountSet getAccountSet() {
        return accountSetThreadLocal.get();
    }

    public static Long getAccountSetId() {
        if (accountSetThreadLocal.get() != null) {
            return accountSetThreadLocal.get().getAccountId();
        } else {
            return -1L;
        }
    }


    public static void setAccountSet(FinanceAccountSet accountSet) {
        accountSetThreadLocal.set(accountSet);
    }

    public static void remove() {
        accountSetThreadLocal.remove();
    }
}
