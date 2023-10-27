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
import java.util.Date;

/**
 * @description: 财务系统报表配置模板
 * @author: zjj
 * @date: 2021-08-31 09:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wk_finance_report_template")
@ApiModel(value = "FinanceReportTemplate", description = "财务系统报表配置模板")
public class FinanceReportTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "配置名称")
    private String name;

    @ApiModelProperty(value = "行次")
    private Integer sort;

    @ApiModelProperty(value = "公式")
    private String formula;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "等级")
    private Integer grade;

    @ApiModelProperty(value = "类型 1 资产负债表 2 利润表 3 现金流量表 4 扩展表")
    private Integer type;

    @ApiModelProperty(value = "现金流量表分类")
    private Integer category;

    @ApiModelProperty(value = "索引")
    private Integer sortIndex;

    @ApiModelProperty(value = "可编辑")
    private boolean editable;

    private Date createTime;

    @ApiModelProperty(value = "行ID")
    private Integer rowId;
}
