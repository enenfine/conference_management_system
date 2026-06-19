<template>
  <div class="documents-page">
    <!-- 会议文件部分 -->
    <el-card class="section-card">
      <div class="page-header">
        <div class="header-title">
          <el-icon>
            <Document/>
          </el-icon>
          会议文件
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="handleUpload">
            <el-icon>
              <Upload/>
            </el-icon>
            上传文件
          </el-button>
        </div>
      </div>

      <div class="documents-content">
        <el-table :data="documentList" style="width: 100%">
          <el-table-column label="会议编号" align="center">
            <template #default="{ row }">
              {{ row.meetingId }}
            </template>
          </el-table-column>

          <el-table-column label="文件名称">
            <template #default="{ row }">
              <div class="file-info">
                <el-icon class="file-icon" :class="getFileIconClass(row.fileName)">
                  <Document/>
                </el-icon>
                <span class="file-name">{{ row.fileName }}</span>
                <el-tag v-if="row.uploadUserId === '1'" size="small" class="file-tag" effect="plain">自建</el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="会议主题">
            <template #default="{ row }">
              <div class="meeting-info">
                <div class="meeting-title">{{ row.meetingTitle }}</div>
                <div class="meeting-meta">
                  主持人：{{ row.uploaderName }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="上传时间" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>

          <el-table-column label="大小" align="center">
            <template #default="{ row }">
              {{ row.formattedFileSize }}
            </template>
          </el-table-column>

          <el-table-column label="操作" fixed="right" align="center">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-button plain type="primary" size="small" class="operation-button" @click="handleDownload(row)">
                  下载
                </el-button>
                <el-button
                    v-if="row.uploadUserId === '1'"
                    plain
                    type="primary"
                    size="small"
                    class="operation-button"
                    @click="handleUploadNewVersion(row)"
                >
                  上传新版本
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <div class="total-info">共 {{ total }} 个文件</div>
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[2, 5, 10, 20]"
              layout="sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              class="custom-pagination"
              :pager-count="5"
          />
        </div>
      </div>
    </el-card>

    <!-- 上传文件对话框 -->
    <el-dialog
        v-model="uploadDialogVisible"
        title=""
        width="580px"
        :close-on-click-modal="false"
        class="minute-dialog custom-dialog"
    >
      <div class="dialog-header">
        <div class="dialog-icon">
          <el-icon>
            <UploadFilled/>
          </el-icon>
        </div>
        <div class="dialog-title">
          <h3>上传会议文件</h3>
          <p>支持多种文件格式，请选择会议并上传相关文件</p>
        </div>
      </div>

      <el-form :model="{ meetingId: selectedMeetingId }" label-width="90px" class="minute-form">
        <el-form-item label="选择会议" required>
          <el-select
              v-model="selectedMeetingId"
              placeholder="请选择关联的会议"
              style="width: 100%"
              filterable
              remote
              :remote-method="handleMeetingSearch"
              :loading="meetingSearchLoading"
              class="minute-select"
          >
            <el-option
                v-for="meeting in meetingOptions"
                :key="meeting.id"
                :label="meeting.title"
                :value="meeting.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="上传文件" required>
          <el-upload
              class="upload-area"
              drag
              action="#"
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
          >
            <div class="upload-content">
              <el-icon class="upload-icon">
                <UploadFilled/>
              </el-icon>
              <div class="upload-text">
                <p class="primary-text">将文件拖到此处，或<em>点击上传</em></p>
                <p class="sub-text">支持扩展名：.pdf, .doc, .docx, .xls, .xlsx, .ppt, .pptx</p>
              </div>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmUpload" :loading="uploading">
            {{ uploading ? '上传中...' : '确认上传' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 上传新版本对话框 -->
    <el-dialog
        v-model="newVersionDialogVisible"
        title=""
        width="580px"
        :close-on-click-modal="false"
        class="minute-dialog custom-dialog"
    >
      <div class="dialog-header">
        <div class="dialog-icon">
          <el-icon>
            <UploadFilled/>
          </el-icon>
        </div>
        <div class="dialog-title">
          <h3>上传新版本</h3>
          <p>上传文件的新版本，保持文件更新记录</p>
        </div>
      </div>

      <div class="minute-form">
        <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleNewVersionFileChange"
            :limit="1"
        >
          <div class="upload-content">
            <el-icon class="upload-icon">
              <UploadFilled/>
            </el-icon>
            <div class="upload-text">
              <p class="primary-text">将文件拖到此处，或<em>点击上传</em></p>
              <p class="sub-text">支持扩展名：.pdf, .doc, .docx, .xls, .xlsx, .ppt, .pptx</p>
            </div>
          </div>
        </el-upload>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="newVersionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmNewVersionUpload" :loading="uploading">
            {{ uploading ? '上传中...' : '确认上传' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {Document, Upload, UploadFilled} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {getMyMeetingFiles, uploadMeetingFile, type MeetingFile, getMeetingFileList} from '@/api/document'
import {getMyCreatedMeetings, searchMeetings} from '@/api/meeting'
import dayjs from 'dayjs'

// 分页相关
const currentPage = ref(1)
const pageSize = ref(2)
const total = ref(0)

// 上传对话框
const uploadDialogVisible = ref(false)
const selectedFile = ref<File | null>(null)
const selectedMeetingId = ref<string>('')
const uploading = ref(false)

// 新版本上传相关
const newVersionDialogVisible = ref(false)
const selectedFileId = ref<string>('')
const newVersionFile = ref<File | null>(null)

// 文档列表数据
const documentList = ref<MeetingFile[]>([])

// 会议列表数据
const meetingOptions = ref<{ id: string; title: string }[]>([])

// 会议搜索加载状态
const meetingSearchLoading = ref(false)

// 获取我创建的会议列表
const fetchMyMeetings = async () => {
  try {
    const response = await getMyCreatedMeetings({
      current: 1,
      size: 50
    })

    if (response.code === 0 && response.data.records) {
      meetingOptions.value = response.data.records.map((meeting: any) => ({
        id: meeting.id,
        title: meeting.title
      }))
    }
  } catch (error) {
    console.error('获取会议列表失败:', error)
    ElMessage.error('获取会议列表失败')
  }
}

// 处理会议搜索
const handleMeetingSearch = async (query: string) => {
  if (query) {
    meetingSearchLoading.value = true
    try {
      const response = await searchMeetings({
        title: query
      })

      if (response.code === 0 && response.data) {
        meetingOptions.value = response.data.map((meeting: any) => ({
          id: meeting.id,
          title: meeting.title
        }))
      }
    } catch (error) {
      console.error('搜索会议失败:', error)
      ElMessage.error('搜索会议失败')
    } finally {
      meetingSearchLoading.value = false
    }
  } else {
    await fetchMyMeetings()
  }
}

// 获取文件列表
const fetchDocumentList = async () => {
  try {
    let response

    if (selectedMeetingId.value) {
      response = await getMeetingFileList({
        meetingId: selectedMeetingId.value
      })
    } else {
      response = await getMyMeetingFiles({
        current: currentPage.value,
        size: pageSize.value
      })
    }

    if (response.code === 0) {
      documentList.value = response.data.records
      total.value = parseInt(response.data.total)
    }
  } catch (error) {
    console.error('获取文件列表失败:', error)
    ElMessage.error('获取文件列表失败')
  }
}

// 处理文件上传
const handleUpload = () => {
  uploadDialogVisible.value = true
  fetchMyMeetings()
}

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
}

const confirmUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  if (!selectedMeetingId.value) {
    ElMessage.warning('请选择关联的会议')
    return
  }

  try {
    uploading.value = true
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('meetingId', selectedMeetingId.value)

    const response = await uploadMeetingFile(formData)
    if (response.code === 0) {
      ElMessage.success('文件上传成功')
      uploadDialogVisible.value = false
      selectedFile.value = null
      selectedMeetingId.value = ''
      await fetchDocumentList()
    } else {
      ElMessage.error(response.message || '文件上传失败')
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
  }
}

// 处理新版本上传
const handleUploadNewVersion = (row: MeetingFile) => {
  selectedFileId.value = row.meetingId
  newVersionDialogVisible.value = true
}

const handleNewVersionFileChange = (file: any) => {
  newVersionFile.value = file.raw
}

const confirmNewVersionUpload = async () => {
  if (!newVersionFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  try {
    uploading.value = true
    const formData = new FormData()
    formData.append('file', newVersionFile.value)
    formData.append('meetingId', selectedFileId.value)

    const response = await uploadMeetingFile(formData)
    if (response.code === 0) {
      ElMessage.success('新版本上传成功')
      newVersionDialogVisible.value = false
      newVersionFile.value = null
      selectedFileId.value = ''
      await fetchDocumentList()
    } else {
      ElMessage.error(response.message || '新版本上传失败')
    }
  } catch (error) {
    console.error('新版本上传失败:', error)
    ElMessage.error('新版本上传失败')
  } finally {
    uploading.value = false
  }
}

// 处理文件下载
const handleDownload = (row: MeetingFile) => {
  window.open('/api' + row.fileUrl, '_blank')
}

// 处理分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchDocumentList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchDocumentList()
}

// 格式化时间
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取文件图标类名
const getFileIconClass = (fileName: string) => {
  const extension = fileName.split('.').pop()?.toLowerCase()
  switch (extension) {
    case 'pdf':
      return 'pdf'
    case 'doc':
    case 'docx':
      return 'word'
    default:
      return ''
  }
}

onMounted(() => {
  fetchDocumentList()
})
</script>

<style scoped>
.documents-page {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 110px);
}

.section-card {
  margin-bottom: 30px;
  border-radius: 5px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

:deep(.el-card__body) {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e6eb;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.header-title .el-icon {
  font-size: 20px;
  color: #4086f4;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.file-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.file-icon.pdf {
  color: #ff4d4f;
}

.file-icon.word {
  color: #1677ff;
}

.file-name {
  color: #1d2129;
  font-size: 13px;
  display: inline-block;
}

.file-tag {
  font-size: 12px;
  padding: 0 8px;
  height: 22px;
  line-height: 20px;
  background: transparent;
  color: #86909c;
  border: 1px solid #e5e6eb;
  flex-shrink: 0;
  margin-left: 8px;
}

:deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f7f8fa;
  --el-table-header-text-color: #1d2129;
  --el-table-text-color: #4e5969;
  --el-table-header-height: 44px;
  --el-table-cell-padding: 12px 16px;
  border: none;
}

:deep(.el-table th) {
  font-weight: 500;
  background: #f7f8fa;
  border-bottom: 1px solid #e5e6eb;
  font-size: 13px;
  height: 44px;
}

:deep(.el-table td) {
  border-bottom: 1px solid #e5e6eb;
  font-size: 13px;
  height: 66px;
}

:deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover td) {
  background-color: #f7f8fa;
}

.operation-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.operation-button {
  padding: 4px 12px;
  height: 28px;
  font-size: 13px;
  --el-button-bg-color: transparent;
  --el-button-border-color: #4086f4;
  --el-button-text-color: #4086f4;
  --el-button-hover-text-color: #fff;
  --el-button-hover-bg-color: #4086f4;
  --el-button-hover-border-color: #4086f4;
}

.table-footer {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px;
  margin-bottom: 30px;
}

.total-info {
  color: #86909c;
  font-size: 13px;
}

.custom-pagination {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: #4086f4;
}

:deep(.el-pagination .el-pager li) {
  border: 1px solid #dcdfe6;
  background: #fff;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  font-size: 14px;
  margin: 0 4px;
  color: #606266;
  border-radius: 2px;
  font-weight: normal;
}

:deep(.el-pagination .el-pager li.is-active) {
  color: #fff;
  font-weight: normal;
  background-color: #4086f4;
  border-color: #4086f4;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: #fff;
  border: 1px solid #dcdfe6;
  padding: 0;
  margin: 0 4px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  border-radius: 2px;
  color: #606266;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover),
:deep(.el-pagination .el-pager li:hover) {
  color: #4086f4;
  border-color: #4086f4;
}

:deep(.el-pagination .el-pager li.is-active:hover) {
  color: #fff;
}

.meeting-info {
  font-size: 13px;
  color: #86909c;
  margin-bottom: 6px;
}

.meeting-title {
  color: #000;
  font-weight: bold;
  margin-bottom: 8px;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background: #f0f7ff;
}

.upload-area :deep(.el-upload-dragger.is-dragover) {
  background-color: #f0f7ff;
  border: 2px dashed #409eff;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 20px;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.upload-text {
  text-align: center;
}

.upload-text .primary-text {
  color: #606266;
  font-size: 14px;
  margin: 0 0 4px;
}

.upload-text .primary-text em {
  color: #409eff;
  font-style: normal;
  cursor: pointer;
}

.upload-text .sub-text {
  color: #909399;
  font-size: 12px;
  margin: 0;
}

:deep(.el-upload-list) {
  margin-top: 16px;
}

:deep(.el-upload-list__item) {
  transition: all 0.3s;
  padding: 8px 12px;
  border-radius: 4px;
}

:deep(.el-upload-list__item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-upload-list__item-name) {
  color: #606266;
  font-size: 13px;
}

:deep(.el-upload-list__item .el-icon) {
  color: #909399;
  font-size: 14px;
}

:deep(.el-upload-list__item .el-icon:hover) {
  color: #409eff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 自定义对话框样式 */
.custom-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #e5e6eb;
  }
}

.dialog-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 24px 24px 20px;
  background: #f8f9fc;
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
  color: #409eff;
}

.dialog-title {
  flex: 1;
}

.dialog-title h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.dialog-title p {
  margin: 0;
  font-size: 14px;
  color: #86909c;
}

.minute-form {
  padding: 24px;

  :deep(.el-form-item__label) {
    font-weight: normal;
    color: #4e5969;
    padding-right: 12px;
    font-size: 14px;
    line-height: 32px;
  }

  :deep(.el-form-item__label::before) {
    margin-right: 2px;
  }

  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
}

.minute-select {
  :deep(.el-input__wrapper) {
    background-color: #fff;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    border-radius: 4px;
    padding: 0 12px;
    height: 32px;
    line-height: 32px;
    transition: all 0.2s;
  }

  :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #409eff inset;
  }

  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #409eff inset !important;
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    color: #1d2129;
  }
}

.dialog-footer :deep(.el-button) {
  margin: 0;
  min-width: 72px;
  height: 32px;
  padding: 0 16px;
  font-size: 14px;
  border-radius: 4px;
  font-weight: normal;
}

.dialog-footer :deep(.el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
}

.dialog-footer :deep(.el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.dialog-footer :deep(.el-button--default) {
  border-color: #dcdfe6;
  color: #606266;
}

.dialog-footer :deep(.el-button--default:hover) {
  color: #409eff;
  border-color: #409eff;
  background-color: #fff;
}
</style>
