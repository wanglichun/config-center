<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <div :class="['sidebar', { 'sidebar-collapse': isCollapse }]">
      <div class="logo">
        <el-icon size="24" color="#409EFF">
          <Setting />
        </el-icon>
        <span v-show="!isCollapse" class="logo-text">{{ t('app.title') }}</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item
            v-if="!route.children"
            :index="route.path"
            :class="{ 'is-active': activeMenu === route.path }"
          >
            <el-icon>
              <component :is="iconMap[route.meta.icon]" />
            </el-icon>
            <template #title>{{ getMenuTitle(route.meta.title) }}</template>
          </el-menu-item>
          
          <el-sub-menu v-else :index="route.path">
            <template #title>
              <el-icon>
                <component :is="iconMap[route.meta.icon]" />
              </el-icon>
              <span>{{ getMenuTitle(route.meta.title) }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon>
                <component :is="iconMap[child.meta.icon]" />
              </el-icon>
              <template #title>{{ getMenuTitle(child.meta.title) }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>

        <el-container class="main-container" :style="{ marginLeft: isCollapse ? '64px' : '240px' }">
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-button
              text
              @click="toggleSidebar"
            >
              <el-icon size="18">
                <Fold v-if="!isCollapse" />
                <Expand v-else />
              </el-icon>
            </el-button>
            
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item
                v-for="item in breadcrumbs"
                :key="item.path"
                :to="item.path"
              >
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-button text @click="toggleTheme">
              <el-icon size="18">
                <Sunny v-if="isDark" />
                <Moon v-else />
              </el-icon>
            </el-button>
            
            <el-dropdown @command="handleLanguageChange">
              <el-button text>
                <span>{{ currentLocale === 'zh-CN' ? '中文' : 'English' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="zh-CN" :class="{ 'is-active': currentLocale === 'zh-CN' }">
                    中文
                  </el-dropdown-item>
                  <el-dropdown-item command="en-US" :class="{ 'is-active': currentLocale === 'en-US' }">
                    English
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.realName?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.realName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    {{ t('nav.profile') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    {{ t('nav.settings') }}
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    {{ t('nav.logout') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容区 -->
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Setting, 
  Fold, 
  Expand, 
  Sunny, 
  Moon, 
  ArrowDown, 
  User, 
  SwitchButton,
  Odometer,
  Clock,
  Monitor
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { BreadcrumbItem } from '@/types/common'
import { useI18n } from 'vue-i18n'
import { setLocale, getLocale } from '@/i18n'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const isCollapse = ref(false)
const isDark = ref(false)
const currentLocale = ref(getLocale())

// 图标映射
const iconMap: Record<string, any> = {
  Dashboard: Odometer,
  Setting,
  Clock,
  User,
  Monitor
}

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 菜单路由
const menuRoutes = computed(() => {
  return router.options.routes
    .find(r => r.path === '/')
    ?.children?.filter(child => 
      !child.meta?.hidden && 
      (!child.meta?.role || child.meta.role.includes(userStore.userInfo?.role || ''))
    ) || []
})

// 面包屑导航
const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  const breadcrumbs: BreadcrumbItem[] = []
  
  matched.forEach((item, index) => {
    if (index === 0) return // 跳过根路由
    
    breadcrumbs.push({
      title: item.meta?.title as string,
      path: index === matched.length - 1 ? undefined : item.path
    })
  })
  
  return breadcrumbs
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 切换主题
const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
}

// 处理语言切换
const handleLanguageChange = (locale: string) => {
  setLocale(locale)
  currentLocale.value = locale
  ElMessage.success(t('common.loading'))
}

// 获取菜单标题
const getMenuTitle = (title: string) => {
  const titleMap: Record<string, string> = {
    '仪表盘': 'nav.dashboard',
    '配置管理': 'nav.config',
    '变更历史': 'nav.history',
    '用户管理': 'nav.users',
    '系统监控': 'nav.monitor',
    '个人中心': 'nav.profile'
  }
  return titleMap[title] ? t(titleMap[title]) : title
}

// 处理用户菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info(t('common.loading'))
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(t('login.messages.logoutConfirm'), t('common.confirm'), {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        })
        
        userStore.logout()
        router.push('/login')
        ElMessage.success(t('login.messages.logoutSuccess'))
      } catch (error) {
        // 用户取消
      }
      break
  }
}

// 监听路由变化，自动展开菜单
watch(
  () => route.path,
  () => {
    // 可以在这里添加一些路由变化的处理逻辑
  }
)
</script>

<style lang="scss" scoped>
.layout {
  height: 100vh;
  
  .sidebar {
    background: #001529;
    transition: width 0.3s;
    height: 100vh;
    position: fixed;
    left: 0;
    top: 0;
    z-index: 100;
    display: flex;
    flex-direction: column;
    width: 240px;
    
    &.sidebar-collapse {
      width: 64px;
    }
    
    .logo {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 60px;
      background: rgba(255, 255, 255, 0.1);
      flex-shrink: 0;
      
      .logo-text {
        margin-left: 12px;
        color: white;
        font-size: 18px;
        font-weight: 600;
      }
    }
    
    .sidebar-menu {
      border: none;
      background: transparent;
      flex: 1;
      overflow-y: auto;
      
      :deep(.el-menu-item) {
        color: rgba(255, 255, 255, 0.8);
        
        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: white;
        }
        
        &.is-active {
          background: #409EFF;
          color: white;
        }
      }
      
      :deep(.el-sub-menu__title) {
        color: rgba(255, 255, 255, 0.8);
        
        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: white;
        }
      }
    }
  }
  
  .main-container {
    transition: margin-left 0.3s;
    height: 100vh;
  }
  
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    background: white;
    border-bottom: 1px solid #f0f0f0;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    
    .header-left {
      display: flex;
      align-items: center;
      
      .breadcrumb {
        margin-left: 20px;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        border-radius: 6px;
        cursor: pointer;
        transition: background-color 0.3s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        .username {
          font-size: 14px;
          color: #333;
        }
      }
      
      .el-dropdown {
        .el-button {
          border: none;
          padding: 8px 12px;
          
          &:hover {
            background: #f5f7fa;
          }
        }
      }
    }
  }
  
  .main {
    background: #f5f7fa;
    padding: 20px;
    overflow-y: auto;
  }
}

// 语言选择器样式
:deep(.el-dropdown-menu__item.is-active) {
  background: #409EFF;
  color: white;
  
  &:hover {
    background: #409EFF;
    color: white;
  }
}

@media (max-width: 768px) {
  .layout {
    .sidebar {
      z-index: 1000;
    }
    
    .main-container {
      margin-left: 0 !important;
    }
    
    .header {
      .breadcrumb {
        display: none;
      }
    }
  }
}
</style> 