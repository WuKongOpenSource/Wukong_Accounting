<template>
  <el-dialog
    ref="wkDialog"
    :visible.sync="insertDialogVisible"
    :before-close="close"
    :close-on-click-modal="false"
    label-position="left"
    width="400px">
    <span slot="title" class="el-dialog__title">
      插入凭证
      <i
        class="wk wk-icon-fill-help wk-help-tips"
        data-type="37"
        data-id="307" />
    </span>
    <el-form ref="form" :rules="rules" :model="form" class="form" label-width="80px" label-position="left">
      <el-form-item label="期间" prop="certificateTime">
        <el-date-picker
          v-model="form.certificateTime"
          type="month"

          value-format="yyyyMM"
          placeholder="选择月" />
      </el-form-item>
      <el-form-item label="凭证字" prop="voucherId">
        <el-select v-model="form.voucherId">
          <el-option v-for="(item,index) in voucherOptions" :key="index" :label="item.voucherName" :value="item.voucherId" />
        </el-select>
      </el-form-item>
      <flexbox>
        <el-form-item class="flex" prop="moveCertificateNum" label-width="0px">

          将上述期间的： <el-input v-model="form.moveCertificateNum" type="number" />

        </el-form-item>
        <el-form-item class="flex" prop="insertCertificateNum" label-width="0px">

          号移动到：<el-input v-model="form.insertCertificateNum" type="number" />号之前

        </el-form-item>
      </flexbox>
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
import { fmFinanceCertificateInsertAPI } from '@/api/fm/voucher'

import { mapGetters } from 'vuex'
import moment from 'moment'
import { objDeepCopy } from '@/utils'

export default {
  props: {
    insertDialogVisible: Boolean
  },
  data() {
    return {
      loading: false,
      rule: [],
      voucherOptions: [],
      form: {
        voucherId: '',
        certificateTime: '',
        moveCertificateNum: '',
        insertCertificateNum: ''
      },
      rules: {
        certificateTime: [
          { required: true, message: '请填期间', trigger: 'blur' }
        ],
        voucherId: [
          { required: true, message: '请选择凭证字', trigger: 'blur' }
        ],
        moveCertificateNum: [
          { required: true, message: '请填写需要移动的凭证号', trigger: 'blur' }
        ],
        insertCertificateNum: [
          { required: true, message: '请填写需要移动的凭证号', trigger: 'blur' }
        ]
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
    if (this.financeCurrentAccount && this.financeCurrentAccount.startTime) {
      this.form.certificateTime = moment(this.financeCurrentAccount.startTime).format('YYYYMM')
    }
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
    /**
     * @description: 提交表单
     */
    submit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.loading = true
          const form = objDeepCopy(this.form)
          fmFinanceCertificateInsertAPI(form).then(res => {
            this.loading = false
            this.$message.success('操作成功')
            this.$emit('handle-success')
            this.$emit('close')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    },
    /**
     * @description: 获取凭证字列表
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

.flex {
  ::v-deep .el-input {
    width: 50px;
    margin: 0 10px;
  }
}

.icon-box {
  display: inline-block;
  width: 50px;
  margin-left: 10px;
  text-align: left;
}

</style>
