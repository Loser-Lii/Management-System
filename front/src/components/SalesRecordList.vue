<template>
  <div class="page">
    <div class="toolbar">
      <select v-if="isAdmin" v-model="filter.salesmanId" @change="load" class="filter-select">
        <option value="">所有销售员</option>
        <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }} ({{ s.employeeNo }})</option>
      </select>
      <select v-model="filter.customerId" @change="load" class="filter-select">
        <option value="">所有客户</option>
        <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <div class="date-group">
        <label>起始日期</label>
        <input type="date" v-model="filter.from" @change="load" class="date-input" :max="filter.to" />
      </div>
      <div class="date-group">
        <label>截止日期</label>
        <input type="date" v-model="filter.to" @change="load" class="date-input" :min="filter.from" />
      </div>
      <button @click="openNew">新增销售记录</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>销售编号</th>
          <th>销售员</th>
          <th>客户</th>
          <th>产品</th>
          <th>数量</th>
          <th>单价</th>
          <th>总金额</th>
          <th>提成率(%)</th>
          <th>提成金额</th>
          <th>销售日期</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ r.salesman?.name || getSalesmanName(r.salesmanId) }}</td>
          <td>{{ r.customer?.name || getCustomerName(r.customerId) }}</td>
          <td>{{ r.product?.name || getProductName(r.productId) }}</td>
          <td>{{ r.quantity }}</td>
          <td>¥{{ Number(r.unitPrice || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</td>
          <td>¥{{ Number(r.totalAmount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</td>
          <td>{{ (Number(r.commissionRate || 0) * 100).toFixed(2) }}%</td>
          <td>¥{{ Number(r.commissionAmount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</td>
          <td>{{ r.saleDate }}</td>
          <td>
            <button @click="edit(r)">编辑</button>
            <button @click="remove(r)" class="danger">删除</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="11" class="empty">无数据</td>
        </tr>
      </tbody>
    </table>

    <Pagination 
      :total="totalRecords" 
      :pageSize="pageSize" 
      :currentPage="currentPage"
      @update:currentPage="currentPage = $event"
    />

    <div v-if="showForm" class="modal">
      <div class="modal-card">
        <h3>{{ editing?.id ? '编辑销售记录' : '新增销售记录' }}</h3>
        <div v-if="isAdmin" class="form-row">
          <label>销售员</label>
          <select v-model.number="form.salesmanId">
            <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }} ({{ s.employeeNo }})</option>
          </select>
        </div>
        <div class="form-row">
          <label>客户</label>
          <select v-model.number="form.customerId">
            <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>产品</label>
          <select v-model.number="form.productId">
            <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }} ({{ p.productNo }})</option>
          </select>
        </div>
        <div class="form-row"><label>数量</label><input type="number" v-model.number="form.quantity" /></div>
        <div class="form-row"><label>单价</label><input type="number" step="0.01" v-model.number="form.unitPrice" /></div>
        <div class="form-row"><label>提成率(%)</label><input type="number" step="0.01" v-model.number="form.commissionRate" /></div>
        <div class="form-row"><label>销售日期</label><input type="date" v-model="form.saleDate" /></div>

        <div class="modal-actions">
          <button @click="save">保存</button>
          <button @click="closeForm" class="muted">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { salesmanApi, customerApi, productApi, salesRecordApi } from '../services/api';
import Pagination from './Pagination.vue';

const props = defineProps<{
  salesmanId?: number
}>();

const userRole = ref(localStorage.getItem('ms_role') || 'salesman');
const currentSalesmanId = ref(Number(localStorage.getItem('ms_salesmanId')) || props.salesmanId);
const isAdmin = computed(() => userRole.value === 'admin');

interface Salesman {
  id: number
  name: string
  employeeNo: string
}

interface Customer {
  id: number
  name: string
}

interface Product {
  id: number
  name: string
  productNo: string
}

interface SalesRecord {
  id?: number
  salesmanId: number
  customerId: number
  productId: number
  quantity: number
  unitPrice: number
  totalAmount?: number
  commissionRate?: number
  commissionAmount?: number
  saleDate: string
  salesman?: { name: string }
  customer?: { name: string }
  product?: { name: string }
}

const salesmen = ref<Salesman[]>([]);
const customers = ref<Customer[]>([]);
const products = ref<Product[]>([]);
const list = ref<SalesRecord[]>([]);
const allData = ref<SalesRecord[]>([]); // 存储所有数据
const filter = reactive({ salesmanId: '', customerId: '', from: '', to: '' });

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});

const showForm = ref(false);
const editing = ref<SalesRecord | null>(null);
const form = reactive({ 
  salesmanId: 0, 
  customerId: 0,
  productId: 0, 
  quantity: 1, 
  unitPrice: 0, 
  commissionRate: 0, 
  saleDate: '' 
});

async function loadSalesmen() { 
  const res = await salesmanApi.getAll(); 
  if (res.code === 200) salesmen.value = res.data || [];
}

async function loadCustomers() {
  const role = userRole.value;
  const salesmanId = isAdmin.value ? undefined : currentSalesmanId.value;
  const res = await customerApi.getAll(role, salesmanId);
  if (res.code === 200) customers.value = res.data || [];
}

async function loadProducts() {
  const res = await productApi.getAll();
  if (res.code === 200) products.value = res.data || [];
}

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

async function load() { 
  try {
    // 销售员只能看到自己的销售记录
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filter.salesmanId ? Number(filter.salesmanId) : undefined) // 管理员可以选择性过滤
      : currentSalesmanId.value; // 销售员只看自己的
    
    const res = await salesRecordApi.getAll(role, salesmanId);
    if (res.code === 200) {
      let data = res.data || [];
      // 客户端筛选
      if (filter.customerId) {
        data = data.filter(r => r.customerId === Number(filter.customerId));
      }
      if (filter.from) {
        data = data.filter(r => r.saleDate >= filter.from);
      }
      if (filter.to) {
        data = data.filter(r => r.saleDate <= filter.to);
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载销售记录失败:', err);
  }
}

function getSalesmanName(id: number) { 
  const s = salesmen.value.find((x) => x.id === id); 
  return s ? s.name : id.toString(); 
}

function getCustomerName(id: number) {
  const c = customers.value.find(x => x.id === id);
  return c ? c.name : id.toString();
}

function getProductName(id: number) {
  const p = products.value.find(x => x.id === id);
  return p ? p.name : id.toString();
}

function openNew() {
  editing.value = null;
  // 销售员自动使用当前登录用户，管理员选择第一个
  form.salesmanId = !isAdmin.value && props.salesmanId ? props.salesmanId : (salesmen.value[0]?.id || 0);
  form.customerId = customers.value[0]?.id || 0;
  form.productId = products.value[0]?.id || 0;
  form.quantity = 1;
  form.unitPrice = 0;
  form.commissionRate = 0;
  form.saleDate = new Date().toISOString().split('T')[0];
  showForm.value = true;
}

function edit(item: SalesRecord) {
  editing.value = item;
  form.salesmanId = item.salesmanId;
  form.customerId = item.customerId;
  form.productId = item.productId;
  form.quantity = item.quantity;
  form.unitPrice = item.unitPrice;
  form.commissionRate = item.commissionRate || 0;
  form.saleDate = item.saleDate;
  showForm.value = true;
}

async function save() {
  // 销售员自动使用当前登录用户ID
  const salesmanId = !isAdmin.value && props.salesmanId ? props.salesmanId : form.salesmanId;
  if (!salesmanId) { alert('请选择销售员'); return; }
  if (!form.customerId) { alert('请选择客户'); return; }
  if (!form.productId) { alert('请选择产品'); return; }
  
  try {
    const payload = {
      salesmanId,
      customerId: form.customerId,
      productId: form.productId,
      quantity: form.quantity,
      unitPrice: form.unitPrice,
      commissionRate: form.commissionRate,
      saleDate: form.saleDate,
    };
    
    if (editing.value?.id) {
      await salesRecordApi.update(editing.value.id, payload);
    } else {
      await salesRecordApi.create(payload);
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

async function remove(item: SalesRecord) {
  if (!confirm('确认删除记录 ' + item.id + ' ?')) return;
  try {
    if (item.id) {
      await salesRecordApi.delete(item.id);
      await load();
    }
  } catch (err: any) {
    alert('删除失败：' + (err?.response?.data?.message || err?.message));
  }
}

onMounted(async () => {
  await Promise.all([loadSalesmen(), loadCustomers(), loadProducts()]);
  await load();
});
</script>

<style scoped>
@import '../assets/table-styles.css';
</style>
