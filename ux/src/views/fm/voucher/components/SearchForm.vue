<template>
  <div class="main">
    <el-form label-width="70px" label-position="top">
      <el-form-item label="会计期间">
        <el-date-picker
          v-model="form.time"
          :picker-options="pickerOptions"
          type="monthrange"
          :clearable="false"
          range-separator="至"
          start-placeholder="开始月份"
          value-format="yyyy-MM"
          end-placeholder="结束月份"
          @change="changeMonth" />
      </el-form-item>

      <el-form-item label="凭证字">
        <el-select v-model="form.voucherId">
          <el-option
            v-for="item in voucherOptions"
            :key="item.voucherId"
            :label="item.voucherName"
            :value="item.voucherId" />
        </el-select>
      </el-form-item>

      <el-form-item label="凭证号">
        <el-input-number
          v-model="form.certificateNum"
          :controls="false"
          class="left-inner" />
      </el-form-item>

      <el-form-item label="摘要">
        <el-input v-model="form.digestContent" />
      </el-form-item>

      <el-form-item label="科目">
        <subject-select-options v-model="form.subjectId" />
      </el-form-item>

      <el-form-item label="金额">
        <flexbox>
          <el-input-number
            v-model="form.minAmount"
            :controls="false" />
          <span class="range-text">至</span>
          <el-input-number
            v-model="form.maxAmount"
            :controls="false" />
        </flexbox>
      </el-form-item>

      <el-form-item label="制单人">
        <wk-user-dialog-select
          v-model="form.userId"
          :radio="true"
          class="relative" />
      </el-form-item>
    </el-form>

    <flexbox class="footer" justify="flex-end">
      <el-button @click="emitClose">关闭</el-button>
      <el-button @click="resetForm">重置</el-button>
      <el-button type="primary" @click="emitChange">查询</el-button>
    </flexbox>
  </div>
</template>

<script>
import { fmFinanceVoucherListAPI } from '@/api/fm/setting'

import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'
import WkUserDialogSelect from '@/components/NewCom/WkUserDialogSelect'

import { isEmpty } from '@/utils/types'
import { mapGetters } from 'vuex'
import parentNodeMixins from '../../mixins/parentNodeMixins'
import timeRangeMixins from '../../mixins/timeRangeMixins'
import { objDeepCopy } from '../../../../utils'

export default {
  name: 'SearchForm',
  components: {
    SubjectSelectOptions,
    WkUserDialogSelect
  },
  mixins: [parentNodeMixins, timeRangeMixins],
  props: {
    defaultValue: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      loading: false,
      voucherOptions: [],
      form: {}
    }
  },
  computed: {
    ...mapGetters(['financeCurrentAccount', 'financeFilterTimeRange']),
    pickerOptions() {
      return {
        disabledDate: date => {
          const current = this.$moment(date)
          return current.isBefore(this.financeFilterTimeRange.minTime.timeObj) ||
            current.isAfter(this.financeFilterTimeRange.maxTime.timeObj)
        }
      }
    }
  },
  watch: {
    defaultValue: {
      handler(val) {
        const form = objDeepCopy(this.form)
        if (!isEmpty(val) && (JSON.stringify(form) !== JSON.stringify(val))) {
          this.form = { ...this.form, ...val }
          this.emitChange()
        }
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    this.getVoucherCodeGroupList()
    if (this.financeCurrentAccount && this.financeCurrentAccount.startTime) {
      this.resetForm()
    }
  },
  methods: {
    /**
     * @description: 重置form
     */
    resetForm() {
      this.form = {
        startTime: '',
        endTime: '',
        subjectId: '',
        digestContent: '',
        voucherId: '',
        userId: '',
        certificateNum: undefined,
        minAmount: undefined,
        maxAmount: undefined,
        time: [
          this.financeCurrentAccount.startTime,
          this.financeCurrentAccount.startTime
        ]
      }
      this.emitChange()
    },

    /**
     * @description: 凭证字列表
     * @param {*}
     * @return {*}
     */
    getVoucherCodeGroupList() {
      fmFinanceVoucherListAPI().then(res => {
        this.voucherOptions = res.data
      }).catch(() => {})
    },

    /**
     * @description: 改变日期
     * @param {*} value
     */
    changeMonth(value) {
      this.form.startTime = this.$moment(value[0]).format('YYYYMM')
      this.form.endTime = this.$moment(value[1]).format('YYYYMM')
    },

    /**
     * @description: 关闭事件
     */
    emitClose() {
      const parent = this.getParentNode('SearchPopoverWrapper')
      if (parent && parent.updateData) {
        parent.updateData({
          searchTime: this.getTimeShowVal(this.form.time),
          timeInputWidth: this.getTimeInputWidth(this.form.time),
          popoverVisible: false
        })
      }
    },

    /**
     * @description: 查询
     */
    emitChange() {
      this.form.startTime = this.$moment(this.form.time[0]).format('YYYYMM')
      this.form.endTime = this.$moment(this.form.time[1]).format('YYYYMM')
      this.$emit('change', this.form)
      this.emitClose()
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-date-editor .el-range-separator {
  padding: 0;
}

::v-deep.el-form-item {
  margin-bottom: 8px;

  .el-form-item__label {
    padding: 0;
    line-height: 20px;
    color: #172b4d;
  }
}

.main {
  padding: 20px;

  .left-inner {
    ::v-deep .el-input__inner {
      text-align: left;
    }
  }

  .el-select,
  .el-input-number,
  .el-date-editor,
  .wk-user-select {
    width: 100%;
  }

  .range-text {
    padding: 0 10px;
  }
}
</style>
