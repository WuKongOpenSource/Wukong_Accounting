export function setHeadDetails(resData, label) {
}

export function setFieldList(fieldList, label, detailData) {
  if (!label) {
    return [
      {
        name: '基本信息',
        list: fieldList?.flat(Infinity)
      }
    ]
  } else {
    const baseList = []
    const systemList = []
    fieldList && fieldList.forEach(item => {
      if (item.sysInformation == 1) {
        systemList.push(item)
      } else if (item.formType !== 'product') {
        // 不展示产品
        baseList.push(item)
      }
    })

    const baseInfoList = [
      {
        name: '基本信息',
        list: baseList
      },
      {
        name: '系统信息',
        list: systemList
      }
    ]

    if (label == 7) {
      const list = [
        {
          formType: 'text',
          fieldName: 'contacts',
          name: '联系人',
          value: detailData.contacts
        },
        {
          formType: 'text',
          fieldName: 'telephone',
          name: '电话',
          value: detailData.telephone
        },
        {
          formType: 'text',
          fieldName: 'detailAddress',
          name: '地址',
          value: detailData.detailAddress
        }
      ]

      baseInfoList.splice(1, 0, {
        name: '收货信息',
        list: list
      })
    }

    return baseInfoList
  }
}
