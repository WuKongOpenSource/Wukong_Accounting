export default {
  methods: {
    getParentNode(componentName) {
      let parent = this.$parent || this.$root
      let name = parent.$options.name
      while (parent && (!name || name !== componentName)) {
        parent = parent.$parent
        if (parent) {
          name = parent.$options.name
        }
      }
      return parent
    }
  }
}
