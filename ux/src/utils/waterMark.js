import {
  enterpriseSecurityWaterMarkConfigQueryAPI
} from '@/api/admin/enterpriseSecuritySetting'

import watermark from 'watermark-dom'
import { mapGetters } from 'vuex'

export default {
  name: 'watermark',
  data() {
    return {}
  },
  computed: {
    ...mapGetters([
      'userInfo',
      'watermarkChange'
    ])
  },
  watch: {
    watermarkChange(val) {
      if (val && this.userInfo) {
        this.queryWaterMarkConfig()
      } else {
        this.removeWaterMark()
      }
    }
  },
  mounted() {},
  methods: {
    /**
     * @description: 查询水印配置
     * @return {*}
     */
    queryWaterMarkConfig() {
      enterpriseSecurityWaterMarkConfigQueryAPI().then(res => {
        const data = res.data || {}
        if (data.status === 1) {
          this.initWaterMark(data.value)
        } else {
          this.removeWaterMark()
        }
      }).catch(() => {

      })
    },

    /**
     * @description: 加载水印
     * @param {*} data
     * @return {*}
     */
    initWaterMark(data) {
      this.$nextTick(() => {
        const {
          content = '',
          fontSize,
          density,
          color,
          angle
        } = JSON.parse(data) || {}

        const { realname, mobile, deptName, companyName } = this.userInfo
        const currentDate = this.$moment().format('YYYY-MM-DD')
        const currentDateTime = this.$moment().format('YYYY-MM-DD HH:mm:ss')
        const waterMarkContent = content.replace(/{realname}/g, realname)
          .replace(/{mobile}/g, mobile && mobile.slice(-4))
          .replace(/{deptName}/g, deptName)
          .replace(/{companyName}/g, companyName)
          .replace(/{date}/g, currentDate)
          .replace(/{time}/g, currentDateTime)

        let xSpace = 0
        let ySpace = 0
        if (density === '0') { // 密集
          xSpace = 20
          ySpace = 10
        } else if (density === '1') { // 中等
          xSpace = 100
          ySpace = 50
        } else if (density === '2') { // 宽松
          xSpace = 200
          ySpace = 100
        }

        watermark.load({
          watermark_txt: waterMarkContent,
          // watermark_x: 0, // 水印起始位置x轴坐标
          // watermark_y: 0, // 水印起始位置Y轴坐标
          // watermark_rows: 0, // 水印行数
          // watermark_cols: 0, // 水印列数
          watermark_x_space: xSpace, // 水印x轴间隔
          watermark_y_space: ySpace, // 水印y轴间隔
          watermark_font: '微软雅黑', // 水印字体
          watermark_color: color || 'black', // 水印字体颜色
          watermark_fontsize: fontSize || '12px', // 水印字体大小
          watermark_alpha: 0.15, // 水印透明度，要求设置在大于等于0.003
          watermark_width: 150, // 水印宽度
          watermark_height: 100, // 水印长度
          watermark_angle: angle // 水印倾斜度数
        })
      })
    },

    /**
     * @description: 移除水印
     * @return {*}
     */
    removeWaterMark() {
      watermark.load({
        watermark_txt: ` `
      })
    }
  }
}
