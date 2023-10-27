<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          数量金额明细账
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="317" />
        </span>
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
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <div>
            <flexbox>
              <search-popover-wrapper :search-time.sync="searchTime">
                <detail-money-search
                  :default-value="searchForm"
                  @change="handleToSearch" />
              </search-popover-wrapper>
              <el-button class="refresh-btn" type="subtle" plain icon="el-icon-refresh-right" @click="reset" />
            </flexbox>
          </div>
        </flexbox>
      </flexbox>
      <div class="table-wrapper">
        <el-table
          id="auxiliaryBalanceSheet"
          :header-cell-style="{'text-align':'center'}"
          :data="detailMoneyData"
          :height="tableHeight"
          style="width: 100%;"
          border>
          <el-table-column
            v-for="(item, index) in DetailMoneyList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            align="center"
            show-overflow-tooltip>
            <template slot-scope="{row}">
              <span
                v-if="item.field === 'certificateNum'"
                class="can-visit--underline"
                @click="openVoucher(row)">
                {{ row[item.field] }}
              </span>
              <span v-else>{{ row[item.field] }}</span>
            </template>
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
import DetailMoneySearch from './components/detailMoneySearch'
import { queryAmountDetailAccount, exportAmountDetailAccountAPI } from '@/api/fm/ledger'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { downloadExcelWithResData } from '@/utils'
import { mapGetters } from 'vuex'
import PrintMixin from '@/views/fm/mixins/Print.js'

export default {
  // 组件名
  name: 'FMNumberMoneyAccount',
  // 注册组件
  components: {
    SearchPopoverWrapper,
    DetailMoneySearch

  },
  mixins: [PrintMixin],
  // 接收父组件数据
  props: {},
  // 组件数据
  data() {
    return {
      loading: false, // 展示加载中效时
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      DetailMoneyList: [
        { label: '日期', field: 'accountTime' },
        { label: '凭证字号', field: 'certificateNum' },
        { label: '摘要', field: 'digestContent' },
        { label: '借方发生额', child: [
          { label: '数量', type: true, field: 'debtorQuantity' },
          { label: '单价', type: true, field: 'debtorAmountUnit' },
          { label: '金额', type: true, field: 'debtorBalance' }

        ] },
        { label: '贷方发生额', type: true, child: [
          { label: '数量', type: true, field: 'creditQuantity' },
          { label: '单价', type: true, field: 'creditAmountUnit' },
          { label: '金额', type: true, field: 'creditBalance' }
        ] },
        { label: '余额', type: true, child: [
          { label: '方向', type: true, field: 'balanceDirection' },
          { label: '数量', type: true, field: 'balanceQuantity' },
          { label: '单价', type: true, field: 'balanceAmountUnit' },
          { label: '金额', type: true, field: 'balance' }
        ] }
      ],
      detailMoneyData: [],
      searchForm: {}
    }
  },
  // 计算属性
  computed: {
    ...mapGetters(['finance'])
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
    // this.getData()
  },

  // 组件方法
  methods: {
    print() {
      this.HandlerPrint('auxiliaryBalanceSheet', { title: '数量金额明细账' })
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
     * @description: 获取表格数据
     */
    getData() {
      queryAmountDetailAccount(this.searchForm).then(res => {
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
        this.detailMoneyData = res.data
      })
    },
    /**
     * @description: 点击重置按钮
     */
    reset() {
      this.getData()
    },
    /**
     * @description: 导出tab列表
     */
    exportTab() {
      exportAmountDetailAccountAPI(this.searchForm).then(res => {
        downloadExcelWithResData(res)
      })
    },
    /**
     * @description: 打开明细账操作
     * @param {*} row
     */
    openVoucher(row) {
      if (this.finance.voucher && this.finance.voucher.save) {
        this.$router.push({
          path: '/fm/voucher/subs/create',
          query: {
            ids: row.certificateId,
            index: 0
          }
        })
        // console.log(routeData)
        // window.open(routeData.href, '_blank')
      } else {
        this.$message.error('暂无权限')
      }
    }
  }
}
</script>

<style scoped lang='scss'>
@import "../table.scss";

.refresh-btn {
  margin-left: $--button-padding-vertical;
}
</style>
