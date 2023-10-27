<template>
  <el-cascader
    ref="elCascader"
    v-model="dataValue"
    :options="options"
    :show-all-levels="false"
    :props="defaultProps"
    :disabled="disabled"
    style="width: 100%;"
    change-on-select
    @change="valueChange" />
</template>
<script type="text/javascript">
import ArrayMixin from './ArrayMixin'

export default {
  name: 'XhProducCate', // 新建 产品分类
  components: {},
  mixins: [ArrayMixin],
  props: {},
  data() {
    return {
      options: [],
      defaultProps: {
        children: 'children',
        label: 'label',
        value: 'categoryId'
      }
    }
  },
  computed: {},
  watch: {},
  mounted() {
  },
  methods: {
    valueChange(val) {
      const nodes = this.$refs.elCascader.getCheckedNodes()
      this.$emit('value-change', {
        index: this.index,
        item: this.item,
        value: val,
        valueContent: nodes.length > 0 ? nodes[0].label : '' // 参考 https://segmentfault.com/q/1010000013063478
      })
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
