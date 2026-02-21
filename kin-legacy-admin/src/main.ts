import { createApp } from 'vue'
import { createPinia } from 'pinia'
import naive, { dateZhCN, zhCN } from 'naive-ui'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(naive, {
  locale: zhCN,
  dateLocale: dateZhCN
})

app.mount('#app')
