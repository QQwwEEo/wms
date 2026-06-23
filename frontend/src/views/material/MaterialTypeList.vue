<template>
  <div class="wms-card">
    <div class="page-header">
      <h2 class="page-title">物资类型管理</h2>
      <el-button type="primary" @click="openAdd" id="btn-add-material-type">
        <el-icon><Plus /></el-icon>新增类型
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="搜索类型名称..." clearable style="width:220px" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="类型名称" min-width="120" />
      <el-table-column prop="code" label="类型编码" width="120" />
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <span :class="['status-badge', row.status === 1 ? 'active' : 'inactive']">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该类型？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入类型名称" id="mt-name" />
        </el-form-item>
        <el-form-item label="类型编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入类型编码" id="mt-code" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注信息" id="mt-remark" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" id="mt-status" />
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
import { materialTypeApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const tableData = ref([])
const formRef = ref(null)
const isEdit = ref(false)

const searchForm = ref({ name: '' })
const page = ref({ current: 1, size: 10, total: 0 })
const form = ref({ id: null, name: '', code: '', remark: '', status: 1 })

const rules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await materialTypeApi.page({
      pageNum: page.value.current,
      pageSize: page.value.size,
      name: searchForm.value.name
    })
    tableData.value = res.data.records
    page.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.value = { name: '' }
  page.value.current = 1
  fetchData()
}

function openAdd() {
  isEdit.value = false
  dialogTitle.value = '新增物资类型'
  form.value = { id: null, name: '', code: '', remark: '', status: 1 }
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 100)
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑物资类型'
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await materialTypeApi.update(form.value)
    } else {
      await materialTypeApi.add(form.value)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  await materialTypeApi.delete(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>
