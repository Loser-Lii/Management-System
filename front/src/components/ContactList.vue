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
      <div class="date-group">
        <label>起始日期</label>
        <input type="date" v-model="filter.from" @change="load" class="date-input" :max="filter.to" />
      </div>
      <div class="date-group">
        <label>截止日期</label>
        <input type="date" v-model="filter.to" @change="load" class="date-input" :min="filter.from" />
      </div>
      <button @click="openNew">新增联络记录</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>记录编号</th>
          <th>销售员</th>
          <th>客户</th>
          <th>联络时间</th>
          <th>联络方式</th>
          <th>摘要</th>
          <th>反馈</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ r.salesman?.name || getSalesmanName(r.salesmanId) }}</td>
          <td>{{ r.customer?.name || getCustomerName(r.customerId) }}</td>
          <td>{{ r.contactTime }}</td>
          <td>{{ r.contactMethod }}</td>
          <td class="summary-cell">{{ r.summary }}</td>
          <td class="summary-cell">{{ r.feedback }}</td>
          <td>
            <button @click="edit(r)">编辑</button>
            <button @click="remove(r)" class="danger">删除</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="8" class="empty">无数据</td>
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
        <h3>{{ editing?.id ? '编辑联络记录' : '新增联络记录' }}</h3>
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
          <label>联络时间</label>
          <input type="datetime-local" v-model="form.contactTime" />
        </div>
        <div class="form-row">
          <label>联络方式</label>
          <select v-model="form.contactMethod">
            <option value="电话">电话</option>
            <option value="微信">微信</option>
            <option value="邮件">邮件</option>
            <option value="上门拜访">上门拜访</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <div class="form-row">
          <label>联络摘要</label>
          <textarea v-model="form.summary" rows="3" placeholder="记录联络内容要点"></textarea>
        </div>
        <div class="form-row">
          <label>客户反馈</label>
          <textarea v-model="form.feedback" rows="3" placeholder="记录客户反馈内容"></textarea>
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
import { salesmanApi, customerApi, contactRecordApi } from '../services/api';
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
}

interface Customer {
  id: number
  name: string
}

interface ContactRecord {
  id?: number
  salesmanId: number
  customerId: number
  contactTime: string
  contactMethod: string
  summary: string
  feedback: string
  salesman?: { name: string }
  customer?: { name: string }
}

const salesmen = ref<Salesman[]>([]);
const customers = ref<Customer[]>([]);
const list = ref<ContactRecord[]>([]);
const allData = ref<ContactRecord[]>([]); // 存储所有数据
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
const editing = ref<ContactRecord | null>(null);
const form = reactive({
  salesmanId: 0,
  customerId: 0,
  contactTime: '',
  contactMethod: '电话',
  summary: '',
  feedback: ''
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

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

async function load() {
  try {
    // 销售员只能看到自己的联络记录
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filter.salesmanId ? Number(filter.salesmanId) : undefined) // 管理员可以选择性过滤
      : currentSalesmanId.value; // 销售员只看自己的
    
    const res = await contactRecordApi.getAll(role, salesmanId);
    if (res.code === 200) {
      let data = res.data || [];
      // 客户端筛选
      if (filter.customerId) {
        data = data.filter(r => r.customerId === Number(filter.customerId));
      }
      if (filter.from) {
        data = data.filter((r: ContactRecord) => r.contactTime >= filter.from);
      }
      if (filter.to) {
        data = data.filter((r: ContactRecord) => r.contactTime <= filter.to);
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载联络记录失败:', err);
  }
}

function getSalesmanName(id: number) {
  const s = salesmen.value.find(x => x.id === id);
  return s ? s.name : id.toString();
}

function getCustomerName(id: number) {
  const c = customers.value.find(x => x.id === id);
  return c ? c.name : id.toString();
}

function openNew() {
  editing.value = null;
  form.salesmanId = !isAdmin.value && props.salesmanId ? props.salesmanId : (salesmen.value[0]?.id || 0);
  form.customerId = customers.value[0]?.id || 0;
  form.contactTime = new Date().toISOString().slice(0, 16);
  form.contactMethod = '电话';
  form.summary = '';
  form.feedback = '';
  showForm.value = true;
}

function edit(item: ContactRecord) {
  editing.value = item;
  form.salesmanId = item.salesmanId;
  form.customerId = item.customerId;
  form.contactTime = item.contactTime.slice(0, 16);
  form.contactMethod = item.contactMethod;
  form.summary = item.summary;
  form.feedback = item.feedback;
  showForm.value = true;
}

async function save() {
  const salesmanId = !isAdmin.value && props.salesmanId ? props.salesmanId : form.salesmanId;
  if (!salesmanId) { alert('请选择销售员'); return; }
  if (!form.customerId) { alert('请选择客户'); return; }
  if (!form.summary.trim()) { alert('请填写联络摘要'); return; }

  try {
    const payload = {
      ...form,
      salesmanId,
      contactTime: form.contactTime + ':00'
    };
    
    if (editing.value?.id) {
      await contactRecordApi.update(editing.value.id, payload);
    } else {
      await contactRecordApi.create(payload);
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

async function remove(item: ContactRecord) {
  if (!confirm('确认删除联络记录 ' + item.id + ' ?')) return;
  try {
    if (item.id) {
      await contactRecordApi.delete(item.id);
      await load();
    }
  } catch (err: any) {
    alert('删除失败：' + (err?.response?.data?.message || err?.message));
  }
}

onMounted(async () => {
  await Promise.all([loadSalesmen(), loadCustomers()]);
  await load();
});
</script>

<style scoped>
@import '../assets/table-styles.css';

.summary-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
