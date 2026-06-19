import request from '@/utils/request'
import type { PageParams } from '@/types/common'

// 文件类型接口
export interface FileType {
  id: string
  typeName: string
  typeCode: string
  mimeType: string
  description: string
  createTime: string
  updateTime: string
}

// 文件详情接口
export interface MaterialFile {
  id: string
  meetingId: string
  fileName: string
  fileUrl: string
  fileSize: string
  fileType: FileType | null
  uploadUserId: string
  uploaderName: string
  uploaderAvatar: string | null
  meetingTitle: string | null
  meetingCode: string | null
  createTime: string
  updateTime: string
  formattedFileSize: string
}

// 获取我的文件列表
export function getMyMaterialList(params: PageParams) {
  return request({
    url: '/meeting-file/my/list/page',
    method: 'get',
    params
  })
}

// 根据会议ID获取文件列表
export function getMaterialListByMeetingId(meetingId: string) {
  return request({
    url: '/meeting-file/list',
    method: 'get',
    params: { meetingId }
  })
}

// 上传文件
export function uploadMaterialFile(data: FormData) {
  return request({
    url: '/meeting-file/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除单个文件
export function deleteMaterialFile(fileId: string) {
  return request({
    url: `/meeting-file/delete/${fileId}`,
    method: 'post'
  })
}

// 批量删除文件
export function batchDeleteMaterialFiles(fileIds: string[]) {
  return request({
    url: '/meeting-file/delete/batch',
    method: 'post',
    data: fileIds
  })
} 