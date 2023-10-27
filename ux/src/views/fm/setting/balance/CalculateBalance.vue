<template>
  <el-dialog
    ref="wkDialog"
    :visible.sync="dialogVisible"
    :before-close="close"
    :close-on-click-modal="false"
    width="600px">
    <span slot="title" class="el-dialog__title">
      试算平衡
      <i
        class="wk wk-icon-fill-help wk-help-tips"
        data-type="42"
        data-id="335" />
    </span>
    <el-table :data="info">
      <el-table-column v-for="(item,index) in fieldList" :key="index" :formatter="fieldFormatter" :label="item.name" :prop="item.field" :width="item.width" />
    </el-table>
  </el-dialog>
</template>

<script>
import { fmFinanceInitialTrialBalanceAPI } from '@/api/fm/setting'

export default {
  props: {
    dialogVisible: Boolean
  },
  data() {
    return {
      loading: false,
      info: [],
      fieldList: [
        { name: '项目', field: 'subject', width: '200px' },
        { name: '借方金额', field: 'debtorBalance', width: '130px' },
        { name: '贷方金额', field: 'creditBalance', width: '130px' },
        { name: '差额', field: 'diff', width: '100' }
      ]
    }
  },
  watch: {
    dialogVisible(val) {
      if (val) {
        this.getDetail()
      } else {
        this.info = []
      }
    }
  },
  mounted() {

  },
  methods: {
    close() {
      this.$emit('close')
    },
    fieldFormatter(row, column) {
      console.log(row, column)
      if (['debtorBalance', 'creditBalance', 'diff'].includes(column.property)) {
        if (row[column.property]) {
          return row[column.property].toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,')
        } else {
          return '0.00'
        }
      }
      // 如果需要格式化
      return row[column.property]
    },
    /**
     * @description: 获取详情数据
     */
    getDetail() {
      this.loading = true
      fmFinanceInitialTrialBalanceAPI().then(res => {
        if (Number(res.data.trialResult)) {
          this.$message.success('恭喜您，您录入的初始余额平衡！')
        } else {
          this.$message.warning('您录入的初始余额不平衡，请仔细核对')
        }
        this.info = [{
          subject: '期初余额（综合本位币）',
          creditBalance: res.data.creditBalance,
          debtorBalance: res.data.debtorBalance,
          diff: res.data.balanceDifferenceBalance
        }, {
          subject: '累计发生额（综合本位币）',
          creditBalance: res.data.addUpCreditBalance,
          debtorBalance: res.data.addUpDebtorBalance,
          diff: res.data.addUpDifferenceBalance
        }]
        this.loading = false
      }).catch((res) => {
        this.loading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form {
  ::v-deep.el-form-item {
    margin-bottom: 10px;
  }
}

.icon-box {
  display: inline-block;
  width: 50px;
  margin-left: 10px;
  text-align: left;
}

</style>
