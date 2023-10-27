<template>
  <div>
    <span v-if="isShowName" style="margin: 0 5px 0 10px;">会计期间</span>
    <el-date-picker
      v-model="time"
      :picker-options="pickerOptions"
      type="monthrange"
      range-separator="至"
      start-placeholder="开始月份"
      value-format="yyyy-MM"
      end-placeholder="结束月份"
      @change="changeMonth" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

import { objDeepCopy } from '@/utils'
export default {
  // 组件名
  name: 'ChooseAccountantTime',
  // 注册组件
  components: {},
  // 接收父组件数据
  props: {
    isShowName: Boolean,
    timeValue: [Object, String]
  },
  // 组件数据
  data() {
    return {
      time: [],
      propsTime: {
        startTime: '',
        endTime: ''
      },
      deepCopgTimeValue: {}
    }
  },
  // 计算属性
  computed: {
    ...mapGetters(['financeFilterTimeRange']),
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
  // 监听属性
  watch: {
    timeValue: {
      handler(val) {
        if (val != undefined) {
          this.deepCopgTimeValue = objDeepCopy(val)
          if (this.deepCopgTimeValue.startTime == '' && this.deepCopgTimeValue.endTime == '') {
            this.time = []
          } else {
            this.time = []
            this.time.push(this.deepCopgTimeValue.startTime)
            this.time.push(this.deepCopgTimeValue.endTime)
          }
        }
      },
      deep: true,
      immediate: true
    }
  },
  // 生命周期钩子函数
  created() {
  },
  mounted() {},
  // 组件方法
  methods: {
    /**
     * @description: 改变时间处理
     * @param {*} value
     */
    changeMonth(value) {
      console.log(value)
      if (value == null) return
      this.propsTime.startTime = this.$moment(value[0]).startOf('month').format('YYYY-MM-DD')
      this.propsTime.endTime = this.$moment(value[1]).endOf('month').format('YYYY-MM-DD')
      this.$emit('getTime', this.propsTime)
    }
  }
}
</script>

<style scoped lang='less'>
::v-deep.el-date-editor .el-range-input{
    margin-left: 8px !important;
}
::v-deep.el-date-editor--monthrange.el-input, .el-date-editor--monthrange.el-input__inner{
  width: 288px !important;
}
</style>
