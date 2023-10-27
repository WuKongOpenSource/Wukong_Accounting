<template>
  <el-dialog
    v-bind="$attrs"
    title="提示"
    width="400px"
    :close-on-click-modal="false"
    :show-close="false"
    append-to-body
    v-on="$listeners"
  >
    <div v-loading="loading" class="dialog-wrap">
      <div class="dialog-title">
        您正在为“{{ subjectName }}“科目配置辅助核算，此科目已被使用，数据将转移到辅助核算项目中。
      </div>
      <el-form
        ref="form"
        label-position="left"
        :model="form"
        :rules="rules"
        label-width="80px"
        :inline="false"
        size="normal">
        <div class="form-title">请选择数据迁移的辅助核算项目：</div>
        <el-form-item
          v-for="(item,index) in formField"
          :key="index"
          :label="item.label"
          :prop="item.fieldName">
          <el-select v-model="form[item.fieldName]" style="width: 100%;">
            <el-option
              v-for="item in item.options"
              :key="item.carteId+item.carteName"
              :label="item.carteName"
              :value="item.carteId" />
          </el-select>
        </el-form-item>
        <div class="form-warning">该操作不可逆，您是否要继续？</div>
      </el-form>
    </div>
    <span slot="footer">
      <el-button type="primary" @click="saveHandle">确定</el-button>
      <el-button @click="closeHandle">取消</el-button>
    </span>
  </el-dialog>
</template>
<script>

import { fmAdjuvantQueryByAdjuvantIdAPI } from '@/api/fm/setting.js'

export default {
  props: {
    tipsFormField: {
      type: Array,
      default: () => []
    },
    nodeData: {
      type: Object,
      default: null
    },
    parentNode: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      loading: false,
      formField: [],
      form: {},
      rules: {}
    }
  },
  computed: {
    subjectName() {
      const nodeData = this.nodeData || this.parentNode
      return nodeData?.subjectName || ''
    }
  },
  created() {
    this.init()
  },
  methods: {
    // 初始化数据
    async init() {
      this.loading = true
      const list = []
      for (let index = 0; index < this.tipsFormField.length; index++) {
        const element = this.tipsFormField[index]
        element.label = element.adjuvantName
        element.value = element.adjuvantId
        element.fieldName = element.adjuvantId
        element.options = await this.getDataListById(element.adjuvantId)
        list.push(element)
      }
      this.rules = list.reduce((prev, curr) => {
        prev[curr.fieldName] = [{ required: true, message: `请输入${curr.label}`, trigger: 'blur' }]
        return prev
      }, {})
      this.formField = list
      this.loading = false
    },
    /**
     * 保存操作
     */
    saveHandle() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const associationMap = Object.keys(this.form).reduce((prev, curr) => {
          prev[curr] = this.formField.find(i => i.adjuvantId == curr).options.find(i => i.carteId == this.form[curr])
          const nodeData = this.nodeData || this.parentNode
          // 添加历史的辅助核算 label 和 labelname
          const adjuvantObj = nodeData.adjuvantList?.find(i => i.adjuvantId == curr)
          if (adjuvantObj) {
            prev[curr].label = adjuvantObj.label
            prev[curr].labelName = adjuvantObj.labelName
          }
          return prev
        }, {})
        this.$emit('save', associationMap)
      })
    },
    // 取消操作
    closeHandle() {
      this.$emit('update:visible', false)
      this.$emit('close')
    },
    /**
     * 获取辅助核算下的数据
     */
    getDataListById(adjuvantId) {
      const params = {
        pageType: 0,
        adjuvantId
      }
      return fmAdjuvantQueryByAdjuvantIdAPI(params).then(res => res.data.list)
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog-wrap {
  .dialog-title {

  }

  .form-title {
    height: 30px;
    line-height: 30px;
  }

  .form-warning {
    color: red;
  }
}

</style>
