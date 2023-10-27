<template>
  <div v-loading="loading" class="app-container">
    <div v-if="showClassify" class="content-header">
      <span @click="goBack">辅助核算</span>
    </div>
    <div v-if="showClassify" class="classify-box">
      <div
        v-for="(item,index) in classifyList"
        :key="index"
        class="classify-item"
        @click="targetList(item)">
        <div class="moreBtn" @click.stop>
          <el-dropdown
            v-if="item.adjuvantType != 1"
            trigger="click"
            class="receive-drop">
            <i class="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native.stop="editClassify(item)">编辑</el-dropdown-item>
              <el-dropdown-item @click.native.stop="deleteClassify(item)">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>

        <div class="classify-item-content">
          <div class="icon">
            <i :class="item.icon" class="wk" />
          </div>
          <div class="name">{{ item.adjuvantName }}</div>
        </div>

      </div>
      <div
        class="classify-item lastRow"
        @click="openAddDialog()">
        <div class="classify-item-content">
          <div class="name">+ 新增分类</div>
        </div>
      </div>
    </div>

    <el-dialog
      :visible.sync="addDialogVisible"
      :title="classifyTitle"
      width="400px">
      <el-form ref="addForm" :model="addForm" :rules="rules" label-position="top">
        <el-form-item label-width="50px" label="名称" prop="name" class="wk-form-item">
          <el-input v-model="addForm.name" autocomplete="off" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveClassify('addForm')">保存</el-button>
        <el-button @click="closeAddDialog()">关闭</el-button>
      </span>
    </el-dialog>

    <account-list-dialog
      :account-dialog-visible="accountDialogVisible"
      :current-type="currentType"
      :fixed-list="fixedList"
      :adjuvant-custom-list="adjuvantCustomList"
    />

  </div>
</template>

<script>
import {
  fmAdjuvantListAPI,
  fmAdjuvantCustomListAPI,
  fmAdjuvantAddAPI,
  fmAdjuvantUpdateAPI,
  fmAdjuvantDeleteAPI
} from '@/api/fm/setting'
import accountListDialog from './components/AccountList'

export default {
  name: 'AssistAccounting',
  components: {
    accountListDialog
  },

  data() {
    return {
      loading: false, // 展示加载中效果
      showClassify: true, // 分类是否展示
      accountDialogVisible: false, // 辅助核算列表显示与隐藏
      addDialogVisible: false, // 新增分类的弹窗

      isEdit: false,
      addForm: {
        name: '',
        adjuvantId: ''
      },
      rules: {
        name: [
          { required: true, message: '名称不能为空', trigger: 'blur' }
        ]
      },
      classifyList: [], // 所有辅助核算目录列表
      fixedList: [], // 固定的6个辅助核算
      adjuvantCustomList: [], // 自定义的辅助核算列表
      currentType: null

    }
  },
  computed: {
    /** 弹窗标题 */
    classifyTitle() {
      if (this.isEdit) return '编辑分类'
      return '新增分类'
    },
    /** 判断 是否最后一行 */
    isLastRow() {
      return function(index) {
        return index >= (this.classifyList.length - (this.classifyList.length % 4))
      }
    }
  },
  watch: {
    addDialogVisible(val) {
      if (!val) {
        this.resetAddForm()
      }
    }
  },
  created() {
    this.getAdjuvantList()
  },
  methods: {
    /**
     * 查询辅助核算目录列表
     */
    getAdjuvantList() {
      this.loading = true
      // 所有的辅助核算
      fmAdjuvantListAPI().then(res => {
        this.fixedList = res.data.filter(el => el.label < 7)
        this.classifyList = res.data

        this.classifyList.forEach(item => {
          item.icon = {
            1: 'wk-customer',
            2: 'wk-icon-supplier',
            3: 'wk-icon-Member-management',
            4: 'wk-project',
            5: 'wk-department',
            6: 'wk-icon-stock',
            7: 'wk-icon-business-accounting'
          }[item.label]
        })

        this.loading = false
      }).catch(() => {
        this.loading = false
      })
      // 自定义的辅助核算
      fmAdjuvantCustomListAPI().then(res => {
        this.adjuvantCustomList = res.data
      }).catch(() => {})
    },
    /**
     * 跳转辅助核算列表
     */
    targetList(item) {
      this.showClassify = false
      this.accountDialogVisible = true
      this.currentType = item.adjuvantId
    },
    /**
     * 返回辅助核算目录
     */
    goBack() {
      this.showClassify = true
      this.accountDialogVisible = false
    },
    /**
     * 打开新增分类弹窗
     */
    openAddDialog() {
      this.isEdit = false
      this.addDialogVisible = true
    },
    /**
     * 关闭新增分类弹窗
     */
    closeAddDialog() {
      this.addDialogVisible = false
      this.resetAddForm()
      this.$refs.addForm.resetFields()
    },
    /**
     * 重置表单
     */
    resetAddForm() {
      this.isEdit = false
      this.addForm = {
        name: '',
        adjuvantId: ''
      }
    },
    /**
     * 保存新增的分类
     */
    saveClassify(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const request = this.isEdit ? fmAdjuvantUpdateAPI : fmAdjuvantAddAPI
          const params = { adjuvantName: this.addForm.name }
          if (this.isEdit) {
            params.adjuvantId = this.addForm.adjuvantId
          }
          request(params).then(res => {
            console.log('保存辅助核算', res)
            this.getAdjuvantList()
          }).catch(() => {})

          this.addDialogVisible = false
          this.$refs[formName].resetFields()
        } else {
          return false
        }
      })
    },
    /**
     * 编辑分类
     */
    editClassify(item) {
      this.isEdit = true
      this.addDialogVisible = true

      this.addForm.name = item.adjuvantName
      this.addForm.adjuvantId = item.adjuvantId
    },
    /**
     * 删除分类
     */
    deleteClassify(item) {
      this.$confirm('删除后不可恢复！您确定要删除该辅助核算吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        fmAdjuvantDeleteAPI({ adjuvantId: item.adjuvantId })
          .then(res => {
            // console.log('删除辅助核算', res)
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.getAdjuvantList()
          }).catch(() => {})
      }).catch(() => {
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "../styles/index.scss";
@import "@/styles/wk-form.scss";

.content-title {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.content-title > span {
  display: inline-block;
  height: 36px;
  margin-left: 20px;
  line-height: 36px;
  cursor: pointer;
}

.classify-box {
  box-sizing: border-box;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-start;
  width: 90%;

  // padding: 10px;
  margin: 10px;
  border: 1px solid #dfe1e6;
  border-top: 4px solid #0052cc;
  border-radius: 4px;
  box-shadow: $--box-shadow-bottom-light;

  .classify-item::after {
    padding-top: 90%;
    content: "";
  }

  .classify-item:hover {
    box-shadow: $--box-shadow-dark;

    .operate-box {
      display: flex;
    }

    .name {
      text-decoration: underline;
    }

    .moreBtn {
      display: block;
    }
  }

  .classify-item {
    // height: 165px;
    position: relative;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: center;
    width: 20%;
    color: #0052cc;
    text-align: center;
    cursor: pointer;
    border: 1px solid #dfe1e6;
    border-top: none;
    border-left: none;

    .moreBtn {
      position: absolute;
      top: 10px;
      right: 15px;
      display: none;
      padding: 10px;
    }

    .operate-box {
      position: absolute;
      top: 5px;
      right: 10px;

      // display: flex;
      display: none;
      align-items: center;

      .el-icon-edit {
        margin-right: 10px;
      }
    }

    .classify-item-content {
      position: absolute;
      width: 100%;

      .icon {
        display: inline-block;
        margin-bottom: 10px;

        .wk {
          font-size: 40px;
        }
      }

      .name {
        position: absolute;
        box-sizing: border-box;
        width: 100%;
        padding: 0 15px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .classify-item.right {
    border-right: none;
  }

  .classify-item.lastRow {
    border-bottom: none;
  }
}
</style>
