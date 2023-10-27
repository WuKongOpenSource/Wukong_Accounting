/** 商业智能管理路由 */
import Layout from '@/views/layout/FmLayout'
const layout = function(meta = {}, path = '/fm') {
  return {
    path: path,
    component: Layout,
    meta: {
      requiresAuth: true,
      ...meta
    }
  }
}

export default [
  {
    name: 'PrintPage',
    path: '/PrintPage', // 查凭证
    component: () => import('@/views/fm/voucher/PrintPage'),
    hidden: true,
    meta: {
      title: '凭证预览',
      icon: 'fm wk-icon-search',
      requiresAuth: true,
      permissions: ['finance', 'voucher', 'read']
    }
  },
  {
    ...layout({
      permissions: ['finance']
    }),
    children: [{
      name: 'Workbench',
      path: 'workbench', // 仪表盘
      component: () => import('@/views/fm/workbench/index'),
      meta: {
        title: '仪表盘',
        icon: 'board'
      }
    }]
  },
  {
    ...layout({
      title: '凭证',
      icon: 'record',
      permissionList: [
        ['finance', 'voucher', 'save'],
        ['finance', 'voucher', 'read'],
        ['finance', 'voucherSummary', 'read']
      ]
    }, '/fm/voucher/subs/'),
    children: [{
      name: 'VoucherCreate',
      path: 'create', // 录凭证
      component: () => import('@/views/fm/voucher/Create'),
      meta: {
        title: '录凭证',
        icon: 'fm wk-icon-edit',
        requiresAuth: true,
        permissionList: [
          ['finance', 'voucher', 'save'],
          ['finance', 'voucher', 'read']
        ]
      }
    }, {
      name: 'VoucherSearch',
      path: 'search', // 查凭证
      component: () => import('@/views/fm/voucher/Search'),
      meta: {
        title: '查凭证',
        icon: 'fm wk-icon-search',
        requiresAuth: true,
        permissions: ['finance', 'voucher', 'read']
      }
    }, {
      name: 'SummarizedVoucher',
      path: 'statistics', // 凭证汇总表
      component: () => import('@/views/fm/voucher/statistics'),
      meta: {
        title: '凭证汇总表',
        icon: 'fm wk-icon-voucher-table',
        requiresAuth: true,
        permissions: ['finance', 'voucherSummary', 'read']
      }
    }]
  },
  {
    ...layout({
      // permissions: ['finance', 'ledger'],
      title: '账簿',
      icon: 'icon-account-book',
      permissionList: [
        ['finance', 'generalLedger', 'read'],
        ['finance', 'subLedger', 'read'],
        ['finance', 'multiColumn', 'read'],
        ['finance', 'accountBalance', 'read']
      ]
    }, '/fm/ledger/subs/'),
    children: [{
      name: 'FinanceDetailedLedger',
      path: 'detailedLedger', // 明细账
      component: () => import('@/views/fm/ledger/detail/index'),
      meta: {
        title: '明细账',
        requiresAuth: true,
        icon: 'fm wk-icon-list',
        permissions: ['finance', 'subLedger', 'read']
      }
    }, {
      name: 'FinanceAllLedger',
      path: 'allLedger', // 总账
      component: () => import('@/views/fm/ledger/all/index'),
      meta: {
        title: '总账',
        icon: 'fm wk-icon-general-ledger',
        requiresAuth: true,
        permissions: ['finance', 'generalLedger', 'read']
      }
    }, {
      name: 'BalanceSheet',
      path: 'balanceSheet', // 余额表
      component: () => import('@/views/fm/ledger/balance/index'),
      meta: {
        title: '科目余额表',
        icon: 'fm wk-icon-account-table',
        requiresAuth: true,
        permissions: ['finance', 'accountBalance', 'read']
      }
    }, {
      name: 'MulticolumnLedger',
      path: 'multicolumnLedger', // 多栏账
      component: () => import('@/views/fm/ledger/multi/index'),
      meta: {
        title: '多栏账',
        icon: 'fm wk-icon-multi-account',
        requiresAuth: true,
        permissions: ['finance', 'multiColumn', 'read']
      }
    }, {
      name: 'AuxiliaryDetailedLedger',
      path: 'auxiliaryDetailedLedger', // 核算项目明细账
      component: () => import('@/views/fm/ledger/auxiliaryDetail/index'),
      meta: {
        title: '核算项目明细账',
        icon: 'fm wk-icon-check-list',
        requiresAuth: true,
        permissions: ['finance', 'subLedger', 'read']
      }
    },
    {
      name: 'AuxiliaryBalanceSheet',
      path: 'auxiliaryBalanceSheet', // 核算项目余额表
      component: () => import('@/views/fm/ledger/auxiliaryBalance/index'),
      meta: {
        title: '核算项目余额表',
        icon: 'fm wk-icon-project-table',
        requiresAuth: true,
        permissions: ['finance', 'accountBalance', 'read']
      }
    },
    {
      name: 'FMNumberMoneyAccount',
      path: 'numberMoneyAccount', // 数量金额明细账
      component: () => import('@/views/fm/ledger/numberMoney/index'),
      meta: {
        title: '数量金额明细账',
        icon: 'fm wk-icon-count-table',
        requiresAuth: true,
        permissions: ['finance', 'subLedger', 'read']
      }
    },
    {
      mame: 'TotalNumberMoney',
      path: 'totalNumberMoney', // 数量金额总账
      component: () => import('@/views/fm/ledger/totalNumber/index'),
      meta: {
        title: '数量金额总账',
        icon: 'fm wk-icon-count-all',
        requiresAuth: true,
        permissions: ['finance', 'generalLedger', 'read']
      }
    }
    ]
  },
  {
    ...layout({
      // permissions: ['fm', 'report'],
      title: '报表',
      icon: 'results-solid',
      permissionList: [
        ['finance', 'balanceSheet', 'read'],
        ['finance', 'profit', 'read'],
        ['finance', 'cashFlow', 'read']
      ]
    }, '/fm/report/subs/'),
    children: [
      {
        name: 'BalanceSheet',
        path: 'balanceSheet', // 资产负债表
        component: () => import('@/views/fm/report/balance'),
        meta: {
          title: '资产负债表',
          requiresAuth: true,
          icon: 'fm wk-icon-balance-sheet',
          permissions: ['finance', 'balanceSheet', 'read']
        }
      }, {
        name: 'IncomeSheet',
        path: 'incomeSheet', // 利润表
        component: () => import('@/views/fm/report/income'),
        meta: {
          title: '利润表',
          icon: 'fm wk-icon-profit',
          requiresAuth: true,
          permissions: ['finance', 'profit', 'read']
        }
      }, {
        name: 'CashSheet',
        path: 'cashSheet', // 现金流量表
        component: () => import('@/views/fm/report/cash'),
        meta: {
          title: '现金流量表',
          requiresAuth: true,
          icon: 'fm wk-icon-cash-table',
          permissions: ['finance', 'cashFlow', 'read']
        }
      }
      // , {
      //   path: 'expensesSheet', // 费用明细表
      //   component: () => import('@/views/fm/report/expensesSheet'),
      //   meta: {
      //     title: '费用明细表',
      //     icon: 'task'
      //   }
      // }
    ]
  },
  {
    ...layout({
      permissions: ['finance', 'checkOut']
    }),
    children: [{
      name: 'SettleAccounts',
      path: 'settleAccounts', // 结账
      component: () => import('@/views/fm/settleAccounts/index'),
      meta: {
        title: '结账',
        icon: 'payment'
      }
    }]
  },
  {
    ...layout({
      title: '设置',
      icon: 'manage',
      permissionList: [
        ['finance', 'subject', 'read'], // 科目
        ['finance', 'voucherWord'], // 凭证字
        ['finance', 'currency'], // 币别
        ['finance', 'financialInitial', 'read'], // 财务初始余额
        ['finance', 'systemParam', 'read'] // 系统参数
      ]
    }, '/fm/manage/subs/'),
    children: [
      {
        name: 'SubjectSet',
        path: 'manage-subject-set', // 科目
        component: () => import('@/views/fm/setting/subject'),
        meta: {
          title: '科目设置',
          requiresAuth: true,
          icon: 'wk wk-icon-manage-line',
          permissions: ['finance', 'subject', 'read']
        }
      },
      {
        name: 'VoucherCodeSet',
        path: 'manage-code-set', // 凭证字
        component: () => import('@/views/fm/setting/voucherCode'),
        meta: {
          title: '凭证字',
          requiresAuth: true,
          icon: 'fm wk-icon-voucher-word',
          permissions: ['finance', 'voucherWord']
        }
      },
      {
        name: 'CurrencySet',
        path: 'manage-currency-set', // 币别
        component: () => import('@/views/fm/setting/currency'),
        meta: {
          title: '币别',
          requiresAuth: true,
          icon: 'wk wk-business-line',
          permissions: ['finance', 'currency']
        }
      },
      {
        name: 'InitIalBalanceSet',
        path: 'manage-initial-balance-set', // 财务初始余额
        component: () => import('@/views/fm/setting/balance'),
        meta: {
          title: '财务初始余额',
          requiresAuth: true,
          icon: 'fm wk-icon-initial-balance',
          permissions: ['finance', 'financialInitial', 'read']
        }
      },
      {
        name: 'SystemParameter',
        path: 'manage-system-parameter', // 系统参数
        component: () => import('@/views/fm/setting/systemparameter'),
        meta: {
          title: '系统参数',
          requiresAuth: true,
          icon: 'fm wk-icon-system',
          permissions: ['finance', 'systemParam', 'read']
        }
      },
      {
        name: 'AssistAccounting',
        path: 'manage-assist-accounting', // 辅助核算
        component: () => import('@/views/fm/setting/assistAccounting'),
        meta: {
          title: '辅助核算',
          icon: 'fm wk-icon-check-computation'
          // requiresAuth: true,
        }
      }
    ]
  }
]

