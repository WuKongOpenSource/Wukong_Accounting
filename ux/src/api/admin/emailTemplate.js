import request from '@/utils/request'

/**
 * 邮箱模板-列表
 * @param {*} data
 */
export function emailTemplateListAPI(data) {
  return request({
    url: 'email/emailTemplate/queryPageList',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 邮箱模板-新建
 * @param {*} data
 */
export function emailTemplateSaveAPI(data) {
  return request({
    url: 'email/emailTemplate/add',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 邮箱模板-编辑
 * @param {*} data
 */
export function emailTemplateUpdateAPI(data) {
  return request({
    url: 'email/emailTemplate/update',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

/**
 * 邮箱模板-删除
 * @param {*} data
 */
export function emailTemplateDeleteAPI(data) {
  return request({
    url: 'email/emailTemplate/deleteTemplate',
    method: 'post',
    data: data
  })
}

/**
 * 邮箱模板-线索、客户、联系人
 * @param {*} data
 */
export function emailTemplateModuleFieldAPI() {
  return request({
    url: 'crmField/queryEmailTemplateFields',
    method: 'post'
  })
}
