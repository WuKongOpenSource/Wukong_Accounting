<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          资产负债表
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="40"
            data-id="319" />
        </span>
        <el-dropdown
          trigger="click"
          class="receive-drop">
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-if="$auth('finance.balanceSheet.print')" @click.native="printClick">打印</el-dropdown-item>
            <el-dropdown-item v-if="$auth('finance.balanceSheet.export')" @click.native="exportTab">导出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="left-box">
          <el-date-picker
            v-model="dataTime"
            :picker-options="pickerOptions"
            :clearable="false"
            format="yyyy 第 MM 期"
            value-format="yyyyMM"
            type="month"
            placeholder="选择月" />
          <el-button
            v-debounce="handleRefresh"
            type="subtle"
            icon="el-icon-refresh-right" />
        </flexbox>
      </flexbox>

      <div class="table-wrapper">
        <el-table
          id="balanceSheet"
          :data="tableData"
          :height="tableHeight"
          border
          stripe>
          <el-table-column
            v-for="(item, index) in fieldList"
            :key="index"
            :prop="item.field"
            :align="item.align"
            :label="item.label"
            :width="item.width"
            show-overflow-tooltip>
            <template slot-scope="{ row,column }">
              <template v-if="['name','name0'].includes(item.field)">
                <div
                  :style="{paddingLeft: getNameIndent(row, item.field)}"
                  class="name-box text-one-ellipsis"
                >
                  {{ row[item.field] }}
                  <i
                    v-if="$auth('finance.balanceSheet.update')"
                    v-show="column.property === 'name' ? row.editable : row.editable0"
                    class="wk wk-icon-modify"
                    @click="openEditFormula(row,column)" />
                </div>
              </template>
              <template
                v-else-if="[
                  'endPeriod',
                  'initialPeriod',
                  'endPeriod0',
                  'initialPeriod0'].includes(item.field)">
                <span>{{ row[item.field] | filterValue }}</span>
              </template>
              <template v-else>
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
        <span>共 {{ tableData.length }} 条</span>
      </div>

      <!-- 编辑公式组件 -->
      <edit-formula
        :edit-formula-visible="showEditFormula"
        :current-item="editItem"
        sheet-type="balanceSheet"
        @editFormulaClose="closeEditFormula"
        @save-success="editSuccess" />

      <!-- 检查是否平衡的弹窗 -->
      <el-dialog
        v-if="verifyResult || !balance"
        :visible.sync="dialogVisible"
        :close-on-click-modal="false"
        class="balance-dialog"
        title="系统提示"
        width="40%">
        <div class="balance-content">
          <div class="text-box">
            <span class="el-icon-warning" />
            <span>{{ balanceMsg }}</span>
          </div>
          <div v-if="verifyResult&&!verifyResult.initialBalance" class="text-box">
            <span>财务初始余额不平,可通过</span>
            <span
              class="can-visit--underline"
              @click="initialBalance">财务初始余额</span>
            <span>修改</span>
          </div>
          <div v-if="verifyResult&&!verifyResult.settled" class="text-box">
            <span>本期尚未结转损益,可通过</span>
            <span
              class="can-visit--underline"
              @click="handleToSettle">结账</span>
            <span>自动结转</span>
          </div>
          <div v-if="verifyResult&&verifyResult.notContains.length > 0" class="text-box">
            <div class="text">您有未设置报表项目的科目：</div>
            <div class="subject-box">
              <div
                v-for="(item,index) in verifyResult.notContains"
                :key="index"
                class="subject-item">
                · {{ item.number }} {{ item.subjectName }}
              </div>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">忽略</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { balanceSheetReportAPI, balanceCheckAPI, exportBalanceSheetReportAPI } from '@/api/fm/report'
import { fmFinanceInitialTrialBalanceAPI } from '@/api/fm/setting'

import EditFormula from './components/EditFormula'

import { downloadExcelWithResData } from '@/utils'

import { separator } from '@/filters/vueNumeralFilter/filters'
import { mapGetters } from 'vuex'
import PrintMixin from '@/views/fm/mixins/Print.js'

export default {
  name: 'BalanceSheet',
  components: {
    EditFormula
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
      pageTitle: '资产负债表',
      loading: false,
      type: 1, // 1 月报 2 季报
      dateValue: [],
      dataTime: '',

      tableHeight: document.documentElement.clientHeight - 240,
      fieldList: [
        { label: '资产', field: 'name', align: 'left', width: 150 },
        { label: '行次', field: 'sort', align: 'center', width: 80 },
        { label: '期末数', field: 'endPeriod', align: 'right' },
        { label: '年初数', field: 'initialPeriod', align: 'right' },
        { label: '负债和所有者权益', field: 'name0', align: 'left', width: 150 },
        { label: '行次', field: 'sort0', align: 'center', width: 80 },
        { label: '期末数', field: 'endPeriod0', align: 'right' },
        { label: '年初数', field: 'initialPeriod0', align: 'right' }
      ],
      tableData: [],
      editItem: {}, // 编辑公式的当前项

      showEditFormula: false, // 编辑公式的弹窗
      balance: true,
      dialogVisible: false, // 检查是否平衡的弹窗
      verifyResult: null,
      printFooterShow: false
    }
  },
  computed: {
    ...mapGetters(['financeCurrentAccount', 'financeFilterTimeRange']),
    balanceMsg() {
      const verifyResult = this.verifyResult && (this.verifyResult.initialBalance || !this.verifyResult.settled || this.verifyResult.notContains.length > 0)
      return `${!this.balance ? '资产负债表不平,' : ''}${verifyResult ? '请检查财务处理、' : ''}${!this.balance ? '报表项目公式设置' : ''}`
    },
    pickerOptions() {
      return {
        disabledDate: date => {
          const current = this.$moment(date)
          return current.isBefore(this.financeFilterTimeRange.minTime.timeObj) ||
            current.isAfter(this.financeFilterTimeRange.maxTime.timeObj)
        }
      }
    }
  },
  watch: {
    dataTime() {
      this.handleRefresh()
    }
  },
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
    if (this.financeCurrentAccount && this.financeCurrentAccount.startTime) {
      this.dataTime = this.$moment(this.financeCurrentAccount.startTime).format('YYYYMM')
    }
    // this.dateValue = [time, time]
    // this.getData()
  },
  methods: {
    /**
     * @description: 获取数据
     */
    getData() {
      this.loading = true
      balanceSheetReportAPI({
        fromPeriod: this.dataTime,
        toPeriod: this.dataTime,
        type: this.type
      }).then(res => {
        this.loading = false
        this.tableData = this.formatData(res.data || [])
        if (this.tableData.length) {
          const row = this.tableData[this.tableData.length - 1]
          const period = (row.endPeriod + row.initialPeriod) === (row.endPeriod0 + row.initialPeriod0)
          this.balance = period
          if (!period) {
            this.dialogVisible = true
          }
        }
      }).catch(() => {
        this.loading = false
      })
    },

    /**
     * @description: 格式化数据
     * @param {object[]} list
     */
    formatData(list) {
      const result = []
      const keys = [
        'editable',
        'endPeriod',
        'formula',
        'grade',
        'initialPeriod',
        'sort',
        'id',
        'name'
      ]
      let ids = list.map(o => o.rowId)
      ids = Array.from(new Set(ids))
      ids.forEach(id => {
        const findArr = list.filter(o => o.rowId === id)
        const item1 = findArr[0]
        const item2 = findArr[1]
        if (item1) {
          const obj = {}
          keys.forEach(key => {
            obj[`${key}0`] = item2 ? item2[key] : null
          })
          result.push({
            ...item1,
            ...obj
          })
        }
      })
      // console.log('result: ', result)
      return result
    },

    /**
     * 资产负债表平衡检查
     */
    checkBalance() {
      this.verifyResult = null
      this.dialogVisible = false
      Promise.all([
        balanceCheckAPI({
          fromPeriod: this.dataTime,
          toPeriod: this.dataTime,
          type: this.type
        }),
        fmFinanceInitialTrialBalanceAPI()
      ]).then(resArr => {
        const result = resArr[0].data || {}
        result.initialBalance = Number(resArr[1].data.trialResult) === 1
        if (
          !result.settled ||
          !result.initialBalance ||
          result.notContains.length > 0
        ) {
          this.verifyResult = result
          this.dialogVisible = true
        }
      }).catch(() => {})
    },

    /**
     * 刷新
     */
    handleRefresh() {
      this.getData()
      this.checkBalance()
    },

    /**
     * @description: 财务初始化余额
     */
    initialBalance() {
      this.dialogVisible = false
      this.$router.push({
        path: '/fm/manage/subs/manage-initial-balance-set'
      })
    },

    /**
     * 去结账
     */
    handleToSettle() {
      this.$router.push('/fm/settleAccounts')
    },

    /**
     * 获取缩进距离
     * @param row
     * @param field
     * @return {string}
     */
    getNameIndent(row, field) {
      if (!row.grade && !row.grade0) return '0'
      const grade = field === 'name' ? Number(row.grade) : Number(row.grade0)
      if (
        isNaN(grade) ||
        grade - 1 < 0
      ) return '0'
      return `${(grade - 1) * 14}px`
    },

    /**
     * @description: 打印
     */
    printClick() {
      this.HandlerPrint('balanceSheet', { title: '资产负债表' })
    },

    /**
     * @description: 导出tab列表
     */
    exportTab() {
      exportBalanceSheetReportAPI({
        fromPeriod: this.dataTime,
        toPeriod: this.dataTime,
        type: this.type,
        exportDataList: this.tableData
      }).then(res => {
        downloadExcelWithResData(res)
      })
    },

    /**
     * @description: 公式比编辑成功 刷新数据
     */
    editSuccess() {
      this.showEditFormula = false
      this.getData()
    },

    /**
     * @description: 打开编辑公式弹窗
     * @param {*} row
     * @param {*} column
     */
    openEditFormula(row, column) {
      const flag = column.property === 'name'
      this.editItem = {
        id: flag ? row.id : row.id0,
        sort: flag ? row.sort : row.sort0,
        name: flag ? row.name : row.name0,
        formula: flag ? row.formula : row.formula0,
        endPeriod: flag ? row.endPeriod : row.endPeriod0,
        initialPeriod: flag ? row.initialPeriod : row.initialPeriod0
      }
      this.showEditFormula = true
    },

    /**
     * 关闭编辑公式
     */
    closeEditFormula() {
      this.showEditFormula = false
    }
  }
}
</script>

<style lang="scss" scoped>
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
        .el-button {
          margin-left: $--button-padding-vertical;
        }
      }
    }

    ::v-deep .el-table {
      &.el-table--border::after {
        display: none;
      }
    }

    .table-wrapper {
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
    .text-box {
      margin-bottom: 10px;
      margin-left: 10px;

      .el-icon-warning {
        font-size: 24px;
        color: #e6a23c;
      }

      span {
        vertical-align: middle;
      }

      .subject-box {
        padding-left: 15px;
        margin-top: 5px;

        .subject-item {
          line-height: 1.5;
        }
      }
    }
  }
}

.text-item-left {
  margin-left: 30px;
}
</style>
