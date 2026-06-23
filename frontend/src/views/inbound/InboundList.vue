<template>
  <div class="wms-card">
    <div class="page-header">
      <h2 class="page-title">入库信息管理</h2>
      <el-button type="primary" @click="openAdd" id="btn-add-inbound">
        <el-icon><Plus /></el-icon>新增入库
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.materialName" placeholder="搜索物资名称..." clearable style="width:220px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="inboundNo" label="入库单号" width="200" />
      <el-table-column prop="materialName" label="物资名称" min-width="120" />
      <el-table-column prop="warehouseName" label="仓库" width="120" />
      <el-table-column prop="supplierName" label="供应商" min-width="130" show-overflow-tooltip />
      <el-table-column prop="quantity" label="入库数量" width="100" />
      <el-table-column label="单价(元)" width="100">
        <template #default="{ row }">{{ row.price ? '¥' + row.price : '-' }}</template>
      </el-table-column>
      <el-table-column label="总金额(元)" width="110">
        <template #default="{ row }">
          <span style="color:#10b981;font-weight:600">{{ row.totalAmount ? '¥' + row.totalAmount : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" label="操作员" width="100" />
      <el-table-column prop="inboundTime" label="入库时间" width="180" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确定删除该入库记录？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" title="新增入库记录" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="入库物资" prop="materialId">
          <el-select v-model="form.materialId" placeholder="选择物资" style="width:100%" @change="onMaterialChange" id="inbound-material">
            <el-option v-for="m in materialList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width:100%" @change="onWarehouseChange" id="inbound-warehouse">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="form.supplierId" placeholder="选择供应商" style="width:100%" @change="onSupplierChange" id="inbound-supplier" clearable>
            <el-option v-for="s in supplierList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0.01" :precision="2" style="width:100%" id="inbound-quantity" />
        </el-form-item>
        <el-form-item label="单价(元)">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" id="inbound-price" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" id="inbound-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { inboundApi, materialApi, warehouseApi, supplierApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const materialList = ref([])
const warehouseList = ref([])
const supplierList = ref([])
const formRef = ref(null)

const searchForm = ref({ materialName: '' })
const page = ref({ current: 1, size: 10, total: 0 })
const form = ref({
  materialId: null, materialName: '', warehouseId: null, warehouseName: '',
  supplierId: null, supplierName: '', quantity: null, price: null, remark: ''
})

const rules = {
  materialId: [{ required: true, message: '请选择物资', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await inboundApi.page({
      pageNum: page.value.current,
      pageSize: page.value.size,
      materialName: searchForm.value.materialName
    })
    tableData.value = res.data.records
    page.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value = { materialName: '' }
  page.value.current = 1
  fetchData()
}

function onMaterialChange(id) {
  const m = materialList.value.find(m => m.id === id)
  if (m) {
    form.value.materialName = m.name
    if (m.warehouseId && !form.value.warehouseId) {
      form.value.warehouseId = m.warehouseId
      form.value.warehouseName = m.warehouseName
    }
  }
}

function onWarehouseChange(id) {
  const w = warehouseList.value.find(w => w.id === id)
  if (w) form.value.warehouseName = w.name
}

function onSupplierChange(id) {
  const s = supplierList.value.find(s => s.id === id)
  if (s) form.value.supplierName = s.name
}

function openAdd() {
  form.value = {
    materialId: null, materialName: '', warehouseId: null, warehouseName: '',
    supplierId: null, supplierName: '', quantity: null, price: null, remark: ''
  }
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 100)
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await inboundApi.add(form.value)
    ElMessage.success('入库成功，库存已更新')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  await inboundApi.delete(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(async () => {
  fetchData()
  const [mr, wr, sr] = await Promise.all([materialApi.list(), warehouseApi.list(), supplierApi.list()])
  materialList.value = mr.data
  warehouseList.value = wr.data
  supplierList.value = sr.data
})
</script>
