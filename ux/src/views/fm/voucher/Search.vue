<template>
  <div class="app-container">
    <flexbox
      v-loading="loading"
      :gutter="0"
      align="flex-start"
      justify="flex-start"
      direction="column"
      class="main">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">
          查凭证
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="37"
            data-id="305" />
        </span>
        <div class="buttons">
          <el-button
            v-if="$auth('finance.subject.save')"
            type="primary"
            @click="addSubject">新增凭证</el-button>
          <el-dropdown
            v-if="moreType.length"
            trigger="click"
            class="receive-drop"
            @command="moreClick">
            <el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-for="(item,index) in moreType"
                :key="index"
                :command="item.type">
                {{ item.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </flexbox>
      <flexbox
        class="content-flex"
        justify="flex-start">
        <flexbox class="search">
          <search-popover-wrapper
            :search-time.sync="searchTime">
            <search-form
              :default-value="searchForm"
              @change="debounceSearchSubmit($event)" />
          </search-popover-wrapper>
          <el-button
            v-debounce="getList"
            type="subtle"
            icon="el-icon-refresh-right" />
        </flexbox>

      </flexbox>

      <flexbox-item class="voucher-table">
        <el-table
          id="table"
          :data="voucherData"
          :height="tableHeight"
          :cell-class-name="cellClassName"
          stripe
          @row-click="handleRowClick"
          @selection-change="handleSelectionChange">
          <el-table-column
            type="selection"
            align="center"
            width="40" />
          <el-table-column
            v-for="(item, index) in voucherList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            :width="item.width"
            :formatter="fieldFormatter"
            :align="item.align"
            :show-overflow-tooltip="!['digestContent','subjectNumber'].includes(item.field)"
            header-align="left">
            <template slot-scope="scope">
              <flexbox
                v-if="item.type=='double' && (item.field == 'subjectNumber' || item.field == 'digestContent')"
                :class="item.className"
                direction="column"
                class="double">
                <el-tooltip
                  v-for="(td,tdIndex) in scope.row.certificateDetails"
                  :key="tdIndex"
                  :content="fieldFormatter(td,item)"
                  :class="`${item.field}_`+scope.row.certificateId+'_'+tdIndex"
                  :disabled="item.field == 'subjectNumber'?td.disabled : td.digestDisabled"
                  effect="dark"
                  placement="top">
                  <div class="td subject-name">{{ fieldFormatter(td,item) }}</div>
                </el-tooltip>
              </flexbox>
              <flexbox
                v-else-if="item.type=='double'"
                :class="item.className"
                direction="column"
                class="double">
                <div
                  v-for="(td,tdIndex) in scope.row.certificateDetails"
                  :key="tdIndex"
                  class="td">
                  {{ fieldFormatter(td,item) }}
                </div>
              </flexbox>
              <template v-else-if="item.field=='fileNum'">
                <i class="wk wk-attachment" />{{ scope.row.fileNum }}
              </template>
              <span v-else>{{ fieldFormatter(scope.row,item) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="240"
          >
            <template slot-scope="{row,column,$index}">
              <el-button type="primary-text" size="default" @click="operateHandle('edit',row,column,$index)">编辑</el-button>
              <!-- <el-button type="primary-text" size="default" @click="operateHandle('copy',row,column,$index)">复制</el-button> -->
              <el-button v-if="row.checkStatus ==0 && $auth('finance.voucher.examine') " type="primary-text" size="default" @click="operateHandle('check',row,column,$index)">审核</el-button>
              <el-button v-if="row.checkStatus ==1 && $auth('finance.voucher.noExamine')" type="primary-text" size="default" @click="operateHandle('deCheck',row,column,$index)">反审核</el-button>
              <el-button v-if="$auth('finance.voucher.print')" type="primary-text" size="default" @click="operateHandle('print',row,column,$index)">打印</el-button>
              <el-button v-if="$auth('finance.voucher.delete')" type="primary-text" size="default" @click="operateHandle('delete',row,column,$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="p-contianer">
          <el-pagination
            :current-page="currentPage"
            :page-sizes="pageSizes"
            :page-size.sync="pageSize"
            :total="total"
            class="p-bar"
            background
            layout="prev, pager, next, sizes, total, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange" />
        </div>
      </flexbox-item>
    </flexbox>

    <tidy-voucher
      v-if="tidyDialogVisible"
      :tidy-dialog-visible="tidyDialogVisible"
      @handle-success="getList"
      @close="tidyDialogVisible=false" />

    <insert-voucher
      v-if="insertDialogVisible"
      :insert-dialog-visible="insertDialogVisible"
      @handle-success="getList"
      @close="insertDialogVisible=false" />

    <el-dialog
      v-if="fileDialogVisible"
      :visible.sync="fileDialogVisible"
      title="查看附件"
      width="570px">
      <reminder
        style="display: inline-block;"
        content="可批量上传jpg、png、bmp、jpeg等图片文件，最大支持一次性上传100M" />
      <voucher-files
        :batch-id="fileTemp.batchId"
        @change="fileChange" />
      <div slot="footer">
        <el-button type="primary" @click="fileDialogVisible = false">确定</el-button>
      </div>
    </el-dialog>

    <PrintPageSetDialog
      v-if="printSizeDialogVisible"
      :visible.sync="printSizeDialogVisible"
      @confirm="sizeConfirm"
    />
  </div>

</template>

<script>
import {
  fmFinanceVoucherQueryListAPI,
  fmFinanceVoucherCheckStatusAPI,
  fmFinanceCertificateDownloadExcelAPI,
  fmFinanceCertificateExcelImportAPI,
  fmFinanceVoucherDeleteByIdsAPI,
  exportCertificateAPI
} from '@/api/fm/voucher'
import {
  fmFinanceSubjectListAPI
} from '@/api/fm/setting'
import { financePrintPreviewAPI, financeCertificatePreviewFinanceAPI } from '@/api/fm/voucher.js'
import SearchPopoverWrapper from '../components/SearchPopoverWrapper'
import SearchForm from './components/SearchForm'
import TidyVoucher from './components/TidyVoucher'
import InsertVoucher from './components/InsertVoucher'
import VoucherFiles from './components/VoucherFiles'
import Reminder from '@/components/Reminder'
import PrintPageSetDialog from '@/views/fm/voucher/components/PrintPageSetDialog.vue'

import Lockr from 'lockr'
import { mapActions, mapGetters } from 'vuex'
import { debounce } from 'throttle-debounce'
import { downloadExcelWithResData } from '@/utils'
import { isEmpty } from '@/utils/types'
import { separator } from '@/filters/vueNumeralFilter/filters'
import PrintMixin from '@/views/fm/mixins/Print.js'
import Nzhcn from 'nzh/cn'
import { accAdd } from '@/utils/acc.js'
import { LOCAL_FM_PAGE_SIZE } from '@/utils/constants.js'

export default {
  name: 'VoucherSearch',
  components: {
    SearchPopoverWrapper,
    SearchForm,
    TidyVoucher,
    InsertVoucher,
    VoucherFiles,
    Reminder,
    PrintPageSetDialog
  },
  mixins: [PrintMixin],
  beforeRouteEnter(to, from, next) {
    // 此处无法直接设置 this.a this.b
    next(vm => {
      console.log(vm)
      if (from.query.params && from.query.listParams) {
        vm.searchForm = { ...vm.searchForm, ...from.query.params }
        vm.currentPage = from.query.listParams.currentPage
        vm.scrollTop = from.query.scrollTop || 0
      }
    })
  },
  data() {
    return {
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 280, // 表的高度
      currencyOptions: [],
      searchForm: {},
      multipleSelection: [],
      voucherList: [
        { label: '日期', field: 'certificateTime', width: '100px' },
        { label: '凭证字号', field: 'certificateNum', width: '100px' },
        { label: '摘要', field: 'digestContent', type: 'double', className: 'left' },
        { label: '科目', field: 'subjectNumber', type: 'double', className: 'left' },
        { label: '借方金额', field: 'debtorBalance', type: 'double', align: 'right', width: '150px' },
        { label: '贷方金额', field: 'creditBalance', type: 'double', align: 'right', width: '150px' },
        { label: '附件', field: 'fileNum', width: '80px' },
        { label: '制单人', field: 'createUserName', width: '100px' },
        { label: '审核人', field: 'examineUserName', width: '100px' }
      ],

      currentPage: 1,
      pageSize: Lockr.get(LOCAL_FM_PAGE_SIZE) || 10,
      pageSizes: [10, 20, 30, 40],
      total: 0,

      subjectList: [],
      debounceSearchSubmit: null,
      tidyDialogVisible: false, //  整理
      insertDialogVisible: false, // 插入
      fileDialogVisible: false,
      fileTemp: null,
      scorllTop: 0,
      // 余额设置
      /** 余额每行的信息 */
      voucherData: [],
      printSizeDialogVisible: false, // 打印弹窗
      printType: ''
    }
  },
  computed: {
    moreType() {
      const lib = [
        { name: '打印凭证', type: 'printVoucher', show: this.$auth('finance.voucher.print') },
        { name: '审核', type: 'examine', show: this.$auth('finance.voucher.examine') && this.multipleSelection.length },
        { name: '反审核', type: 'cancelAudit', show: this.$auth('finance.voucher.noExamine') && this.multipleSelection.length },
        { name: '打印列表', type: 'print', show: this.$auth('finance.voucher.print') },
        { name: '导出', type: 'export', show: this.$auth('finance.voucher.export') },
        { name: '批量删除', type: 'batchDelete', show: this.$auth('finance.voucher.delete') && this.multipleSelection.length },
        { name: '整理', type: 'tidy', show: this.$auth('finance.voucher.arrangement') },
        { name: '插入', type: 'insert', show: this.$auth('finance.voucher.insert') },
        { name: '导入', type: 'import', show: this.$auth('finance.voucher.import') }
      ]
      return lib.filter(item => item.show)
    },
    ...mapGetters(['financeCurrentAccount'])
  },
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 280
      this.isOverflowDisabled()
    }
    this.getSubjectList()
    this.debounceSearchSubmit = debounce(500, this.handleToSearch)
    this.getVoucherTimeRange()
  },
  methods: {
    ...mapActions(['getVoucherTimeRange']),
    /**
     * @description: 获取列表数据
     */
    getList() {
      this.loading = true
      fmFinanceVoucherQueryListAPI({
        page: this.currentPage,
        limit: this.pageSize,
        ...this.searchForm
      })
        .then(res => {
          this.loading = false
          const list = res.data.list || []
          list.forEach(item => {
            item.fileNum = item.fileEntityList ? item.fileEntityList.length : (item.fileNum || 0)
            item.certificateDetails.forEach((el, index) => {
              el.disabled = true
              el.digestDisabled = true
            })
          })
          this.voucherData = list

          const page = this.currentPage
          this.currentPage = 0
          this.$nextTick(() => {
            if (document.querySelector('.is-scrolling-none')) {
              document.querySelector('.is-scrolling-none').scrollTop = this.scrollTop
            }
            this.currentPage = page
            this.scrollTop = 0
          })
          this.isOverflowDisabled()

          this.total = res.data.totalRow
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * @description: 处理搜索数据
     * @param {*} data
     * @return {*}
     */
    handleToSearch(data) {
      this.searchForm = data
      this.currentPage = 1
      this.getList()
    },
    /**
     * @description: 判断元素 文本溢出时是否禁用提示（摘要、科目）
     * @param {*}
     * @return {*}
     */
    isOverflowDisabled() {
      this.$nextTick(() => {
        this.voucherData.forEach(item => {
          item.certificateDetails.forEach((el, index) => {
            // 科目
            const className = 'subjectNumber_' + item.certificateId + '_' + index
            const elment = document.querySelector(`.double .${className}`)
            if (elment && (elment.scrollWidth > elment.offsetWidth)) {
              el.disabled = false
            } else {
              el.disabled = true
            }
            // 摘要
            const digestClassName = 'digestContent_' + item.certificateId + '_' + index
            const digestElment = document.querySelector(`.double .${digestClassName}`)
            if (digestElment && (digestElment.scrollWidth > digestElment.offsetWidth)) {
              el.digestDisabled = false
            } else {
              el.digestDisabled = true
            }
          })
        })
      })
    },

    /**
     * @description: 删除凭证通过id
     */
    deleteById() {
      if (!this.multipleSelection.length) {
        this.$message.warning('请先选择你想要删除的凭证！')
        return
      }
      this.$confirm('您确认要删除此凭证吗？删除后将不可恢复，并会产生断号', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          const ids = this.multipleSelection.map(item => item.certificateId)
          this.loading = true
          fmFinanceVoucherDeleteByIdsAPI({ ids }).then(res => {
            this.getList()
            this.$message.success('操作成功')
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        })
        .catch(() => {
        })
    },
    /**
     * @description: 处理上传文件操作
     * @param {*} dataValue
     */
    fileChange(dataValue) {
      console.log('dataValue: ', dataValue, dataValue.length)
      this.$set(this.fileTemp, 'fileNum', dataValue.length)
    },
    /**
     * @description: 导出
     */
    exportAll() {
      let params = {}
      if (this.multipleSelection?.length) {
        params = { certificateIds: this.multipleSelection.map(item => item.certificateId), ...this.searchForm }
      } else {
        params = {
          page: this.currentPage,
          limit: this.pageSize,
          ...this.searchForm
        }
      }
      exportCertificateAPI(params).then(res => {
        downloadExcelWithResData(res)
      })
    },
    /**
     * @description:更多操作
     * @param {String} type
     */
    moreClick(type) {
      switch (type) {
        case 'batchDelete':
          this.deleteById()
          break
        case 'cancelAudit':
          this.checkStatus(0)
          break
        case 'examine':
          this.checkStatus(1)
          break
        case 'export':
          this.exportAll()
          break
        case 'import':
          this.importVoucher()
          break
        case 'tidy':
          this.tidyDialogVisible = true
          break
        case 'insert':
          this.insertDialogVisible = true
          break
        case 'print':
          // this.printSizeDialogVisible = true
          this.printVoucherList()
          // this.printType = 'printVoucherList'
          break
        case 'printVoucher':
          this.printSizeDialogVisible = true
          // this.printVoucher()
          this.printType = 'printVoucher'
          break
        default:
          break
      }
    },
    sizeConfirm(styleData) {
      if (this.printType == 'printVoucher') {
        this.printVoucher(styleData)
      } else if (this.printType == 'rowPrintVoucher') {
        this.printVoucher(styleData).then(_ => {
          this.multipleSelection = []
        })
      }
      this.printSizeDialogVisible = false
    },
    async printVoucherList() {
      let dataList = []
      try {
        this.loading = true
        const { data } = await fmFinanceVoucherQueryListAPI({
          pageType: 0,
          ...this.searchForm
        })
        dataList = data.list
        this.loading = false
      } catch (e) {
        this.loading = false
        return
      }
      dataList = this.voucherDataformat(dataList)

      const voucherList = this.voucherList.filter(item => item.field != 'fileNum')
      const rawHtml = `
              <head>
                <style>
                  @page {size: auto;size:A3;margin:6mm}
                  td,th{border:1px solid !important;padding: 10px 0;}
                  table{border-collapse: collapse;font-size:20px;}
                  .el-table__cell.gutter{border:none !important;}
                  .el-table__empty-text{text-align: center;width: 100%;}
                  tr{page-break-inside:avoid}
                  .el-table__empty-text{display:none}
                  .el-table__footer-wrapper{display:none}
                </style>
              </head>
              <div >
                <div style='text-align:center;font-size:30px; font-weight:bold'>凭证列表</div>
                <div style='display:flex; font-size:26px;padding: 10px 0'>
                  <span style="flex:1">编制单位：${this.financeCurrentAccount.companyName}</span>
                  <span style="flex:1; text-align:right;float: right;">
                  ${(this.displayTime ||
                    this.$moment(this.financeCurrentAccount.startTime, 'YYYYMM')
                      .format('YYYY年第MM期'))}
                  </span>
              </div>
            </div>
          <table border="1" cellspacing="0" cellpadding="0" style="border-collapse:collapse;width:100%">
            <tr>
              ${voucherList.map(item => `<td>${item.label}</td>`).join(' ')}
            </tr>
            ${dataList.map((listItem, index) => (`
            <tr>
              ${voucherList.map((voucherItem) => (
      this.isShowTd(dataList, listItem, voucherItem, index)
        ? `
            <td rowspan="${
      [
        'certificateTime',
        'certificateNum',
        'createUserName',
        'examineUserName'
      ].includes(voucherItem.field) ? listItem.rowspan : ''
      }">
             ${this.fieldFormatter(listItem, voucherItem) || ''}
            </td>` : ''
    )).join(' ')}
            </tr>
                    `)).join(' ')}
          </table>
                  `
      financePrintPreviewAPI({
        type: 'pdf',
        content: rawHtml
      }).then(res => {
        const data = res.data
        const routeData = this.$router.resolve({
          name: 'PrintPage',
          query: {
            key: data
          }
        })
        window.open(routeData.href, '_blank')
      })
    },
    voucherDataformat(list) {
      const arr = []
      list.forEach(item => {
        if (item.certificateDetails.length) {
          item.certificateDetails.forEach((el, index) => {
            arr.push({
              ...item, ...el,
              rowspan: index == 0 ? item.certificateDetails.length : '',
              examineUserName: el.examineUserName || ''
            })
          })
        }
      })
      return arr
    },
    async printVoucher(styleData) {
      const fieldList = [
        { label: '摘要', field: 'digestContent' },
        { label: '会计科目', field: 'subjectNumber' },
        { label: '借方金额', field: 'debtorBalance' },
        { label: '贷方金额', field: 'creditBalance' }
      ]
      let dataList = []
      if (this.multipleSelection?.length) {
        dataList = JSON.parse(JSON.stringify(this.multipleSelection))
      } else {
        this.loading = true
        try {
          const { data } = await fmFinanceVoucherQueryListAPI({
            pageType: 0,
            ...this.searchForm
          })
          this.loading = false
          dataList = data.list
        } catch (e) {
          this.loading = false
          return
        }
      }
      const newDataList = []
      for (let i = 0; i < dataList.length; i++) {
        const el = dataList[i]
        if (el.certificateDetails?.length) {
          el.totalDebite = this.computeTotal('totalDebite', el)
          el.totalCredit = this.computeTotal('totalCredit', el)
          if (el.certificateDetails.length <= 4) {
            const count = 4 - el.certificateDetails.length
            for (let k = 0; k < count; k++) {
              el.certificateDetails.push({ })
            }
            newDataList.push({ ...el, currentPage: 1, totalPage: 1 })
          } else {
            let arr = []
            let currentPage = 0
            const totalPage = Math.ceil(el.certificateDetails.length / 4)
            for (let j = 0; j < el.certificateDetails.length; j++) {
              const element = el.certificateDetails[j]
              if (j && (j % 4 == 0)) {
                newDataList.push({ ...el, certificateDetails: arr, currentPage: ++currentPage, totalPage })
                arr = []
                arr.push(element)
              } else {
                arr.push(element)
              }
            }
            if (arr.length) {
              const count = 4 - arr.length
              for (let i = 0; i < count; i++) {
                arr.push({ })
              }
              newDataList.push({ ...el, certificateDetails: arr, currentPage: ++currentPage, totalPage })
            }
            // arr.length && newDataList.push({ ...el, certificateDetails: arr, currentPage: ++currentPage, totalPage })
          }
        }
      }
      dataList = newDataList
      const rawHtml = `
      <head>
      <style>
        *{
          margin:0;padding:0;
          font-size:${styleData.fontSize || '14px'};
        }
        th,td{
          padding:20px 0;
        }
        .title{
          padding:10px 0;
        }
        .title-item{
          display:inline-block;
          width:33%;
        }
        .title-item:nth-child(2){
          text-align: center;
        }
        .title-item:nth-child(3){
          text-align: right;
        }
        .footer{
          padding:10px 0;
        }
        .footer-item{
          width:24%;
          display:inline-block;
        }
        .footer-item:nth-child(2){
          text-align: center;
        }
        .footer-item:nth-child(3){
          text-align: center;
        }
        .footer-item:nth-child(4){
          text-align: right;
        }
      </style>
    </head>
        ${dataList.map(selected => (
    `
    <div style="font-size: 20px;">
      <div style="font-size: 30px;text-align: center;">记账凭证</div>
      <div style="width: 200px;border-bottom: 1px solid;border-top: 1px solid;height: 5px; margin: 10px auto;"/>
    </div>
    <div style="overflow: hidden;">
      <span style="float:right;">
        附单据&nbsp;&nbsp;${selected.fileNum || ''}&nbsp;&nbsp;张
      </span>
    </div>
    <div class="title" style="display: flex;justify-content: space-between;padding: 5px 0;">
      <div class="title-item">
        <span>单位：${this.financeCurrentAccount.companyName}</span>
      </div>
      <div class="title-item">
        <span>日期：${this.voucherTime(selected.certificateTime)}</span>
      </div>
      <div class="title-item">
        <span> 凭证号：${selected.certificateNum + ' ' + '(' + selected.currentPage + '/' + selected.totalPage + ')'}</span>
      </div>
    </div>
    <table border="1" cellspacing="0" cellpadding="0" style="border-collapse: collapse;width:100%;page-break-inside:avoid;">
      <tr>
        ${fieldList.map(field => (
      `<th >
      ${field.label}
    </th>`
    )).join(' ')}
      </tr>
        ${selected.certificateDetails.map(data => (
      `<tr>
        ${fieldList.map(field => (
        `<td >
      ${this.fieldFormatter(data, field)}
    </td>`
      )).join(' ')}
      </tr>`
    )).join(' ')}

      <tr>
        <td colspan="2">合计： ${Nzhcn.toMoney(Number(selected.total), {
      outSymbol: false
    })} </td>
        <td> ${selected.totalDebite} </td>
        <td> ${selected.totalCredit} </td>
      </tr>
    </table>
    <div class="footer">
      <div class="footer-item">
        <span>财务主管：</span>
      </div>
      <div class="footer-item">
        <span>审核：${selected.examineUserName || ''}</span>
      </div>
      <div class="footer-item">
        <span>出纳：</span>
      </div>
      <div class="footer-item">
        <span>制单：${selected.createUserName}</span>
      </div>
    </div>
    <div style="page-break-after:always;"></div>
          `
  )).join(' ')}
      `
      financeCertificatePreviewFinanceAPI({
        type: 'pdf',
        content: rawHtml,
        orientation: styleData.orientation || 'Portrait', // Landscape,
        marginLeft: `${styleData.left || 0}mm`,
        marginTop: `${styleData.top || 0}mm`,
        pageWidth: `${styleData.width || 0}mm`,
        pageHeight: `${styleData.height}mm`
      }).then(res => {
        const data = res.data
        const routeData = this.$router.resolve({
          name: 'PrintPage',
          query: {
            key: data
          }
        })
        window.open(routeData.href, '_blank')
      })
      // this.HandlerPrint(rawHtml, { type: 'raw-html' })
    },
    computeTotal(type, data) {
      let num = 0
      data.certificateDetails.forEach(item => {
        num = type == 'totalDebite' ? accAdd(num, item.debtorBalance) : accAdd(num, item.creditBalance)
      })
      return separator(num)
    },
    voucherTime(date) {
      if (date) {
        const time = date.split('-')
        return `${time[0]}年第${time[1]}期`
      }
      return ''
    },
    isShowTd(list, row, column, rowIndex, columnIndex) {
      if (
        [
          'certificateTime',
          'certificateNum',
          'createUserName',
          'examineUserName'
        ].includes(column.field)) {
        if (rowIndex) {
          return list[rowIndex - 1].certificateId != row.certificateId
        }
      }
      return true
    },
    /**
     * @description: 更改每页展示数量
     * @param {*} val
     */
    handleSizeChange(val) {
      Lockr.set(LOCAL_FM_PAGE_SIZE, val)
      this.pageSize = val
      this.getList()
    },
    /**
     * @description: 导入凭证
     */
    importVoucher() {
      this.$wkImport.import('fmVoucher', {
        typeName: '凭证',
        historyShow: false,
        noImportProcess: true,
        ownerSelectShow: false,
        repeatHandleShow: false,
        importRequest: fmFinanceCertificateExcelImportAPI, // 导入请求
        templateRequest: fmFinanceCertificateDownloadExcelAPI, // 模板请求
        userInfo: this.userInfo,
        coverShow: false
      })
    },
    /**
     * @description: 更改当前页数
     * @param {Number} val -currentPage
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList()
    },

    /**
     * @description: 表格选中处理
     * @param {*} val
     */
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    /**
     * @description: 改变审核状态
     * @param {number} status
     */
    checkStatus(status) {
      if (!this.multipleSelection.length) {
        this.$message.warning(`请先选择你想要${status == 1 ? '审核' : '反审核'}的凭证！`)
        return
      }
      this.loading = true
      const ids = this.multipleSelection.map(item => item.certificateId)
      fmFinanceVoucherCheckStatusAPI({ ids: ids, status: status }).then(res => {
        this.$message.success('操作成功')
        this.getList()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /**
     * @description: 行点击操作处理
     * @param {*} row
     * @param {*} column
     * @param {*} event
     */
    handleRowClick(row, column, event) {
      if (column.type === 'selection') {
        return // 多选布局不能点击
      }
      if (column.property === 'certificateNum') {
        // console.log(document.getElementById('table').scrollTop)

        this.$router.push({
          path: '/fm/voucher/subs/create',
          query: {
            ids: this.voucherData.map(item => item.certificateId).join(','),
            index: this.voucherData.findIndex(item => item.certificateId == row.certificateId),
            params: {
              ...this.searchForm
            },
            listParams: {
              currentPage: this.currentPage
            },
            scrollTop: document.querySelector('.is-scrolling-none') && document.querySelector('.is-scrolling-none').scrollTop
          }
        })
      } else if (column.property == 'fileNum') {
        this.fileTemp = row
        this.fileDialogVisible = true
      }
    },
    /**
     * @description: 单元格类名添加
     * @param {*} row
     * @param {*} column
     * @param {*} rowIndex
     * @param {*} columnIndex
     */
    cellClassName({ row, column, rowIndex, columnIndex }) {
      if (column.property === 'certificateNum' || column.property === 'fileNum') {
        return 'can-visit--underline'
      }
    },
    /**
     * @description: 获取子项目中的名字
     * @param {*} name
     * @param {*} parentId
     * @return {string}
     */
    getSubjectName(name, parentId) {
      const parent = this.subjectList.find(item => item.subjectId === parentId)
      if (parent) {
        name = `${parent.subjectName}_${name}`
      }
      if (parent && Number(parent.parentId)) {
        name = this.getSubjectName(name, parent.parentId)
      }
      return name
    },
    /**
     * @description: 列表格式化
     * @param {*} row
     * @param {*} field
     * @return {*}
     */
    fieldFormatter(row, field) {
      // console.log(row, field)
      if (isEmpty(row)) return ' '
      if (field.field === 'subjectNumber') {
        let name = row.subjectName
        const subject = JSON.parse(row.subjectContent)
        if (Number(subject.parentId)) {
          name = this.getSubjectName(name, subject.parentId)
        }
        const arr = [name]
        row.associationBOS.forEach(o => {
          arr.push(o.carteNumber + o.name)
        })
        const valStr = arr.filter(o => o).join('_')
        return `${row[field.field]} ${valStr}`
      } else if (field.field === 'examineUserName') {
        if (!row.checkStatus) {
          return '未审核'
        } else {
          return row[field.field]
        }
      } else if (field.field === 'certificateTime') {
        return this.$moment(row[field.field]).format('YYYY-MM-DD')
      } else if (['debtorBalance', 'creditBalance'].includes(field.field)) {
        if (row[field.field]) {
          return separator(row[field.field])
        } else {
          return ''
        }
      }
      // 如果需要格式化
      return row[field.field]
    },
    /**
     * @description: 获取科目列表
     */
    getSubjectList() {
      fmFinanceSubjectListAPI({
        type: null,
        isTree: 0
      }).then((res) => {
        this.subjectList = res.data || []
      })
    },
    /**
     * @description: 添加
     */
    addSubject() {
      this.$router.push('/fm/voucher/subs/create')
    },

    /**
     * 行操作
     * @param {*} operateType
     * @param {*} row
     * @param {*} column
     * @param {*} index
     */
    operateHandle(operateType, row, column, index) {
      console.log(operateType, row, column, index)
      if (operateType === 'edit') {
        this.$router.push({
          path: '/fm/voucher/subs/create',
          query: {
            ids: this.voucherData.map(item => item.certificateId).join(','),
            index: this.voucherData.findIndex(item => item.certificateId == row.certificateId),
            params: {
              ...this.searchForm
            },
            listParams: {
              currentPage: this.currentPage
            },
            scrollTop: document.querySelector('.is-scrolling-none') && document.querySelector('.is-scrolling-none').scrollTop
          }
        })
      } else if (operateType === 'check') {
        this.updateCheckStatus([row.certificateId], 1)
      } else if (operateType === 'deCheck') {
        this.updateCheckStatus([row.certificateId], 0)
      } else if (operateType === 'print') {
        this.multipleSelection = [row]
        this.printSizeDialogVisible = true
        this.printType = 'rowPrintVoucher'
      } else if (operateType === 'delete') {
        this.$confirm('您确认要删除此凭证吗？删除后将不可恢复，并会产生断号', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            const ids = [row.certificateId]
            this.loading = true
            fmFinanceVoucherDeleteByIdsAPI({ ids }).then(res => {
              this.getList()
              this.$message.success('操作成功')
              this.loading = false
            }).catch(() => {
              this.loading = false
            })
          })
          .catch(() => {
          })
      }
    },
    updateCheckStatus(ids, status) {
      this.loading = true
      fmFinanceVoucherCheckStatusAPI({
        ids: ids,
        status: status
      })
        .then((res) => {
          this.$message.success('操作成功')
          this.getList()
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-dialog__body {
  padding: 20px;
}

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

      .search {
        .el-button {
          margin-left: 8px;
        }
      }

      .buttons {
        float: right;
        width: auto;

        .el-button {
          margin-left: $--button-padding-vertical;
        }

        .dropdown-btn {
          padding: $--button-padding-vertical;
          margin-left: $--button-button-margin-left;
        }
      }
    }

    .voucher-table {
      flex: 1;
      width: 100%;

      ::v-deep .el-table {
        &::before {
          display: none;
        }

        .el-table-column--selection {
          .cell {
            text-indent: initial;
          }
        }

        .cell {
          padding-right: 0;
          padding-left: 0;
          text-indent: 15px;
        }

        td {
          border-right: none;
          border-bottom: 1px solid $--border-color-base;
          border-left: 1px solid $--border-color-base;

          &:first-child {
            border-left: none;
          }
        }

        .double {
          .td {
            width: 100%;
            height: 40px;
            padding-right: 5px;
            line-height: 40px;
            text-align: right;
            border-bottom: 1px solid $--border-color-base;

            &:last-child {
              border-bottom: none;
            }
          }

          .td.subject-name {
            width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          &.left .td {
            text-align: left;
          }
        }

        .el-table__row td:nth-child(7) {
          border-color: $--border-color-base;
        }
      }
    }
  }

  .wk-file-form {
    margin-top: 10px;
  }
}

</style>
