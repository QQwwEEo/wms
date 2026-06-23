<template>
  <div class="wms-card">
    <div class="page-header">
      <h2 class="page-title">物资信息管理</h2>
      <el-button v-if="userStore.role !== 'user'" type="primary" @click="openAdd" id="btn-add-material">
        <el-icon><Plus /></el-icon>新增物资
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="搜索物资名称..." clearable style="width:200px" @keyup.enter="fetchData" />
      <el-select v-model="searchForm.typeId" placeholder="物资类型" clearable style="width:140px">
        <el-option v-for="t in typeList" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-model="searchForm.warehouseId" placeholder="所属仓库" clearable style="width:140px">
        <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
      </el-select>
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="name" label="物资名称" min-width="120" />
      <el-table-column prop="code" label="编码" width="100" />
      <el-table-column prop="typeName" label="类型" width="100" />
      <el-table-column prop="warehouseName" label="仓库" width="120" />
      <el-table-column prop="unit" label="单位" width="70" />
      <el-table-column label="库存数量" width="100">
        <template #default="{ row }">
          <span :style="{ color: row.quantity <= row.minStock ? '#ef4444' : '#10b981', fontWeight: '600' }">
            {{ row.quantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="minStock" label="最低预警值" width="100" />
      <el-table-column label="单价(元)" width="100">
        <template #default="{ row }">{{ row.price ? '¥' + row.price : '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <span :class="['status-badge', row.status === 1 ? 'active' : 'inactive']">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column v-if="userStore.role !== 'user'" label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该物资？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page.current"
      v-model:page-size="page.size"
      :total="page.total"
      layout="total, sizes, prev, pager, next"
      :page-sizes="[10, 20, 50]"
      @change="fetchData"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="物资名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物资名称" id="mat-name" />
        </el-form-item>
        <el-form-item label="物资编码">
          <el-input v-model="form.code" placeholder="物资编码" id="mat-code" />
        </el-form-item>
        <el-form-item label="物资类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="选择物资类型" style="width:100%" @change="onTypeChange" id="mat-type">
            <el-option v-for="t in typeList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width:100%" @change="onWarehouseChange" id="mat-warehouse">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如：箱、个、台" id="mat-unit" />
        </el-form-item>
        <el-form-item label="单价(元)">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" id="mat-price" />
        </el-form-item>
        <el-form-item label="最低预警值">
          <el-input-number v-model="form.minStock" :min="0" :precision="2" style="width:100%" id="mat-min-stock" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" id="mat-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { materialApi, materialTypeApi, warehouseApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tableData = ref([])
const typeList = ref([])
const warehouseList = ref([])
const formRef = ref(null)
const isEdit = ref(false)

const searchForm = ref({ name: '', typeId: null, warehouseId: null })
const page = ref({ current: 1, size: 10, total: 0 })
const form = ref({
  id: null, name: '', code: '', typeId: null, typeName: '',
  warehouseId: null, warehouseName: '', unit: '', price: null, minStock: 5.00, remark: '', status: 1
})

const rules = {
  name: [{ required: true, message: '请输入物资名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await materialApi.page({
      pageNum: page.value.current,
      pageSize: page.value.size,
      ...searchForm.value
    })
    tableData.value = res.data.records
    page.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value = { name: '', typeId: null, warehouseId: null }
  page.value.current = 1
  fetchData()
}

function onTypeChange(id) {
  const t = typeList.value.find(t => t.id === id)
  if (t) form.value.typeName = t.name
}

function onWarehouseChange(id) {
  const w = warehouseList.value.find(w => w.id === id)
  if (w) form.value.warehouseName = w.name
}

function openAdd() {
  isEdit.value = false
  dialogTitle.value = '新增物资'
  form.value = { id: null, name: '', code: '', typeId: null, typeName: '',
    warehouseId: null, warehouseName: '', unit: '', price: null, minStock: 5.00, remark: '', status: 1 }
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 100)
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑物资'
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await materialApi.update(form.value)
    } else {
      await materialApi.add(form.value)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  await materialApi.delete(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(async () => {
  fetchData()
  const [tr, wr] = await Promise.all([materialTypeApi.list(), warehouseApi.list()])
  typeList.value = tr.data
  warehouseList.value = wr.data
})
</script>
