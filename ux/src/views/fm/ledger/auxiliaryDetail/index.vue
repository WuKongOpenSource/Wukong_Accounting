<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          核算项目明细账
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="315" />
        </span>
        <el-dropdown
          trigger="click"
          class="receive-drop">
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-if="$auth('finance.subLedger.export')" @click.native="print">打印</el-dropdown-item>
            <el-dropdown-item v-if="$auth('finance.subLedger.export')" @click.native="exportTab">导出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <div>
            <flexbox>
              <search-popover-wrapper :search-time.sync="searchTime">
                <accounting-detail-search
                  ref="searchForm"
                  :default-value="searchForm"
                  :label="assistant"
                  @change="handleToSearch" />
              </search-popover-wrapper>
            </flexbox>
          </div>
          <!-- 搜索条件 -->
          <div style="margin-left: 8px;">
            <span style="margin-right: 5px;">辅助类</span>
            <el-select v-model="assistant" filterable style="width: 100px;" mode="no-border" @change="assistantChange">
              <el-option
                v-for="item in options"
                :key="item.adjuvantId"
                :label="item.adjuvantName"
                :value="item.adjuvantId" />
            </el-select>
          </div>
        </flexbox>
      </flexbox>
      <div v-if="carteData" class="table-assistant">
        {{ `${assistantName}:${carteData.carteNumber}_${carteData.carteName}` }}
      </div>
      <div class="table-wrapper">
        <el-table
          id="auxiliaryDetailedLedger"
          :data="accountingDetailData"
          :height="tableHeight"
          style="width: 100%;"
          border>
          <el-table-column
            v-for="(item, index) in accountingDetailList"
            :key="index"
            :align="item.hasOwnProperty('type')&&item.type?'right':'center'"
            :prop="item.field"
            :label="item.label"
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
          </el-table-column>
        </el-table>
        <!-- 快速搜索 -->
        <QuickSearch
          ref="quickSearchRef"
          v-loading="searchLoading"
          :data-list="projectList"
          :style="{height: tableHeight+'px'}"
          @nodeClick="nodeClickHandle" />
      </div>
    </div>
  </div>

</template>

<script>
import SearchPopoverWrapper from '@/views/fm/components/SearchPopoverWrapper'
import AccountingDetailSearch from './components/accountingDetailSearch'
import { queryItemsDetailAccount, exportItemsDetailAccountAPI, financeCertificateQueryLabelNameByDataAPI } from '@/api/fm/ledger'
import { fmAdjuvantListAPI } from '@/api/fm/setting'
import { downloadExcelWithResData } from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'
import PrintMixin from '@/views/fm/mixins/Print.js'
import QuickSearch from '@/views/fm/ledger/components/QuickSearch.vue'
import { debounce } from 'throttle-debounce'

export default {
  // 组件名
  name: 'AuxiliaryDetailedLedger',
  // 注册组件
  components: {
    SearchPopoverWrapper,
    AccountingDetailSearch,
    QuickSearch
  },
  mixins: [PrintMixin],
  // 接收父组件数据
  props: {},
  // 组件数据
  data() {
    return {
      loading: false, // 展示加载中效时
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      accountingDetailList: [
        { label: '日期', field: 'accountTime' },
        { label: '凭证字号', field: 'certificateNum' },
        { label: '摘要', field: 'digestContent' },
        { label: '借方', field: 'debtorBalance', type: true },
        { label: '贷方', field: 'creditBalance', type: true },
        { label: '方向', field: 'balanceDirection' },
        { label: '余额', field: 'balance', type: true }],
      accountingDetailData: [],
      carteData: null,
      assistant: null, // 默认 客户辅助id 9
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
      searchForm: { },
      projectName: '',
      searchInput: '',
      projectList: [],
      searchLoading: false
    }
  },
  computed: {
    assistantName() {
      if (this.assistant) {
        return this.options.find(o => o.adjuvantId == this.assistant).adjuvantName
      }
      return ''
    }
  },
  watch: {
    'searchForm.endTime': {
      handler(newVal, oldVal) {
        if (newVal != oldVal) {
          this.getSubjectListDebounce()
        }
      }
    },
    'searchForm.start': {
      handler(newVal, oldVal) {
        if (newVal != oldVal) {
          this.getSubjectListDebounce()
        }
      }
    }
  },
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
    this.getSubjectListDebounce = debounce(500, this.getSubjectList)
    this.getAdjuvantList()
  },
  mounted() {

  },
  // 组件方法
  methods: {
    print() {
      const headerHtml = `
        <div style="font-size:30px;font-weight:bold;text-align:center;padding:10px;">核算项目明细账</div>
        <div style="font-size:22px;font-weight:bold;padding:10px 0;">
          编制单位：${this.financeCurrentAccount.companyName}
        </div>
        <div style="display:flex; justify-content:space-between;font-size:22px;font-weight:bold">
          <span style="flex:1">科目:${this.projectName}</span>
          <span style="flex:1;text-align: center;">${this.getTimeShowVal([this.$moment(this.searchForm.startTime, 'YYYYMM').format('YYYY-MM-DD'), this.$moment(this.searchForm.endTime, 'YYYYMM').format('YYYY-MM-DD')])}</span>
          <span style="flex:1;text-align: right;">辅助类别:${this.assistantName}</span>
        </div>
      `
      this.HandlerPrint('auxiliaryDetailedLedger', { title: '核算项目明细账', headerHtml })
    },
    /**
     * @description: 获取辅助核算数据
     */
    getAdjuvantList() {
      fmAdjuvantListAPI().then(res => {
        this.options = res.data
        // 默认客户类型
        this.assistant = res.data[0].adjuvantId
        // this.getSubjectList()
      }).catch(() => {})
    },
    /**
     * @description: 辅助类选择操作
     */
    assistantChange() {
      this.$refs.searchForm.form.relationId = ''
      this.$refs.searchForm.emitChange()
      this.getSubjectList()
    },
    /**
     * @description:搜索组件传过来的值
     * @param {*} val
     */
    handleToSearch(val, projectName) {
      this.projectName = projectName
      delete val.time
      this.searchForm = val
      console.log('判断传值是否成功', this.searchForm, val)
      this.getData()
    },
    /**
     * @description: 获取表格数据
     */
    getData() {
      this.loading = true
      this.searchForm.adjuvantId = this.assistant
      console.log(this.searchForm)
      queryItemsDetailAccount(this.searchForm).then(res => {
        if (res.data) {
          const list = res.data
          list.forEach(item => {
            for (const key in item) {
              if (key == 'debtorBalance') {
                item[key] = Number(item[key])
              }
              // 把0改为null
              if (item[key] === 0) {
                item[key] = null
              }
              // 增加千位符并且如果没有小数后两位添加两位0
              if (typeof item[key] == 'number' && item[key] != 0 && key != 'certificateId') {
                item[key] = separator(item[key])
              }
            }
          })
          this.accountingDetailData = list
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      }).finally(() => {
        this.$refs?.quickSearchRef?.setCurrentKey(this.searchForm.relationId)
      })
    },
    /**
     * @description: 导出tab列表
     */
    exportTab() {
      exportItemsDetailAccountAPI(this.searchForm).then(res => {
        downloadExcelWithResData(res)
      })
    },
    /**
     * @description: 调整到凭证
     * @param {*} row
     */
    openVoucher(row) {
      console.log(row)
      this.$router.push({
        path: '/fm/voucher/subs/create',
        query: {
          ids: row.certificateId,
          index: 0
        }
      })
      // window.open(routeData.href, '_blank')
    },
    // 获取项目列表
    getSubjectList() {
      this.searchLoading = true
      financeCertificateQueryLabelNameByDataAPI({
        'endTime': this.searchForm.endTime,
        'startTime': this.searchForm.startTime,
        adjuvantId: this.assistant
      }).then(res => {
        this.projectList = []
        res.data.forEach(ele => {
          const item = {
            projectName: '',
            projectId: ''
          }
          item.projectName = `${ele.carteNumber}_${ele.name}`
          item.projectId = ele.relationId
          item.label = `${ele.carteNumber}_${ele.name}`
          item.nodeKey = ele.relationId
          this.projectList.push(item)
        })
        if (this.projectList.length) {
          this.nodeClickHandle(this.projectList[0])
        }
        this.searchLoading = false
        console.log('获取项目列表', this.projectList)
      }).catch(e => {
        this.searchLoading = false
        console.error(e)
      })
    },
    // 右侧辅助核算点击
    nodeClickHandle(nodeData) {
      this.searchForm.relationId = nodeData.projectId
      this.$refs.searchForm.projectName = nodeData.projectName
      this.$refs.searchForm.form.relationId = nodeData.projectId
      this.getData()
    }
  }
}
</script>

<style scoped lang='scss'>
@import "../table.scss";

.table-assistant {
  padding: 16px;
  padding-top: 0;
  line-height: 20px;
}

.table-wrapper {
  display: flex;

  .quick-search {
    display: flex;
    flex-direction: column;
    margin-left: 10px;
    cursor: pointer;
    border: 1px solid #ccc;

    // padding:10px;
    .head {
      display: flex;
      align-items: center;
      padding: 10px;

      .search-title {
        margin-left: 18px;
      }
    }

    .search-input {
      padding: 10px;
    }

    .search-list {
      flex: 1;
      overflow: hidden;
      overflow-y: scroll;

      .list-item-wrap {
        padding: 6px 20px;
        cursor: pointer;

        .list-item{

        }
      }

      .active {
        background-color: #f2f3f6;
      }

      .list-item-wrap:hover {
        background-color: #d7e8fe;
      }
    }
  }
}
</style>
