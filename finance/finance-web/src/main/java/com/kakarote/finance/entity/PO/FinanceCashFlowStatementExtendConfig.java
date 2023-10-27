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

/**
 * <p>
 * 现金流量扩展表
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
@TableName("wk_finance_cash_flow_statement_extend_config")
@ApiModel(value = "FinanceCashFlowStatementExtendConfig对象", description = "现金流量扩展表")
public class FinanceCashFlowStatementExtendConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("配置名称")
    private String name;

    @ApiModelProperty("行次")
    private Integer sort;

    @ApiModelProperty("公式")
    private String formula;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("表格标识")
    private Integer category;

    @ApiModelProperty("创建日期")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("类型：0 支持用户手动配置 1 固定公式")
    private Integer type;

    @ApiModelProperty(value = "月值")
    private BigDecimal monthValue = new BigDecimal(0);

    @ApiModelProperty(value = "年值")
    private BigDecimal yearValue = new BigDecimal(0);

    @ApiModelProperty("可编辑")
    private Boolean editable;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("索引")
    private Integer sortIndex;

    @ApiModelProperty("等级")
    private Integer grade;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
