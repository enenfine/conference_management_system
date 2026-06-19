import {createApp} from 'vue'
import pinia from "./stores";
import piniaPersist from 'pinia-plugin-persistedstate'

import App from './App.vue'
import router from './router'
import './router/permission'  // 导入路由权限控制
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
// import './style.css'

const app = createApp(App)

pinia.use(piniaPersist);
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})
app.mount('#app')
