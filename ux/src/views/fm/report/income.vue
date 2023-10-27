<template>
  <div class="app-container">
    <div class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          {{ pageTitle }}
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="40"
            data-id="320" />
        </span>
        <el-dropdown
          trigger="click"
          class="receive-drop">
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-if="$auth('finance.profit.print')" @click.native="handleToPrint">打印</el-dropdown-item>
            <el-dropdown-item v-if="$auth('finance.profit.export')" @click.native="exportTab">导出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="left-box">
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
            format="yyyy第MM期"
            value-format="yyyyMM"
            placeholder="选择月"
            class="select-date"
            @change="refreshClick" />
          <!-- 选择季度 -->
          <choose-quarter
            v-if="type==2"
            :default-value="dateValue"
            :get-value="getYearMounth" />
          <el-button
            v-debounce="resSearch"
            type="subtle"
            icon="el-icon-refresh-right" />
        </flexbox>
      </flexbox>

      <div v-loading="loading" class="table-wrapper">
        <el-table
          id="incomeSheet"
          :data="balabceData"
          :height="tableHeight"
          :cell-class-name="cellClassName"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
          row-key="id"
          border
          default-expand-all
          style="width: 100%;"
          stripe>
          <el-table-column
            v-for="(item, index) in balabceList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            :align="item.align"
            :formatter="fieldFormatter"
            show-overflow-tooltip>
            <template slot-scope="{row}">
              <template v-if="item.field === 'name'">
                <div
                  :style="{paddingLeft: getNameIndent(row)}"
                  class="name-box text-one-ellipsis"
                >
                  {{ row[item.field] }}
                  <i
                    v-if="$auth('finance.profit.update') && row.editable"
                    class="wk wk-icon-modify"
                    @click="openEditIncomFormula(row)" />
                </div>
              </template>
              <template v-else-if="['yearValue','monthValue'].includes(item.field)">
                <span>{{ row[item.field] | filterValue }}</span>
              </template>
              <template v-else>
                <!-- <span>{{ row[item.field] }}</span> -->
                <span>{{ row[item.field] == 0 ? '' : row[item.field] }}</span>
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
        <span>共 {{ balabceData.length }} 条</span>
      </div>

      <!-- 编辑公式组件 -->
      <edit-incom-formula
        :edit-formula-visible="showEditIncomFormula"
        :current-item="editItem"
        @save-success="editSuccess"
        @editFormulaClose="closeEditIncomFormula"
      />

      <!-- 检查是否平衡的弹窗 -->
      <el-dialog
        v-if="verifyResult"
        :visible.sync="dialogVisible"
        :close-on-click-modal="false"
        class="balance-dialog"
        title="系统提示"
        width="40%">
        <div class="balance-content">
          <div class="icon-box">
            <div class="el-message-box__status el-icon-warning" />
          </div>
          <div class="text-box">
            <div v-if="!verifyResult.balanced" class="text">结转损益凭证手工录入或还未生成结转损益凭证</div>
            <template v-if="verifyResult.notContains.length > 0">
              <div class="text">您有未设置报表项目的科目：</div>
              <div class="subject-box">
                <div
                  v-for="(item,index) in verifyResult.notContains"
                  :key="index"
                  class="subject-item">
                  · {{ item.number }} {{ item.subjectName }}
                </div>
              </div>
            </template>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="jumpBalanceSheet">立即检查</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { incomeStatementReportAPI, incomeCheckAPI, exportIncomeStatementReportAPI } from '@/api/fm/report'

import EditIncomFormula from './components/EditIncomFormula'
import ChooseQuarter from './components/ChooseQuarter'

import { downloadExcelWithResData } from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { mapGetters } from 'vuex'
import PrintMixin from '@/views/fm/mixins/Print.js'

export default {
  name: 'IncomeSheet',
  components: {
    EditIncomFormula,
    ChooseQuarter
  },
  filters: {
    filterValue(val) {
      return val == 0 ? '' : separator(val)
    }
  },
  mixins: [PrintMixin],
  data() {
    return {
      pageTitle: '利润表',
      type: 1, // 1 月报 2 季报
      dateValue: '', // 年月
      yearMounth: {
        fromPeriod: '',
        toPeriod: ''
      },
      // 表格字段列表
      balabceList: [
        { label: '项目', field: 'name', align: 'left' },
        { label: '行次', field: 'sort', align: 'center' },
        { label: '本年累计金额', field: 'yearValue', align: 'right' },
        { label: '本月金额', field: 'monthValue', align: 'right' }
      ],
      balabceData: [], // 列表数据
      showEditIncomFormula: false, // 编辑公式的弹窗
      editItem: {}, // 编辑公式的当前项

      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      currencyOptions: ['rmb', 'sss'],

      dialogVisible: false,
      verifyResult: null,
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
    mounthNum() {
      this.dateValue = this.mounthNum
      this.getIncomeSheetList()
    }
  },
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
    // 获取当月时间
    if (this.mounthNum) {
      this.dateValue = this.mounthNum
      this.verifyBalance()
      this.getIncomeSheetList()
    }
  },
  methods: {
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
     * 判断资产负债表是否平衡
     */
    verifyBalance() {
      this.dialogVisible = false
      this.verifyResult = null
      incomeCheckAPI({
        fromPeriod: this.dateValue,
        toPeriod: this.dateValue,
        type: this.type
      })
        .then(res => {
          this.verifyResult = res.data || {}
          if (
            !this.verifyResult.balanced ||
            this.verifyResult.notContains.length > 0
          ) {
            this.dialogVisible = true
          }
        })
        .catch(() => {})
    },

    /**
     * @description: 重置按钮事件
     */
    resSearch() {
      this.getIncomeSheetList()
    },
    /**
     * @description: 点击刷新按钮
     */
    refreshClick() {
      if (this.type == 1) {
        this.getIncomeSheetList()
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
      this.getIncomeSheetList()
    },
    /**
     * @description: 获取列表
     */
    getIncomeSheetList() {
      this.loading = true
      const params = {
        fromPeriod: '',
        toPeriod: '',
        type: this.type
      }
      if (this.type == 2) {
        params.fromPeriod = this.yearMounth.fromPeriod
        params.toPeriod = this.yearMounth.toPeriod
      } else {
        params.fromPeriod = this.dateValue
        params.toPeriod = this.dateValue
      }
      incomeStatementReportAPI(params)
        .then(res => {
          this.loading = false
          this.balabceData = res.data
        })
        .catch(() => {
          this.loading = false
        })
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
    },
    /**
     * 列表格式化
     */
    fieldFormatter(row, column) {

    },

    handleToPrint() {
      this.HandlerPrint('incomeSheet', { title: '利润表' })
    },

    // 导出tab列表
    exportTab() {
      const params = {
        fromPeriod: '',
        toPeriod: '',
        type: this.type
      }
      if (this.type == 2) {
        params.fromPeriod = this.yearMounth.fromPeriod
        params.toPeriod = this.yearMounth.toPeriod
      } else {
        params.fromPeriod = this.dateValue
        params.toPeriod = this.dateValue
      }
      exportIncomeStatementReportAPI(params).then(res => {
        downloadExcelWithResData(res)
      })
    },

    // 公式编辑成功 刷新数据
    editSuccess() {
      this.showEditIncomFormula = false
      this.getIncomeSheetList()
    },
    // 打开编辑公式弹窗
    openEditIncomFormula(row) {
      this.showEditIncomFormula = true
      this.editItem = row
    },
    /**
     * 关闭编辑公式
     */
    closeEditIncomFormula() {
      this.showEditIncomFormula = false
    },
    // 立即检查
    jumpBalanceSheet() {
      this.dialogVisible = false
      this.$router.push({ path: '/fm/report/subs/balanceSheet' })
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
      ::v-deep.name-text {
        display: inline-block;
        width: 85%;
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
        padding-right: 24px;
        cursor: pointer;

        .wk-icon-modify {
          position: absolute;
          top: 0;
          right: 0;
          bottom: 0;
          color: #999;
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

    .icon-box {
      position: relative;
      width: 25px;

      .el-icon-warning {
        position: absolute;
        top: 6px;
      }
    }

    .text-box {
      margin-left: 10px;

      .text {
        margin-bottom: 15px;
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
  top: 8%;
}
</style>
