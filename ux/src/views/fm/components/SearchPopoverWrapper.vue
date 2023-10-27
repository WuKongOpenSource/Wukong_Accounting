<template>
  <el-popover
    v-model="popoverVisible"
    placement="bottom-start"
    popper-class="no-padding-popover"
    width="400"
    @show="handleShowPopover"
    @hide="handleClosePopover"
  >
    <el-input
      slot="reference"
      :readonly="true"
      :value="searchTime"
      :class="['scene-select', { 'is-show': popoverVisible }]"
      :style="{ width: timeInputWidth }"
      style="margin-left: 10px;"
    >
      <i
        slot="suffix"
        :class="['el-input__icon', 'el-icon-' + iconClass]"
      />
    </el-input>
    <div class="form">
      <slot />
    </div>
  </el-popover>
</template>

<script>
export default {
  name: 'SearchPopoverWrapper',
  data() {
    return {
      popoverVisible: false,
      searchTime: '',
      timeInputWidth: '180px'
    }
  },
  computed: {
    iconClass() {
      return this.popoverVisible ? 'arrow-up' : 'arrow-down'
    }
  },
  watch: {
    searchTime(val) {
      this.$emit('update:searchTime', val)
    }
  },
  methods: {
    handleShowPopover() {
      if (!this.popoverVisible) {
        this.popoverVisible = true
        window.addEventListener('click', this.handleCloseOutsidePopover)
      }
    },
    handleClosePopover() {
      if (this.popoverVisible) {
        this.popoverVisible = false
        window.removeEventListener('click', this.handleCloseOutsidePopover)
      }
    },
    handleCloseOutsidePopover(event) {
      const popover = this.$refs.popover.$el
      if (!popover.contains(event.target)) {
        this.handleClosePopover()
      }
    },
    updateData(data = {}) {
      Object.keys(data).forEach((key) => {
        this[key] = data[key]
      })
    }
  }
}
</script>

<style scoped lang="scss">
.scene-select {
  ::v-deep.el-input__inner {
    cursor: pointer;
    background: $--button-default-background-color;
    border: none;
  }
}

.is-show {
  ::v-deep .el-input__inner {
    color: $--color-white;
    background-color: $--button-default-selected-bg-color !important;
    border-color: $--button-default-selected-bg-color;

    &::placeholder {
      color: $--color-white;
    }
  }

  ::v-deep.el-icon-arrow-up {
    color: $--color-white;
  }
}

.form {
  ::v-deep.el-form-item {
    margin-bottom: 8px;

    .el-form-item__label {
      padding: 0;
      line-height: 20px;
      color: #172b4d;
    }

    .el-input__inner {
      width: 100%;
    }
  }
}
</style>
