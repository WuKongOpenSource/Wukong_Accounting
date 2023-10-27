<template>
  <el-dialog
    v-loading="loading"
    :visible.sync="dialogVisible"
    :append-to-body="true"
    :close-on-click-modal="false"
    title="提示"
    width="500px"
    @close="handleCancel">

    <el-form>
      <el-form-item :label="labelText">
        <el-select v-model="selectedTime">
          <el-option
            v-for="item in optionsList"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <div v-if="settleType === 2" class="warning-text">
      反结账是违反会计法规的非正常操作，会影响历史报表数据，请慎用！
    </div>

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
import {
  settleAccountsAPI
} from '@/api/fm/settleAccounts'
import { mapActions, mapGetters } from 'vuex'

export default {
  name: 'SettleDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    settleTime: {
      type: String,
      required: true
    },
    settleType: {
      type: Number, // 1 结账 2 反结账
      required: true
    },
    settleIds: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      loading: false,
      dialogVisible: false,

      selectedTime: null,
      optionsList: [],

      maxTime: null,
      minTime: null
    }
  },
  computed: {
    ...mapGetters(['financeTimeRange']),
    labelText() {
      return `请确认要${this.settleType === 1 ? '' : '反'}结账到`
    }
  },
  watch: {
    visible: {
      handler() {
        this.dialogVisible = this.visible
        this.loading = false
        if (this.visible) {
          if (this.settleType === 1) {
            this.selectedTime = this.$moment(this.settleTime)
              .add(1, 'M')
              .startOf('month')
              .format('YYYY-MM-DD')
          } else {
            this.selectedTime = this.$moment(this.settleTime)
              .subtract(1, 'M')
              .endOf('month')
              .format('YYYY-MM-DD')
          }

          this.getTimeRange()
        }
      },
      immediate: true
    }
  },
  methods: {
    ...mapActions(['updateCurrentAccount']),
    /**
     * 获取时间区间
     */
    getTimeRange() {
      this.maxTime = this.financeTimeRange.maxTime
      this.minTime = this.financeTimeRange.minTime
      if (this.settleType === 1) {
        const now = this.$moment(this.settleTime).startOf('month')
        const max = this.$moment(this.maxTime).startOf('month')
        this.generateOptions(now.add(1, 'M'), max.add(1, 'M'))
      } else {
        const now = this.$moment(this.settleTime).endOf('month')
        const min = this.$moment(this.minTime).endOf('month')
        this.generateOptions(min, now.subtract(1, 'M'))
      }
    },

    /**
     * 生成时间期数数据
     * @param start
     * @param end
     */
    generateOptions(start, end) {
      const current = start
      const arr = []

      const func = this.settleType === 1 ? 'startOf' : 'endOf'
      while (
        current[func]('month').isBefore(end[func]('month')) ||
        current[func]('month').isSame(end[func]('month'))
      ) {
        arr.push({
          label: current[func]('month').format('YYYY年第MM期'),
          value: current[func]('month').format('YYYY-MM-DD')
        })
        current[func]('month').add(1, 'M')
      }
      this.optionsList = arr
    },

    handleConfirm() {
      const params = {
        statementIds: this.settleIds,
        type: this.settleType,
        certificateTime: this.selectedTime
      }
      if (this.settleType === 1) {
        params.certificateTime = this.$moment(params.certificateTime)
          .subtract(1, 'M')
          .endOf('month')
          .format('YYYY-MM-DD')
      }
      console.log('save: --', params)

      this.loading = true
      settleAccountsAPI(params).then(res => {
        this.$message.success('操作成功')
        this.$emit('confirm')
        this.updateCurrentAccount()
        this.handleCancel()
      }).catch(() => {
        this.loading = false
      })
    },

    handleCancel() {
      this.$emit('update:visible', false)
      this.$emit('close')
    }
  }
}
</script>

<style scoped lang="scss">
.warning-text {
  color: #f56c6c;
}
</style>
