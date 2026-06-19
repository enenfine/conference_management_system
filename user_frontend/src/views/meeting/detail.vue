<template>
  <div class="meeting-detail-page">
    <div class="page-header">
      <el-button @click="router.back()">返回</el-button>
      <h2>{{ meetingInfo.title || '会议详情' }}</h2>
      <div class="actions" v-if="meetingInfo.id">
        <el-button type="primary" @click="router.push(`/meeting/form?id=${meetingInfo.id}`)">编辑会议</el-button>
        <el-button type="danger" @click="handleCancelMeeting" v-if="meetingInfo.status !== 'CANCELLED'">取消会议</el-button>
      </div>
    </div>

    <el-card class="detail-card" shadow="never" v-loading="loading">
      <template #header>基本信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="会议主题">{{ meetingInfo.title }}</el-descriptions-item>
        <el-descriptions-item label="会议编号">{{ meetingInfo.meetingCode }}</el-descriptions-item>
        <el-descriptions-item label="会议类型">{{ meetingInfo.meetingType?.typeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会议状态">
          <el-tag :type="getStatusType(meetingInfo.status)">{{ getStatusText(meetingInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="会议室">{{ meetingInfo.roomName }}</el-descriptions-item>
        <el-descriptions-item label="会议地点">{{ meetingInfo.roomLocation }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(meetingInfo.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(meetingInfo.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="主持人">{{ meetingInfo.organizerName }}</el-descriptions-item>
        <el-descriptions-item label="参会人数">{{ participants.length }}</el-descriptions-item>
        <el-descriptions-item label="会议说明" :span="2">{{ meetingInfo.description || '暂无说明' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="detail-card" shadow="never">
      <template #header>参会人员</template>
      <el-table :data="participants" border>
        <el-table-column prop="userName" label="姓名" />
        <el-table-column prop="userAccount" label="账号" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">{{ row.role === 'HOST' ? '主持人' : '参会人' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="确认状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACCEPTED' ? 'success' : row.status === 'DECLINED' ? 'danger' : 'warning'">
              {{ row.status === 'ACCEPTED' ? '已确认' : row.status === 'DECLINED' ? '已拒绝' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="detail-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>会议附件</span>
          <el-upload :show-file-list="false" :before-upload="beforeUploadFile">
            <el-button type="primary">上传附件</el-button>
          </el-upload>
        </div>
      </template>
      <el-table :data="fileList" border>
        <el-table-column prop="fileName" label="文件名" />
        <el-table-column prop="uploaderName" label="上传人" width="140" />
        <el-table-column prop="fileSize" label="大小" width="120">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="downloadFile(row)">下载/查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getMeetingDetail, getMeetingParticipants, cancelMeeting, type Meeting } from '@/api/meeting'
import { getMeetingFileList, uploadMeetingFile, type MeetingFile } from '@/api/document'

const route = useRoute()
const router = useRouter()
const meetingId = String(route.params.id)
const loading = ref(false)
const meetingInfo = ref<Partial<Meeting>>({})
const participants = ref<any[]>([])
const fileList = ref<MeetingFile[]>([])

const fetchDetail = async () => {
  loading.value = true
  try {
    const [detailRes, participantRes, fileRes] = await Promise.all([
      getMeetingDetail(meetingId),
      getMeetingParticipants(Number(meetingId)),
      getMeetingFileList(meetingId)
    ])
    meetingInfo.value = detailRes.data || {}
    participants.value = participantRes.data || participantRes || []
    fileList.value = fileRes.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取会议详情失败')
  } finally {
    loading.value = false
  }
}

const getStatusText = (status?: string) => {
  const map: Record<string, string> = { PENDING: '待开始', ONGOING: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }
  return status ? map[status] || status : '-'
}

const getStatusType = (status?: string) => {
  const map: Record<string, string> = { PENDING: 'warning', ONGOING: 'success', FINISHED: 'info', CANCELLED: 'danger' }
  return status ? map[status] || 'info' : 'info'
}

const formatTime = (time?: string) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
const formatFileSize = (size: string | number) => {
  const num = Number(size || 0)
  if (num < 1024) return `${num} B`
  if (num < 1024 * 1024) return `${(num / 1024).toFixed(1)} KB`
  return `${(num / 1024 / 1024).toFixed(1)} MB`
}

const beforeUploadFile = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('meetingId', meetingId)
  try {
    await uploadMeetingFile(formData)
    ElMessage.success('上传成功')
    await fetchDetail()
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  }
  return false
}

const downloadFile = (row: MeetingFile) => {
  const url = row.fileUrl?.startsWith('http') ? row.fileUrl : `${import.meta.env.VITE_API_URL || '/api'}${row.fileUrl}`
  window.open(url, '_blank')
}

const handleCancelMeeting = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该会议吗？', '提示', { type: 'warning' })
    await cancelMeeting(meetingId)
    ElMessage.success('取消成功')
    await fetchDetail()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '取消失败')
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.meeting-detail-page { padding: 24px; background: #f5f7fa; min-height: 100%; }
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.page-header h2 { flex: 1; margin: 0; }
.detail-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
