<template>
  <div class="statistics-search">
    <el-form label-width="70px" label-position="top">
      <el-form-item label="会计期间">
        <el-date-picker
          v-model="form.time"
          :picker-options="pickerOptions"
          type="monthrange"
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
        <flexbox>
          <el-input-number
            v-model="form.minCertificateNum"
            :controls="false" />
          <span class="range-text">至</span>
          <el-input-number
            v-model="form.maxCertificateNum"
            :controls="false" />
        </flexbox>
      </el-form-item>
      <el-form-item label="科目级次">
        <flexbox>
          <el-input-number
            v-model="form.minLevel"
            :controls="false" />
          <span class="range-text">至</span>
          <el-input-number
            v-model="form.maxLevel"
            :controls="false" />
        </flexbox>
      </el-form-item>
    </el-form>

    <flexbox class="footer" justify="flex-end">
      <el-button @click="resetForm">重置</el-button>
      <el-button type="primary" @click="emitChange">查询</el-button>
    </flexbox>
  </div>
</template>

<script>
import { fmFinanceVoucherListAPI } from '@/api/fm/setting'

import { mapGetters } from 'vuex'
import parentNodeMixins from '../../mixins/parentNodeMixins'
import timeRangeMixins from '../../mixins/timeRangeMixins'

export default {
  name: 'StatisticsSearch',
  mixins: [parentNodeMixins, timeRangeMixins],
  data() {
    return {
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
        maxCertificateNum: undefined,
        minCertificateNum: undefined,
        maxLevel: 1,
        minLevel: 1,
        voucherId: '',
        time: [
          this.financeCurrentAccount.startTime,
          this.financeCurrentAccount.startTime
        ]
      }
      this.emitChange()
    },

    /**
     * @description:凭证字列表
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
     * @description: 关闭
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
     * @description: 查询操作
     */
    emitChange() {
      const form = {
        ...this.form,
        startTime: this.$moment(this.form.time[0]).format('YYYYMM'),
        endTime: this.$moment(this.form.time[1]).format('YYYYMM')
      }
      Object.keys(form).forEach(key => {
        if (!form[key] && form[key] !== 0) {
          form[key] = ''
        }
      })
      this.$emit('change', form)
      this.emitClose()
    }
  }
}
</script>

<style scoped lang="scss">
::v-deep .el-date-editor .el-range-separator {
  padding: 0;
}

.statistics-search {
  padding: 20px;

  .el-select,
  .el-date-editor {
    width: 100%;
  }

  .range-text {
    padding: 0 10px;
  }
}
</style>
