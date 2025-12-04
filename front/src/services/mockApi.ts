// 简单的前端内存 mock 服务，用于模拟后端 CRUD 操作

type Salesman = {
  id: string;
  name: string;
  contact?: string;
  hireDate?: string;
  baseCommission?: number;
};

type SalesRecord = {
  id: string;
  salesmanId: string;
  productId: string;
  quantity: number;
  unitPrice: number;
  amount: number;
  commissionRate?: number;
  commissionAmount?: number;
  saleTime: string;
  customerName?: string;
  customerContact?: string;
};

// 初始静态数据
const salesmen: Salesman[] = [
  { id: 'S00001', name: '张三', contact: '13800000001', hireDate: '2022-03-01', baseCommission: 5.0 },
  { id: 'S00002', name: '李四', contact: '13800000002', hireDate: '2023-01-15', baseCommission: 4.5 },
  { id: 'S00003', name: '王五', contact: '13800000003', hireDate: '2021-09-10', baseCommission: 6.0 },
];

const sales: SalesRecord[] = [
  {
    id: 'SR0000001',
    salesmanId: 'S00001',
    productId: 'P001',
    quantity: 10,
    unitPrice: 120.0,
    amount: 1200.0,
    commissionRate: 5.0,
    commissionAmount: 60.0,
    saleTime: '2025-11-01 09:30:00',
    customerName: '某医院A',
    customerContact: '010-88880001',
  },
  {
    id: 'SR0000002',
    salesmanId: 'S00002',
    productId: 'P002',
    quantity: 5,
    unitPrice: 300.0,
    amount: 1500.0,
    commissionRate: 4.5,
    commissionAmount: 67.5,
    saleTime: '2025-11-05 14:10:00',
    customerName: '某诊所B',
    customerContact: '13911112222',
  },
];

// 简单延迟模拟
const delay = (ms = 300) => new Promise((res) => setTimeout(res, ms));

// 固定账号配置
const accounts = [
  { username: 'admin', password: '123', role: 'admin', name: '管理员', id: 'ADMIN' },
  { username: 'worker', password: '123', role: 'salesman', name: '销售员', id: 'S00001' },
];

export const mockApi = {
  async login(username: string, password: string) {
    await delay();
    if (!username || !password) return { success: false, message: '用户名或密码不能为空' };
    
    // 查找匹配的账号
    const account = accounts.find((acc) => acc.username === username && acc.password === password);
    
    if (!account) {
      return { success: false, message: '用户名或密码错误' };
    }
    
    const token = 'mock-token-' + Math.random().toString(36).slice(2, 10);
    return { 
      success: true, 
      token, 
      salesmanId: account.id,
      role: account.role,
      name: account.name
    };
  },
  async listSalesmen(keyword = '') {
    await delay();
    if (!keyword) return salesmen.slice();
    const k = keyword.toLowerCase();
    return salesmen.filter((s) => s.id.toLowerCase().includes(k) || s.name.toLowerCase().includes(k));
  },
  async getSalesman(id: string) {
    await delay();
    return salesmen.find((s) => s.id === id) || null;
  },
  async createSalesman(payload: Omit<Salesman, 'id'>) {
    await delay();
    const id = 'S' + (10000 + salesmen.length + 1).toString().slice(1, 6);
    const item: Salesman = { id, ...payload };
    salesmen.push(item);
    return item;
  },
  async updateSalesman(id: string, payload: Partial<Salesman>) {
    await delay();
    const idx = salesmen.findIndex((s) => s.id === id);
    if (idx === -1) throw new Error('Not found');
    salesmen[idx] = { ...salesmen[idx], ...payload };
    return salesmen[idx];
  },
  async deleteSalesman(id: string) {
    await delay();
    const idx = salesmen.findIndex((s) => s.id === id);
    if (idx === -1) throw new Error('Not found');
    salesmen.splice(idx, 1);
    return true;
  },

  // sales records
  async listSalesRecords(filter: { salesmanId?: string; from?: string; to?: string } = {}) {
    await delay();
    let list = sales.slice();
    if (filter.salesmanId) list = list.filter((r) => r.salesmanId === filter.salesmanId);
    if (filter.from) list = list.filter((r) => r.saleTime >= filter.from);
    if (filter.to) list = list.filter((r) => r.saleTime <= filter.to);
    return list;
  },
  async createSalesRecord(payload: Omit<SalesRecord, 'id' | 'amount' | 'commissionAmount'> & { commissionRate?: number }) {
    await delay();
    const id = 'SR' + (sales.length + 1).toString().padStart(7, '0');
    const amount = payload.quantity * payload.unitPrice;
    const commissionRate = payload.commissionRate ?? (salesmen.find((s) => s.id === payload.salesmanId)?.baseCommission ?? 0);
    const commissionAmount = +(amount * (commissionRate / 100)).toFixed(2);
    const record: SalesRecord = { id, ...payload, amount, commissionRate, commissionAmount } as any;
    sales.push(record);
    return record;
  },
  async updateSalesRecord(id: string, payload: Partial<SalesRecord>) {
    await delay();
    const idx = sales.findIndex((s) => s.id === id);
    if (idx === -1) throw new Error('Not found');
    const updated = { ...sales[idx], ...payload } as SalesRecord;
    updated.amount = updated.quantity * updated.unitPrice;
    const commissionRate = updated.commissionRate ?? (salesmen.find((s) => s.id === updated.salesmanId)?.baseCommission ?? 0);
    updated.commissionAmount = +(updated.amount * (commissionRate / 100)).toFixed(2);
    sales[idx] = updated;
    return updated;
  },
  async deleteSalesRecord(id: string) {
    await delay();
    const idx = sales.findIndex((s) => s.id === id);
    if (idx === -1) throw new Error('Not found');
    sales.splice(idx, 1);
    return true;
  },
};
