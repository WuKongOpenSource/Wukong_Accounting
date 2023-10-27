import merge from '@/utils/merge'

const DefaultJXCCreate = {
  title: '',
  showConfirm: true,
  showCancel: true,
  getFields: null
}

export default {
  props: {
    props: Object
  },
  data() {
    return {
      jxcConfig: DefaultJXCCreate,
      relativeProductField: null, //  关联的产品字段 审批流逻辑会返回
      relativeProductAuthLevel: 3 //  关联产品默认3 可查看可编辑，但授权字段里该值会根据实际配置改变 (1 不能查看不能编辑 2可查看  3 可编辑可查看)
    }
  },
  computed: {
    // 关联产品展示和编辑权限
    relativeProductShow() {
      return [2, 3].includes(Number(this.relativeProductAuthLevel))
    },

    relativeProductDisabled() {
      return this.relativeProductAuthLevel != 3
    }
  },
  watch: {
    props: {
      handler() {
        this.jxcConfig = merge({ ...DefaultJXCCreate }, this.props || {})
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    /**
     * @description: 获取自定义提交参数
     * @return {*}
     */
    getCustomFieldPostParams() {
      return new Promise((resolve, reject) => {
        const elForm = this.$refs.elForm
        elForm.validate(valid => {
          if (valid) {
            let params = this.getSubmiteParams(this.baseFields, this.fieldForm)
            // 产品数据
            if (this.relativeProductShow) {
              params = this.getSavedProduct(params)
              const productKeys = [
                'jxcPurchasesProductList',
                'jxcRetreatProductList',
                'jxcPurchasesProductList',
                'jxcSaleProductList',
                'jxcSaleReturnProductList',
                'jxcAllocationProductList',
                'jxcInventoryProductList']
              // 统一校准至product
              let productKey = null
              productKeys.forEach(key => {
                if (params.hasOwnProperty(key)) {
                  params.product = params[key]
                  productKey = key
                }
              })

              if (['jxcRetreatProductList', 'jxcSaleReturnProductList',
                'jxcAllocationProductList', 'jxcInventoryProductList'].includes(productKey)) {
                params.entity.product = params.product
              } else {
                params.entity.product = {
                  discount: params.entity.discount,
                  totalPrice: params.entity.totalPrice,
                  product: params.product
                }
              }
            }
            resolve(params)
          } else {
            // 提示第一个error
            this.getFormErrorMessage(elForm)
            reject()
            return false
          }
        })
      })
    },

    /**
     * @description: 一维数组转二维数据
     * @param {*} fields 一维数组
     * @return {*}
     */
    convertToTwoFields(fields, originFields) {
      const twoFields = []
      let childFields = []

      let productElement = null
      fields.forEach((element, elementIndex) => {
        if (element.formType === 'product') {
          productElement = element
          if (originFields) {
            this.relativeProductField = originFields[elementIndex] // 需要用到原始数据修改value值
          }

          if (fields.length - 1 === elementIndex) {
            twoFields.push(childFields)
          }
          return
        }
        if (!element.stylePercent) element.stylePercent = 50
        if (childFields.length > 0) {
          const linePercent = childFields.reduce((accumulator, current) => {
            return accumulator + current.stylePercent
          }, 0)
          if (linePercent + element.stylePercent > 100) {
            twoFields.push(childFields)
            childFields = []
            childFields.push(element)
          } else {
            childFields.push(element)
          }
          if (fields.length - 1 === elementIndex) {
            twoFields.push(childFields)
          }
        } else {
          childFields.push(element)
          if (fields.length - 1 === elementIndex) {
            twoFields.push(childFields)
          }
        }
      })

      if (productElement) {
        this.relativeProductAuthLevel = productElement.authLevel
      } else {
        this.relativeProductAuthLevel = 1
      }

      return twoFields
    }
  }
}
