<template>
  <div class="app-container">
    <div v-loading="loading" class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          多栏账
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="39"
            data-id="313" />
        </span>
        <el-dropdown
          trigger="click"
          class="receive-drop"
        >
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-if="$auth('finance.multiColumn.export')" @click.native="print">打印</el-dropdown-item>
            <el-dropdown-item v-if="$auth('finance.multiColumn.export')" @click.native="exportTab">导出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <div>
            <flexbox>
              <subject-select-options
                v-model="chooseSubjectId"
                :subject-list="subjectList"
                :subject-name.sync="subjectName"
                :help-obj="helpObj"
                :before-choose="beforeChooseSubject" />
                <!-- @change="subjectChoose" -->
            </flexbox>
          </div>
          <div style="margin-left: 8px;">
            <!-- 会计时间 -->
            <choose-accountant-time
              v-model="timeArray"
              :clearable="false" />
          </div>
        </flexbox>
      </flexbox>

      <div class="table-wrapper">
        <el-table
          id="multicolumnLedger"
          :header-cell-style="{'text-align':'center'}"
          :data="multiColumnData"
          :height="tableHeight"
          border>
          <el-table-column
            v-for="(item, index) in multiColumnList"
            :key="index"
            :align="item.hasOwnProperty('type')&&item.type?'right':'center'"
            :prop="item.field"
            :label="item.label"
            show-overflow-tooltip>
            <el-table-column
              v-for="(cItem,cIndex) in item.children"
              :key="cIndex"
              :prop="String(cItem.field)"
              :label="cItem.label"
              show-overflow-tooltip
            />
            <template slot-scope="scope">
              <span
                v-if="item.field==='certificateNum'"
                class="can-visit--underline"
                @click="openVoucher(scope.row)">
                {{ scope.row[item.field] }}
              </span>
              <span v-else>
                {{ scope.row[item.field] }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import {
  fmFinanceSubjectListAPI
} from '@/api/fm/setting'
import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'
import ChooseAccountantTime from '@/views/fm/components/chooseAccountantTime'
import { queryDiversification, exportDiversificationAPI } from '@/api/fm/ledger'
import { downloadExcelWithResData } from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'
import { mapGetters } from 'vuex'
import PrintMixin from '@/views/fm/mixins/Print.js'

export default {
  // 组件名
  name: 'MulticolumnLedger',
  // 注册组件
  components: {
    SubjectSelectOptions,
    ChooseAccountantTime
  },
  mixins: [PrintMixin],
  // 接收父组件数据
  props: {},
  // 组件数据
  data() {
    return {
      pageTitle: '多栏账',
      chooseSubjectId: '',
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 240, // 表的高度
      multiColumnList: [
        { label: '日期', field: 'accountTime' },
        { label: '凭证字号', field: 'certificateNum' },
        { label: '摘要', field: 'digestContent' },
        { label: '借方', field: 'debtorBalance', type: true },
        { label: '贷方', field: 'creditBalance', type: true },
        { label: '方向', field: 'balanceDirection' },
        { label: '余额', field: 'balance', type: true }],
      multiColumnData: [],
      subjectList: [],
      // 查询数据的参数
      searchForm: {
      },
      timeArray: [],
      helpObj: {
        dataType: 39,
        dataId: 314
      },
      subjectName: ''
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
    // timeArray() {
    //   const timeArray = [
    //     this.financeCurrentAccount.startTime,
    //     this.financeCurrentAccount.startTime
    //   ]
    //   return timeArray
    // }
  },
  // 监听属性
  watch: {
    chooseSubjectId(val) {
      console.log(val)
      if (val === null) this.chooseSubjectId = ''
      this.getDate()
    },
    timeArray: {
      handler() {
        this.getDate()
      }
    }
  },
  // 生命周期钩子函数
  created() {
    this.restTime()
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 240
    }
  },
  mounted() {
    this.getDate()
    this.getSubjectList()
  },
  // 组件方法
  methods: {
    print() {
      this.HandlerPrint('multicolumnLedger', { title: '多栏账', centerText: '科目：' + this.subjectName })
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
     * @description: 获取科目列表
     */
    getSubjectList() {
      fmFinanceSubjectListAPI({
        type: null,
        isTree: 0
      }).then((res) => {
        this.subjectList = res ? res.data || [] : []
      }).catch(() => {})
    },
    /**
     * @description: 点击科目时
     * @param {*} item
     */
    beforeChooseSubject(item) {
      if (!item) return true
      const flag = this.subjectList.find(o => o.parentId === item.subjectId)
      if (!flag) {
        this.$message.warning('不允许选择没有明细的科目')
      }
      return flag
    },
    /**
     * @description: 导出tab列表
     */
    exportTab() {
      exportDiversificationAPI({ ...this.searchForm, headList: this.multiColumnList }).then((res) => {
        downloadExcelWithResData(res)
      })
    },
    /**
     * @description: 查询数据
     */
    getDate() {
      this.loading = true
      this.searchForm.subjectId = this.chooseSubjectId
      this.searchForm.startTime = this.$moment(this.timeArray[0]).format('YYYYMM')
      this.searchForm.endTime = this.$moment(this.timeArray[1]).format('YYYYMM')

      console.log('object', this.searchForm)
      queryDiversification(this.searchForm).then(res => {
        if (res.data.jsonObjects) {
          res.data.jsonObjects.forEach(item => {
            for (const key in item) {
              if (key == 'debtorBalance') {
                item[key] = Number(item[key])
              }
              if (item[key] === 0) {
                item[key] = null
              }
              // 增加千位符并且如果没有小数后两位添加两位0
              if (typeof item[key] == 'number' && item[key] != 0 && key != 'certificateId') {
                item[key] = separator(item[key])
              }
            }
          })
          this.multiColumnData = res.data.jsonObjects
          // 动态的头部数据
          if (res.data.subjects == null) {
            // 删除以前的
            this.multiColumnList = this.multiColumnList.filter(item => {
              return !item.hasOwnProperty('isDelete')
            })
          } else {
          // 先删除原来的
            this.multiColumnList = this.multiColumnList.filter(item => {
              return !item.hasOwnProperty('isDelete')
            })
            // 添加新的 判断是贷方还是借方
            // 借方
            const debtorItemTitle = {
              label: '借方',
              isDelete: true,
              children: []
            }
            // 贷方
            const creditItemTitle = {
              label: '贷方',
              isDelete: true,
              children: []
            }
            res.data.subjects.forEach(item => {
              const childrenItem = {
                label: '',
                field: '',
                type: true
              }
              childrenItem.label = `${item.number}/${item.subjectName}`
              childrenItem.field = item.subjectId
              if (item.balanceDirection == 1) {
                debtorItemTitle.children.push(childrenItem)
              } else if (item.balanceDirection == 2) {
                creditItemTitle.children.push(childrenItem)
              }
            })
            if (debtorItemTitle.children.length > 0) {
              this.multiColumnList.push(debtorItemTitle)
            }
            if (creditItemTitle.children.length > 0) {
              this.multiColumnList.push(creditItemTitle)
            }
          }
        } else {
          this.multiColumnData = []
        }
        this.loading = false
      }).catch((err) => {
        console.log(err)
        this.loading = false
      })
    },
    /**
     * @description: 打开凭证
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
</style>
