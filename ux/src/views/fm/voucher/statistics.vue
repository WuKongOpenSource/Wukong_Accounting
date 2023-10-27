<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          凭证汇总表
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="37"
            data-id="308" />
        </span>
        <div class="buttons">
          <el-button
            v-if="$auth('finance.voucher.save')"
            type="primary"
            @click="addSubject"
          >添加</el-button
          >
          <el-dropdown
            trigger="click"
            class="receive-drop"
          ><el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-if="$auth('finance.voucher.print')" @click.native="printAll">
                打印
              </el-dropdown-item>
              <el-dropdown-item v-if="$auth('finance.voucher.export')" @click.native="exportAll">
                导出
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </flexbox>

      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <search-popover-wrapper :search-time.sync="searchTime">
            <statistics-search @change="searchHandle" />
          </search-popover-wrapper>
        </flexbox>

      </flexbox>

      <div class="voucher-table">
        <el-table
          id="table"
          ref="table"
          :data="voucherData"
          :height="tableHeight"
          :cell-class-name="cellClassName"
          :summary-method="getSummaries"
          :show-summary="true"
          stripe
          @row-click="handleRowClick"
        >
          <el-table-column
            v-for="(item, index) in voucherList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            :formatter="fieldFormatter"
            show-overflow-tooltip
          />
          <div
            v-if="printFooterShow"
            slot="append"
            style="display: flex;padding: 10px 0; font-size: 20px; border: 1px solid;border-top: none;">
            <template v-if="sums && sums.length">
              <span v-for="(item, index) in sums" :key="index" style="flex: 1;">
                {{ index==0 ? "总计：" : item }}
              </span>
            </template>
          </div>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { fmFinanceVoucherQueryListByTypeAPI, exportListByTypeAPI } from '@/api/fm/voucher'
import { fmFinanceParameterQueryParameterAPI } from '@/api/fm/setting'
import PrintMixin from '@/views/fm/mixins/Print.js'
import SearchPopoverWrapper from '../components/SearchPopoverWrapper'
import StatisticsSearch from './components/StatisticsSearch'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { mapActions } from 'vuex'
import { downloadExcelWithResData } from '@/utils'
import { accAdd } from '@/utils/acc.js'
export default {
  name: 'SummarizedVoucher',
  components: {
    SearchPopoverWrapper,
    StatisticsSearch
  },
  mixins: [PrintMixin],
  data() {
    return {
      pageTitle: '凭证汇总表',
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度

      searchForm: {},
      timeRange: [],
      subjectNumberLength: '',
      voucherList: [
        { label: '科目编码', field: 'subjectNumber' },
        { label: '科目名称', field: 'subjectName' },
        { label: '借方金额', field: 'debtorBalances' },
        { label: '贷方金额', field: 'creditBalances' }
      ],
      showSearch: false,
      hideNum: false,

      /** 余额每行的信息 */
      voucherData: [],
      sums: null
    }
  },
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
    this.getVoucherTimeRange()
    this.getParameter()
  },
  methods: {
    ...mapActions(['getVoucherTimeRange']),
    /**
     * @description: 搜索操作
     * @param {*} data
     */
    searchHandle(data) {
      console.log(data)
      this.timeRange = data.time
      delete data.time
      this.searchForm = data
      this.getList()
    },
    /**
     * @description: 获取系统参数
     */
    getParameter() {
      fmFinanceParameterQueryParameterAPI().then(res => {
        if (res.data.rule) {
          const rules = res.data.rule.split('-')
          this.subjectNumberLength = Number(rules[0])
        }
      })
    },
    /**
     * @description: 打印操作 全部
     * @param {*}
     * @return {*}
     */
    printAll() {
      this.HandlerPrint('table', { title: '凭证汇总表' })
    },
    /**
     * @description: 处理行内的点击事件
     * @param {*} row
     * @param {*} column
     * @param {*} event
     */
    handleRowClick(row, column, event) {
      if (column.type === 'selection') {
        return // 多选布局不能点击
      }
      if (column.property === 'subjectNumber') {
        const sendSearchForm = [
          this.$moment(this.timeRange[0]).format('YYYY-MM'),
          this.$moment(this.timeRange[1]).format('YYYY-MM')
        ]
        this.$router.push({
          path: '/fm/ledger/subs/detailedLedger',
          query: {
            timeArray: sendSearchForm,
            _subjectId: row.subjectId
          }
        })
        // window.open(routeData.href, '_blank')
      }
    },

    /**
     * @description: 列表
     */
    getList() {
      this.loading = true
      fmFinanceVoucherQueryListByTypeAPI(this.searchForm)
        .then((res) => {
          this.loading = false
          this.voucherData = res.data
          this.$nextTick(() => {
            this.$refs.table.doLayout()
          })
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * @description：导出全部
     */
    exportAll() {
      exportListByTypeAPI(this.searchForm).then(res => {
        downloadExcelWithResData(res)
      })
    },
    /**
     * @description: 列表格式化
     * @param {*} row
     * @param {*} column
     */
    fieldFormatter(row, column) {
      if (['debtorBalances', 'creditBalances'].includes(column.property)) {
        if (row[column.property]) {
          return separator(row[column.property])
        } else {
          return ''
        }
      }
      return row[column.property]
    },

    /**
     * @description: 单元格class类名
     * @param {*} row
     * @param {*} column
     * @param {*} rowIndex
     * @param {*} columnIndex
     * @return {*}
     */
    cellClassName({ row, column, rowIndex, columnIndex }) {
      if (column.property === 'subjectNumber') {
        return 'can-visit--underline'
      } else if (
        column.property === 'debtorBalances' ||
        column.property === 'creditBalances'
      ) {
        return 'money'
      }
    },

    /**
     * @description: 计算总计
     * @param {*} param
     * @return {*}
     */
    getSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '总计'
          return
        }
        if (index === 1) {
          sums[index] = ''
          return
        }
        const values = data.filter(o => o.subjectNumber.length === this.subjectNumberLength).map((item) => Number(item[column.property]))
        if (!values.every((value) => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr)
            if (!isNaN(value)) {
              return accAdd(prev, curr)
            } else {
              return prev
            }
          }, 0)
          sums[index] = sums[index] ? separator(sums[index]) : ''
        } else {
          sums[index] = ''
        }
      })

      console.log('sums: ', sums)
      this.sums = sums
      return sums
    },

    /**
     * @description: 添加
     * @param {*}
     * @return {*}
     */
    addSubject() {
      this.$router.push('/fm/voucher/subs/create')
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 15px 40px 0;

  .main {
    height: 100%;

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
      }
    }
  }
}

::v-deep.el-table {
  .money {
    color: #ff991f;
  }

  .el-table__footer-wrapper {
    td {
      background-color: #ebecf0;
    }
  }
}

::v-deep .el-table__footer {
  tbody td {
    border-color: transparent;
  }
}
</style>
