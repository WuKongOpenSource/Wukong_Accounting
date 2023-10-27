<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          核算项目余额表
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="316" />
        </span>
        <el-dropdown
          trigger="click"
          class="receive-drop"
        >
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-if="$auth('finance.accountBalance.export')" @click.native="print">打印</el-dropdown-item>
            <el-dropdown-item v-if="$auth('finance.accountBalance.export')" @click.native="exportTab">导出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <div>
            <flexbox>
              <search-popover-wrapper :search-time.sync="searchTime">
                <accounting-balance-search
                  :label="assistant"
                  :default-value="searchForm"
                  @change="handleToSearch" />
              </search-popover-wrapper>
            </flexbox>
          </div>
          <!-- 搜索条件 -->
          <div style="margin-left: 8px;">
            <span style="margin-right: 5px;">辅助类</span>
            <el-select v-model="assistant" filterable style="width: 100px;" mode="no-border">
              <el-option
                v-for="item in options"
                :key="item.adjuvantId"
                :label="item.adjuvantName"
                :value="item.adjuvantId" />
            </el-select>
          </div>
        </flexbox>
      </flexbox>
      <div class="table-wrapper">
        <el-table
          id="auxiliaryBalanceSheet"
          :header-cell-style="{'text-align':'center'}"
          :data="accountingBalanceData"
          :height="tableHeight"
          style="width: 100%;"
          border>
          <el-table-column
            v-for="(item, index) in accountingBalanceList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            align="center"
            show-overflow-tooltip>
            <el-table-column
              v-for="(cItem,cIndex) in item.child"
              :key="cIndex"
              :align="cItem.hasOwnProperty('type')&&cItem.type?'right':'center'"
              :prop="String(cItem.field) "
              :label="cItem.label"
              show-overflow-tooltip
            />
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>

</template>

<script>
import SearchPopoverWrapper from '@/views/fm/components/SearchPopoverWrapper'
import accountingBalanceSearch from './components/accountingBalanceSearch'
import { queryItemsDetailBalanceAccount, exportItemsDetailBalanceAccountAPI } from '@/api/fm/ledger'
import { fmAdjuvantListAPI } from '@/api/fm/setting'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { downloadExcelWithResData } from '@/utils'
import PrintMixin from '@/views/fm/mixins/Print.js'

export default {
  // 组件名
  name: 'AuxiliaryBalanceSheet',
  // 注册组件
  components: {
    SearchPopoverWrapper,
    accountingBalanceSearch
  },
  mixins: [PrintMixin],
  // 接收父组件数据
  props: {},
  // 组件数据
  data() {
    return {
      loading: false, // 展示加载中效时
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      accountingBalanceList: [
        { label: '编码', field: 'carteNumber' },

        { label: '项目名称', field: 'name' },
        { label: '期初余额', child: [
          { label: '借方', type: true, field: 'debtorInitialBalance' },
          { label: '贷方', type: true, field: 'creditInitialBalance' }
        ] },
        { label: '本期发生额', child: [
          { label: '借方', type: true, field: 'debtorCurrentBalance' },
          { label: '贷方', type: true, field: 'creditCurrentBalance' }
        ] },
        { label: '本年累计发生额', child: [
          { label: '借方', type: true, field: 'debtorYearBalance' },
          { label: '贷方', type: true, field: 'creditYearBalance' }
        ] },
        { label: '期末余额', child: [
          { label: '借方', type: true, field: 'debtorEndBalance' },
          { label: '贷方', type: true, field: 'creditEndBalance' }
        ] }
      ],
      accountingBalanceData: [],
      assistant: null, // 默认 客户辅助id 9
      // 辅助类
      options: [
        // { name: '线索', value: 1 },
        // { name: '客户', value: 2 },
        // { name: '联系人', value: 3 },
        // { name: '产品', value: 4 },
        // { name: '商机', value: 5 },
        // { name: '合同', value: 6 },
        // { name: '回款', value: 7 },
        // { name: '发票', value: 8 },
        // { name: '部门', value: 9 },
        // { name: '员工', value: 10 }
      ],
      searchForm: {}
    }
  },
  // 计算属性
  computed: {
  },
  // 监听属性
  watch: {
    assistant() {
      this.getData()
    }
  },
  // 生命周期钩子函数
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }

    this.getAdjuvantList()
  },
  mounted() {
  },
  // 组件方法
  methods: {
    print() {
      this.HandlerPrint('auxiliaryBalanceSheet', { title: '核算项目余额表' })
    },
    /**
     * @description: 获取辅助核算数据
     */
    getAdjuvantList() {
      fmAdjuvantListAPI().then(res => {
        this.options = res.data
        // 默认客户类型
        this.assistant = res.data[0].adjuvantId
      }).catch(() => {})
    },
    /**
     * @description: 搜索组件传过来的值
     * @param {*} val
     */
    handleToSearch(val) {
      delete val.time
      this.searchForm = val
      this.getData()
    },
    /**
     * @description: 导出tab列表
     */
    exportTab() {
      exportItemsDetailBalanceAccountAPI(this.searchForm).then(res => {
        downloadExcelWithResData(res)
      })
    },
    /**
     * @description: 获取表格数据
     */
    getData() {
      this.loading = true
      this.searchForm.adjuvantId = this.assistant
      queryItemsDetailBalanceAccount(this.searchForm).then(res => {
        res.data.forEach(item => {
          for (const key in item) {
            // 把0改为null
            if (item[key] === 0) {
              item[key] = null
            }
            // 增加千位符并且如果没有小数后两位添加两位0
            if (typeof item[key] == 'number' && item[key] !== 0 && key !== 'subjectId' && key !== 'relationId') {
              item[key] = separator(item[key])
            }
          }
        })
        this.accountingBalanceData = res.data
        this.loading = false
      })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped lang='scss'>
@import "../table.scss";
</style>
