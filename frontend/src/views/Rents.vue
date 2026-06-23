<template>
  <section>
    <div class="toolbar">
      <div>
        <h2 class="page-title">租赁办理</h2>
        <p class="page-subtitle">选择家属、逝者、空置墓穴并生成租赁订单</p>
      </div>
      <div class="toolbar-right">
        <el-input v-model="query.keyword" clearable placeholder="家属 / 逝者 / 墓穴" style="width: 240px" @keyup.enter="load" />
        <el-button :icon="Search" @click="load">查询</el-button>
        <el-button v-if="auth.role !== 'finance'" type="primary" :icon="Plus" @click="openForm">办理租赁</el-button>
      </div>
    </div>

    <el-alert v-if="expiring.length" type="warning" show-icon :closable="false" class="expire-alert">
      <template #title>未来 90 天内有 {{ expiring.length }} 条租赁即将到期，可在列表中联系家属续费。</template>
    </el-alert>

    <div class="panel">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column label="订单号" width="90"><template #default="{ row }">{{ get(row, 'rentId', 'rent_id') }}</template></el-table-column>
        <el-table-column label="家属" min-width="110"><template #default="{ row }">{{ get(row, 'familyName') }}</template></el-table-column>
        <el-table-column label="逝者" min-width="110"><template #default="{ row }">{{ get(row, 'deceasedName') }}</template></el-table-column>
        <el-table-column label="墓穴" min-width="110"><template #default="{ row }">{{ get(row, 'graveCode') }}</template></el-table-column>
        <el-table-column label="经办人" min-width="110"><template #default="{ row }">{{ get(row, 'staffName') }}</template></el-table-column>
        <el-table-column label="开始日期" min-width="120"><template #default="{ row }">{{ get(row, 'startDate', 'start_date') }}</template></el-table-column>
        <el-table-column label="到期日期" min-width="120"><template #default="{ row }">{{ get(row, 'expireDate', 'expire_date') }}</template></el-table-column>
        <el-table-column label="金额" width="120"><template #default="{ row }"><span class="amount">￥{{ get(row, 'totalAmount', 'total_amount') }}</span></template></el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><span :class="statusClass(get(row, 'rentStatus', 'rent_status'))">{{ rentStatus(get(row, 'rentStatus', 'rent_status')) }}</span></template>
        </el-table-column>
      </el-table>
      <el-pagination layout="total, prev, pager, next, sizes" :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="(p) => { query.page = p; load() }" @size-change="(s) => { query.size = s; load() }" />
    </div>

    <el-dialog v-model="dialogVisible" title="办理租赁" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="家属" prop="familyId">
          <el-select v-model="form.familyId" filterable style="width: 100%"><el-option v-for="item in families" :key="item.familyId" :label="`${item.familyName} ${item.phone}`" :value="item.familyId" /></el-select>
        </el-form-item>
        <el-form-item label="逝者" prop="deceasedId">
          <el-select v-model="form.deceasedId" filterable style="width: 100%"><el-option v-for="item in deceasedList" :key="get(item, 'deceasedId', 'deceased_id')" :label="`${get(item, 'deceasedName', 'deceased_name')} / ${get(item, 'familyName')}`" :value="Number(get(item, 'deceasedId', 'deceased_id'))" /></el-select>
        </el-form-item>
        <el-form-item label="空置墓穴" prop="graveId">
          <el-select v-model="form.graveId" filterable style="width: 100%" @change="onGraveChange">
            <el-option v-for="item in emptyGraves" :key="item.graveId" :label="`${item.graveCode} / ${item.areaName} / ￥${item.rentPrice}`" :value="item.graveId" />
          </el-select>
        </el-form-item>
        <el-form-item label="经办员工" prop="staffId">
          <el-select v-model="form.staffId" filterable style="width: 100%"><el-option v-for="item in staff" :key="item.staffId" :label="`${item.staffName} (${item.role})`" :value="item.staffId" /></el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="到期日期" prop="expireDate"><el-date-picker v-model="form.expireDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="订单金额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">创建订单</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { deceasedApi, familyApi, graveApi, rentApi, staffApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const expiring = ref([])
const families = ref([])
const deceasedList = ref([])
const emptyGraves = ref([])
const staff = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ keyword: '', page: 1, size: 10 })
const get = (row, ...keys) => keys.map((key) => row[key]).find((value) => value !== undefined && value !== null) || ''
const rentStatus = (value) => ({ active: '有效', expired: '到期', cancelled: '已取消' }[value] || value)
const statusClass = (value) => ['status-dot', value === 'active' ? '' : value === 'expired' ? 'warn' : 'danger'].join(' ')
const today = new Date().toISOString().slice(0, 10)
const blank = () => ({ familyId: null, deceasedId: null, graveId: null, staffId: auth.staffId, startDate: today, expireDate: '', totalAmount: 0 })
const form = reactive(blank())
const rules = {
  familyId: [{ required: true, message: '请选择家属', trigger: 'change' }],
  deceasedId: [{ required: true, message: '请选择逝者', trigger: 'change' }],
  graveId: [{ required: true, message: '请选择空置墓穴', trigger: 'change' }],
  staffId: [{ required: true, message: '请选择经办员工', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  expireDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
  totalAmount: [{ required: true, message: '请输入金额', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const data = await rentApi.list(query)
    rows.value = data.records
    total.value = data.total
    expiring.value = await rentApi.expiring()
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  families.value = (await familyApi.list({ page: 1, size: 1000 })).records
  deceasedList.value = (await deceasedApi.list({ page: 1, size: 1000 })).records
  emptyGraves.value = await graveApi.empty()
  staff.value = await staffApi.list()
}

const openForm = async () => {
  Object.assign(form, blank())
  await loadOptions()
  dialogVisible.value = true
}

const onGraveChange = (id) => {
  const grave = emptyGraves.value.find((item) => item.graveId === id)
  if (grave) form.totalAmount = Number(grave.rentPrice)
}

const save = async () => {
  await formRef.value.validate()
  await rentApi.create(form)
  ElMessage.success('租赁订单已创建，墓穴状态已更新')
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.expire-alert {
  margin-bottom: 14px;
}
</style>

