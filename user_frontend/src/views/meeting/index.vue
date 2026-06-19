<template>
  <div class="meeting-manage">
    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-left">
        <!-- 会议标题搜索 -->
        <el-input
            v-model="searchTitle"
            placeholder="搜索会议标题"
            class="filter-item title-search"
            clearable
            @clear="handleFilterChange"
            @keyup.enter="handleFilterChange"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>

        <!-- 状态筛选 -->
        <el-select v-model="filterStatus" placeholder="会议状态" class="filter-item status-select"
                   @change="handleFilterChange">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>

        <!-- 日期范围 -->
        <div class="date-range">
          <el-date-picker
              v-model="startDate"
              type="datetime"
              placeholder="开始日期"
              class="filter-item date-picker"
              :clearable="true"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleFilterChange"
          />
          <span class="date-separator">至</span>
          <el-date-picker
              v-model="endDate"
              type="datetime"
              placeholder="结束日期"
              class="filter-item date-picker"
              :clearable="true"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleFilterChange"
          />
        </div>

        <!-- 会议室筛选 -->
        <el-select
            v-model="searchRoomId"
            placeholder="选择会议室"
            class="filter-item room-select"
            clearable
            @change="handleFilterChange"
        >
          <el-option label="全部会议室" value=""/>
          <el-option
              v-for="room in meetingRooms"
              :key="room.id"
              :label="room.roomName"
              :value="room.id"
          />
        </el-select>

        <!-- 会议类型筛选 -->
        <el-select
            v-model="searchTypeId"
            placeholder="会议类型"
            class="filter-item type-select"
            clearable
            @change="handleFilterChange"
        >
          <el-option label="全部类型" value=""/>
          <el-option
              v-for="type in meetingTypes"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
          />
        </el-select>

        <!-- 重置按钮 -->
        <el-button class="filter-item" @click="resetFilters">
          <el-icon>
            <Refresh/>
          </el-icon>
          重置
        </el-button>
      </div>
      <div>
        <el-button type="success" class="new-meeting-btn" @click="handleSyncMeetingStatus" :loading="sync_loading">
          <el-icon v-if="!sync_loading">
            <Refresh/>
          </el-icon>
          同步会议
        </el-button>
        <el-button type="primary" class="new-meeting-btn" @click="handleNewMeeting">
          <el-icon>
            <Plus/>
          </el-icon>
          新建会议
        </el-button>
      </div>
    </div>

    <!-- 会议列表切换 -->
    <div class="meeting-tabs">
      <div class="tab-item active">全部会议</div>
    </div>

    <!-- 会议列表 -->
    <div class="meeting-list" ref="listContainer">
      <template v-if="computedDisplayMeetings.length > 0">
        <div v-for="meeting in renderMeetingList()"
             :key="meeting.renderKey"
             class="meeting-col"
             :data-id="meeting.id"
        >
          <div class="meeting-item">
            <div class="meeting-header">
              <div class="meeting-title">
                <h3>{{ meeting.title }}</h3>
                <el-tag v-if="meeting.tag" size="small" class="meeting-tag">{{ meeting.tag }}</el-tag>
              </div>
              <el-tag :type="meeting.status.type" effect="light" size="small" class="status-tag">{{
                  meeting.status.label
                }}
              </el-tag>
            </div>
            <div class="meeting-time">{{ meeting.startTime }}至{{ meeting.endTime }}</div>
            <div class="meeting-desc">{{ meeting.description }}</div>
            <div class="meeting-location">
              <el-icon>
                <Location/>
              </el-icon>
              <span>{{ meeting.room }}</span>
            </div>
            <div class="meeting-footer">
              <div class="meeting-participants">
                <div class="host-info">
                  <span class="host-label">主持人：</span>
                  <el-avatar
                      :size="24"
                      class="host-avatar text-avatar"
                      :style="getAvatarStyle(meeting.host.name)"
                  >
                    {{ getAvatarText(meeting.host.name) }}
                  </el-avatar>
                  <span class="host-name">{{ meeting.host.name || '未知用户' }}</span>
                  <!-- <span class="host-role">{{ meeting.host.role }}</span> -->
                </div>
                <div class="participant-list" v-if="meeting.participants?.length">
                  <span class="participant-label">参会人：</span>
                  <div class="participant-avatars">
                    <el-avatar
                        v-for="participant in meeting.participants"
                        :key="`${meeting.id}-${participant.id}`"
                        :size="24"
                        class="participant-avatar text-avatar"
                        :style="getAvatarStyle(participant.userName)"
                    >
                      {{ getAvatarText(participant.userName) }}
                    </el-avatar>
                    <div v-if="meeting.totalParticipants > 3" class="avatar-more">
                      +{{ meeting.totalParticipants - 3 }}
                    </div>
                  </div>
                </div>
              </div>
              <div class="meeting-actions">
                <el-button v-if="meeting.canEdit" type="primary" link @click="handleEditMeeting(meeting.id)"
                           :disabled="!meeting.canJoin">
                  <el-icon>
                    <Edit/>
                  </el-icon>
                </el-button>
                <el-button v-if="meeting.canDelete" type="danger" link @click="handleDeleteMeeting(meeting.id)">
                  <el-icon>
                    <Delete/>
                  </el-icon>
                </el-button>
                <!--  :disabled="!meeting.canJoin"-->
                <el-button type="primary" class="join-btn" @click="handleViewDetail(meeting.id)">
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </template>
      <el-empty
          v-else
          class="meeting-empty"
          description="暂无会议"
          :image-size="120"
      >
        <el-button type="primary" @click="handleNewMeeting">
          <el-icon>
            <Plus/>
          </el-icon>
          新建会议
        </el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div class="pagination-section" v-if="total > 0">
      <div class="page-info">显示 {{ total }} 个会议中的 {{ computedDisplayMeetings.length }} 个</div>
      <el-pagination
          v-model:current-page="currentPage"
          :total="total"
          :page-size="4"
          layout="prev, pager, next"
          background
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增会议对话框 -->
    <el-dialog
        v-model="addMeetingVisible"
        title=""
        width="640px"
        :close-on-click-modal="false"
        destroy-on-close
        class="meeting-dialog"
    >
      <div class="dialog-header">
        <div class="dialog-icon">
          <el-icon>
            <Plus/>
          </el-icon>
        </div>
        <div class="dialog-title">
          <h3>新建会议</h3>
          <p>创建一个新的会议</p>
        </div>
      </div>
      <el-form
          ref="addMeetingFormRef"
          :model="addMeetingForm"
          :rules="rules"
          label-width="100px"
          class="meeting-form"
          @submit.prevent
      >
        <el-form-item label="会议标题" prop="title">
          <el-input
              v-model="addMeetingForm.title"
              placeholder="请输入会议标题"
              clearable
              :maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="会议描述" prop="description">
          <el-input
              v-model="addMeetingForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入会议描述"
              :maxlength="500"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
              v-model="addMeetingForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledDate"
              class="datetime-picker"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
              v-model="addMeetingForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledDate"
              class="datetime-picker"
          />
        </el-form-item>
        <el-form-item label="会议室" prop="roomId">
          <el-select
              v-model="addMeetingForm.roomId"
              placeholder="请选择会议室"
              class="meeting-select"
          >
            <el-option
                v-for="room in meetingRooms"
                :key="room.id"
                :label="room.roomName"
                :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会议类型" prop="typeId">
          <el-select
              v-model="addMeetingForm.typeId"
              placeholder="请选择会议类型"
              class="meeting-select"
          >
            <el-option
                v-for="type in meetingTypes"
                :key="type.id"
                :label="type.typeName"
                :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="参会人员" prop="participantIds">
          <el-select
              v-model="addMeetingForm.participantIds"
              multiple
              filterable
              placeholder="请选择参会人员"
              class="meeting-select"
              :loading="userLoading"
          >
            <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.userName"
                :value="user.id"
            >
              <div class="user-option">
                <el-avatar
                    :size="24"
                    class="user-avatar text-avatar"
                    :style="getAvatarStyle(user.userName)"
                >
                  {{ getAvatarText(user.userName) }}
                </el-avatar>
                <div class="user-info">
                  <span class="user-name">{{ user.userName || '未知用户' }}</span>
                  <span class="user-profile">{{ user.userProfile }}</span>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addMeetingVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddMeeting" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, onMounted, onBeforeUnmount, watch, nextTick} from 'vue'
import {Plus, Clock, Location, User, Edit, Delete, Search, Refresh} from '@element-plus/icons-vue'
import {
  getAllMeetingList,
  getMeetingTypeList,
  getAllMeetingRooms,
  getMeetingParticipants,
  addMeeting,
  type MeetingListParams,
  type MeetingRecord,
  type MeetingType,
  type MeetingRoom,
  type AddMeetingParams,
  getUserList,
  deleteMeeting, syncMeetingStatus
} from '@/api/meeting'
import {ElMessage, ElMessageBox} from 'element-plus'
import dayjs from 'dayjs'
import isSameOrBefore from 'dayjs/plugin/isSameOrBefore'
import isSameOrAfter from 'dayjs/plugin/isSameOrAfter'
import {useRouter} from 'vue-router'

dayjs.extend(isSameOrBefore)
dayjs.extend(isSameOrAfter)

const router = useRouter()
const sync_loading = ref(false)

interface Host {
  name: string
  role: string
  avatar: string
  color: {
    bg: string
    text: string
  }
}

interface Meeting {
  id: number
  title: string
  tag?: string
  status: {
    label: string
    value: string
    type: string
  }
  time: string
  room: string
  description: string
  host: Host
  participants: Array<{
    id: number
    avatar: string
    color: {
      bg: string
      text: string
    }
  }>
  canEdit?: boolean
  canDelete?: boolean
  canJoin?: boolean
}

// 筛选条件
const filterStatus = ref('all')
const startDate = ref(null)
const endDate = ref(null)
const searchTitle = ref('')
const searchRoomId = ref('')
const searchTypeId = ref('')

// 状态选项
const statusOptions = [
  {label: '全部状态', value: 'all'},
  {label: '进行中', value: 'ONGOING'},
  {label: '未开始', value: 'PENDING'},
  {label: '已结束', value: 'FINISHED'},
  {label: '已取消', value: 'CANCELLED'}
]

// 分页
const currentPage = ref(1)
const pageSize = ref(4)
const total = ref(0)

// 会议列表数据
const displayMeetings = ref<any[]>([])

// 会议类型列表
const meetingTypes = ref<MeetingType[]>([])

// 会议室列表
const meetingRooms = ref<MeetingRoom[]>([])

// 新增会议表单数据
const addMeetingForm = ref<AddMeetingParams>({
  description: '',
  endTime: '',
  participantIds: [],
  roomId: 0,
  startTime: '',
  title: '',
  typeId: 0
})

// 新增会议对话框可见性
const addMeetingVisible = ref(false)

// 用户列表数据
const userList = ref<UserInfo[]>([])
const userLoading = ref(false)

// 添加防抖函数
const debounce = (fn: Function, delay: number) => {
  let timer: number | null = null
  return (...args: any[]) => {
    if (timer) {
      clearTimeout(timer)
    }
    timer = window.setTimeout(() => {
      fn.apply(null, args)
      timer = null
    }, delay)
  }
}

// 添加 ResizeObserver 相关逻辑
const listContainer = ref<HTMLElement | null>(null)
let resizeObserver: ResizeObserver | null = null

// 初始化 ResizeObserver
const initResizeObserver = () => {
  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver((entries) => {
      // 使用 requestAnimationFrame 来限制回调频率
      window.requestAnimationFrame(() => {
        if (!entries.length) return
        handleResize()
      })
    })

    if (listContainer.value) {
      resizeObserver.observe(listContainer.value)
    }
  }
}

// 处理 resize 事件
const handleResize = debounce(() => {
  fetchMeetingList()
}, 100)

// 清理 ResizeObserver
onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})

// 修改会议列表数据的计算方式
const computedDisplayMeetings = computed(() => {
  return displayMeetings.value.map((meeting: MeetingRecord) => {
    const visibleParticipants = meeting.participants?.slice(0, 3) || []
    return {
      ...meeting,
      participants: visibleParticipants,
      totalParticipants: meeting.participants?.length || 0
    }
  })
})

// 优化渲染列表方法
const renderMeetingList = () => {
  return computedDisplayMeetings.value.map((meeting: MeetingRecord) => ({
    ...meeting,
    key: meeting.id,
    shouldRender: true,
    renderKey: `${meeting.id}-${meeting.status.value}`
  }))
}

// 防抖处理筛选条件变化
const debouncedFilterChange = debounce(async () => {
  await fetchMeetingList()
}, 300)

// 修改监听器
watch([searchTitle, filterStatus, startDate, endDate, searchRoomId, searchTypeId], () => {
  currentPage.value = 1
  debouncedFilterChange()
}, {deep: true})

// 生成头像背景色
const getAvatarColor = (name?: string | null) => {
  const colors = [
    {bg: '#FF7D00', text: '#fff'},
    {bg: '#00B42A', text: '#fff'},
    {bg: '#165DFF', text: '#fff'},
    {bg: '#722ED1', text: '#fff'},
    {bg: '#F5319D', text: '#fff'},
    {bg: '#3491FA', text: '#fff'},
    {bg: '#F77234', text: '#fff'},
    {bg: '#7BC616', text: '#fff'}
  ]

  const safeName = name || '用户'
  const index = safeName.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0) % colors.length
  return colors[index]
}

// 头像文字：统一取用户名第一个字，用户名为空时显示“用”
const getAvatarText = (name?: string | null) => {
  const safeName = (name || '用户').trim()
  return safeName.substring(0, 1)
}

// 头像样式：根据用户名固定生成背景色，看起来像图标头像
const getAvatarStyle = (name?: string | null) => {
  const color = getAvatarColor(name)
  return {
    backgroundColor: color.bg,
    color: color.text
  }
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

// 获取状态标签
const getStatusLabel = (status: string): string => {
  const statusMap: Record<string, string> = {
    'ONGOING': '进行中',
    'PENDING': '未开始',
    'FINISHED': '已结束',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态类型
const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    'ONGOING': 'success',
    'PENDING': 'warning',
    'FINISHED': 'info',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取会议列表数据：只请求全部会议，不再区分“我创建 / 我参与”
const fetchMeetingList = async () => {
  try {
    const params: MeetingListParams = {
      current: currentPage.value,
      pageSize: pageSize.value,
      endTime: endDate.value ? dayjs(endDate.value).format('YYYY-MM-DD HH:mm:ss') : '',
      startTime: startDate.value ? dayjs(startDate.value).format('YYYY-MM-DD HH:mm:ss') : '',
      id: 0,
      organizerId: '',
      participantId: 0,
      roomId: searchRoomId.value ? Number(searchRoomId.value) : 0,
      sortField: '',
      sortOrder: '',
      status: filterStatus.value === 'all' ? null : filterStatus.value,
      title: searchTitle.value.trim(),
      typeId: searchTypeId.value ? Number(searchTypeId.value) : 0
    }

    const response = await getAllMeetingList(params)
    await handleMeetingResponse(response)
  } catch (error) {
    console.error('获取会议列表失败:', error)
    ElMessage.error('获取会议列表失败')
  }
}

// 获取参会人列表
const fetchParticipants = async (meetingId: number) => {
  try {
    const response = await getMeetingParticipants(meetingId)
    return response.data
  } catch (error) {
    console.error('获取参会人列表失败:', error)
    return []
  }
}

// 处理会议响应数据
const handleMeetingResponse = async (response: any) => {
  const records = response?.data?.records || []

  const meetings = await Promise.all(records.map(async (meeting: MeetingRecord) => {
    const participants = await fetchParticipants(Number(meeting.id))
    const hostName = meeting.organizerName || '未知用户'
    const hostColor = getAvatarColor(hostName)
    const statusValue = meeting.status || 'UNKNOWN'

    return {
      id: Number(meeting.id),
      title: meeting.title || '未命名会议',
      tag: meeting.meetingType?.typeName || '会议',
      description: meeting.description || '',
      time: `${dayjs(meeting.startTime).format('YYYY-MM-DD HH:mm')} - ${dayjs(meeting.endTime).format('HH:mm')}`,
      room: `${meeting.roomName || '未知会议室'}${meeting.roomLocation ? ` (${meeting.roomLocation})` : ''}`,
      status: {
        label: getStatusLabel(statusValue),
        value: statusValue,
        type: getStatusType(statusValue)
      },
      host: {
        name: hostName,
        role: '主持人',
        avatar: meeting.organizerAvatar || '',
        color: hostColor
      },
      participants: (participants || []).map((p: any) => ({
        ...p,
        userName: p.userName || '未知用户',
        userAvatar: p.userAvatar || '',
        color: getAvatarColor(p.userName || '未知用户')
      })),
      // 现在是“全部会议”，先统一显示编辑/删除按钮。
      // 如果不想在全部会议里编辑删除，把下面两个 true 改成 false。
      canEdit: true,
      canDelete: true,
      canJoin: meeting.status === 'PENDING',
      startTime: meeting.startTime,
      endTime: meeting.endTime,
      roomId: meeting.roomId,
      typeId: meeting.meetingType?.id || 0
    }
  }))

  displayMeetings.value = meetings
  total.value = Number(response?.data?.total || meetings.length || 0)
}

// 处理分页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchMeetingList()
}

// 处理筛选条件变化
const handleFilterChange = async () => {
  currentPage.value = 1
  await fetchMeetingList()
}

// 重置筛选条件
const resetFilters = () => {
  filterStatus.value = 'all'
  startDate.value = null
  endDate.value = null
  searchTitle.value = ''
  searchRoomId.value = ''
  searchTypeId.value = ''
  currentPage.value = 1
  fetchMeetingList()
}

// 修改初始化逻辑
onMounted(async () => {
  await Promise.all([fetchMeetingTypes(), fetchMeetingRooms(), fetchUserList()])
  await fetchMeetingList()
  initResizeObserver()
})

// 处理新建会议
const handleNewMeeting = () => {
  router.push('/meeting/form')
}
//同步会议状态
const handleSyncMeetingStatus = async () => {
  sync_loading.value = true
  setTimeout(async () => {
    const {message} = await syncMeetingStatus();
    if (message == 'ok') {
      ElMessage.success("同步成功");
      sync_loading.value = false
      await fetchMeetingList()
      return
    }
    ElMessage.error(message);
  }, 3000)

}
// 提交新建会议
const submitAddMeeting = async () => {
  if (!addMeetingFormRef.value) return

  try {
    await addMeetingFormRef.value.validate()
    submitting.value = true

    // 转换时间格式为 ISO 8601
    const formData = {
      ...addMeetingForm.value,
      startTime: dayjs(addMeetingForm.value.startTime).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(addMeetingForm.value.endTime).format('YYYY-MM-DD HH:mm:ss')
    }

    const response = await addMeeting(formData)
    if (response.code === 0) {
      ElMessage.success('新建会议成功')
      addMeetingVisible.value = false
      // 重置表单
      addMeetingFormRef.value.resetFields()
      // 刷新会议列表
      await fetchMeetingList()
    } else {
      ElMessage.error(response.message || '新建会议失败')
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

const addMeetingFormRef = ref()
const submitting = ref(false)

// 表单验证规则
const rules = {
  title: [
    {required: true, message: '请输入会议标题', trigger: 'blur'},
    {min: 2, max: 50, message: '标题长度应在 2-50 个字符之间', trigger: 'blur'}
  ],
  description: [
    {required: true, message: '请输入会议描述', trigger: 'blur'},
    {max: 500, message: '描述长度不能超过 500 个字符', trigger: 'blur'}
  ],
  startTime: [
    {required: true, message: '请选择开始时间', trigger: 'change'},
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!value) {
          callback(new Error('请选择开始时间'))
          return
        }
        if (addMeetingForm.value.endTime && dayjs(value).isAfter(dayjs(addMeetingForm.value.endTime))) {
          callback(new Error('开始时间不能晚于结束时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  endTime: [
    {required: true, message: '请选择结束时间', trigger: 'change'},
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (!value) {
          callback(new Error('请选择结束时间'))
          return
        }
        if (addMeetingForm.value.startTime && dayjs(value).isBefore(dayjs(addMeetingForm.value.startTime))) {
          callback(new Error('结束时间不能早于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  roomId: [
    {required: true, message: '请选择会议室', trigger: 'change'}
  ],
  typeId: [
    {required: true, message: '请选择会议类型', trigger: 'change'}
  ]
}

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    userLoading.value = true
    const response = await getUserList({
      current: 1,
      pageSize: 10,
      mpOpenId: '',
      sortField: '',
      sortOrder: '',
      userName: '',
      userRole: 'user'
    })
    userList.value = response.data.records
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    userLoading.value = false
  }
}

// 处理编辑会议
const handleEditMeeting = (meetingId: number) => {
  router.push(`/meeting/form?id=${meetingId}`)
}

// 处理删除会议
const handleDeleteMeeting = async (meetingId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会议吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteMeeting(meetingId)
    if (response.code === 0) {
      ElMessage.success('删除会议成功')
      // 重新加载会议列表
      await fetchMeetingList()
    } else {
      ElMessage.error(response.message || '删除会议失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会议失败:', error)
      ElMessage.error('删除会议失败')
    }
  }
}

// 在 script setup 部分添加处理方法
const handleViewDetail = (meetingId: number) => {
  router.push(`/meeting/detail/${meetingId}`)
}
</script>

<style scoped>
/* 筛选区域样式 */
.meeting-manage {
  padding: 32px;
  background: #fff;
  min-height: calc(100vh - 11vh);
  max-width: 100%;
  contain: layout style paint;
  transform: translateZ(0);
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #fff;
  padding: 12px 16px;
  border-radius: 4px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-select {
  width: 120px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-picker {
  width: 180px;
}

.date-separator {
  color: #86909c;
  font-size: 14px;
}

:deep(.el-input__wrapper) {
  background-color: #f2f3f5;
  box-shadow: none !important;
}

:deep(.el-input__wrapper:hover) {
  background-color: #e5e6eb;
}

:deep(.el-select .el-input__wrapper) {
  background-color: #f2f3f5;
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: #4e5969;
}

:deep(.el-input__inner::placeholder) {
  color: #86909c;
}

:deep(.el-overlay) {
  width: 100%;
  height: 100vh;
}

:deep(.el-dialog) {
  margin: 0 auto;
}

.new-meeting-btn {
  height: 32px;
  padding: 0 16px;
  font-size: 14px;
  border-radius: 4px;
}

.new-meeting-btn .el-icon {
  margin-right: 4px;
  font-size: 14px;
}

.meeting-tabs {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
  padding: 0 4px;
}

.tab-item {
  font-size: 14px;
  color: #4E5969;
  cursor: pointer;
  padding-bottom: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.tab-item.active {
  color: #1D2129;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: #165DFF;
  border-radius: 1px;
}

.tab-item:hover {
  color: #1D2129;
}

.meeting-list {
  margin: 0 -8px;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  position: relative;
  contain: layout style paint;
  transform: translate3d(0, 0, 0);
  will-change: transform;
  z-index: 1;
}

.meeting-col {
  margin-bottom: 16px;
  padding: 0 8px;
  width: 50%;
  flex-shrink: 0;
  contain: layout style;
  transform: translate3d(0, 0, 0);
  will-change: transform;
}

.meeting-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  height: 100%;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  border: 1px solid #E5E7EB;
  contain: content;
  transform: translate3d(0, 0, 0);
  will-change: transform;
  backface-visibility: hidden;
  -webkit-font-smoothing: antialiased;
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.meeting-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.meeting-title h3 {
  font-size: 15px;
  font-weight: 500;
  color: #1D2129;
  margin: 0;
  line-height: 24px;
}

.meeting-tag {
  background: #E8F3FF;
  color: #165DFF;
  border: none;
  height: 22px;
  padding: 0 8px;
  font-size: 12px;
  border-radius: 4px;
  line-height: 22px;
}

.status-tag {
  height: 22px;
  padding: 0 8px;
  font-size: 12px;
  border-radius: 4px;
  line-height: 22px;
  margin-left: 12px;
  flex-shrink: 0;
}

.meeting-time {
  font-size: 14px;
  color: #1D2129;
  margin-bottom: 8px;
  line-height: 22px;
}

.meeting-desc {
  font-size: 13px;
  color: #86909C;
  line-height: 20px;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meeting-location {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #86909C;
  font-size: 13px;
  margin-bottom: 20px;
  line-height: 20px;
}

.meeting-location .el-icon {
  font-size: 16px;
}

.meeting-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-top: 20px;
  border-top: 1px solid #E5E6EB;
}

.meeting-participants {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.host-info,
.participant-list {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  line-height: 20px;
}

.host-label,
.participant-label {
  color: #86909C;
  white-space: nowrap;
  min-width: 52px;
}

.host-avatar,
.participant-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.text-avatar {
  font-size: 13px;
  font-weight: 600;
}

.host-name {
  color: #1D2129;
}

.host-role {
  color: #86909C;
  margin-left: 4px;
}

.participant-avatars {
  display: flex;
  align-items: center;
  contain: layout style;
  transform: translate3d(0, 0, 0);
  will-change: transform;
}

.participant-avatar {
  isolation: isolate;
  contain: layout style;
}

.avatar-more {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #4E5969;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 4px;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1),
  box-shadow 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  transform: translate3d(0, 0, 0);
  will-change: transform, box-shadow;
  contain: layout style;
  isolation: isolate;
}

.avatar-more:hover {
  background: #E5E6EB;
  color: #1D2129;
  transform: translate3d(0, -1px, 0);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.meeting-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: 16px;
}

.meeting-actions .el-button--link {
  height: 32px;
  width: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.meeting-actions .el-icon {
  font-size: 16px;
}

.join-btn {
  height: 32px;
  padding: 0 16px;
  font-size: 13px;
  border-radius: 4px;
  font-weight: 500;
}

:deep(.el-tag--success) {
  --el-tag-bg-color: #E8FFEA;
  --el-tag-border-color: transparent;
  --el-tag-text-color: #00B42A;
}

:deep(.el-tag--warning) {
  --el-tag-bg-color: #FFF3E8;
  --el-tag-border-color: transparent;
  --el-tag-text-color: #FF7D00;
}

:deep(.el-tag--info) {
  --el-tag-bg-color: #F2F3F5;
  --el-tag-border-color: transparent;
  --el-tag-text-color: #86909C;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #165DFF;
  --el-button-border-color: #165DFF;
  --el-button-hover-bg-color: #4080FF;
  --el-button-hover-border-color: #4080FF;
  --el-button-active-bg-color: #0E42D2;
  --el-button-active-border-color: #0E42D2;
}

:deep(.el-button.is-disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-section {
  display: grid;
  grid-template-columns: 200px 1fr 200px;
  align-items: center;
  padding: 24px 24px 5px;
}

.page-info {
  font-size: 13px;
  color: #86909c;
  grid-column: 1;
}

/* 分页组件样式 */
:deep(.el-pagination) {
  grid-column: 2;
  justify-self: center;
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: #165DFF;
  --el-pagination-font-size: 14px;

  .btn-prev,
  .btn-next {
    background-color: transparent;
    border: 1px solid #E5E6EB;
    border-radius: 2px;
    padding: 0;
    margin: 0 4px;

    &:hover {
      color: #165DFF;
    }
  }

  .el-pager {
    li {
      border: 1px solid #E5E6EB;
      border-radius: 2px;
      margin: 0 4px;
      min-width: 32px;
      background-color: #fff;
      color: #4E5969;
      font-weight: normal;

      &:hover {
        color: #165DFF;
        border-color: #165DFF;
      }

      &.is-active {
        background-color: #165DFF;
        color: #fff;
        border-color: #165DFF;
      }
    }
  }
}

/* 响应式布局 */
@media (max-width: 768px) {
  .filter-section {
    flex-wrap: wrap;
  }

  .filter-item {
    width: 100%;
  }

  .meeting-col {
    width: 100%;
  }

  .meeting-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .meeting-participants {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

/* 空状态样式 */
.meeting-empty {
  width: 100%;
  padding: 60px 0;
}

:deep(.meeting-empty .el-empty__description) {
  margin-top: 12px;
  margin-bottom: 20px;
  color: #86909C;
  font-size: 14px;
}

:deep(.meeting-empty .el-empty__image) {
  filter: grayscale(20%) opacity(80%);
}

/* 添加新的样式 */
.title-search {
  width: 200px;
}

.room-select,
.type-select {
  width: 140px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-item {
  margin-bottom: 0;
}

@media (max-width: 1200px) {
  .filter-left {
    flex-wrap: wrap;
    gap: 8px;
  }

  .filter-item {
    margin-bottom: 8px;
  }

  .title-search,
  .status-select,
  .room-select,
  .type-select {
    width: 100%;
    max-width: none;
  }

  .date-range {
    width: 100%;
    justify-content: space-between;
  }
}

:deep(.el-avatar--circle) {
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 添加性能优化相关的样式 */
:deep(.el-avatar) {
  contain: layout style;
  will-change: transform;
  transform: translate3d(0, 0, 0);
}

:deep(.el-tag) {
  contain: layout style;
  will-change: transform;
  transform: translate3d(0, 0, 0);
}

/* 优化滚动性能 */
:deep(*) {
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
}

:deep(.el-dialog) {
  margin: 50px auto;
  //display: none;
}

.meeting-dialog :deep(.el-dialog__header) {
  margin: 0;
  display: none;
}

.dialog-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 24px;
  background: #f7f8fa;
  border-radius: 8px 8px 0 0;
}

.dialog-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #e8f3ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-icon .el-icon {
  font-size: 24px;
  color: #1677ff;
}

.dialog-title {
  flex: 1;
}

.dialog-title h3 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.dialog-title p {
  margin: 0;
  font-size: 14px;
  color: #86909c;
}

.meeting-form {
  padding: 24px;
}

.datetime-picker {
  width: 100%;
}

.meeting-select {
  width: 100%;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f2f3f5;
  margin-top: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #1d2129;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__wrapper) {
  box-shadow: none;
  border: 1px solid #dcdfe6;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__wrapper:hover) {
  border-color: #1677ff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__wrapper.is-focus) {
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
}

/* 优化对话框样式 */
.meeting-dialog :deep(.el-dialog) {
  margin: 0;
  border-radius: 8px;
  overflow: hidden;
  --el-dialog-padding-primary: 0;
  max-width: 90%;
}

.meeting-dialog :deep(.el-dialog__body) {
  margin: 0;
  padding: 0;
  max-height: calc(90vh - 180px);
  overflow-y: auto;
}

.dialog-header {
  padding: 24px 32px;
  background: #f7f8fa;
  border-bottom: 1px solid #e5e6eb;
}

.dialog-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #e8f3ff;
}

.dialog-title h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.meeting-form {
  padding: 32px;
}

/* 优化表单样式 */
.meeting-form :deep(.el-form-item) {
  margin-bottom: 32px;
}

.meeting-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.meeting-form :deep(.el-form-item__label) {
  padding-right: 16px;
  font-size: 14px;
  font-weight: 500;
  line-height: 32px;
  color: #1d2129;
}

/* 优化输入框样式 */
.meeting-form :deep(.el-input__wrapper),
.meeting-form :deep(.el-textarea__wrapper) {
  padding: 4px 12px;
  background-color: #f7f8fa;
  border: 1px solid #e5e6eb;
  box-shadow: none;
  transition: all 0.2s;
}

.meeting-form :deep(.el-input__wrapper:hover),
.meeting-form :deep(.el-textarea__wrapper:hover) {
  border-color: #1677ff;
  background-color: #fff;
}

.meeting-form :deep(.el-input__wrapper.is-focus),
.meeting-form :deep(.el-textarea__wrapper.is-focus) {
  background-color: #fff;
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
}

/* 优化日期选择器和下拉选择器 */
.datetime-picker,
.meeting-select {
  width: 100%;
}

.meeting-form :deep(.el-date-editor),
.meeting-form :deep(.el-select) {
  width: 100%;
}

.meeting-form :deep(.el-select .el-input__wrapper) {
  background-color: #f7f8fa;
}

.meeting-form :deep(.el-select .el-input__wrapper:hover) {
  background-color: #fff;
}

/* 优化底部按钮区域 */
:deep(.el-dialog__footer) {
  padding: 16px 32px;
  background: #fff;
  border-top: 1px solid #f2f3f5;
  position: sticky;
  bottom: 0;
  z-index: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer :deep(.el-button) {
  min-width: 88px;
  height: 36px;
  padding: 0 20px;
  font-size: 14px;
  border-radius: 4px;
  font-weight: 500;
}

.dialog-footer :deep(.el-button--primary) {
  background: #1677ff;
  border-color: #1677ff;
}

.dialog-footer :deep(.el-button--primary:hover) {
  background: #4096ff;
  border-color: #4096ff;
}

.dialog-footer :deep(.el-button--primary:active) {
  background: #0958d9;
  border-color: #0958d9;
}

/* 优化文本域计数器样式 */
.meeting-form :deep(.el-input__count) {
  background: transparent;
  color: #86909c;
  font-size: 12px;
}

/* 优化滚动条样式 */
.meeting-dialog :deep(.el-dialog__body::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

.meeting-dialog :deep(.el-dialog__body::-webkit-scrollbar-thumb) {
  background: #cdd0d6;
  border-radius: 3px;
}

.meeting-dialog :deep(.el-dialog__body::-webkit-scrollbar-track) {
  background: #f2f3f5;
  border-radius: 3px;
}

/* 优化表单项间距 */
.meeting-form :deep(.el-form-item__content) {
  line-height: 32px;
}

/* 优化必填星号颜色 */
.meeting-form :deep(.el-form-item.is-required .el-form-item__label::before) {
  color: #ff4d4f;
}

/* 优化错误状态样式 */
.meeting-form :deep(.el-form-item.is-error .el-input__wrapper),
.meeting-form :deep(.el-form-item.is-error .el-textarea__wrapper) {
  background-color: #fff;
  border-color: #ff4d4f;
}

.meeting-form :deep(.el-form-item.is-error .el-input__wrapper:hover),
.meeting-form :deep(.el-form-item.is-error .el-textarea__wrapper:hover) {
  border-color: #ff7875;
}

.meeting-form :deep(.el-form-item.is-error .el-input__wrapper.is-focus),
.meeting-form :deep(.el-form-item.is-error .el-textarea__wrapper.is-focus) {
  border-color: #ff7875;
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
}

.meeting-form :deep(.el-form-item__error) {
  color: #ff4d4f;
  font-size: 12px;
  line-height: 1.5;
  padding-top: 4px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  color: #1d2129;
  line-height: 1.4;
}

.user-profile {
  font-size: 12px;
  color: #86909c;
  line-height: 1.4;
}

:deep(.el-select-dropdown__item) {
  padding: 0 12px;
}

:deep(.el-select__tags) {
  padding: 2px 0;
}

:deep(.el-tag) {
  margin: 2px 4px 2px 0;
  height: 24px;
  padding: 0 8px;
  border-radius: 4px;
}
</style>