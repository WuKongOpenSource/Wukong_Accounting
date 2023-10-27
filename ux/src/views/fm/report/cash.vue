<template>
  <div class="app-container">
    <div class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          {{ pageTitle }}
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="40"
            data-id="321" />
        </span>
        <div class="buttons">
          <el-button
            v-for="(item,index) in btnList"
            :key="index"
            :plain="item.plain"
            :type="item.primary"
            @click.native="btnClick(item.type)">
            {{ item.name }}
            <i
              v-if="item.type === 'adjust'"
              class="wk wk-icon-fill-help wk-help-tips"
              style="color: white;background: #0052cc;"
              data-type="40"
              data-id="322" />
          </el-button>
          <el-dropdown
            trigger="click"
            class="receive-drop">
            <el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-if="$auth('finance.cashFlow.print')" @click.native="printAll">打印</el-dropdown-item>
              <el-dropdown-item v-if="$auth('finance.cashFlow.export')" @click.native="btnClick('export')">导出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox v-if="step == 1" class="left-box">
          <el-select v-model="type" class="select-quarter" mode="no-border" @change="refreshClick">
            <el-option :value="1" label="月报" />
            <el-option :value="2" label="季报" />
          </el-select>
          <el-date-picker
            v-if="type==1"
            v-model="dateValue"
            :picker-options="pickerOptions"
            :clearable="false"
            type="month"
            value-format="yyyyMM"
            placeholder="选择月"
            class="select-date"
            @change="refreshClick" />
          <choose-quarter v-if="type==2" :default-value="dateValue" :get-value="getYearMounth" />

          <el-button v-debounce="resSearch" plain icon="el-icon-refresh-right" type="subtle" />
        </flexbox>
        <flexbox v-else class="left-box">
          <span v-if="type==1">
            调整辅助数据项 - {{ dateValue }}期
          </span>
          <span v-if="type==2">
            调整辅助数据项 - {{ `${yearMounth.fromPeriod}-${yearMounth.toPeriod}` }}期
          </span>
        </flexbox>
      </flexbox>

      <div v-loading="loading" class="table-wrapper">
        <el-table
          id="cashSheet"
          :data="tableData"
          :height="tableHeight"
          :cell-class-name="cellClassName"
          border
          style="width: 100%;"
          stripe>
          <el-table-column
            v-for="(item, index) in fieldList"
            :key="index"
            :prop="item.field"
            :align="item.align"
            :label="item.label"
            :width="item.width"
            show-overflow-tooltip>
            <template slot-scope="{ row ,column}">
              <template v-if="item.field === 'name'">
                <div
                  :style="{paddingLeft: getNameIndent(row)}"
                  class="name-box text-one-ellipsis"
                  @click="openEditFormula(row)">
                  {{ row[item.field] }}
                  <i
                    v-show="row.editable && step === 2"
                    class="wk wk-icon-modify"
                    @click="openEditFormula(row)" />
                </div>
              </template>
              <template v-else-if="step === 1 && ['yearValue','monthValue'].includes(item.field)">
                <span>{{ row[item.field] | filterValue }}</span>
              </template>
              <template v-else-if="step !== 1 && ['yearValue','monthValue'].includes(item.field)">
                <el-input-number
                  v-if="row.sort!==0"
                  v-model="row[item.field]"
                  :controls="false"
                  :disabled="getDisabledStatus(row)"
                  @change="inputChange(row ,column)" />
              </template>
              <template v-else>
                <!-- <span>{{ row[item.field] }}</span> -->
                <span>{{ row[column.property] === 0 ? '' : row[column.property] }}</span>
              </template>
            </template>
          </el-table-column>
          <div
            v-if="printFooterShow"
            slot="append"
            style="
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            font-size: 20px;
"
          >
            <span>单位负责人：</span>
            <span>会计负责人:</span>
            <span>制表人:</span>
            <span>打印日期:{{ $moment().format('YYYY-MM-DD') }}</span>
          </div>
        </el-table>
      </div>

      <div class="total-box">
        <span>共 {{ tableData.length }} 条</span>
      </div>

      <edit-formula
        :edit-formula-visible="showEditFormula"
        :current-item="editItem"
        sheet-type="cashSheet"
        @editFormulaClose="closeEditFormula"
        @save-success="editSuccess"
      />

      <!-- 检查是否平衡的弹窗 -->
      <el-dialog
        :visible.sync="dialogVisible"
        :close-on-click-modal="false"
        class="balance-dialog"
        title="系统提示"
        width="40%">
        <div class="balance-content">
          <div style="height: 34px;">
            <div class="el-message-box__status el-icon-warning" />
          </div>
          <div class="text-box">
            <div v-if="judgeStep.includes(1)" class="text">资产负债表不平,请检报表项目公式设置！</div>
            <div v-if="judgeStep.includes(2)" class="text">您有未设置报表项目的科目：</div>
            <div v-if="judgeStep.includes(2)" class="subject-box">
              <div v-for="(item,index) in subjectList" :key="index" class="subject-item">
                · {{ item.number }} {{ item.subjectName }}
              </div>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="targetBalance">立即检查</el-button>
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  cashFlowStatementReportAPI,
  updateCashFlowReportAPI,
  cashFlowStatementExtendAPI,
  updateCashFlowExtendAPI,
  cashFlowCheckAPI,
  exportCashFlowStatementReportAPI
} from '@/api/fm/report'

import EditFormula from './components/EditFormula'
import ChooseQuarter from './components/ChooseQuarter'

import { objDeepCopy, downloadExcelWithResData } from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { mapGetters } from 'vuex'
import { evaluate } from 'mathjs'
import PrintMixin from '@/views/fm/mixins/Print.js'

export default {
  name: 'CashSheet',
  components: {
    EditFormula,
    ChooseQuarter
  },
  filters: {
    filterValue(val) {
      const num = Number(val)
      if (isNaN(num) || num === 0) return ''
      return separator(num)
    }
  },
  mixins: [PrintMixin],
  data() {
    return {
      pageTitle: '现金流量表',
      step: 1, // 步骤数
      type: 1, // 1 月报 2 季报
      dateValue: '', // 年月
      yearMounth: {
        fromPeriod: '',
        toPeriod: ''
      },
      tableData: [], // 表格列表的数据
      submitBeforeData: [], // 更改前的数据
      showEditFormula: false, // 编辑公式的弹窗
      editItem: {}, // 编辑公式的当前项
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度

      dialogVisible: false,
      judgeStep: [], // 判断哪些原因不平衡
      subjectList: [], // 不平衡的科目列表
      printFooterShow: false
    }
  },
  computed: {
    ...mapGetters(['financeCurrentAccount', 'financeFilterTimeRange']),
    pickerOptions() {
      return {
        disabledDate: date => {
          const current = this.$moment(date)
          return current.isBefore(this.financeFilterTimeRange.minTime.timeObj) ||
            current.isAfter(this.financeFilterTimeRange.maxTime.timeObj)
        }
      }
    },
    // 按钮列表
    btnList() {
      let lib = []
      if (this.step === 1) {
        lib = [
          { name: '调整', type: 'adjust', plain: false, primary: 'primary', auth: 'finance.cashFlow.update' }
        ]
      } else if (this.step === 2) {
        lib = [
          // { name: '保存', type: 'save2', plain: false, primary: 'primary', auth: '' },
          { name: '下一步', type: 'next2', plain: true, primary: '', auth: '' },
          { name: '清空并重算', type: 'clear2', plain: true, primary: '', auth: '' },
          { name: '返回', type: 'return2', plain: true, primary: '', auth: '' }
        ]
      } else {
        lib = [
          { name: '保存', type: 'save3', plain: false, primary: 'primary', auth: '' },
          { name: '上一步', type: 'return3', plain: true, primary: '', auth: '' },
          { name: '清空并重算', type: 'clear3', plain: true, primary: '', auth: '' }
        ]
      }
      return lib.filter(item => {
        if (!item.auth) return true
        return this.$auth(item.auth)
      })
    },
    // 表格字段列表
    fieldList() {
      if (this.step === 2) {
        return [
          { label: '项目', field: 'name', align: 'left' },
          { label: '行次', field: 'sort', align: 'center', width: '100px' },
          { label: '本月数', field: 'monthValue', align: 'right', width: '260px' },
          { label: '本年数', field: 'yearValue', align: 'right', width: '260px' }
        ]
      } else {
        return [
          { label: '项目', field: 'name', align: 'left' },
          { label: '行次', field: 'sort', align: 'center', width: '100px' },
          { label: '本年累计金额', field: 'yearValue', align: 'right', width: '260px' },
          { label: '本月金额', field: 'monthValue', align: 'right', width: '260px' }
        ]
      }
    },
    mounthNum() {
      if (this.financeCurrentAccount && this.financeCurrentAccount.startTime) {
        const mounthNum = this.financeCurrentAccount.startTime.substring(0, 7)
        var time1 = mounthNum.split('-')
        console.log(`${time1[0]}${time1[1]}`)
        return `${time1[0]}${time1[1]}`
      }
      return ''
    }
  },
  watch: {
    step: {
      handler(val) {
        console.log('账期先后', this.dateValue)
        if (val === 2) {
          this.getCashFlowStatementList()
        } else {
          this.getCashSheetList()
        }
      }
      // deep: true,
      // immediate: true
    }
  },
  created() {
    // console.log('MathJS:----', MathJS)
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
    // 获取当月时间
    if (this.mounthNum) {
      this.dateValue = this.mounthNum
      // console.log('账期', this.dateValue)
      this.getCashSheetList()
      this.judegBalance()
    }
  },
  methods: {
    /**
     * 获取数据框是否禁用
     * @param row
     * @return {*}
     */
    getDisabledStatus(row) {
      return row.formula.includes('L')
    },

    /**
     * 获取缩进距离
     * @param row
     * @return {string}
     */
    getNameIndent(row) {
      if (!row.grade) return '0'
      const grade = Number(row.grade)
      if (
        isNaN(grade) ||
        grade - 1 < 0
      ) return '0'
      return `${(row.grade - 1) * 14}px`
    },

    /**
     * 通过规则获取对应要计算的值
     * @param rule
     * @param column
     * @return {number}
     */
    getValueByRule(rule, column) {
      const sort = Number(rule.replace('L', ''))
      if (isNaN(sort) || !column || !column.property) return 0
      const findRes = this.tableData.find(o => o.sort === sort)
      if (!findRes) return 0
      return findRes[column.property] || 0
    },

    /**
     * 输入金额变化时根据公式自动计算
     * @param row
     * @param column
     */
    inputChange(row, column) {
      const current = `L${row.sort}`
      const arr = this.tableData.filter(o => o.formula.includes(current))
      arr.forEach(item => {
        try {
          let formula = JSON.parse(item.formula)[0]
          const ruleArr = formula.split(/[+\-*\/\(\)\[\]]/g).filter(o => Boolean(o))
          ruleArr.forEach(ruleItem => {
            const val = this.getValueByRule(ruleItem, column)
            const reg = new RegExp(`${ruleItem}(?!\\d)`, 'g')
            formula = formula.replace(reg, val)
          })
          this.$set(item, column.property, evaluate(formula))
        } catch (err) {
          console.log(err)
        }
      })
    },

    /**
     * @description: 判断资产负债表是否平衡
     */
    judegBalance() {
      cashFlowCheckAPI({
        fromPeriod: this.dateValue,
        toPeriod: this.dateValue,
        type: this.type
      })
        .then(res => {
          console.log('平衡检查', res)
          if (res.data.balanceSheet === false) {
            this.dialogVisible = true
            this.judgeStep.push(1)
          }
          if (res.data.notContains.length > 0) {
            this.dialogVisible = true
            this.judgeStep.push(2)
            this.subjectList = res.data.notContains
          }
        })
        .catch(() => {})
    },
    /**
     * @description: 跳转资产负债表
     */
    targetBalance() {
      this.dialogVisible = false
      this.$router.push({ path: '/fm/report/subs/balanceSheet' })
    },
    /**
     * @description: 重置按钮事件
     */
    resSearch() {
      this.getCashSheetList()
    },
    /**
     * @description: 点击刷新按钮
     */
    refreshClick() {
      if (this.type === 1) {
        this.getCashSheetList()
      }
    },
    /**
     * @description: 根据季度获取的年月
     * @param {*} start
     * @param {*} end
     */
    getYearMounth(start, end) {
      this.yearMounth = {
        fromPeriod: start,
        toPeriod: end
      }
      this.getCashSheetList()
    },
    /**
     * @description: 现金流量列表
     */
    getCashSheetList() {
      this.loading = true
      const params = {
        fromPeriod: '',
        toPeriod: '',
        type: this.type
      }
      if (this.type === 2) {
        params.fromPeriod = this.yearMounth.fromPeriod
        params.toPeriod = this.yearMounth.toPeriod
      } else {
        params.fromPeriod = this.dateValue
        params.toPeriod = this.dateValue
      }
      cashFlowStatementReportAPI(params)
        .then(res => {
          // console.log(res)
          this.loading = false
          this.tableData = res.data
        })
        .catch(() => {
          this.tableData = []
          this.loading = false
        })
    },
    /**
     * @description: 获取 调整辅助数据项列表
     */
    getCashFlowStatementList() {
      this.loading = true
      console.log(this.type)
      const params = {
        fromPeriod: '',
        toPeriod: '',
        type: this.type,
        category: 1
      }
      if (this.type === 2) {
        params.fromPeriod = this.yearMounth.fromPeriod
        params.toPeriod = this.yearMounth.toPeriod
      } else {
        params.fromPeriod = this.dateValue
        params.toPeriod = this.dateValue
      }
      cashFlowStatementExtendAPI(params)
        .then(res => {
          console.log('辅助列表', res)
          this.loading = false
          this.tableData = res.data
          this.$nextTick(() => {
            this.submitBeforeData = objDeepCopy(this.tableData)
          })
        })
        .catch(() => {
          this.tableData = []
          this.loading = false
        })
    },
    printAll() {
      this.HandlerPrint('cashSheet', { title: 'cashSheet' })
    },
    /**
     * @description: 编辑辅助列表
     * @param {[boolean]} next
     * @return {*}
     */
    updateCashFlowExtend(next = false) {
      const paramsList = []
      for (let i = 0; i < this.tableData.length; i++) {
        const item = this.tableData[i]
        if (JSON.stringify(item) !== JSON.stringify(this.submitBeforeData[i])) {
          paramsList.push(item)
        }
      }
      updateCashFlowExtendAPI({
        dataList: paramsList,
        currentPeriod: this.dateValue
      })
        .then(res => {
          console.log('保存辅助', res)
          this.loading = false
          if (next) {
            this.step += 1
          } else {
            this.getCashFlowStatementList()
          }
          this.$message.success('保存成功')
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * @description: 按钮的点击
     * @param {string} type
     */
    btnClick(type) {
      if (type === 'adjust') {
        this.step = 2
      } else if (type === 'export') {
        const params = {
          fromPeriod: '',
          toPeriod: '',
          type: this.type
        }
        if (this.type === 2) {
          params.fromPeriod = this.yearMounth.fromPeriod
          params.toPeriod = this.yearMounth.toPeriod
        } else {
          params.fromPeriod = this.dateValue
          params.toPeriod = this.dateValue
        }
        exportCashFlowStatementReportAPI(params).then(res => {
          downloadExcelWithResData(res)
        })
      } else if (type === 'save2') {
        // 保存 编辑的辅助表
        this.updateCashFlowExtend()
      } else if (type === 'next2') {
        // this.step = 3
        this.updateCashFlowExtend(true)
      } else if (type === 'clear2') {
        // console.log(type);
        this.tableData.forEach(el => {
          if (el.editable) {
            el.monthValue = 0
            el.yearValue = 0
          }
        })
      } else if (type === 'return2') {
        this.step = 1
      } else if (type === 'save3') {
        // 保存 编辑的现金流量表
        const params = {
          fromPeriod: '',
          toPeriod: '',
          type: this.type,
          dataList: this.tableData
        }
        if (this.type === 2) {
          params.fromPeriod = this.yearMounth.fromPeriod
          params.toPeriod = this.yearMounth.toPeriod
        } else {
          params.fromPeriod = this.dateValue
          params.toPeriod = this.dateValue
        }
        updateCashFlowReportAPI(params)
          .then(res => {
            // console.log('保存辅助', res)
            this.$message.success('保存成功')
            this.loading = false
            // 返回第一步
            this.step = 1
            // this.getCashSheetList()
          })
          .catch(() => {
            this.loading = false
          })
      } else if (type === 'return3') {
        this.step = 2
      } else if (type === 'clear3') {
        this.tableData.forEach(el => {
          if (el.editable) {
            el.monthValue = 0
            el.yearValue = 0
          }
        })
      }
    },

    /**
     * @description: 公式编辑成功 刷新数据
     * @param {*}
     * @return {*}
     */
    editSuccess() {
      this.showEditFormula = false

      this.getCashFlowStatementList()
    },
    // 打开编辑公式弹窗
    openEditFormula(row) {
      if (row.editable && this.step === 2) {
        this.showEditFormula = true
        this.editItem = row
      }
    },
    /**
     * 关闭编辑公式
     */
    closeEditFormula() {
      this.showEditFormula = false
    },
    /**
     * @description: 通过回调控制class
     * @param {*} row
     * @param {*} column
     * @param {*} rowIndex
     * @param {*} columnIndex
     */
    cellClassName({ row, column, rowIndex, columnIndex }) {
      if (column.name === 'name') {
        // return 'can-visit--underline can-visit--bold'
      }
    }

  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.app-container {
  padding: 15px 40px 0;

  .main {
    position: relative;

    &-header {
      height: 32px;
    }

    &-title {
      height: 100%;
      font-size: $--font-size-xxlarge;
      color: $--color-black;
    }

    .content-flex {
      box-sizing: border-box;
      width: 100%;
      padding: 24px 0;

      .buttons {
        float: right;
        width: auto;

        .el-button {
          margin-left: $--button-padding-vertical;
        }
      }

      .left-box {
        .select-date {
          margin: 0 $--button-padding-vertical;
        }
      }
    }

    .table-wrapper {
      ::v-deep .name-text {
        display: inline-block;
        margin-right: 5px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        vertical-align: middle;
      }

      .cell {
        .wk {
          visibility: hidden;
        }
      }

      .cell:hover {
        .wk {
          visibility: visible;
        }
      }

      .name-box {
        position: relative;
        display: block;
        width: 100%;

        // cursor: pointer;
        padding-right: 24px;

        .wk-icon-modify {
          position: absolute;
          top: 0;
          right: 0;
          bottom: 0;
          color: #999;
          cursor: pointer;
          visibility: hidden;
        }

        &:hover {
          .wk-icon-modify {
            visibility: visible;
          }
        }
      }
    }

    .total-box {
      display: flex;
      justify-content: flex-end;
      padding: 0 30px;
    }
  }
}

.balance-dialog {
  .balance-content {
    display: flex;
    align-items: center;
    justify-content: flex-start;

    .icon-box {
      // width: 20%;
    }

    .text-box {
      margin-left: 10px;

      .text {
        margin-bottom: 15px;

        .target-text {
          color: #2362fb;
          text-decoration: underline;
          cursor: pointer;
        }
      }

      .subject-box {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        justify-content: space-between;

        .subject-item {
          width: 48%;
        }
      }
    }
  }
}

.el-message-box__status::before {
  padding-left: 1px;
}

.el-icon-warning::before {
  content: "\E7A3";
}

.el-message-box__status.el-icon-warning {
  color: #e6a23c;
}

.el-message-box__status {
  position: relative;
  top: 33%;
}

</style>
