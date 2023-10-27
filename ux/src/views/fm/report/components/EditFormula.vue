<template>
  <el-dialog
    ref="wkDialog"
    :visible.sync="editFormulaVisible"
    :before-close="editFormulaClose"
    :close-on-click-modal="false"
    :append-to-body="true"
    width="900px">
    <div slot="title" class="dialog-header">
      <span class="title-box">编辑公式——{{ currentItem.name ? currentItem.name : '' }}</span>
    </div>
    <flexbox class="content-flex">
      <div class="flex-item">
        <div class="text subject-text">科目：</div>
        <select-subject v-model="subjectId" @change="receiveSubject" />
      </div>
      <div class="flex-item">
        <div class="text">取数规则：</div>
        <el-select v-if="sheetType == 'balanceSheet'" v-model="balanceRules" class="select-date" placeholder="">
          <el-option
            v-for="item in fetchOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
        <el-select v-else v-model="cashRules" class="select-date" placeholder="">
          <el-option
            v-for="item in fetchOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </div>
      <div class="flex-item">
        <div class="text">运算符号：</div>
        <el-radio-group v-model="operator">
          <el-radio :label="1">+</el-radio>
          <el-radio :label="2">-</el-radio>
        </el-radio-group>
      </div>
      <el-button class="add" type="primary" @click="addItem">添加</el-button>
    </flexbox>
    <el-table
      :data="formulaList"
      :summary-method="getSummaries"
      max-height="250"
      show-summary
      border
      style="width: 100%;margin: 20px 0;">
      <el-table-column
        v-for="(item, index) in fieldList"
        :key="index"
        :prop="item.field"
        :label="item.label"
        show-overflow-tooltip>
        <template slot-scope="{ row }">
          <span v-if="item.field === 'rules'">{{ row.rules | fetchValue }}</span>
          <span v-else-if="item.field === 'subjectName'">{{ row.subjectNumber }}  {{ row[item.field] }}</span>
          <span v-else>{{ row[item.field] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="text" @click="removeItem(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div
      slot="footer"
      class="dialog-footer">
      <flexbox class="footer-flex">
        <div class="left tips">温馨提示：新公式只对未出报表进行计算，不影响历史报表数据。</div>
        <div class="right">
          <el-button
            v-debounce="save"
            type="primary">保存</el-button>
          <el-button @click="editFormulaClose">取消</el-button>
        </div>
      </flexbox>
    </div>
  </el-dialog>
</template>

<script>
import {
  updateBalanceFormulaAPI,
  updatecashFlowFormulaAPI
} from '@/api/fm/report'

import SelectSubject from '@/views/fm/components/subject/SubjectSelectOptions'

import { isEmpty } from '@/utils/types'
import NP from 'number-precision'

export default {
  name: 'EditFormula',
  components: {
    SelectSubject
  },
  filters: {
    fetchValue(val) {
      return {
        0: '余额',
        1: '借方余额',
        2: '贷方余额',
        3: '科目借方余额',
        4: '科目贷方余额',
        5: '借方发生额',
        6: '贷方发生额',
        7: '损益发生额'
      }[val]
    }
  },
  props: {
    editFormulaVisible: Boolean,
    sheetType: {
      type: String,
      default: 'balanceSheet'
    },
    /** 编辑时候传递进来的信息 */
    currentItem: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      loading: false,

      subjectId: '', // 科目id
      subjectItem: {}, // 要添加的科目项
      balanceRules: 0, // 资产负债表取数规则
      cashRules: 5, // 现金流量表取数规则
      operator: 1, // 运算符号 1 +   2 -

      formulaList: [] // 编辑公式规则列表
    }
  },
  computed: {
    /**
     * @description: 取数规则数据
     */
    fetchOptions() {
      if (this.sheetType === 'balanceSheet') {
        return [
          { value: 0, label: '余额' },
          { value: 1, label: '借方余额' },
          { value: 2, label: '贷方余额' },
          { value: 3, label: '科目借方余额' },
          { value: 4, label: '科目贷方余额' }
        ]
      } else {
        return [
          { value: 5, label: '借方发生额' },
          { value: 6, label: '贷方发生额' },
          { value: 7, label: '损益发生额' }
        ]
      }
    },
    // 编辑公式 表格字段列表
    fieldList() {
      if (this.sheetType === 'balanceSheet') {
        return [
          { label: '科目', field: 'subjectName' },
          { label: '运算符号', field: 'operator' },
          { label: '取数规则', field: 'rules' },
          { label: '期末数', field: 'endPeriod' },
          { label: '年初数', field: 'initialPeriod' }
        ]
      } else {
        return [
          { label: '科目', field: 'subjectName' },
          { label: '运算符号', field: 'operator' },
          { label: '取数规则', field: 'rules' },
          { label: '本月数', field: 'monthValue' },
          { label: '本年数', field: 'yearValue' }
        ]
      }
    }
  },
  watch: {
    currentItem: {
      handler(val) {
        // console.log('监听值', val)
        if (!isEmpty(val) && val.formula) {
          this.formulaList = JSON.parse(val.formula)
          console.log('规则', this.formulaList)
        }
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    /**
     * @description: 获取选择的科目
     * @param {*} data
     * @param {*} node
     */
    receiveSubject(data, node) {
      console.log(data, node)
      this.subjectItem = node
    },
    /**
     * @description: 表格项添加
     */
    addItem() {
      if (isEmpty(this.subjectItem)) {
        this.$message.warning('请填写科目！')
        return
      }

      const findRes = this.formulaList.find(o => o.subjectId === this.subjectId)
      if (findRes) {
        this.$message.warning('科目不能重复添加！')
        return
      }

      const item = {
        subjectId: this.subjectItem.subjectId,
        subjectName: `${this.subjectItem.number} ${this.subjectItem.subjectName}`,
        subjectNumber: this.subjectItem.subjectNumber,
        operator: this.operator === 1 ? '+' : '-'
      }
      if (this.sheetType === 'balanceSheet') {
        item.rules = this.balanceRules
        item.endPeriod = 0
        item.initialPeriod = 0
      } else {
        item.rules = this.cashRules
        item.lastMonthValue = 0
        item.monthValue = 0
        item.yearValue = 0
      }
      console.log(this.subjectItem)
      this.formulaList.unshift(item)

      this.subjectItem = null
      this.subjectId = null
    },
    /**
     * @description: 表格项删除
     * @param {object} item
     */
    removeItem(item) {
      const idx = this.formulaList.findIndex(el => el.subjectId == item.subjectId)
      this.formulaList.splice(idx, 1)
    },
    /**
     * @description: 计算合计
     * @param {object} param
     */
    getSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计'
          return
        }
        if (column.property === 'rules') {
          sums[index] = ''
          return
        }

        const arr = data.map(item => {
          const num = Number(item[column.property])
          if (!isNaN(num)) {
            const step = item.operator === '+' ? 1 : -1
            return num * step
          }
          return num
        })
        if (!arr.every(value => isNaN(value))) {
          sums[index] = arr.reduce((acc, current) => {
            return NP.plus(acc, current)
          })
        } else {
          sums[index] = ''
        }
      })
      return sums
    },

    /**
     * @description:编辑关闭
     */
    editFormulaClose() {
      this.$emit('editFormulaClose')
    },

    /**
     * @description: 保存编辑公式
     */
    save() {
      const request = this.sheetType === 'balanceSheet' ? updateBalanceFormulaAPI : updatecashFlowFormulaAPI
      request({
        formulaBOList: this.formulaList,
        id: this.currentItem.id,
        sort: this.currentItem.sort
      })
        .then(res => {
          console.log('保存编辑公式', res)
          this.$message.success('保存成功')
          this.$emit('save-success')
        })
        .catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog-header {
  text-align: center;

  .title-box {
    font-size: 18px;
    font-weight: 600;
  }
}

.flex-item {
  display: flex;
  align-items: center;
  margin-right: 25px;

  .subject-text {
    width: 70px;
  }
}

.add {
  margin-left: 30px;
}

.dialog-footer {
  .footer-flex {
    justify-content: space-between;

    .tips {
      margin-left: 15px;
      font-size: 12px;
    }
  }
}

</style>
