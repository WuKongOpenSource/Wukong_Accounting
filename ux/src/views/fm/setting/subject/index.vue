<template>
  <div v-loading="loading" class="app-container">
    <div class="content-header">
      <span>
        科目设置
      </span>
      <div class="buttons">
        <el-button
          v-if="$auth('finance.subject.save')"
          type="primary"
          @click="addSubject"
        >新建科目</el-button
        >
        <el-dropdown
          trigger="click"
          class="receive-drop"
          @command="handleCommand"
        >
          <el-button class="dropdown-btn" icon="el-icon-more" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
              v-for="(item, index) in moreBtn"
              :key="index"
              :command="item.type"
            >{{ item.label }}</el-dropdown-item
            >
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
    <flexbox class="content-flex" justify="space-between">
      <template v-if="multipleSelection.length > 0 && batchBtn.length > 0">
        <div class="selected—title">
          已选中
          <span class="selected—count">{{ multipleSelection.length }}</span> 项
        </div>
        <flexbox class="selection-items-box">
          <el-button
            v-for="(item, index) in batchBtn"
            :key="index"
            :icon="item.icon"
            size="medium"
            @click="handleCommand(item.type)"
          >{{ item.label }}</el-button
          >
        </flexbox>
      </template>
      <flexbox-item v-else>
        <el-button
          v-for="(item, index) in subjectTypeOption"
          :key="index"
          :type="subjectType == item.value ? 'selected' : ''"
          @click="subjectType = item.value"
        >
          {{ item.name }}
        </el-button>
      </flexbox-item>
    </flexbox>

    <div class="subject-table">
      <el-table
        id="table"
        ref="table"
        :data="subjectData"
        :height="tableHeight"
        row-key="subjectId"
        stripe
        default-expand-all
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column
          v-for="(item, index) in subjectList"
          :key="index"
          :prop="item.field"
          :label="item.label"
          :width="item.width"
          :formatter="fieldFormatter"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="100px">
          <template slot-scope="scope">
            <el-button type="text" @click="subjectStatus(scope)">
              {{ scope.row.status == 1 ? "已启用" : "已禁用" }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <flexbox slot-scope="scope">
            <el-button
              v-if="$auth('finance.subject.update')"
              type="primary-text"
              @click="subjectEdit(scope.row, $event)"
            >编辑</el-button
            >
            <el-button
              v-if="$auth('finance.subject.save')"
              type="primary-text"
              @click="addSubjectChildren(scope.row)"
            >新建下级</el-button
            >
            <el-button
              v-if="$auth('finance.subject.delete')"
              type="primary-text"
              @click="subjectDelete(scope.row)"
            >删除</el-button
            >
          </flexbox>
        </el-table-column>
      </el-table>
    </div>

    <subject-update-dialog
      :visible.sync="subjectDialogVisible"
      :node-data="selectedNode"
      :subject-type="subjectType"
      :subject-list="subjectData"
      :parent-node="parentNode"
      :help-obj="helpObj.setting"
      @confirm="getSubjectGroupList" />
  </div>
</template>

<script>
import {
  fmFinanceSubjectListAPI,
  fmFinanceSubjectBatchDeleteAPI,
  fmFinanceSubjectUpdateStatusAPI,
  fmFinanceSubjectDownloadExcelAPI,
  fmFinanceSubjectExcelImportAPI,
  exportExportListByTypeAPI
} from '@/api/fm/setting'

import SubjectUpdateDialog from '@/views/fm/components/subject/SubjectUpdateDialog'

import { downloadExcelWithResData } from '@/utils'

export default {
  name: 'SubjectSet',
  components: {
    SubjectUpdateDialog
  },

  data() {
    return {
      loading: false, // 展示加载中效果
      tableHeight: document.documentElement.clientHeight - 290,
      subjectType: 1,
      action: {
        type: 'add'
      },
      helpObj: {
        setting: {
          dataType: 42,
          dataId: 331
        }
      },
      parentNode: null,
      selectedNode: null,
      isAllSelect: false,
      // 科目设置
      /** 科目每行的信息 */
      subjectObj: { name: '', subjectDep: [], settingList: '' },
      subjectData: [],
      multipleSelection: [],
      allIds: [],
      subjectTypeOption: [
        { name: '资产', value: 1 },
        { name: '负债', value: 2 },
        { name: '权益', value: 3 },
        { name: '成本', value: 4 },
        { name: '损益', value: 5 }
      ],
      subjectList: [
        { label: '编码', field: 'number', width: '150px' },
        { label: '名称', field: 'subjectName' },
        { label: '类别', field: 'category', width: '100px' },
        { label: '余额方向', field: 'balanceDirection', width: '80px' },
        { label: '辅助核算', field: 'labelName' },
        { label: '数量', field: 'isAmount', width: '80px' },
        { label: '是否现金项', field: 'isCash', width: '100px' }
      ],
      // 添加科目
      subjectDialogVisible: false
    }
  },
  computed: {
    moreBtn() {
      const list = [
        {
          label: '导入',
          show: this.$auth('finance.subject.import'),
          type: 'import'
        },
        {
          label: '导出',
          show: this.$auth('finance.subject.export'),
          type: 'export'
        }
      ]
      return list.filter((o) => o.show)
    },
    batchBtn() {
      const list = [
        {
          label: '删除',
          show:
            this.$auth('finance.subject.delete') &&
            this.multipleSelection.length,
          type: 'delete'
        },
        {
          label: '批量启用',
          show:
            this.$auth('finance.subject.update') &&
            this.multipleSelection.length,
          type: 'batch-use'
        },
        {
          label: '批量禁用',
          show:
            this.$auth('finance.subject.update') &&
            this.multipleSelection.length,
          type: 'batch-disabled'
        }
      ]
      return list.filter((o) => o.show)
    }
  },
  watch: {
    subjectType() {
      this.getSubjectGroupList()
    }
  },
  created() {
    window.onresize = () => {
      this.tableHeight = document.documentElement.clientHeight - 290
    }
    this.getSubjectGroupList()
  },
  methods: {
    /**
     * 科目列表
     */
    getSubjectGroupList() {
      this.loading = true
      fmFinanceSubjectListAPI({
        type: this.subjectType,
        isTree: 1
      })
        .then((res) => {
          this.loading = false
          this.subjectData = res.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * @description: 公共操作
     * @param {*} type
     */
    handleCommand(type) {
      switch (type) {
        case 'import':
          this.importSubject()
          return
        case 'export':
          this.exportSubject()
          return
        case 'delete':
          this.batchDelete()
          return
        case 'batch-use':
          this.batchUpdateStatus(1)
          return
        case 'batch-disabled':
          this.batchUpdateStatus(2)
          return
      }
    },
    /**
     * 导入
     */
    importSubject() {
      this.$wkImport.import('fmSubject', {
        typeName: '科目',
        historyShow: false,
        noImportProcess: true,
        ownerSelectShow: false,
        repeatHandleShow: false,
        importRequest: fmFinanceSubjectExcelImportAPI, // 导入请求
        templateRequest: fmFinanceSubjectDownloadExcelAPI // 模板请求
      })
    },
    /**
     * 批量删除
     */
    batchDelete() {
      if (!this.multipleSelection.length) {
        this.$message.error('请先选择要删除的科目')
        return
      }
      for (let i = 0; i < this.multipleSelection.length; i++) {
        const item = this.multipleSelection[i]
        if (item.isRelevance) {
          this.$message.error(`[${item.number}]科目已被凭证使用，不能删'`)
          return
        }
        if (this.subjectData.find((o) => o.parentId === item.subjectId)) {
          this.$message.error(
            `[${item.number}]科目下有子级科目，不可删除，不能删'`
          )
          return
        }
      }
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.loading = true
        const ids = this.multipleSelection.map((item) => item.subjectId)
        fmFinanceSubjectBatchDeleteAPI({ ids })
          .then((res) => {
            this.$message.success('操作成功')
            this.getSubjectGroupList()
            this.loading = false
          })
          .catch(() => {
            this.loading = false
          })
          .catch(() => {
          })
      })
    },
    /**
     * 导出
     */
    exportSubject() {
      const params = {
        type: this.subjectType,
        isTree: 0,
        dataList: this.subjectData
      }
      if (this.multipleSelection?.length) {
        params.dataList = this.multipleSelection
      }
      exportExportListByTypeAPI(params).then(res => {
        downloadExcelWithResData(res)
      })
      // exportElTable('subject.xlsx', 'table')
    },
    /**
     * @description:批次改变状态
     * @param {*} status
     */
    batchUpdateStatus(status) {
      if (!this.multipleSelection.length) {
        this.$message.error('请先勾选科目！')
        return
      }
      this.loading = true
      const ids = this.multipleSelection.map((item) => item.subjectId)

      fmFinanceSubjectUpdateStatusAPI({ ids, status })
        .then((res) => {
          this.$message.success('操作成功')
          this.getSubjectGroupList()
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * @description: 处理选择改变事件
     * @param {*} val
     */
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    /**
     * 科目列表格式化
     */
    fieldFormatter(row, column) {
      if (column.property == 'category') {
        if (!row[column.property]) {
          return ''
        }
        console.log(row[column.property])
        const filterCategory = {
          1: [
            { name: '流动资产', value: 1 },
            { name: '非流动资产', value: 2 }
          ],
          2: [
            { name: '流动负债', value: 1 },
            { name: '非流动负债', value: 2 }
          ],
          3: [{ name: '所有者权益', value: 1 }],
          4: [{ name: '成本', value: 1 }],
          5: [
            { name: '营业收入', value: 1 },
            { name: '其他收益', value: 2 },
            { name: '期间费用', value: 3 },
            { name: '其他损失', value: 4 },
            { name: '营业成本及税金', value: 5 },
            { name: '以前年度损益调整', value: 6 },
            { name: '所得税', value: 7 }
          ],
          6: [{ name: '共同', value: 1 }]
        }
        const typeValue = filterCategory[row.type]
        const category = typeValue.find(
          (item) => item.value == row[column.property]
        )
        if (category) {
          return category.name
        } else {
          return ''
        }
      } else if (column.property == 'balanceDirection') {
        return row[column.property] == 1 ? '借' : '贷'
      } else if (column.property == 'isCash') {
        return row[column.property] == 1 ? '是' : ''
      } else if (column.property == 'isAmount') {
        return row[column.property] == 1 ? row.amountUnit || '是' : ''
      } else if (column.property == 'subjectName') {
        var name = row[column.property]
        for (let i = 1; i < row.grade; i++) {
          name = '　　' + name
        }
        return name
      }
      return row[column.property]
    },
    /**
     * 科目编辑
     */
    subjectEdit(data) {
      console.log(data)
      if (data.grade == 1) {
        this.parentNode = null
      } else {
        this.parentNode = this.getParentNode(data, this.subjectData)
      }
      this.selectedNode = data
      this.subjectDialogVisible = true
    },
    /**
     * @description: 获取父节点
     * @param {*} data
     * @param {*} arr
     */
    getParentNode(data, arr) {
      var node = null
      for (let i = 0; i < arr.length; i++) {
        const item = arr[i]
        if (item.subjectId == data.parentId) {
          node = item
        } else if (item.children && item.children.length) {
          node = this.getParentNode(data, item.children)
        }
        if (node) {
          return node
        }
      }
    },
    /**
     * 科目删除
     */
    subjectDelete(row) {
      if (row.isRelevance) {
        this.$message.error('该科目已被凭证使用，不能删除')
        return
      }
      if (this.subjectData.find((o) => o.parentId == row.subjectId)) {
        this.$message.error('该科目下有子级科目，不可删除')
        return
      }
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          fmFinanceSubjectBatchDeleteAPI({
            ids: [row.subjectId]
          })
            .then((res) => {
              this.getSubjectGroupList()
              this.$message.success('删除成功')
            })
            .catch(() => {})
        })
        .catch(() => {
        })
    },

    /**
     * @description: 改变科目状态
     * @param {*} scope
     */
    subjectStatus(scope) {
      // 启用停用
      this.$confirm(
        '您确定要' + (scope.row.status === 2 ? '启用' : '停用') + '该科目?',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          fmFinanceSubjectUpdateStatusAPI({
            ids: [scope.row.subjectId],
            status: scope.row.status === 2 ? 1 : 2
          })
            .then((res) => {
              // scope.row['status'] = scope.row['status'] === 2? 1 : 2
              this.getSubjectGroupList()
              this.$message({
                type: 'success',
                message: '操作成功'
              })
            })
            .catch(() => {})
        })
        .catch(() => {
        })
    },
    // selectAll(selection, first) {
    //   if (!first) {
    //     this.isAllSelect = !this.isAllSelect
    //   }
    //   selection.map(el => {
    //     if (el.children) {
    //       el.children.map(j => {
    //         this.toggleSelection(j, this.isAllSelect)
    //       })
    //       if (el.children.length > 0) {
    //         this.selectAll(el.children, true)
    //       }
    //     }
    //   })
    // },
    // toggleSelection(row, select) {
    //   if (select) {
    //     this.$refs.table.toggleRowSelection(row, select)
    //   } else {
    //     this.$refs.table.clearSelection()
    //   }
    // },
    /**
     * 科目添加
     */
    addSubject() {
      this.parentNode = null
      this.selectedNode = null
      this.subjectDialogVisible = true
    },

    /**
     * 添加下级科目
     * @param data
     */
    addSubjectChildren(data) {
      this.selectedNode = null
      this.parentNode = data
      console.log('parentNode: ', this.parentNode)
      this.subjectDialogVisible = true
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "../styles/index.scss";

.content-header {
  padding-bottom: 0;
}

.selected—title {
  flex-shrink: 0;
  padding-right: 20px;
  font-weight: $--font-weight-semibold;
  line-height: 32px;

  .selected—count {
    color: $--color-primary;
  }
}

::v-deep.el-radio-group > .is-active > .el-radio-button__inner {
  background-color: #091e42;
  border-color: #091e42;
}

.buttons {
  .dropdown-btn {
    margin-left: 8px;
  }
}

.subject-table {
  .el-table {
    width: 100%;
    height: 100%;

    ::v-deep.el-table__expand-icon--expanded,
    ::v-deep.el-table__indent,
    ::v-deep.el-table__placeholder {
      display: none;
    }

    .icon-btn {
      margin: 0 8px;
      font-size: 13px;
      color: #999;
      cursor: pointer;

      &:hover {
        color: $--color-primary;
      }
    }
  }
}
</style>
