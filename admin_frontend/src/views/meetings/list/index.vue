<template>
  <div class="meetings-container">
    <!-- 头部搜索和操作 -->
    <div class="header">
      <div class="title">
        <el-icon><Calendar /></el-icon>
        会议列表
      </div>
      <div class="search-area">
        <div class="search-form">
          <el-input
              v-model="searchParams.title"
              placeholder="请输入会议名称搜索"
              class="search-input"
              :prefix-icon="Search"
              clearable
              @clear="handleSearch"
              @input="handleSearch"
          />
          <el-select
              v-model="searchParams.typeId"
              placeholder="会议类型"
              clearable
              class="search-select"
              @change="handleSearch"
          >
            <el-option
                v-for="item in typeList"
                :key="item.id"
                :label="item.typeName"
                :value="item.id"
            />
          </el-select>
          <el-select
              v-model="searchParams.roomId"
              placeholder="会议室"
              clearable
              class="search-select"
              @change="handleSearch"
          >
            <el-option
                v-for="item in roomList"
                :key="item.id"
                :label="item.roomName"
                :value="item.id"
            />
          </el-select>
          <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              :shortcuts="dateShortcuts"
              @change="handleSearch"
          />
          <el-button type="primary" :icon="Search" @click.stop="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click.stop="handleReset">重置</el-button>
        </div>
        <div class="action-buttons">
          <el-button type="primary" :icon="Plus" @click.stop="handleAdd">新增会议</el-button>
        </div>
      </div>
    </div>

    <!-- 会议表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
          v-loading="loading"
          :data="meetingList"
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa' }"
          :row-style="{ height: '60px' }"
          border
      >
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="{ row }">
            <span class="meeting-id">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="会议名称" min-width="150" align="center">
          <template #default="{ row }">
            <span class="meeting-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="meetingType.typeName" label="会议类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.meetingType?.typeName || row.typeName || "-" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roomName" label="会议室" width="150" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain">{{ row.roomName || "-" }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="organizer" label="组织者" width="80" align="center">
          <template #default="{ row }">
            <el-tooltip :content="`组织者: ${row.organizerName || '-'}`" placement="top">
              <el-avatar :size="24" class="organizer-avatar">
                {{ (row.organizerName || "用").charAt(0) }}
              </el-avatar>
              <span class="organizer-name">{{ row.organizerName || "-" }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="参会人员" min-width="160" align="center">
          <template #default="{ row }">
            <div class="participants-list">
              <div class="avatar-list">
                <el-tooltip
                  v-for="participant in (row.participants || []).slice(0, 3)"
                  :key="participant.id || participant.userId"
                  :content="getParticipantName(participant)"
                  placement="top"
                >
                  <el-avatar
                    :size="24"
                    class="organizer-avatar participant-avatar"
                  >
                    {{ getUserInitial(getParticipantName(participant)) }}
                  </el-avatar>
                </el-tooltip>
              </div>

              <el-tag
                v-if="(row.participants || []).length > 3"
                size="small"
                type="info"
                class="participant-count"
              >
                +{{ (row.participants || []).length - 3 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="会议时间" width="340" align="center">
          <template #default="{ row }">
            <div class="meeting-time">
              <el-icon><Timer /></el-icon>
              <span>{{ row.startTime }}</span>
              <span class="time-separator">至</span>
              <span>{{ row.endTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag
                :type="getStatusType(row.status)"
                size="small"
                effect="light"
                round
            >{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="会议描述" min-width="200" align="center">
          <template #default="{ row }">
            <el-tooltip
              :content="row.description"
              placement="top"
              :show-after="500"
            >
              <span class="description-text">{{ row.description }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" size="small" @click.stop="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-info">
          共 <span class="total-count">{{ total }}</span> 条记录
        </div>
        <div class="pagination-select">
          <el-select v-model="pageSize" @change="handleSizeChange">
            <el-option label="10条/页" :value="10" />
            <el-option label="15条/页" :value="15" />
            <el-option label="20条/页" :value="20" />
          </el-select>
        </div>
        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="Number(total)"
            layout="prev, pager, next"
            background
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑会议' : '新增会议'"
      width="800px"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        class="meeting-form"
        status-icon
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="会议名称" prop="title">
              <el-input v-model="formData.title" placeholder="请输入会议名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会议类型" prop="typeId">
              <el-select v-model="formData.typeId" placeholder="请选择会议类型" class="w-full">
                <el-option
                  v-for="item in typeList"
                  :key="item.id"
                  :label="item.typeName"
                  :value="Number(item.id)"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="会议室" prop="roomId">
              <el-select v-model="formData.roomId" placeholder="请选择会议室" class="w-full">
                <el-option
                  v-for="item in roomList"
                  :key="item.id"
                  :label="item.roomName"
                  :value="Number(item.id)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会议时间" prop="timeRange" required>
              <el-date-picker
                v-model="formData.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                :shortcuts="dateShortcuts"
                class="w-full"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="参会人员" prop="participantIds" required>
          <el-select
            v-model="formData.participantIds"
            multiple
            filterable
            remote
            :remote-method="handleUserSearch"
            :loading="userListLoading"
            placeholder="请选择参会人员"
            class="w-full"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.userName || user.userAccount"
              :value="Number(user.id)"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.userAvatar ? '/api' + user.userAvatar : defaultAvatar" />
                <span>{{ user.userName }}</span>
                <span class="user-account">({{ user.userAccount }})</span>
              </div>
            </el-option>
            <template #empty>
              <div v-if="userListLoading" class="loading-text">
                加载中...
              </div>
              <div v-else class="empty-text">
                暂无数据
              </div>
            </template>
          </el-select>
        </el-form-item>

        <el-form-item label="会议描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入会议描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '更 新' : '创 建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, computed, watch, nextTick} from 'vue'
import {Search, Plus, Refresh, Calendar, Timer, Edit, Delete} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  getMeetingList,
  getMeetingTypeList,
  getMeetingRoomList,
  deleteMeeting,
  addMeeting,
  updateMeeting,
  getMeetingParticipants
} from '@/api/meeting'
import { getUserList } from '@/api/user'
import type {Meeting, MeetingType, MeetingRoom, MeetingListParams} from '@/api/meeting'
import type {FormInstance} from 'element-plus'
import type { UserListParams } from '@/api/user'
import dayjs from 'dayjs'
import { debounce } from 'lodash'
import defaultAvatar from '@/assets/avatar.jpg'


// 查询条件
const searchQuery = ref('')
const typeId = ref('')
const roomId = ref('')
const dateRange = ref<[string, string]>(['', ''])

// 搜索参数
const searchParams = ref<MeetingListParams>({
  current: 1,
  pageSize: 10,
  size: 10,
  title: '',
  typeId: undefined,
  roomId: undefined,
  startTime: undefined,
  endTime: undefined
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 获取参会人显示名称
const getParticipantName = (participant: any) => {
  return participant?.userName || participant?.userAccount || participant?.name || '用户'
}

// 获取名字第一个字
const getUserInitial = (name?: string | null) => {
  const text = (name || '').trim()
  return text ? text.substring(0, 1).toUpperCase() : '用'
}

// 数据列表
const meetingList = ref<Meeting[]>([])
const typeList = ref<MeetingType[]>([])
const roomList = ref<MeetingRoom[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

// 表单数据
const formData = ref<any>({
  title: '',
  typeId: undefined,
  roomId: undefined,
  timeRange: [],
  description: '',
  participantIds: []
})

// 时间范围
const timeRange = ref<[string, string]>(['', ''])

// 用户列表
const userListLoading = ref(false)
const userList = ref<any[]>([])
const userTotal = ref(0)
const userCurrentPage = ref(1)
const userPageSize = ref(10)
const userSearchQuery = ref('')

// 当前时间
const now = computed(() => {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
})

// 开始时间的禁用时间
const startTimeDisabled = (time: Date) => {
  return dayjs(time).isBefore(dayjs(), 'minute')
}

// 结束时间的禁用时间
const endTimeDisabled = (time: Date) => {
  if (!formData.value.startTime) {
    return true
  }
  return dayjs(time).isBefore(dayjs(formData.value.startTime), 'minute')
}

// 状态处理
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    ONGOING: 'success',
    FINISHED: 'info',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待开始',
    ONGOING: '进行中',
    FINISHED: '已结束',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

// 日期快捷选项
const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const start = dayjs().startOf('day')
      const end = dayjs().endOf('day')
      return [start.toDate(), end.toDate()]
    }
  },
  {
    text: '明天',
    value: () => {
      const start = dayjs().add(1, 'day').startOf('day')
      const end = dayjs().add(1, 'day').endOf('day')
      return [start.toDate(), end.toDate()]
    }
  },
  {
    text: '一周内',
    value: () => {
      const start = dayjs().startOf('day')
      const end = dayjs().add(7, 'day').endOf('day')
      return [start.toDate(), end.toDate()]
    }
  }
]

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入会议名称', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: '请选择会议类型', trigger: 'change' }
  ],
  roomId: [
    { required: true, message: '请选择会议室', trigger: 'change' }
  ],
  timeRange: [
    { 
      required: true, 
      message: '请选择会议时间', 
      trigger: 'change',
      type: 'array'
    }
  ],
  participantIds: [
    { required: true, message: '请选择参会人员', trigger: 'change' }
  ]
}

// 获取数据列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getMeetingList(searchParams.value)
    const records = data?.records || []

    const meetingsWithParticipants = await Promise.all(
      records.map(async (meeting: Meeting) => {
        const participants = await getMeetingParticipants(Number(meeting.id))
        return {
          ...meeting,
          participants: participants || []
        }
      })
    )

    meetingList.value = meetingsWithParticipants
    total.value = Number(data?.total || 0)
  } catch (error: any) {
    console.error('获取会议列表失败:', error)
    ElMessage.error(error?.message || '获取会议列表失败')
  } finally {
    loading.value = false
  }
}

// 获取会议类型列表
const fetchTypeList = async () => {
  try {
    const {data} = await getMeetingTypeList()
    typeList.value = Array.isArray(data) ? data : (data?.records || [])
  } catch (error) {
    console.error('获取会议类型列表失败:', error)
  }
}

// 获取会议室列表
const fetchRoomList = async () => {
  try {
    const {data} = await getMeetingRoomList()
    roomList.value = Array.isArray(data) ? data : (data?.records || [])
  } catch (error) {
    console.error('获取会议室列表失败:', error)
  }
}

// 获取用户列表
const fetchUserList = async () => {
  userListLoading.value = true
  try {
    const { data } = await getUserList({
      current: 1,
      pageSize: 20,
      userName: userSearchQuery.value,
      userProfile: '',
      userRole: ''
    })
    userList.value = data.records
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    userListLoading.value = false
  }
}

// 处理用户搜索
const handleUserSearch = (query: string) => {
  if (query !== undefined) {
    userSearchQuery.value = query
  }
}

// 监听用户搜索
watch(userSearchQuery, debounce(() => {
  fetchUserList()
}, 300))

// 用户分页变化
const handleUserPageChange = (page: number) => {
  userCurrentPage.value = page
  fetchUserList()
}

// 用户每页条数变化
const handleUserSizeChange = (size: number) => {
  userPageSize.value = size
  userCurrentPage.value = 1
  fetchUserList()
}

// 删除处理
const handleDelete = (row: Meeting) => {
  ElMessageBox.confirm('确定要删除该会议吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMeeting(row.id)
      ElMessage.success('删除成功')
      if (meetingList.value.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1
        searchParams.value.current = currentPage.value
      }
      await fetchData()
    } catch (error: any) {
      console.error('删除会议失败:', error)
      ElMessage.error(error?.message || '删除会议失败')
    }
  })
}

// 处理时间变化
const handleTimeChange = (val: [string, string]) => {
  if (val) {
    formData.value.startTime = val[0]
    formData.value.endTime = val[1]
  } else {
    formData.value.startTime = ''
    formData.value.endTime = ''
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: '',
    title: '',
    typeId: undefined,
    roomId: undefined,
    timeRange: [],
    description: '',
    participantIds: []
  }
  timeRange.value = ['', '']
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 处理弹窗关闭
const handleDialogClose = () => {
  resetForm()
}

// 新增处理
const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
  fetchTypeList()
  fetchRoomList()
  fetchUserList()
}

// 编辑处理
const handleEdit = async (row: Meeting) => {
  try {
    resetForm()
    isEdit.value = true
    dialogVisible.value = true

    await Promise.all([
      fetchTypeList(),
      fetchRoomList(),
      fetchUserList()
    ])

    const participants = await getMeetingParticipants(Number(row.id))
    const selectedIds = (participants || []).map((p: any) => Number(p.userId))

    // 把当前会议已选参会人补充到下拉选项中，避免显示成 2、3、4
    const existIds = new Set(userList.value.map((u: any) => Number(u.id)))
    const selectedUsers = (participants || [])
      .filter((p: any) => !existIds.has(Number(p.userId)))
      .map((p: any) => ({
        id: Number(p.userId),
        userName: p.userName,
        userAccount: p.userAccount,
        userAvatar: p.userAvatar
      }))
    userList.value = [...selectedUsers, ...userList.value]

    formData.value = {
      id: row.id,
      title: row.title,
      typeId: Number(row.typeId || row.meetingType?.id),
      roomId: Number(row.roomId),
      description: row.description || '',
      participantIds: selectedIds,
      timeRange: [
        dayjs(row.startTime).format('YYYY-MM-DD HH:mm:ss'),
        dayjs(row.endTime).format('YYYY-MM-DD HH:mm:ss')
      ]
    }
  } catch (error: any) {
    console.error('打开编辑弹窗失败:', error)
    ElMessage.error(error?.message || '打开编辑弹窗失败')
  }
}

// 提交表单
const submitLoading = ref(false)
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请先完善会议信息')
    return
  }

  submitLoading.value = true
  try {
    const [startTimeRaw, endTimeRaw] = formData.value.timeRange || []
    const startTime = dayjs(startTimeRaw).format('YYYY-MM-DD HH:mm:ss')
    const endTime = dayjs(endTimeRaw).format('YYYY-MM-DD HH:mm:ss')

    const submitData = {
      title: formData.value.title,
      typeId: Number(formData.value.typeId),
      roomId: Number(formData.value.roomId),
      description: formData.value.description || '',
      startTime,
      endTime,
      participantIds: (formData.value.participantIds || []).map((id: any) => Number(id))
    }

    if (isEdit.value) {
      await updateMeeting({
        ...submitData,
        id: formData.value.id
      })
      ElMessage.success('更新成功')
    } else {
      await addMeeting(submitData)
      ElMessage.success('创建成功')
    }

    // 先关弹窗，再刷新列表，避免刷新列表慢时看起来“没有关闭”
    dialogVisible.value = false
    await nextTick()
    resetForm()
    fetchData()
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error?.message || '保存失败，请查看后端控制台')
  } finally {
    submitLoading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  searchParams.value.current = 1
  searchParams.value.pageSize = pageSize.value
  searchParams.value.size = pageSize.value
  searchParams.value.startTime = dateRange.value?.[0] || undefined
  searchParams.value.endTime = dateRange.value?.[1] || undefined
  fetchData()
}

// 重置搜索
const handleReset = () => {
  currentPage.value = 1
  dateRange.value = ['', '']
  searchParams.value = {
    current: 1,
    pageSize: pageSize.value,
    size: pageSize.value,
    title: '',
    typeId: undefined,
    roomId: undefined,
    startTime: undefined,
    endTime: undefined
  }
  fetchData()
}

// 在打开对话框时获取用户列表
watch(dialogVisible, (val) => {
  if (val) {
    fetchUserList()
  }
})

// 添加分页处理函数
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  searchParams.value.pageSize = val
  searchParams.value.size = val
  searchParams.value.current = 1
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  searchParams.value.current = val
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchTypeList()
  fetchRoomList()
  fetchUserList()
})
</script>

<style scoped lang="scss">
.meetings-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;

  .header {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);

    .title {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        font-size: 20px;
        color: var(--el-color-primary);
      }
    }

    .search-area {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;

      .search-form {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        flex: 1;

        .search-input {
          width: 220px;
        }

        .el-select {
          width: 160px;
        }

        .el-date-picker {
          width: 400px;
        }

        :deep(.el-input__wrapper) {
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        }
      }

      .action-buttons {
        white-space: nowrap;
        display: flex;
        gap: 8px;
      }
    }
  }

  .table-card {
    border-radius: 8px;
    
    :deep(.el-card__body) {
      padding: 0;
    }

    :deep(.el-table) {
      .el-table__header th {
        height: 50px;
        font-weight: 600;
      }

      .el-table__cell {
        vertical-align: middle;
      }
    }
  }

  .meeting-id {
    color: #666;
  }

  .meeting-title {
    color: #1f2937;
    font-weight: 500;
  }

  .meeting-time {
    color: #666;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .organizer-avatar {
    background: var(--el-color-primary);
    margin-right: 8px;
    font-size: 12px;
  }

  .organizer-name {
    font-size: 13px;
  }

  .pagination {
    margin-top: 20px;
    padding: 16px 20px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 16px;
    border-top: 1px solid var(--el-border-color-lighter);

    .pagination-info {
      color: #666;
      font-size: 14px;

      .total-count {
        color: var(--el-color-primary);
        font-weight: 600;
        margin: 0 4px;
      }
    }

    .pagination-select {
      width: 100px;
    }

    :deep(.el-pagination) {
      margin-left: 16px;
    }
  }
}

.w-full {
  width: 100% !important;
}

.text-gray-400 {
  color: #9ca3af;
}

.ml-2 {
  margin-left: 8px;
}

.meeting-form {
  padding: 20px 20px 0;
}

.dialog-footer {
  padding: 20px;
  text-align: right;
  border-top: 1px solid var(--el-border-color-lighter);
  
  .el-button {
    padding-left: 25px;
    padding-right: 25px;
  }
}

.participants-list {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .avatar-list {
    display: flex;
    align-items: center;
    
    .participant-avatar {
      margin-left: -6px;
      
      &:first-child {
        margin-left: 0;
      }
      
      &:hover {
        z-index: 10;
      }
    }
  }
  
  .participant-count {
    margin-left: 4px;
    font-size: 12px;
    height: 24px;
    line-height: 22px;
    padding: 0 8px;
  }
}


.meeting-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 13px;

  .el-icon {
    color: var(--el-color-primary);
  }

  .time-separator {
    color: #999;
    margin: 0 4px;
  }
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  
  .user-account {
    color: #999;
    font-size: 12px;
  }
}

.loading-text,
.empty-text {
  padding: 8px 12px;
  color: #999;
  text-align: center;
  font-size: 13px;
}

.description-text {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #1f2937;
}
</style> 