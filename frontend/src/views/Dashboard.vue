<template>
  <div class="dashboard">
    <!-- 顶部欢迎栏 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="avatar-lg" :style="{ background: avatarColor }">{{ userInitial }}</div>
        <div>
          <h2>你好，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}！👋</h2>
          <p>{{ greeting }} · {{ currentDate }}</p>
        </div>
      </div>
      <el-tag :type="roleTagType" size="large" effect="dark">{{ roleLabel }}</el-tag>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="stat-card"
        :style="{ '--card-color': card.color }"
      >
        <div class="stat-icon" :style="{ backgroundColor: card.color + '1a' }">
          <el-icon size="24" :color="card.color"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">
            <span v-if="statsLoaded">{{ stats[card.key] ?? 0 }}</span>
            <el-skeleton v-else :rows="1" animated style="width:60px" />
          </div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
        <div class="stat-bg-icon">
          <el-icon size="60" :color="card.color" style="opacity:0.08"><component :is="card.icon" /></el-icon>
        </div>
      </div>
    </div>

    <!-- 图表展示区 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-title">物资分类统计</div>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">近7日出入库趋势</div>
        <div ref="lineChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card calendar-card">
        <div class="calendar-header">
          <div class="calendar-month">{{ currentYear }}年{{ currentMonth + 1 }}月</div>
          <div class="calendar-nav">
            <button class="nav-btn" @click="prevMonth" id="btn-calendar-prev">
              <el-icon size="12"><ArrowLeft /></el-icon>
            </button>
            <button class="nav-btn" @click="nextMonth" id="btn-calendar-next">
              <el-icon size="12"><ArrowRight /></el-icon>
            </button>
          </div>
        </div>
        <div class="calendar-weekdays">
          <div v-for="w in ['日', '一', '二', '三', '四', '五', '六']" :key="w" class="weekday">{{ w }}</div>
        </div>
        <div class="calendar-days">
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            :class="[
              'day-cell',
              {
                'not-current': !day.isCurrentMonth,
                'is-today': day.isToday,
                'is-selected': day.isSelected
              }
            ]"
            @click="selectDate(day)"
          >
            <span class="day-num">{{ day.dayNum }}</span>
            <span v-if="day.hasActivity" class="activity-dot"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作区 -->
    <div class="section-header">
      <h3>快捷操作</h3>
    </div>
    <div class="quick-actions">
      <div
        v-for="action in quickActions"
        :key="action.path"
        class="quick-action-card"
        @click="router.push(action.path)"
      >
        <div class="qa-icon" :style="{ background: action.gradient }">
          <el-icon size="22" color="#fff"><component :is="action.icon" /></el-icon>
        </div>
        <div class="qa-info">
          <div class="qa-title">{{ action.title }}</div>
          <div class="qa-desc">{{ action.desc }}</div>
        </div>
        <el-icon color="#94a3b8"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 底部信息 -->
    <div class="dashboard-footer">
      <div class="info-card">
        <el-icon color="#4f46e5"><InfoFilled /></el-icon>
        <div>
          <strong>系统说明</strong>
          <p>当前版本 v1.0.1 | 后端 SpringBoot 3.x + MyBatis-Plus | 前端 Vue 3 + Element Plus</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { dashboardApi, materialApi, inboundApi, outboundApi } from '@/api'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({})
const statsLoaded = ref(false)

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChartInstance = null
let lineChartInstance = null
let resizeObserver = null

// 日历组件相关逻辑与状态
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth()) // 0-11
const selectedDateStr = ref(new Date().toISOString().substring(0, 10))

const inboundDates = ref(new Set())
const outboundDates = ref(new Set())

const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  
  const firstDay = new Date(year, month, 1)
  const startDayOfWeek = firstDay.getDay() // 0: Sun, 1: Mon...
  
  const totalDays = new Date(year, month + 1, 0).getDate()
  const prevTotalDays = new Date(year, month, 0).getDate()
  
  const days = []
  
  // 1. 上月余留天数填充
  for (let i = startDayOfWeek - 1; i >= 0; i--) {
    const d = new Date(year, month - 1, prevTotalDays - i)
    const dateStr = formatDate(d)
    days.push({
      date: d,
      dayNum: prevTotalDays - i,
      isCurrentMonth: false,
      isToday: isSameDay(d, new Date()),
      isSelected: selectedDateStr.value === dateStr,
      hasActivity: inboundDates.value.has(dateStr) || outboundDates.value.has(dateStr)
    })
  }
  
  // 2. 本月天数
  for (let i = 1; i <= totalDays; i++) {
    const d = new Date(year, month, i)
    const dateStr = formatDate(d)
    days.push({
      date: d,
      dayNum: i,
      isCurrentMonth: true,
      isToday: isSameDay(d, new Date()),
      isSelected: selectedDateStr.value === dateStr,
      hasActivity: inboundDates.value.has(dateStr) || outboundDates.value.has(dateStr)
    })
  }
  
  // 3. 下月天数填充，补足网格 (6行 x 7列 = 42格子，或者5行)
  const remaining = 42 - days.length
  const finalPadding = remaining <= 7 ? remaining : (remaining % 7)
  const totalSlots = days.length + finalPadding
  const targetTotal = totalSlots <= 35 ? 35 : 42
  const paddingCount = targetTotal - days.length
  
  for (let i = 1; i <= paddingCount; i++) {
    const d = new Date(year, month + 1, i)
    const dateStr = formatDate(d)
    days.push({
      date: d,
      dayNum: i,
      isCurrentMonth: false,
      isToday: isSameDay(d, new Date()),
      isSelected: selectedDateStr.value === dateStr,
      hasActivity: inboundDates.value.has(dateStr) || outboundDates.value.has(dateStr)
    })
  }
  
  return days
})

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function isSameDay(d1, d2) {
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate()
}

function prevMonth() {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value -= 1
  } else {
    currentMonth.value -= 1
  }
}

function nextMonth() {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value += 1
  } else {
    currentMonth.value += 1
  }
}

function selectDate(day) {
  selectedDateStr.value = formatDate(day.date)
  ElMessage.success(`您选择的日期: ${selectedDateStr.value}`)
}

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，注意休息'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const userInitial = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || '?'
  return name.charAt(0).toUpperCase()
})

const avatarColor = computed(() => {
  const colors = { admin: 'var(--primary)', manager: 'var(--warning)', user: 'var(--success)' }
  return colors[userStore.role] || 'var(--primary)'
})

const roleLabel = computed(() => {
  const labels = { admin: '系统管理员', manager: '仓库管理员', user: '普通用户' }
  return labels[userStore.role] || '用户'
})

const roleTagType = computed(() => {
  const types = { admin: 'danger', manager: 'warning', user: 'success' }
  return types[userStore.role] || ''
})

const statCards = [
  { key: 'userCount', label: '用户总数', icon: 'User', color: '#4f46e5' },
  { key: 'materialCount', label: '物资种类', icon: 'Box', color: '#0ea5e9' },
  { key: 'warehouseCount', label: '仓库数量', icon: 'House', color: '#10b981' },
  { key: 'inboundCount', label: '入库记录', icon: 'Download', color: '#f59e0b' },
  { key: 'outboundCount', label: '出库记录', icon: 'Upload', color: '#ef4444' },
  { key: 'pendingApplyCount', label: '待审批申请', icon: 'Bell', color: '#8b5cf6' },
  { key: 'supplierCount', label: '供应商数量', icon: 'Shop', color: '#06b6d4' }
]

const quickActions = computed(() => {
  const all = [
    {
      path: '/material', title: '物资信息', desc: '查看所有物资库存',
      icon: 'Box', gradient: 'linear-gradient(135deg,#4f46e5,#7c3aed)'
    },
    {
      path: '/apply', title: '物资申请', desc: '提交物资申请单',
      icon: 'DocumentAdd', gradient: 'linear-gradient(135deg,#0ea5e9,#0284c7)'
    },
    {
      path: '/return', title: '物资归还', desc: '提交物资归还单',
      icon: 'RefreshLeft', gradient: 'linear-gradient(135deg,#10b981,#059669)'
    },
    {
      path: '/inbound', title: '入库登记', desc: '录入物资入库信息',
      icon: 'Download', gradient: 'linear-gradient(135deg,#f59e0b,#d97706)'
    },
    {
      path: '/outbound', title: '出库登记', desc: '录入物资出库信息',
      icon: 'Upload', gradient: 'linear-gradient(135deg,#ef4444,#dc2626)'
    },
    {
      path: '/profile', title: '个人信息', desc: '修改个人资料',
      icon: 'Setting', gradient: 'linear-gradient(135deg,#8b5cf6,#7c3aed)'
    }
  ]
  const role = userStore.role
  if (role === 'user') {
    return all.filter(a => ['/material', '/apply', '/return', '/profile'].includes(a.path))
  } else if (role === 'manager') {
    return all.filter(a => ['/material', '/inbound', '/outbound', '/profile'].includes(a.path))
  }
  return all
})

const initPieChart = (materials) => {
  if (!pieChartRef.value) return
  if (pieChartInstance) {
    pieChartInstance.dispose()
  }

  pieChartInstance = echarts.init(pieChartRef.value)

  const typeCounts = {}
  materials.forEach(m => {
    const typeName = m.typeName || '未分类'
    typeCounts[typeName] = (typeCounts[typeName] || 0) + 1
  })

  const pieData = Object.keys(typeCounts).map(name => ({
    name,
    value: typeCounts[name]
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 种 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: { color: '#64748b' }
    },
    color: ['#2563eb', '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#06b6d4'],
    series: [
      {
        name: '物资类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData
      }
    ]
  }

  pieChartInstance.setOption(option)
}

const getLast7Days = () => {
  const dates = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    dates.push(`${d.getFullYear()}-${month}-${day}`)
  }
  return dates
}

const initLineChart = (inbounds, outbounds) => {
  if (!lineChartRef.value) return
  if (lineChartInstance) {
    lineChartInstance.dispose()
  }

  lineChartInstance = echarts.init(lineChartRef.value)

  const dates = getLast7Days()
  const xLabels = dates.map(d => d.substring(5))

  const inboundMap = {}
  const outboundMap = {}

  inbounds.forEach(item => {
    const timeStr = item.inboundTime || item.createTime
    if (timeStr) {
      const dateStr = timeStr.substring(0, 10)
      inboundMap[dateStr] = (inboundMap[dateStr] || 0) + Number(item.quantity || 0)
    }
  })

  outbounds.forEach(item => {
    const timeStr = item.outboundTime || item.createTime
    if (timeStr) {
      const dateStr = timeStr.substring(0, 10)
      outboundMap[dateStr] = (outboundMap[dateStr] || 0) + Number(item.quantity || 0)
    }
  })

  const inboundData = dates.map(d => inboundMap[d] || 0)
  const outboundData = dates.map(d => outboundMap[d] || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } }
    },
    legend: {
      top: 0,
      data: ['入库数量', '出库数量'],
      textStyle: { color: '#64748b' }
    },
    grid: {
      left: '3%',
      right: '4%',
      top: '15%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: xLabels,
        axisLabel: { color: '#64748b' },
        axisLine: { lineStyle: { color: '#cbd5e1' } }
      }
    ],
    yAxis: [
      {
        type: 'value',
        axisLabel: { color: '#64748b' },
        splitLine: { lineStyle: { color: '#f1f5f9' } }
      }
    ],
    series: [
      {
        name: '入库数量',
        type: 'line',
        smooth: true,
        lineStyle: { width: 3, color: '#10b981' },
        showSymbol: false,
        areaStyle: {
          opacity: 0.1,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#10b981' },
            { offset: 1, color: 'rgba(16,185,129,0)' }
          ])
        },
        itemStyle: { color: '#10b981' },
        data: inboundData
      },
      {
        name: '出库数量',
        type: 'line',
        smooth: true,
        lineStyle: { width: 3, color: '#ef4444' },
        showSymbol: false,
        areaStyle: {
          opacity: 0.1,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ef4444' },
            { offset: 1, color: 'rgba(239,68,68,0)' }
          ])
        },
        itemStyle: { color: '#ef4444' },
        data: outboundData
      }
    ]
  }

  lineChartInstance.setOption(option)
}

onMounted(async () => {
  try {
    const res = await dashboardApi.stats()
    stats.value = res.data
    statsLoaded.value = true
  } catch {
    statsLoaded.value = true
  }

  try {
    const [materialsRes, inboundsRes, outboundsRes] = await Promise.all([
      materialApi.list(),
      inboundApi.page({ pageNum: 1, pageSize: 1000 }),
      outboundApi.page({ pageNum: 1, pageSize: 1000 })
    ])

    initPieChart(materialsRes.data || [])
    initLineChart(inboundsRes.data?.records || [], outboundsRes.data?.records || [])

    // 填充有出入库业务的日期集合，以显示小圆点
    const inList = inboundsRes.data?.records || []
    inList.forEach(item => {
      const timeStr = item.inboundTime || item.createTime
      if (timeStr) {
        inboundDates.value.add(timeStr.substring(0, 10))
      }
    })
    const outList = outboundsRes.data?.records || []
    outList.forEach(item => {
      const timeStr = item.outboundTime || item.createTime
      if (timeStr) {
        outboundDates.value.add(timeStr.substring(0, 10))
      }
    })
  } catch (err) {
    console.error('Failed to load chart data', err)
  }

  resizeObserver = new ResizeObserver(() => {
    if (pieChartInstance) pieChartInstance.resize()
    if (lineChartInstance) lineChartInstance.resize()
  })
  if (pieChartRef.value) resizeObserver.observe(pieChartRef.value)
  if (lineChartRef.value) resizeObserver.observe(lineChartRef.value)
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (pieChartInstance) {
    pieChartInstance.dispose()
    pieChartInstance = null
  }
  if (lineChartInstance) {
    lineChartInstance.dispose()
    lineChartInstance = null
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.welcome-banner {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-lg {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  background: var(--primary);
  flex-shrink: 0;
}

.welcome-content h2 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.welcome-content p {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08);
  border-color: var(--card-color);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 图表展示区 */
.charts-grid {
  display: grid;
  grid-template-columns: 1.1fr 2fr 1.1fr;
  gap: 16px;
  margin-bottom: 28px;
}

.chart-card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  height: 320px;
  min-width: 0;
}

.chart-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  flex: 1;
  min-height: 0;
}

/* 极简日历组件样式 */
.calendar-card {
  display: flex;
  flex-direction: column;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.calendar-month {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
}

.calendar-nav {
  display: flex;
  gap: 4px;
}

.nav-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: var(--transition);
}

.nav-btn:hover {
  background: var(--sidebar-item-hover-bg);
  border-color: var(--primary-light);
  color: var(--primary);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  flex: 1;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-primary);
  position: relative;
  transition: var(--transition);
}

.day-cell:hover {
  background: var(--sidebar-item-hover-bg);
}

.day-cell.not-current {
  color: var(--text-muted);
  opacity: 0.4;
}

.day-cell.is-today {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 700;
}

.day-cell.is-selected {
  background: var(--primary);
  color: #ffffff !important;
  font-weight: 700;
  opacity: 1;
}

.day-cell.is-selected .activity-dot {
  background: #ffffff;
}

.activity-dot {
  position: absolute;
  bottom: 4px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--primary);
}

/* 快捷操作 */
.section-header {
  margin-bottom: 14px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin-bottom: 28px;
}

.quick-action-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  border: 1px solid var(--border-color);
  transition: all 0.2s;
}

.quick-action-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  border-color: var(--primary-light);
}

.qa-icon {
  width: 44px;
  height: 44px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.qa-info { flex: 1; }

.qa-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.qa-desc {
  font-size: 12px;
  color: var(--text-muted);
}

/* 底部信息 */
.dashboard-footer { margin-top: 8px; }

.info-card {
  background: var(--primary-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.info-card strong {
  display: block;
  font-size: 14px;
  color: var(--primary);
  margin-bottom: 4px;
}

.info-card p {
  font-size: 12px;
  color: var(--primary-light);
}

@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1.2fr 2fr;
  }
  .calendar-card {
    grid-column: span 2;
    height: auto;
  }
}

@media (max-width: 992px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .calendar-card {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .quick-actions { grid-template-columns: 1fr; }
  .welcome-banner { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>
