<template>
  <el-dialog
    ref="wkDialog"
    :visible.sync="tidyDialogVisible"
    :before-close="close"
    :close-on-click-modal="false"
    label-position="left"
    width="500px">
    <span slot="title" class="el-dialog__title">
      整理凭证
      <i
        class="wk wk-icon-fill-help wk-help-tips"
        data-type="37"
        data-id="306" />
    </span>
    <el-form ref="form" :model="form" class="form" label-width="80px">
      <el-form-item label="整理范围">
        <el-date-picker
          v-model="form.settleTime"
          type="month"
          value-format="yyyyMM"
          placeholder="选择月" />
      </el-form-item>
      <el-form-item label="凭证字">
        <el-select v-model="form.voucherId">
          <el-option v-for="(item,index) in voucherOptions" :key="index" :label="item.voucherName" :value="item.voucherId" />
        </el-select>
      </el-form-item>
      <el-form-item label="起始编号">
        <el-input v-model="form.certificateNum" type="number" />
      </el-form-item>
      <el-form-item label-width="20px">
        <el-radio-group v-model="form.type">
          <el-radio :label="1">按凭证号顺次前移补齐断号</el-radio>
          <el-radio :label="2">按凭证日期重新顺次编号</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <span
      slot="footer"
      class="dialog-footer">
      <el-button
        v-debounce="submit"
        type="primary">确定</el-button>
      <el-button @click="close">取消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { fmFinanceVoucherListAPI } from '@/api/fm/setting'
import { fmFinanceCertificateSettleAPI } from '@/api/fm/voucher'

import { mapGetters } from 'vuex'
import { objDeepCopy } from '@/utils'

export default {
  props: {
    tidyDialogVisible: Boolean
  },
  data() {
    return {
      loading: false,
      rule: [],
      voucherOptions: [],
      form: {
        voucherId: '',
        settleTime: '',
        certificateNum: 1,
        type: 1
      }
    }
  },
  computed: {
    ...mapGetters(['financeCurrentAccount'])
  },
  watch: {
  },
  created() {
    this.getVoucherCodeGroupList()
    this.form.settleTime = this.$moment(this.financeCurrentAccount.startTime).format('YYYYMM')
  },
  mounted() {
  },
  methods: {
    /**
     * @description: 关闭事件冒泡
     */
    close() {
      this.$emit('close')
    },
    submit() {
      this.loading = true
      const form = objDeepCopy(this.form)
      fmFinanceCertificateSettleAPI(form).then(res => {
        this.loading = false
        this.$message.success('操作成功')
        this.$emit('handle-success')
        this.$emit('close')
      }).catch(() => {
        this.loading = false
      })
    },
    /**
     * @description: 凭证字列表
     */
    getVoucherCodeGroupList() {
      fmFinanceVoucherListAPI()
        .then(res => {
          this.voucherOptions = res.data
          for (let i = 0; i < this.voucherOptions.length; i++) {
            const element = this.voucherOptions[i]
            if (element.isDefault) {
              this.form.voucherId = element.voucherId
            }
          }
        })
        .catch(() => {
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

.rule {
  position: relative;
  top: -10px;
  left: 15px;
  font-size: 12px;
  color: #999;
}

.icon-box {
  display: inline-block;
  width: 50px;
  margin-left: 10px;
  text-align: left;
}

</style>
