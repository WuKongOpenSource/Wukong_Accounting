<template>
  <div v-loading="loading" class="app-container">
    <div class="content-header">
      <span>财务初始余额</span>
      <div class="buttons">
        <el-button
          v-if="$auth('finance.financialInitial.update')"
          type="primary"
          @click="saveSubmit"
        >保存</el-button
        >
        <el-button type="primary" @click="trialBalance">试算平衡</el-button>
        <el-dropdown
          trigger="click"
          class="receive-drop"
        >
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <!-- TODO 后期权限 -->
            <el-dropdown-item v-if="$auth('finance.financialInitial.export')" @click.native="importAll">导入</el-dropdown-item>
            <el-dropdown-item v-if="$auth('finance.financialInitial.export')" @click.native="exportAll">导出</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

      </div>
    </div>

    <flexbox class="content-flex" justify="space-between">
      <flexbox class="search">
        <flexbox-item>
          <el-button v-for="(item,index) in balanceTypeOptions" :key="index" :type="balanceType==item.value?'selected':''" @click="balanceType=item.value">
            {{ item.label }}
          </el-button>
        </flexbox-item>
      </flexbox>
    </flexbox>
    <div class="balance-table">
      <el-table
        id="balance-table"
        :data="balanceData"
        :height="tableHeight"
        :cell-style="getCellStyle"
        :row-height="50"
        use-virtual
        style="width: 100%;"
        stripe
      >
        <el-table-column
          v-for="(item, index) in balanceList"
          :key="index"
          :prop="item.field"
          :label="item.label"
          :fixed="item.fixed"
          :width="item.width"
          :formatter="fieldFormatter"
          show-overflow-tooltip
        >
          <template v-if="item.columns && item.columns.length">
            <el-table-column
              v-for="(children, cIndex) in item.columns"
              :key="cIndex"
              :prop="children.field"
              :label="children.label"
              :formatter="fieldFormatter"
              show-overflow-tooltip
            >
              <template slot-scope="scope">
                <span
                  v-if="getDisabledStatus(scope.row, children, scope.$index)"
                >{{ scope.row[children.field] && separator(scope.row[children.field]) }}</span
                >
                <el-input-number
                  v-if="!getDisabledStatus(scope.row, children, scope.$index)"
                  v-model="scope.row[children.field]"
                  :controls="false"
                  @paste.native="inputPaste($event,scope.row,children)"
                  @change="inputChange(scope.row, children)"
                />
              </template>
            </el-table-column>
          </template>
          <template v-if="!item.columns" slot-scope="scope">
            <flexbox class="name-box">
              <template v-if="item.field === 'number'">
                {{ getAssistNumber(scope.row) }}
              </template>
              <div v-else-if="item.field === 'subjectName'" class="subjectName">
                {{ getAssistName(scope.row) }}
              </div>
              <template v-else>
                {{ fieldFormatter(scope.row, item) }}
              </template>
              <i
                v-if="
                  item.field === 'subjectName' &&
                    scope.row.subjectAdjuvantList&&
                    scope.row.subjectAdjuvantList.length
                "
                class="el-icon-plus"
                @click="handleToAddAssist(scope.row)"
              />
              <i
                v-else-if="
                  item.field === 'subjectName' &&
                    scope.row.assistId
                "
                class="el-icon-close"
                @click="handleDeleteAssist(scope.row)"
              />
            </flexbox>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <calculate-balance
      :dialog-visible="calculateBalanceVisible"
      @close="calculateBalanceVisible = false"
    />

    <add-assist-dialog
      v-if="addAssistVisible"
      :visible.sync="addAssistVisible"
      :parent-node="assistParentNode"
      :list="balanceData"
      @update="getBalanceGroupList"
    />
  </div>
</template>

<script>
import {
  fmFinanceInitialListAPI,
  fmFinanceCurrencyListAPI,
  fmFinanceInitialUpdateAPI,
  fmFinanceDeleteFinanceInitialAPI,
  fmFinanceParameterQueryParameterAPI,
  exportExportPageBySubjectTypeAPI,
  financeCertificateDownloadFinanceInitialExcelAPI,
  financeCertificateFinanceInitialImportAPI
} from '@/api/fm/setting'

import CalculateBalance from './CalculateBalance'
import AddAssistDialog from './AddAssistDialog'

import numeral from 'numeral'
import { mapGetters } from 'vuex'
// import moment from 'moment'
import NP from 'number-precision'
// import * as XLSX from 'xlsx'
import { downloadExcelWithResData } from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'

export default {
  name: 'InitIalBalanceSet',
  components: {
    CalculateBalance,
    AddAssistDialog
  },

  data() {
    return {
      separator: separator,
      loading: false, // 展示加载中效果
      isRequest: false,
      tableHeight: document.documentElement.clientHeight - 300,
      balanceType: 1,
      balanceTypeOptions: [
        { label: '资产', value: 1 },
        { label: '负债', value: 2 },
        { label: '权益', value: 3 },
        { label: '成本', value: 4 },
        { label: '损益', value: 5 }
      ],
      currencyOptions: [],
      currencyType: '',
      startTime: '',
      calculateBalanceVisible: false,
      balanceList: [],
      // 余额设置
      /** 余额每行的信息 */
      balanceData: [],
      allPrarentIds: [],
      tableShow: true,

      addAssistVisible: false,
      assistParentNode: null
    }
  },
  computed: {
    ...mapGetters(['financeCurrentAccount']),
    isTheSameTime() {
      return this.$moment(this.financeCurrentAccount.enableTime).format('YYYYMM') === this.$moment(this.financeCurrentAccount.startTime).format('YYYYMM')
    }
  },
  watch: {
    balanceType(after, before) {
      console.log(after, before)
      if (after == 5 && this.startTime.split('-')[1] == '01') {
        this.balanceType = before
        this.$message.warning('年初启用的账套，不需要录入损益初始余额哦！')

        return
      }
      this.getBalanceGroupList()
      this.balanceList = []
      this.$nextTick(() => {
        this.getFieldList()
      })
    },
    currencyType() {
      this.getBalanceGroupList()
    }
  },
  created() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 300
    }

    this.initStartTime()
    this.getBalanceGroupList()
    this.getCurrencyOptions()
  },
  methods: {
    // 导入初始余额
    importAll() {
      this.$wkImport.import('fmInitialBalance', {
        typeName: '初始余额',
        historyShow: false,
        noImportProcess: true,
        ownerSelectShow: false,
        repeatHandleShow: false,
        downloadErrFuc: async() => {},
        importRequest: async(params) => {
          return financeCertificateFinanceInitialImportAPI(params).then(res => {
            console.log(res, this)
            this.initStartTime()
            this.getBalanceGroupList()
            this.getCurrencyOptions()
            return {
              data: {
                totalSize: res.data,
                errSize: 0
              }
            }
          })
        },
        templateRequest: financeCertificateDownloadFinanceInitialExcelAPI,
        userInfo: this.userInfo,
        coverShow: false
      })
    },

    /**
     * 余额列表
     */
    getBalanceGroupList() {
      this.getList(1, [], this.balanceType)
    },
    /**
     * @description: 获取财务初始余额列表数据
     * @param {*} page
     * @param {*} list
     * @param {*} balanceType
     */
    getList(page = 1, list = [], balanceType) {
      if (this.balanceType !== balanceType) return
      this.isRequest = true
      if (page === 1) this.loading = true
      fmFinanceInitialListAPI({
        subjectType: balanceType,
        limit: 500,
        pageType: 1,
        page: page
      })
        .then((res) => {
          if (this.balanceType !== balanceType) return
          this.$nextTick(() => {
            this.balanceData = list.concat(res.data.list)
            this.loading = false
            if (this.balanceData.length < res.data.totalRow) {
              this.getList(page + 1, this.balanceData, balanceType)
            } else {
              this.isRequest = false
              this.$nextTick(() => {
                this.allPrarentIds = this.balanceData.filter(o => o.parentId !== '0').map(o => o.parentId)
              })
            }
          })
        })
        .catch(() => {
          this.loading = false
        })
    },
    getCurrencyOptions() {
      fmFinanceCurrencyListAPI().then((res) => {
        this.currencyOptions = res.data
      })
    },
    exportAll() {
      exportExportPageBySubjectTypeAPI({
        subjectType: this.balanceType,
        limit: 500,
        pageType: 1,
        page: 1
      }).then((res) => {
        downloadExcelWithResData(res)
      })
      // var fileName
      // if (this.financeCurrentAccount && this.financeCurrentAccount.companyName) {
      //   fileName = `${this.financeCurrentAccount.companyName}${moment(this.financeCurrentAccount.startTime).format('YYYY年第MM期')}.xlsx`
      // } else {
      //   fileName = 'balance.xlsx'
      // }
      // // exportElTable(fileName, 'balance-table')
      // const head = []
      // const headtwo = []
      // const fields = []
      // this.balanceList.forEach(field => {
      //   head.push(field.label)
      //   if (field.columns?.length) {
      //     head.push('')
      //     field.columns.forEach(column => {
      //       headtwo.push(column.label)
      //       fields.push(column.field)
      //     })
      //   } else {
      //     fields.push(field.field)
      //     headtwo.push('')
      //   }
      // })
      // const tableBody = this.balanceData.map((o) => {
      //   const data = []
      //   fields.forEach(fieldName => {
      //     if (fieldName === 'balanceDirection') {
      //       data.push(o[fieldName] === 1 ? '借' : '贷')
      //     } else {
      //       data.push(o[fieldName])
      //     }
      //   })
      //   return data
      // })
      // const worksheet = XLSX.utils.aoa_to_sheet([head, headtwo, ...tableBody])
      // const workbook = XLSX.utils.book_new()
      // XLSX.utils.book_append_sheet(workbook, worksheet, '第一页')
      // XLSX.writeFile(workbook, fileName)
    },
    /**
     * 辅助核算关联名称
     * @param row
     * @return {string|string}
     */
    getAssistName(row) {
      return row.carteName ? `${row.subjectName}_${row.carteName}` : row.subjectName
    },

    /**
     * 辅助核算关联编号
     * @param row
     * @return {string|string}
     */
    getAssistNumber(row) {
      return row.carteNumber ? `${row.number}_${row.carteNumber}` : row.number
    },

    getCellStyle({ row, column, rowIndex, columnIndex }) {
      if (columnIndex !== 1) return {}
      return {
        paddingLeft: ((row.grade || 1) - 1) * 13 + 'px'
      }
    },

    initStartTime() {
      fmFinanceParameterQueryParameterAPI().then((res) => {
        this.startTime = res.data.startTime
        this.getFieldList()
      })
    },

    /**
     * 余额列表格式化
     */
    fieldFormatter(row, column) {
      if (column.field == 'balanceDirection') {
        return row[column.field] == 1 ? '借' : '贷'
      }
      return row[column.field]
    },

    getFieldList() {
      let list = null
      const arr0 = [
        { label: '科目编码', field: 'number', width: 130, fixed: 'left' },
        { label: '科目名称', field: 'subjectName', width: 300 },
        { label: '方向', field: 'balanceDirection', width: 80 }
      ]
      const initialBalance = {
        label: '期初余额',
        field: 'initialBalance',
        type: 'input',
        columns: [
          { label: '数量', field: 'initialNum', type: 'input', showType: 'number' },
          { label: '金额', field: 'initialBalance', type: 'input' }
        ]
      }
      const addUpDebtorBalance = {
        label: '本年累计借方',
        field: 'addUpDebtorBalance',
        type: 'input',
        columns: [
          { label: '数量', field: 'addUpDebtorNum', type: 'input', showType: 'number' },
          { label: '金额', field: 'addUpDebtorBalance', type: 'input' }
        ]
      }
      const addUpCreditBalance = {
        label: '本年累计贷方',
        field: 'addUpCreditBalance',
        type: 'input',
        columns: [
          { label: '数量', field: 'addUpCreditNum', type: 'input', showType: 'number' },
          { label: '金额', field: 'addUpCreditBalance', type: 'input' }
        ]
      }
      const beginningBalance = {
        label: '年初余额',
        field: 'beginningBalance',
        type: 'input',
        columns: [
          { label: '数量', field: 'beginningNum', showType: 'number' },
          { label: '金额', field: 'beginningBalance' }
        ]
      }
      if (this.startTime.split('-')[1] == '01') {
        arr0[0].width = 200
        arr0[1].width = 500
        arr0[2].width = 150
        list = [...arr0, initialBalance]
      } else if (this.balanceType == 5) {
        list = [
          ...arr0,
          initialBalance,
          addUpDebtorBalance,
          addUpCreditBalance,
          beginningBalance,
          {
            label: '实际损益发生额',
            type: 'input',
            field: 'profitBalance',
            columns: [
              { label: '数量', field: 'profitBalanceNum', type: 'input', showType: 'number' },
              { label: '金额', field: 'profitBalance', type: 'input' }
            ]
          }
        ]
      } else {
        list = [
          ...arr0,
          initialBalance,
          addUpDebtorBalance,
          addUpCreditBalance,
          beginningBalance
        ]
      }
      this.balanceList = list
    },
    // 粘贴千位制金额
    inputPaste(e, row, field) {
      this.$nextTick(() => {
        row[field.field] = numeral(e.target.value).format('0.00')
        this.inputChange(row, field)
      })
    },
    /**
     * 联动计算合计
     * @param row
     * @param column
     */
    inputChange(row, column) {
      var balance, num
      row.isChange = true
      if (row.balanceDirection === 1) {
        balance = (Number(row.initialBalance) || 0) - (Number(row.addUpDebtorBalance) || 0) + (Number(row.addUpCreditBalance) || 0)
        num = (Number(row.initialNum) || 0) - (Number(row.addUpDebtorNum) || 0) + (Number(row.addUpCreditNum) || 0)
      } else {
        balance = (Number(row.initialBalance) || 0) + (Number(row.addUpDebtorBalance) || 0) - (Number(row.addUpCreditBalance) || 0)
        num = (Number(row.initialNum) || 0) + (Number(row.addUpDebtorNum) || 0) - (Number(row.addUpCreditNum) || 0)
      }
      this.$set(row, 'beginningNum', num || 0)
      this.$set(row, 'beginningBalance', balance || 0)
      if (row.assistId) {
        const adjuvantNode = this.balanceData.find(
          (o) => o.subjectId === row.subjectId && o.subjectAdjuvantList
        )
        const adjuvantFlag = Number(adjuvantNode.balanceDirection)
        const assistNode = this.balanceData.filter(
          (o) => o.subjectId === row.subjectId && o.assistId
        )
        console.log(adjuvantNode)
        const assRes = assistNode
          .map((item) => {
            const number = Number(item[column.field] || 0) || 0
            const childFlag = Number(item.balanceDirection)
            if (['addUpDebtorBalance', 'addUpDebtorNum', 'addUpCreditNum', 'addUpCreditBalance'].includes(column.field)) {
              return number
            } else {
              return adjuvantFlag === childFlag ? number : -number
            }
          })
          .reduce((acc, current) => NP.plus(acc, current))
        this.$set(adjuvantNode, column.field, assRes)
        this.inputChange(adjuvantNode, column)
        return
      }
      // console.log('inputChange', row, column)
      const parentNode = this.balanceData.find(
        (o) => o.subjectId === row.parentId
      )
      console.log(parentNode)
      if (!parentNode) return

      // 如果自节点的方向与父节点的方向相同金额不变，方向相反则取相反数
      const parentFlag = Number(parentNode.balanceDirection)
      const botherNode = this.balanceData.filter(
        (o) => o.parentId === row.parentId && !o.assistId
      )
      const accRes = botherNode
        .map((item) => {
          const number = Number(item[column.field] || 0) || 0
          const childFlag = Number(item.balanceDirection)
          if (['addUpDebtorBalance', 'addUpDebtorNum', 'addUpCreditNum', 'addUpCreditBalance'].includes(column.field)) {
            return number
          } else {
            return parentFlag === childFlag ? number : -number
          }
        })
        .reduce((acc, current) => NP.plus(acc, current))
      this.$set(parentNode, column.field, accRes)
      this.inputChange(parentNode, column)
    },

    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.getBalanceGroupList()
    },

    /**
     * 添加辅助核算关联
     * @param row
     */
    handleToAddAssist(row) {
      this.assistParentNode = row
      this.addAssistVisible = true
      // console.log('addAssistVisible: ', this.addAssistVisible)
    },
    deleteAssistBalance(row) {
      row.isChange = true
      let fieldArr = [
        'initialBalance',
        'addUpDebtorBalance',
        'addUpCreditBalance',
        'beginningBalance',
        'profitBalance'
      ]
      if (row.isAmount) {
        fieldArr = fieldArr.concat([
          'initialNum',
          'addUpDebtorNum',
          'addUpCreditNum',
          'beginningNum',
          'profitBalance'
        ])
      }
      if (row.assistId) {
        const adjuvantNode = this.balanceData.find(
          (o) => o.subjectId === row.subjectId && o.subjectAdjuvantList
        )
        const adjuvantFlag = Number(adjuvantNode.balanceDirection)
        const assistNode = this.balanceData.filter(
          (o) =>
            o.subjectId === row.subjectId &&
            o.assistId &&
            o.assistId !== row.assistId
        )

        const assResArr = assistNode.map((item) => {
          const obj = {}
          fieldArr.forEach((key) => {
            obj[key] = (function() {
              const number = Number(item[key] || 0) || 0
              const childFlag = Number(item.balanceDirection)
              if (['addUpDebtorBalance', 'addUpDebtorNum', 'addUpCreditNum', 'addUpCreditBalance'].includes(key)) {
                return number
              } else {
                return adjuvantFlag === childFlag ? number : -number
              }
            })()
          })
          return obj
        })

        let assRes = {}
        if (!assResArr.length) {
          const numericalObj = {}
          fieldArr.forEach((key) => {
            numericalObj[key] = 0
          })
          assRes = numericalObj
        } else {
          assRes = assResArr.reduce((acc, current) => {
            const numberObj = {}
            fieldArr.forEach((item) => {
              numberObj[item] = NP.plus(acc[item], current[item])
            })
            return numberObj
          })
        }

        fieldArr.forEach((key) => {
          this.$set(adjuvantNode, key, assRes[key])
        })
        this.deleteAssistBalance(adjuvantNode)
        return
      }
      // console.log('inputChange', row, column)
      const parentNode = this.balanceData.find(
        (o) => o.subjectId === row.parentId
      )
      if (!parentNode) return

      // 如果自节点的方向与父节点的方向相同金额不变，方向相反则取相反数
      const parentFlag = Number(parentNode.balanceDirection)
      const botherNode = this.balanceData.filter(
        (o) => o.parentId === row.parentId && !o.assistId
      )
      const accResArr = botherNode.map((item) => {
        const obj = {}
        fieldArr.forEach((key) => {
          obj[key] = (function() {
            const number = Number(item[key] || 0) || 0
            const childFlag = Number(item.balanceDirection)
            if (['addUpDebtorBalance', 'addUpDebtorNum', 'addUpCreditNum', 'addUpCreditBalance'].includes(key)) {
              return number
            } else {
              return parentFlag === childFlag ? number : -number
            }
          })()
        })
        return obj
      })

      let assRes = {}
      if (!accResArr.length) {
        const numericalObj = {}
        fieldArr.forEach((key) => {
          numericalObj[key] = 0
        })
        assRes = numericalObj
      } else {
        assRes = accResArr.reduce((acc, current) => {
          const numberObj = {}
          fieldArr.forEach((item) => {
            numberObj[item] = NP.plus(acc[item], current[item])
          })
          return numberObj
        })
      }

      fieldArr.forEach((key) => {
        this.$set(parentNode, key, assRes[key])
      })
      this.deleteAssistBalance(parentNode)
    },
    handleDeleteAssist(row) {
      fmFinanceDeleteFinanceInitialAPI({ assistId: row.assistId }).then(
        (res) => {
          this.deleteAssistBalance(row)
          this.saveSubmit()
          this.$nextTick(() => {
            this.getBalanceGroupList()
          })
        }
      ).catch(err => { console.log(err) })
    },
    saveSubmit() {
      const submitArray = []
      for (let i = 0; i < this.balanceData.length; i++) {
        const element = this.balanceData[i]
        if (
          element.isChange
        ) {
          submitArray.push({
            initialId: element.initialId,
            initialNum: element.initialNum,
            initialBalance: element.initialBalance,
            addUpDebtorNum: element.addUpDebtorNum,
            addUpDebtorBalance: element.addUpDebtorBalance,
            addUpCreditNum: element.addUpCreditNum,
            addUpCreditBalance: element.addUpCreditBalance,
            beginningNum: element.beginningNum,
            beginningBalance: element.beginningBalance,
            profitBalanceNum: element.profitBalanceNum,
            profitBalance: element.profitBalance
          })
        }
      }
      if (
        this.balanceType == 5 &&
        submitArray.find((item) => Number(item.beginningBalance))
      ) {
        this.$message.error('损益类科目的年初余额应该为零')
        return
      }
      this.loading = true
      fmFinanceInitialUpdateAPI(submitArray)
        .then((res) => {
          this.$message.success('保存成功')
          this.getBalanceGroupList()
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },

    trialBalance() {
      this.calculateBalanceVisible = true
    },

    /**
     * 判断输入框是否允许编辑
     * @param data
     * @param field
     * @param index
     * @return {boolean}
     */
    getDisabledStatus(data, field, index) {
      // return true
      if (!this.isTheSameTime) return true
      if (this.isRequest) return true
      if (!field.type || field.type !== 'input') return true
      if (field.showType === 'number') {
        // 如果当前行是辅助核算，并且没有明细
        if (data.subjectAdjuvantList) {
          const nextRow = this.balanceData[index + 1]
          if (nextRow) {
            return nextRow.number === data.number || Number(!data.isAmount)
          }
        }

        return Number(!data.isAmount) || data.subjectAdjuvantList
      }
      if (this.allPrarentIds.includes(data.subjectId)) return true
      if (data.subjectAdjuvantList) {
        const nextRow = this.balanceData[index + 1]
        if (nextRow) {
          return nextRow.subjectId === data.subjectId && nextRow.assistId
        }
      }
      return false
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "../styles/index.scss";

.content-header {
  padding-bottom: 0;

  .buttons {
    font-size: 0;

    .dropdown-btn {
      margin-left: 8px;
    }
  }
}

.content-flex {
  .search {
    .search-item {
      margin-left: 10px;
    }
  }
}

/* 余额设置 */

.balance-table {
  .el-input-number {
    width: 100%;

    ::v-deep .el-input__inner {
      padding-right: 10px;
      padding-left: 10px;
      text-align: left;
    }
  }

  .name-box {
    position: relative;
    padding-right: 15px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .subjectName {
      width: 250px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .el-icon-close,
    .el-icon-plus {
      position: absolute;
      top: 50%;
      right: 0;
      font-weight: bolder;
      color: #999;
      cursor: pointer;
      transform: translateY(-50%);
    }
  }
}

::v-deep.el-table .cell {
  width: 100% !important;
}

// .numMondy{
//   position: relative;
//   width: 100%;
//   height: 100%;

// }
</style>
