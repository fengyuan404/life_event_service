<template>
  <section>
    <div class="toolbar">
      <div>
        <h2 class="page-title">墓区墓穴管理</h2>
        <p class="page-subtitle">维护墓区基础资料与墓穴租赁状态</p>
      </div>
      <div class="toolbar-right">
        <el-input v-model="query.keyword" clearable placeholder="编码 / 名称 / 状态" style="width: 240px" @keyup.enter="load" />
        <el-button :icon="Search" @click="load">查询</el-button>
      </div>
    </div>

    <div class="panel">
      <el-tabs v-model="active" @tab-change="load">
        <el-tab-pane label="墓穴资源" name="graves">
          <div class="toolbar"><span></span><el-button type="primary" :icon="Plus" @click="openGrave()">新增墓穴</el-button></div>
          <el-table v-loading="loading" :data="graves" stripe>
            <el-table-column label="编号" width="80"><template #default="{ row }">{{ get(row, 'graveId', 'grave_id') }}</template></el-table-column>
            <el-table-column label="墓穴编码" min-width="120"><template #default="{ row }">{{ get(row, 'graveCode', 'grave_code') }}</template></el-table-column>
            <el-table-column label="墓区" min-width="120"><template #default="{ row }">{{ get(row, 'areaName') }}</template></el-table-column>
            <el-table-column label="位置" min-width="160"><template #default="{ row }">{{ get(row, 'locationDesc', 'location_desc') }}</template></el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }"><span :class="statusClass(get(row, 'status'))">{{ statusText(get(row, 'status')) }}</span></template>
            </el-table-column>
            <el-table-column label="价格" width="120"><template #default="{ row }"><span class="amount">￥{{ get(row, 'rentPrice', 'rent_price') }}</span></template></el-table-column>
            <el-table-column label="年限" width="90"><template #default="{ row }">{{ get(row, 'maxYears', 'max_years') }}</template></el-table-column>
            <el-table-column label="操作" width="90" fixed="right"><template #default="{ row }"><el-button :icon="Edit" @click="openGrave(row)" /></template></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="墓区资料" name="areas">
          <div class="toolbar"><span></span><el-button type="primary" :icon="Plus" @click="openArea()">新增墓区</el-button></div>
          <el-table v-loading="loading" :data="areas" stripe>
            <el-table-column prop="areaId" label="编号" width="80" />
            <el-table-column prop="areaCode" label="编码" width="100" />
            <el-table-column prop="areaName" label="名称" min-width="120" />
            <el-table-column prop="areaSize" label="面积" width="110" />
            <el-table-column prop="basePrice" label="基础价格" width="120" />
            <el-table-column prop="environmentDesc" label="环境说明" min-width="220" show-overflow-tooltip />
            <el-table-column label="状态" width="110"><template #default="{ row }"><span :class="statusClass(row.status)">{{ row.status === 'active' ? '启用' : '停用' }}</span></template></el-table-column>
            <el-table-column label="操作" width="90" fixed="right"><template #default="{ row }"><el-button :icon="Edit" @click="openArea(row)" /></template></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-pagination layout="total, prev, pager, next, sizes" :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="(p) => { query.page = p; load() }" @size-change="(s) => { query.size = s; load() }" />
    </div>

    <el-dialog v-model="graveDialog" :title="graveForm.graveId ? '编辑墓穴' : '新增墓穴'" width="560px">
      <el-form ref="graveRef" :model="graveForm" :rules="graveRules" label-width="92px">
        <el-form-item label="墓区" prop="areaId"><el-select v-model="graveForm.areaId" style="width: 100%"><el-option v-for="a in allAreas" :key="a.areaId" :label="a.areaName" :value="a.areaId" /></el-select></el-form-item>
        <el-form-item label="编码" prop="graveCode"><el-input v-model="graveForm.graveCode" /></el-form-item>
        <el-form-item label="位置" prop="locationDesc"><el-input v-model="graveForm.locationDesc" /></el-form-item>
        <el-form-item label="面积" prop="graveSize"><el-input-number v-model="graveForm.graveSize" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-select v-model="graveForm.status" style="width: 100%"><el-option label="空置" value="empty" /><el-option label="已租赁" value="rented" /><el-option label="到期" value="expired" /><el-option label="维护" value="maintenance" /></el-select></el-form-item>
        <el-form-item label="租赁价格" prop="rentPrice"><el-input-number v-model="graveForm.rentPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="最长年限" prop="maxYears"><el-input-number v-model="graveForm.maxYears" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="graveForm.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="graveDialog = false">取消</el-button><el-button type="primary" @click="saveGrave">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="areaDialog" :title="areaForm.areaId ? '编辑墓区' : '新增墓区'" width="560px">
      <el-form ref="areaRef" :model="areaForm" :rules="areaRules" label-width="92px">
        <el-form-item label="编码" prop="areaCode"><el-input v-model="areaForm.areaCode" /></el-form-item>
        <el-form-item label="名称" prop="areaName"><el-input v-model="areaForm.areaName" /></el-form-item>
        <el-form-item label="面积" prop="areaSize"><el-input-number v-model="areaForm.areaSize" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="基础价格" prop="basePrice"><el-input-number v-model="areaForm.basePrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="环境说明"><el-input v-model="areaForm.environmentDesc" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-select v-model="areaForm.status" style="width: 100%"><el-option label="启用" value="active" /><el-option label="停用" value="disabled" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="areaDialog = false">取消</el-button><el-button type="primary" @click="saveArea">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Plus, Search } from '@element-plus/icons-vue'
import { graveApi } from '../api/modules'

const active = ref('graves')
const loading = ref(false)
const graves = ref([])
const areas = ref([])
const allAreas = ref([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })
const get = (row, ...keys) => keys.map((key) => row[key]).find((value) => value !== undefined && value !== null) || ''
const statusText = (value) => ({ empty: '空置', rented: '已租赁', expired: '到期', maintenance: '维护' }[value] || value)
const statusClass = (value) => ['status-dot', value === 'empty' || value === 'active' ? '' : value === 'rented' ? 'warn' : 'danger'].join(' ')

const graveDialog = ref(false)
const areaDialog = ref(false)
const graveRef = ref()
const areaRef = ref()
const blankGrave = () => ({ graveId: null, areaId: null, graveCode: '', locationDesc: '', graveSize: 1, status: 'empty', rentPrice: 0, maxYears: 20, remark: '' })
const blankArea = () => ({ areaId: null, areaCode: '', areaName: '', areaSize: 1, basePrice: 0, environmentDesc: '', status: 'active' })
const graveForm = reactive(blankGrave())
const areaForm = reactive(blankArea())
const graveRules = {
  areaId: [{ required: true, message: '请选择墓区', trigger: 'change' }],
  graveCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  locationDesc: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  graveSize: [{ required: true, message: '请输入面积', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  rentPrice: [{ required: true, message: '请输入价格', trigger: 'change' }],
  maxYears: [{ required: true, message: '请输入年限', trigger: 'change' }]
}
const areaRules = {
  areaCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  areaName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  areaSize: [{ required: true, message: '请输入面积', trigger: 'change' }],
  basePrice: [{ required: true, message: '请输入价格', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const normalizeGrave = (row) => ({
  graveId: get(row, 'graveId', 'grave_id'),
  areaId: get(row, 'areaId', 'area_id'),
  graveCode: get(row, 'graveCode', 'grave_code'),
  locationDesc: get(row, 'locationDesc', 'location_desc'),
  graveSize: Number(get(row, 'graveSize', 'grave_size')),
  status: get(row, 'status'),
  rentPrice: Number(get(row, 'rentPrice', 'rent_price')),
  maxYears: Number(get(row, 'maxYears', 'max_years')),
  remark: get(row, 'remark')
})

const load = async () => {
  loading.value = true
  try {
    const data = active.value === 'graves' ? await graveApi.list(query) : await graveApi.areas(query)
    if (active.value === 'graves') graves.value = data.records
    else areas.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadAllAreas = async () => {
  allAreas.value = (await graveApi.areas({ page: 1, size: 1000 })).records
}

const openGrave = (row) => {
  Object.assign(graveForm, blankGrave(), row ? normalizeGrave(row) : {})
  graveDialog.value = true
}

const saveGrave = async () => {
  await graveRef.value.validate()
  if (graveForm.graveId) await graveApi.update(graveForm.graveId, graveForm)
  else await graveApi.create(graveForm)
  ElMessage.success('保存成功')
  graveDialog.value = false
  load()
}

const openArea = (row) => {
  Object.assign(areaForm, blankArea(), row || {})
  areaDialog.value = true
}

const saveArea = async () => {
  await areaRef.value.validate()
  if (areaForm.areaId) await graveApi.updateArea(areaForm.areaId, areaForm)
  else await graveApi.createArea(areaForm)
  ElMessage.success('保存成功')
  areaDialog.value = false
  load()
  loadAllAreas()
}

onMounted(() => {
  load()
  loadAllAreas()
})
</script>

