<template>
  <el-dialog
    v-loading="loading"
    :visible.sync="dialogVisible"
    :close-on-click-modal="false"
    append-to-body
    width="580px"
    @close="handleCancel">
    <span slot="title" class="el-dialog__title">
      结转损益参数设置
      <i
        v-if="helpObj"
        class="wk wk-icon-fill-help wk-help-tips"
        :data-type="helpObj.dataType"
        :data-id="helpObj.dataId" />
    </span>
    <el-form
      ref="elForm"
      :model="form"
      :rules="rules"
      label-position="left">
      <el-form-item
        label="凭证日期:"
        label-width="80px"
        prop="createTime">
        <el-date-picker
          v-model="form.endTimeDays"
          value-format="yyyy-MM-dd"
          type="date"
          placeholder="选择日期" />
      </el-form-item>
      <el-form-item
        label="凭证字:"
        label-width="80px"
        prop="voucherId">
        <el-select v-model="form.voucherId" >
          <el-option
            v-for="item in voucherList"
            :key="item.voucherId"
            :label="item.voucherName"
            :value="item.voucherId" />
        </el-select>
      </el-form-item>
      <el-form-item
        label="凭证摘要:"
        label-width="80px"
        prop="digestContent">
        <el-input v-model="form.digestContent" />
      </el-form-item>
      <el-form-item
        label="凭证分类:"
        prop="voucherType"
        style="margin: -10px 0 5px;">
        <el-radio-group
          v-model="form.voucherType"
          style="margin-top: -20px;">
          <el-radio :label="1">收益和损失分开结转（分别生成收益凭证和损失凭证）</el-radio>
          <el-radio :label="2">收益和损失同时结转</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        label="“以前年度损益调整”科目:"
        label-width="240px"
        prop="adjustSubjectId">
        <subject-select-options
          v-if="subjectList.length > 0"
          v-model="form.adjustSubjectId"
          :can-create="false"
          :subject-list="subjectList" />
      </el-form-item>
      <el-form-item
        label="“以前年度损益调整”科目的结转科目:"
        label-width="240px"
        prop="endSubjectId">
        <subject-select-options
          v-if="subjectList.length > 0"
          v-model="form.endSubjectId"
          :can-create="false"
          :subject-list="subjectList" />
      </el-form-item>
      <el-form-item
        label="其他损益科目的结转科目:"
        label-width="240px"
        prop="restSubjectId">
        <subject-select-options
          v-if="subjectList.length > 0"
          v-model="form.restSubjectId"
          :can-create="false"
          :subject-list="subjectList" />
      </el-form-item>
      <el-form-item label="" label-width="0">
        <el-checkbox
          v-model="form.endWay"
          :true-label="1"
          :false-label="0">
          结转方式：按余额反向结转
          <el-tooltip placement="top">
            <div slot="content">
              选择“按余额反向结转”结转损益时，按科目余额的相反方向<br>
              结转：没选中时，按科目属性中定义的科目方向反向结账。<br>
              例：管理费用（科目属性定义为借方科目），期末余额为贷方10000<br>
              如选择“按余额反向结转”时，结转分录为借：管理费用10000<br>
              未选择时，结转分录为贷：管理费用-10000
            </div>
            <i class="wk wk-help tips-icon" />
          </el-tooltip>
        </el-checkbox>
      </el-form-item>
    </el-form>

    <div
      slot="footer"
      class="dialog-footer">
      <el-button
        v-debounce="handleConfirm"
        type="primary">确定</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { fmFinanceVoucherListAPI, fmFinanceSubjectListAPI } from '@/api/fm/setting'
import { updateStatementAPI } from '@/api/fm/settleAccounts'

import SubjectSelectOptions from '../../components/subject/SubjectSelectOptions'

export default {
  name: 'ProfitAndLossDialog',
  components: {
    SubjectSelectOptions
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    statementItem: {
      type: Object,
      required: true
    },
    statementTime: {
      type: String,
      required: true
    },
    helpObj: Object
  },
  data() {
    return {
      loading: false,
      dialogVisible: false,

      form: {},
      rules: {
        // createTime: [
        //   { required: true, message: '请选择凭证日期', trigger: ['change', 'blur'] }
        // ],
        voucherId: [
          { required: true, message: '请选择凭证字', trigger: 'change' }
        ],
        digestContent: [
          { required: true, message: '凭证摘要不能为空', trigger: ['change', 'blur'] }
        ],
        voucherType: [
          { required: true, message: '请选择凭证分类', trigger: 'change' }
        ],
        adjustSubjectId: [
          { required: true, message: '“以前年度损益调整”科目不能为空', trigger: 'change' }
        ],
        endSubjectId: [
          { required: true, message: '“以前年度损益调整”科目的结转科目不能为空', trigger: 'change' }
        ],
        restSubjectId: [
          { required: true, message: '其他损益科目的结转科目不能为空', trigger: 'change' }
        ]
      },

      voucherList: [],
      subjectList: []
    }
  },
  watch: {
    visible: {
      handler(val) {
        this.dialogVisible = val
        if (this.statementItem && val) {
          this.form = { ...this.statementItem }
          const lastTime = this.$moment(this.statementTime).endOf('month')
          if (
            this.statementItem.endTimeDays &&
            !isNaN(Number(this.statementItem.endTimeDays))
          ) {
            const day = this.statementItem.endTimeDays
            const lastDay = lastTime.date()
            if (day <= lastDay) {
              lastTime.date(day)
            }
          }
          delete this.form.companyId
          this.$set(this.form, 'endTimeDays', lastTime.format('YYYY-MM-DD'))
        }
      },
      immediate: true
    }
  },
  mounted() {
    this.getVoucherList()
    this.getSubjectList()
  },
  methods: {
    /**
     * 获取凭证字列表
     */
    getVoucherList() {
      fmFinanceVoucherListAPI().then(res => {
        this.voucherList = res.data || []
      }).catch(() => {})
    },

    /**
     * 获取科目列表
     */
    getSubjectList() {
      fmFinanceSubjectListAPI({
        type: null,
        isTree: 0
      }).then((res) => {
        this.subjectList = res.data || []
      }).catch(() => {})
    },

    handleCancel() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
    },

    handleConfirm() {
      this.$refs.elForm.validate(valid => {
        if (valid) {
          const params = { ...this.form }
          if (params.endTimeDays) {
            params.endTimeDays = this.$moment(params.endTimeDays).date()
          }
          console.log('save: ', params)
          this.loading = true
          updateStatementAPI(params).then(res => {
            this.loading = false
            this.$emit('confirm')
            this.handleCancel()
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
::v-deep .el-dialog__body {
  padding: 15px 15px 0;
}

.el-input,
.el-select {
  width: 230px;
}

.el-form {
  height: 55vh;
  overflow-y: auto;
}

.tips-icon {
  font-size: 13px;
  color: #cbcbcb;
  cursor: pointer;
}
</style>
