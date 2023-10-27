<template>
  <div v-loading="loading" class="app-container">
    <div class="content-header">
      <span>
        币别设置
        <i
          class="wk wk-icon-fill-help wk-help-tips"
          data-type="42"
          data-id="334" />
      </span>
      <el-button
        v-if="$auth('finance.currency.save')"
        type="primary"
        @click="addCurrency">新建币别</el-button>
    </div>
    <div class="currency-table">

      <el-table
        :data="currencyData"
        :height="tableHeight"
        style="width: 100%;"
        stripe>
        <el-table-column
          v-for="(item, index) in currencyList"
          :key="index"
          :prop="item.field"
          :label="item.label"
          :formatter="fieldFormatter"
          show-overflow-tooltip />
        <el-table-column
          fixed="right"
          label="操作"
          width="140">
          <template slot-scope="scope">
            <el-button
              v-if="$auth('finance.currency.update')"
              :disabled="scope.row.status != 1"
              type="primary-text"
              @click="currencyEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="$auth('finance.currency.delete')"
              type="primary-text"
              @click="currencyDelete(scope)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <currency-create-dialog
      v-if="currencyDialogVisible"
      :action="action"
      :currency-dialog-visible="currencyDialogVisible"
      @currency-close="currencyClose"
      @currency-submit="currencySubmit" />
  </div>
</template>

<script>
import { fmFinanceCurrencyListAPI, fmFinanceCurrencyDeleteAPI } from '@/api/fm/setting'

import currencyCreateDialog from './components/Create'

export default {
  name: 'CurrencySet',
  components: {
    currencyCreateDialog
  },

  data() {
    return {
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 220,
      // 币别设置
      /** 币别每行的信息 */
      action: { type: 'add' },
      currencyData: [],
      currencyList: [
        { label: '编码', field: 'currencyCoding' },
        { label: '币别', field: 'currencyName' },
        { label: '汇率', field: 'exchangeRate' },
        // { label: '固定汇率', field: 'rate' },
        { label: '是否本位币', field: 'homeCurrency' }
        // { label: '小数位数', field: 'digit' }

      ],
      // 添加币别
      currencyDialogVisible: false
    }
  },
  watch: {
  },
  created() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 220
    }
    this.getCurrencyGroupList()
  },
  methods: {
    /**
     * 币别列表
     */
    getCurrencyGroupList() {
      this.loading = true
      fmFinanceCurrencyListAPI()
        .then(res => {
          this.loading = false
          this.currencyData = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 币别列表格式化
     */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (column.property == 'homeCurrency') {
        return row[column.property] === 1 ? '是' : '否'
      }
      if (column.property == 'rate') {
        return row[column.property] == 1 ? '否' : '是'
      }
      return row[column.property]
    },

    /**
     * 币别编辑
     */
    currencyEdit(data) {
      this.action = { type: 'update', infoData: data }
      this.currencyDialogVisible = true
    },

    /**
     * 币别删除
     */
    currencyDelete(scope) {
      if (scope.row.homeCurrency === 1) {
        this.$message.error('本位币不允许删除')
        return
      }
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          fmFinanceCurrencyDeleteAPI({
            currencyId: scope.row.currencyId
          })
            .then(res => {
              this.getCurrencyGroupList()
              this.$message.success('删除成功')
            })
            .catch(() => {})
        })
        .catch(() => {
        })
    },

    /**
     * 币别添加
     */
    addCurrency() {
      this.action = { type: 'add' }
      this.currencyDialogVisible = true
    },

    /**
     * 币别添加 -- 关闭
     */
    currencyClose() {
      this.currencyDialogVisible = false
    },

    /**
     * 币别添加成功
     */
    currencySubmit() {
      console.log(111)
      this.getCurrencyGroupList()
      this.currencyClose()
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "../styles/index.scss";
</style>
