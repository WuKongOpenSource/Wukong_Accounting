package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceCollectCertificateBO;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceExportBO;
import com.kakarote.finance.entity.BO.FinanceSubjectBO;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.entity.VO.FinanceSubjectIdsVO;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 科目 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface IFinanceSubjectService extends BaseService<FinanceSubject> {
    /**
     * 新建编辑科目
     */
    public OperationLog saveAndUpdate(FinanceSubjectBO subjectBO);

    /**
     * 列表查询科目
     */
    public List<FinanceSubjectVO> queryListByType(Integer type, Integer isTree, Integer returnType, String certificateTime);


    /**
     * 查询科目列表页-返回科目的辅助核算信息
     *
     * @return
     */
    List<FinanceSubjectVO> queryList();

    /**
     * 批量删除科目
     *
     * @return
     */
    public List<OperationLog> deleteByIds(List<Long> ids);

    /**
     * 批量启用禁用科目
     *
     * @return
     */
    public List<OperationLog> updateStatus(List<Long> ids, Integer status);

    /**
     * 汇总凭证计算
     */
    public List<JSONObject> queryListCollect(FinanceCollectCertificateBO certificateBO);

    /**
     * 根据科目id和级次，获取该条件下所有科目id
     */
    public List<Long> queryIds(Long id, Integer minLevel, Integer maxLevel);


    /**
     * 获取该条件下所有科目id
     */
    public List<JSONObject> queryIds(FinanceDetailAccountBO accountBO);


    /**
     * 根据科目编号，获取科目id
     */
    public FinanceSubjectIdsVO queryIdsByNumber(String number);

    /**
     * 根据科目编号，获取科目id
     */
    public FinanceSubjectIdsVO queryIdsById(Long id);

    /**
     * 获取所有符合条件的损益科目id
     */
    public FinanceSubjectIdsVO queryIdsByType(String certificateTime);

    /**
     * 下载导入模板
     *
     * @param response resp
     * @throws IOException ex
     */
    public void downloadExcel(HttpServletResponse response) throws IOException;

    /**
     * excel导入凭证
     *
     * @param file file
     */
    public JSONObject excelImport(MultipartFile file);

    List<Integer> getAdjuvantList(Long subjectId);

    /**
     * 获取符合条件数量科目id
     */
    List<Long> queryIds();


    /**
     * 查询所有未删除的科目
     *
     * @return
     */
    List<FinanceSubject> queryAll();

    /**
     * 获取指定科目类型的所有第一级科目
     *
     * @param type
     * @return
     */
    List<FinanceSubject> queryFirstGradeByType(Integer type);

    /**
     * 导出科目
     *
     * @param exportBO 科目导出对象
     */
    public void exportListByType(FinanceExportBO exportBO);

}
