<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <el-icon size="40" color="#409EFF">
            <Setting />
          </el-icon>
        </div>
        <h2>{{ $t('login.title') }}</h2>
        <p>{{ $t('login.subtitle') }}</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            :placeholder="$t('login.usernamePlaceholder')"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            :placeholder="$t('login.passwordPlaceholder')"
            size="large"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="loginForm.remember">
            {{ $t('login.rememberMe') }}
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? $t('login.loggingIn') : $t('login.login') }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>{{ $t('login.defaultAccount') }}</p>
        <p>{{ $t('login.copyright') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import type { LoginForm } from '@/types/user'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

// 登录表单
const loginForm = reactive<LoginForm>({
  username: 'admin',
  password: 'admin123',
  remember: true
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: t('login.usernameRequired'), trigger: 'blur' },
    { min: 2, max: 20, message: t('login.usernameLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('login.passwordLength'), trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    console.log(t('login.startLogin'), loginForm)
    
    const result = await userStore.userLogin(loginForm)
    console.log(t('login.loginSuccess'), result)
    
    ElMessage.success(t('login.loginSuccess'))
    
    // 确保跳转到正确的页面
    const redirect = router.currentRoute.value.query.redirect as string
    await router.push(redirect || '/dashboard')
    
  } catch (error: any) {
    console.error(t('login.loginFailed'), error)
    
    // 显示具体的错误信息
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error(t('login.loginFailedMessage'))
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .login-box {
    width: 400px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    
    .login-header {
      text-align: center;
      margin-bottom: 30px;
      
      .logo {
        margin-bottom: 16px;
      }
      
      h2 {
        color: #333;
        margin-bottom: 8px;
        font-weight: 600;
      }
      
      p {
        color: #666;
        font-size: 14px;
      }
    }
    
    .login-form {
      .login-btn {
        width: 100%;
        height: 48px;
        font-size: 16px;
        border-radius: 8px;
      }
    }
    
    .login-footer {
      text-align: center;
      margin-top: 20px;
      
      p {
        color: #999;
        font-size: 12px;
        margin: 4px 0;
        
        &:first-child {
          color: #666;
          font-size: 13px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 20px;
    
    .login-box {
      width: 100%;
      padding: 30px 20px;
    }
  }
}
</style> 