package com.kakarote.finance.entity.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 资产负债表报表数据
 * </p>
 *
 * @author dsc
 * @since 2021-12-22
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("wk_finance_balance_sheet_report")
@ApiModel(value = "FinanceBalanceSheetReport对象", description = "资产负债表报表数据")
public class FinanceBalanceSheetReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("开始账期：格式 yyyyMM")
    private String fromPeriod;

    @ApiModelProperty("结束账期：格式 yyyyMM")
    private String toPeriod;

    @ApiModelProperty("类型 1 月度 2 季度")
    private Integer type;

    @ApiModelProperty("1 等级 第几级")
    private Integer grade;

    @ApiModelProperty("配置名称")
    private String name;

    @ApiModelProperty("行次")
    private Integer sort;

    @ApiModelProperty("公式")
    private String formula;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("可编辑")
    private Boolean editable;

    @ApiModelProperty("索引")
    private Integer sortIndex;

    @ApiModelProperty("年初数")
    private BigDecimal initialPeriod;

    @ApiModelProperty("期末数")
    private BigDecimal endPeriod;

    @TableField(exist = false)
    @ApiModelProperty("期初")
    private BigDecimal startPeriod;

    @ApiModelProperty("创建日期")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("是否结账")
    private Boolean settled;

    @ApiModelProperty("行ID")
    private Integer rowId;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "多语言key")
    @TableField(exist = false)
    private Map<String, String> languageKeyMap;
}
