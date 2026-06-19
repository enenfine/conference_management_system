<template>
  <div class="room-detail-page">
    <!-- 返回按钮和标题 -->
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/rooms' }">
          <el-icon>
            <OfficeBuilding/>
          </el-icon>
          会议室列表
        </el-breadcrumb-item>
        <el-breadcrumb-item>{{ roomInfo.roomName }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="header-actions">
        <el-button class="action-button">
          <el-icon>
            <Star/>
          </el-icon>
        </el-button>
        <el-button class="action-button">
          <el-icon>
            <Share/>
          </el-icon>
        </el-button>
      </div>
    </div>

    <!-- 基本信息 -->
    <div class="room-info">
      <div class="info-section">
        <div class="info-item">
          <span class="info-label">会议室：</span>
          <span class="info-value">{{ roomInfo.roomName }}</span>
        </div>
        <div class="info-item">
          <el-icon>
            <User/>
          </el-icon>
          <span class="info-label">容纳人数：</span>
          <span class="info-value">{{ roomInfo.capacity }}人</span>
        </div>
        <div class="info-item">
          <el-icon>
            <Location/>
          </el-icon>
          <span class="info-label">位置：</span>
          <span class="info-value">{{ roomInfo.location }}</span>
        </div>
        <div class="info-item">
          <el-icon>
            <Monitor/>
          </el-icon>
          <span class="info-label">设施：</span>
          <span class="info-value">{{ Array.isArray(roomInfo.facilities) ? roomInfo.facilities.join('、') : '' }}</span>
        </div>
      </div>
    </div>

    <!-- 预订时间表 -->
    <div class="booking-section">
      <div class="date-navigation">
        <el-button-group>
          <el-button
              v-for="(date, index) in dateList.slice(0, 4)"
              :key="index"
              :type="selectedDate === date.value ? 'primary' : ''"
              @click="selectedDate = date.value"
          >
            {{ date.label }}
          </el-button>
        </el-button-group>
        <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            format="MM-DD"
            value-format="YYYY-MM-DD"
        />
      </div>

      <div class="time-slots">
        <div class="time-grid">
          <div v-for="(slot, index) in timeSlots"
               :key="slot.time"
               class="time-slot"
               :class="{
              'is-booked': slot.meetings.length > 0,
              'available': slot.meetings.length === 0
            }"
          >
            <div class="time-label">
              <el-icon>
                <Clock/>
              </el-icon>
              {{ slot.time }}
            </div>
            <div class="slot-status">
              <template v-if="slot.meetings.length > 0">
                <div v-for="(meeting, mIndex) in slot.meetings"
                     :key="mIndex"
                     class="meeting-info"
                >
                  <div class="meeting-time">会议时间：{{ meeting.startTime }}-{{ meeting.endTime }}</div>
                  <div class="meeting-header">
                    <div class="meeting-title">主题：{{ meeting.title }}</div>
                    <div class="meeting-organizer">主持人：{{ meeting.organizer }}</div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="available-text">
                  <el-icon>
                    <CircleCheck/>
                  </el-icon>
                  可使用
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, watch, computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  ArrowLeft,
  Star,
  Share,
  User,
  Location,
  Monitor,
  Clock,
  CircleCheck,
  OfficeBuilding
} from '@element-plus/icons-vue'
import {getRoomDetail, getRoomSchedule, type RoomDetailResponse, type RoomSchedule} from '@/api/meeting'
import {ElMessage} from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 会议室信息
const roomInfo = ref<Partial<RoomDetailResponse>>({})

// 日期选择
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))

// 生成未来5天的日期选项
const dateList = computed(() => {
  const dates = []
  for (let i = 0; i < 5; i++) {
    const date = dayjs().add(i, 'day')
    dates.push({
      label: i === 0 ? '今天' : date.format('MM-DD'),
      value: date.format('YYYY-MM-DD')
    })
  }
  return dates
})

interface TimeSlot {
  time: string
  isBooked: boolean
  meetings: Array<{
    title: string
    organizer: string
    startTime: string
    endTime: string
    isAcrossSlots?: boolean
  }>
}

interface Schedule {
  startTime: string
  endTime: string
  title: string
  organizerName: string
}

// 生成时间段
const timeSlots = ref<TimeSlot[]>([
  {time: '00:00-02:00', isBooked: false, meetings: []},
  {time: '02:00-04:00', isBooked: false, meetings: []},
  {time: '04:00-06:00', isBooked: false, meetings: []},
  {time: '06:00-08:00', isBooked: false, meetings: []},
  {time: '08:00-10:00', isBooked: false, meetings: []},
  {time: '10:00-12:00', isBooked: false, meetings: []},
  {time: '12:00-14:00', isBooked: false, meetings: []},
  {time: '14:00-16:00', isBooked: false, meetings: []},
  {time: '16:00-18:00', isBooked: false, meetings: []},
  {time: '18:00-20:00', isBooked: false, meetings: []},
  {time: '20:00-22:00', isBooked: false, meetings: []},
  {time: '22:00-24:00', isBooked: false, meetings: []}
])

// 获取会议室详情
const fetchRoomDetail = async () => {
  try {
    const response = await getRoomDetail(route.params.id as string)
    if (response.code === 0) {
      roomInfo.value = response.data
      // 处理设施
      if (roomInfo.value.facilities) {
        // roomInfo.value.facilities = JSON.parse(roomInfo.value.facilities)
      }
    }
  } catch (error) {
    console.error('获取会议室详情失败:', error)
    ElMessage.error('获取会议室详情失败')
  }
}

// 获取会议室日程
const fetchRoomSchedule = async () => {
  try {
    const response = await getRoomSchedule(route.params.id as string, selectedDate.value)
    const schedules = response.data as Schedule[]

    // 重置时间段状态
    timeSlots.value = timeSlots.value.map(slot => ({
      ...slot,
      isBooked: false,
      meetings: []
    }))

    // 更新时间段状态
    schedules.forEach(meeting => {
      // 解析会议开始和结束时间
      const startTime = dayjs(meeting.startTime)
      const endTime = dayjs(meeting.endTime)
      const startHour = startTime.hour()
      const endHour = endTime.hour()
      const startMinute = startTime.minute()
      const endMinute = endTime.minute()

      // 计算会议所在的时间段
      const startSlotIndex = Math.floor(startHour / 2)
      const endSlotIndex = Math.floor(endHour / 2)

      // 处理跨时间段的会议
      for (let i = 0; i < timeSlots.value.length; i++) {
        const slot = timeSlots.value[i]
        const slotStartHour = parseInt(slot.time.split('-')[0].split(':')[0])
        const slotEndHour = parseInt(slot.time.split('-')[1].split(':')[0])

        // 检查时间段是否与会议时间重叠
        const isOverlap = (
          // 会议开始时间在当前时间段内
          (startHour >= slotStartHour && startHour < slotEndHour) ||
          // 会议结束时间在当前时间段内
          (endHour > slotStartHour && endHour <= slotEndHour) ||
          // 会议时间完全覆盖当前时间段
          (startHour <= slotStartHour && endHour >= slotEndHour)
        )

        // 如果有重叠,标记为已预订并添加会议信息
        if (isOverlap) {
          timeSlots.value[i].isBooked = true
          timeSlots.value[i].meetings.push({
            title: meeting.title,
            organizer: meeting.organizerName,
            startTime: dayjs(meeting.startTime).format('HH:mm'),
            endTime: dayjs(meeting.endTime).format('HH:mm'),
            isAcrossSlots: startSlotIndex !== endSlotIndex
          })
        }
      }
    })
  } catch (error) {
    console.error('获取会议室日程失败:', error)
    ElMessage.error('获取会议室日程失败')
  }
}

// 监听日期变化
watch(selectedDate, () => {
  if (selectedDate.value) {
    fetchRoomSchedule()
  }
})

// 当天的会议列表
const dayMeetings = computed(() => {
  return timeSlots.value.filter(slot => slot.isBooked)
})

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

onMounted(() => {
  fetchRoomDetail()
  fetchRoomSchedule()
})
</script>

<style scoped>
.room-detail-page {
  padding: 32px;
  background: #fff;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  background: #fff;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

:deep(.el-breadcrumb) {
  font-size: 14px;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #86909c;
  font-weight: normal;
  transition: all 0.3s;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #1677ff;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1d2129;
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-breadcrumb__separator) {
  margin: 0 12px;
  color: #c9cdd4;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.action-button {
  width: 40px;
  height: 40px;
  padding: 0;
  border-radius: 50%;
  transition: all 0.3s;
  color: #666;
  border: 1px solid #e5e6eb;
}

.action-button:hover {
  color: #1677ff;
  border-color: #1677ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.1);
}


.room-info {
  margin-bottom: 32px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.info-section {
  display: flex;
  gap: 64px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-item .el-icon {
  font-size: 30px;
  color: #1677ff;
  background: #f0f7ff;
  padding: 8px;
  border-radius: 8px;
}

.info-label {
  color: #86909c;
  font-size: 14px;
}

.info-value {
  color: #1d2129;
  font-size: 15px;
  font-weight: 500;
}

.booking-section {
  margin-top: 32px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.date-navigation {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
}

.date-navigation :deep(.el-button-group) {
  display: flex;
  gap: 8px;
}

.date-navigation :deep(.el-button) {
  border-radius: 8px !important;
  padding: 10px 20px;
  min-width: 100px;
  font-size: 14px;
  transition: all 0.3s;
}

.date-navigation :deep(.el-button:not(.el-button--primary):hover) {
  background: #f0f7ff;
  border-color: #1677ff;
  color: #1677ff;
}

.date-navigation .el-date-picker {
  width: 120px;
}

.time-slots {
  padding: 0;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e6eb;
  overflow: hidden;
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(3, 1fr);
  gap: 1px;
  position: relative;
  background: #e5e6eb;
}

.time-slot {
  background: #fff;
  padding: 16px;
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  min-height: 140px;
  position: relative;
}

.time-slot::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f56c6c;
  opacity: 0;
  transition: opacity 0.3s;
}

.time-slot.is-booked::before {
  opacity: 0.05;
}

.time-slot:hover {
  background: #fafafa;
}

.time-label {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1d2129;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.time-label .el-icon {
  font-size: 14px;
  color: #1677ff;
}

.slot-status {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meeting-info {
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: #fff8f8;
  /* border: 1px solid #ffd6d6; */
}

.meeting-time {
  font-size: 13px;
  color: #f56c6c;
  font-weight: 500;
}

.meeting-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.meeting-title {
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meeting-organizer {
  font-size: 12px;
  color: #86909c;
  flex-shrink: 0;
}

.available-text {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #52c41a;
  font-size: 14px;
  font-weight: 500;
}

.available-text .el-icon {
  font-size: 16px;
}

@media (max-width: 1400px) {
  .time-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .time-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .time-grid {
    grid-template-columns: 1fr;
  }
}
</style> 