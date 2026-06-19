<template>
  <div class="meeting-type-container">
    <!-- 头部搜索和操作 -->
    <div class="header">
      <div class="title">
        <el-icon><Document /></el-icon>
        会议类型管理
      </div>
      <div class="search-area">
        <div class="search-form">
          <el-input
            v-model="searchParams.typeName"
            placeholder="请输入类型名称"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @input="handleSearch"
          />
          <el-input
            v-model="searchParams.typeCode"
            placeholder="请输入类型编码"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @input="handleSearch"
          />
          <el-button type="primary" :icon="Search" @click.stop="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click.stop="handleReset">重置</el-button>
        </div>
        <div class="action-buttons">
          <el-button type="primary" :icon="Plus" @click.stop="handleAdd">新增类型</el-button>
        </div>
      </div>
    </div>

    <!-- 会议类型表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="typeList"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
        :row-style="{ height: '60px' }"
        border
      >
        <el-table-column prop="id" label="ID" width="80" align="center">
          <template #default="{ row }">
            <span class="type-id">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="类型名称" min-width="150">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="typeCode" label="类型编码" min-width="150">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain">{{ row.typeCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.createTime" placement="top">
              <span class="time-text">{{ row.createTime }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.updateTime" placement="top">
              <span class="time-text">{{ row.updateTime }}</span>
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
            <el-option label="20条/页" :value="20" />
            <el-option label="50条/页" :value="50" />
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑会议类型' : '新增会议类型'"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        class="type-form"
        status-icon
      >
        <el-form-item label="类型名称" prop="typeName">
          <el-input v-model="formData.typeName" placeholder="请输入类型名称" />
        </el-form-item>

        <el-form-item label="类型编码" prop="typeCode">
          <el-input v-model="formData.typeCode" placeholder="请输入类型编码" />
        </el-form-item>

        <el-form-item label="类型描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入类型描述"
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
import { Search, Plus, Refresh, Document, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getMeetingTypeListPage,
  getMeetingTypeDetail,
  addMeetingType,
  updateMeetingType,
  deleteMeetingType
} from '@/api/meeting'
import type {
  MeetingTypeDetail,
  MeetingTypeListParams,
  AddMeetingTypeParams,
  UpdateMeetingTypeParams
} from '@/api/meeting'

// 搜索参数
const searchParams = ref<MeetingTypeListParams>({
  current: 1,
  pageSize: 10,
  typeName: '',
  typeCode: '',
  sortField: '',
  sortOrder: ''
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 数据列表
const typeList = ref<MeetingTypeDetail[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const submitLoading = ref(false)

// 表单数据
const formData = ref<AddMeetingTypeParams | UpdateMeetingTypeParams>({
  typeName: '',
  typeCode: '',
  description: ''
})

// 表单校验规则
const rules = {
  typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  typeCode: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
  description: [{ required: true, message: '请输入类型描述', trigger: 'blur' }]
}

// 获取数据列表
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getMeetingTypeListPage(searchParams.value)
    typeList.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (error) {
    console.error('获取会议类型列表失败:', error)
    ElMessage.error('获取会议类型列表失败')
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

// 重置搜索
const handleReset = () => {
  currentPage.value = 1
  pageSize.value = 10
  searchParams.value = {
    current: 1,
    pageSize: 10,
    typeName: '',
    typeCode: '',
    sortField: '',
    sortOrder: ''
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

// 新增处理
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
  formData.value = {
    typeName: '',
    typeCode: '',
    description: ''
  }
}

// 编辑处理
const handleEdit = async (row: MeetingTypeDetail) => {
  isEdit.value = true
  dialogVisible.value = true
  try {
    const { data } = await getMeetingTypeDetail(row.id)
    formData.value = {
      id: Number(data.id),
      typeName: data.typeName,
      typeCode: data.typeCode,
      description: data.description
    }
  } catch (error) {
    console.error('获取会议类型详情失败:', error)
    ElMessage.error('获取会议类型详情失败')
  }
}

// 删除处理
const handleDelete = (row: MeetingTypeDetail) => {
  ElMessageBox.confirm('确定要删除该会议类型吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMeetingType(row.id)
      ElMessage.success('删除成功')
      if (typeList.value.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1
        searchParams.value.current = currentPage.value
      }
      await fetchData()
    } catch (error: any) {
      console.error('删除会议类型失败:', error)
      ElMessage.error(error?.message || '删除会议类型失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请先完善会议类型信息')
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateMeetingType(formData.value as UpdateMeetingTypeParams)
      ElMessage.success('更新成功')
    } else {
      await addMeetingType(formData.value as AddMeetingTypeParams)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.meeting-type-container {
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
          
          :deep(.el-input__wrapper) {
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
          }
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
      // 设置表头高度
      .el-table__header th {
        height: 50px;
        font-weight: 600;
      }

      // 设置单元格垂直居中
      .el-table__cell {
        vertical-align: middle;
      }
    }
  }

  .type-id {
    color: #666;
    // font-family: monospace;
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

.type-form {
  padding: 20px 20px 0;
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
</style> 