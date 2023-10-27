<template>
  <el-dialog
    ref="wkDialog"
    :visible.sync="editFormulaVisible"
    :before-close="editFormulaClose"
    :close-on-click-modal="false"
    :append-to-body="true"
    width="900px">
    <div slot="title" class="dialog-header">
      <span class="title-box">编辑公式——{{ currentItem.name ? currentItem.name : '' }}</span>
    </div>
    <flexbox class="content-flex">
      <div class="flex-item">
        <select-subject v-model="subjectId" @change="receiveSubject" />
      </div>
      <el-button class="add" type="primary" @click="addItem">添加</el-button>
    </flexbox>
    <el-table
      :data="formulaList"
      :summary-method="getSummaries"
      max-height="250"
      show-summary
      border
      style="width: 100%;margin: 20px 0;">
      <el-table-column
        prop="subjectName"
        label="科目"
        width="250">
        <template slot-scope="{ row }">
          <span>{{ row.subjectNumber }}  {{ row.subjectName }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="operator"
        label="运算符号"
        width="80"
        align="center" />
      <el-table-column
        prop="yearValue"
        label="本年累计金额" />
      <el-table-column
        prop="monthValue"
        label="本月金额" />
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="text" @click="removeItem(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <reminder content="温馨提示：报表项目重新设置公式后，只影响当前及以后期间，反结账后报表的历史数据会重算，请谨慎操作！" />
    <div slot="footer">
      <el-button v-debounce="save" type="primary">保存</el-button>
      <el-button @click="editFormulaClose">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { updateIncomeFormulaAPI } from '@/api/fm/report'

import Reminder from '@/components/Reminder'
import SelectSubject from '@/views/fm/components/subject/SubjectSelectOptions'

import { isEmpty } from '@/utils/types'
import NP from 'number-precision'

export default {
  name: 'EditIncomFormula',
  components: {
    Reminder,
    SelectSubject
  },
  filters: {
    fetchValue(val) {
      return { 0: '金额', 1: '借方余额', 2: '贷方余额', 3: '科目借方余额', 4: '科目贷方余额', 5: '借方发生额', 6: '贷方发生额' }[val]
    }
  },
  props: {
    editFormulaVisible: Boolean,
    type: {
      type: Number,
      default: 1
    },
    /** 编辑时候传递进来的信息 */
    currentItem: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      loading: false,

      subjectId: '', // 科目id
      subjectItem: {}, // 要添加的科目项

      formulaList: [] // 编辑公式规则列表
    }
  },
  watch: {
    currentItem: {
      handler(val) {
        if (!isEmpty(val) && val.formula) {
          this.formulaList = JSON.parse(val.formula)
          console.log('规则列表', this.formulaList)
        }
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    // 获取选择的科目
    receiveSubject(data, node) {
      console.log(data, node)
      this.subjectItem = node
    },

    // 表格项添加
    addItem() {
      // console.log('科目', this.SubjectId)
      if (isEmpty(this.subjectItem)) {
        this.$message.warning('请填写科目！')
        return
      }

      const findRes = this.formulaList.find(o => o.subjectId === this.subjectId)
      if (findRes) {
        this.$message.warning('科目不能重复添加！')
        return
      }

      this.formulaList.unshift({
        subjectId: this.subjectItem.subjectId,
        subjectName: this.subjectItem.subjectName,
        subjectNumber: this.subjectItem.subjectNumber || this.subjectItem.number || '',
        operator: '+',
        rules: 0,
        lastMonthValue: 0,
        monthValue: 0,
        yearValue: 0
      })
      this.subjectId = null
      this.subjectItem = null
    },

    // 表格项删除
    removeItem(item) {
      const idx = this.formulaList.findIndex(el => el.subjectId === item.subjectId)
      this.formulaList.splice(idx, 1)
    },

    // 计算合计
    getSummaries(param) {
      const { columns, data } = param
      // console.log('columns,data', columns, data)
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计'
          return
        }

        const arr = data.map(item => {
          const num = Number(item[column.property])
          if (!isNaN(num)) {
            const step = item.operator === '+' ? 1 : -1
            return num * step
          }
          return num
        })
        if (!arr.every(value => isNaN(value))) {
          sums[index] = arr.reduce((acc, current) => {
            return NP.plus(acc, current)
          })
        } else {
          sums[index] = ''
        }
      })

      return sums
    },

    editFormulaClose() {
      this.$emit('editFormulaClose')
    },

    // 保存编辑公式
    save() {
      updateIncomeFormulaAPI({
        formulaBOList: this.formulaList,
        id: this.currentItem.id
      })
        .then(res => {
          console.log('保存编辑公式', res)
          this.$message.success('保存成功')
          this.$emit('save-success')
        })
        .catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog-header {
  text-align: center;

  .title-box {
    font-size: 18px;
    font-weight: 600;
  }
}

.flex-item {
  display: flex;
  align-items: center;
  margin-right: 25px;

  .subject-text {
    width: 70px;
  }
}

.add {
  margin-left: 30px;
}
</style>
