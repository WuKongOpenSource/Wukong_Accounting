<template>
  <el-dialog
    ref="wkDialog"
    :visible.sync="dialogVisible"
    :before-close="handleClose"
    :close-on-click-modal="false"
    append-to-body
    :width="showSubjectSelectOptions? '700px':'500px'">
    <span slot="title" class="el-dialog__title">
      {{ title }}
      <i
        v-if="nodeData && helpObj"
        class="wk wk-icon-fill-help wk-help-tips"
        :data-type="helpObj.dataType"
        :data-id="helpObj.dataId" />
    </span>

    <flexbox>
      <SubjectSelectOptions
        v-if="showSubjectSelectOptions" />
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        :validate-on-rule-change="false"
        label-width="80px"
        :style="showSubjectSelectOptions?{'marginLeft':'20px'}:{'flex':'1'}"
        class="form">
        <el-form-item
          :inline-message="true"
          label="科目编码"
          prop="number">
          <el-popover
            :value="selectVisible"
            placement="bottom"
            title="可用子集科目编码"
            width="390"
            trigger="manual"
          >
            <div v-if="!parentNode&&canUseSubject" class="wrapper">
              <div class="subject-list">
                <div
                  class="subject-list-item"
                  @click="selectParentNumber">
                  {{ canUseSubject.useNumber }}
                </div>
              </div>
            </div>
            <el-input
              slot="reference"
              :disabled="getNumberDisabled"
              :value="form.number"
              @input="checkNumber"
              @change="getParentNodeId(form.number, null, subjectList)" />
          </el-popover>
        </el-form-item>
        <div class="rule">
          科目级次：{{ codeRule.join('-') }}
        </div>
        <el-form-item label="科目名称" prop="subjectName">
          <el-input v-model.trim="form.subjectName" />
        </el-form-item>
        <el-form-item label="上级科目">
          <el-input
            :value="parentNodeName"
            disabled />
        </el-form-item>
        <el-form-item label="科目类别" prop="category">
          <el-select v-model="form.category" :disabled="categoryDisabled">
            <el-option
              v-for="(item,index) in categoryOptions"
              :key="index"
              :label="item.name"
              :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="余额方向" prop="balanceDirection" class="no-margin">
          <el-radio-group v-model="form.balanceDirection" :disabled="!!form.isRelevance">
            <el-radio :label="1">借</el-radio>
            <el-radio :label="2">贷</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item style="margin-top: 20px;" class="no-margin" label="辅助核算">
          <el-select
            v-model="form.adjuvantIds"
            :disabled="adjuvantDisable"
            multiple>
            <el-option
              v-for="(item,index) in adjuvantOptions"
              :key="index"
              :label="item.adjuvantName"
              :value="item.adjuvantId" />
          </el-select>
        </el-form-item>
        <el-form-item class="no-margin" label-width="20px">
          <el-checkbox
            :value="form.isAmount"
            :true-label="1"
            :false-label="0"
            @change="isAmountChange">数量核算</el-checkbox>
        </el-form-item>
        <el-form-item
          v-if="form.isAmount"
          label="数量单位"
          prop="amountUnit"
          class="no-margin">
          <el-input v-model="form.amountUnit" />
        </el-form-item>
        <!-- <el-form-item class="no-margin" label-width="20px">
        <el-checkbox
          v-model="form.isForeignCurrency"
          :true-label="1"
          :false-label="0">外币核算</el-checkbox>
      </el-form-item>
      <el-form-item
        v-if="form.isForeignCurrency"
        class="no-margin">
        <el-checkbox-group v-model="form.foreignCurrency">
          <el-checkbox
            v-for="(item,index) in currencyList"
            :key="index"
            :label="item.currencyId">
            {{ item.currencyName }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item> -->
        <el-form-item label-width="20px" class="no-margin">
          <el-checkbox
            v-model="form.isCash"
            :disabled="!!(parentNode&&parentNode.isCash)||(selectParent&&selectParent.isCash)"
            :true-label="1"
            :false-label="0">现金及现金等价物</el-checkbox>
        </el-form-item>
      </el-form>
    </flexbox>
    <!-- 验证提示弹窗 -->
    <SubjectTipsDialog
      v-if="visibleTipsDialog"
      :visible.sync="visibleTipsDialog"
      :tips-form-field="tipsFormField"
      :node-data="nodeData"
      :parent-node="parentNode"
      @save="saveTipsHandle"
      @close="closeTipsHandle"
    />

    <div slot="footer" class="dialog-footer">
      <el-button v-debounce="handleConfirm" type="primary">确定</el-button>
      <el-button @click="handleClose">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {
  fmFinanceSubjectAddAPI,
  fmFinanceParameterQueryParameterAPI,
  fmFinanceSubjectListAPI,
  fmAdjuvantListAPI,
  financeCertificateCheckSubjectCertificateAPI
} from '@/api/fm/setting'
import { fmFinanceCurrencyListAPI } from '@/api/fm/setting'

import SubjectSelectOptions from '@/views/fm/components/subject/components/SubjectSelectOptions'
import SubjectTipsDialog from '@/views/fm/components/subject/components/SubjectTipsDialog.vue'

import { isEmpty } from '@/utils/types'
import { objDeepCopy } from '@/utils'

/**
 * 新建/编辑科目
 * @param {Boolean} visible 控制弹窗标志
 * @param {Object} nodeData 编辑的节点对象
 * @param {Object} parentNode 父级节点对象
 * @param {String|Number} subjectType 科目类型
 * @param {Array} subjectList 全部科目（未选择父节点时新建子节点时使用）
 * @event close 关闭弹窗回调
 * @event confirm 确认选择回调，回调参数 data 选择的科目节点数据
 */
export default {
  name: 'SubjectAddOrUpdate',
  components: {
    SubjectSelectOptions,
    SubjectTipsDialog
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    nodeData: {
      type: Object,
      default: null
    },
    parentNode: {
      type: Object,
      default: null
    },
    subjectType: {
      type: [String, Number],
      default: null
    },
    helpObj: Object,
    showSubjectSelectOptions: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      dialogVisible: false,
      form: {
        number: ''
      },
      selectParent: null,
      codeRule: [],
      subjectList: [],
      selfSubjectType: null,
      adjuvantOptions: [], // 辅助核算列表
      currencyList: [], // 币种列表
      visibleTipsDialog: false,
      tipsFormField: []
    }
  },
  computed: {
    title() {
      if (this.nodeData) return '编辑科目'
      return this.parentNode ? '新建下级科目' : '新建科目'
    },
    parentNodeName() {
      if (!this.parentNode && !this.selectParent) return '无上级科目'
      if (this.parentNode) {
        return `${this.parentNode.number} ${this.parentNode.subjectName}`
      } else {
        return `${this.selectParent.number} ${this.selectParent.subjectName}`
      }
    },
    selectVisible() {
      if (!this.parentNode && this.canUseSubject && !this.nodeData) {
        return true
      } else {
        return false
      }
    },
    canUseSubject() {
      const subject = this.getSubjectByNumber(this.subjectList, this.form.number)
      if (subject) {
        let useNumber
        if (subject.children && subject.children.length) {
          useNumber = Math.max(...subject.children.map(item => Number(item.number))) + 1
        } else {
          var num = subject.number
          console.log(this.codeRule[subject.grade])
          for (let i = 1; i < this.codeRule[subject.grade]; i++) {
            num += '0'
          }
          num += '1'
          useNumber = num
        }
        return {
          ...subject,
          useNumber
        }
      }
      return ''
    },
    getNumberDisabled() {
      if (this.nodeData && this.nodeData.children && this.nodeData.children.length) {
        return true
      }
      return false
    },
    // adjuvantOptions() {
    //   return [
    //     { name: '线索', value: 1 },
    //     { name: '客户', value: 2 },
    //     { name: '联系人', value: 3 },
    //     { name: '产品', value: 4 },
    //     { name: '商机', value: 5 },
    //     { name: '合同', value: 6 },
    //     { name: '回款', value: 7 },
    //     { name: '发票', value: 8 },
    //     { name: '部门', value: 9 },
    //     { name: '员工', value: 10 }
    //   ]
    // },
    categoryOptions() {
      const lib = [
        { name: '流动资产', value: 1, type: 1, category: 1 },
        { name: '非流动资产', value: 2, type: 1, category: 2 },

        { name: '流动负债', value: 3, type: 2, category: 1 },
        { name: '非流动负债', value: 4, type: 2, category: 2 },

        { name: '所有者权益', value: 5, type: 3, category: 1 },

        { name: '成本', value: 6, type: 4, category: 1 },

        { name: '营业收入', value: 7, type: 5, category: 1 },
        { name: '其他收益', value: 8, type: 5, category: 2 },
        { name: '期间费用', value: 9, type: 5, category: 3 },
        { name: '其他损失', value: 10, type: 5, category: 4 },
        { name: '营业成本及税金', value: 11, type: 5, category: 5 },
        { name: '以前年度损益调整', value: 12, type: 5, category: 6 },
        { name: '所得税', value: 13, type: 5, category: 7 }
      ]
      if (this.selfSubjectType) {
        return lib.filter(o => o.type === Number(this.selfSubjectType))
      }
      return lib
    },
    categoryDisabled() {
      return Boolean(this.parentNode || this.selectParent)
    },
    rules() {
      return {
        number: [
          { required: true, message: '请输入科目编号', trigger: 'blur' },
          { validator: this.validatorNumber, trigger: 'blur' }
        ],
        subjectName: [
          { required: true, message: '请输入科目名称', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择科目类别', trigger: 'blur' }
        ],
        balanceDirection: [
          { required: true, message: '请选择余额方向', trigger: 'change' }
        ]
      }
    },
    // 辅助核算是否禁止
    adjuvantDisable() {
      // 新建          一定可以编辑
      // 新建末级       一定可以编辑
      // 编辑
      // 	   如果  没有下级  可以编辑
      //     如果  已录入配置且有辅助核算
      if (this.nodeData && this.nodeData?.adjuvantList?.length && this.nodeData.isRelevance) return true
      if (this.isLastLevel && this.parentNode && this.parentNode?.adjuvantList?.length && this.parentNode.isRelevance) return true
      if (this.isCreate || this.isLastLevel) return false
      if (!this.isCreate && this.noSubordinates) return false
      return true
    },
    // 是否是新建
    isCreate() {
      return this.nodeData == null && this.parentNode == null
    },
    // 新建末级
    isLastLevel() {
      return this.nodeData == null && this.parentNode?.children?.length == 0
    },
    // 编辑末级
    isEditLastLevel() {
      return this.nodeData && this.nodeData?.children?.length == 0
    },
    // 是否有下级
    noSubordinates() {
      return this.nodeData?.children?.length == 0
    }
  },
  watch: {
    visible: {
      handler() {
        this.dialogVisible = this.visible
        if (this.visible) {
          this.loading = true
          Promise.all([this.getSubjectGroupList(), this.getCodeRule()]).then(() => {
            this.loading = false
            this.initForm()
          }).catch(() => {
            this.loading = false
          })
          if (this.subjectType) {
            this.selfSubjectType = this.subjectType
          }
        }
      },
      immediate: true
    }
  },
  created() {
    this.getAllAdjuvantList()
    this.getCurrencyGroupList()
  },
  methods: {
    /**
     * 获取科目编码规则
     */
    getCodeRule() {
      return new Promise((resolve, reject) => {
        fmFinanceParameterQueryParameterAPI().then(res => {
          if (res.data && res.data.rule) {
            this.codeRule = res.data.rule.split('-').map(o => Number(o))
            resolve()
          } else {
            reject()
          }
        }).catch(() => {
          reject()
        })
      })
    },
    /**
     * 查询辅助核算目录列表
     */
    getAllAdjuvantList() {
      fmAdjuvantListAPI().then(res => {
        this.adjuvantOptions = res.data
      }).catch(() => {})
    },
    /**
     * 科目列表
     */
    getSubjectGroupList() {
      return new Promise((resolve, reject) => {
        fmFinanceSubjectListAPI({
          type: '',
          isTree: 1
        })
          .then(res => {
            this.subjectList = res.data
            resolve()
          })
          .catch(() => {
            reject()
          })
      })
    },
    getAdjuvantList(node) {
      if (!node.adjuvantList) {
        this.$set(this.form, 'adjuvantIds', [])
        return
      }
      this.$set(this.form, 'adjuvantIds', node.adjuvantList.map(item => item.adjuvantId))
    },

    /**
     * 获取币别列表
     */
    getCurrencyGroupList() {
      fmFinanceCurrencyListAPI()
        .then(res => {
          this.currencyList = res.data
        })
    },

    initForm() {
      if (this.nodeData) {
        this.form = objDeepCopy(this.nodeData)
        this.getAdjuvantList(this.nodeData)
        this.$set(this.form, 'adjuvantIds', this.form.adjuvantList?.map(item => item.adjuvantId) || [])
        const findRes = this.categoryOptions.find(o => {
          return o.category === this.nodeData.category &&
            o.type === Number(this.selfSubjectType)
        })
        if (findRes) {
          this.$set(this.form, 'category', findRes.value)
        }
        // if (this.form.isForeignCurrency) {
        //   this.$set(this.form, 'foreignCurrency', (this.form.foreignCurrency || []))
        // } else {
        //   this.$set(this.form, 'foreignCurrency', [])
        // }
        delete this.form.createTime
        delete this.form.createUserId
      } else {
        this.form = {
          adjuvantIds: [],
          amountUnit: '',
          balanceDirection: 1,
          currencyIds: [],
          grade: '',
          isAmount: 0,
          // isForeignCurrency: 0, // 是否外币核算
          // foreignCurrency: [], // 选择的外币
          isCash: 0,
          number: '',
          subjectName: '',
          type: this.selfSubjectType || '',
          category: this.parentNode ? this.categoryOptions[this.parentNode.category - 1].value : ''
        }
        if (this.parentNode) {
          // if (this.parentNode.children && this.parentNode.children.length) {
          //   const arr = this.parentNode.children.map(o => Number(o.number))
          //   this.form.number = Math.max(...arr) + 1 + ''
          // }
          this.form.balanceDirection = this.parentNode.balanceDirection
          const parentNode = this.getSubjectByNumber(this.subjectList, this.parentNode.number)
          if (parentNode && parentNode.children && parentNode.children.length) {
            const parentNumber = parentNode.number
            const arr = parentNode.children.map(o => {
              return o.number.replace(parentNumber, '')
            })
            const ruleNum = this.codeRule[this.parentNode.grade]
            for (let index = 1; index < Number('9'.repeat(ruleNum)); index++) {
              const str = '0'.repeat(ruleNum - String(index).length) + index
              if (!arr.includes(str)) {
                this.form.number = parentNumber + str
                break
              }
            }
            // const len = arr[0].length
            // const repair = '9'.repeat(len)
            // const findRes = arr.findIndex(item => item == repair)
            // if (findRes != -1) {
            //   arr.splice(findRes, 1)
            // }
            // let Addend = Math.max(...arr) + 1
            // if (String(Addend).length < ruleNum) {
            //   // 补位
            //   Addend = '0'.repeat(ruleNum - String(Math.max(...arr)).length) + Addend
            // }
            // // this.form.number = Number(parentNumber + Math.max(...arr)) + 1 + ''
            // this.form.number = Number(parentNumber + Addend) + ''
          } else if (this.codeRule[this.parentNode.grade]) {
            const ruleNum = this.codeRule[this.parentNode.grade] * 1 - 1
            let number = this.parentNode.number
            for (let i = 0; i < ruleNum; i++) {
              number = number + '0'
            }
            number = number + '1'
            this.form.number = number
          } else {
            this.form.number = this.parentNode.number + '01'
          }

          this.getAdjuvantList(this.parentNode)
          if (this.parentNode.isCash) {
            this.form.isCash = this.parentNode.isCash
          }
          if (this.parentNode.isAmount) {
            this.form.isAmount = this.parentNode.isAmount
            this.form.amountUnit = this.parentNode.amountUnit
          }
        } else {
          // this.form.number = this.action.proBrotherNumber * 1 + 1 + ''
        }
      }
    },
    isAmountChange(val) {
      if (!val && ((this.nodeData && this.nodeData.isAmount) || (this.parentNode && this.parentNode.isAmount))) {
        this.$confirm('取消数量核算将无法查看之前录入的数量，也不能查询此科目的数量核算，也不能查询此科目的数量账簿！', '系统提示', {
          confirmButtonText: '确定',
          cancelButtonText: '放弃',
          type: 'warning'
        }).then(() => {
          this.form.isAmount = val
        }).catch(() => {
        })
      } else {
        this.form.isAmount = val
      }
    },
    getSubjectByNumber(arr, number) {
      var subject
      for (let i = 0; i < arr.length; i++) {
        const item = arr[i]

        if (item.children && item.children.length) {
          subject = this.getSubjectByNumber(item.children, number)
        }
        if (item.number === number) {
          subject = item
        }
        if (subject) {
          return subject
        }
      }
    },
    getSubjectByName(arr, name) {
      var subject
      for (let i = 0; i < arr.length; i++) {
        const item = arr[i]

        if (item.children && item.children.length) {
          subject = this.getSubjectByNumber(item.children, name)
        }
        if (item.subjectName === name) {
          subject = item
        }
        if (subject) {
          return subject
        }
      }
    },
    selectParentNumber() {
      this.selectParent = this.canUseSubject
      this.form.number = this.selectParent.useNumber
      this.form.category = this.selectParent.category
      this.form.isCash = this.selectParent.isCash
      this.getAdjuvantList(this.selectParent)
    },
    validatorNumber(rule, value, callback) {
      if (isEmpty(value)) {
        callback()
      } else {
        if (isNaN(Number(value))) {
          callback(new Error('编码必须为数字'))
          return
        }
        if (this.parentNode) {
          // 有父节点
          if (value.replace(this.parentNode.number, '').length !== this.codeRule[this.parentNode.grade]) {
            callback(new Error('不符合编码规则'))
          } else if (!value.startsWith(this.parentNode.number)) {
            callback(new Error('不符合编码规则'))
          } else {
            callback()
          }
        } else {
          // 没有父节点
          const grade = this.getGradeByNumber(value)
          if (!grade) {
            callback(new Error('不符合编码规则'))
          } else {
            const len = this.codeRule.reduce((acc, current, currentIndex) => {
              return currentIndex < grade ? acc + current : acc
            })
            // console.log('grade len: ', value, String(value).length, len)
            if (String(value).length !== len) {
              callback(new Error('不符合编码规则'))
            } else {
              callback()
            }
          }
        }
      }
    },

    /**
     * 通过科目编码查询科目父节点的科目ID
     * @param {String} number 科目编码
     * @param {Number} grade 科目级别
     * @param {Array} list 全部科目
     * @return {Number|String}
     */
    getParentNodeId(number, grade, list) {
      if (this.parentNode || this.selectParent) {
        if (this.parentNode) {
          this.getAdjuvantList(this.parentNode)
          return this.parentNode.subjectId
        } else {
          this.getAdjuvantList(this.selectParent)
          return this.selectParent.subjectId
        }
      }
      if (!grade) {
        grade = this.getGradeByNumber(number)
      }
      if (!grade) return undefined

      const len = this.codeRule.reduce((acc, current, currentIndex) => {
        return currentIndex < (grade - 1) ? acc + current : acc
      })
      console.log(len, number.length)

      for (let i = 0; i < list.length; i++) {
        const item = list[i]
        if (number.startsWith(item.number)) {
          if (item.grade === grade - 1) {
            this.setCategory(item.type)
            this.selectParent = item

            this.getAdjuvantList(item)
            return item.subjectId
          }
          if (item.children) {
            return this.getParentNodeId(number, grade, item.children)
          }
        } else if (
          grade === item.grade &&
          number.length === item.number.length &&
          number.slice(0, len) === item.number.slice(0, len)
        ) {
          // 全部科目数据中有可能出现没有父节点，只有兄弟节点的情况
          console.log('set category: ', item)
          this.setCategory(item.type)
          return item.parentId
        }
      }
    },
    /**
     * 通过科目编码查询科目的级别
     * @param {String} number 科目编码
     * @return {Number}
     */
    getGradeByNumber(number) {
      let len = 0
      for (let i = 0; i < this.codeRule.length; i++) {
        len = len + this.codeRule[i]
        if (String(this.form.number).length === len) return i + 1
      }
    },
    setCategory(type) {
      this.selfSubjectType = type
      this.$nextTick(() => {
        const firstChild = this.categoryOptions[0]
        this.$set(this.form, 'category', firstChild.value)
      })
    },

    /**
     * 关闭弹窗
     */
    handleClose() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
      this.$emit('close')
      this.$refs.form.resetFields()
    },
    checkNumber(value) {
      console.log(value)
      if (this.parentNode) {
        if (value.includes(this.parentNode.number)) {
          this.form.number = value
        }
        return
      } else {
        if (this.selectParent && !this.getSubjectByNumber(this.subjectList, this.form.number)) {
          this.selectParent = null
        }
      }
      this.form.number = value
    },
    /**
     * 确认选择
     */
    handleConfirm() {
      this.loading = true
      this.$refs.form.validate(valid => {
        if (valid) {
          const form = objDeepCopy(this.form)
          if (!this.parentNode) {
            form.grade = this.getGradeByNumber(form.number)
            if (form.grade > 1) {
              const parentId = this.getParentNodeId(form.number, form.grade, this.subjectList)
              if (!parentId) {
                this.$message.warning('您输入的编码为明细科目编码，但在当前类别找不到其上级科目')
                return
              }
              form.parentId = parentId
            }
          } else {
            form.grade = this.parentNode.grade + 1
            form.parentId = this.parentNode.subjectId
          }
          const categoryObj = this.categoryOptions.find(o => o.value === this.form.category)
          if (categoryObj) {
            form.type = categoryObj.type
            form.category = categoryObj.category
          } else {
            this.$message.error('科目类别错误')
            return
          }
          delete form.companyId
          const nextSave = (associationMap) => {
            if (associationMap) {
              form.associationMap = associationMap
            }
            const parent = this.parentNode || this.selectParent
            const bother = parent && parent.children ? parent.children.length : ''
            if (parent && parent.balance && !bother) {
              const msg = `您正在为科目“${parent.subjectName}”增加第一个下级科目，
            系统将把该科目的数据全部转移到新增的下级科目中，该操作不可逆，您是否要继续？`
              this.$confirm(msg, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                this.submitData(form)
              }).catch(() => {
              })
            } else {
              this.submitData(form)
            }
          }
          if (this.getSubjectByName(this.subjectList, form.subjectName) && !this.nodeData) {
            this.$confirm('已存在相同名称的科目，请确定是否保存?', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            })
              .then(() => {
                if (this.adjuvantDisable === false && this.form.adjuvantIds.length) {
                  const subjectId = this.isLastLevel ? this.parentNode.subjectId : this.nodeData.subjectId
                  this.checkData(subjectId).then(res => {
                    if (res) {
                      this.tipsFormDialog().then(associationMap => {
                        nextSave(associationMap)
                      })
                    } else {
                      nextSave()
                    }
                  })
                } else {
                  nextSave()
                }
              })
              .catch(() => {
              })
          } else {
            if (this.adjuvantDisable === false && this.form.adjuvantIds.length) {
              if (!this.parentNode && !this.selectParent) {
                nextSave()
              } else {
                const subjectId = this.isLastLevel ? this.parentNode.subjectId : this.nodeData.subjectId
                this.checkData(subjectId).then(res => {
                  if (res) {
                    this.tipsFormDialog().then(associationMap => {
                      nextSave(associationMap)
                    })
                  } else {
                    nextSave()
                  }
                })
              }
            } else {
              nextSave()
            }
          }
        }
      })
    },

    // 弹窗提示 异步
    tipsFormDialog() {
      return new Promise((resolve, reject) => {
        const adjuvantList = this.form.adjuvantIds.map(item => {
          return this.adjuvantOptions.find(o => o.adjuvantId == item)
        })
        console.log(adjuvantList, 'adjuvantList')
        this.tipsFormField = adjuvantList
        this.visibleTipsDialog = true
        this._promiseCall = { resolve, reject }
      })
    },
    // 弹窗保存回调
    saveTipsHandle(associationMap) {
      console.log(associationMap, 'form')
      this._promiseCall.resolve(associationMap)
      this.visibleTipsDialog = false
    },
    // 弹窗取消回调
    closeTipsHandle() {
      this._promiseCall.reject()
      this.visibleTipsDialog = false
    },

    // 验证辅助核算数据
    async checkData(subjectId) {
      return financeCertificateCheckSubjectCertificateAPI({ subjectId }).then(res => {
        return res.data
      })
    },

    submitData(form) {
      this.loading = true
      fmFinanceSubjectAddAPI(form).then(res => {
        this.loading = false
        this.$message.success('添加成功')
        this.$emit('confirm')
        this.handleClose()
        this.$refs.form.resetFields()
      }).catch(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped lang="scss">
::v-deep.el-dialog__body {
  padding: 15px !important;
}

.rule {
  float: left;
  margin-top: -18px;
  margin-left: 80px;
  font-size: 12px;
  color: #999;
}

::v-deep .el-form-item__error--inline {
  display: block;
  margin-left: 0;
}

.subject-list {
  max-height: 240px;
  overflow-y: auto;
  font-size: 13px;

  .subject-list-item {
    padding: 7px 10px;
    color: #333;
    cursor: pointer;
    background-color: white;

    &.subject-list-item.active,
    &.subject-list-item:hover {
      color: $--color-primary;
      background-color: #f7f8fa;
    }
  }

  .empty-tips {
    padding: 10px 15px;
    font-size: 12px;
    color: $--color-text-placeholder;
  }
}

.no-margin {
  margin: 0;
}

.el-popver {
  ::v-deep.el-popover__title {
    font-size: 13px;
    color: #999;
  }
}
</style>
