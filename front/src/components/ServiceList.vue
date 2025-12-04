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
      <button @click="openNew">新增服务记录</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>记录编号</th>
          <th>销售员</th>
          <th>客户</th>
          <th>服务日期</th>
          <th>服务类型</th>
          <th>服务内容</th>
          <th>满意度</th>
          <th>客户反馈</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ r.salesman?.name || getSalesmanName(r.salesmanId) }}</td>
          <td>{{ r.customer?.name || getCustomerName(r.customerId) }}</td>
          <td>{{ r.serviceDate }}</td>
          <td>{{ r.serviceType }}</td>
          <td class="content-cell">{{ r.content }}</td>
          <td>
            <span class="badge" :class="{
              'success': r.satisfactionRating >= 4,
              'warning': r.satisfactionRating === 3,
              'danger': r.satisfactionRating < 3
            }">
              {{ r.satisfactionRating }}★
            </span>
          </td>
          <td class="content-cell">{{ r.customerFeedback }}</td>
          <td>
            <button @click="edit(r)">编辑</button>
            <button @click="remove(r)" class="danger">删除</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="9" class="empty">无数据</td>
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
        <h3>{{ editing?.id ? '编辑服务记录' : '新增服务记录' }}</h3>
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
          <label>服务日期</label>
          <input type="date" v-model="form.serviceDate" />
        </div>
        <div class="form-row">
          <label>服务类型</label>
          <select v-model="form.serviceType">
            <option value="产品安装">产品安装</option>
            <option value="产品维修">产品维修</option>
            <option value="技术培训">技术培训</option>
            <option value="售后咨询">售后咨询</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <div class="form-row">
          <label>服务内容</label>
          <textarea v-model="form.content" rows="3" placeholder="详细描述服务内容"></textarea>
        </div>
        <div class="form-row">
          <label>满意度评分</label>
          <select v-model.number="form.satisfactionRating">
            <option :value="5">5分 - 非常满意</option>
            <option :value="4">4分 - 满意</option>
            <option :value="3">3分 - 一般</option>
            <option :value="2">2分 - 不满意</option>
            <option :value="1">1分 - 非常不满意</option>
          </select>
        </div>
        <div class="form-row">
          <label>客户反馈</label>
          <textarea v-model="form.customerFeedback" rows="3" placeholder="记录客户具体反馈"></textarea>
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
import { salesmanApi, customerApi, serviceRecordApi } from '../services/api';
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

interface ServiceRecord {
  id?: number
  salesmanId: number
  customerId: number
  serviceDate: string
  serviceType: string
  content: string
  satisfactionRating: number
  customerFeedback: string
  salesman?: { name: string }
  customer?: { name: string }
}

const salesmen = ref<Salesman[]>([]);
const customers = ref<Customer[]>([]);
const list = ref<ServiceRecord[]>([]);
const allData = ref<ServiceRecord[]>([]); // 存储所有数据
const filter = reactive({ salesmanId: '', customerId: '' });

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});

const showForm = ref(false);
const editing = ref<ServiceRecord | null>(null);
const form = reactive({
  salesmanId: 0,
  customerId: 0,
  serviceDate: '',
  serviceType: '产品安装',
  content: '',
  satisfactionRating: 5,
  customerFeedback: ''
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
    // 销售员只能看到自己的服务记录
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filter.salesmanId ? Number(filter.salesmanId) : undefined) // 管理员可以选择性过滤
      : currentSalesmanId.value; // 销售员只看自己的
    
    const res = await serviceRecordApi.getAll(role, salesmanId);
    if (res.code === 200) {
      let data = res.data || [];
      if (filter.customerId) {
        data = data.filter((r: ServiceRecord) => r.customerId === Number(filter.customerId));
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载服务记录失败:', err);
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
  const today = new Date().toISOString().split('T')[0];
  form.serviceDate = today || '';
  form.serviceType = '产品安装';
  form.content = '';
  form.satisfactionRating = 5;
  form.customerFeedback = '';
  showForm.value = true;
}

function edit(item: ServiceRecord) {
  editing.value = item;
  form.salesmanId = item.salesmanId;
  form.customerId = item.customerId;
  form.serviceDate = item.serviceDate;
  form.serviceType = item.serviceType;
  form.content = item.content;
  form.satisfactionRating = item.satisfactionRating;
  form.customerFeedback = item.customerFeedback;
  showForm.value = true;
}

async function save() {
  const salesmanId = !isAdmin.value && props.salesmanId ? props.salesmanId : form.salesmanId;
  if (!salesmanId) { alert('请选择销售员'); return; }
  if (!form.customerId) { alert('请选择客户'); return; }
  if (!form.content.trim()) { alert('请填写服务内容'); return; }

  try {
    const data = { ...form, salesmanId };
    if (editing.value?.id) {
      await serviceRecordApi.update(editing.value.id, data);
    } else {
      await serviceRecordApi.create(data);
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

async function remove(item: ServiceRecord) {
  if (!confirm('确认删除服务记录 ' + item.id + ' ?')) return;
  try {
    if (item.id) {
      await serviceRecordApi.delete(item.id);
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

.content-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
