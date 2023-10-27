<template>
  <div class="subject-container">
    <div class="subject-list">
      <div
        v-for="(item, index) in subjectList"
        :key="index"
        class="subject-list-item"
        @click="selectsubject(item, index)">
        {{ item.number }}         {{ item.subjectName }}
      </div>
    </div>
    <div class="handle-interval">
      <flexbox
        class="handle-button"
        @click.native="addsubject">
        <i class="wk wk-add handle-button-icon" />
        <div class="handle-button-name">新建科目</div>
      </flexbox>
    </div>

  </div>
</template>

<script type="text/javascript">

export default {
  name: 'SubjectList', // 客户管理下 重要提醒 回款计划提醒
  components: {},
  props: {
    subjectList: {
      type: Array,
      default: () => {
        return []
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
  },
  methods: {
    /**
     * @description: 选择场景、
     * @param {*} item
     * @param {*} index
     */
    selectsubject(item, index) {
      this.subjectSelectId = item.subjectId
      this.$emit('subject', item)
    },
    /**
     * @description: 添加场景
     */
    addsubject() {
      this.$emit('subject-handle')
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.subject-container {
  position: relative;
  width: 100%;
}

.subject-list {
  max-height: 240px;
  margin-bottom: 10px;
  overflow-y: auto;
  font-size: 12px;

  .subject-list-item {
    padding: 10px 15px;
    color: #333;
    cursor: pointer;
    background-color: white;
  }

  .subject-list-item:hover {
    color: $--color-primary;
    background-color: #f7f8fa;
  }

  .subject-list-item-select {
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
