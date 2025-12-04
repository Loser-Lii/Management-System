<template>
  <div class="page">
    <div class="toolbar">
      <select v-if="isAdmin" v-model="filter.salesmanId" @change="load" class="filter-select">
        <option value="">所有销售员</option>
        <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      <select v-model="filter.customerId" @change="load" class="filter-select">
        <option value="">所有客户</option>
        <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <select v-model="filter.status" @change="load" class="filter-select">
        <option value="">所有状态</option>
        <option value="pending">未收款</option>
        <option value="partial">部分收款</option>
        <option value="completed">已完成</option>
      </select>
      <button @click="openNew">新增催款记录</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>记录编号</th>
          <th>销售员</th>
          <th>客户</th>
          <th>催款日期</th>
          <th>应收金额</th>
          <th>实收金额</th>
          <th>收款方式</th>
          <th>截止日期</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ r.salesman?.name || getSalesmanName(r.salesmanId) }}</td>
          <td>{{ r.customer?.name || getCustomerName(r.customerId) }}</td>
          <td>{{ r.collectionDate }}</td>
          <td>¥{{ Number(r.amountDue || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</td>
          <td>¥{{ Number(r.amountReceived || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</td>
          <td>{{ r.collectionMethod }}</td>
          <td>{{ r.dueDate }}</td>
          <td>
            <span class="badge" :class="{
              'success': r.status === 'completed',
              'warning': r.status === 'partial',
              'danger': r.status === 'pending'
            }">
              {{ getStatusText(r.status) }}
            </span>
          </td>
          <td>
            <button @click="edit(r)">编辑</button>
            <button @click="remove(r)" class="danger">删除</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="10" class="empty">无数据</td>
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
        <h3>{{ editing?.id ? '编辑催款记录' : '新增催款记录' }}</h3>
        <div v-if="isAdmin" class="form-row">
          <label>销售员</label>
          <select v-model.number="form.salesmanId">
            <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>客户</label>
          <select v-model.number="form.customerId">
            <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>关联销售记录</label>
          <select v-model.number="form.salesRecordId">
            <option :value="0">无关联</option>
            <option v-for="sr in salesRecords" :key="sr.id" :value="sr.id">
              销售记录 #{{ sr.id }} - ¥{{ sr.totalAmount }}
            </option>
          </select>
        </div>
        <div class="form-row"><label>催款日期</label><input type="date" v-model="form.collectionDate" /></div>
        <div class="form-row"><label>应收金额</label><input type="number" step="0.01" v-model.number="form.amountDue" /></div>
        <div class="form-row"><label>实收金额</label><input type="number" step="0.01" v-model.number="form.amountReceived" /></div>
        <div class="form-row">
          <label>收款方式</label>
          <select v-model="form.collectionMethod">
            <option value="现金">现金</option>
            <option value="银行转账">银行转账</option>
            <option value="支票">支票</option>
            <option value="微信">微信</option>
            <option value="支付宝">支付宝</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <div class="form-row"><label>截止日期</label><input type="date" v-model="form.dueDate" /></div>
        <div class="form-row">
          <label>状态</label>
          <select v-model="form.status">
            <option value="pending">未收款</option>
            <option value="partial">部分收款</option>
            <option value="completed">已完成</option>
          </select>
        </div>
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
import { salesmanApi, customerApi, salesRecordApi, collectionRecordApi } from '../services/api';
import Pagination from './Pagination.vue';

const props = defineProps<{
  salesmanId?: number
}>();

const userRole = ref(localStorage.getItem('ms_role') || 'salesman');
const currentSalesmanId = ref(Number(localStorage.getItem('ms_salesmanId')) || props.salesmanId);
const isAdmin = computed(() => userRole.value === 'admin');

const salesmen = ref<any[]>([]);
const customers = ref<any[]>([]);
const salesRecords = ref<any[]>([]);
const list = ref<any[]>([]);
const allData = ref<any[]>([]); // 存储所有数据
const filter = reactive({ salesmanId: '', customerId: '', status: '' });

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});

const showForm = ref(false);
const editing = ref<any>(null);
const form = reactive({
  salesmanId: 0,
  customerId: 0,
  salesRecordId: 0,
  collectionDate: '',
  amountDue: 0,
  amountReceived: 0,
  collectionMethod: '银行转账',
  dueDate: '',
  status: 'pending'
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

async function loadSalesRecords() {
  const role = userRole.value;
  const salesmanId = isAdmin.value ? undefined : currentSalesmanId.value;
  const res = await salesRecordApi.getAll(role, salesmanId);
  if (res.code === 200) salesRecords.value = res.data || [];
}

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

async function load() {
  try {
    // 销售员只能看到自己的催款记录
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filter.salesmanId ? Number(filter.salesmanId) : undefined) // 管理员可以选择性过滤
      : currentSalesmanId.value; // 销售员只看自己的
    
    const res = await collectionRecordApi.getAll(role, salesmanId);
    if (res.code === 200) {
      let data = res.data || [];
      // 客户端筛选客户和状态
      if (filter.customerId) {
        data = data.filter((r: any) => r.customerId === Number(filter.customerId));
      }
      if (filter.status) {
        data = data.filter((r: any) => r.status === filter.status);
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载催款记录失败:', err);
  }
}

function getSalesmanName(id: number) {
  return salesmen.value.find(x => x.id === id)?.name || id.toString();
}

function getCustomerName(id: number) {
  return customers.value.find(x => x.id === id)?.name || id.toString();
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    'pending': '未收款',
    'partial': '部分收款',
    'completed': '已完成'
  };
  return map[status] || status;
}

function openNew() {
  editing.value = null;
  form.salesmanId = !isAdmin.value && props.salesmanId ? props.salesmanId : (salesmen.value[0]?.id || 0);
  form.customerId = customers.value[0]?.id || 0;
  form.salesRecordId = 0;
  const today = new Date().toISOString().split('T')[0] || '';
  form.collectionDate = today;
  form.amountDue = 0;
  form.amountReceived = 0;
  form.collectionMethod = '银行转账';
  form.dueDate = today;
  form.status = 'pending';
  showForm.value = true;
}

function edit(item: any) {
  editing.value = item;
  form.salesmanId = item.salesmanId;
  form.customerId = item.customerId;
  form.salesRecordId = item.salesRecordId || 0;
  form.collectionDate = item.collectionDate;
  form.amountDue = item.amountDue;
  form.amountReceived = item.amountReceived;
  form.collectionMethod = item.collectionMethod;
  form.dueDate = item.dueDate;
  form.status = item.status;
  showForm.value = true;
}

async function save() {
  const salesmanId = !isAdmin.value && props.salesmanId ? props.salesmanId : form.salesmanId;
  if (!salesmanId) { alert('请选择销售员'); return; }
  if (!form.customerId) { alert('请选择客户'); return; }

  try {
    const payload: any = { ...form, salesmanId };
    if (!payload.salesRecordId) payload.salesRecordId = null;
    
    if (editing.value?.id) {
      await collectionRecordApi.update(editing.value.id, payload);
    } else {
      await collectionRecordApi.create(payload);
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

async function remove(item: any) {
  if (!confirm('确认删除催款记录 ' + item.id + ' ?')) return;
  try {
    if (item.id) {
      await collectionRecordApi.delete(item.id);
      await load();
    }
  } catch (err: any) {
    alert('删除失败：' + (err?.response?.data?.message || err?.message));
  }
}

onMounted(async () => {
  await Promise.all([loadSalesmen(), loadCustomers(), loadSalesRecords()]);
  await load();
});
</script>

<style scoped>
@import '../assets/table-styles.css';
</style>
