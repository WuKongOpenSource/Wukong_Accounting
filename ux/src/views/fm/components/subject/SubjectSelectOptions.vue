<template>
  <div class="subject-select-options">
    <el-popover
      v-model="popoverVisible"
      :disabled="disabled"
      placement="bottom-start"
      :width="popoverWidth"
      trigger="manual"
      popper-class="no-padding-popover">
      <div v-if="!selectedNode" class="wrapper">
        <div ref="subjectBox" class="subject-list">
          <div
            v-for="(item, index) in subjectShowList"
            :key="index"
            :class="{
              active: selectedNode && selectedNode.subjectId === item.subjectId,
              keyselect: index === hoverIndex
            }"
            class="subject-list-item"
            @click="handleSelect(item)">
            {{ item.number }} {{ item.subjectName }}
          </div>
          <div
            v-if="subjectShowList.length === 0"
            class="empty-tips">
            没有匹配的选项
          </div>
        </div>

        <flexbox
          v-if="canCreate"
          align="center"
          justify="flex-start"
          class="create-btn"
          @click.native="handleToCreate">
          <i class="wk wk-add icon" />
          <div class="text">新建科目</div>
        </flexbox>
      </div>

      <div
        v-else-if="showRelevance"
        class="relevance">
        <flexbox
          v-for="(item,key) in relative"
          :key="key"
          class="relevance_item">
          <div class="labelName">
            {{ item.labelName }}
          </div>

          <el-popover
            v-model="item.popoverAssistVisible"
            placement="bottom"
            width="200"
            trigger="click">
            <div class="wrapper">
              <div class="subject-list">
                <div
                  v-for="el in assistShowList(item.name, relationObj[item.adjuvantId]) "
                  :key="el.carteId"
                  class="subject-list-item"
                  @click="handleSelectAssist(el,item)">
                  {{ el.carteNumber }}  {{ el.carteName }}
                </div>
                <div
                  v-if="relationObj[item.adjuvantId] && relationObj[item.adjuvantId].length === 0"
                  class="empty-tips">
                  没有匹配的选项
                </div>
              </div>
              <flexbox
                align="center"
                justify="flex-start"
                class="create-btn"
                @click.native="addAdjuvant(item)">
                <i class="wk wk-add icon" />
                <div class="text">新建辅助核算</div>
              </flexbox>
            </div>
            <el-input slot="reference" v-model="item.name" @blur="assistBlur(item,relationObj[item.adjuvantId])" @focus="assistFocus()" />
          </el-popover>

          <!-- <el-select
                v-model="item.relationId"
                filterable placeholder="请输入关键字"
                @focus="assistFocus()"
                @change="assistChange(...arguments,item)">
                <el-option
                  v-for="el in relationObj[item.label]"
                  :key="el.carteId"
                  :label="el.carteName"
                  :value="el.carteId"/>
              </el-select> -->

        </flexbox>
      </div>

      <el-input
        slot="reference"
        ref="inputCore"
        v-model="subjectName"
        :disabled="disabled"
        @keydown.native.down.stop.prevent="navigateOptions('next')"
        @keydown.native.up.stop.prevent="navigateOptions('prev')"
        @keydown.native.enter.prevent="selectOption"
        @focus="inputFocus"
        @input="inputChange"
        @blur="inputBlur">
        <el-button
          v-if="!disabled"
          slot="suffix"
          class="choose-btn"
          type="text"
          @click.stop.prevent="handleToSelect">科目</el-button>
      </el-input>
    </el-popover>

    <subject-select-dialog
      v-if="selectDialogVisible"
      :visible.sync="selectDialogVisible"
      :before-choose="beforeChoose"
      v-bind="$attrs"
      @confirm="handleSelect" />

    <subject-update-dialog
      v-if="createDialogVisible"
      :visible.sync="createDialogVisible"
      show-subject-select-options
      :subject-list="subjectListLib"
      @confirm="saveSuccess" />

    <!-- 新增辅助核算 -->
    <el-dialog
      :visible.sync="addDialogVisible"
      :title="dialogTitle"
      append-to-body
      width="25%">
      <el-form ref="addForm" :model="addForm" :rules="rules">
        <el-form-item label-width="80px" label="编码：" prop="carteNumber">
          <el-input v-model="addForm.carteNumber" />
        </el-form-item>
        <el-form-item label-width="80px" label="名称：" prop="carteName">
          <el-input v-model="addForm.carteName" />
        </el-form-item>
        <el-form-item v-if="currentAddAssist.label == 6" label-width="80px" label="规格：" prop="specification">
          <el-input v-model="addForm.specification" />
        </el-form-item>
        <el-form-item v-if="currentAddAssist.label == 6" label-width="80px" label="单位：" prop="unit">
          <el-input v-model="addForm.unit" />
        </el-form-item>
        <el-form-item v-if="[1,2].includes(currentAddAssist.label)" label-width="80px" label="备注：" prop="remark">
          <el-input v-model="addForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveAdjuvant('addForm',currentAddAssist.adjuvantId)">保存</el-button>
        <el-button @click="closeAccountDialog()">关闭</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import {
  fmFinanceSubjectListAPI,
  // fmFinanceSubjectAdjuvantListAPI,
  fmFinanceQueryByAdjuvantAPI,
  fmAdjuvantQueryByAdjuvantIdAPI,
  fmAdjuvantCarteAddAPI
} from '@/api/fm/setting'

import emitter from 'element-ui/src/mixins/emitter'
import SubjectSelectDialog from './SubjectSelectDialog'
import SubjectUpdateDialog from './SubjectUpdateDialog'

import { objDeepCopy } from '@/utils'
import { isEmpty } from '@/utils/types'

/**
 * 通过筛选选择科目
 * @param {String|Number} value/v-model 选中的科目ID
 * @param {Boolean} canCreate 是否允许新建科目，默认 false 不允许
 * @param {Boolean} disabled 是否禁用
 * @param {Array} subjectList 科目列表数据(数据不为空时不会去请求科目列表)
 * @param {Function} beforeChoose 选择科目前的回调，返回值必须是一个布尔 true 允许选中 false 不允许选中
 * @param {Function} filterNodeMethod 对科目进行筛选时执行的方法，返回 true 该科目可以显示，返回 false 则该科目会被隐藏
 * @param {Boolean} isRelevance 是否显示辅助核算关联表单
 * @param {Object} relative 辅助核算关联数据
 * @event change 选中的科目改变时回调
 * @event update-list 新建完科目后的回调
 */
export default {
  name: 'SubjectSelectOptions',
  components: {
    SubjectSelectDialog,
    SubjectUpdateDialog
  },
  mixins: [emitter],
  props: {
    value: {
      type: [String, Number]
    },
    isChildren: {
      type: Boolean,
      default: false
    },
    isRelevance: {
      type: Boolean,
      default: false
    },
    canCreate: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    relative: {
      type: Object,
      default: () => {
        return {}
      }
    },
    subjectObject: Object,
    relationObj: {
      type: Object,
      default: () => {
        return {}
      }
    },
    subjectList: {
      type: Array,
      default: null
    },

    beforeChoose: {
      type: Function,
      default: null
    },
    filterNodeMethod: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      popoverVisible: false,
      subjectName: '',
      selectedNode: null,
      subjectListLib: [],
      popoverWidth: '',
      selectDialogVisible: false,
      createDialogVisible: false,
      hoverIndex: -1, // 选择项下标

      /** 新增辅助核算 */
      currentAddAssist: {}, // 辅助项类型对象
      addDialogVisible: false, // 添加辅助项的弹窗
      addForm: {
        carteNumber: '',
        carteName: '',
        specification: '',
        unit: '',
        remark: ''
      },
      rules: {
        carteNumber: [
          { required: true, message: '编码不能为空', trigger: 'blur' }
        ],
        carteName: [
          { required: true, message: '名称不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    /**
     * 筛选出的科目
     */
    subjectShowList() {
      if (!this.subjectName) return this.subjectListLib
      return this.subjectListLib.filter(item => {
        const name = `${item.number} ${item.subjectName}`
        return name.includes(this.subjectName)
      })
    },
    /**
     * 筛选出的辅助核算列表
     */
    assistShowList() {
      return function(searchVal, assistList) {
        if (!searchVal) return assistList || []
        if (assistList) {
          return assistList.filter(item => {
            const name = `${item.carteNumber} ${item.carteName}`
            return name.includes(searchVal)
          })
        } else {
          return []
        }
      }
    },
    /**
     * 是否显示辅助核算
     */
    showRelevance() {
      if (
        this.isRelevance &&
        this.selectedNode &&
        this.selectedNode.adjuvantList &&
        this.selectedNode.adjuvantList.length
      ) {
        return true
      }
      return false
    },
    /** 新增弹窗标题名 */
    dialogTitle() {
      if (isEmpty(this.currentAddAssist)) return '新增'
      return '新增' + this.currentAddAssist.labelName
    }
  },
  watch: {
    value: {
      handler() {
        this.setSelected()
      },
      immediate: true
    },
    subjectShowList: {
      handler() {
        if (
          this.subjectListLib.length > 0 &&
          this.subjectShowList.length === 0
        ) {
          if (!this.subjectObject) {
            this.selectedNode = null
            this.emitChange()
          }
        }
      },
      immediate: true,
      deep: true
    },
    subjectList: {
      handler() {
        if (
          this.subjectList &&
          this.subjectList.length > 0
        ) {
          this.getSubjectList()
        }
      },
      deep: true,
      immediate: true
    },
    // 科目弹窗关闭 科目 选择项下标重置
    popoverVisible: {
      handler(val) {
        this.hoverIndex = -1
        if (val) {
          this.$nextTick(() => {
            this.popoverWidth = this.$el.offsetWidth < 300 ? 300 : this.$el.offsetWidth
          })
        }
      }
    }
  },
  mounted() {
    this.popoverWidth = this.$el.offsetWidth < 300 ? 300 : this.$el.offsetWidth
    this.getSubjectList()
  },
  methods: {
    /**
     * 获取科目列表
     */
    getSubjectList() {
      if (this.subjectList && this.subjectList.length > 0) {
        let list = objDeepCopy(this.subjectList)
        this.formatList(list, '0')

        if (this.filterNodeMethod) {
          list = list.filter((item, index) => {
            const children = list.filter(o => o.parentId === item.subjectId)
            return this.filterNodeMethod(item, children, index)
          })
        }

        this.subjectListLib = list
        this.setSelected()
      } else {
        fmFinanceSubjectListAPI({
          type: null,
          isTree: 0
        }).then((res) => {
          if (this.subjectList && this.subjectList.length > 0) return
          let list = res.data || []
          this.formatList(list, '0')

          if (this.filterNodeMethod) {
            list = list.filter((item, index) => {
              const children = list.filter(o => o.parentId === item.subjectId)
              return this.filterNodeMethod(item, children, index)
            })
          }

          this.subjectListLib = list
          this.setSelected()
        }).catch(() => {})
      }
    },

    /**
     * 格式化拼接科目名称
     * @param list
     * @param parentId
     */
    formatList(list, parentId) {
      const arr = list.filter(o => o.parentId === parentId)
      const parentNode = list.find(o => o.subjectId === parentId)
      arr.forEach(item => {
        if (parentNode) {
          item.subjectName = `${parentNode.subjectName}_${item.subjectName}`
        }
        const children = list.filter(o => o.parentId === item.subjectId)
        children.forEach(child => {
          child.subjectName = `${item.subjectName}_${child.subjectName}`
          this.formatList(list, child.subjectId)
        })
      })
    },

    /**
     * 获取科目的下级科目
     * @param item
     * @return {*[]}
     */
    getChildren(item) {
      if (!item) return []
      return this.subjectListLib.filter(o => o.parentId === item.subjectId)
    },

    /**
     * 设置默认选中的科目
     */
    setSelected() {
      if (this.subjectListLib.length === 0) return
      if (!this.value) {
        this.selectedNode = null
        this.subjectName = ''
      } else {
        let findRes = this.subjectListLib.find(o => o.subjectId === this.value)

        if (this.subjectObject && !findRes) {
          findRes = this.subjectObject
        }
        if (findRes) {
          this.subjectName = `${findRes.number} ${findRes.subjectName}`
          this.$emit('update:subjectName', this.subjectName)

          if (this.isRelevance) {
            if (findRes.adjuvantList) {
              findRes.adjuvantList.forEach(item => {
                if (!this.relative.hasOwnProperty(item.adjuvantId)) {
                  this.$set(this.relative, item.adjuvantId, { ...item, relationId: '', name: '' })
                } else {
                  this.$set(this.relative[item.adjuvantId], 'labelName', item.labelName)
                }
              })
              Object.keys(this.relative).forEach(o => {
                if (!findRes.adjuvantList.find(k => k.adjuvantId == o)) {
                  delete this.relative[o]
                }
              })
              this.selectedNode = findRes
              this.$emit('change', this.selectedNode.subjectId, this.selectedNode)
              this.$emit('focus')
              this.inputBlur()
              this.getAdjuvantList()
              return
            } else {
              Object.keys(this.relative).forEach(o => {
                delete this.relative[o]
              })
            }
          }
          this.selectedNode = findRes
          this.$emit('focus')
        } else {
          this.selectedNode = null
          this.subjectName = ''
          this.emitChange()
        }
      }
    },

    /**
     * 键盘 上下键 选择
     */
    navigateOptions(direction) {
      if (!this.popoverVisible) {
        // this.popoverVisible = true
        return
      }
      if (this.subjectShowList.length === 0) return
      if (direction === 'next') {
        this.hoverIndex++
        if (this.hoverIndex === this.subjectShowList.length) {
          this.hoverIndex = 0
        }
      } else if (direction === 'prev') {
        this.hoverIndex--
        if (this.hoverIndex < 0) {
          this.hoverIndex = this.subjectShowList.length - 1
        }
      }

      this.$nextTick(() => this.scrollToOption())
    },

    /**
     * 滚动条到指定位置
     */
    scrollToOption() {
      // const subjectListDom = document.querySelector('.subject-list')
      const subjectListDom = this.$refs.subjectBox
      const innerDom = document.querySelector('.subject-list .keyselect')
      const top = innerDom ? innerDom.offsetTop : 0
      // console.log('父元素可视区域高度：', subjectListDom.clientHeight)
      // console.log('子元素距离父', innerDom.offsetTop, '子元素高', innerDom.offsetHeight)
      // console.log('子元素距父元素高+自身高度', top + innerDom.clientHeight)

      // 超过父元素的可视区域 滚动到对应的位置
      if (top + innerDom.clientHeight > subjectListDom.clientHeight) {
        subjectListDom.scrollTop = innerDom.offsetTop - 50
      } else {
        subjectListDom.scrollTop = 0
      }
    },

    /**
     * 回车选中 科目项
     */
    selectOption() {
      if (!this.popoverVisible) return
      this.handleSelect(this.subjectShowList[this.hoverIndex])
    },

    /**
     * 辅助核算输入框 聚焦
     */
    assistFocus() {
      this.popoverVisible = true
    },
    /**
     * 辅助核算输入框 失焦
     */
    assistBlur(item, assistList) {
      const exist = assistList.some(el => {
        const name = `${el.carteNumber} ${el.carteName}`
        return name.includes(item.name)
      })
      if (!exist) {
        item.name = ''
      }
    },

    /**
     * 选择的辅助核算发生改变
     */
    assistChange(val, data) {
      // console.log(val, data)
      data.name = this.relationObj[data.adjuvantId].find(item => item.carteId == val).carteName
      // data.name = data.option.find(item => item.carteId == val).carteName
      if (this.checkRelative()) {
        // this.getInitBalance()
        this.$emit('blur')
        this.popoverVisible = false
      }
    },
    getInitBalance() {
      const params = {
        subjectId: this.selectedNode.subjectId,
        assistAdjuvantList: Object.keys(this.relative).map(o => {
          return { adjuvantId: o, relationId: this.relative[o].relationId }
        })
      }
      fmFinanceQueryByAdjuvantAPI(params).then(res => {
        const resData = res.data
        if (resData) {
          this.$set(this.subjectObject, 'adjuvantBalance', resData.initialBalance || 0)
        }
      })
    },
    /**
     * 获取辅助核算数据
     */
    async getAdjuvantList(isRefresh = false) {
      if (!this.selectedNode.adjuvantList || !this.selectedNode.adjuvantList.length) return
      for (let index = 0; index < this.selectedNode.adjuvantList.length; index++) {
        const el = this.selectedNode.adjuvantList[index]
        // 辅助核算项数据存在且不是刷新的时候 阻止多次请求
        if (this.relationObj.hasOwnProperty(el.adjuvantId) && !isRefresh) continue
        this.$set(this.relationObj, el.adjuvantId, [])
        const res = await fmAdjuvantQueryByAdjuvantIdAPI({
          adjuvantId: el.adjuvantId,
          pageType: 0,
          status: 1
        })
        this.$set(this.relationObj, el.adjuvantId, res.data.list)
      }
      if (this.checkRelative()) {
        this.getInitBalance()
      }
    },

    /**
     * 输入框值修改
     */
    inputChange() {
      if (this.selectedNode) {
        this.selectedNode = null
        this.emitChange()
      }
      this.$nextTick(() => {
        if (!this.popoverVisible) {
          this.popoverVisible = true
        }
      })
    },

    /**
     * 输入框聚焦
     */
    inputFocus() {
      if (!this.selectedNode || this.showRelevance) {
        this.$emit('focus')
        this.popoverVisible = true
        this.$nextTick(() => {
          this.hoverIndex = 0
        })
      }
    },

    /**
     * 科目输入框失去焦点
     */
    inputBlur(event) {
      if (event && this.$el.contains(event.relatedTarget)) return
      if (this.isRelevance && this.selectedNode && !this.selectedNode.adjuvantList) {
        return
      } else if (this.isRelevance && this.selectedNode && this.selectedNode.adjuvantList && !this.checkRelative()) {
        return
      }
      this.$emit('blur')
      this.popoverVisible = false
    },

    focus() {
      this.$refs.inputCore.focus()
    },

    checkRelative() {
      for (let o = 0; o < this.selectedNode.adjuvantList.length; o++) {
        const item = this.selectedNode.adjuvantList[o]
        if (!this.relative[item.adjuvantId] || !this.relative[item.adjuvantId].relationId) return false
      }
      return true
    },

    /**
     * 选择辅助核算
     */
    handleSelectAssist(data, subjectItem) {
      subjectItem.relationId = data.carteId
      subjectItem.name = data.carteName
      subjectItem.carteNumber = data.carteNumber
      subjectItem.specification = data.specification
      subjectItem.popoverAssistVisible = false

      if (this.checkRelative()) {
        this.getInitBalance()
        this.$emit('blur')
        this.popoverVisible = false
      }
    },

    /**
     * 去选择科目
     */
    handleToSelect(e) {
      console.log(e)
      this.$refs.inputCore.blur()
      this.selectDialogVisible = true
    },

    /**
     * 去创建科目
     */
    handleToCreate() {
      this.createDialogVisible = true
    },

    /**
     * 选择科目
     * @param item
     */
    handleSelect(item) {
      console.log('选择科目', item)
      if (item) {
        this.selectedNode = this.subjectListLib.find(o => {
          return o.subjectId === item.subjectId
        })
        this.selectedNode.children = this.getChildren(item)
        if (this.beforeChoose) {
          const flag = this.beforeChoose(this.selectedNode)
          if (!flag) {
            this.$refs.inputCore.blur()
            this.selectedNode = null
            return
          }
        }
        this.subjectName = `${item.number} ${item.subjectName}`
        this.emitChange(item.subjectId)
      } else {
        if (this.beforeChoose) {
          const flag = this.beforeChoose(null)
          if (!flag) {
            this.$refs.inputCore.focus()
            return
          }
        }

        this.subjectName = ''
        this.selectedNode = null
        this.emitChange()
      }
    },

    saveSuccess() {
      this.popoverVisible = false
      this.$emit('update-list')
      this.getSubjectList()
    },

    /**
     * 派发事件
     */
    emitChange(data = null) {
      this.$emit('input', data)
      this.$emit('change', data, this.selectedNode)
      this.dispatch('ElFormItem', 'el.form.change', [data, this.selectedNode])
    },

    /**
     * 辅助项添加
     */
    addAdjuvant(item) {
      console.log(item)
      this.currentAddAssist = item
      this.resetForm()
      this.addDialogVisible = true
    },
    /**
     * 保存新增的辅助核算
     */
    saveAdjuvant(formName, adjuvantId) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const params = { ...this.addForm, adjuvantId: adjuvantId }
          fmAdjuvantCarteAddAPI(params).then(res => {
            this.$message.success('保存成功')
            this.getAdjuvantList(true)
          }).catch(() => {})
          this.resetForm()
          this.addDialogVisible = false
        } else {
          return false
        }
      })
    },
    /**
     * 关闭辅助项弹窗
     */
    closeAccountDialog() {
      this.addDialogVisible = false
      this.resetForm()
    },
    /**
     * 重置新增辅助核算表单
     */
    resetForm() {
      this.addForm = {
        carteNumber: '',
        carteName: '',
        specification: '',
        unit: '',
        remark: ''
      }
      // this.$refs.addForm.resetFields()
    }
  }
}
</script>

<style scoped lang="scss">
::v-deep .el-input__suffix {
  display: flex;
  align-items: center;
  justify-content: center;
}

.relevance {
  // background-color: #f7f8fa;
  width: 100%;
  padding: 10px;
  border: 1px solid #4bb8f3;
  border-top: none;
  box-shadow: $--box-shadow-bottom;

  .relevance_item {
    margin-bottom: 10px;

    &:last-child {
      margin-bottom: 0;
    }

    .relative {
      width: 200px;
      margin-left: 15px;
    }

    .labelName {
      width: 60px;
    }
  }
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
    max-height: 240px;
    overflow-y: auto;
    font-size: 12px;

    .subject-list-item {
      padding: 10px 15px;
      font-size: 14px;
      color: #333;
      cursor: pointer;
      background-color: white;

      &.subject-list-item.active,
      &.subject-list-item:hover {
        color: $--color-primary;
        background-color: #f7f8fa;
      }
    }

    .subject-list-item.keyselect {
      color: $--color-primary;
      background-color: #f7f8fa;
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
