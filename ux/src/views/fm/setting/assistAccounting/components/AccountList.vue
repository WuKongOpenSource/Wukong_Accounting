<template>
  <div v-loading="loading" class="account-list">
    <div v-if="accountDialogVisible" class="content-list">
      <flexbox class="main-header" justify="space-between">
        <span class="main-title">辅助核算</span>
        <div class="buttons">
          <el-button
            type="primary"
            @click="addAdjuvant">新增</el-button>
          <el-dropdown
            trigger="click"
            class="receive-drop"
          >
            <el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-if="multipleSelection.length" @click.native="batchDelete">删除</el-dropdown-item>
              <el-dropdown-item @click.native="importAccount">导入</el-dropdown-item>
              <el-dropdown-item @click.native="exportAccount">导出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </flexbox>
      <flexbox class="content-flex" justify="space-between">
        <flexbox-item class="left-box">
          <el-button v-for="(item,index) in fixedList" :key="index" :type="accountType==item.adjuvantId?'selected':''" @click="accountType=item.adjuvantId">
            {{ item.adjuvantName }}
          </el-button>
          <el-select v-model="customValue" class="custom-select" mode="no-border" placeholder="选择自定义辅助核算">
            <el-option
              v-for="item in customOptions"
              :key="item.adjuvantId"
              :label="item.adjuvantName"
              :value="item.adjuvantId" />
          </el-select>
          <el-input
            v-model="searchValue"
            class="searchInput"
            placeholder="输入关键词"
            @change="searchInputChange()">
            <el-button
              slot="suffix"
              type="icon"
              icon="wk wk-sousuo"
              @click="handlerSearch" />
          </el-input>
        </flexbox-item>

      </flexbox>

      <div class="account-table">
        <el-table
          id="table"
          :data="listData"
          height="92%"
          row-key="carteId"
          stripe
          default-expand-all
          border
          @selection-change="handleSelectionChange">
          <el-table-column
            type="selection"
            width="55" />
          <el-table-column
            v-for="(item, index) in accountList"
            :key="index"
            :prop="item.field"
            :label="item.label"
            :width="item.width"
            :formatter="fieldFormatter"
            show-overflow-tooltip />
          <el-table-column
            label="状态"
            width="100px">
            <template slot-scope="scope">
              <el-button
                type="text"
                @click="accountStatus(scope)">
                {{ scope.row.status == 1 ? '已启用' : '已禁用' }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="140">
            <template slot-scope="scope">
              <el-button type="primary-text" @click="accountEdit(scope.row,$event)">编辑</el-button>
              <el-button type="primary-text" @click="accountDelete(scope.row)">删除</el-button>
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

      <el-dialog
        v-if="addDialogVisible"
        :visible.sync="addDialogVisible"
        :title="dialogTitle"
        append-to-body
        width="25%">
        <el-form ref="addForm" :model="addForm" :rules="rules">
          <el-form-item label-width="80px" label="编码：" prop="carteNumber">
            <el-input v-model="addForm.carteNumber" />
          </el-form-item>
          <el-form-item label-width="80px" label="名称：" prop="carteName">
            <el-input v-model="addForm.carteName" />
          </el-form-item>
          <el-form-item v-if="accountLabel == 6" label-width="80px" label="规格：" prop="specification">
            <el-input v-model="addForm.specification" />
          </el-form-item>
          <el-form-item v-if="accountLabel == 6" label-width="80px" label="单位：" prop="unit">
            <el-input v-model="addForm.unit" />
          </el-form-item>
          <el-form-item v-if="[1,2].includes(accountLabel)" label-width="80px" label="备注：" prop="remark">
            <el-input v-model="addForm.remark" type="textarea" />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="saveAdjuvant('addForm')">保存</el-button>
          <el-button @click="closeAccountDialog()">关闭</el-button>
        </span>
      </el-dialog>

    </div>
  </div>
</template>

<script>
import {
  fmAdjuvantCarteAddAPI,
  fmAdjuvantCarteUpdateAPI,
  fmAdjuvantCarteDeleteAPI,
  fmAdjuvantCarteUpdateStatusAPI,
  fmAdjuvantQueryByAdjuvantIdAPI,
  fmAdjuvantDownloadExcelAPI,
  fmAdjuvantExcelImportAPI,
  fmAdjuvantExportExcelAPI
} from '@/api/fm/setting'

// import { exportElTable } from '@/utils'
import { isEmpty } from '@/utils/types'
import { downloadExcelWithResData } from '@/utils/index.js'
export default {
  name: 'AccountList',
  components: {

  },

  props: {
    accountDialogVisible: {
      type: Boolean,
      default: false
    },
    // 当前类型
    currentType: {
      type: [String, Number],
      default: 1
    },
    // 固定的辅助核算
    fixedList: {
      type: Array
    },
    // 自定义辅助核算
    adjuvantCustomList: {
      type: [Array]
    }
  },
  data() {
    return {
      loading: false, // 展示加载中效果

      action: {
        type: 'add'
      },

      // 辅助项设置
      /** 辅助项每行的信息 */
      listData: [],
      multipleSelection: [],

      addDialogVisible: false, // 添加辅助项的弹窗
      isEdit: false, // 弹窗内容是否为编辑

      currentParamsType: '', // 当前类别
      accountType: '', // 固定类别的 id
      accountLabel: 0, // 固定类别的 label

      // accountTypeOption: [
      //   { name: '客户', value: 1 },
      //   { name: '供应商', value: 2 },
      //   { name: '职员', value: 3 },
      //   { name: '项目', value: 4 },
      //   { name: '部门', value: 5 },
      //   { name: '存货', value: 6 }
      // ],

      customValue: '', // 选择的自定义辅助核算
      customOptions: [], // 自定义辅助核算下拉选项
      searchValue: '', // 查询值

      carteId: '', // 编辑项的id
      addForm: {
        carteNumber: '',
        carteName: '',
        specification: '',
        unit: '',
        remark: ''
      },
      rules: {
        carteNumber: [
          { required: true, message: '编码不能为空', trigger: 'blur' }
        ],
        carteName: [
          { required: true, message: '名称不能为空', trigger: 'blur' }
        ]
      },

      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 30, 40, 50, 100],
      total: 0
    }
  },
  computed: {

    /**
     * @description: 新增弹窗标题名
     */
    dialogTitle() {
      if (this.isEdit) return '编辑辅助核算'
      if ([1, 2, 3, 4, 5, 6].includes(this.accountLabel)) {
        return '新增' +
         { 1: '客户', 2: '供应商', 3: '职员', 4: '项目', 5: '部门', 6: '存货' }[this.accountLabel]
      }
      return ''
    },
    /**
     * @description: 列表字段
     */
    accountList() {
      if ([1, 2].includes(this.accountLabel)) {
        return [
          { label: '编码', field: 'carteNumber', width: '150px' },
          { label: '名称', field: 'carteName' },
          { label: '备注', field: 'remark' }
        ]
      } else if ([6].includes(this.accountLabel)) {
        return [
          { label: '编码', field: 'carteNumber', width: '150px' },
          { label: '名称', field: 'carteName' },
          { label: '规格', field: 'specification' },
          { label: '单位', field: 'unit' }
        ]
      } else {
        return [
          { label: '编码', field: 'carteNumber', width: '150px' },
          { label: '名称', field: 'carteName' }
        ]
      }
    }
  },
  watch: {
    // 监听传过来的类型
    currentType: {
      handler(val) {
        const index = this.fixedList.findIndex(el => el.adjuvantId == val)
        if (index != -1) {
          this.accountType = val
          this.customValue = ''
        } else {
          this.accountType = ''
          this.customValue = val
        }
        this.currentParamsType = val
      },
      immediate: true
    },
    // 监听传过来的自定义辅助项
    adjuvantCustomList: {
      handler(val) {
        if (val.length > 0) {
          this.customOptions = val
        }
      },
      immediate: true,
      deep: true
    },
    // 监听6个固定项
    accountType(val) {
      if (val) {
        this.customValue = ''
        this.currentParamsType = val
      }
    },
    // 监听自定义辅助项
    customValue: {
      handler(val) {
        if (val) {
          this.accountType = ''
          this.currentParamsType = val
        }
      },
      immediate: true
    },
    // 监听当前类别
    currentParamsType: {
      handler(val) {
        this.getAccountList()
        const fixedObj = this.fixedList.find(el => el.adjuvantId == val)
        if (!isEmpty(fixedObj)) {
          this.accountLabel = fixedObj.label
        } else {
          this.accountLabel = 7
        }
      },
      immediate: true
    }
  },

  created() {
    this.getAccountList()
  },
  methods: {
    /**
     * 辅助项列表
     */
    getAccountList() {
      this.loading = true
      const params = {
        page: this.currentPage,
        limit: this.pageSize,
        adjuvantId: this.currentParamsType
      }
      if (this.searchValue) {
        params.search = this.searchValue
        params.page = 1
      }
      fmAdjuvantQueryByAdjuvantIdAPI(params)
        .then(res => {
          // console.log('请求数据',res);
          this.loading = false
          this.listData = res.data.list
          this.total = res.data.totalRow
        })
        .catch(() => {
          this.loading = false
        })
    },

    /**
     * @description: 查询输入框值改变
     */
    searchInputChange() {
      if (!this.searchValue) {
        this.getAccountList()
      }
    },
    /**
     * @description: 查询事件
     */
    handlerSearch() {
      this.getAccountList()
    },
    /**
     * 更改当前页数
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.getAccountList()
    },
    /**
     * 更改每页展示数量
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.getAccountList()
    },

    /**
     * 导入
     */
    importAccount() {
      this.$wkImport.import('fmAssist', {
        typeName: '辅助项',
        historyShow: false,
        noImportProcess: true,
        ownerSelectShow: false,
        repeatHandleShow: false,
        importRequest: fmAdjuvantExcelImportAPI, // 导入请求
        templateRequest: fmAdjuvantDownloadExcelAPI, // 模板请求
        importParams: {
          label: this.accountLabel,
          adjuvantId: this.currentParamsType
        }, // 导入参数
        templateParams: { label: this.accountLabel } // 导入模板参数

        // importRequest: fmFinanceSubjectExcelImportAPI, // 导入请求
        // templateRequest: fmFinanceSubjectDownloadExcelAPI // 模板请求
      })
    },
    /**
     * 批量删除
     */
    batchDelete() {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.loading = true
        const ids = this.multipleSelection.map(item => item.carteId)
        fmAdjuvantCarteDeleteAPI(ids).then(res => {
          this.$message.success('操作成功')
          this.getAccountList()
          this.loading = false
        }).catch(() => {
          this.loading = false
        }).catch(() => {
        })
      })
    },
    /**
     * 导出
     */
    exportAccount() {
      // exportElTable('AccountList.xlsx', 'table')
      const params = {
        page: this.currentPage,
        limit: this.pageSize,
        adjuvantId: this.currentParamsType
      }
      if (this.searchValue) {
        params.search = this.searchValue
        params.page = 1
      }
      fmAdjuvantExportExcelAPI(params).then(res => {
        downloadExcelWithResData(res)
      })
        .catch(() => {})
    },
    /**
     * 批量启用
     */
    // batchUpdateStatus(status) {
    //   this.loading = true
    //   const ids = this.multipleSelection.map(item => item.subjectId)

    //   fmAdjuvantCarteUpdateStatusAPI({ ids, status }).then(res => {
    //     this.$message.success('操作成功')
    //     this.getAccountList()
    //     this.loading = false
    //   }).catch(() => {
    //     this.loading = false
    //   })
    // },

    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    /**
     * 辅助项列表格式化
     */
    fieldFormatter(row, column) {
      return row[column.property]
    },
    /**
     * 辅助项 编辑
     */
    accountEdit(row) {
      // console.log('编辑当前行', row)
      this.carteId = row.carteId

      const { carteNumber, carteName, specification, unit, remark } = row
      this.addForm = { carteNumber, carteName, specification, unit, remark }

      this.isEdit = true
      this.addDialogVisible = true
    },

    /**
     * 辅助项 删除
     */
    accountDelete(row) {
      this.$confirm('确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          const ids = [row.carteId]
          fmAdjuvantCarteDeleteAPI(ids)
            .then(res => {
              this.$message.success('操作成功')
              this.getAccountList()
            }).catch(() => {})
        })
        .catch(() => {
        })
    },

    /**
     * @description: 启用停用
     * @param {*} scope
     */
    accountStatus(scope) {
      this.$confirm(
        '您确定要' +
            (scope.row.status == 1 ? '停用' : '启用') +
            '该辅助项?',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          fmAdjuvantCarteUpdateStatusAPI({
            carteId: scope.row.carteId,
            status: scope.row.status == 1 ? 2 : 1
          })
            .then(res => {
              scope.row['status'] = scope.row['status'] == 1 ? 2 : 1
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

    /**
     * 辅助项添加
     */
    addAdjuvant() {
      this.resetForm()
      this.isEdit = false
      this.addDialogVisible = true
    },
    /**
     * 保存新增的分类
     */
    saveAdjuvant(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const request = this.isEdit ? fmAdjuvantCarteUpdateAPI : fmAdjuvantCarteAddAPI
          const params = { ...this.addForm, adjuvantId: this.currentParamsType }
          if (this.isEdit) {
            params.carteId = this.carteId
          }
          request(params).then(res => {
            // console.log('保存辅助核算', res)
            this.$message.success('保存成功')
            this.getAccountList()
          }).catch(() => {})

          this.resetForm()
          this.addDialogVisible = false
        } else {
          return false
        }
      })
    },
    /**
     * 关闭辅助项弹窗
     */
    closeAccountDialog() {
      this.isEdit = false
      this.addDialogVisible = false
      this.resetForm()
    },
    /**
     * 重置表单
     */
    resetForm() {
      this.addForm = {
        carteNumber: '',
        carteName: '',
        specification: '',
        unit: '',
        remark: ''
      }
      this.$refs.addForm?.resetFields()
    }

  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.main-header span {
  font-size: 20px;
}

.account-list {
  // flex: 1;
  display: flex;
  height: 90%;

  .content-list {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
}

.content-title {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

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

  .left-box {
    display: flex;
    align-items: center;

    .custom-select {
      margin-left: $--button-padding-vertical;
    }

    .searchInput {
      width: 200px;
      margin-left: $--button-padding-vertical;
    }
  }

  .buttons {
    float: right;
    width: auto;
  }

  .buttons ::v-deep .el-button {
    margin-left: $--button-padding-vertical;
  }
}

.account-table {
  flex: 1;
  padding-bottom: 24px;

  // height: 0;
  margin: 0 30px 10px;

  // height: 88%;
  overflow: hidden;

  .el-table {
    width: 100%;
    height: 92%;

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
