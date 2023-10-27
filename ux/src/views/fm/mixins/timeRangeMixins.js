export default {
  methods: {
    /**
     * 获取期数范围展示值
     * @desc 开始期数与结束期数相同时只显示开始期数，开始与结束不同时则都展示
     * @param {Array} rangeArr
     * @return {string}
     */
    getTimeShowVal(rangeArr = []) {
      const start = rangeArr[0] || null
      const end = rangeArr[1] || null

      if (!start) return ''
      const startStr = this.$moment(start).format('YYYY年第MM期')

      if (!end) return startStr
      const endStr = this.$moment(end).format('YYYY年第MM期')

      if (startStr === endStr) return startStr
      return `${startStr} 至 ${endStr}`
    },

    /**
     * 获取期数范围输入框宽度
     * @param {Array} rangeArr
     * @return {string}
     */
    getTimeInputWidth(rangeArr = []) {
      const start = this.$moment(rangeArr[0] || null)
      const end = this.$moment(rangeArr[1] || null)
      if (
        !start.isValid() ||
        !end.isValid()
      ) return '180px'
      return start.format('YYYY-MM') !== end.format('YYYY-MM') ? '230px' : '180px'
    }
  }
}
