<template>
  <div>
    <el-dialog
      :visible.sync="showDialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose"
      :append-to-body="true"
      title="凭证模板库"
      width="500px">

      <div class="content">
        <el-table :data="list" @row-dblclick="confirm">
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button type="text" @click="deleteItem(scope.row)">删除</el-button>
            </template>
          </el-table-column>
          <el-table-column label="类别" prop="typeName" />
          <el-table-column label="名称" prop="name" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import { fmFinanceTemplateQueryListAPI, fmFinanceTemplateDeleteByIdAPI } from '@/api/fm/voucher'
// import { objDeepCopy } from '@/utils'

export default {
  // 组件名  选择客户
  name: 'VoucherTemplate',
  // 注册组件
  components: {},
  // 接收父组件数据
  props: {
    showDialogVisible: Boolean
  },
  // 组件数据
  data() {
    return {
      lodaing: false,
      info: {
        templateContent: ''
      },
      list: []
    }
  },
  // 计算属性
  computed: {},
  // 监听属性
  watch: {
  },
  // 生命周期钩子函数
  created() {
    this.getList()
  },
  mounted() {},
  // 组件方法
  methods: {
    /**
     * @description: 获取摘要列表
     */
    getList() {
      fmFinanceTemplateQueryListAPI()
        .then(res => {
          this.list = res.data
        })
        .catch(() => {

        })
    },

    /**
     * @description: 删除操作
     * @param {*} data
     */
    deleteItem(data) {
      this.lodaing = true
      fmFinanceTemplateDeleteByIdAPI({ templateId: data.templateId }).then(res => {
        this.$message.success('删除成功')
        this.getList()
        this.lodaing = false
      }).catch(() => {
        this.lodaing = false
      })
    },
    /**
     * @description: 关闭操作 冒泡事件
     * @param {*}
     * @return {*}
     */
    handleClose() {
      this.$emit('close', false)
    },
    /**
     * @description: 点击确定
     * @param {*} data
     */
    confirm(data) {
      this.$emit('select', data)
      this.$emit('close', false)
    }
  }
}
</script>

<style scoped lang='less'>

::v-deep.el-dialog__body{
    padding: 0 !important;
}
.headerList{
    display: flex;
    width: 100%;
    border-top:1px solid #ccc ;
    border-bottom:1px solid #ccc ;
    .headerList-item{
        padding: 10px 0;
        width:16.66%;
        border-left:1px solid #ccc ;
        text-align: center;
        cursor:pointer
    }
    .headerList-item:hover{
        color: #2396fd;
    }
}
.isActive{
    background-color: #2362fb;
    color:#ffffff ;
}
.isActive:hover{
    color:#ffffff !important;
}
.content{
    width: 100%;
    height: 400px;
    padding: 0 20px;
    overflow-y: auto;
    span{
        line-height: 30px;
    }
   .addInut{
       width: 100%;
       height: 60px;
   }
   .btn-right{
      text-align: right;
      margin-top: 10px;
   }
}
</style>
