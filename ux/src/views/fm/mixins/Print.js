import printJs from 'print-js' // printJs插件
import { mapGetters } from 'vuex'
import timeRangeMixins from '@/views/fm/mixins/timeRangeMixins'

export default {
  data() {
    return {
      printFooterShow: false,
      pageTitle: '',
      searchTime: ''
    }
  },
  mixins: [timeRangeMixins],
  computed: {
    ...mapGetters(['financeCurrentAccount']),
    displayTime() {
      return (
        this.searchTime ||
        (this.dataTime && this.$moment(this.dataTime, 'YYYYMM').format('YYYY年第MM期')) ||
        (this.dateValue && this.$moment(this.dateValue, 'YYYYMM').format('YYYY年第MM期')) ||
        (this.timeArray && this.timeArray.length && this.getTimeShowVal(this.timeArray))
      )
    }
  },
  methods: {
    HandlerPrint(elementId,
      { title = '',
        style = '',
        centerText = '',
        headerHtml = '',
        type = 'html'
      }
    ) {
      this.printFooterShow = true
      const header = `
      <div >
        <div style='text-align:center;font-size:30px; font-weight:bold'>${this.pageTitle || title}</div>
        <div style='display:flex; font-size:26px;padding: 10px 0'>
          <span style="flex:1">编制单位：${this.financeCurrentAccount.companyName}</span>
          <span style="flex:1; text-align:center">${centerText}</span>
          <span style="flex:1; text-align:right">
          ${(this.displayTime ||
            this.$moment(this.financeCurrentAccount.startTime, 'YYYYMM')
              .format('YYYY年第MM期'))}
          </span>
        </div>
      </div>
      `
      this.$nextTick(() => {
        printJs({
          printable: elementId, // 指定元素块id
          header: headerHtml || header, // 用于HTML，Image或JSON打印的可选标头。它将放在页面顶部。此属性将接受文本或原始HTML
          type: type, // 可打印类型。可用的打印选项包括：pdf，html，image，json和raw-html
          scanStyles: false, // 设置为false时，库不会处理应用于正在打印的html的样式
          style: `@page {size: auto;size:A3;margin:6mm}
                  td,th{border:1px solid !important;padding: 10px 0;}
                  table{border-collapse: collapse;font-size:20px;}
                  .el-table__cell.gutter{border:none !important;}
                  .el-table__empty-text{text-align: center;width: 100%;}
                  tr{page-break-inside:avoid}
                  .el-table__empty-text{display:none}
                  .el-table__footer-wrapper{display:none}
                  ` + style, //  该字符串应该应用于正在打印的html,  去掉打印头部和尾部
          onLoadingEnd: () => {
            this.printFooterShow = false
          }
        })
      })
    }
  }

}
