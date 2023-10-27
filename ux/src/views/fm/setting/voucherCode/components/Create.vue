<template>
  <el-dialog
    ref="wkDialog"
    :visible.sync="voucherCodeDialogVisible"
    :before-close="voucherCodeClose"
    :close-on-click-modal="false"
    width="400px">
    <span slot="title" class="el-dialog__title">
      {{ voucherCodeTitle }}
      <i
        v-if="action.type === 'add'"
        class="wk wk-icon-fill-help wk-help-tips"
        data-type="42"
        data-id="332" />
      <i
        v-else-if="action.type === 'update'"
        class="wk wk-icon-fill-help wk-help-tips"
        data-type="42"
        data-id="333" />
    </span>
    <el-form ref="form" :rules="rules" :model="form" class="form" label-width="80px">
      <el-form-item label="凭证字" prop="voucherName">
        <el-input v-model="form.voucherName" />
      </el-form-item>
      <el-form-item label="打印标题" prop="printTitles">
        <el-input v-model="form.printTitles" />
      </el-form-item>
      <el-form-item label="是否默认" prop="isDefault">
        <el-radio-group v-model="form.isDefault">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="0">否</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <span
      slot="footer"
      class="dialog-footer">
      <el-button
        v-debounce="voucherCodeSubmit"
        type="primary">确定</el-button>
      <el-button @click="voucherCodeClose">取消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { fmFinanceVoucherAddAPI } from '@/api/fm/setting'

import { isEmpty } from '@/utils/types'
import { objDeepCopy } from '@/utils'

export default {
  props: {
    voucherCodeDialogVisible: Boolean,
    /** 编辑时候传递进来的信息 */
    action: {
      type: Object,
      default: () => {
        return {
          type: 'add'
        }
      }
    }
  },
  data() {
    return {
      loading: false,
      isDefault: false,
      form: {
      },
      rules: {
        voucherName: [
          { required: true, message: '请输入凭证字', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    voucherCodeTitle() {
      if (this.action.type == 'update') {
        return '编辑凭证字'
      } else {
        return '新建凭证字'
      }
    }
  },
  watch: {
    action: {
      deep: true,
      immediate: true,
      handler(val) {
        if (!val.infoData || isEmpty(val.infoData)) {
          this.form = {
            'isDefault': 0,
            'printTitles': '记账凭证',
            'voucherName': ''
          }
        } else {
          this.isDefault = val.infoData.isDefault
          this.form = objDeepCopy(val.infoData)
        }
      }
    }
  },
  mounted() {
  },
  methods: {
    /**
     * @description: 关闭操作
     */
    voucherCodeClose() {
      this.$emit('voucher-code-close')
    },
    /**
     * @description: 添加凭证
     */
    voucherCodeSubmit() {
      if (this.isDefault && !this.form.isDefault) {
        this.$message.error('请确保至少有一个默认的凭证字')
        return
      }
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true
        fmFinanceVoucherAddAPI(this.form).then(res => {
          this.loading = false
          this.$message.success('添加成功')
          this.$emit('voucher-code-submit')
        }).catch((res) => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>

.icon-box {
  display: inline-block;
  width: 50px;
  margin-left: 10px;
  text-align: left;
}

</style>
