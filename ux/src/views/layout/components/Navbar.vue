<template>
  <div class="navbar">
    <flexbox style="width: auto;">
      <img
        src="@/assets/img/logo.png"
        class="logo"
        @click="enterMainPage">
      <div v-if="title" class="title">{{ title }}</div>
      <div class="menus">
        <el-tabs
          v-model="menuTab"
          class="header-menu"
          @tab-click="navItemsClick">
          <el-tab-pane
            v-for="(item, index) in menus"
            :key="index"
            :index="item.path"
            :name="item.path"
            :label="item.label">
            <!-- <i :style="{ fontSize: item.fontSize }" :class="item.icon" /> -->
            <template slot="label">
              <router-link
                class="router-link"
                :to="{ path: item.fullPath }">
                <span
                  class="label">{{ item.label }}</span>
              </router-link><span v-if="item.num" type="primary" class="el-badge__content el-badge__content--undefined">{{ item.num }}</span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
      <slot name="left" />
    </flexbox>

    <flexbox style="width: auto;">
      <xr-avatar
        v-if="userInfo && Object.keys(userInfo).length > 0"
        disabled
        :name="$getUserName(userInfo)"
        :size="24"
        :src="$getUserImg(userInfo)" />
    </flexbox>
  </div>
</template>

<script>
import { systemMessageUnreadCountAPI } from '@/api/common'
// eslint-disable-next-line
import { mapGetters } from 'vuex'
import { Loading } from 'element-ui'
import { on, off } from '@/utils/dom'
import { isEmpty } from '@/utils/types'

export default {
  filters: {
    langName: function(value) {
      if (value) {
        return { cn: '中文', en: 'English' }[value]
      } else {
        return '中文'
      }
    }
  },
  components: {
  },
  props: {
    title: String,
    menus: Array // 中间菜单数据
  },
  data() {
    return {
      unreadNums: {
        allCount: 0,
        announceCount: 0,
        crmCount: 0,
        examineCount: 0,
        eventCount: 0,
        logCount: 0,
        taskCount: 0
      },
      mesOnlyAnnouncement: false,
      sysMessageShow: false,
      intervalId: null,
      menuTab: '',
      appSideProps: null,
      helpSideShow: false, // 帮助中心
      appreciationSideShow: false // 增值服务
    }
  },
  computed: {
    ...mapGetters([
      'userInfo',
      'lang',
      'logo',
      'manage',
      'collapse',
      'app',
      'messageNum',
      'finance',
      'moduleAuth',
      'moduleData'
    ]),
    moreMenu() {
      const temps = [{
        command: 'baseInfo',
        divided: false,
        label: '基本信息',
        icon: 'wk wk-user'
      }]

      return temps.concat([{
        command: 'logOut',
        divided: false,
        label: '退出登录',
        icon: 'wk wk-logout'
      }, {
        command: 'version',
        divided: false,
        label: `版本 V${WKConfig.version}`,
        icon: 'wk wk-version',
        disabled: true
      }])
    },
    //  左侧下拉数据
    dropMenus() {
      const allList = []
      // eslint-disable-next-line
      for (const key in this.allItemsObj) {
        allList.push(this.allItemsObj[key])
      }
      return allList
    },
    allItemsObj() {
      var tempsItems = {}
      if (
        this.moduleAuth &&
        this.moduleAuth.finance &&
        this.finance &&
        !isEmpty(this.finance)
      ) {
        tempsItems.finance = {
          title: 'FS',
          des: '财务管理系统',
          type: 13,
          module: 'finance',
          path: '/fm',
          icon: 'wk wk-receivables',
          fontSize: '15px'
        }
      }
      return tempsItems
    },

    // 获取当前模块关键字
    currentModule() {
      const { path, query } = this.$route
      let module = query.module
      if (!module) {
        const paths = path.split('/')
        paths.forEach(o => {
          if (o && !module) {
            module = o
          }
        })
      }
      module = module ? module.toLowerCase() : ''
      const vilidModules = this.dropMenus.map(item => item.module)
      if (!vilidModules.includes(module)) {
        module = ''
      }
      return module
    }
  },
  watch: {
    $route: {
      handler(val) {
        const { path, query, meta } = this.$route
        // 子菜单逻辑 解决这部分之前的 当做菜单
        if (path.includes('/subs/')) {
          this.menuTab = path.split('/subs/')[0]
        } else {
          this.menuTab = path
        }

        // 有些多个地方对应一个位置 query 里传入 navActiveMenu
        if (query && query.navActiveMenu) {
          this.menuTab = query.navActiveMenu
        } else if (meta && meta.activeMenu) {
          this.menuTab = meta.activeMenu
        }

        if (this.menus) {
          const menu = this.menus.find(item => item.path === this.menuTab)
          menu && this.$emit('select', menu)
        }
      },
      immediate: true
    }
  },
  created() {
    // this.menuTab = this.menus[0].path
    // this.$store.dispatch('QueryMarketing')
  },

  mounted() {
    on(document, 'mousedown', this.handleDocumentClick)
  },
  beforeDestroy() {
    this.$bus.off('document-visibility')
    if (this.intervalId) {
      clearInterval(this.intervalId)
      this.intervalId = null
    }
    off(document, 'mousedown', this.handleDocumentClick)
  },
  methods: {
    navItemsClick(tab) {
      const menu = this.menus[parseInt(tab.index)]
      this.$router.push(menu.fullPath)
      this.$emit('change', menu)
    },

    enterSystemSet() {
      this.$router.push({
        name: 'manage'
      })
    },

    switchLang(item) {
      this.$store.commit('SET_LANG', item.lang)
      this.langName = item.name
    },

    /**
     * 获取系统未读消息数
     */
    getSystemUnreadNum(state) {
      if (this.intervalId) {
        clearInterval(this.intervalId)
        this.intervalId = null
      }
      if (state == 'visible') {
        this.sendSystemUnreadNum()
        this.intervalId = setInterval(() => {
          this.sendSystemUnreadNum()
        }, 600000)
      }
    },

    sendSystemUnreadNum() {
      systemMessageUnreadCountAPI()
        .then(res => {
          this.unreadNums = res.data
        })
        .catch(() => {
          if (this.intervalId) {
            clearInterval(this.intervalId)
            this.intervalId = null
          }
        })
    },

    /**
     * 有客户权限点击logo 进入仪表盘
     */
    enterMainPage() {
      this.$router.push('/')
    },

    moreMenuClick(command) {
      if (command == 'baseInfo') {
        const time = Date.parse(new Date())
        this.$router.push({
          name: 'person',
          query: {
            selectedIndex: 0,
            time
          }
        })
      } else if (command == 'logOut') {
        this.$confirm('退出登录？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            var loading = Loading.service({
              target: document.getElementById('#app')
            })
            this.$store
              .dispatch('LogOut')
              .then(() => {
                loading.close()
                location.reload()
              })
              .catch(() => {
                loading.close()
                location.reload()
              })
          })
          .catch(() => {})
      }
    },

    /**
     * 控制导航管理隐藏
     */
    handleDocumentClick(e) {
      if (typeof e.target.className !== 'string') return

      if (e.target.className.includes('wk-icon-fill-help')) {
        const type = e.target.dataset.type
        const id = e.target.dataset.id
        if (type && id) {
          this.helpSideProps = {
            menuIndex: 'Question',
            type: parseInt(type),
            id: parseInt(id)
          }
          this.helpSideShow = true
        }
      }

      if (e.target.className.includes('wk-premium-info')) {
        const type = e.target.dataset.type
        const force = e.target.dataset.force // 如果等于1 强制查看
        if (type) {
          // 如果已经开启不查看简介 也就是数量大于0
          if (type === 'BusinessInformation' && force !== '1') {
            const enterprise = this.moduleData.find(item => item.module === 'enterprise' && item.status === 1)
            if (enterprise.number > 0) {
              return
            }
          }

          this.appSideProps = {
            type
          }
          this.appreciationSideShow = true
        }
      }
    },

    /**
     * 查看消息详情
     */
    checkMessageDetail(onlyAnnouncement) {
      this.mesOnlyAnnouncement = onlyAnnouncement
      this.sysMessageShow = true
    },

    /**
     * 模块切换
     */
    dropClick(item) {
      this.$router.push(item.path)
    },

    /**
     * 展示help
     */
    enterHelp() {
      this.helpSideProps = null
      this.helpSideShow = true
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.header-menu {
  .el-tabs__header {
    margin-bottom: 0;
  }

  .el-tabs__nav-wrap::after {
    display: none;
  }

  .el-tabs__item {
    height: 56px;
    padding: 0 12px !important;
    line-height: 56px;

    .router-link {
      display: inline-block;
      user-select: none;
    }

    .label {
      position: relative;

      &:hover {
        color: $--color-primary;
      }
    }

    .label:hover::before {
      position: absolute;
      top: -4px;
      right: -4px;
      bottom: -4px;
      left: -4px;
      z-index: -1;
      content: "";
      background-color: $--color-b50;
      border-radius: $--border-radius-base;
    }
  }

  .el-tabs__active-bar {
    height: 3px;
  }

  .el-button--bg-text {
    padding-right: 4px;
    padding-left: 4px;
    font-weight: bold;

    [class^="el-icon-"] {
      margin-left: 2px;
      font-size: 12px;
      font-weight: bold;
      color: #97a0ae;
    }
  }
}
</style>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "style";

.navbar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 12px;
  background-color: white;

  .more + .logo {
    margin-left: 8px;
  }

  .logo {
    display: block;
    flex-shrink: 0;
    width: 24px;
    height: 24px;
    margin-left: $--interval-base;
    cursor: pointer;
    background-color: white;
  }

  .title {
    margin-right: 20px;
    margin-left: 8px;
    font-size: 18px;
    color: $--color-n600;
  }

  .menus {
  }

  // 模块切换图标
  .wk-grid {
    font-size: 16px;
  }

  .xr-avatar {
    cursor: pointer;
  }

  .xr-avatar + .mark {
    margin-left: 6px;
  }
}

// 右侧操作
.handel-box {
  padding: 0 15px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;

  .handel-button {
    width: 100%;
    background-color: #2362fb;
    border-color: #2362fb;
    border-radius: $--border-radius-base;
  }
}

.hr-top {
  padding-top: 3px;
  margin-top: 8px;
  border-top: 1px solid #f4f4f4;
}

// 系统消息
.wk-icon-gift,
.wk-announcement,
.wk-bell {
  font-size: 20px;
}

.el-badge {
  margin-right: 4px;
}

.wk-announcement:hover,
.wk-bell:hover {
  color: $--color-primary;
}

.el-dropdown-menu {
  ::v-deep .popper__arrow {
    display: none;
  }
}

::v-deep .nav-badge {
  .el-badge__content.is-fixed {
    top: 8px;
    right: 18px;
  }
}

.el-button.is-circle {
  padding: 6px;
  border: none;

  i {
    font-size: 20px;
  }

  &:hover {
    background-color: rgba($color:$--color-b50, $alpha: 0.9);
  }
}

.log-menus {
  position: relative;
  flex-shrink: 0;
  padding-right: $--interval-base;
  margin-right: $--interval-base;

  &::after {
    position: absolute;
    top: 6px;
    right: 0;
    bottom: 6px;
    width: 1px;
    content: " ";
    background-color: $--border-color-base;
  }
}

.el-button.is-current {
  color: $--color-primary;
  background-color: rgba(222, 235, 255, 0.9);
}

// .top-btn {
//   display: flex;
//   flex-direction: row;
//   justify-content: flex-start;
//   align-items: center;
//   color: $--color-primary;
//   .gift {
//     color: $--color-primary;
//     margin-right: 5px;
//   }
// }
</style>

