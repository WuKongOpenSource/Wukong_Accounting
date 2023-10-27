<template>
  <div class="subject-select-options">
    <el-popover
      v-model="popoverVisible"
      width="288"
      trigger="manual"
      popper-class="no-padding-popover">
      <div v-if="!selectedNode" class="wrapper">
        <div class="subject-list">
          <div
            v-for="(item, index) in projectShowList"
            :key="index"
            :class="{active: selectedNode && selectedNode.projectId === item.projectId}"
            class="subject-list-item text-one-ellipsis"
            @click="handleSelect(item)">
            {{ item.projectName }}
          </div>
          <div
            v-if="projectShowList.length === 0"
            class="empty-tips">
            没有匹配的选项
          </div>
        </div>
      </div>

      <el-input
        slot="reference"
        ref="inputCore"
        v-model="projectName"
        @input="inputChange"

        @focus="inputFocus"
        @blur="inputBlur" />

    </el-popover>
  </div>
</template>

<script>
import { queryLabelName } from '@/api/fm/ledger'
export default {
  // 组件名
  name: 'AssistProjectSelect',
  components: {},
  props: {
    value: {
      type: [Number, String],
      default: () => ''
    },
    label: {
      type: [Number, String],
      default: () => ''
    }
  },
  // 组件数据
  data() {
    return {
      popoverVisible: false,
      projectName: '',
      selectedNode: '',
      projectList: [],
      projectId: ''
    }
  },
  // 计算属性
  computed: {
    /**
     筛选出的项目
     */
    projectShowList() {
      if (!this.projectName) return this.projectList
      return this.projectList.filter(item => {
        const name = `${item.projectName}`
        return name.includes(this.projectName)
      })
    }
  },
  // 监听属性
  watch: {
    value: {
      handler(val) {
        this.projectId = val
        this.setDefaultItem()
      },
      immediate: true
    },
    label: {
      handler(val) {
        console.log('object', val)
        this.getSubjectList()
      },
      immediate: true
    }
  },
  // 生命周期钩子函数
  created() {
  },
  mounted() {
  },
  // 组件方法
  methods: {
    // 获取项目列表
    getSubjectList() {
      queryLabelName({
        // label: this.label,
        adjuvantId: this.label || 0
      }).then(res => {
        this.projectList = []
        res.data.forEach(ele => {
          const item = {
            projectName: '',
            projectId: ''
          }
          item.projectName = `${ele.carteNumber}_${ele.name}`
          item.projectId = ele.relationId
          this.projectList.push(item)
        })
        console.log('获取项目列表', this.projectList)
      })
      this.setDefaultItem()
    },
    // 选择科目
    handleSelect(item) {
      if (item) {
        this.selectedNode = this.projectList.find(o => {
          return o.projectId === item.projectId
        })
        this.projectName = `${item.projectName}`
        this.emitChange()
      } else {
        this.projectName = ''
        this.selectedNode = null
      }
    },
    // 设置默认项
    setDefaultItem() {
      if (this.projectList.length === 0) return
      if (!this.value) {
        this.selectedNode = null
        this.projectName = ''
      } else {
        this.selectedNode = this.projectList.find(o => {
          return o.projectId === this.projectId
        })
        this.projectName = `${this.selectedNode.projectName}`
      }
    },
    emitChange() {
      this.$emit('input', this.selectedNode.projectId)
      this.$emit('update:projectName', this.projectName)
    },
    inputFocus() {
      if (!this.selectedNode) {
        this.popoverVisible = true
      }
    },
    inputChange() {
      console.log('this.selectedNode', this.selectedNode)
      if (this.selectedNode) {
        this.selectedNode = null
        this.projectName = ''
        this.$emit('input', '')
      }
      this.$nextTick(() => {
        if (!this.popoverVisible) {
          this.popoverVisible = true
        }
      })
    },
    inputBlur() {
      this.popoverVisible = false
    }
  }
}
</script>

<style scoped lang='scss'>
::v-deep .el-input__suffix {
  display: flex;
  align-items: center;
  justify-content: center;
}

.subject-select-options {
  width: 100%;

  .choose-btn {
    font-size: 12px;
  }
}

.wrapper {
  padding-bottom: 10px;

  .subject-list {
    width: auto;
    max-height: 240px;
    overflow-y: auto;
    font-size: 12px;

    .subject-list-item {
      padding: 10px 15px;
      color: #333;
      cursor: pointer;
      background-color: white;

      &.subject-list-item.active,
      &.subject-list-item:hover {
        width: 100%;
        color: $--color-primary;
        background-color: #f7f8fa;
      }
    }

    .empty-tips {
      padding: 10px 15px;
      font-size: 12px;
      color: $--color-text-placeholder;
    }
  }

  .create-btn {
    width: 100%;
    padding: 10px 15px 5px;
    color: $--color-primary;
    cursor: pointer;
    border-top: 1px solid #efefef;

    .icon {
      margin-right: 8px;
      font-size: 12px;
    }

    .text {
      font-size: 12px;
    }

    &:hover {
      .text {
        text-decoration: underline;
      }
    }
  }
}
</style>
