<template>
  <section>
    <div class="toolbar">
      <div>
        <h2 class="page-title">祭扫预约</h2>
        <p class="page-subtitle">创建预约并完成到场核销或取消</p>
      </div>
      <div class="toolbar-right">
        <el-input v-model="query.keyword" clearable placeholder="家属 / 墓穴 / 状态" style="width: 240px" @keyup.enter="load" />
        <el-button :icon="Search" @click="load">查询</el-button>
        <el-button type="primary" :icon="Plus" @click="openForm">新增预约</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column label="编号" width="80"><template #default="{ row }">{{ get(row, 'bookId', 'book_id') }}</template></el-table-column>
        <el-table-column label="家属" min-width="120"><template #default="{ row }">{{ get(row, 'familyName') }}</template></el-table-column>
        <el-table-column label="墓穴" min-width="110"><template #default="{ row }">{{ get(row, 'graveCode') }}</template></el-table-column>
        <el-table-column label="预约时间" min-width="170"><template #default="{ row }">{{ get(row, 'bookTime', 'book_time') }}</template></el-table-column>
        <el-table-column label="人数" width="80"><template #default="{ row }">{{ get(row, 'visitorCount', 'visitor_count') }}</template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><span :class="statusClass(get(row, 'checkinStatus', 'checkin_status'))">{{ statusText(get(row, 'checkinStatus', 'checkin_status')) }}</span></template></el-table-column>
        <el-table-column label="登记人" min-width="110"><template #default="{ row }">{{ get(row, 'staffName') }}</template></el-table-column>
        <el-table-column label="特殊需求" min-width="180" show-overflow-tooltip><template #default="{ row }">{{ get(row, 'specialNeed', 'special_need') }}</template></el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button :icon="CircleCheck" :disabled="get(row, 'checkinStatus', 'checkin_status') !== 'booked'" @click="checkin(row)">核销</el-button>
              <el-button :icon="Close" type="danger" :disabled="get(row, 'checkinStatus', 'checkin_status') !== 'booked'" @click="cancel(row)">取消</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="total, prev, pager, next, sizes" :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="(p) => { query.page = p; load() }" @size-change="(s) => { query.size = s; load() }" />
    </div>

    <el-dialog v-model="dialogVisible" title="新增祭扫预约" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="家属" prop="familyId"><el-select v-model="form.familyId" filterable style="width: 100%"><el-option v-for="item in families" :key="item.familyId" :label="`${item.familyName} ${item.phone}`" :value="item.familyId" /></el-select></el-form-item>
        <el-form-item label="墓穴" prop="graveId"><el-select v-model="form.graveId" filterable style="width: 100%"><el-option v-for="item in graves" :key="get(item, 'graveId', 'grave_id')" :label="`${get(item, 'graveCode', 'grave_code')} / ${get(item, 'areaName')}`" :value="Number(get(item, 'graveId', 'grave_id'))" /></el-select></el-form-item>
        <el-form-item label="登记员工" prop="staffId"><el-select v-model="form.staffId" filterable style="width: 100%"><el-option v-for="item in staff" :key="item.staffId" :label="`${item.staffName} (${item.role})`" :value="item.staffId" /></el-select></el-form-item>
        <el-form-item label="预约时间" prop="bookTime"><el-date-picker v-model="form.bookTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="到场人数" prop="visitorCount"><el-input-number v-model="form.visitorCount" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="特殊需求"><el-input v-model="form.specialNeed" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, Close, Plus, Search } from '@element-plus/icons-vue'
import { bookApi, familyApi, graveApi, staffApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const families = ref([])
const graves = ref([])
const staff = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ keyword: '', page: 1, size: 10 })
const get = (row, ...keys) => keys.map((key) => row[key]).find((value) => value !== undefined && value !== null) || ''
const statusText = (value) => ({ booked: '已预约', checked_in: '已核销', cancelled: '已取消' }[value] || value)
const statusClass = (value) => ['status-dot', value === 'booked' ? 'warn' : value === 'checked_in' ? '' : 'danger'].join(' ')
const nowIso = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 19)
const blank = () => ({ familyId: null, graveId: null, staffId: auth.staffId, bookTime: nowIso(), visitorCount: 1, specialNeed: '' })
const form = reactive(blank())
const rules = {
  familyId: [{ required: true, message: '请选择家属', trigger: 'change' }],
  graveId: [{ required: true, message: '请选择墓穴', trigger: 'change' }],
  staffId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  bookTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }],
  visitorCount: [{ required: true, message: '请输入人数', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const data = await bookApi.list(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openForm = async () => {
  Object.assign(form, blank())
  families.value = (await familyApi.list({ page: 1, size: 1000 })).records
  graves.value = (await graveApi.list({ page: 1, size: 1000 })).records
  staff.value = await staffApi.list()
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value.validate()
  await bookApi.create(form)
  ElMessage.success('预约创建成功')
  dialogVisible.value = false
  load()
}

const checkin = async (row) => {
  await bookApi.checkin(get(row, 'bookId', 'book_id'))
  ElMessage.success('核销成功')
  load()
}

const cancel = async (row) => {
  await ElMessageBox.confirm('确认取消该预约？', '二次确认', { type: 'warning' })
  await bookApi.cancel(get(row, 'bookId', 'book_id'))
  ElMessage.success('预约已取消')
  load()
}

onMounted(load)
</script>

