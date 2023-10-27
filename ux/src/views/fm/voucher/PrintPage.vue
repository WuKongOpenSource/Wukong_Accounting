<template>
  <embed :src="src" type="application/pdf">
</template>
<script>
import { downloadFileAPI } from '@/api/common'
export default {
  data() {
    return {
      src: ''
    }
  },
  mounted() {
    const key = this.$route.query.key
    const previewPdfUrl = `/financeCertificate/preview.pdf?type=1&key=${key}`
    downloadFileAPI(previewPdfUrl).then(res => {
      this.src = window.URL.createObjectURL(res.data)
    }).catch(() => {})
  }
}
</script>
