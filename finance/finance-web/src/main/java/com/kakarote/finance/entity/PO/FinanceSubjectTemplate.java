package com.kakarote.finance.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description:科目模板
 * @author: zjj
 * @date: 2021-08-31 10:08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wk_finance_subject_template")
@ApiModel(value = "FinanceSubjectTemplate", description = "科目模板")
public class FinanceSubjectTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "科目编号")
    private String number;

    @ApiModelProperty(value = "科目名称")
    private String subjectName;

    @ApiModelProperty(value = "上级科目")
    private Long parentId;

    @ApiModelProperty(value = "科目类型 1.资产 2.负债 3.权益 4.成本 5.损益")
    private Integer type;

    @ApiModelProperty(value = "科目类别 根据类型改变 type为1时 1.流动资产 2.非流动资产 type为2时 1.流动负债 2.非流动负债 type为3 1.所有者权益 type为4 成本 type为5 1.营业收入 2.其他收益 3.期间费用 4.其他损失 5.营业成本及税金 6.以前年度损益调整 7.所得税")
    private Integer category;

    @ApiModelProperty(value = "余额方向 1.借 2.贷4")
    private Integer balanceDirection;

    @ApiModelProperty(value = "数量核算 计量单位")
    private String amountUnit;

    @ApiModelProperty(value = "是否现金科目 1.是 0.否")
    private Integer isCash;

    @ApiModelProperty(value = "1.正常启用 2.正常禁用 3.删除")
    private Integer status;

    @ApiModelProperty(value = "等级，第几级")
    private Integer grade;

    @ApiModelProperty(value = "是否开启核算")
    private Integer isAmount;
}
