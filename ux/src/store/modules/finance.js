import {
  getAccountSetListAPI,
  switchAccountSetAPI
} from '@/api/admin/accountBook'
import { queryCertificateTimeAPI } from '@/api/fm/voucher'
import moment from 'moment'

export default {
  state: {
    currentAccount: {},
    accountList: [],
    visitedViews: [], // 路由缓存
    visitedViewNames: [],
    // 最早的凭证和最新的凭证录入时间
    timeRange: {
      minTime: null, // 最小时间
      maxTime: null // 最大时间
    },
    // 筛选是使用的时间范围(包含当前期)
    filterTimeRange: {
      minTime: {}, // 最小时间
      maxTime: {} // 最大时间
    }
  },
  actions: {
    /**
     * 获取帐套
     * @param commit
     * @param dispatch
     * @param accountId
     */
    getAccountList({ commit, dispatch }, accountId = null) {
      getAccountSetListAPI().then(res => {
        const vm = window.app
        const accountList = res.data || []
        commit('SET_ACCOUNT_LIST', accountList)

        // 没有帐套
        if (accountList.length === 0) {
          // 没有新建帐套的权限
          if (!vm.$auth('manage.finance.accountSet')) {
            // vm.$router.go(-1)
            vm.$confirm('您暂时没有可用的账套，请联系系统管理员。', '提示', {
              confirmButtonText: '确定',
              showCancelButton: false,
              showConfirmButton: false,
              center: true,
              closeOnClickModal: false,
              showClose: false,
              type: 'warning'
            })
            return
          }
          vm.$confirm('您暂时还没有可用的账套，是否前往创建?', '提示', {
            confirmButtonText: '去创建账套',
            showCancelButton: false,
            center: true,
            showClose: false,
            closeOnClickModal: false,
            type: 'warning'
          }).then(() => {
            vm.$router.push('/manage/finance/handle')
          }).catch(() => {
            vm.$router.go(-1)
          })
          return
        }

        const defaultAccount = accountList.find(o => o.isDefault === 1)
        let findRes = null
        if (accountId) {
          if (defaultAccount.accountId === accountId) {
            findRes = defaultAccount
          } else {
            // 如果帐套id不是默认帐套则去切换
            findRes = accountList.find(o => o.accountId === accountId)
            dispatch('switchAccount', findRes)
            return
          }
        } else {
          findRes = defaultAccount
        }

        if (findRes) {
          console.log('获取的账套', findRes)
          commit('SET_CURRENT_ACCOUNT', findRes)
          dispatch('getVoucherTimeRange')
        } else {
          commit('SET_CURRENT_ACCOUNT', accountList[0])
          dispatch('switchAccount', accountList[0])
        }
      }).catch(() => {
        commit('SET_CURRENT_ACCOUNT')
        commit('SET_ACCOUNT_LIST')
      })
    },

    /**
     * 切换帐套
     * @param commit
     * @param account
     */
    switchAccount({ commit }, account) {
      switchAccountSetAPI({
        accountId: account.accountId
      }).then(res => {
        const routeData = window.app.$router.resolve({ path: '/fm/workbench' })
        window.location.href = routeData.href
        window.location.reload()
      }).catch(() => {})
    },

    /**
     * 更新当前帐套信息
     * @param dispatch
     * @param state
     */
    updateCurrentAccount({ dispatch, state }) {
      dispatch('getAccountList', state.currentAccount.accountId)
    },

    /**
     * 获取凭证的最大时间和最小时间
     * @param commit
     */
    getVoucherTimeRange({ commit }) {
      queryCertificateTimeAPI().then(res => {
        commit('SET_VOUCHER_TIME_RANGE', res.data || {})
      }).catch(() => {
      })
    },
    addVisitedView({ commit }, data) {
      console.log('ssdasdsad', data)
      commit('ADD_VISITED_VIEW', data)
      commit('ADD_VISITED_VIEW_NAME', data)
    },
    clearVisItedViews({ commit }, data) {
      commit('CLEAR_VISITED_VIEWS', data)
      commit('CLEAR_VISITED_VIEW_NAMES', data)
    },
    deleteVisItedView({ commit }, data) {
      commit('DELETE_VISITED_VIEW', data)
      commit('DELETE_VISITED_VIEW_NAME', data)
    }
  },
  mutations: {
    SET_CURRENT_ACCOUNT(state, data) {
      state.currentAccount = data || {}
    },
    SET_ACCOUNT_LIST(state, data) {
      state.accountList = data || []
    },
    ADD_VISITED_VIEW(state, data) {
      const visitedViews = state.visitedViews
      if (visitedViews.find(o => o.path == data.path)) return
      visitedViews.push(data)
    },
    CLEAR_VISITED_VIEWS(state) {
      state.visitedViews = []
    },
    DELETE_VISITED_VIEW(state, data) {
      state.visitedViews = state.visitedViews.filter(o => o.path !== data.path)
    },
    ADD_VISITED_VIEW_NAME(state, data) {
      const visitedViewNames = state.visitedViewNames
      if (visitedViewNames.find(o => o == data.name)) return
      visitedViewNames.push(data.name)
    },
    CLEAR_VISITED_VIEW_NAMES(state) {
      state.visitedViewNames = []
    },
    DELETE_VISITED_VIEW_NAME(state, data) {
      state.visitedViewNames = state.visitedViewNames.filter(o => o !== data.name)
    },
    SET_VOUCHER_TIME_RANGE(state, data) {
      let min = moment(data.minTime).startOf('month') // 最早的一个凭证时间
      let max = moment(data.maxTime).startOf('month') // 最晚的一个凭证时间
      const enableTime = moment(state.currentAccount.enableTime).startOf('month') // 帐套启用时间
      const now = moment(state.currentAccount.startTime).startOf('month') // 当前期数

      if (!min.isValid()) min = now
      if (!max.isValid()) max = now

      state.timeRange = {
        minTime: min.format('YYYY-MM-DD'),
        maxTime: max.format('YYYY-MM-DD')
      }

      // 有可能出现当前期还没有录入凭证的情况
      const rangeMin = moment.min([min, max, now, enableTime])
      const rangeMax = moment.max([min, max, now, enableTime])
      state.filterTimeRange = {
        minTime: { val: rangeMin.format('YYYY-MM-DD'), timeObj: rangeMin },
        maxTime: { val: rangeMax.format('YYYY-MM-DD'), timeObj: rangeMax }
      }
    }
  },

  getters: {
    // 财务管理
    financeCurrentAccount: state => state.currentAccount,
    financeAccountList: state => state.accountList,
    financeTimeRange: state => state.timeRange,
    financeVisitedViews: state => state.visitedViews,
    financeVisitedViewNames: state => state.visitedViewNames,
    financeFilterTimeRange: state => state.filterTimeRange
  }
}
