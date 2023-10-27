/**
 * 结账
 */

import request from '@/utils/request'

/**
 * 查询列表页数据
 * @param {*} data
 */
export function queryStatementListAPI(data) {
  return request({
    url: '/financeStatement/queryStatement',
    method: 'post',
    data: data
  })
}

/**
 * 结账/反结账
 * @param {*} data
 */
export function settleAccountsAPI(data) {
  return request({
    url: '/financeStatement/statement',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 结账生成凭证
 * @param {*} data
 */
export function statementCertificateAPI(data) {
  return request({
    url: '/financeStatement/statementCertificate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 根据类型获取结账模版
 */
export function queryTplListByTypeAPI(data) {
  return request({
    url: '/financeStatement/queryListByType',
    method: 'post',
    data: data
  })
}

/**
 * 根据模板id获取科目信息
 */
export function querySubjectListByTplIdAPI(data) {
  return request({
    url: '/financeStatement/querySubjectList',
    method: 'post',
    data: data
  })
}

/**
 * 保存
 */
export function addStatementAPI(data) {
  return request({
    url: '/financeStatement/add',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 更新
 */
export function updateStatementAPI(data) {
  return request({
    url: '/financeStatement/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 保存模版数据
 */
export function addTemplateAPI(data) {
  return request({
    url: '/financeStatement/addTemplate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 更新模版数据
 */
export function updateTemplateAPI(data) {
  return request({
    url: '/financeStatement/updateTemplate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}
