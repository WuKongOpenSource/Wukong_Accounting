<template>
  <ul class="multiple-company-select list">
    <li
      v-for="(item, index) in list"
      :key="index"
      :class="{active: value === item.companyId}"
      class="list-item"
      @click="selectClick(item.companyId, item)">
      <!--<img src="~@/assets/login/m_company.png" alt="" class="icon">-->
      <span class="icon wk wk-enterprise" />
      <span class="text">
        {{ item.companyName }}
      </span>
    </li>
  </ul>
</template>

<script>
export default {
  // 公司选择
  name: 'MultipleCompanySelect',

  components: {},

  props: {
    value: [String, Number],
    list: {
      type: Array,
      default: () => []
    }
  },

  data() {
    return {
    }
  },

  computed: {},

  watch: {},

  created() {},

  mounted() {},

  beforeDestroy() {},

  methods: {
    /**
     * 选择
     */
    selectClick(companyId, item) {
      this.$emit('input', companyId)
      this.$emit('change', item)
    }
  }
}
</script>

<style lang="scss" scoped>
.list {
  max-height: 200px;
  padding: 0;
  overflow: auto;

  .list-item {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    height: 42px;
    padding: 0 10px;
    margin: 0 0 14px;
    color: #555;
    text-align: left;
    list-style: none;
    cursor: pointer;
    background-color: #f6f6f6;
    border: 2px solid #f6f6f6;
    border-radius: $--border-radius-base;

    .icon {
      width: 15px;
      margin-right: 15px;
      color: #dadada;
    }

    .text {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .icon-checked {
      margin-left: 15px;
    }

    &.active {
      background-color: #f4f7ff;
      border-color: $--color-primary;
    }
  }
}
</style>
