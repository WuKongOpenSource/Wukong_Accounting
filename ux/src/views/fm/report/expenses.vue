<template>
  <div v-loading="loading" class="main">
    <div>
      {{ pageTitle }}
      <flexbox class="content-flex" justify="space-between">
        <flexbox class="search">
          <div class="date-box">年月</div>
          <el-select v-model="dateValue" class="select-date" >
            <el-option
              v-for="item in dateOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
          <el-button plain icon="el-icon-refresh-right" />
        </flexbox>

        <flexbox class="buttons">
          <el-button
            type="primary"
            class="rt">打印</el-button>
          <el-button
            type="primary"
            class="rt">导出</el-button>
        </flexbox>
      </flexbox>
      <div class="balabce-table">

        <el-table
          :data="balabceData"
          :height="tableHeight"
          :header-cell-style="headerCellStyle"
          style="width: 100%;"
          stripe>
          <el-table-column
            v-for="(item, index) in balabceList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            :formatter="fieldFormatter"
            show-overflow-tooltip>
            <template v-if="item.type=='input'" slot-scope="scope">
              <el-input v-model="scope.row[item.field]" />
            </template>
            <template v-if="item.columns&&item.columns.length">
              <el-table-column
                v-for="(children, cindex) in item.columns"
                :key="cindex"
                :prop="children.field"
                :label="children.label"
                :formatter="fieldFormatter"
                show-overflow-tooltip>
                <template slot-scope="scope">
                  <!-- <template v-if="children.type=='input'" slot-scope="scope">
                  <el-input v-model="scope.row[item.field]" />
                </template> -->
                  <template v-if="children.type=='input'">
                    <el-input v-model="scope.row[item.field]" />
                  </template>
                </template>
              </el-table-column>
            </template>

          </el-table-column>
        </el-table>
        <div class="p-contianer">
          <el-pagination
            :current-page="currentPage"
            :page-sizes="pageSizes"
            :page-size.sync="pageSize"
            :total="total"
            class="p-bar"
            background
            layout="prev, pager, next, sizes, total, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange" />
        </div>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'ExpensesSheet',

  data() {
    return {
      pageTitle: '费用明细表',

      loading: false, // 展示加载中效果
      balabceType: '1',
      tableHeight: document.documentElement.clientHeight - 260, // 表的高度
      currencyOptions: ['rmb', 'sss'],

      dateValue: '2018-01',
      dateOptions: [
        { value: '2018-01', label: '2018-01' },
        { value: '2018-02', label: '2018-02' },
        { value: '2018-03', label: '2018-03' },
        { value: '2018-04', label: '2018-04' },
        { value: '2018-05', label: '2018-05' },
        { value: '2018-06', label: '2018-06' },
        { value: '2018-07', label: '2018-07' },
        { value: '2018-08', label: '2018-08' }
      ],

      currencyType: '',
      balabceList: [
        { label: '资产', field: 'name' },
        { label: '行次', field: 'deptName' },
        { label: '期末余额', field: 'deptName' },
        { label: '期初余额', field: 'deptName' },
        { label: '负债和所有者权益', field: 'deptName' },
        { label: '行次', field: 'deptName' },
        { label: '期末余额', field: 'deptName' },
        { label: '期初余额', field: 'deptName' }
      ],
      showSearch: false,
      hideNum: false,
      // 余额设置
      /** 余额每行的信息 */
      balabceData: [{}],
      // 添加余额
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 30, 40],
      total: 0
    }
  },
  computed: {
    iconClass() {
      return this.showScene ? 'arrow-up' : 'arrow-down'
    }
  },
  watch: {
    balabceType() {
      this.getbalabceGroupList()
    },
    currencyType() {
      this.getbalabceGroupList()
    }
  },
  created() {
    /** 控制table的高度 */
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 260
    }
    this.getbalabceGroupList()
  },
  methods: {
    /**
     * 余额列表头样式
     */
    headerCellStyle(val, index) {
      return { background: '#F2F2F2' }
    },
    searchHandle() {

    },
    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.getbalabceGroupList()
    },
    /**
     * 更改当前页数
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getbalabceGroupList()
    },

    /**
     * 余额列表
     */
    getbalabceGroupList() {
      // this.loading = true
    //   balabceGroupListAPI({
    //     page: this.currentPage,
    //     limit: this.pageSize
    //   })
    //     .then(res => {
    //       this.loading = false
    //       this.balabceData = res.data.list
    //       this.total = res.data.totalRow
    //     })
    //     .catch(() => {
    //       this.loading = false
    //     })
    },

    /**
     * 余额列表格式化
     */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (column.property == 'deptName') {
        // 格式部门
        const structures = row.deptList || []
        const strName = structures
          .map(item => {
            return item.name
          })
          .join('、')
        return strName || '全公司'
      } else if (column.property === 'status') {
        if (row[column.property] == 1) {
          return '启用'
        }
        return '停用'
      }
      return row[column.property]
    },

    /**
     * 添加
     */
    addSubject() {
      this.$router.push('/fm/voucher/subs/create')
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.content-title {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

// .main{
//     background-color: #fff;
// }
.content-title > span {
  display: inline-block;
  height: 36px;
  margin-left: 20px;
  line-height: 36px;
}

.content-flex {
  box-sizing: border-box;
  width: 100%;
  padding: 20px 30px;

  .buttons {
    float: right;
    width: auto;
  }

  .search {
    .search-item {
      margin-left: 10px;
    }

    .select-date {
      margin: 0 15px;
    }
  }

  .buttons ::v-deep .el-button {
    margin-right: 10px;
  }
}

/* 余额设置 */

.balabce-table {
  box-sizing: border-box;
  flex: 1;
  margin: 30px;
  margin-top: 0;
  overflow: auto;
  border: 1px solid #e6e6e6;
}

.p-contianer {
  position: relative;
  height: 44px;
  background-color: white;

  .p-bar {
    float: right;
    margin: 5px 100px 0 0;
    font-size: 14px !important;
  }
}
</style>
