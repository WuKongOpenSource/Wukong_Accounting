/** 系统管理路由 */
import Layout from '@/views/layout/AdminLayout'

const layout = function(meta = {}, path = '/manage', requiresAuth = true) {
  return {
    path: path,
    component: Layout,
    meta: {
      requiresAuth: requiresAuth,
      ...meta
    }
  }
}

export default [
  // 财务管理，账套管理
  {
    ...layout({
      permissions: ['manage', 'finance'],
      title: '财务管理',
      icon: 'icon-fm-line'
    }, '/manage/finance'),
    alwaysShow: true,
    children: [{
      path: 'handle',
      component: () => import('@/views/admin/finance/accountBook'),
      meta: {
        title: '账套管理',
        requiresAuth: true,
        permissions: ['manage', 'finance', 'accountSet']
      }
    }]
  }
]
