<template>
  <flexbox
    v-loading="loading"
    :gutter="0"
    class="app-container main"
    direction="column"
    align="stretch">
    <xr-header
      label="账套管理">
      <template slot="otherLabel">
        <i
          class="wk wk-icon-fill-help wk-help-tips"
          data-type="43"
          data-id="338" />
      </template>
    </xr-header>
    <flexbox-item class="container">
      <flexbox
        align="flex-start"
        justify="flex-start"
        wrap="wrap"
        class="content">
        <div
          v-for="(item,index) in accountList"
          :key="index"
          class="content-item-wrap">
          <flexbox
            :gutter="0"
            align="center"
            justify="flex-start"
            direction="column"
            class="content-item">
            <flexbox class="content-item-top">
              <flexbox-item>
                {{ item.companyName }}
              </flexbox-item>
              <el-button
                type="primary-text"
                @click="handleToEditAccount(item)">编辑</el-button>
            </flexbox>
            <flexbox-item class="content-item-center">
              <p>联系人：{{ item.contacts }}</p>
              <p>联系电话：{{ item.mobile }}</p>
            </flexbox-item>
            <flexbox
              align="center"
              justify="flex-start"
              class="content-item-bottom">
              <flexbox-item>
                <span>会计期间：</span><span>{{ formatTime(item.startTime) }}</span>
              </flexbox-item>
              <el-button
                type="primary-text"
                @click="handleToEditAuth(item)">授权</el-button>
              <el-button
                type="primary"
                @click="openTally(item)">记账</el-button>
            </flexbox>
          </flexbox>
        </div>
        <div class="content-item-wrap">
          <div class="content-item">
            <div class="create-box">
              <div
                class="create-box-icon"
                @click="handleToCreateAccount">
                <i class="el-icon-plus" />
              </div>
              <p class="create-box-text">
                新建账套
              </p>
            </div>
          </div>
        </div>
      </flexbox>
    </flexbox-item>

    <!-- 新建账套 -->
    <el-dialog
      v-if="dialogVisible"
      v-loading="loading"
      :lock-scroll="true"
      :visible="dialogVisible"
      :before-close="addaccountClose"
      :title="accountDialogTitle"
      :close-on-click-modal="false"
      width="800px">
      <div>
        <create-sections title="基本信息">
          <el-form
            ref="accountForm"
            :rules="basicInfFormRules"
            :model="accountForm"
            label-position="top">
            <el-row :gutter="40">
              <el-col
                v-for="(item, index) in accountFieldList"
                :key="index"
                :span="12">
                <el-form-item
                  :prop="item.prop"
                  :label-width="formLabelWidth"
                  :label="item.title">
                  <el-input
                    v-model="accountForm[item.prop]"
                    autocomplete="off" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </create-sections>

        <create-sections title="联系方式">
          <el-form
            ref="contactForm"
            :model="contactForm"
            label-position="top">
            <el-row :gutter="40">
              <el-col
                v-for="(item, index) in contactFieldList"
                :key="index"
                :span="12">
                <el-form-item
                  :prop="item.prop"
                  :label-width="formLabelWidth"
                  :label="item.title">
                  <el-input
                    v-model="contactForm[item.prop]"
                    autocomplete="off" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </create-sections>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSaveAccount">确定</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </div>
    </el-dialog>

    <!-- 授权 -->
    <el-dialog
      v-if="accountItem"
      :visible.sync="authDialogVisible"
      :close-on-click-modal="false"
      title="授权"
      width="800px">
      <!-- 主体 -->
      <div>
        <!-- title -->
        <flexbox class="power-title">
          <flexbox-item>账套名:{{ accountItem.companyName }}</flexbox-item>
          <el-button
            type="primary"
            @click="relateEmployeeShow = true">添加员工</el-button>
        </flexbox>
        <!-- table -->
        <el-table
          v-loading="loading"
          :data="accountUserList"
          :class="WKConfig.tableStyle.class"
          :stripe="WKConfig.tableStyle.stripe"
          :height="250"
          style="width: 100%;">
          <el-table-column
            type="index"
            width="50" />
          <el-table-column
            prop="realname"
            label="姓名"
            width="220" />
          <el-table-column
            v-for="(item,index) in financeRoleList"
            :key="index"
            :prop="item.prop"
            :label="item.label">
            <template slot-scope="{row}">
              <el-checkbox
                v-model="row[item.prop]"
                :disabled="row.isFounder===1" />
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="{row, $index}">
              <el-button
                :disabled="row.isFounder===1"
                type="text"
                @click.native.prevent="deleteRow(row, $index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSaveAuth(false)">确定</el-button>
        <el-button @click="authDialogVisible = false">取消</el-button>
      </div>
    </el-dialog>

    <!-- 选择员工 -->
    <wk-dep-user-dialog
      v-if="relateEmployeeShow"
      :visible.sync="relateEmployeeShow"
      :props="{
        showDisableUser: false,
        showDept: false,
      }"
      @change="employeesSave" />

    <!-- 初始化账套 -->
    <account-step-dialog
      v-if="showAccount"
      :visible.sync="showAccount"
      :company-info="companyInfo" />
  </flexbox>
</template>

<script>
import {
  queryPageList,
  getUserByaccountId,
  addAccount,
  updateAccount,
  saveAccountAuth
} from '@/api/admin/accountBook'
import { getFinanceRoleByTypeAPI } from '@/api/admin/accountBook'

import WkDepUserDialog from '@/components/NewCom/WkUserDialogSelect/Dialog'
import CreateSections from '@/components/CreateSections'
import AccountStepDialog from './components/AccountStepDialog'
import XrHeader from '@/components/XrHeader'

import { mapGetters } from 'vuex'

export default {
  name: 'AccountBook',
  components: {
    CreateSections,
    WkDepUserDialog,
    AccountStepDialog,
    XrHeader
  },
  data() {
    return {
      loading: false,
      accountList: [], // 账套列表
      accountItem: null, // 当前选中的账套
      accountUserList: [], // 账套下的员工列表
      financeRoleList: [], // 财务角色列表

      accountFieldList: [
        { title: '公司编码', prop: 'companyCode' },
        { title: '公司名称', prop: 'companyName' },
        { title: '公司简介', prop: 'companyProfile' },
        { title: '所在行业', prop: 'industry' },
        { title: '所在地', prop: 'location' },
        { title: '法人代表', prop: 'legalRepresentative' },
        { title: '身份证号', prop: 'idNum' },
        { title: '营业执照号', prop: 'businessLicenseNum' },
        { title: '组织机构代码', prop: 'organizationCode' },
        { title: '备注', prop: 'remark' }
      ],
      contactFieldList: [
        { title: '联系人', prop: 'contacts' },
        { title: '办公电话', prop: 'officeTelephone' },
        { title: '手机号', prop: 'mobile' },
        { title: '传真号码', prop: 'faxNum' },
        { title: 'QQ', prop: 'qqNum' },
        { title: 'Email', prop: 'email' },
        { title: '其它', prop: 'other' },
        { title: '详细地址', prop: 'address' }
      ],
      basicInfFormRules: {
        companyCode: [{ required: true, message: '请输入公司编码', trigger: 'blur' }],
        companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
      },
      accountForm: {}, // 基本资料
      contactForm: {}, // 联系方式

      dialogVisible: false,
      formLabelWidth: '120px',
      accountDialogTitle: '新建账套',
      authDialogVisible: false,
      relateEmployeeShow: false,
      // 点击记账
      showAccount: false,
      companyInfo: {
        companyName: '',
        accountId: ''
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  watch: {
    accountUserList: {
      handler(n) {
        n.forEach(oneItem => {
          this.financeRoleList.forEach(item => {
            if (!oneItem.hasOwnProperty(item.prop)) {
              this.$set(oneItem, item.prop, false)
            }
          })
        })
      },
      deep: true,
      immediate: true
    }
  },
  created() {
    this.getData()
  },
  methods: {
    /**
     * 查询全部账套列表
     */
    getData() {
      this.loading = true
      queryPageList().then(res => {
        this.loading = false
        this.accountList = res.data
      })
    },

    formatTime(time) {
      if (!time) return ''
      return this.$moment(time).format('YYYY年第MM期')
    },

    /**
     * 点击记账
     * @param item
     */
    async openTally(item) {
      const res = await getUserByaccountId({ accountId: item.accountId })
      console.log('用户信息', res, item.accountId, this.userInfo.userId)
      const findItem = res.data.userList.find(item => {
        return item.userId === this.userInfo.userId
      })
      if (findItem) { // 证明本用户有该账套的权限
        if (item.status == 1) {
          this.$message.success('跳转至现有记账')
          this.$router.push({
            path: '/fm/workbench',
            query: {
              id: item.accountId
            }
          })
        } else {
          this.companyInfo.companyName = item.companyName
          this.companyInfo.accountId = item.accountId
          this.showAccount = true
        }
      } else {
        this.$message.error('您没有该账套的权限')
      }
    },

    /**
     * 根据账套ID获取账套下的员工
     * @param accountId
     */
    getUserListByAccount(accountId) {
      this.loading = true
      getUserByaccountId({
        accountId
      }).then(res => {
        const list = (res.data || {}).userList || []
        // 获取财务的超级管理员，把超管放到第一个
        const findIndex = list.findIndex(item => {
          return item.hasOwnProperty('isFounder') && Number(item.isFounder) === 1
        })
        if (findIndex !== -1 && findIndex !== 0) {
          list.unshift(list[findIndex])
          list.splice(findIndex + 1, 1)
        }
        this.accountUserList = list
        this.loading = false
      })
    },

    /**
     * 获取财务角色列表
     */
    getRoleList() {
      getFinanceRoleByTypeAPI({
        type: 4
      }).then(res => {
        this.financeRoleList = (res.data || []).map(item => {
          return {
            prop: String(item.roleId),
            label: item.roleName
          }
        })
      })
    },

    /**
     * 新建账套
     */
    handleToCreateAccount() {
      this.accountDialogTitle = '新建账套'
      this.accountItem = null
      this.dialogVisible = true
      this.accountFieldList.forEach(item => {
        this.$set(this.accountForm, item.prop, '')
      })
      this.contactFieldList.forEach(item => {
        this.$set(this.contactForm, item.prop, '')
      })
    },

    /**
     * 编辑账套
     * @param accountItem
     */
    handleToEditAccount(accountItem) {
      this.accountItem = accountItem
      this.accountDialogTitle = '编辑账套'
      this.accountFieldList.forEach(field => {
        this.$set(this.accountForm, field.prop, accountItem[field.prop])
      })
      this.contactFieldList.forEach(field => {
        this.$set(this.contactForm, field.prop, accountItem[field.prop])
      })
      this.dialogVisible = true
    },

    /**
     * 保存账套信息
     */
    handleSaveAccount() {
      this.loading = true
      this.$refs.accountForm.validate((valid) => {
        if (valid) {
          const request = this.accountItem ? updateAccount : addAccount
          request({
            ...(this.accountItem || {}),
            ...this.accountForm,
            ...this.contactForm
          }).then(res => {
            setTimeout(() => {
              this.dialogVisible = false
              this.loading = false
              this.getData()
            }, 2000)
          }).catch(() => {
            this.loading = false
            this.dialogVisible = false
          })
        } else {
          this.loading = false
          return false
        }
      })
    },

    /**
     * 打开授权弹窗
     * @param accountItem
     */
    handleToEditAuth(accountItem) {
      this.accountItem = accountItem
      this.getUserListByAccount(accountItem.accountId)
      this.getRoleList()
      this.authDialogVisible = true
    },
    /**
     * 关闭添加账套
     */
    addaccountClose() {
      this.$refs.accountForm.clearValidate()
      this.dialogVisible = false
    },
    /**
     * 删除账套下的员工
     * @param row
     */
    deleteRow(row, index) {
      this.accountUserList.splice(index, 1)
    },

    /**
     * 保存财务角色员工
     * @param value 是否禁止关闭授权弹窗
     */
    handleSaveAuth(value = false) {
      const list = []
      this.accountUserList.forEach(userItem => {
        if (userItem.hasOwnProperty('isFounder') && userItem.isFounder === 1) return
        const roleIdList = []
        this.financeRoleList.forEach(roleItem => {
          if (userItem[roleItem.prop]) {
            roleIdList.push(roleItem.prop)
          }
        })
        list.push({
          roleIdList,
          userId: userItem.userId
        })
      })

      saveAccountAuth({
        accountId: this.accountItem.accountId,
        userList: list
      }).then(res => {
        if (value) {
          this.authDialogVisible = true
          this.getUserListByAccount(this.accountItem.accountId)
        } else {
          this.authDialogVisible = false
          this.$message.success('保存成功')
        }
      })
    },

    /**
     * 关联员工确定
     */
    employeesSave(_, deptIds, userList) {
      this.addUserToList(userList, true)
      this.relateEmployeeShow = false
    },

    /**
     * 添加员工
     * @param list
     * @param value
     */
    addUserToList(list = []) {
      list.forEach(user => {
        const findRes = this.accountUserList.find(o => o.userId === user.userId)
        if (!findRes) {
          const authObj = {}
          this.financeRoleList.forEach(k => {
            authObj[k.prop] = false
          })
          this.accountUserList.push({
            ...user,
            ...authObj
          })
        }
      })

      this.relateEmployeeShow = false
    }
  }
}
</script>

<style scoped lang='scss'>
@import "../styles/index.scss";

::v-deep .el-table__body td:last-child {
  border-right-color: #ebeef5;
}

::v-deep .el-form-item {
  padding-top: 12px;
  margin-bottom: 0 !important;
}

::v-deep .el-form--label-top .el-form-item__label {
  padding: 0 !important;
  line-height: 25px;
}

::v-deep .el-dialog__body {
  max-height: 60vh !important;
  overflow-y: auto !important;
}

.app-container {
  padding: 15px 40px 0;

  .container {
    width: 100%;
    margin-top: 16px;
    overflow-y: auto;

    .content {
      .content-item-wrap {
        padding-right: 8px;
      }

      .content-item {
        width: 330px;
        height: 174px;
        padding: 0 8px 8px;
        margin-bottom: 8px;
        background-color: $--background-color-base;
        border-radius: $--border-radius-base;

        .content-item-top {
          width: 100%;
          height: 40px;
          overflow: hidden;
          font-weight: bold;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .content-item-center {
          width: 100%;
          padding: 16px;
          line-height: 1.8;
          background-color: white;
          border-radius: $--border-radius-base;
          box-shadow: $--box-shadow-bottom;
        }

        .content-item-bottom {
          width: 100%;
          margin-top: 8px;
          margin-bottom: 4px;
        }

        .create-box {
          width: 100%;
          padding-top: 20px;
          text-align: center;
          cursor: pointer;

          .create-box-icon {
            width: 88px;
            height: 88px;
            margin: 0 auto;
            font-size: 50px;
            color: #90938b;
            border: 1px dashed #909399;
            border-radius: 50%;

            .el-icon-plus {
              font-weight: bold;
              line-height: 88px;
            }
          }

          .create-box-text {
            width: 100%;
            margin-top: 20px;
          }
        }
      }
    }
  }

  .power-title {
    padding-bottom: 20px;
  }
}

</style>
