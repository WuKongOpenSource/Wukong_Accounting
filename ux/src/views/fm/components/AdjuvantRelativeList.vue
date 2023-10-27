<template>
  <div class="account-list">
    <el-dialog
      :visible.sync="accountDialogVisible"
      :before-close="cancel"
      :title="relativeTitle"
      class="account-dialog"
      append-to-body
      width="60%">

      <div class="content-list">
        <flexbox class="content-flex" justify="space-between">

          <div class="left-box">
            <el-input
              v-model="searchValue"
              size="mini"
              class="searchInput"
              placeholder="输入关键词"
              @keydown.native.enter.prevent="handlerSearch"
              @change="searchInputChange()">
              <el-button slot="append" @click="handlerSearch()">查询</el-button>
            </el-input>
          </div>

          <flexbox class="buttons">
            <el-button
              type="primary"
              class="rt"
              @click="addAdjuvant">新增</el-button>
            <el-button
              type="primary"
              class="rt"
              @click="batchDelete">删除</el-button>
            <el-button
              type="primary"
              class="rt"
              @click="importAccount"
            >导入</el-button>
            <el-button
              type="primary"
              class="rt"
              @click="exportAccount">导出</el-button>
              <!-- <el-button
              v-if="$auth('finance.subject.update')"
              type="primary"
              class="rt"
              @click="batchUpdateStatus(1)">批量启用</el-button> -->
          </flexbox>
        </flexbox>

        <div class="account-table">
          <el-table
            id="table"
            ref="table"
            v-loading="loading"
            :data="listData"
            height="92%"
            row-key="carteId"
            stripe
            default-expand-all
            border
            @selection-change="handleSelectionChange">
            <el-table-column
              type="selection"
              :selectable="canSelectRow"
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
                <i
                  class="wk wk-icon-modify icon-btn"
                  @click="accountEdit(scope.row,$event)" />
                <i
                  class="wk wk-icon-bin icon-btn"
                  @click="accountDelete(scope.row)" />
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

        <!-- 新增的弹窗 -->
        <el-dialog
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
            <el-form-item v-if="label == 6" label-width="80px" label="规格：" prop="specification">
              <el-input v-model="addForm.specification" />
            </el-form-item>
            <el-form-item v-if="label == 6" label-width="80px" label="单位：" prop="unit">
              <el-input v-model="addForm.unit" />
            </el-form-item>
            <el-form-item v-if="[1,2].includes(label)" label-width="80px" label="备注：" prop="remark">
              <el-input v-model="addForm.remark" type="textarea" />
            </el-form-item>
          </el-form>
          <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="saveAdjuvant('addForm')">保存</el-button>
            <el-button @click="closeAccountDialog()">关闭</el-button>
          </span>
        </el-dialog>

      </div>

      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirm()">确定</el-button>
        <el-button @click="cancel()">取消</el-button>
      </span>

    </el-dialog>
  </div>
</template>

<script>
import {
  fmAdjuvantCarteAddAPI, // 新增
  fmAdjuvantCarteUpdateAPI, // 编辑
  fmAdjuvantCarteDeleteAPI, // 删除
  fmAdjuvantCarteUpdateStatusAPI, // 状态更改
  fmAdjuvantQueryByAdjuvantIdAPI, // 列表查询
  fmAdjuvantDownloadExcelAPI, // 下载导入模板
  fmAdjuvantExcelImportAPI, // 导入
  fmAdjuvantExportExcelAPI // 导出
} from '@/api/fm/setting'
import { downloadExcelWithResData } from '@/utils'
// import { isEmpty } from '@/utils/types'

export default {
  name: 'AdjuvantRelativeList',
  components: {

  },

  props: {
    accountDialogVisible: {
      type: Boolean,
      default: false
    },
    /** 当前类型 id */
    currentType: {
      type: [String, Number],
      default: ''
    },
    /** 当前类型label */
    label: {
      type: [Number, String],
      default: () => ''
    }
  },
  data() {
    return {
      loading: false, // 展示加载中效果
      addDialogVisible: false, // 添加辅助项的弹窗
      isEdit: false, // 新增弹窗内容是否为编辑

      searchValue: '', // 查询值
      listData: [], // tab列表数据
      multipleSelection: [], // 勾选项列表数据

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
      pageSizes: [10, 20, 30, 40, 100, 500],
      total: 0
    }
  },
  computed: {
    /** 关联的弹窗名 */
    // eslint-disable-next-line vue/return-in-computed-property
    relativeTitle() {
      if (this.label == 7) return '关联自定义辅助核算'
      if ([1, 2, 3, 4, 5, 6].includes(this.label)) {
        return '关联' +
         { 1: '客户', 2: '供应商', 3: '职员', 4: '项目', 5: '部门', 6: '存货' }[this.label]
      }
    },
    /** 新增弹窗标题名 */
    // eslint-disable-next-line vue/return-in-computed-property
    dialogTitle() {
      if (this.isEdit) return '编辑辅助核算'
      if ([1, 2, 3, 4, 5, 6].includes(this.label)) {
        return '新增' +
         { 1: '客户', 2: '供应商', 3: '职员', 4: '项目', 5: '部门', 6: '存货' }[this.label]
      }
    },
    /** 列表字段 */
    accountList() {
      if ([1, 2].includes(this.label)) {
        return [
          { label: '编码', field: 'carteNumber', width: '150px' },
          { label: '名称', field: 'carteName' },
          { label: '备注', field: 'remark' }
        ]
      } else if ([6].includes(this.label)) {
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
    /** 监听传过来的类型 */
    currentType: {
      handler(val) {
        if (val) {
          this.getAccountList()
        }
      },
      immediate: true
    }
  },

  created() {
    // this.getAccountList()
  },
  methods: {
    /** diaolog 取消 */
    cancel() {
      this.$emit('close', false)
    },
    /** diaolog 确定 */
    confirm() {
      this.$emit('close', false)
      this.$emit('selectList', this.multipleSelection)
    },
    /** 禁用辅助核算不允许选中 */
    canSelectRow(data) {
      return data.status === 1
    },
    /**
     * 辅助项列表
     */
    getAccountList(searchVal) {
      this.loading = true
      const params = {
        page: this.currentPage,
        status: 1,
        limit: this.pageSize,
        adjuvantId: this.currentType
      }
      if (searchVal) {
        params.search = searchVal
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

    /** 查询输入框值改变 */
    searchInputChange() {
      if (!this.searchValue) {
        this.getAccountList()
      }
    },
    /** 查询事件 */
    handlerSearch() {
      this.getAccountList(this.searchValue)
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
          label: this.label,
          adjuvantId: this.currentType
        }, // 导入参数
        templateParams: { label: this.label } // 导入模板参数
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
      const params = {
        page: this.currentPage,
        status: 1,
        limit: this.pageSize,
        adjuvantId: this.currentType
      }
      if (this.searchValue) {
        params.page = 1
        params.search = this.searchValue
      }

      fmAdjuvantExportExcelAPI(params)
        .then(res => {
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

    /** 列表勾选事件 */
    handleSelectionChange(val) {
      // console.log('勾选', val)
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
      this.isEdit = true
      this.addDialogVisible = true
      this.carteId = row.carteId
      const { carteNumber, carteName, specification, unit, remark } = row
      this.addForm = { carteNumber, carteName, specification, unit, remark }
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

    /** 启用停用 */
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
              if (this.multipleSelection.find(o => o === scope.row)) {
                this.$refs.table.toggleRowSelection(scope.row, false)
              }
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
          const params = { ...this.addForm, adjuvantId: this.currentType }
          if (this.isEdit) {
            params.carteId = this.carteId
          }
          request(params).then(res => {
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
      // this.$refs.addForm.resetFields()
    }

  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
  .account-list {
    display: flex;
    height: 90%;
  }

  .account-dialog {
    ::v-deep.el-dialog {
      .el-dialog__body {
        height: 600px;
      }
    }
  }

  .content-list {
    display: flex;
    flex-direction: column;
    height: 100%;
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
      // display: flex;
      // align-items: center;
      .searchInput {
        width: 200px;
      }
    }

    .buttons {
      float: right;
      width: auto;
    }

    .buttons ::v-deep .el-button {
      margin-right: 10px;
    }
  }

  .account-table {
    flex: 1;
    padding-bottom: 24px;
    margin: 0 30px 10px;

    // height: 88%;
    overflow: hidden;

    // height: 0;
    border: 1px solid #e6e6e6;

    .el-table {
      width: 100%;

      // height: 92%;
      ::v-deep td {
        border-left: 1px solid #ebeef5;
      }

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
