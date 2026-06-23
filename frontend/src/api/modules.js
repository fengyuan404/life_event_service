import http from './http'

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  logout: () => http.post('/auth/logout')
}

export const dashboardApi = {
  summary: () => http.get('/dashboard/summary'),
  monthIncome: () => http.get('/dashboard/month-income')
}

export const familyApi = {
  list: (params) => http.get('/families', { params }),
  create: (data) => http.post('/families', data),
  update: (id, data) => http.put(`/families/${id}`, data),
  remove: (id) => http.delete(`/families/${id}`)
}

export const deceasedApi = {
  list: (params) => http.get('/deceased', { params }),
  create: (data) => http.post('/deceased', data),
  update: (id, data) => http.put(`/deceased/${id}`, data)
}

export const graveApi = {
  areas: (params) => http.get('/grave-areas', { params }),
  createArea: (data) => http.post('/grave-areas', data),
  updateArea: (id, data) => http.put(`/grave-areas/${id}`, data),
  list: (params) => http.get('/graves', { params }),
  create: (data) => http.post('/graves', data),
  update: (id, data) => http.put(`/graves/${id}`, data),
  empty: () => http.get('/graves/empty')
}

export const rentApi = {
  list: (params) => http.get('/rents', { params }),
  create: (data) => http.post('/rents', data),
  update: (id, data) => http.put(`/rents/${id}`, data),
  expiring: () => http.get('/rents/expiring')
}

export const paymentApi = {
  list: (params) => http.get('/payments', { params }),
  create: (data) => http.post('/payments', data)
}

export const bookApi = {
  list: (params) => http.get('/sacrifice-books', { params }),
  create: (data) => http.post('/sacrifice-books', data),
  checkin: (id) => http.put(`/sacrifice-books/${id}/checkin`),
  cancel: (id) => http.put(`/sacrifice-books/${id}/cancel`)
}

export const staffApi = {
  list: () => http.get('/staff')
}

