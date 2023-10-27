package com.kakarote.finance.mapper;

import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.BO.FinanceAccountSetBO;
import com.kakarote.finance.entity.PO.FinanceAccountSet;
import com.kakarote.finance.entity.VO.FinanceAccountListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 账套表 Mapper 接口
 * </p>
 *
 * @author dsc
 * @since 2021-08-28
 */
public interface FinanceAccountSetMapper extends BaseMapper<FinanceAccountSet> {

    List<FinanceAccountSetBO> getUserIdList(@Param("accountId") Long accountId);

    List<FinanceAccountListVO> getAccountSetList(@Param("userId") Long userId);

    String getSettleTime(@Param("accountId") Long accountId);

    List<Long> getRoleIdList(@Param("accountId") Long accountId, @Param("userId") Long userId);

    void removeAllData(@Param("tableName") String tableName);
}
