import request from '@/utils/request'

/**
 * 保存凭证
 * @param {*} data
 */
export function fmFinanceVoucherAddAPI(data) {
  return request({
    url: `/financeCertificate/${data.certificateId ? 'update' : 'add'}`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 查询凭证列表
 * @param {*} data
 */
export function fmFinanceVoucherQueryListAPI(data) {
  return request({
    url: '/financeCertificate/queryPageList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 查询凭证汇总
 * @param {*} data
 */
export function fmFinanceVoucherQueryListByTypeAPI(data) {
  return request({
    url: '/financeCertificate/queryListByType',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 查询凭证详情
 * @param {*} data
 */
export function fmFinanceVoucherQueryByIdAPI(data) {
  return request({
    url: '/financeCertificate/queryById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 审核凭证
 * @param {*} data
 */
export function fmFinanceVoucherCheckStatusAPI(data) {
  return request({
    url: '/financeCertificate/updateCheckStatusByIds',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 删除凭证
 * @param {*} data
 */
export function fmFinanceVoucherDeleteByIdsAPI(data) {
  return request({
    url: '/financeCertificate/deleteByIds',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 查询摘要列表
 * @param {*} data
 */
export function fmFinanceDigestQueryListAPI(data) {
  return request({
    url: '/financeDigest/queryList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 保存编辑摘要
 * @param {*} data
 */
export function fmFinanceDigestAddOrUpdataAPI(data) {
  return request({
    url: `/financeDigest/${data.digestId ? 'update' : 'add'}`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 删除摘要
 * @param {*} data
 */
export function fmFinanceDigestDeleteAPI(data) {
  return request({
    url: '/financeDigest/deleteById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 查询凭证号
 * @param {*} data
 */
export function fmFinanceCertificateQueryNumAPI(data) {
  return request({
    url: '/financeCertificate/queryCertificateNum',
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
export function fmFinanceCertificateDownloadExcelAPI(data) {
  return request({
    url: 'financeCertificate/import/template/download',
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
export function fmFinanceCertificateDownloadErrorExcelAPI(data) {
  return request({
    url: '/financeCertificate/downExcel',
    method: 'post',
    data: data,
    responseType: 'blob',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 导入凭证
 * @param {*} data
 */
export function fmFinanceCertificateExcelImportAPI(data) {
  var param = new FormData()
  Object.keys(data).forEach(key => {
    param.append(key, data[key])
  })
  return request({
    url: '/financeCertificate/import',
    method: 'post',
    data: param,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
/**
 * 插入凭证
 * @param {*} data
 */
export function fmFinanceCertificateInsertAPI(data) {
  return request({
    url: '/financeCertificate/insertCertificate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 插入凭证
 * @param {*} data
 */
export function fmFinanceCertificateSettleAPI(data) {
  return request({
    url: '/financeCertificate/certificateSettle',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 凭证模板类型列表
 * @param {*} data
 */
export function fmFinanceTemplateTypeQueryListAPI(data) {
  return request({
    url: '/financeTemplateType/queryList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 添加凭证模板类型
 * @param {*} data
 */
export function fmFinanceTemplateTypeAddAPI(data) {
  return request({
    url: '/financeTemplateType/add',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 删除凭证模板类型
 * @param {*} data
 */
export function fmFinanceTemplateTypeDeleteByIdAPI(data) {
  return request({
    url: '/financeTemplateType/deleteById',
    method: 'post',
    params: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 凭证模板列表
 * @param {*} data
 */
export function fmFinanceTemplateQueryListAPI(data) {
  return request({
    url: '/financeTemplate/queryList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
/**
 * 添加凭证模板
 * @param {*} data
 */
export function fmFinanceTemplateAddAPI(data) {
  return request({
    url: '/financeTemplate/add',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
/**
 * 删除凭证模板
 * @param {*} data
 */
export function fmFinanceTemplateDeleteByIdAPI(data) {
  return request({
    url: '/financeTemplate/deleteById',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 获取最大凭证时间和最小凭证时间
 */
export function queryCertificateTimeAPI() {
  return request({
    url: 'financeCertificate/queryCertificateTime',
    method: 'post'
  })
}

/**
 * 导出凭证
 * @param {*} data
 */
export function exportCertificateAPI(data) {
  return request({
    url: 'financeCertificate/exportCertificate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出凭证汇总
 * @param {*} data
 */
export function exportListByTypeAPI(data) {
  return request({
    url: 'financeCertificate/exportListByType',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 凭证列表打印预览
 * type
 * @param {*} data
 */
export function financePrintPreviewAPI(data) {
  return request({
    url: 'financeCertificate/previewFinance',
    method: 'post',
    data: data
  })
}

/**
 *  修改自定义配置
 * @param {*} data
 */
export function adminConfigSetCustomSettingAPI(data) {
  return request({
    url: `/adminConfig/setCustomSetting/${data.customKey}`,
    method: 'post',
    data: data.data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 *  查询自定义配置
 * @param {*} data
 */
export function adminConfigQueryCustomSettingAPI(data) {
  return request({
    url: `/adminConfig/queryCustomSetting/${data.customKey}`,
    method: 'post',
    data: data.data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 凭证列表打印预览
 * type
 * @param {*} data
 */
export function financeCertificatePreviewFinanceAPI(data) {
  return request({
    url: 'financeCertificate/preview',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
