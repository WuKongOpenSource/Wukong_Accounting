<template>
  <div class="voucher-files">
    <div v-if="showFileForm" class="section">
      <div class="section__hd">
        <span>附件</span>
        <span v-if="fileList.length">({{ fileList.length }})</span>
      </div>
      <div class="section__bd">
        <file-cell
          v-for="(file, fileIndex) in fileList"
          :key="file.fileId"
          :data="file"
          delete-show
          :list="fileList"
          :index="fileIndex"
          @delete="accessoryDeleteFun" />
        <el-upload
          :http-request="httpRequest"
          class="upload-file"
          action="https://jsonplaceholder.typicode.com/posts/"
          multiple
          list-type="picture">
          <span class="add-btn">
            <i class="wk wk-l-plus" />
            <span class="label">附件</span>
          </span>
        </el-upload>
      </div>
    </div>
    <!-- <wk-file-form
      v-if="showFileForm"
      v-model="batchId"
      :default-value="fileList"
      @change="fileChange" /> -->
  </div>
</template>

<script>
import { adminFileQueryFileListAPI, adminFileDeleteByIdAPI } from '@/api/admin/file'
import FileCell from '@/components/NewCom/WkFile/Cell'
// import WkFileForm from '@/components/NewCom/WkFile/Form'

export default {
  name: 'VoucherFiles',
  components: {
    FileCell
    // WkFileForm
  },
  props: {
    batchId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      showFileForm: false,

      fileList: []
    }
  },
  mounted() {
    this.getFileListByBatchId()
  },
  methods: {
    /**
     * @description: 获取文件列表通过批次id
     */
    getFileListByBatchId() {
      this.loading = true
      this.showFileForm = false
      this.$nextTick(() => {
        adminFileQueryFileListAPI(this.batchId).then(res => {
          this.loading = false
          this.showFileForm = true
          this.fileList = res.data || []
        }).catch(() => {
          this.loading = false
          this.showFileForm = true
        })
      })
    },
    /**
     * @description: 附件 -- 上传
     * @param {*} val
     */
    httpRequest(val) {
      this.$wkUploadFile.upload({
        file: val.file,
        params: {
          batchId: this.batchId
        }
      }).then(({ res }) => {
        const data = res.data || {}
        this.fileList.push(data)
        // this.$emit('httpRequest', this.taskData)
        this.fileChange(this.fileList, data.batchId)
        this.$message.success('上传成功')
      }).catch(() => {})
    },
    /**
     * @description: 删除文件操作
     * @param {*} index
     * @param {*} item
     */
    accessoryDeleteFun(item, index) {
      adminFileDeleteByIdAPI(item.fileId).then(res => {
        this.fileList.splice(index, 1)
        this.fileChange(this.fileList, item.batchId)
      }).catch(() => {})
    },
    /**
     * @description: 文件选择操作
     * @param {object[]} fileList
     * @param {string} batchId
     */
    fileChange(fileList, batchId) {
      this.fileList = fileList
      this.showFileForm = false
      this.$nextTick(() => {
        this.showFileForm = true
      })
      this.$emit('change', fileList, batchId)
    }
  }
}
</script>

<style scoped lang="scss">
// 附件
.upload-file ::v-deep .el-upload-list--picture {
  display: none;
}

.section {
  padding: 10px 0;

  &__hd {
    span {
      font-weight: $--font-weight-semibold;
    }
  }

  &__bd {
    margin-top: #{$--interval-base * 2};
  }

  ::v-deep.wk-file-cell {
    background-color: #f4f5f7;

    .wk-file-cell__body {
      color: #172b4d;
    }

    .wk-file-cell__size {
      font-size: 14px;
      color: #6b778c;
    }
  }
}
</style>
