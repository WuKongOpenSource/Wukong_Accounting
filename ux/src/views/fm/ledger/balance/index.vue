<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          余额表
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="312" />
        </span>
        <div class="buttons">
          <!-- <div class="checkbox-item">
              <el-checkbox v-model="checked">显示本年累计</el-checkbox>
            </div>
            <div class="checkbox-item">
              <el-checkbox v-model="checked">显示辅助核算</el-checkbox>
            </div> -->
          <!-- <el-button
              v-if="$auth('finance.accountBalance.print')"
              type="primary">打印</el-button> -->
          <el-dropdown trigger="click" class="receive-drop">
            <el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-if="$auth('finance.accountBalance.export')"
                @click.native="print"
              >打印</el-dropdown-item>
              <el-dropdown-item
                v-if="$auth('finance.accountBalance.export')"
                @click.native="exportTab"
              >导出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <div>
            <flexbox>
              <search-popover-wrapper :search-time.sync="searchTime">
                <balance-search
                  :default-value="searchForm"
                  @change="handleToSearch"
                />
              </search-popover-wrapper>
              <el-button
                type="subtle"
                style="margin-left: 8px;"
                icon="el-icon-refresh-right"
                @click="getDate"
              />
            </flexbox>
          </div>
          <div class="checkbox-item">
            <el-checkbox
              v-model="expandAll"
              @change="openBtn"
            >展开所有级次</el-checkbox
            >
          </div>
        </flexbox>
      </flexbox>
      <div class="table-wrapper">
        <el-table
          v-if="balanceSheetTable"
          id="balanceSheet"
          ref="balanceSheetTable"
          :header-cell-style="{ 'text-align': 'center' }"
          :default-expand-all="expandAll"
          :data="balabceData"
          :height="tableHeight"
          :tree-props="{ children: 'subjects' }"
          :cell-class-name="cellClassName"
          row-key="id"
          style="width: 100%;"
          border
          stripe
          @row-click="handleRowClick"
        >
          <el-table-column
            v-for="(item, index) in fieldList"
            :key="index"
            :formatter="fieldFormatter"
            :label="item.label"
            :prop="item.prop"
            show-overflow-tooltip
          >
            <el-table-column
              v-for="(ite, inde) in item.columns"
              :key="index + '_' + inde"
              :formatter="fieldFormatterMoney"
              :prop="String(ite.prop)"
              :label="ite.label"
              align="right"
              show-overflow-tooltip
            />
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>

import { queryDetailBalanceAccount, exportDetailBalanceAccountAPI } from '@/api/fm/ledger'
import { downloadExcelWithResData } from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'
import SearchPopoverWrapper from '@/views/fm/components/SearchPopoverWrapper'
import BalanceSearch from './components/balanceSearch'
import PrintMixin from '@/views/fm/mixins/Print.js'

import { mapGetters } from 'vuex'
export default {
  name: 'BalanceSheet',
  components: {
    SearchPopoverWrapper,
    BalanceSearch
  },
  mixins: [PrintMixin],
  props: {},
  data() {
    return {
      expandAll: false,
      balanceSheetTable: true,
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      /** 总账每行的信息 */
      balabceData: [],
      fieldList: [
        {
          label: '科目编码',
          prop: 'number'
        },
        {
          label: '科目名称',
          prop: 'subjectName'
        },
        {
          label: '期初余额',
          columns: [
            {
              label: '借方',
              prop: 'debtorInitialBalance'
            },
            {
              label: '贷方',
              prop: 'creditInitialBalance'
            }
          ]
        },
        {
          label: '本期发生额',
          columns: [
            {
              label: '借方',
              prop: 'debtorCurrentBalance'
            },
            {
              label: '贷方',
              prop: 'creditCurrentBalance'
            }
          ]
        },
        {
          label: '本年累计发生额',
          columns: [
            {
              label: '借方',
              prop: 'debtorYearBalance'
            },
            {
              label: '贷方',
              prop: 'creditYearBalance'
            }
          ]
        },
        {
          label: '期末余额',
          columns: [
            {
              label: '借方',
              prop: 'debtorEndBalance'
            },
            {
              label: '贷方',
              prop: 'creditEndBalance'
            }
          ]
        }
      ],
      // 查询数据的参数
      searchForm: {},
      timeArray: []
    }
  },
  // 计算属性
  computed: {
    ...mapGetters(['financeCurrentAccount']),
    iconClass() {
      return this.showScene ? 'arrow-up' : 'arrow-down'
    }
  },
  // 生命周期钩子函数
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
  },
  methods: {
    print() {
      this.expandAll = true
      this.openBtn()
      this.$nextTick(() => {
        this.HandlerPrint('balanceSheet', { title: '科目余额表', style: `
        td:first-child>div:{width:100%;}
        .cell.el-tooltip{word-break:break-all;}
        ` })
      })
    },
    /**
     * @description: 传过来的搜索条件
     * @param {object} data
     */
    handleToSearch(data) {
      this.timeArray = data.time
      delete data.time
      this.searchForm = data
      this.getDate()
    },
    /**
     * @description: 设置数值
     * @param {object} data
     */
    setNUmber(data) {
      data.forEach((item) => {
        item.id = Math.ceil(Math.random() * 10000000)
        if (
          item.hasOwnProperty('subjects') &&
          typeof item.subjects !== 'object'
        ) {
          if (item.subjects.length > 0) {
            item.subjects = JSON.parse(item.subjects)
          }
        }

        if (item.hasOwnProperty('subjects') && item.subjects.length > 0) {
          item.subjects = item.subjects.map((o, index) => {
            o.id = Math.ceil(Math.random() * 10000000)
            return o
          })
          this.setNUmber(item.subjects)
        }
      })
    },
    getDate() {
      this.loading = true
      queryDetailBalanceAccount(this.searchForm)
        .then((res) => {
          console.log('获取数据科目余额表', res)
          this.setNUmber(res.data)
          this.balabceData = res.data

          this.loading = false
        })
        .catch((err) => {
          console.log(err)
          this.loading = false
        })
    },
    /**
     * @description: 查看详情
     * @param {object} data
     * @return {*}
     */
    lookDetail(data) {
      this.$router.push({
        path: '/fm/ledger/subs/detailedLedger',
        query: {
          timeArray: [
            this.$moment(this.timeArray[0]).format('YYYY-MM'),
            this.$moment(this.timeArray[1]).format('YYYY-MM')
          ],
          _subjectId: data.subjectId
        }
      })
      // window.open(routeData.href, '_blank')
    },
    // 导出tab列表
    exportTab() {
      exportDetailBalanceAccountAPI({ ...this.searchForm, isLaunch: this.expandAll ? 1 : 0 }).then(res => {
        downloadExcelWithResData(res)
      })
    },
    // 展开
    openBtn() {
      this.balanceSheetTable = false
      this.$nextTick(() => {
        this.balanceSheetTable = true
      })
    },
    handleRowClick(row, column, event) {
      if (column.property === 'number') {
        this.lookDetail(row)
      }
    },
    /**
     * 列表展示格式化 科目编码/科目名称
     */
    fieldFormatter(row, column) {
      // console.log('row, column', row, column)
      if (column.property == 'number') {
        let str = row.number
        if (row.adjuvantList && row.adjuvantList.length > 0) {
          const arr = []
          row.adjuvantList.forEach((el) => {
            arr.push(el.carteNumber)
          })
          const valStr = arr.join('_')
          str = str + '_' + valStr
        }
        return str
      } else if (column.property == 'subjectName') {
        let str = row.subjectName
        if (row.adjuvantList && row.adjuvantList.length > 0) {
          const arr = []
          row.adjuvantList.forEach((el) => {
            arr.push(el.carteName)
          })
          const valStr = arr.join('_')
          str = str + '_' + valStr
        }
        return str
      } else {
        return row[column.property]
      }
    },
    /**
     * 金额格式化
     */
    fieldFormatterMoney(row, column) {
      if (row[column.property]) {
        return separator(row[column.property])
      } else {
        return ''
      }
    },
    cellClassName({ row, column, rowIndex, columnIndex }) {
      console.log(row)
      if (column.property === 'number') {
        if (
          !row.subjects ||
          row.subjects.length == 0
        ) {
          return 'can-visit--underline no-children no-father'
        } else {
          return 'can-visit--underline'
        }
      }
    }
  }
}
</script>

<style scoped lang="scss">
@import "../table.scss";

::v-deep .el-table--border td:first-child .cell {
  display: flex !important;

  .el-table__placeholder {
    display: none;
  }
}

::v-deep.no-father,
.no-children {
  padding-left: 23px !important;
}

.checkbox-item {
  padding: 0 10px;
}
</style>
