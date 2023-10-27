package com.kakarote.finance.entity.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 现金流量表数据
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
@TableName("wk_finance_cash_flow_statement_report")
@ApiModel(value = "FinanceCashFlowStatementReport对象", description = "现金流量表数据")
public class FinanceCashFlowStatementReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("开始账期：格式 yyyyMM")
    private String fromPeriod;

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

    @ApiModelProperty(value = "月值")
    private BigDecimal monthValue = new BigDecimal(0);

    @ApiModelProperty(value = "年值")
    private BigDecimal yearValue = new BigDecimal(0);

    @ApiModelProperty("索引")
    private Integer sortIndex;

    @ApiModelProperty("现金流量表分类")
    private Integer category;

    @ApiModelProperty("创建日期")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("账套表")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("结束账期：格式 yyyyMM")
    private String toPeriod;

    @ApiModelProperty("类型 1 月度 2 季度")
    private Integer type;

    @ApiModelProperty("1 等级 第几级")
    private Integer grade;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "多语言key")
    @TableField(exist = false)
    private Map<String, String> languageKeyMap;

}