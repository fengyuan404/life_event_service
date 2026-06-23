<template>
  <section>
    <div class="toolbar">
      <div>
        <h2 class="page-title">家属管理</h2>
        <p class="page-subtitle">维护联系人、证件与地址信息</p>
      </div>
      <div class="toolbar-right">
        <el-input v-model="query.keyword" clearable placeholder="姓名 / 电话 / 证件" style="width: 240px" @keyup.enter="load" />
        <el-button :icon="Search" @click="load">查询</el-button>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="familyId" label="编号" width="80" />
        <el-table-column prop="familyName" label="家属姓名" min-width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="phone" label="电话" min-width="130" />
        <el-table-column prop="idCard" label="证件号码" min-width="150" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="110" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button :icon="Edit" @click="openForm(row)" />
              <el-button :icon="Delete" type="danger" @click="remove(row)" />
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination layout="total, prev, pager, next, sizes" :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="(p) => { query.page = p; load() }" @size-change="(s) => { query.size = s; load() }" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.familyId ? '编辑家属' : '新增家属'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="姓名" prop="familyName"><el-input v-model="form.familyName" /></el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender"><el-radio-button label="男" /><el-radio-button label="女" /></el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="证件号码" prop="idCard"><el-input v-model="form.idCard" /></el-form-item>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { familyApi } from '../api/modules'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ keyword: '', page: 1, size: 10 })
const emptyForm = () => ({ familyId: null, familyName: '', gender: '男', phone: '', idCard: '', address: '', remark: '' })
const form = reactive(emptyForm())
const rules = {
  familyName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入证件号码', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const data = await familyApi.list(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openForm = (row) => {
  Object.assign(form, emptyForm(), row || {})
  dialogVisible.value = true
}

const save = async () => {
  await formRef.value.validate()
  if (form.familyId) await familyApi.update(form.familyId, form)
  else await familyApi.create(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除家属「${row.familyName}」？`, '二次确认', { type: 'warning' })
  await familyApi.remove(row.familyId)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

