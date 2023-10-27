import request from '@/utils/request'

// 查询总账
export function queryDetailUpAccount(data) {
  return request({
    url: 'financeCertificate/queryDetailUpAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
// 查询明细账
export function queryDetailAccount(data) {
  return request({
    url: 'financeCertificate/queryDetailAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 查询多栏账
export function queryDiversification(data) {
  return request({
    url: 'financeCertificate/queryDiversification',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
// 查询科目余额表
export function queryDetailBalanceAccount(data) {
  return request({
    url: 'financeCertificate/queryDetailBalanceAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
// 核算获取辅助项目
export function queryLabelName(data) {
  return request({
    url: 'financeCertificate/queryLabelName',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
// 核算项目余额表
export function queryItemsDetailBalanceAccount(data) {
  return request({
    url: 'financeCertificate/queryItemsDetailBalanceAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
// 核算项目明细账
export function queryItemsDetailAccount(data) {
  return request({
    url: 'financeCertificate/queryItemsDetailAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
// 数量金额明细账
export function queryAmountDetailAccount(data) {
  return request({
    url: 'financeCertificate/queryAmountDetailAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 数量总账
export function queryAmountDetailUpAccount(data) {
  return request({
    url: 'financeCertificate/queryAmountDetailUpAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 导出明细账
 * @param {*} data
 */
export function exportDetailAccountAPI(data) {
  return request({
    url: 'financeCertificate/exportDetailAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出总账
 * @param {*} data
 */
export function exportDetailUpAccountAPI(data) {
  return request({
    url: 'financeCertificate/exportDetailUpAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出科目余额表
 * @param {*} data
 */
export function exportDetailBalanceAccountAPI(data) {
  return request({
    url: 'financeCertificate/exportDetailBalanceAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出多栏账
 * @param {*} data
 */
export function exportDiversificationAPI(data) {
  return request({
    url: 'financeCertificate/exportDiversification',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出核算项目明细账
 * @param {*} data
 */
export function exportItemsDetailAccountAPI(data) {
  return request({
    url: 'financeCertificate/exportItemsDetailAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出核算项目余额表
 * @param {*} data
 */
export function exportItemsDetailBalanceAccountAPI(data) {
  return request({
    url: 'financeCertificate/exportItemsDetailBalanceAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出数量金额明细账
 * @param {*} data
 */
export function exportAmountDetailAccountAPI(data) {
  return request({
    url: 'financeCertificate/exportAmountDetailAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 * 导出数量总账
 * @param {*} data
 */
export function exportAmountDetailUpAccountAPI(data) {
  return request({
    url: 'financeCertificate/exportAmountDetailUpAccount',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    responseType: 'blob'
  })
}

/**
 *  项目明细账树形结构
 * @param {*} data
 */
export function financeCertificateItemsDetailTreeAPI(data) {
  return request({
    url: `/financeCertificate/itemsDetailTree`,
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

// 数量金额明细账
export function financeCertificateQueryLabelNameByDataAPI(data) {
  return request({
    url: 'financeCertificate/queryLabelNameByData',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
