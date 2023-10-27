package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceAdjuvantCarteBO;
import com.kakarote.finance.entity.PO.FinanceAdjuvantCarte;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface IFinanceAdjuvantCarteService extends BaseService<FinanceAdjuvantCarte> {

    /**
     * 新建编辑辅助核算关联信息
     *
     * @return
     */

    public OperationLog saveAndUpdate(FinanceAdjuvantCarte adjuvantCarte);

    /**
     * 根据辅助核算查询关联信息
     */
    public BasePage<FinanceAdjuvantCarte> queryByAdjuvantId(FinanceAdjuvantCarteBO carteBO);

    /**
     * 启用禁用
     *
     * @return
     */
    public OperationLog updateStatusById(FinanceAdjuvantCarte adjuvantCarte);

    /**
     * 删除
     *
     * @return
     */
    public List<OperationLog> deleteById(List<Long> ids);

    /**
     * 导出模板
     *
     * @param response response
     */
    public void downloadExcel(HttpServletResponse response, Integer label);

    /**
     * excel导入辅助核算
     *
     * @param file file
     */
    public JSONObject excelImport(MultipartFile file, Integer label, Long adjuvantId);

    /**
     * crm数据同步
     */
    public void synchronizeData(List<FinanceAdjuvantCarteBO> carteBOS);

    /**
     * 导出数据
     *
     * @param carteBO
     * @param response
     */
    public void exportExcel(FinanceAdjuvantCarteBO carteBO, HttpServletResponse response);

    /**
     * 获取辅助核算名称
     *
     * @param id
     * @return
     */
    String getName(Long id);

}
