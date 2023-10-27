<template>
  <div>
    <el-dialog
      :visible.sync="showDialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose"
      :append-to-body="true"
      title="凭证摘要库"
      width="500px">

      <div class="content">
        <flexbox>
          <el-input v-model="info.typeName" placeholder="请输入类别名称" class="addInut" />
          <el-button @click="save">保存</el-button>
          <el-button @click="reset">取消</el-button>
        </flexbox>

        <el-table :data="list" class="table" @row-dblclick="confirm">
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button type="text" @click="editItem(scope.row)">编辑</el-button>
              <el-button type="text" @click="deleteItem(scope.row)">删除</el-button>
            </template>
          </el-table-column>
          <el-table-column label="类别名称" prop="typeName" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fmFinanceTemplateTypeQueryListAPI, fmFinanceTemplateTypeAddAPI, fmFinanceTemplateTypeDeleteByIdAPI } from '@/api/fm/voucher'
import { objDeepCopy } from '@/utils'

export default {
  // 组件名  选择客户
  name: 'DigestSelectDialog',
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
        typeName: ''
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
    this.getTypeList()
  },
  mounted() {},
  // 组件方法
  methods: {
    /**
     * @description: 获取摘要列表
     */
    getTypeList() {
      fmFinanceTemplateTypeQueryListAPI()
        .then(res => {
          this.list = res.data
        })
        .catch(() => {

        })
    },
    /**
     * @description: 保存操作
     */
    save() {
      this.lodaing = true
      fmFinanceTemplateTypeAddAPI(this.info).then(res => {
        this.$message.success('添加成功')
        this.getTypeList()
        this.reset()
        this.lodaing = false
      }).catch(() => {
        this.lodaing = false
      })
    },
    /**
     * @description: 编辑某一项
     * @param {*} data
     */
    editItem(data) {
      this.info = objDeepCopy(data)
    },
    /**
     * @description: 删除某一项
     * @param {*} data
     */
    deleteItem(data) {
      this.lodaing = true
      fmFinanceTemplateTypeDeleteByIdAPI({ typeId: data.typeId }).then(res => {
        this.$message.success('删除成功')
        this.getTypeList()
        this.lodaing = false
      }).catch(() => {
        this.lodaing = false
      })
    },
    /**
     * @description: 重置操作
     */
    reset() {
      this.info = {
        typeName: ''
      }
    },
    /**
     * @description: 关闭/取消 事件冒泡
     */
    handleClose() {
      this.$emit('close', false)
    },
    /**
     * @description: 点击确定操作-事件冒泡
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
.table{
  margin-top: 15px;
}
.content{
    width: 100%;
    height: 400px;
    padding: 10px 20px;
    overflow-y: auto;
    span{
        line-height: 30px;
    }
   .btn-right{
      text-align: right;
      margin-top: 10px;
   }
}
</style>
