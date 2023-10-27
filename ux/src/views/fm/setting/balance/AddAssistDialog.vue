<template>
  <el-dialog
    v-loading="loading"
    :visible.sync="dialogVisible"
    :before-close="handleClose"
    :close-on-click-modal="false"
    append-to-body
    title="添加明细"
    width="500px">
    <el-form label-position="left" label-width="100px">
      <el-form-item label="科目：">
        <el-input v-model="form.subjectName" disabled />
      </el-form-item>
      <div class="assist-text">辅助核算：</div>
      <template v-if="showAssistForm && form.assist">
        <el-form-item
          v-for="(item, index) in form.assist.assistAdjuvantList"
          :key="index"
          :label="item.labelName"
          class="assist-item">

          <adjuvant-relative
            :current-type="item.adjuvantId"
            :label="item.label"
            @value-change="relativeChange($event, item)"
          />

          <!-- <wk-dept-dialog-select
            v-if="item.label === 9"
            v-model="item.relationId"
            class="relative" />
          <wk-user-dialog-select
            v-else-if="item.label === 10"
            v-model="item.relationId"
            class="relative" /> -->

          <!-- <crm-relative-cell
            v-else
            :value="item.relationId"
            :relative-type="getCrmType(item.label)"
            :radio="false"
            class="relative"
            @value-change="relativeChange($event, item)" /> -->
        </el-form-item>
      </template>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button v-debounce="handleConfirm" type="primary">确定</el-button>
      <el-button @click="handleClose">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { fmFinanceInitialAddAPI } from '@/api/fm/setting'

import AdjuvantRelative from '@/views/fm/components/AdjuvantRelative'

import { objDeepCopy } from '@/utils'

export default {
  name: 'AddAssistDialog',
  components: {
    AdjuvantRelative
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    parentNode: {
      type: Object,
      required: true
    },
    list: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      form: {},
      relative: {}
    }
  },
  computed: {
    showAssistForm() {
      return this.parentNode &&
        this.parentNode.subjectAdjuvantList &&
        this.parentNode.subjectAdjuvantList.length > 0
    }
  },
  watch: {
    visible: {
      handler() {
        this.dialogVisible = this.visible
        if (this.visible) {
          console.log('parentNode: ', this.parentNode)
          this.$nextTick(() => {
            const list = objDeepCopy(this.parentNode.subjectAdjuvantList)
            list.forEach(item => {
              item.relationId = []
              delete item.companyId
            })

            this.form = {
              ...this.parentNode,
              beginningBalance: 0,
              beginningNum: 0,
              initialBalance: 0,
              initialNum: 0,
              addUpCreditBalance: 0,
              addUpCreditNum: 0,
              addUpDebtorBalance: 0,
              addUpDebtorNum: 0,
              subjectName: `${this.parentNode.number} ${this.parentNode.subjectName}`,
              assist: {
                accountId: this.parentNode.accountId,
                subjectId: this.parentNode.subjectId,
                assistAdjuvantList: list
              }
            }
            delete this.form.companyId
            delete this.form.subjectAdjuvantList
            delete this.form.initialId
          })
        }
      },
      immediate: true
    }
  },
  methods: {
    getCrmType(type) {
      return {
        1: 'leads',
        2: 'customer',
        3: 'contacts',
        4: 'product',
        5: 'business',
        6: 'contract',
        7: 'receivables',
        8: 'invoice'
      }[type]
    },

    relativeChange(data, item) {
      // console.log('relativeChange: ', arguments)
      // console.log('外层接受', data, item)
      item.relationId = data
    },

    /**
     * 根据选择的关联项自动生成数据
     */
    generateData(formData) {
      if (!formData) return []
      const list = formData.assist.assistAdjuvantList || []
      if (!list || list.length === 0) return []
      const copyList = objDeepCopy(list).filter(o => o.relationId.length > 0)
      const maxlength = Math.max(...copyList.map(o => o.relationId.length))
      if (maxlength === 0) return []
      let notEmptyLen = 0 // 记录有多少个不为空的类别
      copyList.forEach(item => {
        const len = maxlength - item.relationId.length
        if (item.relationId.length > 0) notEmptyLen++
        // 补空位，类别的值需要逐一对应
        item.relationId = item.relationId.concat(Array(len).fill(null))
      })

      // 二维数组排列组合
      const getValues = (arr1, arr2) => {
        const res = []
        for (let i = 0; i < arr1.length; i++) {
          const v1 = arr1[i]
          for (let j = 0; j < arr2.length; j++) {
            const v2 = arr2[j]
            res.push([...v1, v2])
          }
        }
        return res
      }
      let arr = [[]]
      const initArr = copyList.map(o => o.relationId)
      console.log('initArr: ', initArr)
      for (let i = 0; i < initArr.length; i++) {
        arr = getValues(arr, initArr[i])
      }
      console.log('arr: ', arr)

      // 去重，过滤掉为空和没有全部填充的类别
      arr = arr.map(o => o.join('-'))
      arr = Array.from(new Set(arr))
      arr = arr.map(o => o.split('-'))
        .filter(o => {
          return o.filter(c => c).length >= notEmptyLen
        })
      console.log('arr--: ', arr)

      const listArr = []
      arr.forEach(ids => {
        const child = []
        copyList.forEach((o, i) => {
          if (!ids[i]) return
          child.push({
            ...o,
            relationId: ids[i]
          })
        })
        if (child.length > 0) {
          listArr.push(child)
        }
      })
      console.log('listArr: ', listArr)

      return listArr.map(item => {
        const data = objDeepCopy(formData)
        data.assist.assistAdjuvantList = item
        return {
          ...data,
          initialBalanceNum: 0,
          beginningNum: 0,
          addUpDebtorNum: 0,
          addUpCreditNum: 0
        }
      })
    },

    handleConfirm() {
      console.log('save form: ', this.form)
      if (this.loading) return
      const paramsItem = objDeepCopy(this.form)
      console.log('提交的总数据', paramsItem)
      paramsItem.assist.assistAdjuvantList.forEach(item => {
        // const type = this.getCrmType(item.label)
        // if (type) {
        //   item.relationId = item.relationId.map(o => o[`${type}Id`])
        // }

        item.relationId = item.relationId.map(o => o.carteId)
      })

      // const list = this.generateData(paramsItem)
      const list = paramsItem.assist.assistAdjuvantList.map(o => {
        return {
          adjuvantId: o.adjuvantId,
          subjectId: o.subjectId,
          carteIdList: o.relationId
        }
      })
      console.log('save list: ', list)

      this.loading = true
      fmFinanceInitialAddAPI(list).then(res => {
        setTimeout(() => {
          this.loading = false
          this.$emit('update')
          this.handleClose()
        }, 2000)
      }).catch(() => {
        this.loading = false
      })
    },

    handleClose() {
      this.dialogVisible = false
      this.$emit('close')
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped lang="scss">
.assist-text {
  line-height: 40px;
}

.el-form-item {
  margin-bottom: 10px;

  &.assist-item {
    ::v-deep .el-form-item__label {
      text-align: right;
    }
  }
}

.el-input {
  width: 360px;
}
</style>
