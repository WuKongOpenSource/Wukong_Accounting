<template>
  <el-dialog
    ref="wkDialog"
    :title="currencyTitle"
    :visible.sync="currencyDialogVisible"
    :before-close="currencyClose"
    :close-on-click-modal="false"
    width="40%">
    <el-form ref="form" :rules="rules" :model="form" class="form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="币别代码" prop="currencyCoding">
            <el-select
              v-model="form.currencyCoding"
              filterable
              clearable
            >
              <el-option
                v-for="(item,index) in currencySymbol"
                :key="index"
                :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="币别名称" prop="currencyName">
            <el-select
              v-model="form.currencyName"
              filterable
              clearable
            >
              <el-option
                v-for="(item,index) in currencyNameList"
                :key="index"
                :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="记账汇率" prop="exchangeRate">
            <el-input v-model="form.exchangeRate" />
          </el-form-item>
        </el-col>
        <!-- <el-col :span="12">
          <el-form-item label="汇率">
            <el-select v-model="form.rate" >
              <el-option label="固定汇率" :value="0" />
              <el-option label="浮动汇率" :value="1" />
            </el-select>
          </el-form-item>
        </el-col> -->
      </el-row>
      <!-- <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="金额小数位数">
            <el-select v-model="form.digit" >
              <el-option label="0" :value="0" />
              <el-option label="1" :value="1" />
              <el-option label="2" :value="2" />
              <el-option label="3" :value="3" />
              <el-option label="4" :value="4" />
              <el-option label="5" :value="5" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="折算方式">
            <el-radio-group v-model="form.convertWay">
              <el-radio :label="0">原币 × 汇率 = 本位币</el-radio>
              <el-radio :label="1">原币 ÷ 汇率 = 本位币</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row> -->
    </el-form>
    <span
      slot="footer"
      class="dialog-footer">
      <el-button
        v-debounce="currencySubmit"
        type="primary">确定</el-button>
      <el-button @click="currencyClose">取消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { fmFinanceCurrencyAddOrUpdateAPI } from '@/api/fm/setting'

import { isEmpty } from '@/utils/types'
import { objDeepCopy } from '@/utils'

export default {
  props: {
    currencyDialogVisible: Boolean,
    /** 编辑时候传递进来的信息 */
    action: {
      type: Object,
      default: () => {
        return {
          type: 'add'
        }
      }
    }
  },
  data() {
    return {
      loading: false,
      form: {
        abOptions: []
      },
      name: '',
      rules: {
        currencyCoding: [
          { required: true, message: '请输入币别代码', trigger: 'blur' }
        ],
        currencyName: [
          { required: true, message: '请输入币别名称', trigger: 'blur' }
        ],
        exchangeRate: [
          { required: true, message: '请输入记账汇率', trigger: 'blur' }
        ]
      },
      // 货币符号列表
      currencySymbol: [
        { label: 'RMB', value: 'RMB' },
        { label: 'HKD', value: 'HKD' },
        { label: 'MOP', value: 'MOP' },
        { label: 'USD', value: 'USD' },
        { label: 'EUR', value: 'EUR' },
        { label: 'JPY', value: 'JPY' },
        { label: 'EUR', value: 'EUR' },
        { label: 'GBP', value: 'GBP' },
        { label: 'PHP', value: 'PHP' },
        { label: 'THB', value: 'THB' },
        { label: 'DKK', value: 'DKK' },
        { label: 'AUD', value: 'AUD' }
      ],
      // 货币名称
      currencyNameList: [
        { label: '人民币', value: '人民币' },
        { label: '港币', value: '港币' },
        { label: '澳门元', value: '澳门元' },
        { label: '美元', value: '美元' },
        { label: '欧元', value: '欧元' },
        { label: '日元', value: '日元' },
        { label: '欧元', value: '欧元' },
        { label: '英镑', value: '英镑' },
        { label: '菲律宾比索', value: '菲律宾比索' },
        { label: '泰铢', value: '泰铢' },
        { label: '丹麦克朗', value: '丹麦克朗' },
        { label: '澳大利亚元', value: '澳大利亚元' }
      ]
    }
  },
  computed: {
    currencyTitle() {
      if (this.action.type == 'update') {
        return '编辑币别'
      } else {
        return '新增币别'
      }
    }
  },
  watch: {
    action: {
      deep: true,
      immediate: true,
      handler: function(val) {
        if (!val.infoData || isEmpty(val.infoData)) {
          this.form = {
            'currencyCoding': '',
            'currencyName': '',
            'exchangeRate': ''
            // 'rate': 0,
            // 'digit': 2,
            // 'convertWay': 0
          }
        } else {
          this.form = objDeepCopy(val.infoData)
        }
      }
    }
  },
  mounted() {
  },
  methods: {
    currencyClose() {
      this.$emit('currency-close')
    },
    currencySubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return

        this.loading = true
        fmFinanceCurrencyAddOrUpdateAPI(this.form).then(res => {
          this.loading = false
          this.$message.success('添加成功')
          this.$emit('currency-submit')
        }).catch(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.form {
  ::v-deep.el-form-item {
    margin-bottom: 10px;

    .el-form-item__label {
      width: 100%;
      line-height: 35px;
      text-align: left;
    }

    .el-form-item__content {
      margin: 0;

      .el-select {
        width: 100%;
      }
    }
  }
}

.icon-box {
  display: inline-block;
  width: 50px;
  margin-left: 10px;
  text-align: left;
}

</style>
