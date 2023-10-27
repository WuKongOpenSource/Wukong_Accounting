package com.kakarote.finance.constant;

import com.kakarote.core.common.ResultCode;

/**
 * @ClassName: FinanceCodeEnum
 * @Author: Blue
 * @Description: FinanceCodeEnum
 * @Date: 2021/8/24 11:37
 */
public enum FinanceCodeEnum implements ResultCode {

    /**
     * 财务模块错误码
     */
    FINANCE_ADJUVANT_UPDATE_TYPE_ERROR(7001, "固定辅助核算，不能删除"),
    FINANCE_CERTIFICATE_VOUCHER_REPEATING_ERROR(7002, "当前日期凭证字与凭证号重复，请重新填写"),
    FINANCE_TYPE_ERROR(7003, "当前数据被引用，不能删除"),
    FINANCE_ADJUVANT_DELETE_ERROR(7004, "删除失败！待删除的核算含有明细项目，无法删除，请先删除明细项!"),
    FINANCE_ASSOCIATION_DELETE_ERROR(7005, "删除失败！当前数据有参与财务核算!"),
    FINANCE_STATEMENT_ERROR(7006, "没有结转损益，不能进行结账!"),
    FINANCE_SUBJECT_ERROR(7007, "科目名称重复，不能添加!"),
    FINANCE_CERTIFICATE_DELETE_ERROR(7008, "删除失败,凭证已审核，不允许删除已审核凭证！"),
    FINANCE_CERTIFICATE_STATUS_ERROR(7009, "凭证审核后才允许结账！"),
    FINANCE_SUBJECT_NUMBER_ERROR(7010, "科目编码重复，不能添加!"),
    FINANCE_CERTIFICATE_UPDATE_STATUS_ERROR(7011, "凭证借贷方不平衡，不能进行审核"),
    FINANCE_INITIAL_DELETE_ERROR(7012, "不为辅助核算，不能进行删除！"),
    FINANCE_SUBJECT_DELETE_ERROR(7013, "科目已被余额表使用，不能删除"),
    FINANCE_VOUCHER_DELETE_ERROR(7014, "该凭证字关联的有凭证，不能删除"),
    FINANCE_USED_VOUCHER_EDIT_ERROR(7015, "使用中的凭证字，不能编辑"),
    FINANCE_ADJUVANT_CARTE_NUM_OR_NAME_NULL_ERROR(7016, "辅助核算类型编码或者名字不能为空"),
    FINANCE_ADJUVANT_CARTE_NUM_EXIST_ERROR(7017, "编码已存在"),
    FINANCE_ADJUVANT_SUBJECT_DELETE_ERROR(7018, "删除失败！待删除的核算有关联科目"),
    FINANCE_SUBJECT_NOT_FOUND_ERROR(7019, "科目未找到!"),
    FINANCE_STATEMENT_NOT_BALANCE(7020, "收支不平衡，不能结账!"),
    FINANCE_STATEMENT_IS_FRACTURE(7021, "凭证存在断号，不能进行结账，请整理凭证后再结账!"),
    FINANCE_PROFIT_NOT_BALANCE(7022, "利润表不平衡，不能进行结账!"),
    FINANCE_SHEET_NOT_BALANCE(7023, "资产负债表不平衡，不能进行结账!"),
    FINANCE_STATEMENT_INITIAL_NOT_BALANCE(7024, "财务初始不平衡，不能进行结账!"),
    FINANCE_CERTIFICATE_TIME_ERROR(7025, "该期已结账，不能进行添加凭证！"),
    FINANCE_SUBJECT_TYPE_ERROR(7026, "科目与上级科目，科目类别或科目类型不一致，不能添加!"),
    FINANCE_PRINT_TEMPLATE_NOT_EXIST_ERROR(7027, "使用的打印模板不能为空"),
    FINANCE_PRINT_PRE_VIEW_ERROR(2045, "仅支持pdf和word格式预览"),
    FINANCE_INCOME_STATEMENT_NOT_EXIST_ERROR(7028, "利润表不存在"),
    FINANCE_IMPORT_EXCEL_ERROR(7029, "请使用最新的导入模板"),
    FINANCE_IMPORT_EXCEL_DOWN_ERROR(7030, "已结账不允许导入"),
    ;

    FinanceCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private int code;
    private String msg;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
