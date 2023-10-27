<template>
  <el-dialog
    v-loading="loading"
    :visible.sync="dialogVisible"
    :title="title"
    :close-on-click-modal="false"
    append-to-body
    width="880px"
    @close="handleCancel">
    <div class="category-create">
      <el-form
        label-width="auto"
        label-position="left">
        <el-row :gutter="20">
          <el-col :span="18">
            <el-form-item label="名称" label-width="50px">
              <el-input
                v-model="form.digestContent" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="" label-width="0">
              <el-checkbox
                v-model="form.isEndOver"
                :true-label="1"
                :false-label="0"
                @change="handleChangeEnd">
                期末结转凭证
              </el-checkbox>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row
          v-if="form.isEndOver"
          :gutter="20">
          <el-col :span="8">
            <el-form-item label="科目" label-width="40px">
              <subject-select-options
                v-if="subjectList.length > 0"
                v-model="form.subjectId"
                :subject-list="subjectList" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="取数规则">
              <el-select v-model="form.gainRule">
                <el-option
                  v-for="item in ruleOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时间类型">
              <el-select v-model="form.timeType">
                <el-option
                  v-for="item in timeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-table
        :data="tableData"
        height="40vh">
        <el-table-column prop="digestContent" label="摘要" width="200">
          <template slot-scope="{row, column}">
            <el-input v-model="row[column.property]" />
          </template>
        </el-table-column>
        <el-table-column prop="isLend" label="借/贷" width="80">
          <template slot-scope="{row, column}">
            <el-select v-model="row[column.property]">
              <el-option :value="1" label="借" />
              <el-option :value="2" label="贷" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="subjectId" label="科目" width="369">
          <template slot-scope="{row, column}">
            <subject-select-options
              v-if="subjectList.length > 0"
              v-model="row[column.property]"
              :subject-list="subjectList" />
          </template>
        </el-table-column>
        <el-table-column prop="moneyRatio" label="金额比例%" width="120">
          <template slot-scope="{row, column}">
            <el-input-number
              v-model="row[column.property]"
              :controls="false" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="80">
          <template slot-scope="{$index}">
            <el-button type="primary-text" @click="handleDelete($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-button
        type="text"
        icon="el-icon-plus"
        class="add-btn"
        plain
        @click="handleAdd">添加一条数据</el-button>
    </div>

    <span
      slot="footer"
      class="dialog-footer">
      <el-button
        v-debounce="handleConfirm"
        type="primary">确定</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </span>

    <subject-select-options
      v-if="selectSubjectDialogVisible"
      :visible.sync="selectSubjectDialogVisible"
      @confirm="selectSubject" />
  </el-dialog>
</template>

<script>
import {
  querySubjectListByTplIdAPI,
  addTemplateAPI,
  updateTemplateAPI
} from '@/api/fm/settleAccounts'
import {
  fmFinanceSubjectListAPI
} from '@/api/fm/setting'

import SubjectSelectOptions from '../../components/subject/SubjectSelectOptions'

export default {
  name: 'TplDetailDialog',
  components: {
    SubjectSelectOptions
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    tplType: {
      type: [String, Number]
    },
    tplData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      loading: false,
      dialogVisible: false,
      form: {}, // 表单

      ruleOptions: [
        { label: '余额', value: 1 },
        { label: '借方余额', value: 2 },
        { label: '贷方余额', value: 3 }
        // { label: '借方发生额', value: 4 },
        // { label: '贷方发生额', value: 5 },
        // { label: '损益发生额', value: 6 }
      ],
      timeOptions: [
        { label: '期末', value: 1 },
        { label: '期初', value: 2 },
        { label: '年初', value: 3 }
      ],
      subjectList: [],
      tableData: [],
      editRowIndex: null,

      selectSubjectDialogVisible: false
    }
  },
  computed: {
    title() {
      return this.tplData ? '编辑分类' : '新增分类'
    }
  },
  watch: {
    visible: {
      handler(val) {
        this.dialogVisible = val
        let digestContent = ''
        if (val) {
          if (this.tplData) {
            digestContent = this.tplData.digestContent || ''
            this.form = { ...this.tplData }
            this.getTplSubjectByTplId()
          }
        } else {
          digestContent = ''
        }
        this.$set(this.form, 'digestContent', digestContent)
      },
      immediate: true
    }
  },
  created() {
    this.getSubjectList()
  },
  methods: {
    /**
     * @description: 获取科目列表
     */
    getSubjectList() {
      this.loading = true
      fmFinanceSubjectListAPI({
        type: null,
        isTree: 0
      }).then((res) => {
        this.loading = false
        this.subjectList = res.data || []
      }).catch(() => {
        this.loading = false
      })
    },

    /**
     * 根据模板id获取模版下的科目
     */
    getTplSubjectByTplId() {
      this.loading = true
      querySubjectListByTplIdAPI({
        templateId: this.tplData.templateId
      }).then(res => {
        this.loading = false
        console.log(res.data)
        this.tableData = res.data || []
      }).catch(() => {
        this.loading = false
      })
    },

    /**
     * @description: 期末结转凭证
     */
    handleChangeEnd() {
      if (this.form.isEndOver === 1) {
        this.$set(this.form, 'subjectId', null)
        this.$set(this.form, 'timeType', 1)
        this.$set(this.form, 'gainRule', 1)
      } else {
        const keys = ['subjectId', 'timeType', 'gainRule']
        keys.forEach(key => {
          delete this.form[key]
        })
      }
    },

    /**
     * @description: 选择科目
     * @param {*} data
     */
    selectSubject(data) {
      this.selectSubjectDialogVisible = false
      // this.selectTemp.subject = data
      console.log('data: ', data)
      this.tableData[this.editRowIndex].number = data.subjectId
      this.$set(this.tableData, this.editRowIndex, this.tableData[this.editRowIndex])
    },

    /**
     * 删除条目
     * @param index
     */
    handleDelete(index) {
      this.tableData.splice(index, 1)
    },

    /**
     * 添加条目
     */
    handleAdd() {
      this.tableData.push({
        digestContent: '',
        isLend: 1,
        moneyRatio: 0,
        number: ''
      })
    },

    /**
     * @description: 取消操作
     */
    handleCancel() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
    },

    /**
     * @description: 确定操作
     */
    handleConfirm() {
      const params = {
        ...this.form,
        templateType: this.tplType
      }
      if (this.tplData) {
        params.templateId = this.tplData.templateId
      }

      if (this.tableData.length === 0) {
        this.$message.error('请添加科目数据')
        return
      }

      for (let i = 0; i < this.tableData.length; i++) {
        const item = this.tableData[i]
        if (!item.digestContent) {
          this.$message.error('摘要不能为空')
          return
        }
        if (!item.subjectId) {
          this.$message.error('科目不能为空')
          return
        }
      }
      const num1 = this.tableData
        .filter(o => o.isLend === 1)
        .reduce((a, b) => a.moneyRatio + b.moneyRatio, 0)
      if (num1 > 100) {
        this.$message.error('借方科目百分比之和不能超过100%')
        return
      }
      const num2 = this.tableData
        .filter(o => o.isLend === 1)
        .reduce((a, b) => a.moneyRatio + b.moneyRatio, 0)
      if (num2 > 100) {
        this.$message.error('贷方科目百分比之和不能超过100%')
        return
      }

      params.templateSubjects = this.tableData
      if (!params.isEndOver || params.isEndOver !== 1) {
        const keys = ['subjectId', 'timeType', 'gainRule']
        keys.forEach(key => {
          delete this.form[key]
        })
      }
      console.log('save: ', params)

      const request = this.tplData ? updateTemplateAPI : addTemplateAPI
      this.loading = true
      request(params).then(res => {
        this.loading = false
        this.$emit('confirm', params.isEndOver)
        this.handleCancel()
      }).catch(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped lang="scss">
::v-deep .el-dialog__body {
  padding: 0;
}

::v-deep .el-checkbox__label {
  font-weight: normal;
}

.category-create {
  padding: 15px;

  ::v-deep .el-icon-delete {
    color: red;
  }

  .el-input-number {
    width: 100%;
  }

  .add-btn {
    margin-top: 15px;
    margin-left: 10px;
  }
}
</style>
