<template>
  <el-dialog
    class="expiration-reset-dialog"
    :title="showText.title"
    :visible="true"
    :append-to-body="true"
    width="500px"
    custom-class="no-padding-dialog"
    :close-on-click-modal="false"
    @close="closeDialog">
    <div
      v-if="type === 'reset'"
      class="dialog-body">
      <p class="dialog-title-tip">{{ showText.tip }}</p>

      <el-form
        ref="resetPasswordForm"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-position="left"
        label-width="80px">
        <el-form-item
          v-for="(item, index) in formList"
          :key="index"
          :label="item.label"
          :prop="item.prop">
          <el-input
            v-model="resetPasswordForm[item.prop]"
            :maxlength="50" />
          <p
            v-if="item.prop == 'newPassword'"
            class="dialog-title-tip form-tip">{{ passwordRulesText }}</p>
        </el-form-item>
      </el-form>
    </div>

    <div
      v-else
      class="dialog-body forget">
      <el-form
        ref="ruleForm"
        :model="ruleForm"
        :rules="rules"
        label-width="100px"
        label-position="top"
        @submit.native.prevent>
        <div class="form-title">重置您的帐户密码</div>
        <el-form-item label="" prop="phone">
          <el-input
            v-if="ruleResult.phone.edit"
            v-model.trim="ruleForm.phone"
            placeholder="请输入手机号"
            @keyup.enter.native="saveClick" />
          <show-item v-else :content="ruleForm.phone" @click.native="editClick('phone')" />
        </el-form-item>
        <el-form-item v-if="smscodeShow" label="" prop="smscode">
          <el-input v-model.trim="ruleForm.smscode">
            <verify-button
              slot="suffix"
              ref="verifyButton"
              :disabled="!ruleResult.phone"
              @success="verifySuccess"
            />
          </el-input>
        </el-form-item>
        <multiple-company-select
          v-if="companyList.length > 0"
          v-model="companyId"
          :list="companyList"
        />
        <el-form-item v-if="passwordShow" label="" prop="password">
          <el-input
            v-model.trim="ruleForm.password"
            :type="passwordType"
            placeholder="请输入密码">
            <i
              slot="suffix"
              :style="{ color: passwordType === '' ? '#243858':'#C1C7D0' }"
              class="wk wk-icon-eye-solid"
              @click="passwordType = passwordType === '' ? 'password':''" />
          </el-input>
        </el-form-item>
      </el-form>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button
        v-if="type == 'reset'"
        type="primary-text"
        @click="handleClick('forget')">忘记密码？</el-button>

      <el-button
        v-if="type == 'forget'"
        type="primary-text"
        @click="handleClick('reset')">返回</el-button>
      <el-button
        type="primary"
        @click="saveClick">{{ type == 'reset' ? '重置' : loginBtnName }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {
  enterpriseSecurityConfigQueryAPI
} from '@/api/admin/enterpriseSecuritySetting'
import {
  adminUsersResetPasswordAPI
} from '@/api/user/personCenter'
import {
  sendSmsAPI,
  forgetPwdAPI,
  resetPwdAPI,
  verfySmsAPI
} from '@/api/login'

import ShowItem from '@/views/login/component/ShowItem.vue'
import MultipleCompanySelect from '@/views/login/component/MultipleCompanySelect'
import VerifyButton from '@/components/Verify/Button'

import LoginMixin from '@/views/login/component/LoginMixin'
import { isEmpty } from '@/utils/types'
import { debounce } from 'throttle-debounce'
import { removeAuth } from '@/utils/auth'
import { mapGetters } from 'vuex'
import Lockr from 'lockr'
import { LOCAL_CLEAR_PAGE_TIME, LOCAL_FREE_TIME } from '@/utils/constants.js'

export default {
  name: 'PasswordExpirationReset', // 密码过期重置
  components: {
    ShowItem,
    MultipleCompanySelect,
    VerifyButton
  },
  mixins: [LoginMixin],
  props: {
    resetRule: Object
  },
  data() {
    var validateSmscode = (rule, value, callback) => {
      this.smscodePass || this.loading ? callback() : callback(new Error('短信验证码错误'))
    }

    var validatePassword = (rule, value, callback) => {
      if (this.passwordShow) {
        value.length > 0 ? callback() : callback(new Error('请输入密码'))
      } else {
        callback()
      }
    }
    return {
      resetPasswordForm: {},
      resetPasswordRules: {},
      formList: [{
        label: '原密码',
        prop: 'oldPassword'
      }, {
        label: '新密码',
        prop: 'newPassword'
      }, {
        label: '确认密码',
        prop: 'confirmPassword'
      }],
      passwordRulesText: '',

      type: 'reset', // reset: 重置密码，forget: 忘记密码
      logOutOfLogin: false, // 是否需要退出登录

      // 忘记密码
      loading: false,
      ruleForm: {
        phone: '',
        smscode: '',
        password: ''
      },
      ruleResult: {
        phone: {
          validate: false,
          edit: true
        },
        smscode: {
          validate: false,
          edit: false
        },
        password: {
          validate: false,
          edit: false
        }
      },
      rules: {
        phone: [
          { required: true, message: '手机号不能为空', trigger: 'change' }
        ],
        smscode: [
          { validator: validateSmscode, trigger: 'blur' }
        ],
        password: [
          { validator: validatePassword, trigger: 'blur' }
        ]
      },

      smscodePass: false,
      companyList: [],
      companyId: '',
      passwordType: 'password'
    }
  },

  computed: {
    ...mapGetters(['userInfo']),
    showText() {
      if (!isEmpty(this.resetRule)) {
        // isExpire: 是否过期，ruleChange: 规则是否变更
        const { isExpire, ruleChange } = this.resetRule
        if (isExpire) {
          return {
            title: '密码到期重置',
            tip: '您的密码已到期，为了不影响您使用系统，请重置密码（如忘记密码，请联系系统管理员或点击下方忘记密码重置）'
          }
        } else if (ruleChange) {
          return {
            title: '密码规则变更后重置密码',
            tip: '系统登录密码规则已变更，为了不影响您使用系统，请修改为符合要求的密码。（如忘记密码，请联系系统管理员或点击下方忘记密码重置）'
          }
        }
      }
      return {}
    },
    smscodeShow() {
      return !this.ruleResult.phone.edit && this.ruleResult.phone.validate && !this.passwordShow
    },
    loginBtnName() {
      if (this.smscodeShow) {
        return '验证'
      }
      return this.ruleResult.phone.edit ? '继续' : '重置密码'
    },
    passwordShow() {
      return !!this.companyId // 有公司id，就到修改密码
    }
  },
  created() {
    this.getPasswordSetting()
    this.debouncedHandleLogin = debounce(300, this.handleLogin)
  },
  beforeDestroy() {
    this.clearTimer()
  },
  methods: {
    /**
     * @description: 获取重置密码设置
     * @return {*}
     */
    getPasswordSetting() {
      enterpriseSecurityConfigQueryAPI({ type: 1 })
        .then(res => {
          /**
           * pwdCharRequire
           * 0.不限
           * 1.必须包含字母+数字组合
           * 2.必须包含大写字母+小写字母+数字组合
           * 3.必须包含字母+特殊字符+数字组合
           * 4.必须包含大写字母+小写字母+特殊字符+数字组合
           */
          const { minimumPwdLen, pwdCharRequire, enforcePwdHistory, changePwdRule } = res.data
          this.logOutOfLogin = changePwdRule == 1

          const passwordRegexObj = {
            0: new RegExp(`^.{${minimumPwdLen},16}$`), // 不限
            1: new RegExp(`^(?=.*[A-Za-z])(?=.*\\d).{${minimumPwdLen},16}$`), // 必须包含字母+数字组合
            2: new RegExp(`^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{${minimumPwdLen},16}$`), // 必须包含大写字母+小写字母+数字组合
            3: new RegExp(`^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{${minimumPwdLen},16}$`), // 必须包含字母+特殊字符+数字组合
            4: new RegExp(`^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).{${minimumPwdLen},16}$`) // 必须包含大写字母+小写字母+特殊字符+数字组合
          }

          const passwordRulesTextObj = {
            0: '不限规则',
            1: '必须包含字母、数字',
            2: '必须包含大写字母、小写字母和数字组成',
            3: '必须包含字母、特殊字符和数字',
            4: '必须包含大写字母、小写字母、特殊字符和数字'
          }

          this.resetPasswordRules = {
            oldPassword: [
              { required: true, message: '请输入原密码', trigger: ['blur', 'change'] }
            ],
            newPassword: [
              { required: true, message: '请输入新密码', trigger: ['blur', 'change'] },
              {
                pattern: passwordRegexObj[pwdCharRequire],
                message: `密码由${minimumPwdLen}-16位${pwdCharRequire == 0 ? '任意字符组成' : passwordRulesTextObj[pwdCharRequire]}`
              }
            ],
            confirmPassword: [
              { required: true, message: '请再次输入新密码', trigger: ['blur', 'change'] }
            ]
          }

          this.passwordRulesText = `密码规则：${minimumPwdLen}-16位，${passwordRulesTextObj[pwdCharRequire]}。${enforcePwdHistory ? `不可与最近${enforcePwdHistory}个密码重复。` : ''}`
          this.$nextTick(() => {
            this.$refs.resetPasswordForm.clearValidate()
          })
        })
    },

    handleClick(type) {
      if (type == 'forget') {
        this.type = 'forget'
        this.$nextTick(() => {
          this.$refs.ruleForm.clearValidate()
        })
      } else if (type == 'reset') {
        this.type = 'reset'
        this.$nextTick(() => {
          this.$refs.resetPasswordForm.clearValidate()
        })
      }
    },

    /**
     * @description: 清理验证按钮定时器
     * @return {*}
     */
    clearTimer() {
      this.$refs.verifyButton && this.$refs.verifyButton.resetTimer()
    },

    /**
     * @description: 获取公司id
     * @return {*}
     */
    getCompany() {
      forgetPwdAPI({
        phone: this.ruleForm.phone,
        smscode: this.ruleForm.smscode
      }).then(res => {
        const companyList = res.data || []
        if (companyList.length > 1) {
          this.companyList = companyList
        }

        if (companyList.length > 0) {
          this.companyId = companyList[0].companyId
        }
      }).catch(() => {})
    },

    /**
     * 编辑
     */
    editClick(prop) {
      this.ruleResult[prop].edit = !this.ruleResult[prop].edit
      this.ruleResult.smscode.validate = false
      this.companyList = []
      this.companyId = ''
      this.ruleResult.smscode = ''
      this.ruleResult.password = ''

      this.smscodePass = false

      this.$refs.ruleForm.clearValidate()
    },

    /**
     * 获取验证码
     */
    verifySuccess(params) {
      sendSmsAPI({
        telephone: this.ruleForm.phone,
        type: 2,
        ...(params || {})
      }).then(() => {
        // 开启倒计时
        this.$refs.verifyButton.startTimer()
      }).catch()
    },

    /**
     * @description: 确定
     * @return {*}
     */
    saveClick() {
      if (this.type == 'reset') { // 重置密码
        this.$refs.resetPasswordForm.validate(async(valid) => {
          if (valid) {
            const { oldPassword, newPassword, confirmPassword } = this.resetPasswordForm
            if (newPassword !== confirmPassword) {
              this.$message.error('两次密码输入不一致')
              return
            }
            const params = {
              id: this.userInfo.userId,
              oldPwd: oldPassword,
              newPwd: newPassword
            }
            adminUsersResetPasswordAPI(params)
              .then(() => {
                if (!this.logOutOfLogin) {
                  this.$message.success('修改成功')
                  this.closeDialog()
                  return
                }

                removeAuth()
                  .then(() => {
                    this.$confirm('修改成功, 请重新登录', '提示', {
                      confirmButtonText: '确定',
                      showCancelButton: false,
                      type: 'warning'
                    }).then(() => {
                      Lockr.rm(LOCAL_CLEAR_PAGE_TIME)
                      Lockr.rm(LOCAL_FREE_TIME)
                      location.reload()
                      this.closeDialog()
                    }).catch(() => {
                      Lockr.rm(LOCAL_CLEAR_PAGE_TIME)
                      Lockr.rm(LOCAL_FREE_TIME)
                      location.reload()
                      this.closeDialog()
                    })
                  })
              })
              .catch(() => {})
          }
        })
      } else if (this.type == 'forget') { // 忘记密码
        if (this.smscodeShow) {
          this.loading = true
          verfySmsAPI({
            phone: this.ruleForm.phone,
            smsCode: this.ruleForm.smscode
          }).then(res => {
            if (res.data === 1) {
              this.getCompany()
              this.smscodePass = true
            } else {
              this.smscodePass = false
            }
            this.loading = false
            this.$refs.ruleForm.validateField('smscode')
          }).catch(
            this.loading = false
          )
        } else {
          this.$refs.ruleForm.validate(async(valid) => {
            await this.changeFormState()
            if (this.ruleResult.phone.validate) {
              if (!this.smscodePass) {
                this.ruleResult.phone.edit = false
                this.clearTimer()
              } else {
                if (this.passwordShow) {
                  this.loading = true
                  resetPwdAPI({
                    ...this.ruleForm,
                    companyId: this.companyId
                  })
                    .then(() => {
                      removeAuth()
                        .then(() => {
                          this.$confirm('修改成功, 请重新登录', '提示', {
                            confirmButtonText: '确定',
                            showCancelButton: false,
                            type: 'warning'
                          }).then(() => {
                            Lockr.rm(LOCAL_CLEAR_PAGE_TIME)
                            Lockr.rm(LOCAL_FREE_TIME)
                            location.reload()
                            this.closeDialog()
                          }).catch(() => {
                            Lockr.rm(LOCAL_CLEAR_PAGE_TIME)
                            Lockr.rm(LOCAL_FREE_TIME)
                            location.reload()
                            this.closeDialog()
                          })
                        })
                      this.loading = false
                    })
                    .catch(() => {
                      this.loading = false
                    })
                }
              }
            }
            return false
          })
        }
      }
    },

    closeDialog() {
      this.$emit('close')
    }
  }
}
</script>

<style lang="scss" scoped>
.expiration-reset-dialog {
  .dialog-body {
    position: relative;
    padding: 0 20px;

    .dialog-title-tip {
      margin-bottom: 20px;
      color: $--color-n100;

      &.form-tip {
        margin-bottom: unset;
        line-height: 1.5;
      }
    }

    &.forget {
      .form-title {
        margin: 10px 0;
      }

      .multiple-company-select {
        margin-bottom: 22px;
      }

      ::v-deep .el-form-item {
        .el-input__inner {
          height: 40px;
          line-height: 40px;
          border-width: 2px;

          &::placeholder {
            font-size: 14px;
            font-weight: 500;
            color: $--color-text-placeholder;
          }

          &:hover {
            background-color: $--color-n30;
          }

          &:focus {
            background-color: #fff;
          }
        }
      }
    }
  }
}
</style>
