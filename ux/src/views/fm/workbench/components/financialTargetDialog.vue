<template>
  <div>
    <el-dialog
      :visible="true"
      :close-on-click-modal="false"
      :before-close="handleClose"
      :append-to-body="true"
      title="财务指标管理"
      width="600px">
      <el-form :model="addForm">
        <flexbox justify="space-between">
          <el-form-item label="项目名称">
            <el-input v-model="addForm.projectName" />
          </el-form-item>
          <el-form-item label="科目名称">
            <subject-select-options
              v-model="addForm.subjectId"
            />
          </el-form-item>

        </flexbox>
        <flexbox>
          <el-form-item label="运算符号">
            <el-select v-model="addForm.operation">
              <el-option v-for="(item,index) in operationList" :key="index" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="取数规则">
            <el-select v-model="addForm.accessRule">
              <el-option v-for="(item,index) in accessRuleList" :key="index" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间类型">
            <el-select v-model="addForm.timeType">
              <el-option v-for="(item,index) in timeTypeList" :key="index" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </flexbox>
      </el-form>
      <div class="content">
        <el-table :data="list" @row-dblclick="confirm">
          <el-table-column label="科目" prop="typeName" />
          <el-table-column label="运算符号" prop="name" />
          <el-table-column label="取数规则" prop="name" />
          <el-table-column label="时间类型" prop="name" />
          <el-table-column label="操作" width="120">
            <template slot-scope="scope">
              <el-button type="text" @click="deleteItem(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="saveClick">确定</el-button>
        <el-button @click="handleClose">取消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'

export default {
  // 组件名  选择客户
  name: 'VoucherTemplate',
  components: {
    SubjectSelectOptions
  },
  // 注册组件
  // 接收父组件数据
  props: {
    showVisible: Boolean
  },
  // 组件数据
  data() {
    return {
      lodaing: false,
      info: {
        templateContent: ''
      },
      accessRuleList: ['余数'],
      operationList: ['+', '-', '*', '/'],
      timeTypeList: ['期末'],
      addForm: {
        projectName: '',
        subjectId: '',
        operation: '',
        accessRule: '',
        timeType: ''

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
    // 获取摘要列表
    getList() {
    //   fmFinanceTemplateQueryListAPI()
    //     .then(res => {
    //       this.list = res.data
    //     })
    //     .catch(() => {

    //     })
    },
    save() {
    },
    saveClick() {

    },
    deleteItem(data) {
    //   this.lodaing = true
    //   fmFinanceTemplateDeleteByIdAPI({ templateId: data.templateId }).then(res => {
    //     this.$message.success('删除成功')
    //     this.getList()
    //     this.lodaing = false
    //   }).catch(() => {
    //     this.lodaing = false
    //   })
    },
    handleClose() {
      this.$emit('close', false)
    },
    // 点击确定
    confirm(data) {
      this.$emit('select', data)
      this.$emit('close', false)
    }
  }
}
</script>

<style scoped lang='less'>
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
.el-form-item{
    flex: 1;
    padding-right: 8px;
    margin-bottom: 16px;
    &:last-child{
        padding-right: 0;
    }
    ::v-deep.el-form-item__label{
        line-height: 25px;
    }
}
.content{
    width: 100%;
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
