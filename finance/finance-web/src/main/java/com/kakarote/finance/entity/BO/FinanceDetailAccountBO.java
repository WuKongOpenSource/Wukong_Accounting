package com.kakarote.finance.entity.BO;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: FinanceDetailAccountBO
 * @Author: Blue
 * @Description: FinanceDetailAccountBO
 * @Date: 2021/8/26 9:49
 */
@Data
@ToString
@ApiModel("明细账查询对象")
public class FinanceDetailAccountBO {

    @ApiModelProperty(value = "会计期间开始时间")
    private String startTime;

    @ApiModelProperty(value = "会计期间结束时间")
    private String endTime;

    @ApiModelProperty(value = "科目id")
    private Long subjectId;

    @ApiModelProperty(value = "开始科目id")
    private Long startSubjectId;

    @ApiModelProperty(value = "结束科目id")
    private Long endSubjectId;

    @ApiModelProperty(value = "开始科目编号")
    private Integer startNumber;

    @ApiModelProperty(value = "结束科目编号")
    private Integer endNumber;


    @ApiModelProperty(value = "小凭证号")
    private Integer minCertificateNum;

    @ApiModelProperty(value = "大凭证号")
    private Integer maxCertificateNum;

    @ApiModelProperty(value = "小科目级次")
    private Integer minLevel;

    @ApiModelProperty(value = "大科目级次")
    private Integer maxLevel;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;

    @ApiModelProperty(value = "余额方向 1.借 2.贷")
    private Integer balanceDirection;

    @ApiModelProperty(value = "开启数量查询 1.开启")
    private Integer isQuantity;

    private Integer isFlat;

    private Integer accountBookDirection;

    private List<Long> subjectIds = new ArrayList<>();

    private List<Integer> timeList;

    private List<Long> debtorSubjectIds;

    private List<Long> detailIds;

    @ApiModelProperty(value = "标签 1 客户 2 供应商 3 职员 4 项目 5 部门 6 存货 7 自定义", required = true)
    private Integer label;

    @ApiModelProperty(value = "辅助核算关联类型id")
    private Long relationId;

    private List<Long> relationIdList = new ArrayList<>();

    @ApiModelProperty(value = "辅助核算id")
    private Long adjuvantId;

    private List<JSONObject> searchSubject;

    @ApiModelProperty(value = "明细表 余额方向与父级一致")
    private Integer isBalanceDirection;

    @ApiModelProperty(value = "科目表，查询科目余额")
    private Integer isSubject;

    private List<Long> assistIds;

    private Integer degree;

    private Integer rule;

    @ApiModelProperty(value = "表头(导出用)")
    private List<Map<String, Object>> headList;

    @ApiModelProperty(value = "是否展开 0 不展开 1 展开")
    private Integer isLaunch;

}
