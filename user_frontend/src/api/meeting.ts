import request from '@/utils/request'

export interface MeetingType {
  id: string
  typeName: string
  typeCode: string
  description: string
  createTime: string
  updateTime: string
}

export interface MeetingRoom {
  id: string
  roomName: string
  roomCode?: string
}

export interface MeetingRecord {
  id: string
  title: string
  description: string
  meetingCode: string
  roomId: string
  roomName: string
  roomLocation: string
  roomCapacity: number
  roomFacilities: string[]
  meetingType: MeetingType
  startTime: string
  endTime: string
  organizerId: string
  organizerName: string
  organizerAvatar: string
  participantCount: number
  fileCount?: number
  status: string
  createTime: string
  updateTime: string
  participants?: any[]
}

export interface Meeting extends MeetingRecord {}

export interface MeetingListParams {
  current: number
  pageSize: number
  id?: number | string
  title?: string
  roomId?: number | string
  typeId?: number | string
  startTime?: string | null
  endTime?: string | null
  organizerId?: string
  participantId?: number | string
  status?: string | null
  sortField?: string
  sortOrder?: string
}

export interface AddMeetingParams {
  title: string
  description: string
  roomId: number | string
  typeId: number | string
  startTime: string
  endTime: string
  participantIds?: Array<number | string>
}

export interface MeetingParticipant {
  id: string
  meetingId: string
  userId: string
  userName: string
  userAccount: string | null
  userAvatar: string
  role: string
  status: string
  createTime: string
  updateTime: string
}

export interface UserListParams {
  current: number
  pageSize: number
  userName?: string
  userRole?: string
}

export interface UserInfo {
  id: string
  userAccount: string
  userName: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
}

export interface RoomDetail {
  id: string
  roomName: string
  roomCode: string
  roomImage?: string[]
  capacity: number
  location: string
  facilities: string[]
  status: number
  isOccupied?: boolean
  currentMeeting?: any
  upcomingMeetings?: any[]
  createTime: string
  updateTime: string
}

export interface RoomListParams {
  current: number
  pageSize: number
  name?: string
  code?: string
  location?: string
  minCapacity?: number
  maxCapacity?: number
  status?: number
  sortField?: string
  sortOrder?: string
}

export interface RoomDetailResponse extends RoomDetail {}

export interface RoomSchedule {
  meetingId: number
  title: string
  startTime: string
  endTime: string
  typeName?: string
  organizerName?: string
  organizerAvatar?: string
}

export function getAllMeetingList(params: MeetingListParams) {
  return request({ url: '/meeting/list/page/vo', method: 'post', data: params })
}

export function getMyMeetingList(params: MeetingListParams) {
  return request({ url: '/meeting/my/list/page', method: 'post', data: params })
}

export function getParticipatedMeetingList(params: { current: number; size: number }) {
  return request({ url: '/meeting/my/participated', method: 'get', params })
}

export function getMeetingTypeList() {
  return request({ url: '/meeting-type/list', method: 'get' })
}

export function getAllMeetingRooms() {
  return request({ url: '/meeting/room/list/all', method: 'get' })
}

export function getRoomList(params: RoomListParams) {
  return request({ url: '/meeting/room/list/page/vo', method: 'post', data: params })
}

export function getRoomDetail(id: string) {
  return request({ url: '/meeting/room/get/vo', method: 'get', params: { id } })
}

export function getRoomSchedule(roomId: string, date: string) {
  return request({ url: `/meeting/room/${roomId}/schedule`, method: 'get', params: { date } })
}

export function addMeeting(params: AddMeetingParams) {
  return request({ url: '/meeting/add', method: 'post', data: params })
}

export function updateMeeting(params: AddMeetingParams & { id: number | string; status?: string }) {
  return request({ url: '/meeting/update', method: 'post', data: params })
}

export function deleteMeeting(id: number | string) {
  return request({ url: '/meeting/delete', method: 'post', data: { id } })
}

export function cancelMeeting(id: number | string) {
  return request({ url: '/meeting/cancel', method: 'post', params: { id } })
}

export function getMeetingDetail(id: string) {
  return request({ url: '/meeting/get/vo', method: 'get', params: { id } })
}

export function getMeetingParticipants(meetingId: number) {
  return request({ url: '/meeting-participant/list', method: 'get', params: { meetingId } })
}

export function getMeetingParticipantList(meetingId: string) {
  return getMeetingParticipants(Number(meetingId))
}

export function getUserList(params: UserListParams) {
  return request({ url: '/user/list/page/vo', method: 'post', data: params })
}

export function syncMeetingStatus() {
  return request({ url: '/meeting/sync/status', method: 'get' })
}

export const getMyCreatedMeetings = (params: { current: number; size: number }) => {
  return request({ url: '/meeting/my/created', method: 'get', params })
}

export const searchMeetings = (params: { title: string }) => {
  return request({ url: '/meeting/search', method: 'get', params })
}
