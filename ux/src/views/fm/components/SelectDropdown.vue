<template>
  <div class="select-container">
    <div class="select-list">
      <div
        v-for="(item, index) in list"
        :key="index"
        class="select-list-item"
        @click="select(item, index)">
        {{ item[prop.label] }}
        <slot slot="label" :slot-scope="item" />
      </div>
    </div>
    <div class="handle-interval">
      <flexbox
        class="handle-button"
        @click.native="addselect">
        <i class="wk wk-add handle-button-icon" />
        <div class="handle-button-name">{{ prop.addTitle }}</div>
      </flexbox>
    </div>

  </div>
</template>

<script type="text/javascript">

export default {
  name: 'SelectDropdown',
  components: {},
  props: {
    list: {
      type: Array,
      default: () => {
        return []
      }
    },
    prop: {
      type: Object,
      default: () => {
        return {
          label: 'name',
          addTitle: '新建'
        }
      }
    }
  },
  data() {
    return {
    }
  },
  computed: {
  },
  watch: {},
  mounted() {
    console.log('')
  },
  methods: {

    // 选择场景、
    select(item, index) {
      this.$emit('select', item)
    },
    // 添加场景
    addselect() {
      this.$emit('handle-add')
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.select-container {
  position: relative;
  width: 100%;
}

.select-list {
  max-height: 240px;
  margin-bottom: 10px;
  overflow-y: auto;
  font-size: 12px;

  .select-list-item {
    padding: 10px 15px;
    color: #333;
    cursor: pointer;
    background-color: white;
  }

  .select-list-item:hover {
    color: $--color-primary;
    background-color: #f7f8fa;
  }

  .select-list-item-select {
    color: $--color-primary;
    background-color: #f7f8fa;
  }
}

.handle-button {
  padding: 6px 20px;
  font-size: 12px;
  color: $--color-primary;
  cursor: pointer;

  .handle-button-icon {
    margin-top: 3px;
    margin-right: 8px;
  }

  .handle-button-name {
    font-size: 12px;
  }
}

.handle-button:hover {
  .handle-button-name {
    text-decoration: underline;
  }
}

.handle-interval {
  border-top: 1px solid #efefef;
}
</style>
