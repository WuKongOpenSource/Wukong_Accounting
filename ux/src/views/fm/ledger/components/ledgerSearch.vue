<template>
  <div class="main">
    <el-form ref="form" :model="form" class="form" label-width="70px">
      <el-form-item label="会计期间">
        <choose-accountant-time :time-value="form" :is-show-name="false" @getTime="getAccountantTime" />
      </el-form-item>
      <el-form-item label="起始科目">
        <subject-select-options v-model="form.srartSubject" />
      </el-form-item>
      <el-form-item label="结束科目">
        <subject-select-options v-model="form.endSubject" />
      </el-form-item>

      <el-form-item label="科目级次">
        <flexbox>
          <el-input-number v-model="form.startNum" :min="1" controls-position="right" @change="handleChange" />
          <span style="padding: 0 10px;">
            至
          </span>
          <el-input-number v-model="form.endNum" :min="1" controls-position="right" @change="handleChange" />
        </flexbox>
      </el-form-item>
      <div class="checkbox-item">
        <el-checkbox v-model="form.isShowAssist">显示辅助核算</el-checkbox>
      </div>
      <div class="checkbox-item">
        <el-checkbox v-model="form.isShowMoney">余额为0不显示</el-checkbox>
      </div>
      <div class="checkbox-item">
        <el-checkbox v-model="form.isShowUnchange">无发生额且余额为0不显示</el-checkbox>
      </div>
      <div class="checkbox-item">
        <el-checkbox v-model="form.isShowAll">无发生额不显示本期合计、本年累计</el-checkbox>
      </div>
      <flexbox class="footer" justify="flex-end">
        <el-button @click="closeProper">关闭</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="primary" @click="search">查询</el-button>
      </flexbox>
    </el-form>
  </div>
</template>

<script>

import { objDeepCopy } from '@/utils'
import ChooseAccountantTime from './chooseAccountantTime'
import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'
import { mapGetters } from 'vuex'
export default {
  name: 'LedgerSearch',
  components: {
    ChooseAccountantTime,
    SubjectSelectOptions
  },
  props: {
    searchForm: {
      type: Object,
      default: () => {
        return {}
      }
    },
    nowData: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      loading: false,
      voucherOptions: [],
      form: {
        startTime: '',
        endTime: '',
        srartSubject: '',
        endSubject: '',
        startNum: '',
        endNum: '',
        isShowAssist: false,
        isShowMoney: false,
        isShowUnchange: false,
        isShowAll: false
      },
      name: '',

      checked: false
    }
  },
  computed: {
    subjectTitle() {
      if (this.infoData.id) {
        return '编辑科目'
      } else {
        return '新建科目'
      }
    },
    ...mapGetters(['financeCurrentAccount']),
    mounthNum() {
      const mounthNum = this.financeCurrentAccount.startTime.substring(0, 7)
      console.log(mounthNum)
      return mounthNum
    }
  },
  watch: {
    searchForm(val) {
      this.form = objDeepCopy(val)
    },
    nowData: {
      handler(val) {
        this.form.startTime = val.startTime
        this.form.endTime = val.endTime
      },
      deep: true,
      immediate: true

    }
  },
  mounted() {
  },
  methods: {
    /**
     * @description: 获取会计时间
     * @param {*} val
     * @return {*}
     */
    getAccountantTime(val) {
      this.form.startTime = val.startTime.substring(0, 7)
      this.form.endTime = val.endTime.substring(0, 7)
      const sendTime = {
        startTime: val.startTime.substring(0, 7),
        endTime: val.endTime.substring(0, 7)
      }
      this.$emit('restTime', sendTime)
    },
    /**
     * @description: 获取起始科目
     * @param {*} val
     */
    subjectNameStart(val) {
      this.form.srartSubject = val
    },
    /**
     * @description: 获取结束科目
     * @param {*} val
     */
    subjectNameEnd(val) {
      this.form.endSubject = val
    },
    /**
     * @description: 隐藏搜索
     */
    subjectClose() {
      this.$emit('hidden-search')
    },
    /**
     * @description: 搜索冒泡
     */
    search() {
      this.$emit('search', this.form)
    },
    /**
     * @description: 重置数据
     */
    reset() {
      this.form = {
        startTime: this.mounthNum,
        endTime: this.mounthNum,
        srartSubject: '',
        endSubject: '',
        startNum: '1',
        endNum: '1',
        isShowAssist: false,
        isShowMoney: false,
        isShowUnchange: false,
        isShowAll: false
      }

      this.$emit('resetSearch', this.form)
    },
    subjectSubmit() {
    },
    handleChange() {},
    /**
     * @description: 点击关闭
     */
    closeProper() {
      this.$emit('closeProper', false)
    }
  }
}
</script>

<style lang="scss" scoped>
.main {
  padding: 20px;
}

.form {
  ::v-deep.el-form-item {
    margin-bottom: 10px;
  }
}

.icon-box {
  display: inline-block;
  width: 50px;
  margin-left: 10px;
  text-align: left;
}

.checkbox-item {
  padding: 10px 0;
}

</style>
