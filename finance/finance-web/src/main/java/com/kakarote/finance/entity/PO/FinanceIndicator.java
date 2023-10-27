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
 * @author zjj
 * @ClassName FinanceIndicator.java
 * @Description 财务指标
 * @createTime 2021-10-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wk_finance_indicator")
@ApiModel(value = "FinanceIndicator 对象", description = "财务指标")
public class FinanceIndicator implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "配置名称")
    private String name;

    @ApiModelProperty(value = "行次")
    private Integer sort;

    @ApiModelProperty("公式")
    private String formula;

    @ApiModelProperty(value = "类型 1 资产负债表 2 利润表 3 现金流量表")
    private Integer type;

}
