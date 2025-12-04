import axios from 'axios'
import type { AxiosInstance } from 'axios'

// 统一响应格式
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// 创建axios实例
const axiosInstance: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：添加token
axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('ms_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应
axiosInstance.interceptors.response.use(
  response => {
    return response.data // 直接返回data部分
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 包装函数以提供正确的类型
const api = {
  get: <T = any>(url: string, config?: any): Promise<Result<T>> => axiosInstance.get(url, config),
  post: <T = any>(url: string, data?: any, config?: any): Promise<Result<T>> => axiosInstance.post(url, data, config),
  put: <T = any>(url: string, data?: any, config?: any): Promise<Result<T>> => axiosInstance.put(url, data, config),
  delete: <T = any>(url: string, config?: any): Promise<Result<T>> => axiosInstance.delete(url, config),
}

// ==================== 认证接口 ====================
export const authApi = {
  login: (username: string, password: string): Promise<Result<{
    token: string
    salesmanId: number | null
    userId: number | null
    username: string
    role: string
    name: string
    needCompleteProfile?: boolean
  }>> => 
    api.post('/auth/login', { username, password }),
  register: (data: {
    username: string
    password: string
    role: string
    name?: string
    adminKey?: string
  }): Promise<Result<string>> =>
    api.post('/auth/register', data),
  logout: () => 
    api.post('/auth/logout')
}

// ==================== 销售员接口 ====================
export const salesmanApi = {
  getAll: () => api.get('/salesmen'),
  getById: (id: number) => api.get(`/salesmen/${id}`),
  search: (keyword: string) => api.get('/salesmen/search', { params: { keyword } }),
  create: (data: any) => api.post('/salesmen', data),
  update: (id: number, data: any) => api.put(`/salesmen/${id}`, data),
  updateContact: (id: number, data: any) => {
    const patchApi = {
      patch: <T = any>(url: string, data?: any): Promise<Result<T>> => 
        axiosInstance.patch(url, data)
    }
    return patchApi.patch(`/salesmen/${id}/contact`, data)
  },
  delete: (id: number) => api.delete(`/salesmen/${id}`),
  promote: (id: number) => api.post(`/salesmen/${id}/promote`),
  demote: (id: number) => api.post(`/salesmen/${id}/demote`),
  getPerformanceSuggestion: () => api.get('/salesmen/performance-suggestion')
}

// ==================== 客户接口 ====================
export const customerApi = {
  getAll: (role?: string, salesmanId?: number) => 
    api.get('/customers', { params: { role, salesmanId } }),
  getAllWithSalesman: (role?: string, salesmanId?: number, level?: string) =>
    api.get('/customers/with-salesman', { params: { role, salesmanId, level } }),
  getById: (id: number) => api.get(`/customers/${id}`),
  search: (keyword: string, role?: string, salesmanId?: number) => 
    api.get('/customers/search', { params: { keyword, role, salesmanId } }),
  create: (data: any) => api.post('/customers', data),
  update: (id: number, data: any) => api.put(`/customers/${id}`, data),
  delete: (id: number) => api.delete(`/customers/${id}`)
}

// ==================== 产品接口 ====================
export const productApi = {
  getAll: () => api.get('/products'),
  getById: (id: number) => api.get(`/products/${id}`),
  search: (keyword: string) => api.get('/products/search', { params: { keyword } }),
  create: (data: any) => api.post('/products', data),
  update: (id: number, data: any) => api.put(`/products/${id}`, data),
  delete: (id: number) => api.delete(`/products/${id}`)
}

// ==================== 销售记录接口 ====================
export const salesRecordApi = {
  getAll: (role?: string, salesmanId?: number) => 
    api.get('/sales', { params: { role, salesmanId } }),
  getById: (id: number) => api.get(`/sales/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/sales/salesman/${salesmanId}`),
  getByCustomer: (customerId: number) => api.get(`/sales/customer/${customerId}`),
  getByDateRange: (startDate: string, endDate: string) => 
    api.get('/sales/date-range', { params: { startDate, endDate } }),
  create: (data: any) => api.post('/sales', data),
  update: (id: number, data: any) => api.put(`/sales/${id}`, data),
  delete: (id: number) => api.delete(`/sales/${id}`),
  getTotalSales: (salesmanId: number) => api.get(`/sales/stats/total-sales/${salesmanId}`),
  getTotalCommission: (salesmanId: number) => api.get(`/sales/stats/total-commission/${salesmanId}`)
}

// ====================联络记录接口 ====================
export const contactRecordApi = {
  getAll: (role?: string, salesmanId?: number) => 
    api.get('/contacts', { params: { role, salesmanId } }),
  getById: (id: number) => api.get(`/contacts/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/contacts/salesman/${salesmanId}`),
  getByCustomer: (customerId: number) => api.get(`/contacts/customer/${customerId}`),
  create: (data: any) => api.post('/contacts', data),
  update: (id: number, data: any) => api.put(`/contacts/${id}`, data),
  delete: (id: number) => api.delete(`/contacts/${id}`)
}

// ==================== 服务记录接口 ====================
export const serviceRecordApi = {
  getAll: (role?: string, salesmanId?: number) => 
    api.get('/services', { params: { role, salesmanId } }),
  getById: (id: number) => api.get(`/services/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/services/salesman/${salesmanId}`),
  getByCustomer: (customerId: number) => api.get(`/services/customer/${customerId}`),
  getAverageSatisfaction: (salesmanId: number) => api.get(`/services/satisfaction/${salesmanId}`),
  create: (data: any) => api.post('/services', data),
  update: (id: number, data: any) => api.put(`/services/${id}`, data),
  delete: (id: number) => api.delete(`/services/${id}`)
}

// ==================== 催款记录接口 ====================
export const collectionRecordApi = {
  getAll: (role?: string, salesmanId?: number) => 
    api.get('/collections', { params: { role, salesmanId } }),
  getById: (id: number) => api.get(`/collections/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/collections/salesman/${salesmanId}`),
  getByStatus: (status: string) => api.get(`/collections/status/${status}`),
  getCollectionRate: (salesmanId: number) => api.get(`/collections/rate/${salesmanId}`),
  create: (data: any) => api.post('/collections', data),
  update: (id: number, data: any) => api.put(`/collections/${id}`, data),
  delete: (id: number) => api.delete(`/collections/${id}`)
}

// ==================== 投诉记录接口 ====================
export const complaintRecordApi = {
  getAll: (role?: string, salesmanId?: number) => 
    api.get('/complaints', { params: { role, salesmanId } }),
  getById: (id: number) => api.get(`/complaints/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/complaints/salesman/${salesmanId}`),
  getByStatus: (status: string) => api.get(`/complaints/status/${status}`),
  getCount: (salesmanId: number) => api.get(`/complaints/count/${salesmanId}`),
  create: (data: any) => api.post('/complaints', data),
  update: (id: number, data: any) => api.put(`/complaints/${id}`, data),
  delete: (id: number) => api.delete(`/complaints/${id}`)
}

// ==================== 绩效统计接口 ====================
export const performanceApi = {
  getAllPerformance: () => api.get('/performance/all'),
  getSalesmanPerformance: (salesmanId: number) => api.get(`/performance/salesman/${salesmanId}`),
  getPerformanceByDateRange: (salesmanId: number, startDate: string, endDate: string) =>
    api.get(`/performance/salesman/${salesmanId}/date-range`, { params: { startDate, endDate } }),
  getActiveSalesmenCount: () => api.get('/performance/active-salesmen-count')
}

export default api
