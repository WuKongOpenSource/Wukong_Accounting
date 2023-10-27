<template>
  <div>
    <el-dialog
      :visible.sync="showDialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose"
      :append-to-body="true"
      title="新增模板"
      width="400px">

      <div class="content">
        <el-form ref="form" :model="info" :rules="rules" label-width="80px">
          <el-form-item label="模板类型:" prop="typeId">
            <el-popover

              trigger="click"
              popper-class="no-padding-popover"
              width="250">
              <el-input
                slot="reference"
                v-model="info.typeName"
                :readonly="true"
                class="scene-select">
                <i
                  slot="suffix"
                  class="'el-input__icon' 'el-icon-arrow-down'" />
              </el-input>
              <select-dropdown
                :list="typeList"
                :prop="{label:'typeName',addTitle:'添加类型'}"
                @select="templateSelect"
                @handle-add="selectTemplateDialogVisible=true"
              />
            </el-popover>
          </el-form-item>
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="info.name" />
          </el-form-item>
          <el-form-item label="保存金额">
            <el-checkbox v-model="info.saveMoney" />
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" justify="flex-end">
        <el-button type="primary" @click="confirm">确认</el-button>
        <el-button @click="handleClose">取消</el-button>
      </span>

    </el-dialog>
    <template-select-dialog
      v-if="selectTemplateDialogVisible"
      :show-dialog-visible="selectTemplateDialogVisible"
      @close="selectTemplateClose"
      @select="selectTemplateSubmit"
    />
  </div>
</template>

<script>

import { fmFinanceTemplateTypeQueryListAPI, fmFinanceTemplateAddAPI } from '@/api/fm/voucher'

import SelectDropdown from '@/views/fm/components/SelectDropdown'
import TemplateSelectDialog from './TemplateSelectDialog'

// import { objDeepCopy } from '@/utils'

export default {
  // 组件名  选择客户
  name: 'VoucherTemplate',
  // 注册组件
  components: {
    SelectDropdown,
    TemplateSelectDialog
  },
  // 接收父组件数据
  props: {
    content: Array,
    showDialogVisible: Boolean
  },
  // 组件数据
  data() {
    return {
      lodaing: false,
      selectTemplateDialogVisible: false,
      info: {
        typeName: '',
        typeId: '',
        templateTypeName: '',
        name: '',
        saveMoney: false
      },
      typeList: [],
      rules: {
        typeId: [
          { required: true, message: '请选择模板类型', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '模板名称不能为空', trigger: 'blur' }
        ]
      }
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
      fmFinanceTemplateTypeQueryListAPI()
        .then(res => {
          this.typeList = res.data
        })
        .catch(() => {

        })
    },
    /**
     * @description: 模板选择
     * @param {*} data
     */
    templateSelect(data) {
      this.info.typeId = data.typeId
      this.info.typeName = data.typeName
    },
    /**
     * @description: 选择模板提交
     * @param {object} data
     */
    selectTemplateSubmit(data) {
      this.info.typeId = data.typeId
      this.info.typeName = data.typeName
    },
    /**
     * @description:关闭事件冒泡
     */
    handleClose() {
      this.$emit('close', false)
    },
    /**
     * @description: 选择模板关闭
     */
    selectTemplateClose() {
      console.log('dddd')
      this.selectTemplateDialogVisible = false
      this.getList()
    },
    //
    /**
     * @description: 点击确定
     * @param {object} data
     */
    confirm(data) {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.lodaing = true
          const list = this.content.map(item => {
            if (this.info.saveMoney) {
              return {
                associationBOS: item.associationBOS,
                digestContent: item.digestContent,
                subject: item.subject,
                subjectId: item.subjectId,
                quantity: item.quantity,
                debtorBalance: item.debtorBalance,
                creditBalance: item.creditBalance
              }
            } else {
              return {
                associationBOS: item.associationBOS,
                digestContent: item.digestContent,
                subject: item.subject,
                subjectId: item.subjectId,
                quantity: '',
                debtorBalance: '',
                creditBalance: ''
              }
            }
          })
          fmFinanceTemplateAddAPI({ ...this.info, content: JSON.stringify(list) }).then(res => {
            this.$message.success('保存成功')
            this.lodaing = false
            this.$emit('close', false)
            console.log(res)
          }).catch(() => {
            this.lodaing = false
          })
        }
      })
    }
  }
}
</script>

<style scoped lang='less'>

::v-deep.el-dialog__body{
    padding:  10px !important;
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
    height: 300px;
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
