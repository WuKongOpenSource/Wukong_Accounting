import request from '@/utils/request'

// 利润表统计
export function incomeStatementAPI() {
  return request({
    url: 'financeDashboard/incomeStatement ',
    method: 'post'
  })
}

// 查询仪表盘品配置
export function dashboardConfigAPI() {
  return request({
    url: 'financeDashboard/config ',
    method: 'post'
  })
}

// 修改仪表盘品配置 {config": "",}
export function dashboardUpdateConfigAPI(data) {
  return request({
    url: 'financeDashboard/updateConfig ',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}

// 指标列表
export function indicator(data) {
  return request({
    url: 'financeDashboard/indicator ',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}
// 指标列表
export function statistics(data) {
  return request({
    url: 'financeDashboard/statistics ',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

