package com.kakarote.finance.entity.PO;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 科目
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
@TableName("wk_finance_subject")
@ApiModel(value = "FinanceSubject对象", description = "科目")
public class FinanceSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "subject_id", type = IdType.ASSIGN_ID)
    private Long subjectId;

    @ApiModelProperty("科目编号")
    private String number;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("上级科目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @ApiModelProperty("科目类型 1.资产 2.负债 3.权益 4.成本 5.损益6.共同")
    private Integer type;

    @ApiModelProperty("科目类别 根据类型改变 type为1时 1.流动资产 2.非流动资产 type为2时 1.流动负债 2.非流动负债 type为3 1.所有者权益 type为4 成本 type为5 1.营业收入 2.其他收益 3.期间费用 4.其他损失 5.营业成本及税金 6.以前年度损益调整 7.所得税 type为6 时  1.共同")
    private Integer category;

    @ApiModelProperty("余额方向 1.借 2.贷")
    private Integer balanceDirection;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("数量核算 计量单位")
    private String amountUnit;

    @ApiModelProperty("是否现金科目 1.是 0.否")
    private Integer isCash;

    @ApiModelProperty("1.正常启用 2.正常禁用 3.删除")
    private Integer status;

    @ApiModelProperty("等级，第几级")
    private Integer grade;

    @ApiModelProperty("是否开启核算 1.开启 0.不开启")
    private Integer isAmount;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "语言包map")
    private Map<String, String> languageKeyMap;


    @TableField(exist = false)
    @ApiModelProperty(value = "子科目")
    private List<FinanceSubject> children = new ArrayList<>();

    public static List<FinanceSubject> listToTree(List<FinanceSubject> list) {
        //递归查找子结点
        return list.stream()
                .filter(o -> (ObjectUtil.isNull(o.getParentId()) || ObjectUtil.equal(0L, o.getParentId())))
                .map(tree -> findChildren(tree, list))
                .collect(Collectors.toList());
    }

    private static FinanceSubject findChildren(FinanceSubject tree, List<FinanceSubject> list) {
        list.stream()
                .filter(node -> ObjectUtil.equal(tree.getSubjectId(), node.getParentId()))
                .map(node -> findChildren(node, list))
                .forEachOrdered(children -> tree.getChildren().add(children));
        return tree;
    }

}
