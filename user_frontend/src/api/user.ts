import request from '@/utils/request'


//获取用户ip地址
export function getUserIp() {
    return request({
        url: '/user/get/ip',
        method: 'get'
    })
}