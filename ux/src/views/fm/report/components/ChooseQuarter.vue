<template>
  <div>
    选择季度
    <mark
      v-show="showSeason"
      style="position: fixed;top: 0;right: 0;bottom: 0;left: 0;z-index: 999;background: rgba(0, 0, 0, 0);"
      @click.stop="showSeason=false"
    />
    <el-input v-model="showValue" placeholder="请选择季度" style="width: 192px; margin-right: 10px;" @focus="showSeason=true">
      <i slot="prefix" class="el-input__icon el-icon-date" />
    </el-input>
    <el-card
      v-show="showSeason"
      class="box-card"
      style="position: fixed;z-index: 9999;width: 322px;padding: 0 3px 20px;margin-top: 10px;"
    >
      <div slot="header" class="clearfix" style="padding: 0;text-align: center;">
        <button
          type="button"
          aria-label="前一年"
          class="el-picker-panel__icon-btn el-date-picker__prev-btn el-icon-d-arrow-left"
          @click="prev"
        />
        <span role="button" class="el-date-picker__header-label">{{ year }}年</span>
        <button
          type="button"
          aria-label="后一年"
          class="el-picker-panel__icon-btn el-date-picker__next-btn el-icon-d-arrow-right"
          @click="next"
        />
      </div>
      <div class="text item" style="text-align: center;">
        <el-button
          :disabled="canBtnOne"
          type="text"
          style="float: left;width: 40%;color: #606266;"
          @click="selectSeason(0)"
        >第一季度</el-button>
        <el-button
          :disabled="canBtnTwo"
          type="text"
          style="float: right;width: 40%;color: #606266;"
          @click="selectSeason(1)"
        >第二季度</el-button>
      </div>
      <div class="text item" style="text-align: center;">
        <el-button
          :disabled="canBtnThree"
          type="text"
          style="float: left;width: 40%;color: #606266;"
          @click="selectSeason(2)"
        >第三季度</el-button>
        <el-button
          :disabled="canBtnFour"
          type="text"
          style="float: right;width: 40%;color: #606266;"
          @click="selectSeason(3)"
        >第四季度</el-button>
      </div>
    </el-card>
  </div>
</template>
<script>
/**
 * @api: valueArr : 季度value defalut['01-03', '04-06', '07-09', '10-12'] 默认值待设置
 */
export default {
  name: 'ChooseQuarter',
  props: {
    getValue: {
      default: () => {},
      type: Function
    },
    defaultValue: {
      default: '',
      type: String
    }
  },
  data() {
    return {
      canBtnOne: false,
      canBtnTwo: false,
      canBtnThree: false,
      canBtnFour: false,
      valueArr: ['01-03', '04-06', '07-09', '10-12'],
      showSeason: false,
      season: '', // 季度
      nowYear: new Date().getFullYear(),
      year: new Date().getFullYear(),
      showValue: ''
    }
  },
  watch: {
    defaultValue: function(value, oldValue) {
      console.log('当前的月2222222', value)
    },
    showValue: {
      handler(n) {
        console.log('获取的新数据', n)
        const year = n.substring(0, 4)
        const season = n.substring(5, 6)
        const seasonValue = this.valueArr[Number(season) - 1]
        const mounthArr = seasonValue.split('-')
        const yearMounth = {
          fromPeriod: `${year}${mounthArr[0]}`,
          toPeriod: `${year}${mounthArr[1]}`
        }
        console.log('获取的新数据', yearMounth)
        this.getValue(`${year}${mounthArr[0]}`, `${year}${mounthArr[1]}`)
      }
    }
  },
  created() {
    console.log('当前的月11111', this.defaultValue)
    if (this.defaultValue) {
      const value = this.defaultValue
      this.year = value.substring(0, 4)
      const mounth = value.substring(4, 6)
      // 获取季度
      const getQuarter = this.quarter(mounth)
      this.showValue = `${this.year}年${getQuarter}季度`
    }
  },
  methods: {
    /**
     * @description: 返回季度
     * @param {*} val
     */
    quarter(val) {
      const mounth = val.toString()
      if (['01', '02', '03'].includes(mounth)) {
        this.canBtnOne = false
        this.canBtnTwo = true
        this.canBtnThree = true
        this.canBtnFour = true
        return 1
      }
      if (['04', '05', '06'].includes(mounth)) {
        this.canBtnOne = false
        this.canBtnTwo = false
        this.canBtnThree = true
        this.canBtnFour = true
        return 2
      }
      if (['07', '08', '09'].includes(mounth)) {
        this.canBtnOne = false
        this.canBtnTwo = false
        this.canBtnThree = false
        this.canBtnFour = true
        return 3
      }
      if (['10', '11', '12'].includes(mounth)) {
        this.canBtnOne = false
        this.canBtnTwo = false
        this.canBtnThree = false
        this.canBtnFour = false
        return 4
      }
    },
    /**
     * @description: 上一年
     */
    prev() {
      this.year = this.year * 1 - 1
    },
    /**
     * @description: 下一年
     */
    next() {
      if (this.year * 1 + 1 > this.nowYear) {
        this.$message.error('不可以选择大于当前的年份')
        return
      }
      this.year = this.year * 1 + 1
    },
    /**
     * @description: 选择季度
     * @param {number} i
     */
    selectSeason(i) {
      const that = this
      that.season = i + 1
      that.showSeason = false
      this.showValue = `${this.year}年${this.season}季度`
    }
  }
}
</script>

