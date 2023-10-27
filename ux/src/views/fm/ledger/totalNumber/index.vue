<template>
  <div class="app-container">

    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          数量金额总账
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="318" />
        </span>
        <el-dropdown
          trigger="click"
          class="receive-drop"
        >
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="print">打印</el-dropdown-item>
            <el-dropdown-item @click.native="exportTab">导出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <div>
            <flexbox>
              <!-- 搜索条件 -->
              <search-popover-wrapper :search-time.sync="searchTime">
                <total-money-search
                  :default-value="searchForm"
                  @change="handleToSearch" />
              </search-popover-wrapper>
              <el-button style="margin-left: 8px;" type="subtle" plain icon="el-icon-refresh-right" @click="reset" />
            </flexbox>
          </div>
        </flexbox>
      </flexbox>
      <div class="table-wrapper">
        <!-- <div class="table-title">科目: <span>核算项目明细账</span> </div> -->
        <el-table
          id="totalNumberMoney"
          :header-cell-style="{'text-align':'center'}"
          :data="totalMoneyData"
          :height="tableHeight"
          style="width: 100%;"
          border>
          <el-table-column
            v-for="(item, index) in totalMoneyList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            align="center"
            show-overflow-tooltip>
            <el-table-column
              v-for="(cItem,cIndex) in item.child"
              :key="cIndex"
              show-overflow-tooltip
              :align="cItem.hasOwnProperty('type')&&cItem.type?'right':'center'"
              :prop="String(cItem.field) "
              :label="cItem.label" />
          </el-table-column>
        </el-table>
      <!-- <div class="p-contianer"></div> -->
      </div>
    </div>
  </div>

</template>

<script>
import SearchPopoverWrapper from '@/views/fm/components/SearchPopoverWrapper'
import TotalMoneySearch from './components/totalMoneySearch'
import { queryAmountDetailUpAccount, exportAmountDetailUpAccountAPI } from '@/api/fm/ledger'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { downloadExcelWithResData } from '@/utils'
import PrintMixin from '@/views/fm/mixins/Print.js'

export default {
  // 组件名
  name: 'TotalNumberMoney',
  // 注册组件
  components: {
    SearchPopoverWrapper,
    TotalMoneySearch
  },
  mixins: [PrintMixin],
  // 接收父组件数据
  props: {},
  // 组件数据
  data() {
    return {
      loading: false, // 展示加载中效时
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      totalMoneyList: [
        { label: '科目编码', field: 'number' },
        { label: '科目名称', field: 'subjectName' },
        { label: '单位', field: 'amountUnit' },
        { label: '期初余额', child: [
          { label: '方向', field: 'initialBalanceDirection' },
          { label: '数量', type: true, field: 'initialQuantity' },
          { label: '单价', type: true, field: 'initialUnitPrice' },
          { label: '金额', type: true, field: 'initialBalance' }
        ] },

        { label: '本期借方', child: [
          { label: '数量', type: true, field: 'debtorQuantity' },
          { label: '金额', type: true, field: 'debtorBalance' }
        ] },

        { label: '本期贷方', type: true, child: [
          { label: '数量', type: true, field: 'creditQuantity' },
          { label: '金额', type: true, field: 'creditBalance' }
        ] },

        { label: '本年累计借方', child: [
          { label: '数量', type: true, field: 'debtorYearQuantity' },
          { label: '金额', type: true, field: 'debtorYearBalance' }
        ] },

        { label: '本年累计贷方', child: [
          { label: '数量', type: true, field: 'creditYearQuantity' },
          { label: '金额', type: true, field: 'creditYearBalance' }
        ] },

        { label: '期末余额', child: [
          { label: '方向', field: 'endBalanceDirection' },
          { label: '数量', type: true, field: 'endQuantity' },
          { label: '单价', type: true, field: 'initialEndPrice' },
          { label: '金额', type: true, field: 'endBalance' }
        ] }

      ],
      totalMoneyData: [],
      searchForm: {}
    }
  },
  // 计算属性
  computed: {
  },
  // 监听属性
  watch: {
  },
  // 生命周期钩子函数
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
  },
  // 组件方法
  methods: {
    print() {
      this.HandlerPrint('totalNumberMoney', { title: '数量金额总账' })
    },
    // 搜索组件传过来的值
    handleToSearch(val) {
      delete val.time
      this.searchForm = val
      console.log('判断传值是否成功', val)
      this.getData()
    },
    // 点击重置按钮
    reset() {
      this.getData()
    },
    // 获取表格数据
    getData() {
      queryAmountDetailUpAccount(this.searchForm).then(res => {
        res.data.forEach(item => {
          for (const key in item) {
            if (key == 'debtorBalance') {
              item[key] = Number(item[key])
            }
            // 把0改为null
            if (item[key] === 0) {
              item[key] = null
            }
            // 增加千位符并且如果没有小数后两位添加两位0
            if (typeof item[key] == 'number' &&
                item[key] != 0 &&
                key !== 'number' &&
                key !== 'subjectId') {
              item[key] = separator(item[key])
            }
          }
        })
        this.totalMoneyData = res.data
      })
    },
    // 导出tab列表
    exportTab() {
      exportAmountDetailUpAccountAPI(this.searchForm).then(res => {
        downloadExcelWithResData(res)
      })
    }
  }
}
</script>

<style scoped lang='scss'>
@import "../table.scss";
</style>
