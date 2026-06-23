<template>
  <section>
    <div class="toolbar">
      <div>
        <h2 class="page-title">统计看板</h2>
        <p class="page-subtitle">家属、墓穴、租赁、预约与营收概况</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="stats-grid">
      <div v-for="item in cards" :key="item.label" class="stat-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <div class="panel chart-panel">
      <div class="toolbar">
        <div>
          <h3>月度营收</h3>
          <p class="page-subtitle">按缴费时间聚合展示</p>
        </div>
      </div>
      <div ref="chartRef" class="chart"></div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { dashboardApi } from '../api/modules'

const summary = ref({})
const income = ref([])
const chartRef = ref()
let chart

const cards = computed(() => [
  { label: '家属总数', value: summary.value.familyCount ?? 0 },
  { label: '逝者档案', value: summary.value.deceasedCount ?? 0 },
  { label: '空置墓穴', value: summary.value.emptyGraveCount ?? 0 },
  { label: '有效租赁', value: summary.value.activeRentCount ?? 0 },
  { label: '本月收入', value: `￥${summary.value.monthIncome ?? 0}` },
  { label: '待祭扫预约', value: summary.value.pendingBookCount ?? 0 }
])

const renderChart = async () => {
  await nextTick()
  chart ||= echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 24, right: 22, bottom: 34, left: 54 },
    xAxis: { type: 'category', data: income.value.map((item) => item.incomeMonth) },
    yAxis: { type: 'value' },
    series: [{
      name: '收入',
      type: 'bar',
      data: income.value.map((item) => item.totalIncome),
      itemStyle: { color: '#0f766e' },
      barMaxWidth: 42
    }]
  })
}

const load = async () => {
  summary.value = await dashboardApi.summary()
  income.value = await dashboardApi.monthIncome()
  renderChart()
}

onMounted(load)
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.stat-panel {
  min-height: 104px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.stat-panel span {
  color: #6b7280;
  font-size: 13px;
}

.stat-panel strong {
  color: #111827;
  font-size: 26px;
}

.chart-panel h3 {
  margin: 0;
}

.chart {
  height: 360px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

