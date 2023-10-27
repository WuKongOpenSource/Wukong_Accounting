<template>
  <div
    v-if="(!lazy || loaded) || active"
    v-show="active"
    class="sm-main">
    <div class="sm-main__hd">
      <div>
        <template v-if="!onlyAnnouncement">
          <el-select
            v-model="menuLabel"
            style="width: 200px;"
            mode="no-border"
            @change="refreshList">
            <el-option
              v-for="item in menuList"
              :key="item.label"
              :label="item.name"
              :value="item.label" />
          </el-select>

        </template>
        <span class="filter-label">显示:</span>
        <el-button
          v-for="(item, index) in [{
            label: '未读',
            value: 0
          },{
            label: '已读',
            value: 1
          }]"
          :key="index"
          :type="item.value === isRead ? 'selected' : null"
          @click="typeClick(item.value)">{{ item.label }}</el-button>

        <div class="handle">
          <el-dropdown
            trigger="click"
            @command="handleCommand">
            <el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                command="read">{{ `全部${currentMenu.label == 'all' ? '' : currentMenu.name}标记为已读` }}</el-dropdown-item>
              <el-dropdown-item
                command="delete">{{ `删除${currentMenu.name}已读消息` }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
      <!-- 数量 -->
      <div class="total-row">共<span>{{ totalRow }}</span>条</div>
    </div>

    <div class="sm-content">
      <div class="sm-list">
        <div
          :key="scrollKey"
          v-infinite-scroll="getList"
          infinite-scroll-distance="80"
          infinite-scroll-disabled="scrollDisabled"
          class="sm-main__bd">
          <message-cell
            v-for="(item, index) in list"
            :key="index"
            :data="item"
            :data-index="index"
            @detail="checkCRMDetail"
            @download="downloadError"
            @read="readMessageClick"
            @delete="deleteMessageClick" />
          <p
            v-if="loading"
            class="scroll-bottom-tips">加载中...</p>
          <p
            v-if="noMore"
            class="scroll-bottom-tips">没有更多了</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import {
  systemMessageListAPI,
  systemMessageReadAPI,
  systemMessageReadAllAPI,
  systemMessageClearAPI,
  systemMessageDeleteByIdAPI } from '@/api/common'
import MessageCell from './MessageCell'

import { mapGetters } from 'vuex'

export default {
  // 导航头部系统消息
  name: 'SystemMessage',
  components: {
    MessageCell
  },
  props: {
    // 仅公告
    onlyAnnouncement: {
      type: Boolean,
      default: false
    },
    unreadNums: Object,
    show: Boolean,
    lazy: Boolean
  },
  data() {
    return {
      showTodayDetail: false,
      // 1 任务 2 日志 3 oa审批 4公告 5 日程 6 客户管理
      menuLabel: 'all',

      // 列表
      list: [],
      loading: false,
      noMore: false,
      scrollKey: Date.now(),
      page: 1,
      isRead: 0, // 仅展示未读
      totalRow: 0, // 总数量
      loaded: false
    }
  },
  computed: {
    ...mapGetters([]),
    active() {
      const active = this.show
      if (active) {
        // eslint-disable-next-line vue/no-side-effects-in-computed-properties
        this.loaded = true
      }
      return active
    },

    permissionSave() {
      return this.oa && this.oa.announcement && this.oa.announcement.save
    },

    title() {
      if (this.onlyAnnouncement) {
        return this.unreadNums.announceCount && this.unreadNums.announceCount > 0 ? `公告（${this.unreadNums.announceCount}）` : '公告'
      }
      return this.unreadNums.allCount > 0 ? `通知（${this.unreadNums.allCount}）` : '通知'
    },

    scrollDisabled() {
      return this.loading || this.noMore
    },

    labelValue() {
      if (this.onlyAnnouncement) {
        return 4
      }
      return this.menuLabel == 'all' ? '' : this.menuLabel
    },

    menuList() {
      const menuList = [{
        name: '全部',
        label: 'all',
        countKey: 'allCount'
      }]
      return menuList
    },

    currentMenu() {
      return this.menuList.find(item => {
        return item.label === this.menuLabel
      })
    }
  },
  watch: {
    unreadNums() {
      // 未读 根据接口刷新全部数量
      if (this.isRead == 0) {
        if (this.onlyAnnouncement) {
          this.totalRow = this.unreadNums.announceCount
        } else {
          this.totalRow = this.unreadNums.allCount
        }
      }
    }
  },
  mounted() {
  },

  beforeDestroy() {},
  methods: {
    /**
     * 日程事件
     */
    todayHandle() {
      this.$bus.emit('handleSuccess')
      this.showTodayDetail = false
    },

    announcementSubmiteSuccess() {
      this.refreshList()
      this.$emit('update-count')
    },

    /**
     * body
     */
    /**
     * 刷新列表
     */
    refreshList() {
      this.page = 1
      this.list = []
      this.noMore = false
      this.scrollKey = Date.now()
    },

    /**
     * 类型切换
     */
    typeClick(value) {
      this.isRead = value
      this.refreshList()
    },

    /**
     * 获取列表
     */
    getList() {
      this.loading = true
      const params = {
        page: this.page,
        limit: 15,
        label: this.labelValue
      }

      params.isRead = this.isRead

      systemMessageListAPI(params)
        .then(res => {
          this.loading = false
          if (this.labelValue == params.label) {
            const resData = res.data || {}
            if (!this.noMore) {
              if (this.page == 1) {
                this.list = resData.list
              } else {
                this.list = this.list.concat(resData.list)
              }

              this.page++
            }
            this.noMore = resData.lastPage
            this.totalRow = resData.totalRow
          } else {
            this.refreshList()
          }
        })
        .catch(() => {
          this.noMore = true
          this.loading = false
        })
    },

    /**
     * 查看详情
     */
    checkCRMDetail(type, id, dataIndex, data) {
    },

    /**
     * 下载错误数据
     */
    downloadError(messageId) {
    },

    /**
     * 读取消息
     */
    readMessageClick(messageId, index) {
      systemMessageReadAPI({ messageId })
        .then(res => {
          this.list.splice(index, 1)
          this.$emit('update-count')
        })
        .catch(() => {
        })
    },

    deleteMessageClick(messageId, index) {
      this.$confirm('确定删除这条消息?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          systemMessageDeleteByIdAPI(messageId)
            .then(res => {
              this.refreshList()
              this.$emit('update-count')
              this.$message.success('操作成功')
            })
            .catch(() => {})
        })
        .catch(() => {})
    },

    handleCommand(action) {
      if (action === 'delete') {
        this.allDeleteClick()
      } else {
        this.allMarkDoneClick()
      }
    },

    /**
     * 全部标记完成
     */
    allMarkDoneClick() {
      systemMessageReadAllAPI({
        label: this.labelValue
      })
        .then(res => {
          this.list.forEach(item => {
            item.isRead = 1
          })
          this.$emit('update-count')
        })
        .catch(() => {
        })
    },

    /**
     * 全部删除
     */
    allDeleteClick() {
      if (this.currentMenu) {
        const name = this.currentMenu.label == 'all' ? '' : this.currentMenu.name
        this.$confirm(`确定删除全部${name}已读消息?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            systemMessageClearAPI({
              label: this.labelValue
            })
              .then(res => {
                this.refreshList()
                this.$emit('update-count')
                this.$message.success('操作成功')
              })
              .catch(() => {})
          })
          .catch(() => {})
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.sm-main {
  position: relative;
  height: 100%;

  &__hd {
    padding: 20px 24px 8px;
    font-size: 14px;

    .filter-label {
      margin-right: 8px;
    }

    .el-select + .filter-label {
      margin-left: 16px;
    }

    .title {
      font-weight: 600;
    }

    .handle {
      display: flex;
      float: right;

      .dropdown-btn {
        padding: 8px;
      }

      .notice-btn {
        color: $--color-white;
        background: $--color-primary;
      }

      .notice-btn + .el-dropdown {
        margin-left: 8px;
      }
    }

    .total-row {
      margin-top: 24px;
      font-size: 14px;
      color: $--color-text-secondary;

      span {
        color: $--color-text-primary;
      }
    }
  }

  &__bd {
    height: 100%;
    overflow-y: auto;
  }
}

.sm-content {
  position: relative;
  height: calc(100% - 100px);
}

.sm-menu {
  position: absolute;
  top: 0;
  bottom: 50px;
  left: 0;
  z-index: 1;
  width: 150px;
  padding: 10px 0;
  overflow-y: auto;
  font-size: 14px;
  background-color: white;
  border-right: 1px solid $--border-color-base;

  .xr-menu-item {
    padding: 16px 20px;
  }
}

.sm-list {
  height: 100%;
}

.scroll-bottom-tips {
  margin: 15px 0 65px;
}
</style>
