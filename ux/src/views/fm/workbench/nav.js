export default [
  {
    title: '凭证',
    children: [
      {
        id: '001',
        name: '查凭证',
        check: false,
        icon: require('@/assets/workbenchSvg/voucherSearch.png'),
        url: '/fm/voucher/subs/search'
      },
      {
        id: '002',
        name: '凭证汇总表',
        check: false,
        icon: require('@/assets/workbenchSvg/voucherIndex.png'),
        url: '/fm/voucher/subs/statistics'
      }
    ]
  },
  {
    title: '账簿',
    children: [
      {
        id: '003',
        name: '总账',
        check: false,
        icon: require('@/assets/workbenchSvg/allLedger.png'),
        url: '/fm/ledger/subs/allLedger'
      },
      {
        id: '004',
        name: '明细账',
        check: false,
        icon: require('@/assets/workbenchSvg/detailedLedger.png'),
        url: '/fm/ledger/subs/detailedLedger'
      },
      {
        id: '005',
        name: '多栏账',
        check: false,
        icon: require('@/assets/workbenchSvg/multicolumnLedger.png'),
        url: '/fm/ledger/subs/multicolumnLedger'
      },
      {
        id: '006',
        name: '余额表',
        check: false,
        icon: require('@/assets/workbenchSvg/balanceSheet.png'),
        url: '/fm/ledger/subs/balanceSheet'
      }
    ]
  },
  {
    title: '报表',
    children: [
      {
        id: '007',
        name: '资产负债表',
        check: false,
        icon: require('@/assets/workbenchSvg/reportBalanceSheet.png'),
        url: '/fm/report/subs/balanceSheet'
      },
      {
        id: '008',
        name: '利润表',
        check: false,
        icon: require('@/assets/workbenchSvg/incomeSheet.png'),
        url: '/fm/report/subs/incomeSheet'
      },
      {
        id: '009',
        name: '现金流量表',
        check: false,
        icon: require('@/assets/workbenchSvg/cashSheet.png'),
        url: '/fm/report/subs/cashSheet'
      }
    ]
  },
  {
    title: '设置',
    children: [
      {
        id: '010',
        name: '科目设置',
        check: false,
        icon: require('@/assets/workbenchSvg/subjectSet.png'),
        url: '/fm/manage/subs/manage-subject-set',
        key: 'subject-set'
      },
      {
        id: '012',
        name: '凭证字',
        check: false,
        icon: require('@/assets/workbenchSvg/voucherCodeSet.png'),
        url: '/fm/manage/subs/manage-code-set',
        key: 'voucher-code-set'
      },
      {
        id: '013',
        name: '币别',
        check: false,
        icon: require('@/assets/workbenchSvg/currencySet.png'),
        url: '/fm/manage/subs/manage-currency-set',
        key: 'currency-set'
      },
      {
        id: '014',
        name: '财务初始化余额',
        check: false,
        icon: require('@/assets/workbenchSvg/initialBalance.png'),
        url: '/fm/manage/subs/manage-initial-balance-set',
        key: 'initial-balance'
      },
      {
        id: '015',
        name: '系统参数',
        check: false,
        icon: require('@/assets/workbenchSvg/systemParameter.png'),
        url: '/fm/manage/subs/manage-system-parameter',
        key: 'system-parameter'
      }
    ]
  }
]
