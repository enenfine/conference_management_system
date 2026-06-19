<template>
  <div class="users-container">
    <!-- 头部搜索和操作 -->
    <div class="header">
      <div class="title">用户管理</div>
      <div class="actions">
        <el-input
          v-model="searchQuery"
          placeholder="请输入用户名称搜索"
          class="search-input"
          :prefix-icon="Search"
          clearable
          @clear="handleSearch"
          @input="handleSearchInput"
        />
        <el-button type="primary" @click="handleAdd">+ 添加用户</el-button>
        <el-select v-model="sortBy" placeholder="排序" style="width: 100px">
          <el-option label="用户名" value="userName" />
          <el-option label="创建时间" value="createTime" />
        </el-select>
      </div>
    </div>

    <!-- 用户表格 -->
    <el-table
      v-loading="loading"
      :data="userList"
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa' }"
    >
      <el-table-column prop="id" label="ID" width="80" align="center">
        <template #default="{ row }">
          <span class="user-id">{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userAccount" label="用户账户" width="120" />
      <el-table-column prop="userName" label="用户昵称" width="120" />
      <el-table-column label="用户头像" width="100" align="center">
        <template #default="{ row }">
          <el-avatar
            :size="40"
            class="name-avatar"
            :style="{ backgroundColor: getAvatarColor(row.userName || row.userAccount) }"
          >
            {{ getUserInitial(row.userName || row.userAccount) }}
          </el-avatar>
        </template>
      </el-table-column>


      <el-table-column prop="userProfile" label="用户简介" show-overflow-tooltip />
      <el-table-column prop="userRole" label="用户角色" width="100">
        <template #default="{ row }">
          <el-tag
            :type="row.userRole === 'admin' ? 'danger' : 'info'"
            effect="plain"
          >
            {{ row.userRole === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          <el-button link type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <div class="pagination-info">
        Total {{ total }}
      </div>
      <div class="pagination-select">
        <el-select v-model="pageSize" @change="handleSizeChange">
          <el-option label="10/page" :value="10" />
          <el-option label="20/page" :value="20" />
          <el-option label="50/page" :value="50" />
        </el-select>
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="Number(total)"
        layout="prev, pager, next"
        @current-change="handleCurrentChange"
      />
      <div class="goto">
        Go to
        <el-input 
          v-model.number="currentPage" 
          style="width: 60px"
          @change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 用户表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="rules"
        label-width="80px"
        status-icon
      >
        <el-form-item label="头像" prop="userAvatar">
          <div class="avatar-upload-wrapper">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
            >
              <div class="upload-area">
                <el-avatar
                  v-if="userForm.userAvatar"
                  :size="90"
                  :src="getAvatarUrl(userForm.userAvatar)"
                />
                <div v-else class="upload-placeholder">
                  <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
                  <span>点击上传头像</span>
                </div>
                <div class="upload-mask">
                  <div class="upload-hover">
                    <el-icon><Upload /></el-icon>
                    <span>更换头像</span>
                  </div>
                </div>
              </div>
            </el-upload>
            <div class="upload-info">
              <div class="upload-tip">
                <el-icon><InfoFilled /></el-icon>
                <span>支持 jpg、png 格式</span>
              </div>
              <div class="upload-tip">
                <el-icon><Warning /></el-icon>
                <span>大小不超过 2MB</span>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="账户" prop="userAccount">
          <el-input v-model="userForm.userAccount" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="昵称" prop="userName">
          <el-input v-model="userForm.userName" />
        </el-form-item>
        <el-form-item label="简介" prop="userProfile">
          <el-input
            v-model="userForm.userProfile"
            type="textarea"
            :rows="3"
            placeholder="请输入用户简介"
          />
        </el-form-item>
        <el-form-item label="角色" prop="userRole">
          <el-select v-model="userForm.userRole" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, onUnmounted } from 'vue'
import { Search, Plus, Upload, UserFilled, InfoFilled, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadProps } from 'element-plus'
import { 
  getUserList, 
  updateUser, 
  uploadAvatar, 
  addUser, 
  deleteUser,
  getUserDetail,
  resetUserPassword
} from '@/api/user'
import defaultAvatar from '@/assets/avatar.jpg'
// 表格数据
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sortBy = ref('')
const sortOrder = ref('ascend')

const userList = ref([])


// 上传相关配置
const baseUrl = import.meta.env.VITE_BASE_URL
const uploadUrl = '/api/user/upload/avatar'
const uploadHeaders = {
  // 如果需要添加token等认证信息
  // Authorization: 'Bearer ' + localStorage.getItem('token')
}

// 获取完整的头像URL
const getAvatarUrl = (avatar: string) => {
  if (!avatar) return defaultAvatar
  return `${baseUrl}${avatar}`
}

// 搜索防抖
const searchTimeout = ref<number | null>(null)

// 输入搜索处理
const handleSearchInput = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = window.setTimeout(() => {
    handleSearch()
  }, 300) // 300ms 防抖
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      current: currentPage.value,
      pageSize: pageSize.value,
      sortField: sortBy.value,
      sortOrder: sortOrder.value,
      userName: searchQuery.value
    })
    if (res.code === 0) {
      userList.value = res.data.records
      total.value = Number(res.data.total)
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取用户名第一个字
const getUserInitial = (name?: string | null) => {
  const text = (name || '').trim()
  return text ? text.substring(0, 1).toUpperCase() : '用'
}

// 根据用户名生成固定头像颜色
const getAvatarColor = (name?: string | null) => {
  const colors = [
    '#409EFF',
    '#67C23A',
    '#E6A23C',
    '#F56C6C',
    '#909399',
    '#8E44AD',
    '#16A085',
    '#D35400'
  ]

  const text = (name || '用户').trim()
  let sum = 0

  for (let i = 0; i < text.length; i++) {
    sum += text.charCodeAt(i)
  }

  return colors[sum % colors.length]
}


// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1 // 切换每页条数时重置到第一页
  fetchUserList()
}

const handleCurrentChange = (val: number) => {
  if (val < 1) {
    currentPage.value = 1
  } else if (val > Math.ceil(total.value / pageSize.value)) {
    currentPage.value = Math.ceil(total.value / pageSize.value)
  } else {
    currentPage.value = val
  }
  fetchUserList()
}

// 表单数据
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()

const userForm = reactive({
  id: 0,
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user'
})

// 表单校验规则
const rules = {
  userAccount: [
    { required: true, message: '请输入用户账户', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ]
}

// 头像上传前的验证
const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传成功的回调
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 0) {
    userForm.userAvatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

// 新增用户
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  Object.assign(userForm, {
    id: 0,
    userAccount: '',
    userName: '',
    userAvatar: '',
    userProfile: '',
    userRole: 'user'
  })
}

// 编辑用户
const handleEdit = async (row: any) => {
  try {
    const res = await getUserDetail(row.id)
    if (res.code === 0) {
      dialogType.value = 'edit'
      dialogVisible.value = true
      Object.assign(userForm, res.data)
    }
  } catch (error) {
    ElMessage.error('获取用户详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await addUser(userForm)
          ElMessage.success('添加成功')
        } else {
          await updateUser(userForm)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchUserList()
      } catch (error) {
        ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
      }
    }
  })
}

// 删除用户
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    '确定要删除该用户吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 重置密码
const handleResetPassword = (row: any) => {
  ElMessageBox.confirm(
    '确定要重置该用户的密码吗？重置后密码将变为：12345678',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await resetUserPassword({
        checkPassword: '12345678',
        userName: row.userAccount,
        userNewPassword: '12345678',
        userOldPassword: '12345678'
      })
      ElMessage.success('密码重置成功')
    } catch (error) {
      ElMessage.error('密码重置失败')
    }
  }).catch(() => {
    // 取消重置
  })
}

// 监听排序变化
watch(sortBy, () => {
  currentPage.value = 1
  fetchUserList()
})

// 初始化
onMounted(() => {
  fetchUserList()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
})
</script>

<style scoped lang="scss">
.users-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .title {
      font-size: 18px;
      font-weight: 500;
      color: #1f2937;
    }

    .actions {
      display: flex;
      gap: 12px;
      align-items: center;

      .search-input {
        width: 240px;
      }
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 16px;

    .pagination-info {
      color: #666;
    }

    .goto {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
    }
  }
}

:deep(.el-table) {
  .el-table__header {
    th {
      font-weight: 500;
      color: #1f2937;
      background-color: #f8fafc;
    }
  }
}

.avatar-upload-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 2px dashed var(--el-border-color-lighter);
    border-radius: 12px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    background-color: var(--el-fill-color-lighter);

    &:hover {
      border-color: var(--el-color-primary);
      background-color: var(--el-fill-color);
      
      .upload-mask {
        opacity: 1;
      }
    }
  }

  .upload-area {
    width: 140px;
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
  }

  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: var(--el-text-color-secondary);
    
    .avatar-uploader-icon {
      font-size: 32px;
      margin-bottom: 12px;
      color: var(--el-color-info-light-3);
    }
    
    span {
      font-size: 14px;
    }
  }

  .upload-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      180deg,
      rgba(0, 0, 0, 0) 0%,
      rgba(0, 0, 0, 0.6) 100%
    );
    opacity: 0;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .upload-hover {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #fff;
    transform: translateY(5px);
    transition: all 0.3s ease;
    
    .el-icon {
      font-size: 24px;
      margin-bottom: 8px;
    }
    
    span {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

.name-avatar {
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  border: 4px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}


.upload-info {
  padding-top: 12px;

  .upload-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
    
    .el-icon {
      font-size: 14px;
    }
    
    span {
      font-size: 13px;
    }

    &:last-child {
      margin-bottom: 0;
    }
  }
}

// 添加头像预览的样式
:deep(.el-avatar) {
  border: 4px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style> 