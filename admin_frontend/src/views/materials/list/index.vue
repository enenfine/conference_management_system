<template>
  <div class="material-container">
    <!-- 头部标题和操作 -->
    <div class="header">
      <div class="title">
        <el-icon><Document /></el-icon>
        资料列表
      </div>
      <div class="action-buttons">
        <el-button
          type="danger"
          :icon="Delete"
          :disabled="!selectedRows.length"
          @click="handleBatchDelete(selectedRows)"
        >批量删除</el-button>
        <el-button type="primary" :icon="Upload" @click="handleUpload">上传文件</el-button>
      </div>
    </div>

    <!-- 文件列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="fileList"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
        :row-style="{ height: '60px' }"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="{ row }">
            <span class="file-id">{{ row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="fileName" label="文件名称" min-width="250">
          <template #default="{ row }">
            <div class="file-info">
              <el-icon class="file-icon"><Document /></el-icon>
              <span class="file-name">{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="meetingTitle" label="关联会议" min-width="180">
          <template #default="{ row }">
            <span class="meeting-title">{{ row.meetingTitle }}</span>
          </template>
        </el-table-column>

        <el-table-column label="上传者" width="150">
          <template #default="{ row }">
            <div class="uploader-info">
              <el-avatar :size="24" :src="row.uploaderAvatar" class="uploader-avatar">
                {{ row.uploaderName.charAt(0) }}
              </el-avatar>
              <span class="uploader-name">{{ row.uploaderName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="formattedFileSize" label="文件大小" width="120">
          <template #default="{ row }">
            <span class="file-size">{{ row.formattedFileSize }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="上传时间" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ row.createTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDownload(row)">
              <el-icon><Download /></el-icon>下载
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
            <el-option label="8条/页" :value="8" />
            <el-option label="16条/页" :value="16" />
            <el-option label="24条/页" :value="24" />
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

    <!-- 上传文件弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="上传文件"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        class="upload-form"
      >
        <el-form-item label="关联会议" prop="meetingId">
          <el-select v-model="formData.meetingId" placeholder="请选择会议" class="w-full">
            <el-option
              v-for="item in meetingList"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            >
              <div class="meeting-option">
                <span>{{ item.title }}</span>
                <span class="meeting-time-info">{{ item.startTime }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选择文件" prop="file">
          <el-upload
            class="upload-file"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            accept=".doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 Word、Excel、PPT、PDF 格式文件
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            上 传
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Document, Upload, Download, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadFile } from 'element-plus'
import { getMyMaterialList, uploadMaterialFile, deleteMaterialFile, batchDeleteMaterialFiles } from '@/api/material'
import type { MaterialFile } from '@/api/material'
import { getMeetingList } from '@/api/meeting'
import type { Meeting } from '@/api/meeting'

// 分页相关
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const loading = ref(false)

// 数据列表
const fileList = ref<MaterialFile[]>([])

// 获取数据列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getMyMaterialList({
      current: currentPage.value,
      size: pageSize.value
    })
    fileList.value = data.records
    total.value = Number(data.total)
  } catch (error) {
    console.error('获取文件列表失败:', error)
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

// 上传相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const meetingList = ref<Meeting[]>([])

interface UploadFormData {
  meetingId: string
  file: File | null
}

const formData = ref<UploadFormData>({
  meetingId: '',
  file: null
})

const rules = {
  meetingId: [{ required: true, message: '请选择关联会议', trigger: 'change' }],
  file: [{ required: true, message: '请选择要上传的文件', trigger: 'change' }]
}

// 获取会议列表
const fetchMeetingList = async () => {
  try {
    const { data } = await getMeetingList({
      current: 1,
      pageSize: 100
    })
    meetingList.value = data.records
  } catch (error) {
    console.error('获取会议列表失败:', error)
  }
}

// 文件选择处理
const handleFileChange = (uploadFile: UploadFile) => {
  formData.value.file = uploadFile.raw || null
}

// 上传处理
const handleUpload = () => {
  dialogVisible.value = true
  formData.value = {
    meetingId: '',
    file: null
  }
}

// 提交处理
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid && formData.value.file) {
      submitLoading.value = true
      try {
        const submitFormData = new FormData()
        submitFormData.append('meetingId', formData.value.meetingId)
        submitFormData.append('file', formData.value.file)

        await uploadMaterialFile(submitFormData)
        ElMessage.success('上传成功')
        dialogVisible.value = false
        fetchData()
      } catch (error: any) {
        console.error('上传失败:', error)
        ElMessage.error(error.message || '上传失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 下载处理
const handleDownload = (row: MaterialFile) => {
  const baseUrl = import.meta.env.VITE_BASE_URL || '/api'
  window.open(baseUrl + row.fileUrl)
}

// 删除处理
const handleDelete = (row: MaterialFile) => {
  ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMaterialFile(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除文件失败:', error)
      ElMessage.error('删除文件失败')
    }
  })
}

// 可以选择性添加批量删除功能
const handleBatchDelete = (rows: MaterialFile[]) => {
  ElMessageBox.confirm(`确定要删除选中的 ${rows.length} 个文件吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await batchDeleteMaterialFiles(rows.map(row => row.id))
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('批量删除文件失败:', error)
      ElMessage.error('批量删除文件失败')
    }
  })
}

// 添加选中行数据
const selectedRows = ref<MaterialFile[]>([])

// 选择变化处理
const handleSelectionChange = (rows: MaterialFile[]) => {
  selectedRows.value = rows
}

onMounted(() => {
  fetchData()
  fetchMeetingList()
})
</script>

<style scoped lang="scss">
.material-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;

  .header {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        font-size: 20px;
        color: var(--el-color-primary);
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

  .file-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .file-icon {
      color: var(--el-color-primary);
      font-size: 18px;
    }

    .file-name {
      color: #1f2937;
    }
  }

  .meeting-title {
    color: #1f2937;
  }

  .uploader-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .uploader-avatar {
      background: var(--el-color-primary);
      font-size: 12px;
    }

    .uploader-name {
      color: #1f2937;
    }
  }

  .file-size {
    color: #666;
    font-size: 13px;
  }

  .time-text {
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

.upload-form {
  padding: 20px 20px 0;

  .upload-file {
    :deep(.el-upload) {
      width: 100%;
    }

    :deep(.el-upload__tip) {
      color: #666;
      font-size: 12px;
      margin-top: 8px;
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

.meeting-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .meeting-time-info {
    color: #666;
    font-size: 12px;
  }
}
</style> 