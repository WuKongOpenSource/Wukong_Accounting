<template>
  <el-date-picker
    v-model="time"
    :picker-options="pickerOptions"
    :clearable="clearable"
    type="monthrange"
    range-separator="至"
    start-placeholder="开始月份"
    value-format="yyyy-MM-dd HH:mm:ss"
    end-placeholder="结束月份"
    @change="changeMonth" />
</template>
<script>
import { mapGetters } from 'vuex'

export default {
  name: 'ChooseAccountantTime',
  components: {},
  props: {
    value: {
      type: [Array],
      default: () => []
    },
    clearable: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      time: []
    }
  },
  computed: {
    ...mapGetters(['financeFilterTimeRange']),
    pickerOptions() {
      return {
        disabledDate: date => {
          const month = new Date(date).getMonth()
          const minTime = new Date(date).getMonth(this.financeFilterTimeRange.minTime.val)
          const maxTime = new Date(date).getMonth(this.financeFilterTimeRange.maxTime.val)
          return month < minTime || month > maxTime
        }
      }
    }
  },
  watch: {
    value: {
      handler(val) {
        console.log('val', val)
        this.time = [...val]
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {},
  methods: {
    changeMonth(value) {
      this.$emit('input', value)
      this.$emit('change', value)
    }
  }
}
</script>

<style scoped lang="scss">
.el-date-editor {
  ::v-deep .el-range-separator {
    width: unset;
  }
}
</style>
