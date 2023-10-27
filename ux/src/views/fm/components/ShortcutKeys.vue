<template>
  <el-popover
    placement="bottom"
    width="420"
    trigger="hover"
  >
    <div class="wrapper">
      <img src="@/assets/img/keys.png" class="img" alt="">
      <!-- <el-table :data="keysList" >
        <el-table-column v-for="(item,index) in fieldList" :label="item.value" :key="index" :prop="item.prop" />
      </el-table> -->

    </div>
    <el-button slot="reference" type="text">快捷键</el-button>
  </el-popover>
</template>

<script>

export default {
  name: 'ShortcutKeys',
  data() {
    return {
      keysList: [
        { key: '保存并新增', value: 'F12' },
        { key: '新增', value: 'Alt + N' },
        { key: '录入第二条分录', value: 'Enter' },
        { key: '自动平衡借贷方金额', value: '=' },
        { key: '保存', value: 'Ctrl + S' }
      ],
      fieldList: [
        { prop: 'key', value: '凭证' },
        { prop: 'value', value: '快捷键' }
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
.img {
  width: 400px;
}

.el-table {
  ::v-deep.el-table__row {
    .el-table__cell {
      padding: 6px 0;
    }

    height: 30px;
  }
}
</style>

