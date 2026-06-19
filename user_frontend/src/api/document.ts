import request from '@/utils/request'

// 文件列表响应接口
export interface MeetingFile {
  id: string
  meetingId: string
  fileName: string
  fileUrl: string
  fileSize: string
  fileType: string | null
  uploadUserId: string
  uploaderName: string
  uploaderAvatar: string
  meetingTitle: string
  meetingCode: string | null
  createTime: string
  updateTime: string
  formattedFileSize: string
}

// 分页响应数据接口
export interface MeetingFileListResponse {
  code: number
  data: {
    records: MeetingFile[]
    total: string
    size: string
    current: string
    orders: any[]
    optimizeCountSql: boolean
    searchCount: boolean
    countId: null
    maxLimit: null
    pages: string
  }
  message: string
}

/**
 * 获取我的会议文件列表
 * @param params 分页参数
 * @returns Promise
 */
export const getMyMeetingFiles = (params: { current: number; size: number; keyword?: string }) => {
  return request<MeetingFileListResponse>({
    url: '/meeting-file/my/list/page',
    method: 'get',
    params
  })
}

/**
 * 上传会议文件
 * @param data FormData 包含 file 和 meetingId
 * @returns Promise
 */
export const uploadMeetingFile = (data: FormData) => {
  return request({
    url: '/meeting-file/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

/**
 * 根据会议ID获取文件列表
 * @param params 查询参数
 * @returns Promise
 */
export const getMeetingFileList = (params: { meetingId: string; keyword?: string }) => {
  return request<MeetingFileListResponse>({
    url: '/meeting-file/list',
    method: 'get',
    params
  })
} 