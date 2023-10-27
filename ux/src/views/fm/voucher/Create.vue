<template>
  <div v-loading="loading" class="app-container">
    <div class="main">
      <div class="main-header">
        <span class="main-title">
          录凭证
          <i
            class="wk wk-icon-fill-help wk-help-tips"
            data-type="37"
            data-id="302" />
        </span>
      </div>
      <div class="content-button">
        <template
          v-if="
            certificateIds.length &&
              (certificateIndex || certificateIndex === 0)
          "
        >
          <el-button
            v-if="$auth('finance.voucher.save')"
            type="primary"
            @click="resetAdd"
          >新增</el-button
          >
          <el-button
            v-if="!isPass && canSave"
            @click="saveSubmit(false)"
          >保存</el-button
          >
          <!-- <el-button @click="printClick">打印</el-button> -->
          <template v-if="!info.isStatement">
            <el-button
              v-if="info.checkStatus && $auth('finance.voucher.noExamine')"
              @click="updateCheckStatus"
            >
              反审核
            </el-button>
            <el-button
              v-if="!info.checkStatus && $auth('finance.voucher.examine')"
              @click="updateCheckStatus"
            >
              审核
            </el-button>
            <el-button
              v-if="$auth('finance.voucher.delete')"
              @click="deleteOne"
            >
              删除
            </el-button>
          </template>
          <el-button v-if="$auth('finance.voucher.save')" @click="copyOne">
            复制
          </el-button>
          <el-button v-if="$auth('finance.voucher.save')" @click="print">
            打印
          </el-button>
          <el-button v-if="fromSearch" @click="back"> 返回 </el-button>
        </template>
        <template v-else>
          <el-button
            v-if="!isPass && canSave"
            type="primary"
            @click="saveSubmit(true)"
          >保存并新增</el-button
          >
          <el-button
            v-if="!isPass && canSave"
            @click="saveSubmit(false)"
          >保存</el-button
          >
          <!-- <el-button @click="printClick">打印</el-button> -->
          <el-dropdown
            trigger="click"
            class="receive-drop"
            @command="templateClick"
          >
            <el-button class="dropdown-btn" icon="el-icon-more" />
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="save">保存为模板</el-dropdown-item>
              <el-dropdown-item
                v-if="!isPass"
                command="use"
              >使用模板</el-dropdown-item
              >
            </el-dropdown-menu>
          </el-dropdown>
        </template>
        <div class="button-right">
          <shortcut-keys class="key" />
          <el-button-group class="wk-header-page-btn">
            <el-button
              :disabled="
                !(
                  certificateIds[certificateIndex - 1] ||
                  (certificateIds.length && certificateIndex === '')
                )
              "
              type="subtle"
              title="上一张"
              icon="el-icon-arrow-left"
              @click="changeCertificateIndex(-1)"
            />
            <el-button
              :disabled="!certificateIds[certificateIndex + 1]"
              type="subtle"
              title="下一张"
              icon="el-icon-arrow-right"
              @click="changeCertificateIndex(1)"
            />
          </el-button-group>
        </div>
      </div>

      <div id="note" class="note">
        <div class="note-header">
          <div class="note-title">
            <div>记账凭证</div>
            <span class="note-title-time">{{ voucherTime }}</span>
          </div>
        </div>
        <flexbox class="content-flex">
          <div class="content-flex-item">
            凭证字
            <el-select
              v-model="info.voucherId"
              :disabled="isPass"
              @input="getVoucherNum"
            >
              <el-option
                v-for="(item, index) in voucherOptions"
                :key="index"
                :label="item.voucherName"
                :value="item.voucherId"
              />
            </el-select>
            <el-input
              v-model="info.certificateNum"
              :disabled="isPass"
              type="number"
            >
              <template slot="append">号</template>
            </el-input>
            <el-date-picker
              v-model="info.certificateTime"
              :picker-options="datePicker"
              :disabled="isPass"
              :clearable="false"
              value-format="yyyy-MM-dd"
              type="date"
              placeholder="选择日期"
              @change="getVoucherNum"
            />
          </div>

          <div class="right content-flex-item">
            付单据
            <!-- <el-input-number v-model="info.fileNum" controls-position="right"  :disabled="isPass" :min="0" ><template slot="append">张</template></el-input-number> -->
            <el-input
              :value="info.fileNum"
              :disabled="isPass"
              type="number"
              :maxlength="10"
              @input="fileNumChange"
            >
              <template slot="append">张</template>
            </el-input>
          </div>
        </flexbox>
        <div class="subject-table">
          <table id="voucher" class="voucher" cellpadding="0" cellspacing="0">
            <thead>
              <tr>
                <th class="col_operate" />
                <th class="col_summary">摘要</th>
                <th class="col_subject">会计科目</th>
                <th v-if="showNumColumn" class="col_quantity">数量</th>
                <th class="col_money">
                  <strong class="tit">借方金额</strong>
                  <div class="money_unit">
                    <span v-for="(item, index) in moneyUnits" :key="index">{{
                      item
                    }}</span>
                  </div>
                </th>
                <th class="col_money">
                  <strong class="tit">贷方金额</strong>
                  <div class="money_unit">
                    <span v-for="(item, index) in moneyUnits" :key="index">{{
                      item
                    }}</span>
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in list" :key="index" class="entry_item">
                <td class="col_operate">
                  <el-dropdown
                    trigger="hover"
                    placement="right-start"
                    class="receive-drop"
                  >
                    <i class="wk wk-add" />
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item
                        @click.native="addLine(index)"
                      >从上方插入行</el-dropdown-item
                      >
                      <el-dropdown-item
                        @click.native="addLine(index + 1)"
                      >从下方插入行</el-dropdown-item
                      >
                    </el-dropdown-menu>
                  </el-dropdown>
                  <!-- <el-popover
                    placement="top"
                    width="200"
                    trigger="hover">
                    <div class="add-box">
                      <div class="add-item" @click="addLine(index)">从上方插入行</div>
                      <div class="add-item" @click="addLine(index + 1)">从下方插入行</div>
                    </div>
                    <i slot="reference" class="wk wk-add" />
                  </el-popover> -->
                  <!-- <i class="wk wk-add" @click="addLine(index)" /> -->
                  <i class="wk wk-delete" @click="deleteLine(index)" />
                </td>
                <td class="col_summary" data-edit="summary" tabindex="-1" @focus="editSummaryFocus(item,index,$event)">
                  <el-input
                    v-if="item.edit_summary"
                    :ref="index + 'summary'"
                    v-model="item.digestContent"
                    :disabled="isPass"
                    class="edit_summary"
                    @blur="inputBlur(index, 'summary',item)"
                    @focus="fillDigest(item, index)"
                  />
                  <div v-else>
                    {{ item.digestContent }}
                  </div>
                  <el-button
                    class="btn"
                    type="primary-text"
                    @click="selectDigestOption(item)"
                  >摘要</el-button
                  >
                </td>
                <td class="col_subject" data-edit="subject">
                  <subject-select-options
                    v-if="subjectList&&subjectList.length"
                    :ref="index + 'subject'"
                    v-model="item.subjectId"
                    :style="{ opacity: coord == index + 'subject' ? 1 : 0 }"
                    :subject-object="item.subject"
                    :no-use-disabled="true"
                    :disabled="isPass"
                    :relative="item.associationBOS"
                    :is-relevance="true"
                    :subject-list="subjectList"
                    :before-choose="beforeChooseSubject"
                    :filter-node-method="filterSubject"
                    :help-obj="helpObj.subject"
                    can-create
                    @focus="inputFocus(index, 'subject')"
                    @blur="inputBlur(index, 'subject')"
                    @update-list="getSubjectList"
                    @change="subjectSelect(arguments, item)"
                  />
                  <div
                    v-show="coord != index + 'subject'"
                    class="cell_val subject_val"
                    @click="inputFocus(index, 'subject')"
                  >
                    {{ itemSubjectName(item) }}
                  </div>

                  <div class="balance">
                    {{ itemBalance(item) }}
                  </div>
                </td>
                <td v-if="showNumColumn" class="col_quantity">
                  <div
                    v-if="item.subject.isAmount"
                    class="cell_val quantity_val"
                  >
                    <p>
                      数量：
                      <el-input
                        v-model="item.quantity"
                        v-WkNumber="'positiveFloat'"
                        :disabled="isPass"
                        class="mini-input"
                        @change="countMoney(item)"
                      />
                      <span v-if="item.subject.amountUnit">{{
                        item.subject.amountUnit
                      }}</span>
                    </p>
                    <p style="margin-top: 4px;">
                      单价：
                      <el-input
                        v-model="item.price"
                        v-WkNumber="'positiveNum'"
                        :disabled="isPass"
                        class="mini-input"
                        oninput="setTimeout(()=>{value=value.replace(/^(0+|(-\d+))$/,'')},1000)"
                        @change="countMoney(item)"
                      />
                    </p>
                  </div>
                </td>
                <td
                  class="col_debite"
                  data-edit="money"
                  @click="inputFocus(index, 'depite')"
                >
                  <el-input
                    :ref="index + 'depite'"
                    v-model="item.debtorBalance"
                    :style="{ opacity: coord == index + 'depite' ? 1 : 0 }"
                    :disabled="isPass"
                    type="number"
                    class="edit_money"
                    @focus="inputFocus(index, 'depite')"
                    @keyup.native="balanceMoney($event, item, 'debtorBalance')"
                    @change="moneyChange($event, item, 'debtorBalance')"
                    @blur="inputBlur(index, 'depite')"
                    @click.prevent.stop
                  />
                  <div
                    v-show="coord != index + 'depite'"
                    class="cell_val debit_val"
                  >
                    <span
                      v-for="(el, elIndex) in moneyStr(item.debtorBalance)"
                      :key="elIndex"
                      class="money-str"
                      :style="{
                        color:
                          filterMoney(item.debtorBalance) >= 0 ? '' : 'red',
                      }"
                    >
                      {{ el }}
                    </span>
                  </div>
                </td>
                <td
                  class="col_credit"
                  data-edit="money"
                  @click="inputFocus(index, 'credit')"
                >
                  <el-input
                    :ref="index + 'credit'"
                    v-model="item.creditBalance"
                    :style="{ opacity: coord == index + 'credit' ? 1 : 0 }"
                    :disabled="isPass"
                    type="number"
                    class="edit_money"
                    @focus="inputFocus(index, 'credit')"
                    @keyup.native="balanceMoney($event, item, 'creditBalance')"
                    @change="moneyChange($event, item, 'creditBalance')"
                    @blur="inputBlur(index, 'credit')"
                    @click.prevent.stop
                  />
                  <div
                    v-show="coord != index + 'credit'"
                    class="cell_val credit_val"
                  >
                    <span
                      v-for="(el, elIndex) in moneyStr(item.creditBalance)"
                      :key="elIndex"
                      class="money-str"
                      :style="{
                        color:
                          filterMoney(item.creditBalance) >= 0 ? '' : 'red',
                      }"
                    >
                      {{ el }}
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
            <tfoot>
              <tr>
                <td class="col_operate" />
                <td :colspan="showNumColumn ? 3 : 2" class="col_total">
                  合计：<span id="capAmount">{{ number_chinese }}</span>
                </td>
                <td class="col_debite">
                  <div id="debit_total" class="cell_val debit_total">
                    <span
                      v-for="(el, elIndex) in moneyStr(totalDebite)"
                      :key="elIndex"
                      class="money-str"
                      :style="{
                        color: filterMoney(totalDebite) >= 0 ? '' : 'red',
                      }"
                    >
                      {{ el }}
                    </span>
                  </div>
                </td>
                <td class="col_credit">
                  <div id="credit_total" class="cell_val credit_total">
                    <span
                      v-for="(el, elIndex) in moneyStr(totalCredit)"
                      :key="elIndex"
                      class="money-str"
                      :style="{
                        color: filterMoney(totalCredit) >= 0 ? '' : 'red',
                      }"
                    >
                      {{ el }}
                    </span>
                  </div>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
        <flexbox class="vch_ft" justify="space-between">
          <span id="vch_people_display">
            制单人：<span>{{
              info.certificateId ? info.createUserName : userInfo.realname
            }}</span>
          </span>
          <span v-if="isPass">
            审核人：<span id="ldr_people_display">{{
              info.examineUserName
            }}</span>
          </span>
        </flexbox>
      </div>

      <flexbox class="bottom-flex">
        <el-button
          v-if="!isPass && canSave"
          type="primary"
          @click="saveSubmit(true)"
        >保存并新增</el-button
        >
        <el-button
          v-if="!isPass && canSave"
          @click="saveSubmit(false)"
        >保存</el-button
        >
      </flexbox>

      <img v-if="isPass" :src="passImg" alt="" class="pass-pic">
    </div>

    <digest-select-dialog
      v-if="selectDigestDialogVisible"
      :show-dialog-visible="selectDigestDialogVisible"
      :help-obj="helpObj.digest"
      @closeOptions="selectDigestClose"
      @select-digest="selectDigestSubmit"
    />

    <voucher-template
      v-if="selectTemplateDialogVisible"
      :show-dialog-visible="selectTemplateDialogVisible"
      @close="selectTemplateDialogVisible = false"
      @select="selectTemplate"
    />

    <save-template-dialog
      v-if="saveTemplateDialogVisible"
      :show-dialog-visible="saveTemplateDialogVisible"
      :content="list"
      @close="saveTemplateDialogVisible = false"
    />
    <PrintPageSetDialog
      v-if="printSizeDialogVisible"
      :visible.sync="printSizeDialogVisible"
      @confirm="sizeConfirm"
    />
  </div>
</template>

<script>
import {
  fmFinanceVoucherListAPI,
  fmFinanceSubjectListAPI
} from '@/api/fm/setting'
import {
  fmFinanceVoucherAddAPI,
  fmFinanceVoucherQueryByIdAPI,
  fmFinanceVoucherCheckStatusAPI,
  fmFinanceCertificateQueryNumAPI,
  fmFinanceVoucherDeleteByIdsAPI
} from '@/api/fm/voucher'
import { financeCertificatePreviewFinanceAPI } from '@/api/fm/voucher.js'
// import { printPreviewAPI } from '@/api/admin/crm.js'
import SubjectSelectOptions from '@/views/fm/components/subject/SubjectSelectOptions'
import SaveTemplateDialog from './components/SaveTemplateDialog'
import VoucherTemplate from './components/VoucherTemplate'
import ShortcutKeys from '../components/ShortcutKeys'
import PrintPageSetDialog from '@/views/fm/voucher/components/PrintPageSetDialog.vue'

import { debounce } from 'throttle-debounce'
import { isEmpty } from '@/utils/types'
import {
  objDeepCopy
} from '@/utils'
import { separator } from '@/filters/vueNumeralFilter/filters'
import PrintMixin from '@/views/fm/mixins/Print.js'
import { mapActions, mapGetters } from 'vuex'
import DigestSelectDialog from './components/DigestSelectDialog.vue'
import Nzhcn from 'nzh/cn'
import { accAdd } from '@/utils/acc.js'

export default {
  name: 'VoucherCreate',
  components: {
    SubjectSelectOptions,
    VoucherTemplate,
    ShortcutKeys,
    DigestSelectDialog,
    SaveTemplateDialog,
    PrintPageSetDialog
  },
  mixins: [PrintMixin],
  data() {
    return {
      loading: false, // 展示加载中效果
      voucherOptions: [], // 凭证字选项
      debounceSaveSubmit: null,
      relationObj: {},
      info: {
        fileNum: '',
        voucherId: '',
        certificateTime: '',
        certificateNum: ''
      },
      list: [],
      helpObj: {
        subject: {
          dataType: 37,
          dataId: 304
        },
        digest: {
          dataType: 37,
          dataId: 300
        }
      },
      coord: '',
      subjectList: [], // 科目列表
      certificateIds: [], // 查看凭证详情Id列表
      selectTemp: null, // 选择科目和摘要的浅拷贝对象
      certificateIndex: '', // 当前查看凭证详情索引，空为新增
      moneyUnits: [
        '亿',
        '千',
        '百',
        '十',
        '万',
        '千',
        '百',
        '十',
        '元',
        '角',
        '分'
      ],
      selectDigestDialogVisible: false, // 摘要库弹窗
      saveTemplateDialogVisible: false, // 保存模板
      selectTemplateDialogVisible: false, // 选择模板
      fieldList: [
        { label: '摘要', name: 'digestContent' },
        { label: '会计科目', name: 'subjectNumber' },
        { label: '借方金额', name: 'debtorBalance' },
        { label: '贷方金额', name: 'creditBalance' }
      ],
      printSizeDialogVisible: false
    }
  },
  computed: {
    ...mapGetters([
      'userInfo',
      'financeCurrentAccount',
      'financeFilterTimeRange'
    ]),
    canSave() {
      if (this.certificateIndex || this.certificateIndex === 0) {
        return this.$auth('finance.voucher.update')
      } else {
        return this.$auth('finance.voucher.save')
      }
    },
    datePicker() {
      return {
        disabledDate: (time) => {
          const startTime = this.financeCurrentAccount.startTime
          console.log(
            new Date(startTime).getMonth(),
            new Date(time).getMonth()
          )
          if (
            startTime &&
            new Date(startTime).getMonth() > new Date(time).getMonth() &&
            new Date(startTime).getFullYear() === new Date(time).getFullYear()
          ) {
            return true
          } else if (
            new Date(time).getTime() <
            new Date(
              this.financeFilterTimeRange.minTime.val + ' 00:00:00'
            ).getTime()
          ) {
            return true
          }
          return false
        }
      }
    },
    // 显示数量列
    showNumColumn() {
      for (let i = 0; i < this.list.length; i++) {
        const element = this.list[i]
        if (element.subject && element.subject.isAmount) {
          return true
        }
      }
      return false
    },
    voucherTime() {
      console.log('certificateTime: ', this.info.certificateTime)
      if (this.info.certificateTime) {
        const time = this.info.certificateTime.split('-')
        return `${time[0]}年第${time[1]}期`
      }
      return ''
    },
    // 总借款
    totalDebite() {
      let money = 0
      for (let i = 0; i < this.list.length; i++) {
        console.log(this.list[i].debtorBalance * 1)
        money += this.list[i].debtorBalance * 1
      }
      return money.toFixed(2)
    },
    // 计算合计中文大写
    number_chinese() {
      if (!this.totalDebite * 1 || !this.totalCredit * 1) {
        return ''
      }
      if (this.totalDebite !== this.totalCredit) {
        return ''
      }
      return Nzhcn.toMoney(Number(this.totalDebite), {
        outSymbol: false
      })
    },
    fromSearch() {
      const copyObj = objDeepCopy(this.$route.query)
      delete copyObj.hideTab
      return !isEmpty(copyObj)
    },
    // 总贷款
    totalCredit() {
      let money = 0
      for (let i = 0; i < this.list.length; i++) {
        console.log(this.list[i].creditBalance * 1)
        money += this.list[i].creditBalance * 1
      }
      return money.toFixed(2)
    },

    // 通过审核
    isPass() {
      if (this.info.isStatement) return true
      return Number(this.info.checkStatus) === 1
    },
    // 审核通过后的图标
    passImg() {
      if (!this.isPass) return ''
      if (this.info.isStatement) return require('@/assets/img/checkout.png')
      return require('@/assets/img/audit.png')
    },
    // 获取凭证字
    certificateNum() {
      const findRes = this.voucherOptions.find(item => {
        return item.voucherId == this.info.voucherId
      })
      if (findRes) {
        return findRes.voucherName
      } else {
        return ''
      }
    }
  },
  watch: {
    'info.certificateTime'() {
      this.getSubjectList()
    },
    financeCurrentAccount() {
      if (!this.certificateIndex && this.certificateIndex !== 0) {
        this.getInfoTime()
      }
    },
    '$route.query': {
      handler(val) {
        if (isEmpty(val)) {
          this.resetAdd()
        }
      },
      deep: true,
      immediate: true
    }
  },
  created() {
    this.initRouterQuery()
    window.addEventListener('keydown', this.shortcutKeysDown)
    if (!this.certificateIndex && this.certificateIndex !== 0) {
      this.initForm()
      this.getInfoTime()
    }
    this.debounceSaveSubmit = debounce(1000, this.saveSubmit)
    this.getVoucherOption()
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.shortcutKeysDown)
  },
  methods: {
    /**
     * 设置大小弹窗
     */
    sizeConfirm(styleData) {
      console.log(styleData)
      this.printSubmit(styleData)
    },
    print() {
      this.printSizeDialogVisible = true
    },
    editSummaryFocus(item, index, e) {
      console.log(e)
      this.$set(item, 'edit_summary', true)
      this.$nextTick(() => {
        this.$refs[index + 'summary'][0].focus()
      })
    },
    printSubmit(styleData) {
      const copyValue = JSON.parse(JSON.stringify(this.info))
      const copyList = JSON.parse(JSON.stringify(this.list))
      const dataList = [{ ...copyValue, certificateDetails: copyList, certificateNum: this.certificateNum + '_' + this.info.certificateNum }]
      const newDataList = []
      for (let i = 0; i < dataList.length; i++) {
        const el = dataList[i]
        if (el.certificateDetails?.length) {
          el.totalDebite = this.computeTotal('totalDebite', el)
          el.totalCredit = this.computeTotal('totalCredit', el)
          if (el.certificateDetails.length <= 4) {
            const count = 4 - el.certificateDetails.length
            for (let k = 0; k < count; k++) {
              el.certificateDetails.push({ })
            }
            newDataList.push({ ...el, currentPage: 1, totalPage: 1 })
          } else {
            let arr = []
            let currentPage = 0
            const totalPage = Math.ceil(el.certificateDetails.length / 4)
            for (let j = 0; j < el.certificateDetails.length; j++) {
              const element = el.certificateDetails[j]
              if (j && (j % 4 == 0)) {
                newDataList.push({ ...el, certificateDetails: arr, currentPage: ++currentPage, totalPage })
                arr = []
                arr.push(element)
              } else {
                arr.push(element)
              }
            }
            if (arr.length) {
              const count = 4 - arr.length
              for (let i = 0; i < count; i++) {
                arr.push({ })
              }
              newDataList.push({ ...el, certificateDetails: arr, currentPage: ++currentPage, totalPage })
            }
          }
        }
      }
      const rawHtml = `
      <head>
      <style>
        *{
          margin:0;
          padding:0;
          font-size:${styleData.fontSize || '14px'};
        }
        th,td{
          padding:20px 0;
        }
        .title{
          padding:10px 0;
        }
        .title-item{
          display:inline-block;
          width:33%;
        }
        .title-item:nth-child(2){
          text-align: center;
        }
        .title-item:nth-child(3){
          text-align: right;
        }
        .footer{
          padding:10px 0;
        }
        .footer-item{
          width:24%;
          display:inline-block;
        }
        .footer-item:nth-child(2){
          text-align: center;
        }
        .footer-item:nth-child(3){
          text-align: center;
        }
        .footer-item:nth-child(4){
          text-align: right;
        }
      </style>
      ${newDataList.map(selected => (

    `<div style="font-size: 20px;">
      <div style="font-size: 30px;text-align: center;">记账凭证</div>
      <div style="width: 200px;border-bottom: 1px solid;border-top: 1px solid;height: 5px; margin: 10px auto;"/>
    </div>
    <div style="overflow: hidden;">
      <span style="float:right;">
        附单据&nbsp;&nbsp;${this.info.fileNum || ''}&nbsp;&nbsp;张
      </span>
    </div>
    <div class="title" style="display: flex;justify-content: space-between;padding: 5px 0;">
      <div class="title-item">
        <span>单位：${this.financeCurrentAccount.companyName}</span>
      </div>
      <div class="title-item">
        <span>日期：${this.voucherTime}</span>
      </div>
      <div class="title-item">
        <span> 凭证号：${selected.certificateNum + ' ' + '(' + selected.currentPage + '/' + selected.totalPage + ')'}</span>
      </div>
    </div>
    <table border="1" cellspacing="0" cellpadding="0" style="border-collapse: collapse;width:100%">
      <tr>
        ${this.fieldList.map(field => (
      `<th >
      ${field.label}
    </th>`
    )).join(' ')}
      </tr>
        ${selected.certificateDetails.map(data => (
      `<tr>
        ${this.fieldList.map(field => (
        `<td >
      ${this.formatter(data, field)}
    </td>`
      )).join(' ')}
      </tr>`
    )).join(' ')}

      <tr>
        <td colspan="2">合计： ${this.number_chinese} </td>
        <td> ${separator(this.totalDebite)} </td>
        <td> ${separator(this.totalCredit)} </td>
      </tr>
    </table>
    <div class="footer" style="display: flex;justify-content: space-between;padding: 5px 2px">
      <div class="footer-item">
        <span>财务主管：</span>
      </div>
      <div class="footer-item">
        <span>审核：${this.info.examineUserName}</span>
      </div>
      <div class="footer-item">
        <span>出纳：</span>
      </div>
      <div class="footer-item">
        <span>制单：${this.info.certificateId ? this.info.createUserName : this.userInfo.realname}</span>
      </div>
    </div>
    <div style="page-break-after:always;"></div>
    `
  )).join(' ')}
      `
      financeCertificatePreviewFinanceAPI({
        type: 'pdf',
        content: rawHtml,
        orientation: styleData.orientation || 'Portrait', // Landscape,
        marginLeft: `${styleData.left || 0}mm`,
        marginTop: `${styleData.top || 0}mm`,
        pageWidth: `${styleData.width || 0}mm`,
        pageHeight: `${styleData.height || 0}mm`
      }).then(res => {
        const data = res.data
        const routeData = this.$router.resolve({
          name: 'PrintPage',
          query: {
            key: data
          }
        })
        window.open(routeData.href, '_blank')
      })
      // this.HandlerPrint(rawHtml, { type: 'raw-html', style: '@page{size:b5 landscape;}' })
    },
    computeTotal(type, data) {
      let num = 0
      data.certificateDetails.forEach(item => {
        num = type == 'totalDebite' ? accAdd(num, item.debtorBalance) : accAdd(num, item.creditBalance)
      })
      return separator(num)
    },
    formatter(row, column) {
      if (column.name == 'subjectNumber') {
        return this.itemSubjectName(row)
      } else if (column.name == 'creditBalance' || column.name == 'debtorBalance') {
        if (row[column.name]) {
          return separator(row[column.name])
        }
        return ''
      }
      return row[column.name] || ' '
    },
    ...mapActions(['getVoucherTimeRange']),
    /**
     * @description: 科目列表头样式
     * @param {*} val
     * @param {*} index
     * @return {*}
     */
    headerCellStyle(val, index) {
      return { background: '#F2F2F2' }
    },
    /**
     * @description: 确认选择科目
     * @param {*} data
     * @param {*} item
     * @return {*}
     */
    subjectSelect(data, item) {
      console.log(data, item)
      // if (this.isPass) {
      //   return
      // }
      item.subject = data[1] || {}
    },

    /**
     * @description: 确认选择摘要
     * @param {*} data
     * @return {*}
     */
    selectDigestOption(data) {
      this.selectTemp = data
      this.selectDigestDialogVisible = true
    },
    /**
     * @description: 获取科目列表
     * @param {*}
     * @return {*}
     */
    getSubjectList() {
      fmFinanceSubjectListAPI({
        type: null,
        certificateTime: this.$moment(this.info.certificateTime).format('YYYYMM'),
        isTree: 5
      }).then((res) => {
        this.subjectList = res.data || []
      })
    },
    /**
     * @description: 选择科目之前的操作
     * @param {*} node
     * @return {*}
     */
    beforeChooseSubject(node) {
      if (node.children && node.children.length > 0) {
        this.$message.warning('不允许选择非明细科目')
        return false
      }
      return true
    },
    /**
     * @description: 过滤科目
     * @param {*} node
     * @param {*} children
     * @param {*} index
     */
    filterSubject(node, children, index) {
      return (!children || children.length === 0) && node.status
    },

    /**
     * @description: 金额展示
     * @param {*} data
     * @return {*}
     */
    moneyStr(data) {
      const str =
        this.filterMoney(data) === '' ? '' : Math.abs(this.filterMoney(data))
      return String(str)
    },

    /**
     * @description: 初始化添加表单
     * @param {*}
     * @return {*}
     */
    initForm() {
      this.list = [
        {
          digestContent: '',
          subject: {},
          subjectId: '',
          associationBOS: {},
          quantity: '',
          debtorBalance: '',
          creditBalance: ''
        },
        {
          digestContent: '',
          subject: {},
          subjectId: '',
          associationBOS: {},
          quantity: '',
          debtorBalance: '',
          creditBalance: ''
        },
        {
          digestContent: '',
          subject: {},
          subjectId: '',
          associationBOS: {},
          quantity: '',
          debtorBalance: '',
          creditBalance: ''
        },
        {
          digestContent: '',
          subject: {},
          subjectId: '',
          associationBOS: {},
          quantity: '',
          debtorBalance: '',
          creditBalance: ''
        }
      ]
      this.info = {
        fileNum: '',
        voucherId: '',
        certificateTime: this.info.certificateTime,
        certificateNum: ''
      }
      if (
        !this.info.certificateTime
      ) {
        this.getInfoTime()
      }
      this.getDefaultVoucher()
    },
    /**
     * @description: 拼接显示的科目名称
     * @param {*} data
     * @param {*} index
     * @return {*}
     */
    itemSubjectName(data, index) {
      if (!data.subject || isEmpty(data.subject)) {
        return ''
      }
      let str = data.subject.number + ' ' + data.subject.subjectName
      if (!data.subject.adjuvantList) {
        return str
      }

      const nameArr = Object.keys(data.associationBOS).map((key) => {
        let name = ''
        // let name = data.associationBOS[key].name
        // let name = data.associationBOS[key].carteNumber + data.associationBOS[key].name

        if (data.associationBOS[key].relationId) {
          name =
            data.associationBOS[key].carteNumber +
            data.associationBOS[key].name
          // 如果存货规格存在时 拼接上
          if (data.associationBOS[key].specification) {
            name += ' ' + data.associationBOS[key].specification
          }
        } else {
          name = data.associationBOS[key].name
        }

        // const relationList = this.relationObj[key]
        // if (relationList) {
        //   if (data.associationBOS[key].relationId) {
        //     const assistData = relationList.find(o => o.carteId == data.associationBOS[key].relationId)
        //     // if (!isEmpty(assistData)) {
        //     //   name = assistData.carteNumber + name
        //     // }
        //     // 存货加上规格
        //     if (data.associationBOS[key].label == 6 && !isEmpty(assistData)) {
        //       name = name + ' ' + assistData.specification
        //     }
        //   }
        // }

        return name
      })
      if (nameArr.length) {
        str += '_' + nameArr.join('_')
      }

      return str
    },
    /**
     * @description: 计算科目余额
     * @param {*} data
     * @return {*}
     */
    itemBalance(data) {
      if (data.subject && !isEmpty(data.subject)) {
        if (data.subject.labelName) {
          return `余额：${data.subject.adjuvantBalance || 0}`
        }
        return `余额：${data.subject.balance || 0}`
      }
      return ''
    },
    /**
     * @description: 获取系统期数
     */
    getInfoTime() {
      const accountTime = this.financeCurrentAccount.startTime
      const accountDate = accountTime
        ? this.$moment(accountTime)
        : this.$moment()

      if (accountDate.format('YYYY-MM') === this.$moment().format('YYYY-MM')) {
        this.$set(
          this.info,
          'certificateTime',
          this.$moment().format('YYYY-MM-DD')
        )
      } else {
        this.$set(
          this.info,
          'certificateTime',
          accountDate.endOf('month').format('YYYY-MM-DD')
        )
      }
      this.getVoucherNum()
    },
    /**
     * @description: 切换凭证详情
     * @param {*} data
     * @return {*}
     */
    changeCertificateIndex(data) {
      if (!this.certificateIds.length) {
        return
      }
      if (
        data == -1 &&
        !this.certificateIds[this.certificateIndex - 1] &&
        this.certificateIndex !== ''
      ) {
        this.$message.warning('没有上一张了')
        return
      } else if (data == 1 && !this.certificateIds[this.certificateIndex + 1]) {
        this.$message.warning('没有下一张了')
        return
      }
      if (data == -1 && this.certificateIndex === '') {
        this.certificateIndex = this.certificateIds.length - 1
      } else {
        this.certificateIndex = this.certificateIndex + data
      }

      this.getDetail()
    },
    /**
     * @description: 获取凭证号
     * @param {*} data
     * @return {*}
     */
    getVoucherNum(data) {
      console.log(data)
      if (!this.info.voucherId || !this.info.certificateTime) {
        return
      }
      const params = {
        voucherId: this.info.voucherId,
        settleTime: this.$moment(this.info.certificateTime).format('YYYYMM')
      }
      fmFinanceCertificateQueryNumAPI(params).then((res) => {
        this.info.certificateNum = res.data.certificateNum
      })
    },
    /**
     * @description: 模板点击
     * @param {string} type
     * @return {*}
     */
    templateClick(type) {
      console.log(type)
      if (type == 'save') {
        if (this.checkForm()) {
          this.saveTemplateDialogVisible = true
        }
      } else {
        this.selectTemplateDialogVisible = true
      }
    },

    /**
     * @description: 选择模板
     * @param {object} data
     */
    selectTemplate(data) {
      this.list = JSON.parse(data.content)
      console.log(this.list)
    },
    /**
     * @description: 详情更改为添加
     */
    resetAdd() {
      this.$router.push({
        path: '/fm/voucher/subs/create'
      })
      this.certificateIds = []
      this.certificateIndex = ''
      this.initForm()
    },
    /**
     * @description: 审核反审核
     * @param {*}
     * @return {*}
     */
    updateCheckStatus() {
      if (!this.checkForm()) {
        return
      }
      this.loading = true
      fmFinanceVoucherCheckStatusAPI({
        ids: [this.info.certificateId],
        status: this.info.checkStatus == 1 ? 0 : 1
      })
        .then((res) => {
          this.$message.success('操作成功')
          this.getDetail()
          this.getVoucherTimeRange()
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * @description: 添加一行数据
     * @param {number} index
     */
    addLine(index) {
      this.list.splice(index, 0, {
        digestContent: '',
        subject: {},
        subjectId: '',
        associationBOS: {},
        quantity: '',
        debtorBalance: '',
        creditBalance: ''
      })
    },
    /**
     * @description: 查询凭证详情
     */
    getDetail() {
      this.loading = true
      console.log(
        '请求参数',
        this.certificateIds,
        this.certificateIndex,
        this.certificateIds[this.certificateIndex]
      )
      fmFinanceVoucherQueryByIdAPI({
        certificateId: this.certificateIds[this.certificateIndex]
      })
        .then((res) => {
          this.loading = false
          this.info = res.data
          this.list = []
          this.$nextTick(() => {
            this.list = res.data.certificateDetails
              .sort((a, b) => {
                return a.sort - b.sort
              })
              .map((item) => {
                console.log(item, 'ssssddd')
                const associationBOS = {}
                if (item.associationBOS) {
                  item.associationBOS.forEach((o) => {
                    associationBOS[o.adjuvantId] = o
                  })
                }
                return {
                  ...item,
                  associationBOS,
                  subject: JSON.parse(item.subjectContent)
                }
              })
          })
        })
        .catch((err) => {
          console.log(err)
          this.certificateIds = []
          this.certificateIndex = ''
          this.initForm()
          this.loading = false
        })
    },
    /**
     * @description: 获取默认的凭证字
     */
    getDefaultVoucher() {
      for (let i = 0; i < this.voucherOptions.length; i++) {
        const element = this.voucherOptions[i]
        if (element.isDefault) {
          this.info.voucherId = element.voucherId
          this.getVoucherNum()
        }
      }
    },
    /**
     * @description:获取凭证字列表
     * @param {*}
     * @return {*}
     */
    getVoucherOption() {
      fmFinanceVoucherListAPI().then((res) => {
        this.voucherOptions = res.data
        if (!this.certificateIndex && this.certificateIndex !== 0) {
          this.getDefaultVoucher()
        }
      })
    },
    /**
     * @description: 删除一行
     * @param {number} index
     * @return {*}
     */
    deleteLine(index) {
      if (this.list.length <= 2) {
        this.$message.warning('至少保留两条分录!')
        return
      }
      this.list.splice(index, 1)
    },
    /**
     * @description:验证表单
     */
    checkForm() {
      if (!this.list.filter(
        (item) =>
          !isEmpty(item.subject) && (item.debtorBalance || item.creditBalance)
      ).length) {
        this.$message.error('请检查格式！')
        return false
      }
      if (this.totalDebite !== this.totalCredit) {
        this.$message.error('录入借贷不平！')
        return false
      }
      if (!this.list[0].digestContent) {
        this.$message.error('第一条摘要不能为空')
        return false
      }
      for (let i = 0; i < this.list.length; i++) {
        const element = this.list[i]
        if (
          !isEmpty(element.subject) &&
          !element.debtorBalance &&
          !element.creditBalance
        ) {
          this.$message.error(`第${i + 1}条借贷金额不能为空`)
          return false
        }
        if (
          element.subject &&
          element.subject.adjuvantList &&
          element.subject.adjuvantList.length
        ) {
          for (let o = 0; o < element.subject.adjuvantList.length; o++) {
            const item = element.subject.adjuvantList[o]
            if (
              !element.associationBOS[item.adjuvantId] ||
              !element.associationBOS[item.adjuvantId].relationId
            ) {
              this.$message.error(`第${i + 1}条辅助核算不能为空`)
              return false
            }
          }
        }
        if (
          (element.debtorBalance || element.creditBalance) &&
          isEmpty(element.subject)
        ) {
          this.$message.error(`第${i + 1}条科目不能为空`)
          return false
        }
      }
      return true
    },
    /**
     * @description: 输入框聚焦
     * @param {*} index
     * @param {*} type
     */
    inputFocus(index, type) {
      if (this.isPass) {
        return
      }
      if (this.coord == index + type) {
        return
      }
      this.coord = index + type
      this.$nextTick(() => {
        // console.log(this.$refs, this.coord)
        if (this.$refs[this.coord] instanceof Array) {
          this.$refs[this.coord][0].focus()
        } else {
          if (this.$refs[this.coord]) {
            this.$refs[this.coord].focus()
          }
        }
      })
    },
    /**
     * @description: 据单输入
     * @param {*} val
     * @return {*}
     */
    fileNumChange(val) {
      if (!(String(val).length > 10)) {
        if (val < 0) {
          this.info.fileNum = 0
        } else {
          this.info.fileNum = val
        }
      }
    },
    /**
     * @description: 格式化金额
     * @param {*} data
     * @return {*}
     */
    filterMoney(data) {
      if (!data || isNaN(Number(data))) return ''
      return Number(data).toFixed(2).replace('.', '')
    },

    /**
     * @description: 复制操作
     */
    copyOne() {
      if (this.checkForm()) {
        this.certificateIds = []
        this.certificateIndex = ''
        const list = objDeepCopy(
          this.list.map((o) => {
            return {
              ...o,
              certificateId: undefined,
              detailId: undefined
            }
          })
        )
        this.info.certificateTime = ''
        this.initForm()
        this.getVoucherTimeRange()
        this.list = list
      }
    },
    /**
     *@description: 删除
     */
    deleteOne() {
      this.$confirm(
        '您确认要删除此凭证吗？删除后将不可恢复，并会产生断号',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          this.loading = true
          fmFinanceVoucherDeleteByIdsAPI({ ids: [this.info.certificateId] })
            .then((res) => {
              this.$message.success('操作成功')
              if (this.fromSearch) {
                this.$router.back()
              } else {
                this.certificateIds = []
                this.certificateIndex = ''
                this.initForm()
              }
              this.getVoucherTimeRange()
              this.loading = false
            })
            .catch(() => {
              this.loading = false
            })
        })
        .catch(() => {
        })
    },
    /**
     * @description: 输入金额
     * @param {*} data
     * @param {*} item
     * @param {*} type
     */
    moneyChange(data, item, type) {
      data = (data * 1).toFixed(2)
      item[type] = data * 1
      if (type == 'creditBalance') {
        item.debtorBalance = ''
      } else {
        item.creditBalance = ''
      }
      console.log(data, type)
    },
    /**
     * @description: 自动计算金额
     * @param {*} data
     */
    countMoney(data) {
      console.log('data', data)
      if (!data.balanceDirection) {
        data.debtorBalance =
          Math.floor((data.quantity || 0) * (data.price || 0) * 100) / 100
        data.creditBalance = ''
      } else {
        data.debtorBalance = ''
        data.creditBalance =
          Math.floor((data.quantity || 0) * (data.price || 0) * 100) / 100
      }
    },
    /**
     * @description:平衡金额
     * @param {*} key
     * @param {*} data
     * @param {*} type
     * @return {*}
     */
    balanceMoney(key, data, type) {
      if (key.key === '=' && this.totalCredit !== this.totalDebite) {
        if (Number(this.totalCredit) > Number(this.totalDebite)) {
          if (type == 'creditBalance') {
            data[type] =
              0 - (Number(this.totalCredit) - Number(this.totalDebite))
            data.debtorBalance = 0
          } else {
            data.creditBalance = 0
            data.debtorBalance =
              Number(this.totalCredit) - Number(this.totalDebite)
          }
        } else {
          if (type == 'creditBalance') {
            data[type] =
              0 - (Number(this.totalCredit) - Number(this.totalDebite))
            data.debtorBalance = 0
          } else {
            data.creditBalance = 0
            data.debtorBalance =
              Number(this.totalCredit) - Number(this.totalDebite)
          }
        }
      }
    },
    /**
     * @description: 初始化路由获取详情数据
     */
    initRouterQuery() {
      if (this.fromSearch) {
        const ids = this.$route.query.ids
        if (typeof ids == 'string') {
          this.certificateIds = this.$route.query.ids.split(',')
        } else if (typeof this.$route.query.ids == 'number') {
          this.certificateIds.push(this.$route.query.ids)
        } else {
          this.certificateIds = [this.$route.query.ids]
        }
        this.certificateIndex = this.$route.query.index
        this.getDetail()
      }
    },
    /**
     * @description: 路由回退
     */
    back() {
      this.$router.back()
    },
    /**
     * @description: 选择摘要关闭
     */
    selectDigestClose() {
      this.selectTemp = null
      this.selectDigestDialogVisible = false
    },
    /**
     * @description: 选择摘要提交
     * @param {*} data
     */
    selectDigestSubmit(data) {
      if (this.isPass) {
        return
      }
      this.selectTemp.digestContent = data.digestContent
      this.selectDigestClose()
    },
    /**
     * @description: 双击自动填充摘要
     * @param {*} data
     * @param {number|string} index
     * @return {*}
     */
    fillDigest(data, index) {
      this.inputFocus(index, 'summary')
      if (this.isPass || index === 0) {
        return
      }
      if (!data.digestContent && this.list[index - 1].digestContent) {
        data.digestContent = this.list[index - 1].digestContent
        // 选中文本
        this.$nextTick(() => {
          this.$refs[index + 'summary'][0].select()
        })
      }
    },
    /**
     * @description: 输入框失去焦点操作
     * @param {number} index
     * @param {string} type
     */
    inputBlur(index, type, item) {
      if (this.coord == index + type) {
        this.coord = ''
      }
      if (type == 'summary') {
        item.edit_summary = false
      }
    },
    /**
     * 打印
     */
    // printClick() {
    //   printHTML('note')
    // },

    /**
     * @description: 添加保存
     * @param {*} again
     */
    saveSubmit(again = false) {
      if (Number(this.info.statementType) === 2) {
        this.$message.error('结转损益的凭证不允许编辑')
        return
      }
      if (!this.info.voucherId) {
        this.$message.error('请选择凭证字')
        return
      }
      if (!this.info.certificateNum) {
        this.$message.error('请选择凭证字号')
        return
      }
      if (!this.info.certificateTime) {
        this.$message.error('请选择凭日期')
        return
      }
      if (!this.checkForm()) {
        return
      }
      const list = this.list
        .filter(
          (item) =>
            !isEmpty(item.subject) && (item.debtorBalance || item.creditBalance)
        )
        .map((item, index) => {
          const subjectParams = objDeepCopy(item.subject)
          delete subjectParams.adjuvantList
          const associationBOS = Object.keys(item.associationBOS).map((o) => {
            return item.associationBOS[o]
          })

          // let subjectParams = item.subject.adjuvantIds
          return {
            ...item,
            subjectId: item.subject.subjectId,
            subjectNumber: item.subject.number,
            sort: index + 1,
            // subjectContent: JSON.stringify(item.subject),
            subjectContent: JSON.stringify(subjectParams),
            associationBOS: associationBOS,
            subject: undefined
          }
        })
      this.loading = true
      fmFinanceVoucherAddAPI({
        ...this.info,
        total: this.totalDebite,
        certificateDetails: list
      })
        .then((res) => {
          this.$message.success('保存成功')
          if (!this.info.certificateId) {
            this.certificateIds.push(res.data.certificateId)
          }

          if (again) {
            this.initForm()
          } else {
            if (!this.info.certificateId) {
              this.certificateIndex = this.certificateIds.length - 1
            }
            this.getDetail()
          }
          this.getVoucherTimeRange()
          this.loading = false
        })
        .catch(() => {
          this.loading = false
        })
    },
    /**
     * @description: 快捷键操作
     * @param {number} key:keyCode
     */
    shortcutKeysDown(key) {
      console.log(key)
      if (
        key.keyCode == 83 &&
        key.ctrlKey &&
        this.canSave &&
        !this.isPass
      ) {
        // 保存
        this.debounceSaveSubmit(false)
        key.preventDefault()
      } else if (
        key.keyCode == 123 &&
        this.canSave &&
        !this.isPass
      ) {
        // 保存并新增
        this.debounceSaveSubmit(true)
        key.preventDefault()
      } else if (
        key.keyCode == 78 &&
        key.altKey &&
        this.$auth('finance.voucher.save')
      ) {
        // 新增
        this.resetAdd()
        key.preventDefault()
      } else if (key.keyCode == 13 && this.coord) {
        // 下一步
        const inputAll = Array(...document.querySelectorAll('input'))
        const index = inputAll.findIndex(
          (item) => item == document.activeElement
        )
        if (
          this.coord.includes('credit') &&
          Number(this.coord.replace('credit', '')) == this.list.length - 1
        ) {
          this.list.push({
            digestContent: '',
            subject: {},
            subjectId: '',
            associationBOS: {},
            quantity: '',
            debtorBalance: '',
            creditBalance: ''
          })
          this.$nextTick(() => {
            [...document.querySelectorAll('input')][index + 1].focus()
            console.log(document.querySelectorAll('input'))
          })
          return
        }
        this.$nextTick(() => {
          inputAll[index + 1].focus()
        })
      } else if (key.keyCode <= 40 && key.keyCode >= 37 && this.coord) {
        let num
        switch (key.keyCode) {
          case 37:
            num = -1
            break
          case 38:
            num = -4
            break
          case 39:
            num = 1
            break
          case 40:
            num = 4
            break
          default:
            break
        }
        const inputAll = Array(...document.querySelectorAll('input'))
        const index = inputAll.findIndex(
          (item) => item == document.activeElement
        )
        if (
          inputAll[index + num] &&
          document
            .querySelector('.subject-table')
            .contains(inputAll[index + num])
        ) {
          inputAll[index + num].focus()
        }
        key.preventDefault()
      }
    }
  }
}
</script>

<style lang="scss">
.wk-header-page-btn {
  flex-shrink: 0;
  margin-left: #{$--interval-base * 3};

  .el-button + .el-button {
    margin-left: $--interval-base !important;
  }

  .el-button {
    padding: 6px;

    i {
      font-weight: bold;
    }
  }
}
</style>

<style rel="stylesheet/scss" lang="scss" scoped>
.app-container {
  padding: 15px 40px 0;

  .main {
    position: relative;
    display: inline-block;

    &-header {
      height: 32px;
    }

    &-title {
      height: 100%;
      font-size: $--font-size-xxlarge;
      color: $--color-black;
    }

    .pass-pic {
      position: absolute;
      top: 55px;
      left: 80%;
      z-index: 10;
      width: 150px;
    }
  }

  .subject-table {
    position: relative;
    box-sizing: border-box;
    flex: 1;
    width: 1080px;
    margin-top: 0;
    margin-bottom: 10px;

    &::-webkit-scrollbar {
      display: none;
      width: 0 !important;
    }

    .voucher {
      width: 1080px;
      border-collapse: collapse;

      &::-webkit-scrollbar {
        display: none;
        width: 0 !important;
      }

      th {
        height: 48px;
        font-size: 14px;
        font-weight: bold;
        color: $--color-n300;
        text-align: center;
      }
    }
  }

  .content-button {
    display: flex;
    justify-content: space-between;
    margin-top: 24px;

    .dropdown-btn {
      padding: $--button-padding-vertical;
      margin-left: $--button-button-margin-left;
    }

    .button-right {
      margin-left: auto;
    }
  }

  // 凭条
  .note {
    padding: 24px 24px 24px 0;
    margin-top: 24px;
    background-color: $--background-color-base;
    border-radius: $--border-radius-base;

    &-title {
      position: relative;
      font-size: $--font-size-extra-large;
      text-align: center;

      &-time {
        position: absolute;
        top: 2px;
        right: 0;
        font-size: $--font-size-base;
        color: $--color-text-secondary;
      }
    }
  }

  .content-flex {
    width: 100%;
    height: 60px;
    padding-left: 24px;

    &-item {
      display: flex;
      align-items: center;
    }

    .el-input,
    .el-select {
      width: 100px;
      margin-left: 8px;
    }

    .el-date-editor {
      width: 150px;
    }

    .right {
      margin-left: auto;
    }

    ::v-deep .el-input {
      .el-input-group__append {
        padding: 0 8px;
      }
    }
  }

  .bottom-flex {
    justify-content: flex-end;
    margin-top: 15px;
    text-align: right;
  }

  .vch_ft {
    padding: 0 30px;
  }
}

/* 科目设置 */
.voucher {
  background-color: $--color-white;

  ::v-deep .el-input {
    .el-input__inner {
      background-color: white;
      border-color: white;
      border-radius: 0;

      &:focus {
        border-color: $--color-b200;
      }
    }
  }

  // 合计
  .col_total {
    padding: 0 8px;
  }
}

.voucher .col_money {
  font-size: 12px;
}

.voucher .col_money .tit {
  display: block;
  height: 25px;
  line-height: 25px;
}

.voucher .money_unit {
  display: flex;
  height: 22px;
  font-weight: normal;
  line-height: 22px;
  text-align: center;
  border-top: 1px solid #dadada;
}

.voucher .money_unit span {
  display: inline;
  flex: 1;
  float: left;
  height: 100%;
  margin-right: 1px;
  background-color: #fff;
}

.voucher .money_unit .last {
  width: 18px;
  margin-right: 0;
}

.voucher th,
.voucher td {
  border: 1px solid $--color-n50;

  ::v-deep .subject-select-options {
    height: 100%;

    &:hover {
      .choose-btn {
        display: block;
      }
    }

    .choose-btn {
      display: none;
    }
  }

  ::v-deep .el-input {
    height: 100%;

    input {
      height: 100%;
    }

    input:focus {
      box-sizing: border-box;
      border-bottom: 1px solid $--color-b200;
      outline: none;
    }
  }
}

.voucher td {
  height: 60px;
  overflow: hidden;

  .cell_val {
    height: 100%;
  }
}

.voucher td.col_debite .cell_val,
.voucher td.col_credit .cell_val,
.voucher td.col_money .cell_val,
.money_unit {
  background-image: url(../../../assets/img/money_rp.png);
  background-repeat: repeat-y;
  background-size: 100% 100%;
}

.col_quantity {
  width: 150px;
}

.voucher .col_debite,
.voucher .col_credit,
.voucher .col_money {
  position: relative;
  width: 220px;

  .cell_val {
    position: absolute;
    top: 0;
    left: 0;
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }
}

.col_summary .cell_val {
  word-break: break-all;
  word-wrap: break-word;
}

.col_subject .cell_val {
  height: 60px;
  word-break: break-all;
  word-wrap: break-word;
}

.col_subject {
  position: relative;

  ::v-deep.subject-select-options {
    position: absolute;
    top: 0;
    left: 0;
  }

  /* stylelint-disable-next-line no-duplicate-selectors */
  .cell_val {
    position: absolute;
    top: 0;
    left: 0;
    line-height: 60px;
  }

  .cell_val.subject_val {
    display: flex;
    align-items: center;
    padding: 8px 8px 16px 16px;
    overflow-y: auto;
    line-height: 16px;
  }

  .balance {
    position: absolute;
    bottom: 0;
    display: none;
    padding-left: 10px;
    font-size: 13px;
    color: #c0c0c0;
  }

  &:hover {
    .balance {
      display: block;
    }
  }
}

.voucher .col_operate {
  width: 26px;
  text-align: center;
  background-color: $--background-color-base;
  border: 1px solid $--background-color-base;
  border-right: 1px solid $--color-n50;
}

.voucher .col_debite .cell_val,
.voucher .col_credit .cell_val {
  right: -5px;
  *right: 4px;
  overflow: hidden;
  font-family: tahoma;
  font-size: 14px;
  font-weight: bold;
  text-align: right;
  letter-spacing: 10px;

  .money-str {
    display: inline-block;
    width: 20px;
    text-align: center;

    // letter-spacing: 5.1px;
  }
}

.cell_val {
  width: 100%;
}

.col_summary {
  width: 200px;
}

.col_quantity .quantity_val {
  width: 150px;
  padding: 0 3px;
  color: $--color-text-secondary;

  p {
    input {
      width: 54px;
    }
  }

  input {
    border: none;
    border-bottom: 1px solid #ccc;
  }

  input:focus {
    box-sizing: border-box;
    border-bottom: 1px solid $--color-b200;
    outline: none;
  }

  ::v-deep .el-input {
    width: 54px;
    border: none;
    border-bottom: 1px solid #ccc;
  }
}

.entry_item {
  .col_operate {
    i {
      display: none;
      font-size: 13px;
      line-height: 20px;
      color: $--color-n500;
      cursor: pointer;

      &:hover {
        color: $--color-primary;
      }
    }
  }

  &:hover {
    .col_operate {
      i {
        display: block;
      }
    }
  }
}

.col_subject,
.col_summary {
  position: relative;

  .btn {
    position: absolute;
    top: calc(50% - 15px);
    right: 5px;
    display: none;
  }

  &:hover {
    .btn {
      display: block;
    }
  }
}

.mini-input {
  width: 70px !important;
}

::v-deep.mini-input > .el-input__inner {
  padding: 0 0 0 4px !important;
}

.add-box {
  cursor: pointer;

  .add-item {
    padding: 4px;
  }

  .add-item:hover {
    background-color: $--color-n20;
  }
}
</style>
