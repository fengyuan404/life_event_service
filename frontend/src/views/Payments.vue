<template>
  <section>
    <div class="toolbar">
      <div>
        <h2 class="page-title">缴费管理</h2>
        <p class="page-subtitle">登记租赁费、管理费、续费并汇入营收统计</p>
      </div>
      <div class="toolbar-right">
        <el-input v-model="query.keyword" clearable placeholder="票据 / 家属 / 类型" style="width: 240px" @keyup.enter="load" />
        <el-button :icon="Search" @click="load">查询</el-button>
        <el-button type="primary" :icon="Plus" @click="openForm">登记缴费</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column label="票据编号" min-width="150"><template #default="{ row }">{{ get(row, 'invoiceNo', 'invoice_no') }}</template></el-table-column>
        <el-table-column label="家属" min-width="110"><template #default="{ row }">{{ get(row, 'familyName') }}</template></el-table-column>
        <el-table-column label="逝者" min-width="110"><template #default="{ row }">{{ get(row, 'deceasedName') }}</template></el-table-column>
        <el-table-column label="墓穴" min-width="100"><template #default="{ row }">{{ get(row, 'graveCode') }}</template></el-table-column>
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ get(row, 'payType', 'pay_type') }}</template></el-table-column>
        <el-table-column label="方式" width="100"><template #default="{ row }">{{ get(row, 'payMethod', 'pay_method') }}</template></el-table-column>
        <el-table-column label="金额" width="120"><template #default="{ row }"><span class="amount">￥{{ get(row, 'payAmount', 'pay_amount') }}</span></template></el-table-column>
        <el-table-column label="时间" min-width="170"><template #default="{ row }">{{ get(row, 'payTime', 'pay_time') }}</template></el-table-column>
        <el-table-column label="收款人" min-width="110"><template #default="{ row }">{{ get(row, 'staffName') }}</template></el-table-column>
      </el-table>
      <el-pagination layout="total, prev, pager, next, sizes" :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="(p) => { query.page = p; load() }" @size-change="(s) => { query.size = s; load() }" />
    </div>

    <el-dialog v-model="dialogVisible" title="登记缴费" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="租赁订单" prop="rentId">
          <el-select v-model="form.rentId" filterable style="width: 100%" @change="onRentChange">
            <el-option v-for="item in rents" :key="get(item, 'rentId', 'rent_id')" :label="`#${get(item, 'rentId', 'rent_id')} ${get(item, 'familyName')} / ${get(item, 'graveCode')}`" :value="Number(get(item, 'rentId', 'rent_id'))" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款员工" prop="staffId"><el-select v-model="form.staffId" filterable style="width: 100%"><el-option v-for="item in staff" :key="item.staffId" :label="`${item.staffName} (${item.role})`" :value="item.staffId" /></el-select></el-form-item>
        <el-form-item label="缴费类型" prop="payType"><el-select v-model="form.payType" style="width: 100%"><el-option label="租赁费" value="租赁费" /><el-option label="管理费" value="管理费" /><el-option label="续费" value="续费" /><el-option label="其他" value="其他" /></el-select></el-form-item>
        <el-form-item label="缴费方式" prop="payMethod"><el-select v-model="form.payMethod" style="width: 100%"><el-option label="现金" value="现金" /><el-option label="银行卡" value="银行卡" /><el-option label="微信" value="微信" /><el-option label="支付宝" value="支付宝" /><el-option label="转账" value="转账" /></el-select></el-form-item>
        <el-form-item label="金额" prop="payAmount"><el-input-number v-model="form.payAmount" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="缴费时间" prop="payTime"><el-date-picker v-model="form.payTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="票据编号" prop="invoiceNo"><el-input v-model="form.invoiceNo" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { paymentApi, rentApi, staffApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const rents = ref([])
const staff = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ keyword: '', page: 1, size: 10 })
const get = (row, ...keys) => keys.map((key) => row[key]).find((value) => value !== undefined && value !== null) || ''
const nowIso = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19)
const blank = () => ({ rentId: null, staffId: auth.staffId, payType: '租赁费', payMethod: '微信', payAmount: 0.01, payTime: nowIso(), invoiceNo: `INV${Date.now()}`, remark: '' })
const form = reactive(blank())
const rules = {
  rentId: [{ required: true, message: '请选择租赁订单', trigger: 'change' }],
  staffId: [{ required: true, message: '请选择收款员工', trigger: 'change' }],
  payType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  payMethod: [{ required: true, message: '请选择方式', trigger: 'change' }],
  payAmount: [{ required: true, message: '请输入金额', trigger: 'change' }],
  payTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  invoiceNo: [{ required: true, message: '请输入票据编号', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const data = await paymentApi.list(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openForm = async () => {
  Object.assign(form, blank())
  rents.value = (await rentApi.list({ page: 1, size: 1000 })).records
  staff.value = await staffApi.list()
  dialogVisible.value = true
}

const onRentChange = (id) => {
  const rent = rents.value.find((item) => Number(get(item, 'rentId', 'rent_id')) === id)
  if (rent) form.payAmount = Number(get(rent, 'totalAmount', 'total_amount') || 0.01)
}

const save = async () => {
  await formRef.value.validate()
  await paymentApi.create(form)
  ElMessage.success('缴费登记成功，营收统计已可查询')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

