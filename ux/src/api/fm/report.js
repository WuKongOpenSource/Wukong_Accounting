import request from '@/utils/request'

/**
 * 资产负债列表查询
 * @param {*} data
 */
export function balanceSheetReportAPI(data) {
  return request({
    url: '/financeReport/balanceSheetReport',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 资产负债表平衡检查
 * @param {*} data
 */
export function balanceCheckAPI(data) {
  return request({
    url: '/financeReport/balanceSheetReport/balanceCheck',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 编辑资产负债表公式
 * @param {*} data
 */
export function updateBalanceFormulaAPI(data) {
  return request({
    url: '/financeReport/balanceSheetConfig/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 利润列表查询
 * @param {*} data
 */
export function incomeStatementReportAPI(data) {
  return request({
    url: '/financeReport/incomeStatementReport',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 利润表平衡检查
 * @param {*} data
 */
export function incomeCheckAPI(data) {
  return request({
    url: '/financeReport/incomeStatementReport/balanceCheck',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 编辑利润表公式
 * @param {*} data
 */
export function updateIncomeFormulaAPI(data) {
  return request({
    url: '/financeReport/incomeStatementConfig/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 现金流量列表查询
 * @param {*} data
 */
export function cashFlowStatementReportAPI(data) {
  return request({
    url: '/financeReport/cashFlowStatementReport',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 现金流量表平衡检查
 * @param {*} data
 */
export function cashFlowCheckAPI(data) {
  return request({
    url: '/financeReport/cashFlowStatementReport/balanceCheck',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 编辑 现金流量表
 * @param {*} data
 */
export function updateCashFlowReportAPI(data) {
  return request({
    url: '/financeReport/cashFlowStatementReport/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 调整辅助数据项列表查询
 * @param {*} data
 */
export function cashFlowStatementExtendAPI(data) {
  return request({
    url: '/financeReport/cashFlowStatementExtend/list',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 编辑辅助数据列表
 * @param {*} data
 */
export function updateCashFlowExtendAPI(data) {
  return request({
    url: '/financeReport/cashFlowStatementExtend/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 编辑扩展数据公式
 * @param {*} data
 */
export function updatecashFlowFormulaAPI(data) {
  return request({
    url: '/financeReport/cashFlowStatementExtendConfig/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 资产负债表导出
 * @param {*} data
 */
export function exportBalanceSheetReportAPI(data) {
  return request({
    url: '/financeReport/exportBalanceSheetReport',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 利润表导出
 * @param {*} data
 */
export function exportIncomeStatementReportAPI(data) {
  return request({
    url: 'financeReport/exportIncomeStatementReport',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 现金流量表导出
 * @param {*} data
 */
export function exportCashFlowStatementReportAPI(data) {
  return request({
    url: 'financeReport/exportCashFlowStatementReport',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}
