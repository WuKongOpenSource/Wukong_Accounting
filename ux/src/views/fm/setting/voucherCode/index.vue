<template>
  <div v-loading="loading" class="app-container">
    <div class="content-header">
      <span>凭证字</span>
      <el-button
        v-if="$auth('finance.voucherWord.save')"
        type="primary"
        class="rt"
        @click="addVoucherCode">新建凭证字</el-button>
    </div>
    <div class="voucherCode-table">
      <el-table
        :data="voucherCodeData"
        :height="tableHeight"
        style="width: 100%;"
        stripe>
        <el-table-column
          v-for="(item, index) in voucherCodeList"
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
              v-if="$auth('finance.voucherWord.update')"
              type="primary-text"
              @click="voucherCodeEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="$auth('finance.voucherWord.delete')"
              type="primary-text"
              @click="voucherCodeDelect(scope)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <voucher-code-create-dialog
      v-if="voucherCodeDialogVisible"
      :action="action"
      :voucher-code-dialog-visible="voucherCodeDialogVisible"
      @voucher-code-close="voucherCodeClose"
      @voucher-code-submit="voucherCodeSubmit" />
  </div>
</template>

<script>
import { fmFinanceVoucherListAPI, fmFinanceVoucherDeleteAPI } from '@/api/fm/setting'

import voucherCodeCreateDialog from './components/Create'

export default {
  name: 'VoucherCodeSet',
  components: {
    voucherCodeCreateDialog
  },

  data() {
    return {
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 220,
      // 凭证字设置
      /** 凭证字每行的信息 */
      action: { type: 'add' },
      voucherCodeData: [],
      voucherCodeList: [
        { label: '凭证字', field: 'voucherName' },
        { label: '打印标题', field: 'printTitles' },
        { label: '是否默认', field: 'isDefault' }
      ],
      // 添加凭证字
      voucherCodeDialogVisible: false
    }
  },
  watch: {
  },
  created() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 220
    }
    this.getVoucherCodeGroupList()
  },
  methods: {
    /**
     * 凭证字列表
     */
    getVoucherCodeGroupList() {
      this.loading = true
      fmFinanceVoucherListAPI()
        .then(res => {
          this.loading = false
          this.voucherCodeData = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * 凭证字列表格式化
     */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (column.property == 'isDefault') {
        return row.isDefault ? '是' : '否'
      } else {
        return row[column.property]
      }
    },

    /**
     * 凭证字编辑
     */
    voucherCodeEdit(data) {
      this.action = { type: 'update', infoData: data }
      this.voucherCodeDialogVisible = true
    },

    /**
     * 凭证字删除
     */
    voucherCodeDelect(scope) {
      if (scope.row.isDefault) {
        this.$message.error('默认凭证字不允许删除')
        return
      }
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          fmFinanceVoucherDeleteAPI({
            voucherId: scope.row.voucherId
          })
            .then(res => {
              this.getVoucherCodeGroupList()
              this.$message.success('删除成功')
            })
            .catch(() => {})
        })
        .catch(() => {
        })
    },

    /**
     * 凭证字添加
     */
    addVoucherCode() {
      this.action = { type: 'add' }
      this.voucherCodeDialogVisible = true
    },

    /**
     * 凭证字添加 -- 关闭
     */
    voucherCodeClose() {
      this.voucherCodeDialogVisible = false
    },

    /**
     * 凭证字添加成功
     */
    voucherCodeSubmit() {
      this.getVoucherCodeGroupList()
      this.voucherCodeClose()
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "../styles/index.scss";

.content-header {
  padding-bottom: 0;
  margin-bottom: 24px;
}
</style>
