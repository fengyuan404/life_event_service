<template>
  <section>
    <div class="toolbar">
      <div>
        <h2 class="page-title">逝者档案</h2>
        <p class="page-subtitle">关联家属并维护安葬规格、碑文等信息</p>
      </div>
      <div class="toolbar-right">
        <el-input v-model="query.keyword" clearable placeholder="逝者 / 家属 / 证件" style="width: 240px" @keyup.enter="load" />
        <el-button :icon="Search" @click="load">查询</el-button>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column label="编号" width="80"><template #default="{ row }">{{ get(row, 'deceasedId', 'deceased_id') }}</template></el-table-column>
        <el-table-column label="逝者姓名" min-width="120"><template #default="{ row }">{{ get(row, 'deceasedName', 'deceased_name') }}</template></el-table-column>
        <el-table-column label="家属" min-width="120"><template #default="{ row }">{{ get(row, 'familyName') }}</template></el-table-column>
        <el-table-column label="性别" width="80"><template #default="{ row }">{{ get(row, 'gender') }}</template></el-table-column>
        <el-table-column label="离世日期" min-width="120"><template #default="{ row }">{{ get(row, 'deathDate', 'death_date') }}</template></el-table-column>
        <el-table-column label="关系" min-width="100"><template #default="{ row }">{{ get(row, 'relationToFamily', 'relation_to_family') }}</template></el-table-column>
        <el-table-column label="规格" width="100"><template #default="{ row }">{{ get(row, 'burialType', 'burial_type') }}</template></el-table-column>
        <el-table-column label="碑文" min-width="180" show-overflow-tooltip><template #default="{ row }">{{ get(row, 'epitaph') }}</template></el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }"><el-button :icon="Edit" @click="openForm(row)" /></template>
        </el-table-column>
      </el-table>
      <el-pagination layout="total, prev, pager, next, sizes" :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="(p) => { query.page = p; load() }" @size-change="(s) => { query.size = s; load() }" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.deceasedId ? '编辑逝者' : '新增逝者'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="家属" prop="familyId">
          <el-select v-model="form.familyId" filterable style="width: 100%">
            <el-option v-for="item in families" :key="item.familyId" :label="`${item.familyName} ${item.phone}`" :value="item.familyId" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名" prop="deceasedName"><el-input v-model="form.deceasedName" /></el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender"><el-radio-button label="男" /><el-radio-button label="女" /></el-radio-group>
        </el-form-item>
        <el-form-item label="证件号码" prop="idCard"><el-input v-model="form.idCard" /></el-form-item>
        <el-form-item label="出生日期" prop="birthDate"><el-date-picker v-model="form.birthDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="离世日期" prop="deathDate"><el-date-picker v-model="form.deathDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="与家属关系" prop="relationToFamily"><el-input v-model="form.relationToFamily" /></el-form-item>
        <el-form-item label="安葬规格" prop="burialType">
          <el-select v-model="form.burialType" style="width: 100%"><el-option label="单穴" value="单穴" /><el-option label="双穴" value="双穴" /><el-option label="家庭穴" value="家庭穴" /></el-select>
        </el-form-item>
        <el-form-item label="碑文"><el-input v-model="form.epitaph" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Plus, Search } from '@element-plus/icons-vue'
import { deceasedApi, familyApi } from '../api/modules'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const families = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ keyword: '', page: 1, size: 10 })
const get = (row, ...keys) => keys.map((key) => row[key]).find((value) => value !== undefined && value !== null) || ''
const emptyForm = () => ({ deceasedId: null, familyId: null, deceasedName: '', gender: '男', idCard: '', birthDate: '', deathDate: '', relationToFamily: '', epitaph: '', burialType: '单穴' })
const form = reactive(emptyForm())
const rules = {
  familyId: [{ required: true, message: '请选择家属', trigger: 'change' }],
  deceasedName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [{ required: true, message: '请输入证件号码', trigger: 'blur' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  deathDate: [{ required: true, message: '请选择离世日期', trigger: 'change' }],
  relationToFamily: [{ required: true, message: '请输入关系', trigger: 'blur' }],
  burialType: [{ required: true, message: '请选择规格', trigger: 'change' }]
}

const normalize = (row) => ({
  deceasedId: get(row, 'deceasedId', 'deceased_id'),
  familyId: get(row, 'familyId', 'family_id'),
  deceasedName: get(row, 'deceasedName', 'deceased_name'),
  gender: get(row, 'gender'),
  idCard: get(row, 'idCard', 'id_card'),
  birthDate: get(row, 'birthDate', 'birth_date'),
  deathDate: get(row, 'deathDate', 'death_date'),
  relationToFamily: get(row, 'relationToFamily', 'relation_to_family'),
  epitaph: get(row, 'epitaph'),
  burialType: get(row, 'burialType', 'burial_type')
})

const load = async () => {
  loading.value = true
  try {
    const data = await deceasedApi.list(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadFamilies = async () => {
  families.value = (await familyApi.list({ page: 1, size: 1000 })).records
}

const openForm = (row) => {
  Object.assign(form, emptyForm(), row ? normalize(row) : {})
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value.validate()
  if (form.deceasedId) await deceasedApi.update(form.deceasedId, form)
  else await deceasedApi.create(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

onMounted(() => {
  load()
  loadFamilies()
})
</script>

