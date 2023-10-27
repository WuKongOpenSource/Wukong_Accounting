<template>
  <div class="">
    <flexbox
      slot="reference"
      :class="[disabled ? 'is_disabled' : 'is_valid']"
      wrap="wrap"
      class="user-container xh-form-border"
      @click.native="contentClick">
      <div
        v-for="(item, index) in dataValue"
        :key="index"
        class="user-item">
        {{ item.carteName }}
      </div>
      <div
        v-if="dataValue.length == 0"
        class="add-item">+添加</div>
    </flexbox>

    <adjuvant-relative-list
      :account-dialog-visible="accountDialogVisible"
      :current-type="currentType"
      :label="label"
      :fixed-list="fixedList"
      :adjuvant-custom-list="adjuvantCustomList"
      @close="handlerClose"
      @selectList="receiveAdjuvantList"
    />

  </div>
</template>

<script>
// import { queryLabelName } from '@/api/fm/ledger'
import AdjuvantRelativeList from './AdjuvantRelativeList'

export default {
  name: 'AdjuvantRelative',
  components: {
    AdjuvantRelativeList
  },
  props: {
    /** 当前类型id */
    currentType: {
      type: [String, Number],
      default: 1
    },
    /** 当前类型label */
    label: {
      type: [Number, String],
      default: () => ''
    },
    value: {
      type: [Number, String],
      default: () => ''
    }
  },
  data() {
    return {
      accountDialogVisible: false, // 辅助核算列表显示与隐藏
      disabled: false,

      // currentType: null, // 当前类型
      fixedList: [],
      adjuvantCustomList: [],
      dataValue: [] // 选择值
    }
  },
  computed: {
  },
  watch: {
    value: {
      handler(val) {

      },
      immediate: true
    },
    label: {
      handler(val) {
        console.log('object', val)
      },
      immediate: true
    }
  },
  created() {
  },
  mounted() {
  },
  methods: {
    /** 打开弹窗 */
    contentClick() {
      this.accountDialogVisible = true
    },
    /** 关闭弹窗 */
    handlerClose() {
      this.accountDialogVisible = false
    },
    /** 接受选择的辅助核算列表 */
    receiveAdjuvantList(data) {
      // console.log('接受', data)
      this.$emit('value-change', data)
      this.dataValue = data
    }

  }
}
</script>

<style scoped lang='scss'>
  .user-container {
    position: relative;
    min-height: 34px;
    font-size: 12px;
    line-height: 15px;
    color: #333;
    cursor: pointer;
    border: 1px solid #ddd;
    border-radius: 3px;

    .user-item {
      padding: 5px;
      margin: 3px;
      cursor: pointer;
      background-color: #e2ebf9;
      border-radius: 3px;
    }

    .add-item {
      padding: 5px;
      color: $--color-text-placeholder;
      cursor: pointer;
    }

    .delete-icon {
      color: #999;
      cursor: pointer;
    }
  }

  .user-container.is_disabled {
    cursor: not-allowed;
    background-color: #f5f7fa;
    border-color: #e4e7ed;

    .user-item {
      color: #c0c4cc;
      cursor: not-allowed;
      background-color: #f0f4f8ea;
    }

    .delete-icon {
      color: #c0c4cc;
      cursor: not-allowed;
    }

    .add-item {
      color: #c0c4cc;
      cursor: not-allowed;
    }
  }

  .user-container.is_valid:hover {
    border-color: #c0c4cc;
  }
</style>
