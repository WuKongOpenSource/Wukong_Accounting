<template>
  <div v-loading="loading" class="app-container">
    <div class="content-header">
      <span>币别设置</span>
      <el-button
        type="primary"
        @click="addTemplate">新增</el-button>
    </div>
    <div class="template-table">

      <el-table
        :data="list"
        :height="tableHeight"
        style="width: 100%;"
        stripe>
        <el-table-column
          v-for="(item, index) in templateList"
          :key="index"
          :prop="item.field"
          :label="item.label"
          :formatter="fieldFormatter"
          show-overflow-tooltip />
        <el-table-column
          fixed="right"
          label="操作"
          width="140">
          <template slot-scope="scope">
            <el-button
              :disabled="scope.row.status != 1"
              type="text"
              @click="templateEdit(scope.row)">编辑</el-button>
            <el-button
              type="text"
              @click="templateDelete(scope)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template-create-dialog
      v-if="templateDialogVisible"
      :action="action"
      :template-dialog-visible="templateDialogVisible"
      @template-close="templateClose"
      @template-submit="templateSubmit" />
  </div>
</template>

<script>
import { fmFinanceTemplateQueryListAPI, fmFinanceTemplateDeleteByIdAPI } from '@/api/fm/voucher'

import templateCreateDialog from './components/Create'

export default {
  name: 'TemplateSet',
  components: {
    templateCreateDialog
  },

  data() {
    return {
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 220,
      // 币别设置
      /** 币别每行的信息 */
      action: { type: 'add' },
      list: [],
      templateList: [
        { label: '类别', field: 'typeName' },
        { label: '模板名称', field: 'name' },
        { label: '汇率', field: 'exchangeRate' },
        // { label: '固定汇率', field: 'rate' },
        { label: '是否本位币', field: 'hometemplate' }
        // { label: '小数位数', field: 'digit' }

      ],
      // 添加币别
      templateDialogVisible: false
    }
  },
  watch: {
  },
  created() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 220
    }
    this.getList()
  },
  methods: {
    /**
     * 币别列表
     */
    getList() {
      this.loading = true
      fmFinanceTemplateQueryListAPI()
        .then(res => {
          this.loading = false
          this.list = res.data.map(o => {
            o.content = JSON.parse(o.content)
            return o
          })
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * 币别列表格式化
     */
    fieldFormatter(row, column) {
      // 如果需要格式化
      if (column.property == 'hometemplate') {
        return row[column.property] === 1 ? '是' : '否'
      }
      if (column.property == 'rate') {
        return row[column.property] == 1 ? '否' : '是'
      }
      return row[column.property]
    },

    /**
     * 币别编辑
     */
    templateEdit(data) {
      this.action = { type: 'update', infoData: data }
      this.templateDialogVisible = true
    },

    /**
     * 币别删除
     */
    templateDelete(scope) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          fmFinanceTemplateDeleteByIdAPI({
            templateId: scope.row.templateId
          })
            .then(res => {
              this.getList()
              this.$message.success('删除成功')
            })
            .catch(() => {})
        })
        .catch(() => {
        })
    },

    /**
     * 币别添加
     */
    addTemplate() {
      this.action = { type: 'add' }
      this.templateDialogVisible = true
    },

    /**
     * 币别添加 -- 关闭
     */
    templateClose() {
      this.templateDialogVisible = false
    },

    /**
     * 币别添加成功
     */
    templateSubmit() {
      console.log(111)
      this.getList()
      this.templateClose()
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "../styles/index.scss";
</style>
