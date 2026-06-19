<template>
  <div class="rooms-page">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-input
          v-model="searchText"
          placeholder="搜索会议室..."
          class="search-input"
          :prefix-icon="Search"
      />
      <el-select v-model="locationFilter" placeholder="所有位置" class="filter-select">
        <el-option label="所有位置" value=""/>
        <el-option v-for="floor in locations" :key="floor" :label="floor" :value="floor"/>
      </el-select>
      <el-select v-model="capacityFilter" placeholder="容纳人数" class="filter-select">
        <el-option label="容纳人数" value=""/>
        <el-option v-for="cap in capacities" :key="cap" :label="`${cap}人`" :value="cap"/>
      </el-select>
      <el-select v-model="facilityFilter" placeholder="设施" class="filter-select">
        <el-option label="设施" value=""/>
        <el-option v-for="facility in facilities" :key="facility" :label="facility" :value="facility"/>
      </el-select>
      <el-button type="primary" class="add-btn" @click="handleAddRoom">
        <el-icon>
          <Plus/>
        </el-icon>
        添加会议
      </el-button>
    </div>

    <!-- 会议室列表 -->
    <div class="room-grid">
      <template v-if="filteredRooms.length > 0">
        <div v-for="room in filteredRooms" :key="room.id" class="room-card">
          <div class="room-image">
            <img
                :src="getRoomImage(room)"
                :alt="room.roomName"
                @error="handleImageError"
            />
            <div class="room-status" :class="{ 'status-occupied': room.isOccupied || room.status === 0 }">
              {{ room.isOccupied ? '使用中' : room.status === 0 ? '维护中' : '可用' }}
            </div>
          </div>
          <div class="room-content">
            <h3>{{ room.roomName }}</h3>
            <div class="room-info">
              <div class="info-item">
                <el-icon>
                  <Location/>
                </el-icon>
                <span>{{ room.location }}</span>
              </div>
              <div class="info-item">
                <el-icon>
                  <User/>
                </el-icon>
                <span>{{ room.capacity }} 人</span>
              </div>
              <div class="info-item">
                <el-icon>
                  <Monitor/>
                </el-icon>
                <span>{{ room.facilities.join('、') }}</span>
              </div>
            </div>
            <div class="room-actions">
              <el-button
                  type="primary"
                 :size="'large'"
                  @click="handleView(room)"
                  style="width: 100%;"
                  plain
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </template>
      <div v-else class="empty-state">
        <el-empty
            description="暂无符合条件的会议室"
            :image-size="120"
        >
          <template #extra>
            <el-button type="primary" @click="resetFilters">重置筛选</el-button>
          </template>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <div class="page-info">显示 {{ total }} 个会议室中的 {{ filteredRooms.length }} 个</div>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :pager-count="5"
        layout="prev, pager, next"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, onMounted} from 'vue'
import {Search, Location, User, Monitor, Plus} from '@element-plus/icons-vue'
import {getRoomList, type RoomListParams, type RoomDetail} from '@/api/meeting'
import {ElMessage} from 'element-plus'
import {useRouter} from 'vue-router'
import defaultRoomImage from '@/assets/banner/banner3.jpg'

const apiUrl = import.meta.env.VITE_API_URL
const router = useRouter()

const getRoomImage = (room: RoomDetail) => {
  if (!room.roomImage || room.roomImage.length === 0 || !room.roomImage[0]) {
    return defaultRoomImage
  }

  const imageUrl = room.roomImage[0]

  // 后端如果已经返回完整 http 地址，直接使用；否则拼接后端 API 地址
  if (/^https?:\/\//.test(imageUrl)) {
    return imageUrl
  }

  return `${apiUrl}/${imageUrl}`
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = defaultRoomImage
}


// 搜索和筛选
const searchText = ref('')
const locationFilter = ref('')
const capacityFilter = ref('')
const facilityFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 会议室数据
const rooms = ref<RoomDetail[]>([])

// 获取会议室列表
const fetchRoomList = async () => {
  try {
    const params: RoomListParams = {
      code: '',
      current: currentPage.value,
      location: locationFilter.value,
      maxCapacity: capacityFilter.value ? parseInt(capacityFilter.value) : 100,
      minCapacity: 0,
      name: searchText.value,
      pageSize: pageSize.value,
      sortField: '',
      sortOrder: '',
      status: ''
    }
    const response = await getRoomList(params)
    rooms.value = response.data.records
    total.value = parseInt(response.data.total)
  } catch (error) {
    console.error('获取会议室列表失败:', error)
    ElMessage.error('获取会议室列表失败')
  }
}

// 筛选选项
const locations = computed(() => {
  const locationSet = new Set(rooms.value.map(room => room.location.split('楼')[0] + '楼'))
  return Array.from(locationSet)
})

const capacities = computed(() => {
  const capacitySet = new Set(rooms.value.map(room => room.capacity))
  return Array.from(capacitySet).sort((a, b) => a - b)
})

const facilities = computed(() => {
  const facilitySet = new Set(rooms.value.flatMap(room => room.facilities))
  return Array.from(facilitySet)
})

// 根据筛选条件过滤会议室
const filteredRooms = computed(() => {
  return rooms.value.filter(room => {
    const matchSearch = room.roomName.toLowerCase().includes(searchText.value.toLowerCase())
    const matchLocation = !locationFilter.value || room.location.includes(locationFilter.value)
    const matchCapacity = !capacityFilter.value || room.capacity === parseInt(capacityFilter.value)
    const matchFacility = !facilityFilter.value || room.facilities.includes(facilityFilter.value)
    return matchSearch && matchLocation && matchCapacity && matchFacility
  })
})

// 处理页面变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchRoomList()
}

// 查看会议室详情
const handleView = (room: RoomDetail) => {
  router.push(`/room/detail/${room.id}`)
}

// 添加会议室
const handleAddRoom = () => {
  // TODO: 实现添加会议室逻辑
  router.push("/meeting/form")
}

// 重置筛选
const resetFilters = () => {
  searchText.value = ''
  locationFilter.value = ''
  capacityFilter.value = ''
  facilityFilter.value = ''
  currentPage.value = 1
  fetchRoomList()
}

// 初始化数据
onMounted(() => {
  fetchRoomList()
})
</script>

<style scoped>
.rooms-page {
  padding: 24px;
  padding-bottom: 0px;
  background: #fff;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
}

.sub-title {
  margin: 8px 0 0;
  font-size: 14px;
  color: #86909c;
}

.filter-section {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 160px;
}

.add-btn {
  margin-left: auto;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.room-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.room-image {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.room-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #00b42a;
  background: #e8ffea;
}

.status-occupied {
  color: #86909c;
  background: #f2f3f5;
}

.room-content {
  padding: 12px;
}

.room-content h3 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.room-info {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  color: #86909c;
  font-size: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.room-actions {
  display: flex;
  gap: 6px;
  margin-top: 12px;
}

.room-actions .el-button {
  flex: 1;
  padding: 6px 12px;
  font-size: 12px;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px 0;
}

.pagination {
  display: grid;
  grid-template-columns: 200px 1fr 200px;
  align-items: center;
  padding: 24px;
  padding-top: 0px;
}

.page-info {
  font-size: 13px;
  color: #86909c;
  grid-column: 1;
}

:deep(.el-pagination) {
  grid-column: 2;
  justify-self: center;
  --el-pagination-bg-color: #fff;
  --el-pagination-hover-color: var(--el-color-primary);
  --el-pagination-button-bg-color: #fff;
  --el-pagination-button-disabled-bg-color: #fff;
}

@media (max-width: 1400px) {
  .room-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1200px) {
  .room-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .room-grid {
    grid-template-columns: 1fr;
  }

  .filter-section {
    flex-wrap: wrap;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }

  .add-btn {
    width: 100%;
    margin-left: 0;
  }

  .pagination {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .page-info {
    order: 2;
  }

  :deep(.el-pagination) {
    width: 100%;
    display: flex;
    justify-content: center;
  }
}
</style> 