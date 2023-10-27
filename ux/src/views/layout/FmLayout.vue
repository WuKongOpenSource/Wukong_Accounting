<template>
  <el-container v-loading="loading">
    <el-header class="nav-container">
      <navbar
        v-if="menus && menus.length > 0"
        :menus="menus"
        title="财务管理"
        nav-index="/fm"
        @select="menuSelect"
      />
      <div class="quick-nav">
        <div class="nav-box">
          <div class="text">快捷入口：</div>
          <quick-nav />
        </div>
        <el-dropdown placement="top">
          <div class="account-item switch-btn">
            <p class="name">{{ financeCurrentAccount.companyName }}</p>
            <p class="time">{{ formatTime() }}</p>
            <i class="el-icon-caret-bottom" />
          </div>
          <el-dropdown-menu slot="dropdown" class="account-select">
            <el-dropdown-item
              v-for="item in financeAccountList"
              :key="item.accountId"
              @click.native="switchAccount(item)"
            >{{ item.companyName }}</el-dropdown-item
            >
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>
    <wk-container
      :menu="sideMenus"
      class-name="fm-container"
      :header-obj="headerCellObj"
    >
      <!-- <sidebar
        :items="fmRouters"
        main-router="fm">
        <el-popover
          v-if="financeCurrentAccount"
          slot="sidebar-bottom"
          :key="collapse ? 'right-start' : 'top'"
          v-model="sidebarPopoverVisible"
          :visible-arrow="false"
          :placement="collapse ? 'right-start' : 'top'"
          popper-class="el-popove--sidebar"
          width="190"
          trigger="hover">
          <template>
            <div
              v-for="item in financeAccountList"
              :key="item.accountId"
              class="el-menu-item el-menu-item--bottom"
              @click="switchAccount(item)">
              <item
                :collapse="false"
                :title="item.companyName" />
            </div>
          </template>
          <div
            slot="reference"
            class="el-menu-item el-menu-item--bottom">
            <item
              v-if="collapse"
              icon="customer" />
            <div
              v-else
              class="menu-item-content account-item">
              <p class="name">{{ financeCurrentAccount.companyName }}</p>
              <p class="time">{{ formatTime() }}</p>
            </div>
          </div>
        </el-popover>
      </sidebar> -->

      <el-main id="crm-main-container" style="padding: 0;">
        <slot name="header" />
        <router-view v-if="showMain" class="app-main-content" />
        <!-- <keep-alive :include="financeVisitedViewNames">
          <router-view :key="key" class="app-main-content" />
        </keep-alive> -->
        <slot />
      </el-main>
    </wk-container>
  </el-container>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { Navbar/*, Sidebar, AppMain*/ } from './components'
// import Item from './components/Sidebar/Item'
import WkContainer from './components/WkContainer'
// import Breadcrumb from '@/components/Breadcrumb'
import QuickNav from './components/QuickNav'

import { getNavMenus } from './components/utils'
import path from 'path'

export default {
  name: 'FmLayout',
  components: {
    Navbar,
    // Sidebar,
    // AppMain,
    WkContainer,
    // Breadcrumb,
    QuickNav
    // Item
  },
  data() {
    return {
      sidebarPopoverVisible: false,
      loading: false,
      showMain: true,
      sideMenus: []
    }
  },
  computed: {
    ...mapGetters([
      'fmRouters',
      'collapse',
      'financeVisitedViewNames',
      'financeCurrentAccount',
      'financeAccountList'
    ]),
    // 菜单
    menus() {
      return getNavMenus(this.fmRouters || [], '/fm')
    },
    key() {
      return this.$route.path
    },
    // 左侧菜单头部信息
    headerCellObj() {
      const { path } = this.$route
      if (path.includes('voucher')) {
        return {
          icon: 'fm wk-icon-voucher',
          label: '凭证',
          des: '凭证管理'
        }
      } else if (path.includes('ledger')) {
        return {
          icon: 'wk wk-icon-account-book',
          label: '账簿',
          des: '账簿管理'
        }
      } else if (path.includes('report')) {
        return {
          icon: 'wk wk-results-solid',
          label: '报表',
          des: '报表管理'
        }
      } else if (path.includes('manage')) {
        return {
          icon: 'wk wk-manage',
          label: '设置',
          des: '设置管理'
        }
      }
      return null
    },

    // 隐藏面包屑
    isHideBreadcrumb() {
      return (
        this.$route.path.includes('/workbench') ||
        this.$route.path.includes('/settleAccounts')
      )
    }
  },
  watch: {
    financeCurrentAccount() {
      this.showMain = false
      this.$nextTick(() => {
        this.showMain = true
      })
    },
    collapse() {
      this.sidebarPopoverVisible = false
    }
  },
  mounted() {
    console.log(this.$store)
    const accountId = this.$route.query.id || null
    this.loading = true
    this.getAccountList(accountId || null).then(res => {
      this.loading = false
    }).catch(() => {
      this.loading = false
    })
  },
  methods: {
    ...mapActions(['getAccountList', 'switchAccount']),

    /**
     * 菜单选择
     */
    menuSelect(menu) {
      console.log('seleeeee')
      const router = this.fmRouters[menu.index]
      let children = []
      if (router && router.children && router.children.length > 1) {
        children = router.children.filter((item) => !item.hidden)
      }
      if (children.length > 1) {
        this.sideMenus = this.getSideMenus(router.path, children)
      } else {
        this.sideMenus = []
      }
    },

    /**
     * 获取siderMenus
     */
    getSideMenus(mainPath, children) {
      const sideMenus = []
      children.forEach((item) => {
        if (!item.hidden) {
          sideMenus.push({
            ...item,
            path: path.resolve(mainPath, item.path)
          })
        }
      })
      return sideMenus
    },

    /**
     * 格式化时间
     */
    formatTime() {
      if (!this.financeCurrentAccount) return ''
      return this.$moment(this.financeCurrentAccount.startTime).format(
        'YYYY年第MM期'
      )
    }
  }
}
</script>

<style lang="scss">
@import "./components/Sidebar/variables.scss";

.el-menu-item--bottom {
  height: auto !important;
  padding: 0 !important;
  line-height: normal;
  background-color: transparent !important;

  .menu-item-content {
    padding: 0 16px;
    text-overflow: unset;
    background-color: rgba($color: #fff, $alpha: 0.1) !important;

    &:hover {
      background-color: $--background-color-base !important;
    }
  }
}

.el-popove--sidebar {
  padding: 0 !important;
  background-color: $menuBg;
  border-color: $--border-color-base;
}
</style>

<style lang="scss" scoped>
@import "./styles/common.scss";

::v-deep .navbar {
  .el-tabs__item {
    font-size: 16px;
  }
}

.account-select {
  width: 190px;
}

.aside-container {
  position: relative;
  box-sizing: border-box;
  background-color: #2d3037;
}

.nav-container {
  min-width: 1200px;
  padding: 0;
  box-shadow: 0 1px 2px #dbdbdb;
}

.el-container {
  height: 100%;
  overflow: hidden;
}

::v-deep .scrollbar-wrapper {
  padding-bottom: 60px;
}

.app-main-content {
  height: 100% !important;
}

.quick-nav {
  position: fixed;
  top: 56px;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 24px;
  box-shadow: 0 1px 2px $--color-n30;

  .nav-box {
    display: flex;
    align-items: center;
    font-size: 15px;
  }

  .account-item {
    position: relative;
    display: flex;
    align-items: center;
    padding-left: 8px;
    margin-right: 8px;
    line-height: 1.5;
    cursor: pointer;
    border-left: 1px solid $--color-n30;

    .name {
      margin-right: 16px;
    }

    .time {
      margin-right: 8px;
      font-size: $--font-size-small;
    }
  }
}

.fm-container {
  margin-top: 40px;

  ::v-deep .wk-side-menu {
    .el-menu-item {
      font-size: 16px;
    }
  }
}
</style>
