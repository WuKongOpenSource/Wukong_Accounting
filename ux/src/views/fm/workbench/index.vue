<template>
  <flexbox
    :gutter="0"
    align="flex-start"
    justify="flex-start"
    direction="column"
    class="app-container">
    <div class="title">仪表盘</div>
    <flexbox class="top-title">
      <flexbox-item class="top-title-left">
        常用功能
      </flexbox-item>
      <el-button
        class="sort-btn"
        type="subtle"
        icon="wk wk-manage"
        @click="openSetting" />
    </flexbox>
    <flexbox
      :gutter="0"
      align="center"
      justify="flex-start"
      class="top-header">
      <flexbox
        v-if="finance.voucher && finance.voucher.save"
        align="center"
        justify="flex-start"
        class="top-item card"
        @click.native="handleToAddVoucher">
        <div class="top-item-content">
          <img class="top-item-img" src="@/assets/workbenchSvg/createVoucher.png" alt="">
          <div class="top-item-label">录凭证</div>
        </div>
      </flexbox>
      <flexbox
        v-for="(item,index) in settingArr"
        :key="index"
        align="center"
        justify="flex-start"
        class="top-item card"
        @click.native="handleNavTo(item)">
        <div class="top-item-content">
          <img :src="item.icon" class="top-item-img" alt="">
          <div class="top-item-label">{{ item.name }}</div>
        </div>
      </flexbox>
    </flexbox>
    <flexbox justify="space-between" class="target">
      <div class="top-title">
        财务指标
      </div>
      <!-- <el-button type="text" icon="wk wk-config">自定义</el-button> -->
    </flexbox>
    <!-- 图表 -->
    <flexbox-item v-loading="loading" class="card chart-card">

      <flexbox class="centent">
        <el-button icon="el-icon-arrow-left" type="selected" :disabled="!isShowLeft" @click="btnArr('right')" />
        <!-- <div :class="isShowLeft?'':'noShowArr'" class="left-arr arr" @click="btnArr('right')">
          <i class="el-icon-arrow-left" />
        </div> -->

        <div ref="box" v-loading="loading" class="center-content">
          <div
            v-for="(item,index) in financeList "
            :key="index"
            :class="currentIndex==index?'center-item--active':''"
            class="center-item card"
            @click="getDataById(item,index)">
            <div class="center-item-content">
              <p>{{ item.name }}</p>
              <p>{{ item.value }}</p>
            </div>
          </div>
        </div>
        <el-button icon="el-icon-arrow-right" type="selected" :disabled="!isShowRight" @click="btnArr('left')" />
        <!-- <div :class="isShowRight?'':'noShowArr'" class="right-arr arr" @click="btnArr('left')">
          <i class="el-icon-arrow-right" />
        </div> -->

      </flexbox>
      <flexbox class="card-chart">
        <flexbox
          :gutter="0"
          align="flex-start"
          justify="flex-start"
          direction="column"
          class="left">
          <flexbox class="card-title">
            <flexbox-item class="card-title-text">
              <span>
                {{ miniTitle }}变化趋势
              </span>
              <span class="card-title-des">
                （单位：元）
              </span>
              <i
                class="wk wk-icon-fill-help wk-help-tips"
                data-type="38"
                data-id="301" />
            </flexbox-item>
          </flexbox>
          <flexbox-item class="card-body">
            <div id="line-chart" ref="myEchart" class="chart" />
          </flexbox-item>
        </flexbox>

        <flexbox
          :gutter="0"
          align="flex-start"
          justify="flex-start"
          direction="column"
          class="right">
          <flexbox class="card-title">
            <flexbox-item class="card-title-text">
              {{ monthNum }}月{{ miniTitle }}结构分析
              <i
                class="wk wk-icon-fill-help wk-help-tips"
                :data-type="38"
                :data-id="303" />
              <span class="card-title-des">
                （单位：元）
              </span>
            </flexbox-item>
          </flexbox>
          <flexbox-item class="card-body">
            <div id="annular-chart" class="chart" style="width: 100%;" />
          </flexbox-item>
        </flexbox>
      </flexbox>

    </flexbox-item>

    <nav-setting
      :visible="showSetting"
      :default-value="workbenchConfig"
      @close="showSetting = false"
      @confirm="chooseSure" />
    <financial-target-dialog v-if="addTargetShow" @close="addTargetShow=false" />
  </flexbox>

</template>

<script>
import {
  incomeStatementAPI,
  dashboardConfigAPI,
  dashboardUpdateConfigAPI,
  indicator,
  statistics
} from '@/api/fm/workbench'

import navSetting from './components/navSetting'
import FinancialTargetDialog from './components/financialTargetDialog'

import * as echarts from 'echarts'
import { separator } from '@/filters/vueNumeralFilter/filters'
import NavLib from './nav'
import { mapGetters } from 'vuex'
import xrTheme from '@/styles/xr-theme.scss'
import { accAdd } from '@/utils/acc.js'
export default {
  name: 'Workbench',
  components: {
    navSetting,
    FinancialTargetDialog
  },
  data() {
    return {
      loading: false,
      showSetting: false,
      isShowRight: false,
      isShowLeft: false,
      miniTitle: '利润',
      financeList: [],
      currentIndex: -1,
      timer: null,
      addTargetShow: false,
      lineChartInstance: null, // 折线图实例
      annularChartInstance: null, // 环状图实例 36B37E
      lineOptions: {
        color: ['#0065FF', '#36B37E', '#FF5630', '#FFAB00'],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          }
        },
        legend: {
          top: 15,
          right: 0,
          data: ['收入', '成本', '利润', '费用']
        },
        grid: {
          left: '0%',
          right: '24px',
          // top: '100px',
          bottom: '8px',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            boundaryGap: false,
            data: [],
            axisTick: {
              alignWithLabel: true,
              lineStyle: { width: 0 }
            },
            axisLabel: {
              color: xrTheme.colorBlack,
              fontWeight: xrTheme.axisLabelFontWeight
            },
            axisLine: {
              lineStyle: { color: xrTheme.axisLineColor }
            },
            splitLine: {
              show: false
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            // 标轴刻度相关设置
            axisTick: {
              show: false
            },
            // 坐标轴轴线相关设置
            axisLabel: {
              color: xrTheme.colorBlack,
              fontWeight: xrTheme.axisLabelFontWeight
            },
            // 坐标轴轴线相关设置
            axisLine: {
              show: false
            },
            // 坐标轴在 grid 区域中的分隔线
            splitLine: {
              lineStyle: {
                color: xrTheme.axisLineColor
              }
            }
          }
        ],
        series: [
          {
            name: '收入',
            id: 'income',
            type: 'line',
            smooth: true,
            symbol: 'rect',
            areaStyle: {
              opacity: 0.2,
              origin: 'start'
            },
            data: []
          },
          {
            name: '成本',
            id: 'firstCost',
            type: 'line',
            smooth: true,
            symbol: 'rect',
            areaStyle: {
              opacity: 0.2,
              origin: 'start'
            },
            data: []
          },
          {
            name: '利润',
            id: 'profit',
            type: 'line',
            smooth: true,
            symbol: 'rect',
            areaStyle: {
              opacity: 0.2,
              origin: 'start'
            },
            data: []
          },
          {
            name: '费用',
            id: 'cost',
            type: 'line',
            smooth: true,
            symbol: 'rect',
            areaStyle: {
              opacity: 0.2,
              origin: 'start'
            },
            data: []
          }
        ]
      },
      // 饼图
      annularOptions: {
        color: ['#0065FF', '#36B37E', '#FF5630', '#FFAB00', '#FF0', '#AB00', '#F00'],
        tooltip: {
          trigger: 'item'
        },
        legend: {
          top: '40%',
          orient: 'vertical',
          left: '70%'
        },
        center: ['40%', '50%'],
        series: [
          {
            center: ['40%', '50%'],
            name: '',
            type: 'pie',
            radius: ['50%', '65%'],
            hoverOffset: 5,
            selectedOffset: 5,
            label: {
              show: false,
              position: 'center'
            },
            labelLine: {
              show: false
            },
            data: [
              { prop: 'income', value: 0, name: '收入' },
              { prop: 'firstCost', value: 0, name: '成本' },
              { prop: 'profit', value: 0, name: '利润' },
              { prop: 'cost', value: 0, name: '费用' }
            ]
          }
        ]
      },
      workbenchConfig: ''
    }
  },
  computed: {
    ...mapGetters([
      'financeCurrentAccount',
      'finance'
    ]),
    monthNum() {
      if (!this.financeCurrentAccount || !this.financeCurrentAccount.startTime) return ''
      return this.$moment(this.financeCurrentAccount.startTime).month() + 1
    },
    settingArr() {
      const arr = []
      const ids = this.workbenchConfig.split(',')
      if (ids.length === 0) return arr
      NavLib.forEach(item => {
        item.children.forEach(child => {
          if (ids.includes(child.id)) {
            if (child.id == '001') {
              if (this.finance.voucher && this.finance.voucher.read) {
                arr.push(child)
              }
            } else {
              arr.push(child)
            }
          }
        })
      })
      return arr
    }
  },
  mounted() {
    this.getData()
    this.getHeaderData()
    this.getCenterData()
    this.isShowArr()
  },
  methods: {
    /**
     * @description: 控制左右按钮是否禁用
     * @return {*}
     */
    isShowArr() {
      console.log(this.$refs.box.scrollLeft, this.$refs.box.scrollWidth - this.$refs.box.offsetWidth - 50)
      if (this.$refs.box.scrollLeft == 0) {
        this.isShowLeft = false
        this.isShowRight = true
        return
      }
      if (this.$refs.box.scrollLeft >= (this.$refs.box.scrollWidth - this.$refs.box.offsetWidth - 50)) {
        this.isShowRight = false
        this.isShowLeft = true
        return
      }
      if (this.$refs.box.scrollLeft > 0 && this.$refs.box.scrollLeft < this.$refs.box.scrollWidth) {
        this.isShowRight = true
        this.isShowLeft = true
        return
      }
    },
    /**
     * @description: 处理点击箭头操作事件
     * @param {Object} data
     */
    btnArr(data) {
      if (this.timer) return
      const all = 210
      let recodeStep = 0
      const step = 10
      var that = this
      that.timer = setInterval(function() {
        recodeStep += step
        if (data == 'left') {
          that.$refs.box.scrollLeft += step
        }
        if (data == 'right') {
          that.$refs.box.scrollLeft -= step
        }
        if (recodeStep >= all || that.$refs.box.scrollLeft == 0) {
          clearInterval(that.timer)
          that.timer = null
          that.isShowArr()
        }
      }, 10)
    },
    /**
     * @description:中间的列表
     */
    getCenterData() {
      indicator().then(res => {
        res.data.forEach(item => {
          item.value = separator(item.value)
        })
        this.financeList = res.data
      })
    },
    /**
     * @description:根据id获取详情
     * @param {Object} passItem
     * @param {Number|string} index
     */
    getDataById(passItem, index) {
      this.loading = true
      this.currentIndex = index
      const params = {
        id: passItem.id
      }
      statistics(params).then(res => {
        this.miniTitle = passItem.name
        /** 重置数据
         * 折线图和饼图
         */
        this.lineOptions.legend.data = []
        this.lineOptions.series = []
        this.lineOptions.xAxis[0].data = []
        this.annularOptions.series[0].data = []

        const findItem = res.data.find(item => {
          return item.list.length > 0
        })
        /** x轴数据（年月） */
        const Xdata = []

        if (findItem) {
          /** 拼接名字 */
          this.lineOptions.legend.data = [passItem.name]
          this.lineOptions.series = [
            {
              name: findItem.name,
              type: 'line',
              smooth: true,
              areaStyle: {
                opacity: 0.2,
                origin: 'start'
              },
              data: []
            }
          ]
          res.data.forEach(secondItem => {
            let dataNum = 0
            const years = secondItem.period.substring(2, 4)
            const month = secondItem.period.substring(4, 6)
            this.lineOptions.xAxis[0].data.push(`${years}年${month}月`)

            if (secondItem.list.length !== 0) {
              secondItem.list.forEach(thirdItem => {
                dataNum = accAdd(thirdItem.value, dataNum)
              })
            }
            this.lineOptions.series[0].data.push(dataNum)
          })
          console.log(this.lineOptions)
          // findItem.list.forEach(item => {
          //   this.lineOptions.legend.data.push(item.subjectName)
          //   const ele = {
          //     name: item.subjectName,
          //     type: 'line',
          //     smooth: true,
          //     areaStyle: {
          //       opacity: 0.2,
          //       origin: 'start'
          //     },
          //     data: []
          //   }
          //   this.lineOptions.series.push(ele)
          // })
          /** 根据名字去拼接数据 */
          // this.lineOptions.legend.data.forEach((firstName, firstIndex) => {
          //   /** firstName:子项的名字 */
          //   const dataArr = []
          //   res.data.forEach(secondItem => {
          //     /** firstIndex==0 条件 只允许第一遍拼接x轴日期 */
          //     if (firstIndex == 0) {
          //       /** 拼接x轴的年月 */
          //       const years = secondItem.period.substring(2, 4)
          //       const month = secondItem.period.substring(4, 6)
          //       Xdata.push(`${years}年${month}月`)
          //     }

          //     if (secondItem.list.length == 0) {
          //       dataArr.push(0)
          //     } else {
          //       secondItem.list.forEach(thirdItem => {
          //         if (thirdItem.subjectName == firstName) {
          //           dataArr.push(thirdItem.value)
          //         }
          //       })
          //     }
          //   })
          //   /** 给对应的折线赋值 */
          //   this.lineOptions.series.forEach(item => {
          //     if (item.name == firstName) {
          //       item.data = objDeepCopy(dataArr)
          //     }
          //   })
          //   /** x轴数据赋值 */
          //   this.lineOptions.xAxis[0].data = Xdata
          // })
          /** 拼接饼图数据 */
          res.data.forEach(item => {
            const month = item.period.substring(4, 6)
            if (month == this.monthNum) {
              console.log(item)
              /** 判断当前账期是否有数据 */
              if (item.list.length == 0) {
                /** 如果当前账期没有数据 */
                this.lineOptions.legend.data.forEach(sonItem => {
                  const annularOptionsItem = {
                    value: '--',
                    name: ''
                  }
                  annularOptionsItem.name = sonItem
                  this.annularOptions.series[0].data.push(annularOptionsItem)
                })
              } else {
                let outherNum = 0
                item.list.forEach((sonItem) => {
                  if (this.annularOptions.series[0].data.length <= 4 && sonItem.value > 0) {
                    const annularOptionsItem = {
                      value: '--',
                      name: ''
                    }
                    annularOptionsItem.name = sonItem.subjectName
                    annularOptionsItem.value = sonItem.value <= 0 ? '--' : sonItem.value
                    this.annularOptions.series[0].data.push(annularOptionsItem)
                  } else {
                    outherNum += sonItem.value
                  }
                })
                if (item.list.length > 5) {
                  this.annularOptions.series[0].data.push({
                    name: '其他',
                    value: outherNum
                  })
                }
              }
            }
          })
        } else {
          this.lineOptions.legend.data = [passItem.name]
          const ele = {
            name: passItem.name,
            type: 'line',
            smooth: true,
            areaStyle: {
              opacity: 0.2,
              origin: 'start'
            },
            data: []
          }
          const fatherData = []
          res.data.forEach(element => {
            /** 拼接x轴的年月 */
            const years = element.period.substring(2, 4)
            const month = element.period.substring(4, 6)
            Xdata.push(`${years}年${month}月`)

            fatherData.push(element.value)
            /**
             * 拼接饼图数据
             * 取当前账期的月份*/
            if (month == this.monthNum) {
              const annularOptionsData = {
                value: element.value <= 0 ? '--' : element.value,
                name: passItem.name
              }
              this.annularOptions.series[0].data = [annularOptionsData]
            }
          })
          ele.data = fatherData
          this.lineOptions.series.push(ele)
          /** x轴数据赋值 */
          this.lineOptions.xAxis[0].data = Xdata
        }
        this.renderLineChart()
        this.renderAnnularChart()
        // this.lineChartInstance.setOption(this.lineOptions, true)
        // this.annularChartInstance.setOption(this.annularOptions, true)
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    /**
     * @description: 获取头部默认展示
     */
    getHeaderData() {
      dashboardConfigAPI().then(res => {
        this.workbenchConfig = res.data.config || ''
      })
    },
    /**
     * @description: 获取随机颜色
     */
    getRandomColor() {
      return '#' + Math.floor(Math.random() * 16777215).toString(16)
    },
    /**
     * @description: 打开财务指标设置
     */
    openFinanceSetting() {
      this.showFinanceSetting = true
    },

    /**
     * @description: 打开常用设置
     */
    openSetting() {
      this.showSetting = true
    },

    /**
     * @description: 选择常用
     * @param {Object} data
     */
    chooseSure(data) {
      this.workbenchConfig = data
      dashboardUpdateConfigAPI({
        config: data
      }).then(res => {
        console.log(res)
        this.settingArr = data
        this.showSetting = false
      })
    },

    /**
     * @description: 添加凭证
     * @param {*}
     * @return {*}
     */
    handleToAddVoucher() {
      this.$router.push({ path: '/fm/voucher/subs/create' })
    },

    /**
     * @description:点击其它跳转相应页面
     * @param {Object} item
     * @param {String} item.url
     */
    handleNavTo(item) {
      console.log('item', item)
      this.$router.push({ path: item.url })
    },
    /**
     * @description: 根据获取的数据渲染页面
     * @param {*} res
     */
    rendererpage(res) {
      // 利润表数据
      // 赋值x轴
      const Xdata = []
      const firstCost = []
      const profit = []
      const cost = []
      const other = []
      // 展示数据
      const income = []
      res.data.forEach(item => {
        // x轴数据
        const years = item.period.substring(2, 4)
        const month = item.period.substring(4, 6)
        Xdata.push(`${years}年${month}月`)
        // 赋值对应的数据
        income.push(item.income)
        firstCost.push(item.firstCost)
        profit.push(item.profit)
        cost.push(item.cost)
        other.push(item.other)
        // 获取仪表盘数据
        if (month == this.monthNum) {
          this.annularOptions.series[0].data.forEach(ele => {
            for (const key in item) {
              if (ele.prop == key) {
                if (item[key] <= 0)item[key] = '--'
                ele.value = item[key]
              }
            }
          })
        }
      })
      this.lineOptions.xAxis[0].data = Xdata
      this.lineOptions.series.forEach((item, index) => {
        // this.annularOptions.color[index] = this.getRandomColor()
        if (item.id == 'income') {
          item.data = income
        }
        if (item.id == 'firstCost') {
          item.data = firstCost
        }
        if (item.id == 'profit') {
          item.data = profit
        }
        if (item.id == 'cost') {
          item.data = cost
        }
      })
      // 赋值成功以后在渲染折线图
      this.renderLineChart()
      this.renderAnnularChart()
      console.log('res', this.lineOptions.xAxis)
    },
    /**
     * @description: 获取利润表统计
     */
    getData() {
      incomeStatementAPI().then(res => {
        this.rendererpage(res)
      })
    },

    /**
     * @description: 渲染折线图
     */
    renderLineChart() {
      if (this.lineChartInstance) {
        this.lineChartInstance.setOption(this.lineOptions, true)
      } else {
        this.lineChartInstance = echarts.init(document.getElementById('line-chart'))
        this.lineChartInstance.setOption(this.lineOptions, true)
      }
    },

    /**
     * @description:渲染环形图
     */
    renderAnnularChart() {
      if (this.annularChartInstance) {
        this.annularChartInstance.setOption(this.annularOptions, true)
      } else {
        this.annularChartInstance = echarts.init(document.getElementById('annular-chart'))
        this.annularChartInstance.setOption(this.annularOptions, true)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.app-container {
  position: relative;
  width: 100%;
  min-width: 1200px;
  height: 100%;
  padding: #{$--interval-base * 3} #{$--interval-base * 5} #{$--interval-base * 3};
  overflow: auto;

  .card {
    padding: 20px 12px 16px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: $--box-shadow-bottom-light;

    &:hover {
      box-shadow: $--box-shadow-hover-bottom-light;
    }

    .card-title {
      padding-bottom: $--interval-base;

      .card-title-text {
        font-size: 16px;
      }

      .card-title-des {
        color: $--color-text-secondary;
      }

      .card-title-right {
        font-size: 13px;

        .el-button {
          margin: 0;
        }
      }
    }

    .card-body {
      width: 100%;

      ::v-deep.xs-empty-parent--relative {
        margin-top: 5%;
      }
    }
  }

  > .title {
    font-size: $--font-size-xxlarge;
  }

  .top-title {
    font-size: 16px;
  }

  .top-header {
    width: 100%;
    margin-top: 16px;

    .top-item {
      width: 24%;
      height: 120px;
      padding: 0 24px;
      margin-right: 1.333%;
      background-image: linear-gradient(to bottom, $--color-n20, $--color-n20 40px, transparent 40px, transparent 100%);

      &:last-child {
        margin-right: 0;
      }

      &:hover {
        color: #2362fb;
        cursor: pointer;
      }

      .top-item-content {
        margin-top: -24px;

        .top-item-img {
          width: 38px;
          height: 38px;
          vertical-align: middle;
        }

        .top-item-label {
          margin-top: $--interval-base;
        }

        span {
          margin-left: 16px;
          font-size: 14px;
        }
      }
    }
  }

  .center-content {
    display: flex;
    padding: 16px 1px;
    margin: 0 16px;
    overflow: hidden;
    transition: all linear 0.5s;

    .center-item {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      width: 200px;
      height: 80px;
      padding: 0 30px;
      margin-right: 10px;
      cursor: pointer;

      &:last-child {
        margin-right: 0;
      }

      .center-item-content {
        p {
          font-size: 16px;
          line-height: 24px;
        }
      }
    }

    .center-item--active {
      color: white;
      background-color: $--color-primary;
    }
  }

  .chart-card {
    display: flex;
    flex-direction: column;
    width: 100%;
    padding: 24px;
    margin-top: 15px;

    .card-chart {
      height: 100%;
      margin-top: 24px;
    }

    .left {
      width: 62%;
      height: 100%;
    }

    .right {
      width: calc(38% - 30px);
      height: 100%;
      margin-left: 30px;
    }

    .card-body {
      flex: 1;
      overflow: hidden;

      .chart {
        width: 100%;
        height: 100%;
      }
    }
  }

  .centent {
    // .el-button{
    //   &:hover{
    //     background-color: #091E42;
    //     color: white;
    //   }
    // }
  }
}

.target {
  line-height: 50px;
}

.chart {
  width: 100%;
  min-height: 300px;
}

.noShowArr {
  color: #a5adba;
  cursor: not-allowed !important;
}
</style>
