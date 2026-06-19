<template>
  <div class="room-container">
    <!-- 头部搜索和操作 -->
    <div class="header">
      <div class="title">
        <el-icon><OfficeBuilding /></el-icon>
        会议室管理
      </div>
      <div class="search-area">
        <div class="search-form">
          <el-input
            v-model="searchParams.name"
            placeholder="会议室名称"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @input="handleSearch"
          />
          <el-input
            v-model="searchParams.code"
            placeholder="会议室编号"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @input="handleSearch"
          />
          <el-input
            v-model="searchParams.location"
            placeholder="位置"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @input="handleSearch"
          />
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <div class="action-buttons">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增会议室</el-button>
        </div>
      </div>
    </div>

    <!-- 会议室列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="roomList"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
        :row-style="{ height: '60px' }"
        border
      >
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="{ row }">
            <span class="room-id">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="会议室信息" min-width="200">
          <template #default="{ row }">
            <div class="room-detail">
              <span class="room-name">{{ row.roomName }}</span>
              <span class="room-code">{{ row.roomCode }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="location" label="位置" width="150">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.location }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="capacity" label="容量" width="100" align="center">
          <template #default="{ row }">
            <span class="capacity">{{ row.capacity }}人</span>
          </template>
        </el-table-column>

        <el-table-column label="设施" min-width="200">
          <template #default="{ row }">
            <div class="facilities">
              <el-tag
                v-for="item in row.facilities"
                :key="item"
                size="small"
                effect="light"
                class="facility-tag"
              >{{ item }}</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
              effect="light"
              round
            >{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
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
            <el-option label="6条/页" :value="4" />
            <el-option label="8条/页" :value="8" />
            <el-option label="10条/页" :value="16" />
            <el-option label="12条/页" :value="24" />
          </el-select>
        </div>
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑会议室' : '新增会议室'"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        class="room-form"
      >
        <el-form-item label="会议室名称" prop="roomName">
          <el-input v-model="formData.roomName" placeholder="请输入会议室名称" />
        </el-form-item>

        <el-form-item label="会议室编号" prop="roomCode">
          <el-input v-model="formData.roomCode" placeholder="请输入会议室编号" />
        </el-form-item>

        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入位置" />
        </el-form-item>

        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="formData.capacity" :min="1" :max="1000" />
        </el-form-item>

        <el-form-item label="设施" prop="facilities">
          <el-select
            v-model="formData.facilities"
            multiple
            placeholder="请选择设施"
            class="w-full"
          >
            <el-option
              v-for="item in facilityOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="0">维护中</el-radio>
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="2">已使用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
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
import { ref, onMounted } from 'vue'
import { OfficeBuilding, Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getRoomList,
  addRoom,
  updateRoom,
  deleteRoom,
} from '@/api/room'
import type { Room, RoomListParams, AddRoomParams } from '@/api/room'
// 搜索参数
const searchParams = ref<RoomListParams>({
  current: 1,
  pageSize: 6,
  name: '',
  code: '',
  location: ''
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)
const loading = ref(false)

// 数据列表
const roomList = ref<Room[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const submitLoading = ref(false)

// 设施选项
const facilityOptions = [
  { label: '投影仪', value: '投影仪' },
  { label: '白板', value: '白板' },
  { label: '视频会议系统', value: '视频会议系统' },
  { label: '音响', value: '音响' },
  { label: '空调', value: '空调' }
]

// 表单数据
const formData = ref<AddRoomParams>({
  roomName: '',
  roomCode: '',
  capacity: 20,
  location: '',
  facilities: [],
  description: '',
  status: 1,
})

// 表单校验规则
const rules = {
  roomName: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }],
  roomCode: [{ required: true, message: '请输入会议室编号', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'change' }]
}

// 获取数据列表


const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getRoomList(searchParams.value)
    roomList.value = data.records
    total.value = Number(data.total)
  } catch (error) {
    console.error('获取会议室列表失败:', error)
    ElMessage.error('获取会议室列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  searchParams.value.current = 1
  fetchData()
}

// 重置处理
const handleReset = () => {
  searchParams.value = {
    current: 1,
    pageSize: 6,
    name: '',
    code: '',
    location: ''
  }
  fetchData()
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  searchParams.value.pageSize = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  searchParams.value.current = val
  fetchData()
}

// 获取状态类型
const getStatusType = (status: number) => {
  const statusMap: Record<number, string> = {
    0: 'warning',  // 维护中
    1: 'success',  // 可用
    2: 'info'      // 已使用
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '维护中',
    1: '可用',
    2: '已使用'
  }
  return statusMap[status] || '未知状态'
}

// 新增处理
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  formData.value = {
    roomName: '',
    roomCode: '',
    capacity: 20,
    location: '',
    facilities: [],
    description: '',
    status: 1,
    }
}

// 编辑处理
const handleEdit = (row: Room) => {
  isEdit.value = true
  dialogVisible.value = true
  formData.value = {
    id: Number(row.id),
    roomName: row.roomName,
    roomCode: row.roomCode,
    location: row.location,
    capacity: row.capacity,
    facilities: row.facilities,
    status: row.status,
    description: row.description || '',
  }
}

// 删除处理
const handleDelete = (row: Room) => {
  ElMessageBox.confirm('确定要删除该会议室吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRoom(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除会议室失败:', error)
      ElMessage.error('删除会议室失败')
    }
  })
}


// 提交处理
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateRoom(formData.value)
          ElMessage.success('更新成功')
        } else {
          await addRoom(formData.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error: any) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.room-container {
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

        :deep(.el-input__wrapper) {
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        }
      }

      .action-buttons {
        white-space: nowrap;
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

  .room-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .room-image {
      width: 60px;
      height: 40px;
      border-radius: 4px;
    }

    .room-detail {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .room-name {
        color: #1f2937;
        font-weight: 500;
      }

      .room-code {
        color: #666;
        font-size: 13px;
      }
    }
  }

  .facilities {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .facility-tag {
      margin: 0;
    }
  }

  .capacity {
    color: #666;
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

.room-form {
  padding: 20px 20px 0;

  .upload-images {
    :deep(.el-upload--picture-card) {
      width: 100px;
      height: 100px;
      line-height: 100px;
    }
  }
}

.dialog-footer {
  padding: 20px;
  text-align: right;
  border-top: 1px solid var(--el-border-color-lighter);
  
  .el-button {
    padding-left: 25px;
    padding-right: 25px;
    
    &:not(:first-child) {
      margin-left: 12px;
    }
  }
}

.w-full {
  width: 100% !important;
}

.room-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  max-width: 220px;
  
  .room-image {
    width: 105px;
    height: 70px;
    border-radius: 4px;
    cursor: pointer;
    
    &:hover {
      opacity: 0.9;
    }
  }

  .image-error {
    width: 105px;
    height: 70px;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 20px;
    border-radius: 4px;
  }

  .no-image {
    width: 105px;
    height: 70px;
    background: #f5f7fa;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
    gap: 4px;
    font-size: 12px;

    .el-icon {
      font-size: 16px;
    }
  }
}

.room-image-wrapper {
  width: 104px;
  height: 68px;
  margin: 0 auto;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
}

.room-image {
  width: 104px;
  height: 68px;
  display: block;
  border-radius: 6px;
}

</style> 