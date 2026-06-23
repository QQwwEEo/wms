<template>
  <div class="wms-card">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">仓库管理</h2>
      <el-button type="primary" @click="openAdd" id="btn-add-warehouse">
        <el-icon><Plus /></el-icon>新增
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="搜索仓库名称..." clearable style="width:220px" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="name" label="仓库名称" min-width="150" />
      <el-table-column prop="location" label="位置" min-width="180" show-overflow-tooltip />
      <el-table-column prop="capacity" label="容量" min-width="100" />
      <el-table-column prop="managerName" label="管理员" min-width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page.current"
      v-model:page-size="page.size"
      :total="page.total"
      layout="total, sizes, prev, pager, next"
      :page-sizes="[10, 20, 50]"
      @change="fetchData"
      style="margin-top:16px;justify-content:flex-end"
    />

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="0" style="width:100%" placeholder="请输入容量" />
        </el-form-item>
        <el-form-item label="管理员姓名" prop="managerName">
          <el-input v-model="form.managerName" placeholder="请输入管理员姓名" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { warehouseApi } from '@/api'

const searchForm = ref({ name: '' })
const tableData = ref([])
const loading = ref(false)
const page = ref({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增仓库')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = ref({
  id: null,
  name: '',
  location: '',
  capacity: 0,
  managerName: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: page.value.current,
      size: page.value.size,
      name: searchForm.value.name || undefined
    }
    const res = await warehouseApi.page(params)
    if (res.data) {
      tableData.value = res.data.records || []
      page.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { name: '' }
  page.value.current = 1
  fetchData()
}

const openAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增仓库'
  form.value = { id: null, name: '', location: '', capacity: 0, managerName: '', remark: '' }
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑仓库'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await warehouseApi.update(form.value)
        ElMessage.success('更新成功')
      } else {
        await warehouseApi.add(form.value)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (e) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await warehouseApi.delete(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}
.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>
