import request from '@/utils/request'

/**
 * 查询科目列表
 * @param {*} data
 */
export function fmFinanceSubjectListAPI(data) {
  return request({
    url: 'financeSubject/queryListByType',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 添加科目
 * @param {*} data
 */
export function fmFinanceSubjectAddAPI(data) {
  return request({
    url: `/financeSubject/${data.subjectId ? 'update' : 'add'}`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 查询科目辅助核算
 * @param {*} data
 */
export function fmFinanceSubjectAdjuvantListAPI(data) {
  return request({
    url: '/financeSubject/getAdjuvantList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 删除辅助核算
 * @param {*} data
 */
export function fmFinanceDeleteFinanceInitialAPI(data) {
  return request({
    url: '/financeInitial/deleteByAssistId',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 查询辅助核算余额
 * @param {*} data
 */
export function fmFinanceQueryByAdjuvantAPI(data) {
  return request({
    url: '/financeInitial/queryByAdjuvant',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 批量删除科目
 * @param {*} data
 */
export function fmFinanceSubjectBatchDeleteAPI(data) {
  return request({
    url: '/financeSubject/deleteByIds',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 下载导入模板
 * @param {*} data
 */
export function fmFinanceSubjectDownloadExcelAPI(data) {
  return request({
    url: '/financeSubject/downloadExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 下载错误数据
 * @param {*} data
 */
export function fmFinanceSubjectDownloadErrorExcelAPI(data) {
  return request({
    url: '/financeSubject/downExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 导入
 * @param {*} data
 */
export function fmFinanceSubjectExcelImportAPI(data) {
  var param = new FormData()
  Object.keys(data).forEach(key => {
    param.append(key, data[key])
  })
  return request({
    url: '/financeSubject/excelImport',
    method: 'post',
    data: param,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
/**
 * 批量启用禁用科目
 * @param {*} data
 */
export function fmFinanceSubjectUpdateStatusAPI(data) {
  return request({
    url: '/financeSubject/updateStatus',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 凭证字列表
 * @param {*} data
 */
export function fmFinanceVoucherListAPI(data) {
  return request({
    url: '/financeVoucher/queryList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 添加凭据字
 * @param {*} data
 */
export function fmFinanceVoucherAddAPI(data) {
  return request({
    url: `/financeVoucher/${data.voucherId ? 'update' : 'add'}`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 删除凭据字
 * @param {*} data
 */
export function fmFinanceVoucherDeleteAPI(data) {
  return request({
    url: '/financeVoucher/deleteById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 币种列表
 * @param {*} data
 */
export function fmFinanceCurrencyListAPI(data) {
  return request({
    url: '/financeCurrency/queryAllList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 添加币别
 * @param {*} data
 */
export function fmFinanceCurrencyAddOrUpdateAPI(data) {
  return request({
    url: `/financeCurrency/${data.currencyId ? 'update' : 'add'}`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 删除币别
 * @param {*} data
 */
export function fmFinanceCurrencyDeleteAPI(data) {
  return request({
    url: '/financeCurrency/deleteById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 财务初始余额
 * @param {*} data
 */
export function fmFinanceInitialListAPI(data) {
  return request({
    url: '/financeInitial/queryPageBySubjectType',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 *添加财务初始余额
 * @param {*} data
 */
export function fmFinanceInitialAddAPI(data) {
  return request({
    url: '/financeInitial/add',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 *保存财务初始余额
 * @param {*} data
 */
export function fmFinanceInitialUpdateAPI(data) {
  return request({
    url: '/financeInitial/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 财务初始余额添加单条数据
 * @param {*} data
 */
export function fmFinanceInitialSaveAndUpdateAPI(data) {
  return request({
    url: '/financeInitial/saveAndUpdate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 *财务初始余额试算平衡
 * @param {*} data
 */
export function fmFinanceInitialTrialBalanceAPI(data) {
  return request({
    url: '/financeInitial/queryTrialBalance',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 查询系统参数
 * @param {*} data
 */
export function fmFinanceParameterQueryParameterAPI(data) {
  return request({
    url: '/financeParameter/queryParameter',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 保存系统参数
 * @param {*} data
 */
export function fmFinanceParameterUpdateParameterAPI(data) {
  return request({
    url: '/financeParameter/updateParameter',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 查询辅助核算目录列表
 * @param {*} data
 */
export function fmAdjuvantListAPI(data) {
  return request({
    url: 'financeAdjuvant/queryAllList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 查询所有自定义辅助核算
 * @param {*} data
 */
export function fmAdjuvantCustomListAPI(data) {
  return request({
    url: 'financeAdjuvant/queryCustomList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 保存辅助核算
 * @param {*} data
 */
export function fmAdjuvantAddAPI(data) {
  return request({
    url: 'financeAdjuvant/add',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 保存辅助核算
 * @param {*} data
 */
export function fmAdjuvantUpdateAPI(data) {
  return request({
    url: 'financeAdjuvant/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 删除辅助核算
 * @param {*} data
 */
export function fmAdjuvantDeleteAPI(data) {
  return request({
    url: 'financeAdjuvant/deleteById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
      // 'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 辅助核算列表项 新增
 * @param {*} data
 */
export function fmAdjuvantCarteAddAPI(data) {
  return request({
    url: 'financeAdjuvantCarte/add',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 辅助核算列表项 编辑
 * @param {*} data
 */
export function fmAdjuvantCarteUpdateAPI(data) {
  return request({
    url: 'financeAdjuvantCarte/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 辅助核算列表项 删除
 * @param {*} data
 */
export function fmAdjuvantCarteDeleteAPI(data) {
  return request({
    url: 'financeAdjuvantCarte/deleteById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 辅助核算列表项 启用禁用
 * @param {*} data
 */
export function fmAdjuvantCarteUpdateStatusAPI(data) {
  return request({
    url: 'financeAdjuvantCarte/updateStatusById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 根据辅助核算查询关联信息
 * @param {*} data
 */
export function fmAdjuvantQueryByAdjuvantIdAPI(data) {
  return request({
    url: 'financeAdjuvantCarte/queryByAdjuvantId',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 辅助核算 下载导入模板
 * @param {*} data
 */
export function fmAdjuvantDownloadExcelAPI(data) {
  return request({
    url: 'financeAdjuvantCarte/downloadExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 辅助核算 excel导入
 * @param {*} data
 */
export function fmAdjuvantExcelImportAPI(data) {
  var param = new FormData()
  Object.keys(data).forEach(key => {
    param.append(key, data[key])
  })
  return request({
    url: 'financeAdjuvantCarte/excelImport',
    method: 'post',
    data: param,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载错误数据
 * @param {*} data
 */
export function fmAdjuvantCarteDownloadErrorExcelAPI(data) {
  return request({
    url: '/financeAdjuvantCarte/downExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 根据辅助核算查询关联信息
 * @param {*} data
 */
export function fmAdjuvantExportExcelAPI(data) {
  return request({
    url: 'financeAdjuvantCarte/exportExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 导出科目
 * @param {*} data
 */
export function exportExportListByTypeAPI(data) {
  return request({
    url: 'financeSubject/exportListByType',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出财务初始余额
 * @param {*} data
 */
export function exportExportPageBySubjectTypeAPI(data) {
  return request({
    url: 'financeCertificate/exportPageBySubjectType',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 *  下载财务初始余额导入模板
 * @param {*} data
 */
export function financeCertificateDownloadFinanceInitialExcelAPI(data) {
  return request({
    url: `/financeCertificate/downloadFinanceInitialExcel`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    },
    responseType: 'blob'
  })
}

/**
 *  导入财务初始余额
 * @param {*} data
 */
export function financeCertificateFinanceInitialImportAPI(data) {
  var param = new FormData()
  Object.keys(data).forEach(key => {
    param.append(key, data[key])
  })
  return request({
    url: `/financeCertificate/financeInitialImport`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 *  检查科目是否已经录入凭证
 * @param {*} data
 */
export function financeCertificateCheckSubjectCertificateAPI(data) {
  return request({
    url: `/financeCertificate/checkSubjectCertificate`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
