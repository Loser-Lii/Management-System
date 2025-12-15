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
    if (config.url?.includes('/services')) {
      console.log('[API DEBUG] GET /services request:', config.params)
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
    if (response.config.url?.includes('/services')) {
      console.log('[API DEBUG] /services response code:', response.data?.code, 'data length:', response.data?.data?.length)
    }
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
  getAll: (includeResigned?: boolean) => api.get('/salesmen', { params: { includeResigned } }),
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
  resign: (id: number) => api.post(`/salesmen/${id}/resign`),
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
  findWithoutSalesman: () => api.get('/customers/without-salesman'),
  getById: (id: number) => api.get(`/customers/${id}`),
  search: (keyword: string, role?: string, salesmanId?: number) => 
    api.get('/customers/search', { params: { keyword, role, salesmanId } }),
  create: (data: any) => api.post('/customers', data),
  update: (id: number, data: any) => api.put(`/customers/${id}`, data),
  delete: (id: number) => api.delete(`/customers/${id}`),
  unbind: (id: number) => api.put(`/customers/${id}/unbind`)
}

// ==================== 产品接口 ====================
export const productApi = {
  getAll: () => api.get('/products'),
  getById: (id: number) => api.get(`/products/${id}`),
  search: (keyword: string) => api.get('/products/search', { params: { keyword } }),
  getNextNo: () => api.get<string>('/products/next-no'),
  create: (data: any) => api.post('/products', data),
  update: (id: number, data: any) => api.put(`/products/${id}`, data),
  delete: (id: number) => api.delete(`/products/${id}`)
}

// ==================== 销售记录接口 ====================
export const salesRecordApi = {
  getAll: (role?: string, salesmanId?: number) => 
    api.get('/sales', { params: { role, salesmanId } }),
  getById: (id: number) => api.get(`/sales/${id}`),
  getByOrderNo: (orderNo: string) => api.get(`/sales/order/${orderNo}`),
  getBySalesman: (salesmanId: number) => api.get(`/sales/salesman/${salesmanId}`),
  getByCustomer: (customerId: number) => api.get(`/sales/customer/${customerId}`),
  getByDateRange: (startDate: string, endDate: string) => 
    api.get('/sales/date-range', { params: { startDate, endDate } }),
  getByStatus: (status: string) => api.get(`/sales/status/${status}`),
  getBySalesmanAndStatus: (salesmanId: number, status: string) => 
    api.get(`/sales/salesman/${salesmanId}/status/${status}`),
  getProductsBySalesmanAndCustomer: (salesmanId: number, customerId: number) =>
    api.get('/sales/products/by-salesman-customer', { params: { salesmanId, customerId } }),
  create: (data: any) => api.post('/sales', data),
  update: (id: number, data: any) => api.put(`/sales/${id}`, data),
  updateByOrderNo: (orderNo: string, data: any) => api.put(`/sales/order/${orderNo}`, data),
  delete: (id: number) => api.delete(`/sales/${id}`),
  deleteByOrderNo: (orderNo: string) => api.delete(`/sales/order/${orderNo}`),
  submit: (id: number, operator: string) => api.post(`/sales/${id}/submit`, null, { params: { operator } }),
  submitByOrderNo: (orderNo: string, operator: string) => api.post(`/sales/order/${orderNo}/submit`, null, { params: { operator } }),
  approve: (id: number, approver: string) => api.post(`/sales/${id}/approve`, null, { params: { approver } }),
  approveByOrderNo: (orderNo: string, approver: string) => api.post(`/sales/order/${orderNo}/approve`, null, { params: { approver } }),
  reject: (id: number, approver: string, reason: string) => 
    api.post(`/sales/${id}/reject`, null, { params: { approver, reason } }),
  rejectByOrderNo: (orderNo: string, approver: string, reason: string) => 
    api.post(`/sales/order/${orderNo}/reject`, null, { params: { approver, reason } }),
  withdraw: (id: number, operator: string) => api.post(`/sales/${id}/withdraw`, null, { params: { operator } }),
  withdrawByOrderNo: (orderNo: string, operator: string) => api.post(`/sales/order/${orderNo}/withdraw`, null, { params: { operator } }),
  getTotalSales: (salesmanId: number) => api.get(`/sales/stats/total-sales/${salesmanId}`),
  getTotalCommission: (salesmanId: number) => api.get(`/sales/stats/total-commission/${salesmanId}`)
}

// ====================联络记录接口 ====================
export const contactRecordApi = {
  getAll: (role?: string, salesmanId?: number) => {
    const params: any = {}
    if (role) params.role = role
    if (salesmanId) params.salesmanId = salesmanId
    return api.get('/contacts', { params })
  },
  getById: (id: number) => api.get(`/contacts/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/contacts/salesman/${salesmanId}`),
  getByCustomer: (customerId: number) => api.get(`/contacts/customer/${customerId}`),
  create: (data: any, role?: string) => {
    console.log('[ContactRecord CREATE] Sending data:', JSON.stringify(data, null, 2))
    const params: any = {}
    if (role) params.role = role
    return api.post('/contacts', data, { params })
  },
  update: (id: number, data: any) => {
    console.log('[ContactRecord UPDATE] Sending data:', JSON.stringify(data, null, 2))
    return api.put(`/contacts/${id}`, data)
  },
  delete: (id: number) => api.delete(`/contacts/${id}`)
}

// ==================== 服务记录接口 ====================
export const serviceRecordApi = {
  getAll: (role?: string, salesmanId?: number) => {
    const params: any = {}
    if (role) params.role = role
    if (salesmanId) params.salesmanId = salesmanId
    return api.get('/services', { params })
  },
  getById: (id: number) => api.get(`/services/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/services/salesman/${salesmanId}`),
  getByCustomer: (customerId: number) => api.get(`/services/customer/${customerId}`),
  getAverageSatisfaction: (salesmanId: number) => api.get(`/services/satisfaction/${salesmanId}`),
  create: (data: any) => api.post('/services', data),
  update: (id: number, data: any) => api.put(`/services/${id}`, data),
  processService: (id: number, solution: string) => api.put(`/services/${id}/process`, { solution }),
  rateService: (id: number, satisfaction: number) => api.put(`/services/${id}/rate`, { satisfaction }),
  delete: (id: number) => api.delete(`/services/${id}`)
}

// ==================== 催款记录接口 ====================
export const collectionRecordApi = {
  getAll: (role?: string, salesmanId?: number) => {
    const params: any = {}
    if (role) params.role = role
    if (salesmanId) params.salesmanId = salesmanId
    return api.get('/collections', { params })
  },
  getById: (id: number) => api.get(`/collections/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/collections/salesman/${salesmanId}`),
  getByStatus: (status: string) => api.get(`/collections/status/${status}`),
  getCollectionRate: (salesmanId: number) => api.get(`/collections/rate/${salesmanId}`),
  create: (data: any, role?: string) => {
    console.log('[CollectionRecord CREATE] Sending data:', JSON.stringify(data, null, 2))
    const params: any = {}
    if (role) params.role = role
    return api.post('/collections', data, { params })
  },
  update: (id: number, data: any) => {
    console.log('[CollectionRecord UPDATE] Sending data:', JSON.stringify(data, null, 2))
    return api.put(`/collections/${id}`, data)
  },
  delete: (id: number) => api.delete(`/collections/${id}`)
}

// ==================== 投诉记录接口 ====================
export const complaintRecordApi = {
  getAll: (role?: string, salesmanId?: number) => {
    const params: any = {}
    if (role) params.role = role
    if (salesmanId) params.salesmanId = salesmanId
    return api.get('/complaints', { params })
  },
  getById: (id: number) => api.get(`/complaints/${id}`),
  getBySalesman: (salesmanId: number) => api.get(`/complaints/salesman/${salesmanId}`),
  getByStatus: (status: string) => api.get(`/complaints/status/${status}`),
  getBySeverity: (severity: string) => api.get(`/complaints/severity/${severity}`),
  getByType: (type: string) => api.get(`/complaints/type/${type}`),
  getStatistics: (salesmanId?: number) => {
    const params = salesmanId ? `?salesmanId=${salesmanId}` : '';
    return api.get(`/complaints/statistics${params}`);
  },
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

// ==================== 审计日志接口 ====================
export const auditLogApi = {
  getAll: () => api.get('/audit-logs'),
  getEntityLogs: (entityName: string, entityId: number) => api.get(`/audit-logs/entity/${entityName}/${entityId}`),
  getEntityTypeLogs: (entityName: string) => api.get(`/audit-logs/entity-type/${entityName}`),
  getOperatorLogs: (operator: string) => api.get(`/audit-logs/operator/${operator}`)
}

export default api
