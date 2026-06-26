<template>
  <div class="wms-card">
    <div class="page-header">
      <h2 class="page-title">物资信息管理</h2>
      <div class="action-buttons" v-if="userStore.role !== 'user'">
        <el-button type="primary" @click="openAdd" id="btn-add-material">
          <el-icon><Plus /></el-icon>新增物资
        </el-button>
        <el-button type="success" plain @click="openImport" id="btn-import-material">
          <el-icon><Upload /></el-icon>导入
        </el-button>
        <el-button type="warning" plain @click="handleExport" id="btn-export-material">
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
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
          <span :style="{ color: row.quantity <= row.minStock ? 'var(--danger)' : 'var(--success)', fontWeight: '600' }">
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

    <!-- 导入物资对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入物资数据" width="580px" :close-on-click-modal="false" @close="clearImport">
      <div class="import-content">
        <!-- 步骤提示或注意事项 -->
        <div class="import-tip-card">
          <div class="tip-title">导入说明</div>
          <ul class="tip-list">
            <li>请使用系统提供的标准模板进行填写；</li>
            <li>支持**自定义修改模板**：允许调整列顺序，或将表头名称修改为同义别名（如将“物资名称”修改为“名称”）；</li>
            <li>“物资名称”、“物资编码”、“物资类型”、“所属仓库”为**必填项**；</li>
            <li>若导入的“物资类型”不存在，系统将**自动创建**该类型；若“所属仓库”不存在，该行将**报错**；</li>
            <li>若“物资编码”在系统已存在，将**覆盖/更新**已有物资的单价、预警值等信息。</li>
          </ul>
          <div class="template-download-wrapper">
            <el-button type="primary" link @click="downloadTemplate">
              <el-icon><Download /></el-icon> 点击下载标准 Excel 导入模板
            </el-button>
          </div>
        </div>

        <!-- 拖拽上传区 -->
        <el-upload
          class="import-upload"
          drag
          action=""
          :http-request="uploadFile"
          :before-upload="beforeUpload"
          :show-file-list="false"
          :disabled="importing"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text" v-if="!importing">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <div class="el-upload__text" v-else>
            正在解析并导入数据，请稍候...
          </div>
          <template #tip>
            <div class="el-upload__tip text-center">
              仅支持 xlsx / xls 格式的 Excel 文件，且大小不超过 10MB
            </div>
          </template>
        </el-upload>

        <!-- 导入结果展示 -->
        <div v-if="importResult" class="import-result-card">
          <el-alert
            :title="importResult.errors && importResult.errors.length > 0 ? '导入完成，但存在部分校验失败的数据' : '数据导入成功！'"
            :type="importResult.errors && importResult.errors.length > 0 ? 'warning' : 'success'"
            show-icon
            :closable="false"
          />
          <div class="result-summary">
            成功导入：<strong class="success-text">{{ importResult.successCount }}</strong> 条物资数据。
          </div>
          
          <!-- 错误行列表 -->
          <div v-if="importResult.errors && importResult.errors.length > 0" class="error-list-wrapper">
            <div class="error-header">错误明细 ({{ importResult.errors.length }}条)</div>
            <div class="error-container">
              <div v-for="(err, idx) in importResult.errors" :key="idx" class="error-item">
                <el-icon class="error-icon"><Warning /></el-icon>
                <span>{{ err }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false" :disabled="importing">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { materialApi, materialTypeApi, warehouseApi } from '@/api'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

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

// 导入与导出相关逻辑
const importDialogVisible = ref(false)
const importing = ref(false)
const importResult = ref(null)

function openImport() {
  importDialogVisible.value = true
}

function clearImport() {
  importResult.value = null
  importing.value = false
}

function beforeUpload(file) {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                  file.type === 'application/vnd.ms-excel' ||
                  file.name.endsWith('.xlsx') ||
                  file.name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件 (.xlsx 或 .xls)')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB')
    return false
  }
  return true
}

async function uploadFile(options) {
  importing.value = true
  importResult.value = null
  const formData = new FormData()
  formData.append('file', options.file)
  
  try {
    const res = await materialApi.importExcel(formData)
    importResult.value = res.data
    if (res.data.errors && res.data.errors.length > 0) {
      ElMessage.warning(res.message || '导入完成，但有部分数据校验失败')
    } else {
      ElMessage.success('物资数据导入成功')
    }
    fetchData()
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '导入失败，请检查模板格式')
  } finally {
    importing.value = false
  }
}

async function downloadTemplate() {
  try {
    const res = await request.get('/material/import/template', { responseType: 'blob' })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '物资导入模板.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error(error)
    ElMessage.error('下载模板失败')
  }
}

async function handleExport() {
  try {
    const res = await request.get('/material/export', {
      params: searchForm.value,
      responseType: 'blob'
    })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '物资导出列表.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败')
  }
}

onMounted(async () => {
  fetchData()
  const [tr, wr] = await Promise.all([materialTypeApi.list(), warehouseApi.list()])
  typeList.value = tr.data
  warehouseList.value = wr.data
})
</script>

<style scoped>
.action-buttons {
  display: flex;
  gap: 8px;
}

.import-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.import-tip-card {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px 16px;
}

.tip-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
  font-size: 14px;
}

.tip-list {
  padding-left: 20px;
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.tip-list li {
  margin-bottom: 4px;
}

.template-download-wrapper {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
}

.import-upload {
  width: 100%;
}

.import-upload :deep(.el-upload-dragger) {
  border: 2px dashed #cbd5e1;
  background-color: #fbfbfa;
  transition: border-color 0.2s, background-color 0.2s;
}

.import-upload :deep(.el-upload-dragger):hover {
  border-color: var(--el-color-primary);
  background-color: #f1f5f9;
}

.text-center {
  text-align: center;
  margin-top: 8px;
  color: #94a3b8;
}

.import-result-card {
  margin-top: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  background-color: #fff;
}

.result-summary {
  margin: 12px 0;
  font-size: 14px;
  color: #334155;
}

.success-text {
  color: var(--el-color-success);
  font-size: 16px;
}

.error-list-wrapper {
  border: 1px solid #fee2e2;
  background-color: #fef2f2;
  border-radius: 6px;
  padding: 12px;
}

.error-header {
  font-weight: 600;
  color: #991b1b;
  margin-bottom: 8px;
  font-size: 13px;
}

.error-container {
  max-height: 150px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.error-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  color: #b91c1c;
  line-height: 1.4;
}

.error-icon {
  margin-top: 2px;
  flex-shrink: 0;
}
</style>
