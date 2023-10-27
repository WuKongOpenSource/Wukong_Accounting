<template>
  <div class="main">
    <el-form ref="form" :model="form" class="form" label-width="70px" label-position="top">
      <el-form-item label="会计期间">
        <choose-accountant-time v-model="form.time" />
      </el-form-item>
      <el-form-item label="辅助项目">
        <assist-project-select
          v-model="form.relationId"
          :label="label" />
      </el-form-item>
      <el-form-item label="科目">
        <subject-select-options v-model="form.subjectId" />
      </el-form-item>
      <!-- <el-form-item label="科目级次" >
        <flexbox>
          <el-input-number
            :controls="false"
            v-model="form.minLevel"
            :min="1"
            controls-position="right"/>
          <span style="padding:0 10px">
            至
          </span>
          <el-input-number
            :controls="false"
            v-model="form.maxLevel"
            :min="1"
            controls-position="right"/>
        </flexbox>
      </el-form-item> -->
      <!-- <el-form-item label="币别" >
        <el-select
          v-model="form.currencyId"
          placeholder="请选择币别"
          style="width:288px">
          <el-option
            v-for="item in currencyList"
            :key="item.currencyId"
            :label="item.currencyCoding"
            :value="item.currencyId"/>
        </el-select>

      </el-form-item> -->

      <!-- <div class="checkbox-item">
        <el-checkbox v-model="form.isShowSubject">显示科目</el-checkbox>
      </div>
      <div class="checkbox-item">
        <el-checkbox v-model="form.isShowMoney">余额为0不显示</el-checkbox>
      </div>
      <div class="checkbox-item">
        <el-checkbox v-model="form.isShowUnchange">无发生额且余额为0不显示</el-checkbox>
      </div> -->
      <flexbox class="footer" justify="flex-end">
        <el-button @click="emitClose">关闭</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button type="primary" @click="emitChange">查询</el-button>
      </flexbox>
    </el-form></div>

</template>

<script>
import ChooseAccountantTime from '@/views/fm/components/chooseAccountantTime'
import AssistProjectSelect from '@/views/fm/components/AssistProjectSelect'
import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'
import parentNodeMixins from '@/views/fm/mixins/parentNodeMixins'
import timeRangeMixins from '@/views/fm/mixins/timeRangeMixins'
import { mapGetters } from 'vuex'
import { fmFinanceCurrencyListAPI } from '@/api/fm/setting'
export default {
  name: 'AccountingBalanceSearch',
  components: {
    ChooseAccountantTime,
    SubjectSelectOptions,
    AssistProjectSelect
  },
  mixins: [parentNodeMixins, timeRangeMixins],
  props: {
    defaultValue: {
      type: Object,
      default: () => {}
    },
    label: {
      type: [String, Number],
      default: () => ''
    }
  },
  data() {
    return {
      loading: false,
      currencyList: [], // 币别列表
      form: {}
    }
  },
  computed: {
    ...mapGetters(['financeCurrentAccount'])
  },
  watch: {
    defaultValue: {
      handler(val) {
        this.form.startTime = val.startTime
        this.form.endTime = val.endTime
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    if (this.financeCurrentAccount && this.financeCurrentAccount.startTime) {
      this.resetForm()
    }
    this.getCurrencyGroupList()
  },
  methods: {
    /**
     * @description: 重置form
     */
    resetForm() {
      this.form = {
        relationId: '',
        subjectId: '',
        currencyId: '',
        minLevel: '1',
        maxLevel: '1',
        isShowSubject: false,
        isShowMoney: false,
        isShowUnchange: false,
        time: [
          this.financeCurrentAccount.startTime,
          this.financeCurrentAccount.startTime
        ]
      }
      this.emitChange()
    },
    /**
     * @description: 获取币别列表
     */
    getCurrencyGroupList() {
      fmFinanceCurrencyListAPI()
        .then(res => {
          this.currencyList = res.data
        })
    },
    /**
     * @description: 关闭操作
     */
    emitClose() {
      const parent = this.getParentNode('SearchPopoverWrapper')
      if (parent && parent.updateData) {
        parent.updateData({
          searchTime: this.getTimeShowVal(this.form.time),
          timeInputWidth: this.getTimeInputWidth(this.form.time),
          popoverVisible: false
        })
      }
    },
    /**
     * @description: 查询操作
     */
    emitChange() {
      const form = {
        ...this.form,
        startTime: this.$moment(this.form.time[0]).format('YYYYMM'),
        endTime: this.$moment(this.form.time[1]).format('YYYYMM')
      }
      Object.keys(form).forEach(key => {
        if (!form[key] && form[key] !== 0) {
          form[key] = ''
        }
      })
      this.$emit('change', form)
      this.emitClose()
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-checkbox__label {
  font-weight: normal;
}

.main {
  padding: 20px;
}

.form {
  ::v-deep.el-form-item {
    margin-bottom: 10px;
  }
}

.checkbox-item {
  padding: 10px 0;
}
</style>
