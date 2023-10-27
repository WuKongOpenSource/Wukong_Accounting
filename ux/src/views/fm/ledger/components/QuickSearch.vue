<template>
  <div v-loading="loading" class="quick-search">
    <div v-if="!foldStatus" class="search-wrap">
      <div class="head">
        <i class="icon wk wk-d-arrow-right" @click="e=>foldStatus=!foldStatus" />
        <div class="title-text">
          快速搜索
        </div>
      </div>
      <div class="search-input">
        <el-input v-model="quickSearch" size="small" suffix-icon="el-icon-search" />
      </div>
      <div class="search-content">
        <el-tree
          v-if="dataList.length"
          ref="tree"
          :data="dataList"
          node-key="nodeKey"
          :current-node-key="currentNodeKey"
          default-expand-all
          :props="defaultProps"
          highlight-current
          :filter-node-method="filterNode"
          @node-click="handleNodeClick">
          <div slot-scope="{ node }">
            <el-tooltip :content="node.label" effect="dark" placement="top">
              <span>
                {{ node.label }}
              </span>
            </el-tooltip>
          </div>
        </el-tree>
        <el-empty v-else description="暂无数据" />
      </div>
    </div>
    <div v-else class="fold-wrap" @click="e=>foldStatus=!foldStatus">
      <i class="icon wk wk-d-arrow-right" />
    </div>
  </div>
</template>

<script>

export default {
  props: {
    dataList: {
      type: Array,
      default: () => []
    },
    currentNodeKey: {
      type: [String, Number],
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      quickSearch: '',
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      foldStatus: false
    }
  },
  watch: {
    quickSearch(val) {
      this.$refs.tree.filter(val)
    }
  },
  created() {
  },
  methods: {
    /**
     * 节点点击
     * @param {*} data
     */
    handleNodeClick(data) {
      this.$emit('nodeClick', data)
    },
    /**
     * 过滤节点
     * @param {*} value
     * @param {*} data
     */
    filterNode(value, data) {
      console.log(value, data)
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    /**
     * 设置当前激活节点
     */
    setCurrentKey(key) {
      // if (!key) return
      const nodeRes = this.$refs.tree?.getNode(key)
      if (nodeRes) {
        this.$refs.tree?.setCurrentKey(key)
      } else {
        this.$refs.tree?.setCurrentKey(nodeRes)
      }
    }
  }

}
</script>

<style lang="scss" scoped>
  .quick-search {
    // display: flex;
    // flex-direction: column;
    margin-left: 10px;

    // overflow: hidden;
    border: 1px solid #ccc;

    .search-wrap {
      display: flex;
      flex-direction: column;
      width: 300px;
      height: 100%;
    }

    .fold-wrap {
      // display: flex;
      width: 30px;
      height: 100%;
      padding: 10px 0;
      text-align: center;
      cursor: pointer;
      background-color: #f2f3f6;
      transform: rotateY(180deg);

      .icon {
      }
    }

    .head {
      display: flex;
      align-items: center;
      height: 40px;
      padding: 10px;
      line-height: 40px;
      cursor: pointer;

      .title-text {
        margin-left: 18px;
      }
    }

    .search-input {
      padding: 5px 10px;
    }

    .search-content {
      flex: 1;
      width: 100%;
      padding: 10px;
      overflow: hidden;
      overflow-y: scroll;
    }
  }
</style>
