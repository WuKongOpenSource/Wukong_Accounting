<template>
  <el-dialog
    :visible.sync="visible"
    :before-close="close"
    width="600px"
    title="常用功能设置">
    <reminder
      style="display: inline-block;"
      content="最多只能自定义添加3个常用功能" />

    <flexbox class="nav-wrapper">
      <div
        v-for="(item,index) in dataArr"
        :key="index"
        class="setting-box">
        <p class="setting-box-title">{{ item.title }}</p>
        <p
          v-for="child in item.children"
          v-show="canShow(child)"
          :key="child.id"
          class="setting-box-item">
          <el-checkbox
            v-model="child.check"
            @change="checkedChange(child)">
            {{ child.name }}
          </el-checkbox>
        </p>
      </div>
    </flexbox>

    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="chooseSure">确定</el-button>
      <el-button @click="close">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import Reminder from '@/components/Reminder'
import NavLib from '../nav'
import { mapGetters } from 'vuex'
export default {
  name: 'OftenSetting',
  components: {
    Reminder
  },
  props: {
    visible: Boolean,
    defaultValue: String
  },
  data() {
    return {
      dataArr: NavLib,
      chooseArr: []
    }
  },
  computed: {
    ...mapGetters([
      'finance'
    ])
  },
  watch: {
    visible: {
      handler(val) {
        if (val) {
          this.chooseArr = []
          const ids = (this.defaultValue || '').split(',')
          NavLib.forEach(item => {
            item.children.forEach(child => {
              if (ids.includes(child.id)) {
                this.$set(child, 'check', true)
                this.chooseArr.push(child)
              } else {
                this.$set(child, 'check', false)
              }
            })
          })
        }
      },
      immediate: true
    }
  },
  methods: {
    canShow(item) {
      if (item.id == '001') {
        if (this.finance.voucher && this.finance.voucher.read) {
          return true
        } else {
          return false
        }
      }
      return true
    },
    /**
     * @description: 常用功能设置复选框操作处理
     * @param {Object} item
     * @param {String|Number} item.id
     * @param {Boolean} item.check
     * @return {*}
     */
    checkedChange(item) {
      const findIndex = this.chooseArr.findIndex(o => o.id === item.id)
      if (item.check && findIndex === -1) {
        this.chooseArr.push(item)
        if (this.chooseArr.length > 3) {
          this.$nextTick(() => {
            const arr = this.chooseArr.splice(0, this.chooseArr.length - 3)
            arr.forEach(o => {
              this.$set(o, 'check', false)
            })
          })
        }
      }
      if (!item.check && findIndex !== -1) {
        this.chooseArr.splice(findIndex, 1)
      }
    },

    /**
     * @description: 取消操作
     */
    close() {
      this.$emit('close')
    },

    /**
     * @description: 确定操作
     * @param {*}
     * @return {*}
     */
    chooseSure() {
      this.$emit('confirm', this.chooseArr.map(o => o.id).join(','))
    }
  }
}
</script>

<style scoped lang='scss'>
.nav-wrapper {
  height: 250px;
  margin-top: 20px;

  .setting-box {
    height: 100%;
    padding: 0 20px;

    .setting-box-title {
      margin-bottom: $--interval-base;
      font-size: 18px;
    }

    .setting-box-item {
      margin-bottom: 5px;
    }
  }
}

// ::v-deep.el-dialog__header{
//   border-bottom: 1px solid #e6e6e6;
// }
::v-deep.el-dialog__body {
  padding: 15px 15px 30px;
}
</style>
