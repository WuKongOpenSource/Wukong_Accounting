<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          明细账
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="309" />
        </span>
        <el-dropdown
          trigger="click"
          class="receive-drop"
        >
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
              <subject-select-options
                v-model="chooseSubjectId"
                :subject-name.sync="subjectName"
                @change="subjectIdChange" />
            </flexbox>
          </div>
          <div style="margin-left: 8px;">
            <choose-accountant-time v-model="timeArray" :clearable="false" />
            <!-- 会计时间 -->
            <!-- <choose-accountant-time
              :time-value="timeArray"
              :is-show-name="true"
              @getTime="getAccountantTime" /> -->
          </div>
        </flexbox>
      </flexbox>

      <div class="table-wrapper">
        <el-table
          id="detailedLedger"
          :header-cell-style="{'text-align':'center'}"
          :data="detailData"
          :height="tableHeight">
          <el-table-column
            v-for="(item, index) in balabceList"
            :key="index"
            :prop="item.field"
            :align="item.hasOwnProperty('type')&&item.type?'right':'center'"
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
          :data-list="searchDataList"
          :style="{height: tableHeight+'px'}"
          @nodeClick="nodeClickHandle" />
      </div>
    </div>
  </div>
</template>

<script>
import { exportDetailAccountAPI, financeCertificateItemsDetailTreeAPI } from '@/api/fm/ledger.js'
import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'
import ChooseAccountantTime from '@/views/fm/components/chooseAccountantTime'
import { queryDetailAccount } from '@/api/fm/ledger'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { fmFinanceSubjectListAPI } from '@/api/fm/setting'
import { downloadExcelWithResData } from '@/utils'
import { mapGetters } from 'vuex'
import PrintMixin from '@/views/fm/mixins/Print.js'
import moment from 'moment'
import QuickSearch from '@/views/fm/ledger/components/QuickSearch.vue'
export default {
  // 组件名
  name: 'FinanceDetailedLedger',
  // 注册组件
  components: {
    SubjectSelectOptions,
    ChooseAccountantTime,
    QuickSearch
  },
  // 组件数据
  mixins: [PrintMixin],
  // 接收父组件数据
  props: {},
  data() {
    return {
      loading: false, // 展示加载中效果
      chooseSubjectId: '', // 默认的科目id
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      balabceList: [
        { label: '日期', field: 'accountTime' },
        { label: '凭证字号', field: 'certificateNum' },
        { label: '摘要', field: 'digestContent' },
        { label: '借方', field: 'debtorBalance', type: true },
        { label: '贷方', field: 'creditBalance', type: true },
        { label: '方向', field: 'balanceDirection' },
        { label: '余额', field: 'balance', type: true }],
      detailData: [],
      searchForm: {},
      timeArray: [],
      frist: true,
      subjectName: '',
      searchLoading: false,
      searchDataList: [],
      activeSearchKey: ''
    }
  },
  // 计算属性
  computed: {
    iconClass() {
      return this.showScene ? 'arrow-up' : 'arrow-down'
    },
    ...mapGetters([
      'financeCurrentAccount'
    ])
  },
  watch: {
    timeArray: {
      handler(val) {
        console.log('查询时间的', val)
        if (val == null) {
          return
        }
        if (!this.frist) {
          this.getDate()
        }
        this.getSearchDataList()
      },
      deep: true
    }
  },

  // 生命周期钩子函数
  created() {
    this.restTime()
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
    const query = this.$route.query || {}
    if (query._subjectId) {
      this.chooseSubjectId = query._subjectId
      console.log(query.timeArray)
      this.timeArray = query.timeArray
      this.getDate()
    } else {
      // 获取默认的科目id后获取数据
      this.getSubjectList()
    }
  },

  // 组件方法
  methods: {
    /**
     * 获取快速切换数据列表
     */
    getSearchDataList() {
      this.searchLoading = false
      const params = {
        startTime: moment(this.timeArray[0]).format('YYYYMM'),
        endTime: moment(this.timeArray[1]).format('YYYYMM')
      }
      financeCertificateItemsDetailTreeAPI(params).then(res => {
        console.log(res)
        this.searchLoading = false
        const resData = res.data
        this.mapLabel(resData)
        this.searchDataList = resData
      }).catch(e => {
        this.searchLoading = false
      })
    },
    // 递归函数，用于遍历数组并映射 label 字段
    mapLabel(arr) {
      arr.forEach(item => {
        item.label = item.number + ' ' + item.subjectName
        item.nodeKey = item.subjectId

        if (item.children && item.children.length > 0) {
          this.mapLabel(item.children)
        }
        if (item.adjuvantList?.length) {
          item.adjuvantList.forEach(ad => {
            item.children.push({
              ...ad,
              label: item.number + ' ' + ad.carteNumber + '_' + ad.carteName,
              nodeKey: ad.subjectId + '_' + ad.carteId
            })
          })
        }
      })
    },

    // 快速搜索节点点击
    nodeClickHandle(data) {
      console.log(data)
      this.subjectName = data.subjectName || data.label
      this.chooseSubjectId = data.subjectId
      if (data.carteId) {
        this.searchForm.carteId = data.carteId
      }
      this.activeSearchKey = data.nodeKey
      this.subjectIdChange(data.subjectId)
    },

    print() {
      this.HandlerPrint('detailedLedger', { title: '明细账', centerText: '科目：' + this.subjectName })
    },
    /**
     * @description: 重置时间
     */
    restTime() {
      this.searchForm = {
        startTime: this.$moment(this.financeCurrentAccount.startTime).format('YYYYMM'),
        endTime: this.$moment(this.financeCurrentAccount.startTime).format('YYYYMM'),
        subjectId: '',
        maxCertificateNum: '',
        minCertificateNum: '',
        maxLevel: 1,
        minLevel: 1
      }
      this.timeArray = [
        this.financeCurrentAccount.startTime,
        this.financeCurrentAccount.startTime
      ]
    },
    /**
     * @description: 获取默认明细账中科目id后请求接口
     */
    getSubjectList() {
      fmFinanceSubjectListAPI({
        type: 1,
        isTree: 1
      }).then(res => {
        if (res.data.length > 0) {
          this.frist = false
          this.chooseSubjectId = res.data[0].subjectId
        }
        this.getDate()
      })
    },
    /**
     * @description:子集操作改变处理
     * @param {number} id
     */
    subjectIdChange(id) {
      this.searchForm.subjectId = id
      this.getDate()
    },
    /**
     * @description: 导出tab列表
     */
    exportTab() {
      exportDetailAccountAPI(this.searchForm).then(res => {
        downloadExcelWithResData(res)
      })
    },
    /**
     * @description: 获取数据
     */
    getDate() {
      // this.$refs?.quickSearchRef?.setCurrentKey(this.activeSearchKey)
      this.loading = true
      this.searchForm.subjectId = this.chooseSubjectId
      this.searchForm.startTime = this.$moment(this.timeArray[0]).format('YYYYMM')
      this.searchForm.endTime = this.$moment(this.timeArray[1]).format('YYYYMM')
      queryDetailAccount(this.searchForm).then(res => {
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
            if (typeof item[key] == 'number' && item[key] != 0) {
              item[key] = separator(item[key])
            }
          }
        })
        this.detailData = res.data
        this.loading = false
        this.$refs?.quickSearchRef?.setCurrentKey(this.activeSearchKey || this.chooseSubjectId)
      }).catch(() => {
        this.loading = false
      })
    },
    /**
     * @description: 跳转凭证
     * @param {*} row
     */
    openVoucher(row) {
      this.$router.push({
        path: '/fm/voucher/subs/create',
        query: {
          ids: row.certificateId,
          index: 0
        }
      })
      // window.open(routeData.href, '_blank')
    }
  }
}
</script>

<style scoped lang="scss">
@import "../table.scss";

.table-wrapper {
  display: flex;
}
</style>
