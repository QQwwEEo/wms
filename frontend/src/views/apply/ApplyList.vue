<template>
  <div class="wms-card">
    <div class="page-header">
      <h2 class="page-title">物资申请管理</h2>
      <el-button type="primary" @click="openAdd" id="btn-add-apply">
        <el-icon><Plus /></el-icon>发起申请
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.materialName" placeholder="搜索物资名称..." clearable style="width:200px" />
      <el-select v-model="searchForm.status" placeholder="申请状态" clearable style="width:130px">
        <el-option label="待审批" value="pending" />
        <el-option label="已批准" value="approved" />
        <el-option label="已拒绝" value="rejected" />
      </el-select>
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
      <el-table-column prop="applyNo" label="申请单号" width="200" />
      <el-table-column prop="materialName" label="物资名称" min-width="120" />
      <el-table-column prop="quantity" label="申请数量" width="100" />
      <el-table-column prop="applyReason" label="申请原因" min-width="150" show-overflow-tooltip />
      <el-table-column prop="userName" label="申请人" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span :class="['status-badge', row.status]">{{ statusText(row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="userStore.isAdmin && row.status === 'pending'"
            type="warning"
            link
            @click="openApprove(row)"
          >审批</el-button>
          <el-popconfirm title="确定删除该申请？" @confirm="handleDelete(row.id)">
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

    <!-- 申请对话框 -->
    <el-dialog v-model="dialogVisible" title="发起物资申请" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="申请物资" prop="materialId">
          <el-select v-model="form.materialId" placeholder="选择物资" style="width:100%" @change="onMaterialChange" id="apply-material">
            <el-option v-for="m in materialList" :key="m.id" :label="`${m.name} (库存:${m.quantity}${m.unit||''})`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width:100%" id="apply-quantity" />
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input v-model="form.applyReason" type="textarea" :rows="3" placeholder="请说明申请原因" id="apply-reason" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveVisible" title="审批物资申请" width="480px" :close-on-click-modal="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="物资名称">{{ currentApply?.materialName }}</el-descriptions-item>
        <el-descriptions-item label="申请数量">{{ currentApply?.quantity }}</el-descriptions-item>
        <el-descriptions-item label="申请原因">{{ currentApply?.applyReason }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentApply?.userName }}</el-descriptions-item>
      </el-descriptions>
      <el-form style="margin-top:16px">
        <el-form-item label="审批结果" label-width="80px">
          <el-radio-group v-model="approveForm.status" id="approve-status">
            <el-radio-button value="approved">批准</el-radio-button>
            <el-radio-button value="rejected">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批备注" label-width="80px">
          <el-input v-model="approveForm.approveRemark" type="textarea" :rows="2" id="approve-remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveLoading" @click="handleApprove">确认审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { applyApi, materialApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const approveLoading = ref(false)
const dialogVisible = ref(false)
const approveVisible = ref(false)
const tableData = ref([])
const materialList = ref([])
const formRef = ref(null)
const currentApply = ref(null)

const searchForm = ref({ materialName: '', status: '' })
const page = ref({ current: 1, size: 10, total: 0 })
const form = ref({ materialId: null, materialName: '', quantity: 1, applyReason: '' })
const approveForm = ref({ status: 'approved', approveRemark: '' })

const rules = {
  materialId: [{ required: true, message: '请选择物资', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入申请数量', trigger: 'blur' }]
}

function statusText(s) {
  return { pending: '待审批', approved: '已批准', rejected: '已拒绝' }[s] || s
}

async function fetchData() {
  loading.value = true
  try {
    const res = await applyApi.page({
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
  searchForm.value = { materialName: '', status: '' }
  page.value.current = 1
  fetchData()
}

function onMaterialChange(id) {
  const m = materialList.value.find(m => m.id === id)
  if (m) form.value.materialName = m.name
}

function openAdd() {
  form.value = { materialId: null, materialName: '', quantity: 1, applyReason: '' }
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 100)
}

function openApprove(row) {
  currentApply.value = row
  approveForm.value = { status: 'approved', approveRemark: '' }
  approveVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await applyApi.add(form.value)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleApprove() {
  approveLoading.value = true
  try {
    await applyApi.approve(currentApply.value.id, approveForm.value)
    ElMessage.success('审批完成')
    approveVisible.value = false
    fetchData()
  } finally {
    approveLoading.value = false
  }
}

async function handleDelete(id) {
  await applyApi.delete(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(async () => {
  fetchData()
  const res = await materialApi.list()
  materialList.value = res.data
})
</script>
