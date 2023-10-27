<template>
  <el-dialog
    v-loading="loading"
    :visible="dialogVisible"
    :before-close="close"
    width="600px"
    title="创建全新账套">
    <el-form
      ref="createForm"
      :model="createForm"
      :rules="createRules"
      label-width="100px"
      class="demo-ruleForm">
      <el-form-item label="公司名称：" prop="companyName">
        <el-input v-model="createForm.companyName" :disabled="true" />
      </el-form-item>
      <el-form-item label="本位币：" prop="currencyId" required>
        <el-select v-model="createForm.currencyId" placeholder="请选币种">
          <el-option
            v-for="item in RMBList"
            :key="item.currencyId"
            :label="item.currencyCoding"
            :value="item.currencyId" />
        </el-select>
      </el-form-item>
      <el-form-item label="启用期间：" required>
        <el-date-picker
          v-model="createForm.startTime"
          :clearable="false"
          value-format="yyyy-MM-dd"
          format="yyyy 年 MM 期"
          type="month"
          placeholder="选择期间" />
      </el-form-item>
      <el-form-item label="会计制度：" prop="bookkeeperId">
        <el-select v-model="createForm.bookkeeperId" placeholder="请选择活动区域">
          <el-option
            v-for="item in accountSystemList"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="报表预览：">
        <el-button type="text" @click="imgClick(0)">资产负债表</el-button>
        <el-button type="text" @click="imgClick(1)">科目预览表</el-button>
        <el-button type="text" @click="imgClick(2)">利润表</el-button>
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm()">创建</el-button>
      <el-button @click="close">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { saveAccountSet, queryListByAccountId, getUserByaccountId } from '@/api/admin/accountBook'
import { mapGetters } from 'vuex'
export default {
  name: 'AccountStepDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    companyInfo: [String, Object]
  },
  data() {
    return {
      loading: false,
      dialogVisible: false,

      createForm: {
        companyName: '',
        accountId: '', //	账套id
        bookkeeperId: '1', // 会计制度id
        currencyId: '', // 本位币
        startTime: '' // 启用期间
      },
      createRules: {
        name: [
          { required: true, message: '请输入活动名称', trigger: 'blur' },
          { min: 3, max: 5, message: '长度在 3 到 5 个字符', trigger: 'blur' }
        ]
      },
      RMBList: [],
      accountSystemList: [
        {
          value: '1',
          label: '小企业会计准则（2013年颁）'
        }
      ],
      imgList: [
        {
          id: 999,
          showName: '资产负债表',
          name: 'balanceSheet.png',
          url: require('@/assets/img/balanceSheet.png')
        },
        {
          id: 998,
          showName: '科目预览表',
          name: 'subject.png',
          url: require('@/assets/img/project.png')
        },
        {
          id: 997,
          showName: '余额表',
          name: 'profit.png',
          url: require('@/assets/img/profit.png')
        }
      ]
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  watch: {
    visible: {
      handler() {
        this.dialogVisible = this.visible
      },
      immediate: true
    },
    companyInfo: {
      handler(n) {
        this.createForm.companyName = n.companyName
        this.createForm.accountId = n.accountId
      },
      deep: true,
      immediate: true
    }
  },
  // 生命周期钩子函数
  created() {
    this.createForm.startTime = this.$moment().format('YYYY-MM-DD')
    this.getRMBData()
  },
  methods: {
    // getRMBData
    getRMBData() {
      queryListByAccountId({ accountId: this.createForm.accountId }).then(res => {
        this.RMBList = res.data
        const firstChild = this.RMBList[0] || {}
        this.$set(this.createForm, 'currencyId', firstChild.currencyId)
      })
    },
    // 图片预览
    imgClick(index) {
      this.$wkPreviewFile.preview({
        index: index || 0,
        data: this.imgList.map(function(item) {
          return {
            url: item.url,
            name: item.name
          }
        })
      })
    },

    // 点击第二步的创建
    submitForm() {
    // 判断校验
      this.$refs.createForm.validate((valid) => {
        if (valid) {
          this.loading = true
          getUserByaccountId({ accountId: this.createForm.accountId }).then(res => {
            const findItem = res.data.userList.find(item => {
              return item.userId == this.userInfo.userId
            })
            if (findItem) {
              this.jumpMainPage()
            }
          })
        }
      })
    },
    jumpMainPage() {
      saveAccountSet(this.createForm).then(res => {
        this.loading = false
        this.close()
        this.$message.success('创建成功')
        this.$router.push({
          path: '/fm/workbench',
          query: {
            id: this.createForm.accountId
          }
        })
      }).catch(() => {
        this.loading = false
      })
    },

    close() {
      this.$emit('update:visible', false)
      this.$emit('close')
    }
  }
}
</script>

<style scoped lang='less'>
.el-select,
.el-input,
.el-date-editor {
  width: 100%;
}
</style>
