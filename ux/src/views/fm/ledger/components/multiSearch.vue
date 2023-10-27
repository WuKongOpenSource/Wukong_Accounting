<template>
  <div>
    <el-popover
      id="closePopover"
      v-model="showSearch"
      trigger="manual"
      popper-class="no-padding-popover"
      width="400">
      <el-input
        slot="reference"
        :readonly="true"
        :value="`${searchForm.startTime} 至${searchForm.endTime} `"
        style="margin-left: 10px;"
        class="scene-select"
        @focus="isFocus">
        <i
          slot="suffix"
          :class="['el-input__icon', 'el-icon-' + iconClass]" />
      </el-input>
      <search-form v-if="type=='VoucherSearch'" :now-data="searchForm" @restTime="restTime" @closeProper="closeProper" @search="tableSearch" @resetSearch="resetSearch" />
      <!-- <a-search v-else-if="type=='ADLSearch'" :now-data="searchForm" @restTime="restTime" @closeProper="closeProper" @search="tableSearch" @resetSearch="resetSearch"/> -->
      <!-- <number-money-search v-else-if="type=='numberMoneySearch'" :now-data="searchForm" @restTime="restTime" @closeProper="closeProper" @search="tableSearch" @resetSearch="resetSearch"/> -->
      <ledger-search v-else :now-data="searchForm" @restTime="restTime" @closeProper="closeProper" @search="tableSearch" @resetSearch="resetSearch" />
    </el-popover>
  </div>
</template>
<script>
import LedgerSearch from '@/views/fm/ledger/components/ledgerSearch'// 总账和科目余额表
import SearchForm from '@/views/fm/voucher/components/SearchForm' // 查凭证
// import ASearch from '@/views/fm/ledger/auxiliaryDetailedLedger/components/ASearch' // 查凭证
// import numberMoneySearch from '@/views/fm/ledger/numberMoneyAccount/components/numberMoneySearch'
import { mapGetters } from 'vuex'
import { objDeepCopy } from '@/utils'
export default {
  name: 'MultiSearch',
  components: {
    LedgerSearch,
    SearchForm
    // ASearch,
    // numberMoneySearch
  },
  props: {
    type: {
      type: [String],
      default: ''
    }
  },
  data() {
    return {
      // 控制显示隐藏
      showSearch: false,
      // 搜索时间
      searchForm: {
        startTime: '',
        endTime: ''
      }
    }
  },
  computed: {
    iconClass() {
      return this.showScene ? 'arrow-up' : 'arrow-down'
    },
    ...mapGetters([
      'financeCurrentAccount'
    ]),
    mounthNum() {
      const mounthNum = this.financeCurrentAccount.startTime.substring(0, 7)
      console.log(mounthNum)
      return mounthNum
    }
  },
  watch: {
  },
  created() {
    // 获取当前时间
    this.searchForm.startTime = this.mounthNum
    this.searchForm.endTime = this.mounthNum
  },
  mounted() {},
  methods: {
    /**
     * @description: 点击选择框
     */
    isFocus() {
      this.showSearch = true
    },
    /**
     * @description: 渲染选择的时间
     * @param {*} sendTime
     */
    restTime(sendTime) {
      this.searchForm.startTime = sendTime.startTime
      this.searchForm.endTime = sendTime.endTime
    },
    /**
     * @description: 点击重置
     * @param {*} resetValue
     */
    resetSearch(resetValue) {
      this.showSearch = false
      this.searchForm.startTime = this.mounthNum
      this.searchForm.endTime = this.mounthNum
      this.searchForm = objDeepCopy(this.searchForm)
      this.$emit('multiSearchObejct', { ...resetValue, ...this.searchForm })
    },
    /**
     * @description: 点击搜索
     * @param {*} val
     */
    tableSearch(val) {
      this.showSearch = false
      console.log('搜索的值', val)
      this.$emit('multiSearchObejct', val)
    },
    /**
     * @description: 点击关闭
     */
    closeProper() {
      const nowTime = this.mounthNum
      if (this.searchForm != undefined && (this.searchForm.startTime != nowTime || this.searchForm.endTime != nowTime)) {
        console.log('不需要重置')
      } else {
        this.searchForm.startTime = this.mounthNum
        this.searchForm.endTime = this.mounthNum
      }
      this.showSearch = false
    }
  }
}
</script>

