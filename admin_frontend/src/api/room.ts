import request from '@/utils/request'
import type { PageParams } from '@/types/common'

// 会议室列表查询参数
export interface RoomListParams extends PageParams {
  code?: string
  name?: string
  location?: string
  minCapacity?: number
  maxCapacity?: number
  status?: number
  sortField?: string
  sortOrder?: string
}

// 会议接口
export interface RoomMeeting {
  id: string
  title: string
  description: string
  meetingCode: string
  startTime: string
  endTime: string
  status: string
  createTime: string
  updateTime: string
}

// 会议室详情接口
export interface Room {
  id: string
  roomName: string
  roomCode: string
  capacity: number
  location: string
  facilities: string[]
  status: number
  isOccupied: boolean
  currentMeeting: RoomMeeting | null
  upcomingMeetings: RoomMeeting[]
  createTime: string
  updateTime: string
}

// 会议室基本信息
export interface RoomBasic {
  id: string
  name: string
  code: string
  capacity: number
  location: string
  facilities: string[]
  status: number
  createTime: string
  updateTime: string
}

// 新增会议室参数
export interface AddRoomParams {
  roomName: string
  roomCode: string
  capacity: number
  location: string
  facilities: string[]
  description: string
  status: number
}

// 更新会议室参数
export interface UpdateRoomParams extends AddRoomParams {
  id: number
}

// 获取会议室列表
export function getRoomList(params: RoomListParams) {
  return request({
    url: '/meeting/room/list/page/vo',
    method: 'post',
    data: params
  })
}

// 获取会议室详情
export function getRoomDetail(id: string | number) {
  return request({
    url: '/meeting/room/get/vo',
    method: 'get',
    params: { id }
  })
}

// 新增会议室
export function addRoom(data: AddRoomParams) {
  return request({
    url: '/meeting/room/add',
    method: 'post',
    data
  })
}

// 更新会议室
export function updateRoom(data: UpdateRoomParams) {
  return request({
    url: '/meeting/room/update',
    method: 'post',
    data
  })
}

// 删除会议室
export function deleteRoom(id: string | number) {
  return request({
    url: '/meeting/room/delete',
    method: 'post',
    data: { id }
  })
}
