<template>
  <div class="page">
    <div class="toolbar">
      <select v-model="filter.status" @change="load" class="filter-select">
        <option value="">所有状态</option>
        <option value="pending">未处理</option>
        <option value="processing">处理中</option>
        <option value="resolved">已处理</option>
      </select>
      <select v-if="isAdmin" v-model="filter.salesmanId" @change="load" class="filter-select">
        <option value="">所有销售员</option>
        <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      <select v-model="filter.customerId" @change="load" class="filter-select">
        <option value="">所有客户</option>
        <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>投诉编号</th>
          <th>客户</th>
          <th>销售员</th>
          <th>投诉日期</th>
          <th>投诉类型</th>
          <th>投诉内容</th>
          <th>处理人</th>
          <th>状态</th>
          <th>处理结果</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ r.customer?.name || getCustomerName(r.customerId) }}</td>
          <td>{{ r.salesman?.name || getSalesmanName(r.salesmanId) }}</td>
          <td>{{ r.complaintDate }}</td>
          <td>{{ r.complaintType }}</td>
          <td class="content-cell">{{ r.content }}</td>
          <td>{{ r.handler || '-' }}</td>
          <td>
            <span class="badge" :class="{
              'success': r.status === 'resolved',
              'warning': r.status === 'processing',
              'danger': r.status === 'pending'
            }">
              {{ getStatusText(r.status) }}
            </span>
          </td>
          <td class="content-cell">{{ r.result || '-' }}</td>
          <td>
            <button @click="handleComplaint(r)" :disabled="r.status === 'resolved'">处理</button>
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
        <h3>处理投诉</h3>
        <div class="info-section">
          <div class="info-row"><label>客户：</label><span>{{ editing?.customer?.name || getCustomerName(editing?.customerId) }}</span></div>
          <div class="info-row"><label>销售员：</label><span>{{ editing?.salesman?.name || getSalesmanName(editing?.salesmanId) }}</span></div>
          <div class="info-row"><label>投诉日期：</label><span>{{ editing?.complaintDate }}</span></div>
          <div class="info-row"><label>投诉类型：</label><span>{{ editing?.complaintType }}</span></div>
          <div class="info-row"><label>投诉内容：</label><span>{{ editing?.content }}</span></div>
        </div>
        <div class="form-row"><label>处理人</label><input v-model="form.handler" placeholder="处理此投诉的负责人" /></div>
        <div class="form-row">
          <label>状态</label>
          <select v-model="form.status">
            <option value="processing">处理中</option>
            <option value="resolved">已处理</option>
          </select>
        </div>
        <div class="form-row">
          <label>处理结果</label>
          <textarea v-model="form.result" rows="4" placeholder="记录处理结果和采取的措施" required></textarea>
        </div>
        <div class="form-row"><label>处理日期</label><input type="date" v-model="form.handleDate" /></div>
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
import { salesmanApi, customerApi, complaintRecordApi } from '../services/api';
import Pagination from './Pagination.vue';

const props = defineProps<{
  salesmanId?: number
}>();

const userRole = ref(localStorage.getItem('ms_role') || 'salesman');
const currentSalesmanId = ref(Number(localStorage.getItem('ms_salesmanId')) || props.salesmanId);
const isAdmin = computed(() => userRole.value === 'admin');

const salesmen = ref<any[]>([]);
const customers = ref<any[]>([]);
const list = ref<any[]>([]);
const allData = ref<any[]>([]); // 存储所有数据
const filter = reactive({ status: '', salesmanId: '', customerId: '' });

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
  customerId: 0,
  salesmanId: 0,
  complaintDate: '',
  complaintType: '产品质量',
  content: '',
  handler: '',
  status: 'pending',
  result: '',
  handleDate: ''
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
    // 销售员只能看到自己的投诉记录
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filter.salesmanId ? Number(filter.salesmanId) : undefined) // 管理员可以选择性过滤
      : currentSalesmanId.value; // 销售员只看自己的
    
    const res = await complaintRecordApi.getAll(role, salesmanId);
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
    console.error('加载投诉记录失败:', err);
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
    'pending': '未处理',
    'processing': '处理中',
    'resolved': '已处理'
  };
  return map[status] || status;
}

function handleComplaint(item: any) {
  if (item.status === 'resolved') {
    alert('该投诉已处理完成');
    return;
  }
  editing.value = item;
  const today = new Date().toISOString().split('T')[0] || '';
  form.customerId = item.customerId;
  form.salesmanId = item.salesmanId;
  form.complaintDate = item.complaintDate;
  form.complaintType = item.complaintType;
  form.content = item.content;
  form.handler = item.handler || '';
  form.status = item.status === 'pending' ? 'processing' : item.status;
  form.result = item.result || '';
  form.handleDate = item.handleDate || today;
  showForm.value = true;
}

async function save() {
  if (!form.handler.trim()) { alert('请填写处理人'); return; }
  if (!form.result.trim()) { alert('请填写处理结果'); return; }

  try {
    if (editing.value?.id) {
      await complaintRecordApi.update(editing.value.id, { 
        handler: form.handler,
        status: form.status,
        result: form.result,
        handleDate: form.handleDate
      });
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

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

.info-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row label {
  font-weight: 600;
  color: #495057;
  min-width: 90px;
}

.info-row span {
  color: #212529;
}
</style>
