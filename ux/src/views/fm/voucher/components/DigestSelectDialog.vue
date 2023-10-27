<template>
  <el-dialog
    :visible.sync="showDialogVisible"
    :close-on-click-modal="false"
    :before-close="handleClose"
    :append-to-body="true"
    width="500px">
    <span slot="title" class="el-dialog__title">
      凭证摘要库
      <i
        v-if="helpObj"
        class="wk wk-icon-fill-help wk-help-tips"
        :data-type="helpObj.dataType"
        :data-id="helpObj.dataId" />
    </span>
    <div class="content">
      <span>摘要内容</span>
      <el-input v-model="info.digestContent" type="textarea" class="addInut" />
      <div class="btn-right">
        <el-button type="primary" @click="save">保存</el-button>
        <el-button @click="reset">取消</el-button>
      </div>
      <el-table :data="list" @row-dblclick="confirm">
        <el-table-column label="摘要内容" prop="digestContent" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="primary-text" @click="editItem(scope.row)">编辑</el-button>
            <el-button type="primary-text" @click="deleteItem(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>

<script>

import { fmFinanceDigestQueryListAPI, fmFinanceDigestAddOrUpdataAPI, fmFinanceDigestDeleteAPI } from '@/api/fm/voucher'
import { objDeepCopy } from '@/utils'

export default {
  // 组件名  选择客户
  name: 'DigestSelectDialog',
  // 注册组件
  components: {},
  // 接收父组件数据
  props: {
    showDialogVisible: Boolean,
    helpObj: Object
  },
  // 组件数据
  data() {
    return {
      lodaing: false,
      info: {
        digestContent: ''
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
    this.getDisgestList()
  },
  mounted() {},
  // 组件方法
  methods: {
    /**
     * @description: 获取摘要列表
     */
    getDisgestList() {
      fmFinanceDigestQueryListAPI()
        .then(res => {
          console.log('摘要列表', res)
          this.list = res.data
        })
        .catch(() => {

        })
    },
    /**
     * @description: 保存操作
     */
    save() {
      if (!this.info.digestContent.trim()) {
        this.$message.error('摘要内容不能为空')
        return
      }
      this.lodaing = true
      fmFinanceDigestAddOrUpdataAPI(this.info).then(res => {
        this.$message.success('添加成功')
        this.getDisgestList()
        this.reset()
        this.lodaing = false
      }).catch(() => {
        this.lodaing = false
      })
    },
    /**
     * @description: 编辑某一项
     * @param {object} data
     */
    editItem(data) {
      this.info = objDeepCopy(data)
    },
    /**
     * @description: 删除某一项
     * @param {object} data
     */
    deleteItem(data) {
      this.lodaing = true
      fmFinanceDigestDeleteAPI({ digestId: data.digestId }).then(res => {
        this.$message.success('删除成功')
        this.getDisgestList()
        this.lodaing = false
      }).catch(() => {
        this.lodaing = false
      })
    },
    /**
     * @description: 重置info
     */
    reset() {
      this.info = {
        digestContent: ''
      }
    },
    /**
     * @description: 关闭option冒泡
     */
    handleClose() {
      this.$emit('closeOptions', false)
    },
    /**
     * @description:点击确定
     * @param {object} data
     */
    confirm(data) {
      this.$emit('select-digest', data)
      this.$emit('closeOptions', false)
    }
  }
}
</script>

<style scoped lang='less'>

::v-deep.el-dialog__body{
    padding: 0 !important;
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
   .el-table {
     margin-top: 8px;
   }
}
</style>
