package com.kakarote.finance.entity.BO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.kakarote.finance.entity.PO.FinanceStatementSubject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * @ClassName: FinanceStatementSaveBO
 * @Author: Blue
 * @Description: FinanceStatementSaveBO
 * @Date: 2021/8/27 15:09
 */
@Data
@ToString
@ApiModel("结账保存BO")
public class FinanceStatementSaveBO {

    @ApiModelProperty(value = "结账id")
    private Long statementId;

    @ApiModelProperty(value = "结账名称")
    private String statementName;

    @ApiModelProperty(value = "是否期末转结 1.是 0.否")
    private Integer isEndOver;

    @ApiModelProperty(value = "转结科目id")
    private Long subjectId;

    @ApiModelProperty(value = "取数规则 1.余额2.借方余额3.贷方余额 4.借方发生额5.贷方发生额6.损益发生额")
    private Integer gainRule;

    @ApiModelProperty(value = "时间类型 1.期末 2.期初 3.年初")
    private Integer timeType;

    @ApiModelProperty(value = "凭证字")
    private Long voucherId;

    @ApiModelProperty(value = "凭证摘要")
    private String digestContent;

    @ApiModelProperty(value = "凭证分类 1收益和损失分开结转 （分别生成收益凭证和损失凭证） 2.收益和损失同时结转")
    private Integer voucherType;

    @ApiModelProperty(value = "“以前年度损益调整”科目")
    private Long adjustSubjectId;

    @ApiModelProperty(value = "“以前年度损益调整”科目的结转科目")
    private Long endSubjectId;

    @ApiModelProperty(value = "其他损益科目的结转科目")
    private Long restSubjectId;

    @ApiModelProperty(value = "结转方式：按余额反向结转 1.是 0.否")
    private Integer endWay;

    @ApiModelProperty(value = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUserId;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "结转损益日期，最大31，超过31默认最后一天")
    private Integer endTimeDays;

    @ApiModelProperty(value = "结账类型 1.普通结账 2.结账损益 3.转出未交增值税 4.计提地税 5.计提所得税")
    private Integer statementType;

    @ApiModelProperty(value = "结账与科目关联信息表")
    private List<FinanceStatementSubject> subjectList;
}
