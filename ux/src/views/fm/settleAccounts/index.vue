<template>
  <flexbox
    v-loading="loading"
    :gutter="0"
    align="flex-start"
    justify="flex-start"
    direction="column"
    class="app-container">
    <div class="main-header">
      <span class="main-title">
        结账
        <i
          class="wk wk-icon-fill-help wk-help-tips"
          data-type="41"
          data-id="323" />
      </span>
      <div class="buttons">
        <el-button @click="getData">
          重新测算
          <i
            style="color: #344563;background: #f4f5f7;"
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="41"
            data-id="330" />
        </el-button>
        <el-button
          v-if="$auth('finance.checkOut.generate')"
          type="primary"
          @click="handleToVoucher()">生成凭证</el-button>
        <el-button
          v-if="$auth('finance.checkOut.checkOut')"
          type="primary"
          @click="handleToSettleAccounts(1)">结账</el-button>
        <el-button
          v-if="$auth('finance.checkOut.cancelClosing') && showBackSettle"
          type="primary"
          @click="handleToSettleAccounts(2)">反结账</el-button>
      </div>
    </div>
    <div class="wrapper">
      <flexbox class="header">
        <div class="info text-one-ellipsis">
          {{ infoText }}
        </div>
        <el-checkbox
          v-model="checkedAll"
          :indeterminate="checkedHarf"
          @change="handleChangeAllChecked">全选</el-checkbox>
      </flexbox>

      <flexbox
        align="flex-start"
        justify="flex-start"
        wrap="wrap"
        class="body">
        <flexbox
          v-for="(item, index) in settleData.statements"
          :key="index"
          align="center"
          justify="center"
          direction="column"
          class="list-item">
          <div class="box">
            <flexbox class="box-title">
              <el-checkbox v-model="item.checked" @change="handleChangeItemChecked" />
              <flexbox-item class="type">
                {{ item.statementName }}
                <i
                  v-if="getDataId(item.statementType)"
                  class="wk wk-icon-fill-help wk-help-tips"
                  data-type="41"
                  :data-id="getDataId(item.statementType)" />
              </flexbox-item>
              <el-button
                icon="wk wk-icon-setup"
                type="text"
                @click="handleToEdit(item)" />
            </flexbox>

            <div class="box-content">
              <div><span class="num">{{ item.balance | separator }}</span></div>
              <flexbox justify="space-between" class="des">
                <div class="unit">金额</div>
                <div
                  v-if="item.certificates && item.certificates.length > 0"
                  class="name">
                  <span
                    v-for="cert in item.certificates"
                    :key="cert.certificateId"
                    class="certificate-text"
                    @click="handleToVoucherDetail(cert)">
                    {{ cert.certificateNum }}
                  </span>
                </div>
              </flexbox>
            </div>
            <div class="box-handle">
              <el-button
                v-if="item.certificates && item.certificates.length > 0 && $auth('finance.checkOut.generate')"
                type="text"
                class="create"
                @click="handleToVoucher(item)">
                重新生成
              </el-button>
              <el-button
                v-else-if="$auth('finance.checkOut.generate')"
                type="text"
                class="create"
                @click="handleToVoucher(item)">
                生成凭证
              </el-button>
            </div>
          </div>
        </flexbox>
        <flexbox
          align="center"
          justify="center"
          direction="column"
          class="list-item">
          <div class="box add-box" @click="handleAddTpl">
            <i class="el-icon-plus" />
          </div>
          <div>期末结转凭证模板</div>
        </flexbox>
      </flexbox>
    </div>

    <choose-tpl-dialog
      v-if="addTplDialogVisible"
      :visible.sync="addTplDialogVisible"
      :tpl-item="statementItem"
      :help-obj="helpObj.choose"
      @confirm="getData()" />

    <statement-detail-dialog
      v-if="updateDialogVisible && statementItem && statementItem.statementType !== 2"
      :visible.sync="updateDialogVisible"
      :statement-item="statementItem"
      @confirm="getData()" />

    <profit-and-loss-dialog
      v-if="updateDialogVisible && statementItem && statementItem.statementType === 2"
      :visible.sync="updateDialogVisible"
      :statement-item="statementItem"
      :statement-time="settleData.settleTime"
      :help-obj="helpObj.profit"
      @confirm="getData()" />

    <settle-dialog
      v-if="settleDialogVisible"
      :visible.sync="settleDialogVisible"
      :settle-time="settleData.settleTime"
      :settle-type="settleType"
      :settle-ids="settleIds"
      @confirm="getData()" />
  </flexbox>
</template>

<script>
import {
  queryStatementListAPI,
  statementCertificateAPI
} from '@/api/fm/settleAccounts'

import ChooseTplDialog from './components/ChooseTplDialog' // 选择模版
import ProfitAndLossDialog from './components/ProfitAndLossDialog' // 结转损益详情
import StatementDetailDialog from './components/StatementDetailDialog'
import SettleDialog from './components/SettleDialog' // 结账/反结账

import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'SettleAccounts',
  components: {
    StatementDetailDialog,
    ChooseTplDialog,
    ProfitAndLossDialog,
    SettleDialog
  },
  data() {
    return {
      loading: false,
      checkedAll: false,
      settleData: {
        settleTime: '',
        statements: []
      },

      addTplDialogVisible: false,
      statementItem: {},
      updateDialogVisible: false,

      settleDialogVisible: false,
      settleType: null,
      settleIds: [],
      helpObj: {
        choose: {
          dataType: 41,
          dataId: 329
        },
        profit: {
          dataType: 41,
          dataId: 327
        }
      }
    }
  },
  computed: {
    ...mapGetters(['financeTimeRange', 'financeCurrentAccount']),
    infoText() {
      const time = this.$moment(this.settleData.settleTime)
      return `${time.year()}年第${time.month() + 1}期共录入凭证 ${this.settleData.number || 0} 张`
    },
    checkedHarf() {
      const len = this.settleData.statements.filter(o => o.checked).length
      if (len === 0) return false
      return len !== this.settleData.statements.length
    },
    showBackSettle() {
      if (
        !this.financeCurrentAccount ||
        !this.settleData.settleTime
      ) return false

      const enableTime = this.$moment(this.financeCurrentAccount.enableTime).format('YYYYMM')
      const nowTime = this.$moment(this.settleData.settleTime).format('YYYYMM')
      if (nowTime === enableTime) {
        return false
      }
      return true
    }
  },
  mounted() {
    this.getData()
    this.getVoucherTimeRange()
  },
  methods: {
    ...mapActions(['getVoucherTimeRange']),
    /**
     * @description: 获取结账数据
     */
    getData() {
      this.loading = true
      queryStatementListAPI().then(res => {
        this.loading = false
        this.settleData = res.data
        this.settleData.statements.forEach(item => {
          this.$set(item, 'checked', false)
        })
      }).catch(() => {
        this.loading = false
      })
    },
    /**
     * @description: 获取详情的id
     * @param {string} value
     */
    getDataId(value) {
      return {
        3: 324,
        4: 325,
        5: 326,
        2: 328
      }[value]
    },
    /**
     * @description: 跳转到凭证详情
     * @param {*} item
     * @return {*}
     */
    handleToVoucherDetail(item) {
      this.$router.push({
        path: '/fm/voucher/subs/create',
        query: {
          ids: item.certificateId,
          index: 0
        }
      })
    },

    /**
     * @description: 切换全选状态
     */
    handleChangeAllChecked() {
      this.settleData.statements.forEach(item => {
        this.$set(item, 'checked', this.checkedAll)
      })
    },

    /**
     * @description: 切换单个选中状态
     * @param {*}
     * @return {*}
     */
    handleChangeItemChecked() {
      const len = this.settleData.statements.filter(o => o.checked).length
      if (len === 0) {
        this.checkedAll = false
        return
      }
      this.checkedAll = len === this.settleData.statements.length
    },

    /**
     * @description: 结账/反结账
     * @param {*} type
     * @return {*}
     */
    handleToSettleAccounts(type) {
      const arr = this.settleData.statements.filter(o => o.checked)
      if (arr.length === 0) {
        this.settleIds = this.settleData.statements.map(o => o.statementId)
      } else {
        this.settleIds = arr.map(o => o.statementId)
      }
      this.settleType = type
      this.settleDialogVisible = true
    },

    /**
     * @description: 生成凭证
     * @param {*} item
     */
    handleToVoucher(item) {
      let params = {}
      if (!item) {
        const arr = this.settleData.statements.filter(o => o.checked)
        let ids = []
        if (arr.length === 0) {
          ids = this.settleData.statements.map(o => o.statementId)
        } else {
          ids = arr.map(o => o.statementId)
        }
        params = {
          certificateTime: this.settleData.settleTime,
          statementIds: ids
        }
      } else {
        params = {
          certificateTime: this.settleData.settleTime,
          statementIds: [item.statementId]
        }
      }

      this.loading = true
      statementCertificateAPI(params).then(res => {
        this.loading = false
        this.getData()
        this.getVoucherTimeRange()
        this.$message.success('操作成功')
      }).catch(() => {
        this.loading = false
      })
    },

    /**
     * @description:编辑某一项
     * @param {*} item
     */
    handleToEdit(item) {
      if (
        item.statementType === 2 &&
        !this.$auth('finance.checkOut.profitAndLoss')
      ) {
        this.$message.error('权限不足')
        return
      }
      this.statementItem = item
      this.updateDialogVisible = true
    },

    /**
     * @description: 添加转账模版
     */
    handleAddTpl() {
      this.addTplDialogVisible = true
    }
  }
}
</script>

<style scoped lang="scss">
.app-container {
  padding: 32px 40px 0;

  .main {
    position: relative;

    &-header {
      display: flex;
      justify-content: space-between;
      width: 100%;
      height: 32px;
    }

    &-title {
      height: 100%;
      font-size: $--font-size-xxlarge;
      color: $--color-black;
    }
  }

  .buttons {
    font-size: 0;

    .el-button {
      margin-left: $--button-padding-vertical;
    }
  }

  .create {
    &:hover {
      color: $--color-primary;
    }
  }

  .wrapper {
    width: 100%;
    height: 100%;
    margin-top: 24px;
    background-color: white;

    .header {
      width: 100%;

      .info {
        height: 32px;
        margin-right: 16px;
        font-size: 16px;
        line-height: 32px;
      }
    }

    .body {
      margin-top: 8px;

      .list-item {
        width: 20%;
        padding-left: $--interval-base;

        .box {
          width: 100%;
          padding: 0 8px 8px;
          margin-bottom: 8px;
          overflow: hidden;
          background-color: $--background-color-base;
          border-radius: $--border-radius-base;

          .box-title {
            width: 100%;
            height: 40px;
            padding: 0 8px;

            .type {
              margin-right: 5px;
              overflow: hidden;
              font-size: $--font-size-small;
              color: $--color-text-secondary;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            ::v-deep .wk-icon-full-setting {
              font-size: 14px;
            }
          }

          .box-content {
            width: 100%;
            height: 68px;
            padding: 16px;
            overflow: hidden;
            background-color: white;
            border-radius: $--border-radius-base;
            box-shadow: $--box-shadow-bottom;

            .num {
              font-size: 16px;
            }

            .des {
              margin-top: 6px;
            }

            .unit {
              font-size: $--font-size-small;
              color: $--color-text-secondary;
            }

            .name {
              font-size: $--font-size-small;
              text-align: right;

              .certificate-text {
                padding: 3px;
                color: $--color-primary;
                cursor: pointer;

                &:hover {
                  text-decoration: underline;
                }
              }
            }
          }

          &-handle {
            margin-top: $--interval-base;
            text-align: right;
          }
        }

        .add-box {
          height: 134px;
          font-size: 36px;
          line-height: 134px;
          text-align: center;
          cursor: pointer;
          border: 1px dashed $--border-color-base;
        }
      }

      .list-item:first-child {
        padding-left: 0;
      }
    }
  }
}
</style>
