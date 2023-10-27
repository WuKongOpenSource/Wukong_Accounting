<template>
  <div v-loading="loading" class="app-container">
    <div class="content-header">
      <span>系统参数设置</span>
    </div>
    <div class="form-warp">
      <el-form :model="infoData" label-position="top" class="content-form" label-width="100px">
        <div class="form-group">
          <h3>基础参数</h3>
          <div class="form-group-items">
            <el-form-item label="公司名称">
              <el-input v-model="infoData.companyName" disabled class="sole" />
            </el-form-item>
            <el-form-item label="本位币">
              <el-select v-model="infoData.currencyId" mode="no-border" class="sole" disabled>
                <el-option
                  v-for="(item,index) in currencyList"
                  :key="index"
                  :label="item.currencyCoding"
                  :value="item.currencyId" />
              </el-select>
            </el-form-item>
            <el-form-item label="启用期间">
              <el-date-picker
                v-model="infoData.startTime"
                class="sole"
                type="month"
                disabled
                placeholder="选择月" />
            </el-form-item>
            <el-form-item label="会计制度">
              <el-select v-model="infoData.bookkeeperId" class="sole" mode="no-border">
                <el-option
                  v-for="(item,index) in accountSystemList"
                  :key="index"
                  :label="item.label"
                  :value="item.value" />
              </el-select>
            </el-form-item>
          </div>
        </div>
        <div class="form-group">
          <h3>
            科目参数
            <i
              class="wk wk-icon-fill-help wk-help-tips"
              data-type="42"
              data-id="336" />
          </h3>
          <div class="form-group-items">
            <el-form-item label="科目级次">
              <flexbox>
                <el-select
                  mode="no-border"
                  :value="infoData.level"
                  @input="levelChange">
                  <el-option
                    v-for="(item,index) in subjectLevel"
                    :key="index"
                    :label="item"
                    :value="item" />
                </el-select>
                <reminder
                  style="display: inline-block;"
                  content="科目级次和长度调大后，不能再调小（即：不能再恢复到调整前的级次和长度），请谨慎操作！" />
              </flexbox>
            </el-form-item>
            <el-form-item label="编码长度" class="num-code">
              <div
                v-for="(item,index) in infoData.level"
                :key="index">
                <el-input-number
                  v-model="infoData.rule[index]"
                  :min="Number(proRule[index])||2"
                  :max="5"
                  style="width: 80px;"
                  type="number"
                  controls-position="right"
                  @change="ruleChange($event,index)" />
                <span v-if="item!==infoData.level">-</span>
              </div>
            </el-form-item>
          </div>
        </div>
        <div class="form-group">
          <h3>账簿</h3>
          <div class="form-group-items">
            <el-form-item>
              <el-checkbox
                v-model="infoData.accountBookDirection"
                :true-label="1"
                :false-label="2">
                账簿余额方向与科目方向相同
              </el-checkbox>
            </el-form-item>
            <!-- <el-form-item ><el-checkbox v-model="infoData.deficitExamine" :true-label="1" :false-label="2">现金、银行存款科目赤字检查</el-checkbox></el-form-item> -->
            <el-form-item>
              <el-checkbox
                v-model="infoData.voucherExamine"
                :true-label="1"
                :false-label="2">
                凭证审核后才允许结账
              </el-checkbox>
            </el-form-item>
          </div>
        </div>
        <el-button
          v-if="$auth('finance.systemParam.update')"
          type="primary"
          @click="submit">保存</el-button>
      <!-- <div class="form-group">
        <h3>资产</h3>
        <el-form-item ><el-checkbox v-model="infoData.propertyUnable" :true-label="1" :false-label="2">生成折旧凭证后不能新增和修改以前期间的卡片</el-checkbox></el-form-item>
      </div> -->
      <!-- <div class="form-group">
        <h3>开票人信息</h3>
        <el-form-item label="纳税人名称"><el-input v-model="infoData.taxpayerName" /></el-form-item>
        <el-form-item label="纳税人识别号"> <el-input v-model="infoData.taxpayerNumber" /></el-form-item>
      </div> -->
      </el-form>
    </div>
  </div>
</template>

<script>
import { fmFinanceParameterQueryParameterAPI, fmFinanceParameterUpdateParameterAPI } from '@/api/fm/setting'
import { fmFinanceCurrencyListAPI } from '@/api/fm/setting'
import { objDeepCopy } from '@/utils'
import Reminder from '@/components/Reminder'

export default {
  name: 'SystemParameter',
  components: {
    Reminder
  },

  data() {
    return {
      loading: false,
      infoData: {},
      proLevel: '',
      proRule: [],
      currencyList: [],
      accountSystemList: [
        {
          value: '1',
          label: '小企业会计准则（2013年颁）'
        }
      ]
    }
  },
  computed: {
    subjectLevel() {
      const level = []
      for (let i = 0; i < (9 - this.proLevel); i++) {
        const num = this.proLevel + i
        level.push(num)
      }
      return level
    }
  },
  watch: {
  },
  created() {
    this.getData()
    this.getCurrencyGroupList()
  },
  methods: {
    /**
     * 系统参数列表
     */
    getData() {
      this.loading = true
      fmFinanceParameterQueryParameterAPI()
        .then(res => {
          this.loading = false
          console.log(res.data)
          if (res.data) {
            res.data.rule = (res.data.rule || '').split('-')
            this.infoData = {
              ...res.data,
              level: res.data.level || 0
            }
            this.proLevel = this.infoData.level
            this.proRule = objDeepCopy(res.data.rule)
          }
        })
        .catch((cat) => {
          console.log(cat)
          this.loading = false
        })
    },
    getCurrencyGroupList() {
      fmFinanceCurrencyListAPI()
        .then(res => {
          this.currencyList = res.data
        })
    },
    /**
     * @description: 规则改变
     * @param {*} data
     * @param {*} index
     */
    ruleChange(data, index) {
      console.log(data, this.proRule[index])
      if (data < this.proRule[index]) {
        this.infoData.rule.splice(index, 1, this.proRule[index])
      }
    },
    /**
     * 系统参数添加成功
     */
    submit() {
      const params = objDeepCopy(this.infoData)
      params.rule = params.rule.join('-')
      this.loading = true
      fmFinanceParameterUpdateParameterAPI(params).then(res => {
        this.$message.success('保存成功')
        this.getData()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /**
     * @description: 等级改变
     * @param {*} data
     */
    levelChange(data) {
      console.log(data)
      if (data > this.infoData.level) {
        for (let i = 0; i < data - this.infoData.level; i++) {
          this.infoData.rule.push(2)
        }
      } else {
        for (let i = 0; i < this.infoData.level - data; i++) {
          this.infoData.rule.pop()
        }
      }
      console.log(this.infoData.rule)
      this.infoData.level = data
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "../styles/index.scss";

/* 系统参数设置 */
.content-form {
  .form-group {
    margin-bottom: 32px;

    h3 {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 600;
    }

    .el-input {
      max-width: 220px;
    }

    .num-code {
      ::v-deep.el-form-item__content {
        display: flex;
      }
    }

    // &-items {
    //   padding: 16px;
    //   background-color: white;
    //   border-radius: $--border-radius-base;
    //   box-shadow: $--box-shadow-bottom;
    // }

    ::v-deep .el-form-item {
      margin-bottom: 16px;

      .el-form-item__label {
        padding-bottom: 2px;
        line-height: 16px;
        color: #42526e;
      }
    }
  }
}

.form-warp {
  // overflow-y: auto;
  // height: calc(100% - 64px);
  padding: 24px 32px;
  background-color: white;
  border-top: 3px solid #2362fb;
  border-radius: 4px;
  border-radius: $--border-radius-base;
  box-shadow: $--box-shadow-dark;
}

.num-code {
  ::v-deep.el-input-number__decrease,
  ::v-deep.el-input-number__increase {
    width: 18px;
  }
}

.no-border {
  ::v-deepinput {
    border: none;
  }
}

.sole {
  width: 280px;
  max-width: none !important;
}

::v-deep.reminder-body {
  color: #797a81;
  background-color: transparent !important;
}
</style>
