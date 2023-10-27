<template>
  <el-dialog
    class="print-set-dialog"
    title="凭证打印"
    v-bind="$attrs"
    width="500px"
    :close-on-click-modal="false"
    v-on="$listeners"
    @close="closeHandle">
    <el-form
      ref="form"
      label-position="top"
      :model="form"
      :rules="rules"
      label-width="80px"
      :inline="false"
      size="normal">
      <el-form-item label="打印类型">
        <el-radio-group v-model="form.printType">
          <el-radio label="A4">
            A4
          </el-radio>
          <el-radio label="B5">
            B5
          </el-radio>
          <el-radio label="custom">
            自定义纸张
          </el-radio>
        </el-radio-group>
        <div v-if="form.printType =='custom'" class="set-item-wrap">
          <div class="set-item">
            <span>宽度</span>
            <el-input
              v-model="form.width"
              oninput="value=value.replace(/[^0-9.]/g,'')"
              :min="1"
              type="number" />
            <span>毫米</span>
          </div>
          <div class="set-item">
            <span>长度</span>
            <el-input
              v-model="form.height"
              type="number"
              oninput="value=value.replace(/[^0-9.]/g,'')"
              :min="1" />
            <span>毫米</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="图像方向">
        <el-radio-group v-model="form.orientation">
          <el-radio label="Landscape">
            纵向
          </el-radio>
          <el-radio label="Portrait">
            横向
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="边框调整">
        <div class="border-set">
          <div class="set-item">
            <span>左</span>
            <el-input
              v-model="form.left"
              oninput="value=value.replace(/[^0-9.]/g,'')"
              :min="1"
              type="number" />
            <span>毫米</span>
          </div>
          <div class="set-item">
            <span>上</span>
            <el-input
              v-model="form.top"
              oninput="value=value.replace(/[^0-9.]/g,'')"
              :min="1"
              type="number" />
            <span>毫米</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="字体大小">
        <div class="set-item">
          <el-input
            v-model="form.fontSize"
            oninput="value=value.replace(/[^0-9.]/g,'')"
            :min="12"
            type="number" />
          <span>像素</span>
        </div>
      </el-form-item>
    </el-form>
    <span slot="footer">
      <el-button type="primary" @click="submit">保存并打印</el-button>
      <el-button @click="closeHandle">取消</el-button>
    </span>
  </el-dialog>
</template>
<script>
import {
  adminConfigSetCustomSettingAPI,
  adminConfigQueryCustomSettingAPI
} from '@/api/fm/voucher.js'
const FM_PRINT_PAGE_SET = 'FM_PRINT_PAGE_SET'
export default {
  data() {
    return {
      form: {
        printType: 'B5',
        orientation: 'Portrait',
        top: 0,
        left: 0,
        width: 0,
        height: 0,
        fontSize: 16
      },
      rules: {}
    }
  },
  created() {
    this.getData()
  },
  methods: {
    // 获取配置设置
    getData() {
      adminConfigQueryCustomSettingAPI({ customKey: FM_PRINT_PAGE_SET }).then(res => {
        console.log(res)
        if (res.data && res.data.length) {
          this.form = res.data[0]
        }
      })
    },
    // 获取所有参数
    getStyleData() {
      const { height, width } = this.getPageSize(this.form.printType)
      return {
        height,
        width,
        top: this.form.top,
        left: this.form.left,
        orientation: this.form.orientation,
        fontSize: this.form.fontSize + 'px'
      }
    },

    // 获取纸张大小
    getPageSize(printType) {
      if (printType == 'A4') {
        return {
          width: 297,
          height: 210
        }
      }
      if (printType == 'B5') {
        return {
          width: 250,
          height: 176
        }
      }
      return {
        width: this.form.width,
        height: this.form.height
      }
    },

    // 保存并打印
    submit() {
      adminConfigSetCustomSettingAPI({ customKey: FM_PRINT_PAGE_SET, data: [this.form] })
      const styleData = this.getStyleData()
      this.$emit('confirm', styleData)
    },
    // 关闭操作
    closeHandle() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 0;

  ::v-deep .el-form-item__label {
    padding-bottom: 0;
  }
}

.set-item {
  display: flex;
  margin-right: 16px;

  .el-input {
    width: 60px;
  }
}

.set-item-wrap {
  display: flex;
}

.border-set {
  display: flex;
}
</style>
