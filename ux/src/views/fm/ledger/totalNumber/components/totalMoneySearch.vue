<template>
  <div class="main">
    <el-form ref="form" :model="form" class="form" label-width="70px" label-position="top">
      <el-form-item label="会计期间">
        <choose-accountant-time
          v-model="form.time" />
      </el-form-item>
      <el-form-item label="起始科目">
        <subject-select-options v-model="form.startSubjectId" />
      </el-form-item>
      <el-form-item label="结束科目">
        <subject-select-options v-model="form.endSubjectId" />
      </el-form-item>
      <el-form-item label="科目级次">
        <flexbox>
          <el-input-number
            v-model="form.minLevel"
            :controls="false"
            :min="1"
            controls-position="right" />
          <span style="padding: 0 10px;">
            至
          </span>
          <el-input-number
            v-model="form.maxLevel"
            :controls="false"
            :min="1"
            controls-position="right" />
        </flexbox>
      </el-form-item>
      <!-- <div class="checkbox-item">
        <span>数量显示小数位数</span>
        <el-input-number v-model="form.numberPlaces" controls-position="right"/>
      </div>
      <div class="checkbox-item">
        <span>单价显示小数位数</span>
        <el-input-number v-model="form.pricePlaces" controls-position="right"/>
      </div>
      <div class="checkbox-item">
        <el-checkbox v-model="form.isShowservice">显示辅助服务</el-checkbox>
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
    </el-form>
  </div>

</template>

<script>
import ChooseAccountantTime from '@/views/fm/components/chooseAccountantTime'
import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'
import parentNodeMixins from '@/views/fm/mixins/parentNodeMixins'
import timeRangeMixins from '@/views/fm/mixins/timeRangeMixins'
import { mapGetters } from 'vuex'
export default {
  name: 'TotalMoneySearch',
  components: {
    ChooseAccountantTime,
    SubjectSelectOptions
  },
  mixins: [parentNodeMixins, timeRangeMixins],
  props: {
    defaultValue: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      loading: false,
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
  },
  methods: {

    /**
     * @description: 重置form
     */
    resetForm() {
      this.form = {
        startTime: '',
        endTime: '',
        startSubjectId: '',
        endSubjectId: '',
        minLevel: '1',
        maxLevel: '9',
        numberPlaces: '',
        pricePlaces: '',
        isShowservice: false,
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
     * @description: 获取辅助项目id
     * @param {*} id
     */
    getprojectId(id) {
      this.form.assistId = id
      console.log('assistId', id)
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

  .checkbox-item-number {
    width: 60px;
    padding: 0 0 0 8px;
  }
}
</style>
