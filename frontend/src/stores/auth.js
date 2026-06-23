import { defineStore } from 'pinia'

const saved = JSON.parse(localStorage.getItem('life-event-auth') || '{}')

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: saved.token || '',
    staffId: saved.staffId || null,
    staffName: saved.staffName || '',
    role: saved.role || ''
  }),
  getters: {
    loggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setAuth(payload) {
      this.token = payload.token
      this.staffId = payload.staffId
      this.staffName = payload.staffName
      this.role = payload.role
      localStorage.setItem('life-event-auth', JSON.stringify(payload))
    },
    clear() {
      this.token = ''
      this.staffId = null
      this.staffName = ''
      this.role = ''
      localStorage.removeItem('life-event-auth')
    }
  }
})

