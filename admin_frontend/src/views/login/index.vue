<template>
  <div class="login-page">
    <!-- 粒子背景 -->
    <canvas ref="canvasRef" class="particles-canvas"></canvas>

    <div class="login-container">
      <div class="login-box">
        <div class="login-header">
          <div class="logo">
            <el-icon size="32">
              <Monitor/>
            </el-icon>
          </div>
          <h2>会议管理系统后台</h2>
          <p>Conference Management System</p>
        </div>

        <div class="login-content">

          <!-- 登录表单 -->
          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              class="login-form"
          >
            <el-form-item prop="userAccount">
              <el-input
                  v-model="loginForm.userAccount"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  size="large"
                  class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="userPassword">
              <el-input
                  v-model="loginForm.userPassword"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  size="large"
                  show-password
                  class="custom-input"
              />
            </el-form-item>

            <el-form-item class="login-btn">
              <el-button
                  type="primary"
                  :loading="loading"
                  size="large"
                  class="submit-btn"
                  @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <div class="footer-text">
        © {{ new Date().getFullYear() }} Conference Management System. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted, onUnmounted} from 'vue'
import {User, Lock, Monitor} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {useRouter, useRoute} from 'vue-router'
import {useUserStore} from '@/stores/modules/user'
import {login} from '@/api/login'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const loginFormRef = ref()

const loginForm = reactive({
  userAccount: '',
  userPassword: ''
})

const loginRules = {
  userAccount: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'}
  ],
  userPassword: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur'}
  ]
}

const handleLogin = () => {
  if (!loginFormRef.value) return

  loginFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(loginForm)
      if (res.data.userRole != 'admin') {
        ElMessage.error('非管理员账户，无法登录！')
        return;
      }
      userStore.setUserInfo(res.data)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect as string
      router.push(redirect || '/dashboard')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

interface Particle {
  x: number
  y: number
  size: number
  speedX: number
  speedY: number
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
let animationFrameId: number
const particles: Particle[] = []
const particleCount = 80

// 初始化粒子
const initParticles = (canvas: HTMLCanvasElement) => {
  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      size: Math.random() * 2 + 1,
      speedX: Math.random() * 2 - 1,
      speedY: Math.random() * 2 - 1
    })
  }
}

// 绘制粒子
const drawParticles = (ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement) => {
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  ctx.fillStyle = 'rgba(255, 255, 255, 0.5)'
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)'

  particles.forEach((particle, i) => {
    // 绘制粒子
    ctx.beginPath()
    ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2)
    ctx.fill()

    // 绘制连接线
    particles.slice(i + 1).forEach(otherParticle => {
      const dx = particle.x - otherParticle.x
      const dy = particle.y - otherParticle.y
      const distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < 100) {
        ctx.beginPath()
        ctx.moveTo(particle.x, particle.y)
        ctx.lineTo(otherParticle.x, otherParticle.y)
        ctx.stroke()
      }
    })

    // 更新粒子位置
    particle.x += particle.speedX
    particle.y += particle.speedY

    // 边界检查
    if (particle.x < 0 || particle.x > canvas.width) particle.speedX *= -1
    if (particle.y < 0 || particle.y > canvas.height) particle.speedY *= -1
  })
}

// 动画循环
const animate = () => {
  if (!canvasRef.value) return

  const ctx = canvasRef.value.getContext('2d')
  if (!ctx) return

  drawParticles(ctx, canvasRef.value)
  animationFrameId = requestAnimationFrame(animate)
}

// 调整 canvas 大小
const resizeCanvas = () => {
  if (!canvasRef.value) return

  canvasRef.value.width = window.innerWidth
  canvasRef.value.height = window.innerHeight
}

onMounted(() => {
  if (!canvasRef.value) return

  resizeCanvas()
  initParticles(canvasRef.value)
  animate()

  window.addEventListener('resize', resizeCanvas)
})

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId)
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<style scoped lang="scss">
.login-page {
  position: relative;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%), radial-gradient(circle at top right, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.particles-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.login-container {
  position: relative;
  z-index: 1;
}

.login-box {
  width: 420px;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2),
  inset 0 0 0 1px rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo {
    width: 64px;
    height: 64px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    box-shadow: 0 8px 16px rgba(30, 60, 114, 0.3);
  }

  h2 {
    font-size: 28px;
    color: #1e3c72;
    margin-bottom: 8px;
    font-weight: 600;
  }

  p {
    font-size: 16px;
    color: #666;
    letter-spacing: 1px;
  }
}

.login-content {
  h3 {
    text-align: center;
    margin-bottom: 30px;

    .welcome-text {
      display: block;
      font-size: 24px;
      color: #1e3c72;
      font-weight: 600;
      margin-bottom: 8px;
    }

    .sub-text {
      display: block;
      font-size: 14px;
      color: #666;
    }
  }
}

.login-form {
  :deep(.el-input) {
    --el-input-height: 44px;

    .el-input__wrapper {
      background: #fff;
      box-shadow: none;
      border: 1px solid #dcdfe6;
      border-radius: 8px;
      padding: 0 16px;
      transition: all 0.3s ease;

      &:hover {
        border-color: #c0c4cc;
        transform: translateY(-1px);
      }

      &.is-focus {
        border-color: #409eff;
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
        transform: translateY(-1px);
      }
    }

    .el-input__inner {
      font-size: 15px;
      height: 44px;
      color: #606266;

      &::placeholder {
        color: #c0c4cc;
      }
    }

    .el-input__prefix-inner {
      font-size: 18px;
      color: #909399;
    }
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border: none;
  margin-top: 20px;
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 1px;
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #15325f 0%, #1e3c72 100%);
    transform: translateY(-2px);
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
  }
}

.footer-text {
  position: fixed;
  bottom: 20px;
  left: 0;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}
</style>