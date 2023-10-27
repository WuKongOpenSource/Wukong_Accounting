<template>
  <el-dialog
    v-loading="loading"
    :visible.sync="dialogVisible"
    :append-to-body="true"
    :close-on-click-modal="false"
    width="800px"
    @close="handleCancel">
    <span slot="title" class="el-dialog__title">
      新增期末结转凭证模板
      <i
        v-if="helpObj"
        class="wk wk-icon-fill-help wk-help-tips"
        :data-type="helpObj.dataType"
        :data-id="helpObj.dataId" />
    </span>
    <flexbox class="tab-header">
      <el-button v-for="(item,index) in dataList" :key="index" :type="activeTemplateType==item.templateType?'selected':''" @click="activeTemplateType=item.templateType">
        {{ item.tabName }}
      </el-button>
    </flexbox>
    <div v-for="(tab,index) in dataList" v-show="activeTemplateType==tab.templateType" :key="index" class="tpl-list">
      <div
        v-for="item in tab.list"
        :key="item.templateId"
        :class="{active: getActiveStatus(item)}"
        class="tpl-list-item"
        @click="handleChoose(item)">
        {{ item.digestContent }}
        <i class="wk wk-edit edit-btn" @click.stop="handleAddCategory(item)" />
      </div>
      <div
        class="tpl-list-item add-btn"
        @click="handleAddCategory()">
        <i class="el-icon-plus" />
      </div>
    </div>
    <!-- <el-tabs>
      <el-tabs
        v-model="activeTemplateType"
        type="border-card">
        <el-tab-pane
          v-for="tab in dataList"
          :key="tab.templateType"
          :name="tab.templateType"
          :label="tab.tabName">
          <div class="tpl-list">
            <div
              v-for="item in tab.list"
              :key="item.templateId"
              :class="{active: getActiveStatus(item)}"
              class="tpl-list-item"
              @click="handleChoose(item)">
              {{ item.digestContent }}
              <i class="wk wk-edit edit-btn" @click.stop="handleAddCategory(item)" />
            </div>
            <div
              class="tpl-list-item add-btn"
              @click="handleAddCategory()">
              <i class="el-icon-plus" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-tabs> -->
    <span
      slot="footer"
      class="dialog-footer">
      <el-button
        v-debounce="handleConfirm"
        type="primary">保存</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </span>

    <update-tpl-dialog
      v-if="addOrUpdateDialogVisible"
      :visible.sync="addOrUpdateDialogVisible"
      :tpl-data="selectedTpl"
      :tpl-type="activeTemplateType"
      @confirm="handleConfirmUpdate" />
  </el-dialog>
</template>

<script>
import {
  queryTplListByTypeAPI,
  querySubjectListByTplIdAPI,
  addStatementAPI
} from '@/api/fm/settleAccounts'

import UpdateTplDialog from './UpdateTplDialog'

export default {
  name: 'ChooseTplDialog',
  components: {
    UpdateTplDialog
  },
  props: {
    visible: {
      type: Boolean,
      required: true,
      default: false
    },
    helpObj: Object
  },
  data() {
    return {
      loading: false,
      dialogVisible: false,
      activeTemplateType: null,
      dataList: [],

      selectedTpl: null, // 选中的模版
      addOrUpdateDialogVisible: false // 分类弹窗标志
    }
  },
  computed: {},
  watch: {
    visible: {
      handler() {
        this.dialogVisible = this.visible
      },
      deep: true
    },
    activeTemplateType: {
      handler(val) {
        const findRes = this.dataList.find(o => o.templateType === val)
        if (findRes && findRes.list.length === 0) {
          this.getTplList(this.activeTemplateType)
        }
      },
      deep: true
    }
  },
  mounted() {
    this.dialogVisible = this.visible
    this.getData()
  },
  methods: {
    /**
     * @description: 获取数据
     */
    getData() {
      this.dataList = [
        { templateType: '1', tabName: '日常开支', list: [] },
        { templateType: '2', tabName: '采购销售', list: [] },
        { templateType: '3', tabName: '往来款（含个人借款）', list: [] },
        { templateType: '4', tabName: '转账业务', list: [] }
        // { templateType: '5', tabName: '高级结转', list: [] }
      ]
      this.activeTemplateType = this.dataList[0].templateType
    },

    /**
     * 根据模版类型查询模版
     * @param type
     */
    getTplList(type) {
      this.loading = true
      queryTplListByTypeAPI({
        type
      }).then(res => {
        this.loading = false
        const findRes = this.dataList.find(o => o.templateType === this.activeTemplateType)
        if (findRes) {
          this.$set(findRes, 'list', res.data || [])
        }
      }).catch(() => {
        this.loading = false
      })
    },

    /**
     * 根据模板id获取模版下的科目
     */
    getTplSubjectByTplId(templateId) {
      return new Promise((resolve, reject) => {
        querySubjectListByTplIdAPI({
          templateId: templateId
        }).then(res => {
          resolve(res.data || [])
        }).catch(() => {
          reject()
        })
      })
    },

    /**
     * 判断模版勾选状态
     * @param item
     * @return {boolean}
     */
    getActiveStatus(item) {
      if (!this.selectedTpl) return false
      return this.selectedTpl.templateId === item.templateId
    },

    /**
     * 选择分类
     * @param item
     */
    handleChoose(item) {
      this.selectedTpl = item
    },

    /**
     * 取消选择
     */
    handleCancel() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
    },

    /**
     * 确定选择
     */
    async handleConfirm() {
      console.log('this.selectedTpl', this.selectedTpl)
      const params = {
        statementType: this.activeTemplateType,
        statementName: this.selectedTpl.digestContent,
        isEndOver: this.selectedTpl.isEndOver
      }
      this.loading = true
      try {
        params.subjectList = await this.getTplSubjectByTplId(this.selectedTpl.templateId)
        addStatementAPI(params).then(res => {
          this.loading = false
          this.$emit('confirm')
          this.handleCancel()
        }).catch(() => {
          this.loading = false
        })
      } catch (e) {
        this.loading = false
      }
    },

    /**
     * 点击添加/编辑分类
     */
    handleAddCategory(data = null) {
      this.addOrUpdateDialogVisible = true
      this.selectedTpl = data
    },

    /**
     * 更新分类
     */
    handleConfirmUpdate(val) {
      this.getTplList(this.activeTemplateType)
      this.selectedTpl.isEndOver = val
    }
  }
}
</script>

<style scoped lang="scss">
::v-deep .el-dialog__body {
  padding: 0;
}

::v-deep .el-tabs__header {
  margin: 0;
}

::v-deep .el-tabs--border-card {
  border-bottom: 0 none;
}

.tab-header {
  padding: 8px 16px;
  padding-bottom: 0;
}

.tpl-list {
  width: 100%;
  min-height: 200px;
  max-height: 50vh;
  padding: 15px;
  overflow: auto;

  .tpl-list-item {
    position: relative;
    float: left;
    padding: 8px 25px;
    margin: 5px 10px;
    font-size: 14px;
    cursor: pointer;
    border: 1px solid $--border-color-base;
    border-radius: $--border-radius-base;

    .wk-edit {
      position: absolute;
      top: 50%;
      right: 5px;
      font-size: 14px;
      cursor: pointer;
      visibility: hidden;
      transform: translateY(-50%);
    }

    &:hover,
    &.active {
      color: white;
      background-color: $--color-primary;
      border-color: $--color-primary;
    }

    &:hover .edit-btn {
      visibility: initial;
    }
  }

  .add-btn {
    width: 100px;
    text-align: center;
    border-style: dashed;
  }
}
</style>
