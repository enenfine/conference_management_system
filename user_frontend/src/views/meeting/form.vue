<template>
  <div class="meeting-form-page">
    <div class="page-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/meetings' }">
            <el-icon><Calendar /></el-icon>
            会议列表
          </el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEdit ? '编辑会议' : '新建会议' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <div class="form-content">
      <div class="quick-create">
        <h3>快速创建建议</h3>
        <div class="suggestion-list">
          <div class="suggestion-item" @click="applyTemplate('项目周会')">
            <div class="suggestion-icon">
              <el-icon>
                <Calendar />
              </el-icon>
            </div>
            <div class="suggestion-content">
              <h4>项目周会</h4>
              <p>回顾上周工作进展，讨论本周工作计划和项目里程碑完成情况</p>
            </div>
          </div>
          <div class="suggestion-item" @click="applyTemplate('产品评审会')">
            <div class="suggestion-icon">
              <el-icon>
                <Monitor />
              </el-icon>
            </div>
            <div class="suggestion-content">
              <h4>产品评审会</h4>
              <p>评审新功能原型设计，确认产品方案可行性和开发排期</p>
            </div>
          </div>
          <div class="suggestion-item" @click="applyTemplate('技术分享会')">
            <div class="suggestion-icon">
              <el-icon>
                <Share />
              </el-icon>
            </div>
            <div class="suggestion-content">
              <h4>技术分享会</h4>
              <p>分享技术难点攻克经验，探讨技术架构优化方案</p>
            </div>
          </div>
          <div class="suggestion-item" @click="applyTemplate('团队建设会')">
            <div class="suggestion-icon">
              <el-icon>
                <UserFilled />
              </el-icon>
            </div>
            <div class="suggestion-content">
              <h4>团队建设会</h4>
              <p>增进团队凝聚力，分享团队文化，制定团队发展计划</p>
            </div>
          </div>
        </div>
      </div>

      <div class="form-container">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="meeting-form"
          @submit.prevent>
          <el-form-item label="会议标题" prop="title" class="title-item">
            <el-input v-model="formData.title" placeholder="请输入会议标题" clearable :maxlength="50" show-word-limit>
              <template #prefix>
                <el-icon>
                  <Document />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="会议描述" prop="description">
            <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入会议描述" :maxlength="500"
              show-word-limit resize="none" />
          </el-form-item>

          <div class="time-section">
            <el-form-item label="开始时间" prop="startTime" class="time-item">
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="年/月/日 --:--"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disabledDate"
                class="datetime-picker"
                :clearable="false"
                :default-time="new Date(2000, 0, 1, 8, 0, 0)"
              >
                <template #prefix>
                  <el-icon><Clock /></el-icon>
                </template>
              </el-date-picker>
            </el-form-item>

            <el-form-item label="结束时间" prop="endTime" class="time-item">
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="年/月/日 --:--"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disabledDate"
                class="datetime-picker"
                :clearable="false"
                :default-time="new Date(2000, 0, 1, 9, 0, 0)"
              >
                <template #prefix>
                  <el-icon><Clock /></el-icon>
                </template>
              </el-date-picker>
            </el-form-item>
          </div>

          <el-form-item label="会议室" prop="roomId">
            <el-select v-model="formData.roomId" placeholder="请选择会议室" class="form-select"
              popper-class="meeting-select-dropdown">
              <template #prefix>
                <el-icon>
                  <Location />
                </el-icon>
              </template>
              <el-option v-for="room in meetingRooms" :key="room.id" :label="room.roomName" :value="room.id">
                <span class="select-label">{{ room.roomName }}</span>
                <span class="select-extra">{{ room.roomLocation }}</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="会议类型" prop="typeId">
            <el-select v-model="formData.typeId" placeholder="请选择会议类型" class="form-select"
              popper-class="meeting-select-dropdown">
              <template #prefix>
                <el-icon>
                  <List />
                </el-icon>
              </template>
              <el-option v-for="type in meetingTypes" :key="type.id" :label="type.typeName" :value="type.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="参会人员" prop="participantIds" class="form-item-participants">
            <el-select
              v-model="formData.participantIds"
              multiple
              filterable
              remote
              reserve-keyword
              :remote-method="handleUserSearch"
              @visible-change="handleUserDropdownVisible"
              :loading="userLoading"
              placeholder="请选择参会人员"
              class="form-select form-select_user"
              popper-class="meeting-select-dropdown"
            >
              <template #prefix>
                <el-icon>
                  <User />
                </el-icon>
              </template>
              <template #empty>
                <div class="select-empty">
                  <el-icon>
                    <Search />
                  </el-icon>
                  <span>{{ userLoading ? '加载中...' : '暂无数据' }}</span>
                </div>
              </template>
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="getUserDisplayName(user)"
                :value="user.id"
              >
                <div class="user-option">
                  <el-avatar
                    :size="32"
                    class="user-avatar text-avatar"
                    :style="getAvatarStyle(getUserDisplayName(user))"
                  >
                    {{ getAvatarText(getUserDisplayName(user)) }}
                  </el-avatar>
                  <div class="user-info">
                    <div class="user-name">
                      {{ getUserDisplayName(user) }}
                      <span class="user-profile">{{ user.userProfile || '暂无职位信息' }}</span>
                    </div>
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button @click="goBack">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                {{ isEdit ? '保存修改' : '创建会议' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {
  getMeetingTypeList,
  getAllMeetingRooms,
  addMeeting,
  getUserList,
  getMeetingDetail,
  getMeetingParticipants,
  updateMeeting,
  type MeetingType,
  type MeetingRoom,
  type AddMeetingParams
} from '@/api/meeting'
import {
  ArrowLeft, Document, Clock, Location, List, User, Calendar,
  Monitor, Share, Search, UserFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const formRef = ref()
const submitting = ref(false)

// 表单数据
const formData = ref<AddMeetingParams>({
  description: '',
  endTime: '',
  participantIds: [],
  roomId: '',
  startTime: '',
  title: '',
  typeId: ''
})

// 会议类型列表
const meetingTypes = ref<MeetingType[]>([])
// 会议室列表
const meetingRooms = ref<MeetingRoom[]>([])
// 用户列表
const userList = ref<any[]>([])
const userLoading = ref(false)

const searchQuery = ref('')

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入会议标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在 2-50 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入会议描述', trigger: 'blur' },
    { max: 500, message: '描述长度不能超过 500 个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!value) {
          callback(new Error('请选择开始时间'))
          return
        }
        if (formData.value.endTime && dayjs(value).isAfter(dayjs(formData.value.endTime))) {
          callback(new Error('开始时间不能晚于结束时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!value) {
          callback(new Error('请选择结束时间'))
          return
        }
        if (formData.value.startTime && dayjs(value).isBefore(dayjs(formData.value.startTime))) {
          callback(new Error('结束时间不能早于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  roomId: [
    { required: true, message: '请选择会议室', trigger: 'change' }
  ],
  typeId: [
    { required: true, message: '请选择会议类型', trigger: 'change' }
  ],
  participantIds: [
    { required: true, message: '请选择参会人员', trigger: 'change' }
  ]
}

// 获取会议类型列表
const fetchMeetingTypes = async () => {
  try {
    const response = await getMeetingTypeList()
    meetingTypes.value = response.data
  } catch (error) {
    console.error('获取会议类型列表失败:', error)
    ElMessage.error('获取会议类型列表失败')
  }
}

// 获取会议室列表
const fetchMeetingRooms = async () => {
  try {
    const response = await getAllMeetingRooms()
    meetingRooms.value = response.data
  } catch (error) {
    console.error('获取会议室列表失败:', error)
    ElMessage.error('获取会议室列表失败')
  }
}

// 统一处理用户接口返回数据，避免接口返回结构或空字段导致下拉框无法渲染
const normalizeUserList = (response: any) => {
  const records =
    response?.data?.records ||
    response?.data?.data?.records ||
    response?.records ||
    []

  return records
    .filter((user: any) => user && user.id !== undefined && user.id !== null)
    .map((user: any) => ({
      ...user,
      userName: user.userName || user.userAccount || `用户${user.id}`,
      userAvatar: user.userAvatar || '',
      userProfile: user.userProfile || ''
    }))
}

const getUserQueryParams = (userName = '', pageSize = 50) => ({
  current: 1,
  pageSize,
  mpOpenId: '',
  sortField: '',
  sortOrder: '',
  userName,
  // 不在前端强制筛选 userRole，避免数据库里 userRole 为空时查不到人员
  userRole: ''
})

// 获取用户列表
const fetchUserList = async () => {
  try {
    userLoading.value = true
    const response = await getUserList(getUserQueryParams('', 50))
    userList.value = normalizeUserList(response)
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userList.value = []
    ElMessage.error('获取用户列表失败，请检查 /user/list/page/vo 接口')
  } finally {
    userLoading.value = false
  }
}

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 获取会议详情
const fetchMeetingDetail = async (id: string) => {
  try {
    const [detailRes, participantsRes] = await Promise.all([
      getMeetingDetail(id),
      getMeetingParticipants(id)
    ])

    if (detailRes.code === 0 && participantsRes.code === 0) {
      const detail = detailRes.data
      const participants = participantsRes.data

      // 填充表单数据
      formData.value = {
        id: detail.id,
        title: detail.title,
        description: detail.description,
        startTime: detail.startTime,
        endTime: detail.endTime,
        roomId: detail.roomId,
        typeId: detail.meetingType.id,
        status: detail.status,
        participantIds: participants.map(p => p.userId)
      }
    } else {
      ElMessage.error('获取会议详情失败')
    }
  } catch (error) {
    console.error('获取会议详情失败:', error)
    ElMessage.error('获取会议详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 修改日期格式以匹配后端期望的格式 yyyy-MM-dd HH:mm:ss
    const submitData = {
      ...formData.value,
      startTime: dayjs(formData.value.startTime).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(formData.value.endTime).format('YYYY-MM-DD HH:mm:ss')
    }

    const response = await (isEdit.value ? updateMeeting(submitData) : addMeeting(submitData))
    if (response.code === 0) {
      ElMessage.success(isEdit.value ? '修改会议成功' : '新建会议成功')
      router.push('/meetings')
    } else {
      ElMessage.error(response.message || (isEdit.value ? '修改会议失败' : '新建会议失败'))
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('表单验证失败，请检查输入')
    }
  } finally {
    submitting.value = false
  }
}

// 处理用户搜索
const handleUserSearch = async (query: string) => {
  const keyword = query.trim()

  if (!keyword) {
    await fetchUserList()
    return
  }

  try {
    userLoading.value = true
    const response = await getUserList(getUserQueryParams(keyword, 20))
    userList.value = normalizeUserList(response)
  } catch (error) {
    console.error('搜索用户失败:', error)
    userList.value = []
    ElMessage.error('搜索用户失败，请检查 /user/list/page/vo 接口')
  } finally {
    userLoading.value = false
  }
}

// 下拉框展开时，如果还没有用户数据，主动加载一次
const handleUserDropdownVisible = async (visible: boolean) => {
  if (visible && userList.value.length === 0 && !userLoading.value) {
    await fetchUserList()
  }
}

const getUserDisplayName = (user: any) => {
  return user?.userName || user?.userAccount || (user?.id ? `用户${user.id}` : '未知用户')
}

const getAvatarColor = (name?: string | null) => {
  const colors = [
    { bg: '#FF7D00', text: '#fff' },
    { bg: '#00B42A', text: '#fff' },
    { bg: '#165DFF', text: '#fff' },
    { bg: '#722ED1', text: '#fff' },
    { bg: '#F5319D', text: '#fff' },
    { bg: '#3491FA', text: '#fff' },
    { bg: '#F77234', text: '#fff' },
    { bg: '#7BC616', text: '#fff' }
  ]

  const safeName = name || '用户'
  const index = safeName.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0) % colors.length
  return colors[index]
}

const getAvatarText = (name?: string | null) => {
  const safeName = (name || '用户').trim()
  return safeName.substring(0, 1)
}

const getAvatarStyle = (name?: string | null) => {
  const color = getAvatarColor(name)
  return {
    backgroundColor: color.bg,
    color: color.text
  }
}

// 添加模板应用功能
const applyTemplate = (type: string) => {
  switch (type) {
    case '项目周会':
      formData.value = {
        ...formData.value,
        title: '项目周会',
        description: '回顾上周工作进展，讨论本周工作计划和项目里程碑完成情况',
        typeId: meetingTypes.value.find(t => t.typeName === '项目会议')?.id || 0
      }
      break
    case '产品评审会':
      formData.value = {
        ...formData.value,
        title: '产品评审会',
        description: '评审新功能原型设计，确认产品方案可行性和开发排期',
        typeId: meetingTypes.value.find(t => t.typeName === '管理会议')?.id || 0
      }
      break
    case '技术分享会':
      formData.value = {
        ...formData.value,
        title: '技术分享会',
        description: '分享技术难点攻克经验，探讨技术架构优化方案',
        typeId: meetingTypes.value.find(t => t.typeName === '项目会议')?.id || 0
      }
      break
    case '团队建设会':
      formData.value = {
        ...formData.value,
        title: '团队建设会',
        description: '增进团队凝聚力，分享团队文化，制定团队发展计划',
        typeId: meetingTypes.value.find(t => t.typeName === '部门例会')?.id || 0
      }
      break
  }
}

onMounted(async () => {
  // 判断是否为编辑模式
  const meetingId = route.query.id as string
  isEdit.value = !!meetingId

  // 获取所需数据
  await Promise.all([
    fetchMeetingTypes(),
    fetchMeetingRooms(),
    fetchUserList()
  ])

  // 如果是编辑模式，获取会议详情
  if (isEdit.value && meetingId) {
    await fetchMeetingDetail(meetingId)
  }
})
</script>

<style scoped>
.meeting-form-page {
  min-height: calc(100vh - 100px);
  background: #fff;
  padding-bottom: 40px;
}

.page-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-button {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.form-content {
  max-width: 1200px;
  margin: 60px auto 0px;
  padding: 0 24px;
  display: flex;
  gap: 24px;
}

.quick-create {
  width: 320px;
  flex-shrink: 0;
}

.quick-create h3 {
  font-size: 14px;
  color: #1d2129;
  margin: 0 0 16px 0;
  font-weight: normal;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  height: 140px;
  display: flex;
  flex-direction: column;
}

.suggestion-item:hover {
  border-color: #1677ff;
  background: #f0f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.suggestion-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #e8f3ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.suggestion-icon .el-icon {
  font-size: 20px;
  color: #1677ff;
}

.suggestion-item h4 {
  font-size: 15px;
  color: #1d2129;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.suggestion-item p {
  font-size: 13px;
  color: #86909c;
  margin: 0;
  line-height: 1.6;
}

.form-container {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 32px 40px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  border: 1px solid #ccc;
  max-width: 800px;
  height: 100%;
}

.meeting-form {
  width: 100%;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
  line-height: 1.5;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__wrapper) {
  padding: 8px 12px;
  height: 40px;
  background-color: #f7f8fa;
  border: 1px solid #e5e6eb;
  box-shadow: none !important;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__wrapper:hover) {
  border-color: #1677ff;
  background-color: #fff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__wrapper.is-focus) {
  background-color: #fff;
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1) !important;
}

:deep(.el-textarea__wrapper) {
  height: auto;
  padding: 4px 12px;
}

:deep(.el-input__inner) {
  height: 24px;
  line-height: 24px;
  font-size: 14px;
  color: #1d2129;
}

:deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
  padding: 4px;
  color: #1d2129;
}

.time-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin-bottom: 28px;
}

.time-item {
  margin-bottom: 0;
}

/* 调整会议室和会议类型的宽度 */
.form-select {
  width: 320px !important;
}
.form-select_user {
  width: 100% !important;
}

/* 参会人员选择器使用全宽 */
.form-item-participants :deep(.el-select) {
  width: 100% !important;
}

:deep(.el-select .el-input__wrapper) {
  height: 40px;
}

:deep(.el-select .el-input__inner) {
  height: 24px;
  line-height: 24px;
}

:deep(.el-select__tags) {
  padding: 4px 0;
  flex-wrap: wrap;
  max-height: none;
}

:deep(.el-select__tags-text) {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-tag) {
  height: 24px;
  padding: 0 8px;
  border-radius: 4px;
  margin: 2px 4px 2px 0;
  max-width: 150px;
}

:deep(.el-tag .el-tag__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-select-dropdown) {
  max-height: none !important;
}

:deep(.el-select-dropdown__wrap) {
  max-height: 400px !important;
}

:deep(.meeting-select-dropdown .el-select-dropdown__item) {
  height: auto !important;
  line-height: 1.5;
  padding: 8px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px;
  min-height: 48px;
  width: 100%;
}

.user-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666;
}

.text-avatar {
  font-size: 14px;
  font-weight: 600;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 12px;
  color: #1d2129;
  line-height: 12px;
  margin: 0;
}

.user-profile {
  font-size: 12px;
  color: #86909c;
  line-height: 12px;
  margin: 0;
  margin-left: 10px;
}

:deep(.el-form-item.is-required .el-form-item__label::before) {
  color: #ff4d4f;
}

@media (max-width: 1200px) {
  .form-content {
    flex-direction: column;
  }

  .quick-create {
    width: 100%;
  }

  .suggestion-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }

  .form-container {
    padding: 24px;
  }

  .form-select {
    width: 100% !important;
  }
}

@media (max-width: 768px) {
  .time-section {
    grid-template-columns: 1fr;
  }

  .form-container {
    padding: 20px;
  }
}

.select-label {
  font-size: 14px;
  color: #1d2129;
}

.select-extra {
  float: right;
  font-size: 12px;
  color: #86909c;
}

.select-empty {
  padding: 24px 0;
  text-align: center;
  color: #86909c;
}

.select-empty .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

:deep(.meeting-select-dropdown) {
  border-radius: 8px;
  padding: 4px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

:deep(.meeting-select-dropdown .el-select-dropdown__item) {
  border-radius: 4px;
  margin: 2px 0;
  padding: 8px 12px;
  height: auto !important;
  min-height: 48px;
  line-height: 1.5;
}

:deep(.el-select-dropdown__wrap) {
  max-height: 400px;
}

:deep(.el-select-dropdown__list) {
  padding: 4px;
}

:deep(.el-select-dropdown__item.hover) {
  background-color: #f0f7ff;
}

:deep(.el-select-dropdown__item.selected) {
  background-color: #e8f3ff;
  color: #1677ff;
  font-weight: 500;
}

.form-actions {
  margin-top: 40px;
  padding-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #f2f3f5;
}

:deep(.el-button) {
  height: 36px;
  padding: 0 20px;
  font-size: 14px;
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background: #1677ff;
  border-color: #1677ff;
}

:deep(.el-button--primary:hover) {
  background: #4096ff;
  border-color: #4096ff;
}

:deep(.el-button--primary:active) {
  background: #0958d9;
  border-color: #0958d9;
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
</style>