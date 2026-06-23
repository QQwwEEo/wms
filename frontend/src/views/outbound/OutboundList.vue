<template>
  <div class="wms-card">
    <div class="page-header">
      <h2 class="page-title">出库信息管理</h2>
      <el-button type="primary" @click="openAdd" id="btn-add-outbound">
        <el-icon><Plus /></el-icon>新增出库
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.materialName" placeholder="搜索物资名称..." clearable style="width:220px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="outboundNo" label="出库单号" width="210" />
      <el-table-column prop="materialName" label="物资名称" min-width="120" />
      <el-table-column prop="warehouseName" label="仓库" width="120" />
      <el-table-column prop="quantity" label="出库数量" width="100" />
      <el-table-column prop="receiver" label="领用人" width="100" />
      <el-table-column prop="operatorName" label="操作员" width="100" />
      <el-table-column prop="outboundTime" label="出库时间" width="180" />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确定删除该出库记录？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" title="新增出库记录" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="出库物资" prop="materialId">
          <el-select v-model="form.materialId" placeholder="选择物资" style="width:100%" @change="onMaterialChange" id="outbound-material">
            <el-option v-for="m in materialList" :key="m.id" :label="`${m.name} (库存:${m.quantity}${m.unit||''})`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width:100%" @change="onWarehouseChange" id="outbound-warehouse">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0.01" :precision="2" style="width:100%" id="outbound-quantity" />
        </el-form-item>
        <el-form-item label="领用人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入领用人姓名" id="outbound-receiver" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" id="outbound-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { outboundApi, materialApi, warehouseApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const materialList = ref([])
const warehouseList = ref([])
const formRef = ref(null)

const searchForm = ref({ materialName: '' })
const page = ref({ current: 1, size: 10, total: 0 })
const form = ref({
  materialId: null, materialName: '', warehouseId: null, warehouseName: '',
  quantity: null, receiver: '', remark: ''
})

const rules = {
  materialId: [{ required: true, message: '请选择物资', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }],
  receiver: [{ required: true, message: '请输入领用人', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await outboundApi.page({
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
    if (m.warehouseId) {
      form.value.warehouseId = m.warehouseId
      form.value.warehouseName = m.warehouseName
    }
  }
}

function onWarehouseChange(id) {
  const w = warehouseList.value.find(w => w.id === id)
  if (w) form.value.warehouseName = w.name
}

function openAdd() {
  form.value = {
    materialId: null, materialName: '', warehouseId: null, warehouseName: '',
    quantity: null, receiver: '', remark: ''
  }
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 100)
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await outboundApi.add(form.value)
    ElMessage.success('出库成功，库存已更新')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  await outboundApi.delete(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(async () => {
  fetchData()
  const [mr, wr] = await Promise.all([materialApi.list(), warehouseApi.list()])
  materialList.value = mr.data
  warehouseList.value = wr.data
})
</script>
