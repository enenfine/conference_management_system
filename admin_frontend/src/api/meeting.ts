import request from '@/utils/request'
import type { PageParams } from '@/types/common'
import { ElMessage } from 'element-plus'

// 会议列表参数类型
export interface MeetingListParams extends PageParams {
  endTime?: string
  id?: number
  organizerId?: string
  participantId?: number
  roomId?: number
  sortField?: string
  sortOrder?: string
  startTime?: string
  title?: string
  typeId?: number
}

// 会议类型接口
export interface MeetingType {
  id: number
  typeName: string
}

// 会议室接口
export interface MeetingRoom {
  id: number
  roomName: string
}

// 会议详情接口
export interface Meeting {
  id: string
  title: string
  typeId: number
  roomId: number
  startTime: string
  endTime: string
  organizerName: string
  status: string
  meetingType: MeetingType
  description: string
  participantIds: number[]
}

// 新增会议参数
export interface AddMeetingParams {
  title: string
  typeId: number
  roomId: number
  startTime: string
  endTime: string
  participantIds: number[]
  description: string
}

// 更新会议参数
export interface UpdateMeetingParams extends AddMeetingParams {
  id: string
}

// 会议类型列表查询参数
export interface MeetingTypeListParams extends PageParams {
  typeCode?: string
  typeName?: string
  sortField?: string
  sortOrder?: string
}

// 会议类型详情
export interface MeetingTypeDetail {
  id: string
  typeName: string
  typeCode: string
  description: string
  createTime: string
  updateTime: string
}

// 新增会议类型参数
export interface AddMeetingTypeParams {
  typeName: string
  typeCode: string
  description: string
}

// 更新会议类型参数
export interface UpdateMeetingTypeParams extends AddMeetingTypeParams {
  id: number
}

// 获取会议列表
export function getMeetingList(params: PageParams) {
  return request({
    url: '/meeting/list/page',
    method: 'post',
    data: params
  })
}

// 获取会议类型列表
export function getMeetingTypeList() {
  return request({
    url: '/meeting-type/list',
    method: 'get'
  })
}

// 获取会议室列表
export function getMeetingRoomList() {
  return request({
    url: '/meeting/room/list/all',
    method: 'get'
  })
}

// 新增会议
export function addMeeting(data: AddMeetingParams): Promise<{ code: number; data: { id: number }; message: string }> {
  return request({
    url: '/meeting/add',
    method: 'post',
    data
  })
}

// 更新会议
export function updateMeeting(data: UpdateMeetingParams) {
  return request({
    url: '/meeting/update',
    method: 'post',
    data
  })
}

// 删除会议
export function deleteMeeting(id: string | number) {
  return request({
    url: '/meeting/delete',
    method: 'post',
    data: { id }
  })
}

// 获取会议详情
export function getMeetingDetail(id: string) {
  return request({
    url: `/meeting/get?id=${id}`,
    method: 'get'
  })
}

// 获取会议类型分页列表
export function getMeetingTypeListPage(params: MeetingTypeListParams) {
  return request({
    url: '/meeting-type/list/page',
    method: 'post',
    data: params
  })
}

// 获取会议类型详情
export function getMeetingTypeDetail(params: { id: string | number }) {
  return request({
    url: '/meeting-type/get',
    method: 'get',
    params
  })
}

// 新增会议类型
export function addMeetingType(data: AddMeetingTypeParams) {
  return request({
    url: '/meeting-type/add',
    method: 'post',
    data
  })
}

// 更新会议类型
export function updateMeetingType(data: UpdateMeetingTypeParams) {
  return request({
    url: '/meeting-type/update',
    method: 'post',
    data
  })
}

// 删除会议类型
export function deleteMeetingType(id: string | number) {
  return request({
    url: '/meeting-type/delete',
    method: 'post',
    data: { id }
  })
}

// 添加会议参与者
export const addMeetingParticipants = async (meetingId: number, userIds: number[]) => {
  try {
    const response = await request({
      url: '/meeting-participant/add',
      method: 'post',
      data: {
        meetingId,
        userIds
      }
    })
    if (response.code === 0) {
      return true
    }
    ElMessage.error(response.message || '添加参会人员失败')
    return false
  } catch (error: any) {
    ElMessage.error('添加参会人员失败：' + error.message)
    return false
  }
}

// 获取会议参与者列表
export interface MeetingParticipant {
  id: string
  meetingId: string
  meetingTitle: string
  meetingCode: string
  meetingStartTime: string
  meetingEndTime: string
  userId: string
  userName: string
  userAccount: string | null
  userAvatar: string
  role: string
  status: string
  createTime: string
  updateTime: string
}

export const getMeetingParticipants = async (meetingId: number) => {
  try {
    const response = await request({
      url: '/meeting-participant/list',
      method: 'get',
      params: { meetingId }
    })
    if (response.code === 0) {
      return response.data as MeetingParticipant[]
    }
    return []
  } catch (error) {
    console.error('获取参会人员失败:', error)
    return []
  }
}
