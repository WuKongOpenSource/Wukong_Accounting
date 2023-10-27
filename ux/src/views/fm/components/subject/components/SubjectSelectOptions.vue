<template>
  <div class="subject-select">
    <flexbox
      :gutter="0"
      class="headerList">
      <flexbox-item>
        <el-button v-for="(item,index) in typeList" :key="index" :type="activeType==item.label?'selected':''" @click="handleChangeType(item)">
          {{ item.name }}
        </el-button>
      </flexbox-item>
    </flexbox>
    <div class="content">
      <el-tree
        ref="elTree"
        v-loading="loading"
        :data="treeData"
        :props="defaultProps"
        :expand-on-click-node="false"
        node-key="subjectId"
        highlight-current
        default-expand-all
        @node-click="handleNodeClick">
        <span slot-scope="{ data }" class="custom-tree-node">
          {{ data.number + ' ' + data.subjectName }}
        </span>
      </el-tree>
    </div>
  </div>
</template>

<script>

import { fmFinanceSubjectListAPI } from '@/api/fm/setting'
import { objDeepCopy } from '@/utils'

/**
 * 选择科目树形结构弹窗
 * @param {Boolean} visible 控制弹窗标志
 * @param {Function} beforeChoose 选择科目前的回调，返回值必须是一个布尔 true 允许选中 false 不允许选中
 * @event close 关闭弹窗回调
 * @event confirm 确认选择回调，回调参数 data 选择的科目节点数据
 */
export default {
  props: {
    noUseDisabled: {
      type: Boolean,
      default: false
    },
    isChildren: {
      type: Boolean,
      default: false
    },
    beforeChoose: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      activeType: 1,
      loading: false,
      typeList: [
        { name: '资产', label: 1 },
        { name: '负债', label: 2 },
        { name: '权益', label: 3 },
        { name: '成本', label: 4 },
        { name: '损益', label: 5 }
        // { name: '共同', label: 6 }
      ],

      selectedNode: null, // 选中的节点
      dataCache: {}, // 缓存数据
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'subjectName'
      }
    }
  },
  mounted() {
    this.getSubjectList()
  },
  methods: {
    /**
     * 获取科目列表
     */
    getSubjectList() {
      this.loading = true
      fmFinanceSubjectListAPI({
        type: this.activeType,
        isTree: 1
      })
        .then(res => {
          this.loading = false
          this.dataCache[this.activeType] = res.data
          var treeData = objDeepCopy(res.data)
          if (this.noUseDisabled) {
            this.treeData = this.filterSubjectList(treeData)
          } else {
            this.treeData = treeData
          }
        }).catch(() => {
          this.loading = false
        })
    },
    /**
     * 切换类型
     * @param item
     */
    handleChangeType(item) {
      this.activeType = item.label
      if (
        !this.dataCache[this.activeType] ||
        this.dataCache[this.activeType].length === 0
      ) {
        this.getSubjectList()
      } else {
        const treeData = objDeepCopy(this.dataCache[this.activeType])
        if (this.noUseDisabled) {
          this.treeData = this.filterSubjectList(treeData)
        } else {
          this.treeData = treeData
        }
      }
      this.selectedNode = null
    },
    filterSubjectList(list) {
      for (let i = 0; i < list.length; i++) {
        const item = list[i]
        if (item.children) this.filterSubjectList(item.children)
        if (!item.status) {
          if (item.children && item.children.length) {
            list = list.splice(list.indexOf(item), 1, item.children)
          } else {
            list.splice(list.indexOf(item), 1)
          }
        }
      }
      return list
    },
    /**
     * 节点点击选中
     * @param item
     */
    handleNodeClick(item) {
      if (this.beforeChoose) {
        const flag = this.beforeChoose(item)
        if (!flag) return
      }
      this.selectedNode = item
      this.$emit('input', item)
    }
  }
}
</script>

<style lang="scss" scoped>
.headerList {
  display: flex;
  width: 100%;
  padding: 0 16px;

  .headerList-item {
    padding: 10px 0;
    text-align: center;
    cursor: pointer;
    border-left: 1px solid #ccc;

    &:first-child {
      border: 0 none;
    }

    &:hover,
    &.active {
      color: white;
      background-color: $--color-primary;
    }
  }
}

.content {
  width: 100%;
  height: 50vh;
  padding: 16px;
  overflow-y: auto;

  ::v-deep .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content,
  ::v-deep .el-tree-node__content:hover {
    color: $--color-primary;
    background-color: #deebff;
  }
}
</style>
