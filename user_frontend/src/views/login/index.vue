<template>
  <div class="login-container">
    <div class="login-box">
      <div class="form-container">
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="登录" name="login">
            <!-- 使用 el-form 包裹表单内容 -->
            <el-form
                @submit.prevent="handleLogin"
                :model="loginForm"
                :rules="loginRules"
                ref="loginFormRef"
            >
              <h2 class="title">欢迎回来</h2>

              <!-- 用户名输入 -->
              <div class="form-group">
                <el-form-item prop="userAccount">  <!-- 绑定验证规则 -->
                  <el-input
                      v-model="loginForm.userAccount"
                      placeholder="用户名"
                      :prefix-icon="User"
                      clearable
                  />
                </el-form-item>
              </div>

              <!-- 密码输入 -->
              <div class="form-group">
                <el-form-item prop="userPassword">
                  <el-input
                      v-model="loginForm.userPassword"
                      type="password"
                      placeholder="密码"
                      :prefix-icon="Lock"
                      show-password
                      clearable
                  />
                </el-form-item>
              </div>

              <!-- 登录按钮 -->
              <el-button
                  type="primary"
                  class="submit-button"
                  native-type="submit"
                  :loading="loading"
                  :disabled="loading"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>

            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <div class="form-content">
              <h2 class="title">创建账号</h2>
              <div class="register-form-wrapper">
                <!-- 包裹 el-form 并监听 submit 事件 -->
                <el-form @submit.prevent="handleRegister"> <!-- 关键修改：添加表单提交监听 -->
                  <div class="form-group">
                    <!-- 用户名输入框 -->
                    <el-form-item> <!-- 可选：添加 el-form-item 增强表单验证能力 -->
                      <el-input
                          v-model="registerForm.userAccount"
                          placeholder="请输入用户名"
                          :prefix-icon="User"
                      />
                    </el-form-item>
                  </div>

                  <div class="form-group">
                    <!-- 密码输入框 -->
                    <el-form-item>
                      <el-input
                          v-model="registerForm.userPassword"
                          type="password"
                          placeholder="请输入密码"
                          :prefix-icon="Lock"
                          show-password
                      />
                    </el-form-item>
                  </div>

                  <div class="form-group">
                    <!-- 确认密码输入框 -->
                    <el-form-item>
                      <el-input
                          v-model="registerForm.checkPassword"
                          type="password"
                          placeholder="请再次输入密码"
                          :prefix-icon="Lock"
                          show-password
                      />
                    </el-form-item>
                  </div>

                  <!-- 注册按钮：添加 native-type="submit" -->
                  <el-button
                      type="primary"
                      class="submit-button"
                      native-type="submit"
                  >
                    立即注册
                  </el-button>
                </el-form>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div class="image-container">
        <img :src="activeTab === 'login' ? loginImage : registerImage" :alt="activeTab === 'login' ? '登录' : '注册'"
             class="auth-image">
        <div class="overlay-content">
          <h2>{{ activeTab === 'login' ? '会议管理系统' : '开启您的旅程' }}</h2>
          <p>{{ activeTab === 'login' ? '高效便捷的会议管理平台' : '加入我们，体验智能会议管理' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive} from 'vue'
import {User, Lock} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {useRouter, useRoute} from 'vue-router'
import {useUserStore} from '@/stores/modules/user.ts'
import loginImage from '@/assets/login.svg'
import registerImage from '@/assets/register.svg'
import {login, register} from '@/api/login'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('login')

const loginForm = reactive({
  userAccount: '',
  userPassword: ''
})

const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const handleLogin = async () => {
  if (!loginForm.userAccount || !loginForm.userPassword) {
    ElMessage.warning('请填写完整的登录信息')
    return
  }

  try {
    const res = await login(loginForm)

    // 使用 Pinia store 存储用户信息
    userStore.setUserInfo(res.data)
    console.log('登录后的 store 状态:', {
      isLogin: userStore.isLogin,
      userInfo: userStore.userInfo
    })
    ElMessage.success('登录成功')
    // 获取重定向地址
    const redirect = route.query.redirect as string
    router.push(redirect || '/meetings')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败，请稍后重试')
  }
}

const handleRegister = async () => {
  if (!registerForm.userAccount || !registerForm.userPassword || !registerForm.checkPassword) {
    ElMessage.warning('请填写完整的注册信息')
    return
  }
  if (registerForm.userPassword !== registerForm.checkPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  try {
    const response = await register(registerForm)
    console.log(response)
    if (response.code === 0) {
      ElMessage.success('注册成功')
      // 清空注册表单
      registerForm.userAccount = ''
      registerForm.userPassword = ''
      registerForm.checkPassword = ''
      // 切换到登录页
      activeTab.value = 'login'
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error: any) {
    console.error('注册错误：', error)
    ElMessage.error(error.response?.message || '注册失败，请稍后重试')
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(140deg, #f6f7ff 0%, #e9f0ff 50%, #f0f4ff 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(140deg, rgba(68, 129, 235, 0.05) 0%, rgba(4, 190, 254, 0.08) 100%);
  pointer-events: none;
}

.login-box {
  width: min(1000px, 90%);
  height: min(600px, 90%);
  display: flex;
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.form-container {
  width: 50%;
  padding: 2.5rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  position: relative;
}

.form-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, rgba(68, 129, 235, 0.05), rgba(4, 190, 254, 0.05));
  pointer-events: none;
}

.login-tabs {
  flex: 1;
  position: relative;
  z-index: 1;
}

:deep(.el-tabs__header) {
  margin-bottom: 2.5rem;
  border-bottom: none;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: rgba(0, 0, 0, 0.05);
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(45deg, #4481eb, #04befe);
  height: 3px;
  border-radius: 3px;
}

:deep(.el-tabs__item) {
  font-size: 1.1rem;
  color: #2c3e50;
  font-weight: 500;
  padding: 0 2rem;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: #4481eb;
  font-weight: 600;
}

:deep(.el-tabs__item:hover) {
  color: #4481eb;
}

.form-content {
  padding: 0 1rem;
  position: relative;
}

.title {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 2.5rem;
  text-align: center;
  font-weight: 600;
  background: linear-gradient(45deg, #4481eb, #04befe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 10px rgba(68, 129, 235, 0.1);
}

.form-group {
  margin-bottom: 1.8rem;
  position: relative;
}

:deep(.el-input) {
  --el-input-bg-color: rgba(255, 255, 255, 0.9);
  --el-input-border-color: rgba(0, 0, 0, 0.08);
  --el-input-hover-border-color: #4481eb;
  --el-input-focus-border-color: #4481eb;
  height: 50px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03) !important;
  padding: 0 1rem;
  border-radius: 12px;
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(68, 129, 235, 0.08) !important;
  transform: translateY(-1px);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(68, 129, 235, 0.12) !important;
  transform: translateY(-1px);
}

:deep(.el-input__inner) {
  height: 50px;
  font-size: 1rem;
  color: #2c3e50;
}

:deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

:deep(.el-input__prefix-inner) {
  font-size: 1.2rem;
  color: #94a3b8;
}

.submit-button {
  width: 100%;
  height: 50px;
  background: linear-gradient(45deg, #4481eb, #04befe);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  margin-top: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.submit-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
  );
  transition: 0.5s;
}

.submit-button:hover {
  background: linear-gradient(45deg, #3371db, #03a9f4);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(68, 129, 235, 0.3);
}

.submit-button:hover::before {
  left: 100%;
}

.image-container {
  width: 50%;
  background: linear-gradient(45deg, #4481eb, #04befe);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 2rem;
}

.auth-image {
  width: 80%;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 10px 20px rgba(0, 0, 0, 0.2));
  transition: all 0.6s ease-in-out;
}

.overlay-content {
  position: absolute;
  bottom: 3rem;
  left: 0;
  width: 100%;
  text-align: center;
  color: white;
  padding: 0 2rem;
}

.overlay-content h2 {
  font-size: 2rem;
  margin-bottom: 1rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.overlay-content p {
  font-size: 1.1rem;
  opacity: 0.9;
}

@media (max-width: 1024px) {
  .login-box {
    width: 90%;
    max-width: 800px;
  }
}

@media (max-width: 768px) {
  .login-box {
    width: 95%;
    height: 90%;
    flex-direction: column-reverse;
  }

  .form-container,
  .image-container {
    width: 100%;
  }

  .form-container {
    padding: 1.5rem;
  }

  .image-container {
    padding: 2rem;
    min-height: 200px;
  }

  .auth-image {
    width: 60%;
  }

  .overlay-content {
    bottom: 1rem;
  }

  .overlay-content h2 {
    font-size: 1.5rem;
  }

  .overlay-content p {
    font-size: 1rem;
  }
}
</style>