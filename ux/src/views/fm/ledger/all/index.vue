<template>
  <div class="app-container">
    <div
      v-loading="loading"
      class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          总账
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="311" />
        </span>
        <div class="buttons">
          <!-- <el-button
              v-if="$auth('finance.subject.print')"
              type="primary">打印</el-button> -->
          <el-dropdown
            trigger="click"
            class="receive-drop"
          >
            <el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-if="$auth('finance.generalLedger.export')" @click.native="print">打印</el-dropdown-item>
              <el-dropdown-item v-if="$auth('finance.generalLedger.export')" @click.native="exportTab">导出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <div>
          <flexbox>
            <search-popover-wrapper :search-time.sync="searchTime">
              <total-search
                :default-value="searchForm"
                @change="handleToSearch" />
            </search-popover-wrapper>
            <el-button
              type="subtle"
              style="margin-left: 8px;"
              icon="el-icon-refresh-right"
              @click="getData()" />
          </flexbox>
        </div>

      </flexbox>
      <div class="table-wrapper">
        <el-table
          id="allLedger"
          :header-cell-style="{'text-align':'center'}"
          :data="tableData"
          :height="tableHeight"
          :span-method="objectSpanMethod"
          border
          stripe>
          <el-table-column
            v-for="(item, index) in balabceList"
            :key="index"
            :align="item.hasOwnProperty('type')&&item.type?'right':'center'"
            :prop="item.field"
            :label="item.label"
            show-overflow-tooltip>
            <template slot-scope="{row}">
              <span
                v-if="item.field=='number'"
                class="can-visit--underline"
                @click="clickTime(row)">
                {{ row[item.field] }}
              </span>
              <span v-else>{{ row[item.field] }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { queryDetailUpAccount, exportDetailUpAccountAPI } from '@/api/fm/ledger'
import { downloadExcelWithResData } from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'
// 多选
import SearchPopoverWrapper from '@/views/fm/components/SearchPopoverWrapper'
import TotalSearch from './components/totalSearch'
import PrintMixin from '@/views/fm/mixins/Print.js'

// // 定义不需要双向绑定的值
// const balabceList = Object.freeze([
//   { label: '科目编码', field: 'number' },
//   { label: '科目名称', field: 'subjectName' },
//   { label: '期间', field: 'time' },
//   { label: '摘要', field: 'digestContent' },
//   { label: '借方', field: 'debtorBalance', type: true },
//   { label: '贷方', field: 'creditBalance', type: true },
//   { label: '方向', field: 'balanceDirection' },
//   { label: '余额', field: 'balance', type: true }
// ])
// console.log('表头数据', balabceList)

export default {
  name: 'FinanceAllLedger',
  components: {
    SearchPopoverWrapper,
    TotalSearch
  },
  mixins: [PrintMixin],
  props: {},
  // 组件数据
  data() {
    return {
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      balabceList: [
        { label: '科目编码', field: 'number' },
        { label: '科目名称', field: 'subjectName' },
        { label: '期间', field: 'time' },
        { label: '摘要', field: 'digestContent' },
        { label: '借方', field: 'debtorBalance', type: true },
        { label: '贷方', field: 'creditBalance', type: true },
        { label: '方向', field: 'balanceDirection' },
        { label: '余额', field: 'balance', type: true }],
      /** 总账table数据 */
      tableData: [],
      // 查询数据的参数
      searchForm: { },
      timeArray: [],

      rowSpanArr: []
    }
  },
  // 计算属性
  computed: {
  },
  watch: {
  },

  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
  },
  mounted() {
  },
  // 组件方法
  methods: {
    print() {
      this.HandlerPrint('allLedger', { title: '总账' })
    },
    /**
     * @description: 导出tab列表
     */
    exportTab() {
      exportDetailUpAccountAPI(this.searchForm).then((res) => {
        downloadExcelWithResData(res)
      })
    },

    /**
     * @description: 传过来的搜索条件
     * @param {*} data
     * @return {*}
     */
    handleToSearch(data) {
      this.timeArray = data.time
      delete data.time
      this.searchForm = data
      this.getData()
    },

    /**
     * @description: 获取列表数据
     */
    getData() {
      this.loading = true
      queryDetailUpAccount(this.searchForm).then(res => {
        this.loading = false
        const arr = ['debtorBalance', 'creditBalance', 'balance']
        res.data.forEach(item => {
          arr.forEach(key => {
            if (item.hasOwnProperty(key)) {
              if (!item[key] || isNaN(Number(item[key]))) {
                item[key] = ''
              } else {
                item[key] = separator(item[key])
              }
            }
            item.time = item.accountTime || item.monthTime || ''
          })
        })

        this.getRowSpan(res.data || [])
        this.tableData = res.data
      })
    },

    /**
     * @description: 获取行数据
     * @param {object[]} list
     */
    getRowSpan(list) {
      const rowSpanArr = Array(list.length).fill(0)

      let rowSpan = 0
      let prevNumber = null
      list.forEach((item, index) => {
        if (index === 0) {
          // 第一行
          prevNumber = item.number
        } else if (index === list.length - 1) {
          // 最后一行
          if (prevNumber === item.number) {
            rowSpanArr[index - rowSpan] = rowSpan + 1
          } else {
            rowSpanArr[index - rowSpan] = 1
          }
        } else {
          if (prevNumber !== item.number) {
            rowSpanArr[index - rowSpan] = rowSpan
            rowSpan = 0
            prevNumber = item.number
          }
        }
        rowSpan++
      })

      console.log('row span arr: ', rowSpanArr)
      this.rowSpanArr = rowSpanArr
    },

    /**
     * @description: 点击期间弹窗明细账
     * @param {*} data
     */
    clickTime(data) {
      this.$router.push({
        path: '/fm/ledger/subs/detailedLedger',
        query: {
          timeArray: [
            this.$moment(this.timeArray[0]).format('YYYYMM'),
            this.$moment(this.timeArray[1]).format('YYYYMM')],
          _subjectId: data.subjectId
        }
      })
      // window.open(routeData.href, '_blank')
    },

    /**
     * @description: 表格合并
     * @param {*} row
     * @param {*} column
     * @param {*} rowIndex
     * @param {*} columnIndex
     */
    objectSpanMethod({ row, column, rowIndex, columnIndex }) {
      if (columnIndex <= 1) {
        return {
          rowspan: this.rowSpanArr[rowIndex],
          colspan: this.rowSpanArr[rowIndex] === 0 ? 0 : 1
        }
      } else {
        return {
          rowspan: 1,
          colspan: 1
        }
      }
    }
  }
}
</script>

<style scoped lang="scss">
@import "../table.scss";

::v-deep .el-table {
  td.el-table__cell {
    border-bottom-color: $--border-color-base;
  }
}
</style>
