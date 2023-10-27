package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.entity.PO.FinanceCertificate;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.entity.VO.FinanceCertificateVO;
import com.kakarote.finance.entity.VO.FinanceDiversificationVO;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 凭证表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface IFinanceCertificateService extends BaseService<FinanceCertificate> {

    /**
     * 新建或编辑凭证表
     */
    public FinanceCertificate saveAndUpdate(FinanceCertificateBO financeCertificateBO);


    /**
     * 分页查询凭证表
     */
    public BasePage<FinanceCertificateVO> queryPage(FinanceSearchCertificateBO searchCertificateBO);

    /**
     * 审核凭证
     */
    public List<OperationLog> updateCheckStatusByIds(List<Long> ids, Integer status);

    /**
     * 删除凭证
     *
     * @return
     */
    public List<OperationLog> deleteByIds(List<Long> ids);

    /**
     * 凭证汇总
     */
    public List<JSONObject> queryListCollect(FinanceCollectCertificateBO certificateBO);

    /**
     * 查询凭证详情
     */
    public FinanceCertificateVO queryById(Long certificateId);

    /**
     * 查询明细账
     */
    List<JSONObject> queryDetailAccount(FinanceDetailAccountBO accountBO);

    /**
     * 查询总账
     */
    public List<JSONObject> queryDetailUpAccount(FinanceDetailAccountBO accountBO);

    /**
     * 科目余额表
     *
     * @param queryBO 请求
     * @return data
     */
    List<JSONObject> querySubjectBalance(FinanceSubjectBalanceQueryBO queryBO);

    /**
     * 查询科目余额表
     */
    public List<JSONObject> queryOldDetailBalanceAccount(FinanceDetailAccountBO accountBO);

    /**
     * 查询多栏账
     */
    public FinanceDiversificationVO queryDiversification(FinanceDetailAccountBO accountBO);

    /**
     * 根据科目ids，查询科目余额信息
     */
    public List<JSONObject> queryListDetailBalanceAccount(FinanceDetailAccountBO accountBO);

    /**
     * 导入凭证
     *
     * @param file file
     */
    JSONObject voucherImport(MultipartFile file) throws IOException, IllegalAccessException;

    /**
     * 下载凭证导入模板
     *
     * @param response response
     */
    void downloadVoucherImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 根据凭证字，凭证时间，返回凭证号
     */
    public JSONObject queryNumByTime(FinanceCertificateBO financeCertificateBO);

    /**
     * 凭证插入
     */
    public void insertCertificate(FinanceCertificateInsertBO insertBO);


    /**
     * 获取当前账套最早录凭证日期和最晚录凭证日期
     */
    public JSONObject queryCertificateTime();

    /**
     * 凭证整理
     */
    public void certificateSettle(FinanceCertificateSettleBO settleBO);

    /**
     * 获取当前顺序凭证号
     */
    public JSONObject queryCertificateNum(FinanceCertificateSettleBO settleBO);

    /**
     * 查询科目余额
     *
     * @param accountId
     * @param fromPeriod
     * @param toPeriod
     * @return
     */
    List<JSONObject> querySubjectBalance(Long accountId, String fromPeriod, String toPeriod);

    /**
     * 数量金额明细账
     */
    public List<JSONObject> queryAmountDetailAccount(FinanceDetailAccountBO accountBO);


    /**
     * 数量总账
     */
    public List<JSONObject> queryAmountDetailUpAccount(FinanceDetailAccountBO accountBO);

    /**
     * 核算项目余额表
     *
     * @param accountBO
     * @return
     */
    public List<JSONObject> queryItemsDetailBalanceAccount(FinanceDetailAccountBO accountBO);

    /**
     * 核算获取辅助项目名称
     *
     * @param adjuvantId 辅助核算id
     * @return 辅助核算名称
     */
    public List<JSONObject> queryLabelName(Long adjuvantId);

    /**
     * 核算项目明细账
     *
     * @param accountBO 查询条件
     */
    public List<JSONObject> queryItemsDetailAccount(FinanceDetailAccountBO accountBO);

    /**
     * 核算获取存在凭证的辅助项目名称
     *
     * @param association 辅助核算id
     * @return 辅助核算名称
     */
    public List<JSONObject> queryLabelNameByData(FinanceCertificateAssociationBO association);


    /**
     * 项目明细账树形结构
     *
     * @param startTime 当前期数
     * @return data
     */
    public List<FinanceSubjectVO> itemsDetailTree(String startTime);

    /**
     * 导出凭证
     *
     * @param searchCertificateBO
     */
    public void exportCertificate(FinanceSearchCertificateBO searchCertificateBO);

    /**
     * 导出凭证汇总
     *
     * @param searchCertificateBO
     */
    public void exportListByType(FinanceCollectCertificateBO searchCertificateBO);

    /**
     * 导出明细账
     *
     * @param accountBO
     */
    public void exportDetailAccount(FinanceDetailAccountBO accountBO);

    /**
     * 导出总账
     *
     * @param accountBO
     */
    public void exportDetailUpAccount(FinanceDetailAccountBO accountBO);

    /**
     * 导出科目余额表
     *
     * @param queryBO
     */
    public void exportDetailBalanceAccount(FinanceSubjectBalanceQueryBO queryBO);

    /**
     * 导出多栏账
     *
     * @param accountBO
     */
    public void exportDiversification(FinanceDetailAccountBO accountBO);

    /**
     * 导出核算项目明细账
     *
     * @param accountBO
     */
    public void exportItemsDetailAccount(FinanceDetailAccountBO accountBO);

    /**
     * 导出核算项目余额表
     *
     * @param accountBO
     */
    public void exportItemsDetailBalanceAccount(FinanceDetailAccountBO accountBO);

    /**
     * 导出数量金额明细账
     *
     * @param accountBO
     */
    public void exportAmountDetailAccount(FinanceDetailAccountBO accountBO);

    /**
     * 导出数量总账
     *
     * @param accountBO
     */
    public void exportAmountDetailUpAccount(FinanceDetailAccountBO accountBO);

    /**
     * 导出财务初始余额
     *
     * @param accountBO accountBO
     */
    public void exportPageBySubjectType(FinanceInitialPageBO accountBO);

    /**
     * 下载财务初始余额模板
     *
     * @param accountBO accountBO
     */
    public void downloadFinanceInitialExcel(FinanceInitialPageBO accountBO);

    /**
     * 导入财务初始余额模板
     *
     * @param file file
     * @return 导入size
     */
    public Integer financeInitialImport(MultipartFile file);


    /**
     * 格式化财务导出日期
     *
     * @param accountBO
     * @return
     */
    public String formatFinanceStartTime(FinanceDetailAccountBO accountBO);

    /**
     * 财务凭证预览
     *
     * @param propertiesBO 打印参数
     * @return path
     */
    public String preview(CrmPrintPropertiesBO propertiesBO);

    /**
     * 财务凭证预览
     *
     * @param content content
     * @param type    type
     * @return path
     */
    public String previewFinance(String content, String type, String landscape, String pageSize);

    /**
     * 移动财务凭证信息，用于当科目已录入凭证后又添加辅助核算，需要将原凭证移动到辅助核算下
     *
     * @param subject        当前科目信息
     * @param associationMap 辅助核算信息
     */
    public void moveFinanceCertificate(FinanceSubject subject, Map<Long, JSONObject> associationMap);

    /**
     * 检查科目是否已经录入凭证
     *
     * @param subjectId 科目id
     * @return true 已录入 false 未录入
     */
    public boolean checkSubjectCertificate(Long subjectId);

}
