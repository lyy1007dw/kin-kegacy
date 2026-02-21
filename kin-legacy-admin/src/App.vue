<script setup lang="ts">
import { 
  NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, 
  zhCN, dateZhCN, darkTheme, type GlobalThemeOverrides 
} from 'naive-ui'
import { ref, computed } from 'vue'

const isDark = ref(false)

const lightThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#8B5A2B',
    primaryColorHover: '#A0522D',
    primaryColorPressed: '#6B4423',
    primaryColorSuppl: '#8B5A2B',
    successColor: '#7CB342',
    successColorHover: '#8BC34A',
    successColorPressed: '#689F38',
    warningColor: '#F9A825',
    warningColorHover: '#FBC02D',
    warningColorPressed: '#F57F17',
    errorColor: '#D84315',
    errorColorHover: '#E64A19',
    errorColorPressed: '#BF360C',
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    fontFamily: '"Noto Serif SC", -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
  },
  Button: {
    borderRadiusMedium: '10px',
    fontSizeMedium: '14px',
    heightMedium: '36px'
  },
  Card: {
    borderRadius: '16px',
    paddingMedium: '24px'
  },
  Input: {
    borderRadius: '8px',
    heightMedium: '38px'
  },
  Select: {
    borderRadius: '8px',
    heightMedium: '38px'
  },
  Menu: {
    itemColorActive: 'rgba(196, 163, 90, 0.15)',
    itemColorActiveHover: 'rgba(196, 163, 90, 0.2)',
    itemColorHover: 'rgba(255, 255, 255, 0.08)',
    itemTextColor: 'rgba(255, 255, 255, 0.85)',
    itemTextColorHover: '#FFFFFF',
    itemTextColorActive: '#C4A35A',
    itemTextColorActiveHover: '#C4A35A'
  },
  DataTable: {
    borderRadius: '12px',
    thColor: '#FAF6F1',
    thTextColor: '#3D2914',
    tdColor: '#FFFFFF',
    tdTextColor: '#4A3728',
    thFontWeight: '600',
    thPaddingMedium: '16px 20px',
    tdPaddingMedium: '16px 20px'
  },
  Tag: {
    borderRadius: '6px'
  },
  Modal: {
    borderRadius: '16px'
  },
  Pagination: {
    itemBorderRadius: '8px'
  }
}

const darkThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#C4A35A',
    primaryColorHover: '#D4B896',
    primaryColorPressed: '#A08040',
    primaryColorSuppl: '#C4A35A',
    bodyColor: '#121212',
    cardColor: '#1e1e1e',
    textColorBase: '#e0e0e0',
    textColor1: '#e0e0e0',
    textColor2: '#a0a0a0',
    textColor3: '#666666',
    borderColor: '#333333',
    successColor: '#7CB342',
    successColorHover: '#8BC34A',
    successColorPressed: '#689F38',
    warningColor: '#F9A825',
    warningColorHover: '#FBC02D',
    warningColorPressed: '#F57F17',
    errorColor: '#D84315',
    errorColorHover: '#E64A19',
    errorColorPressed: '#BF360C',
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    fontFamily: '"Noto Serif SC", -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
  },
  Button: {
    borderRadiusMedium: '10px',
    fontSizeMedium: '14px',
    heightMedium: '36px'
  },
  Card: {
    borderRadius: '16px',
    paddingMedium: '24px',
    color: '#1e1e1e',
    borderColor: '#333333'
  },
  Input: {
    borderRadius: '8px',
    heightMedium: '38px',
    color: '#2a2a2a',
    colorFocus: '#2a2a2a'
  },
  Select: {
    borderRadius: '8px',
    heightMedium: '38px'
  },
  Menu: {
    color: 'transparent',
    itemColorActive: 'rgba(196, 163, 90, 0.2)',
    itemColorActiveHover: 'rgba(196, 163, 90, 0.25)',
    itemColorHover: 'rgba(255, 255, 255, 0.08)',
    itemTextColor: 'rgba(255, 255, 255, 0.85)',
    itemTextColorHover: '#FFFFFF',
    itemTextColorActive: '#C4A35A',
    itemTextColorActiveHover: '#C4A35A'
  },
  DataTable: {
    borderRadius: '12px',
    thColor: '#252525',
    thTextColor: '#e0e0e0',
    tdColor: '#1e1e1e',
    tdTextColor: '#c0c0c0',
    thFontWeight: '600',
    thPaddingMedium: '16px 20px',
    tdPaddingMedium: '16px 20px',
    borderColor: '#333333'
  },
  Tag: {
    borderRadius: '6px'
  },
  Modal: {
    borderRadius: '16px',
    color: '#1e1e1e'
  },
  Pagination: {
    itemBorderRadius: '8px'
  },
  Dropdown: {
    color: '#1e1e1e'
  }
}

const themeOverrides = computed(() => isDark.value ? darkThemeOverrides : lightThemeOverrides)

const updateTheme = (dark: boolean) => {
  isDark.value = dark
  document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')
}

window.addEventListener('theme-change', ((e: CustomEvent) => {
  updateTheme(e.detail)
}) as EventListener)
</script>

<template>
  <NConfigProvider 
    :locale="zhCN" 
    :date-locale="dateZhCN" 
    :theme="isDark ? darkTheme : null"
    :theme-overrides="themeOverrides"
  >
    <NMessageProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <router-view />
        </NNotificationProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-warm);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #C4B5A5;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #A89888;
}
</style>
