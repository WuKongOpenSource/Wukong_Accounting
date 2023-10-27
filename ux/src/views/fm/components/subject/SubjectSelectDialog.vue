<template>
  <el-dialog
    :visible.sync="dialogVisible"
    :close-on-click-modal="false"
    :before-close="handleClose"
    append-to-body
    width="500px">
    <span slot="title" class="el-dialog__title">
      选择科目
      <i
        v-if="helpObj"
        class="wk wk-icon-fill-help wk-help-tips"
        :data-type="helpObj.dataType"
        :data-id="helpObj.dataId" />
    </span>
    <SubjectSelectOptions
      v-model="selectedNode"
      v-bind="$attrs"
      v-on="$listeners"
    />
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleConfirm">确定</el-button>
      <el-button @click="handleClose">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>

import SubjectSelectOptions from './components/SubjectSelectOptions.vue'

export default {
  name: 'SubjectSelectOptionsDialog',
  components: {
    SubjectSelectOptions
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    helpObj: Object
  },
  data() {
    return {
      dialogVisible: false,
      selectedNode: null
    }
  },
  watch: {
    visible: {
      handler() {
        this.dialogVisible = this.visible
      },
      immediate: true
    }
  },
  methods: {
    /**
     * 关闭弹窗
     */
    handleClose() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
      this.$emit('close')
    },

    /**
     * 确认选择
     */
    handleConfirm() {
      this.$emit('confirm', this.selectedNode)
      this.handleClose()
    }
  }
}
</script>

<style scoped lang="scss">
::v-deep.el-dialog__body {
  padding: 0;
}
</style>
