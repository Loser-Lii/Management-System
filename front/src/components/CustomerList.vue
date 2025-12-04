<template>
  <div class="page">
    <div class="toolbar">
      <input v-model="keyword" placeholder="按客户名称或联系人搜索" @input="load" />
      <select v-model="filterType" @change="load" class="filter-select">
        <option value="">所有类型</option>
        <option value="individual">个人</option>
        <option value="enterprise">企业</option>
      </select>
      <select v-model="filterLevel" @change="load" class="filter-select">
        <option value="">所有等级</option>
        <option value="A">A级</option>
        <option value="B">B级</option>
        <option value="C">C级</option>
      </select>
      <!-- 管理员可以按销售员筛选 -->
      <select v-if="isAdmin" v-model="filterSalesmanId" @change="load" class="filter-select">
        <option value="">所有销售员</option>
        <option v-for="s in salesmanList" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      <button @click="openNew">新增客户</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>客户编号</th>
          <th>客户名称</th>
          <th>联系人</th>
          <th>负责销售员</th>
          <th>联系电话</th>
          <th>邮箱</th>
          <th>地址</th>
          <th>客户类型</th>
          <th>客户等级</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in list" :key="c.id">
          <td>{{ c.id }}</td>
          <td>{{ c.name }}</td>
          <td>{{ c.contactPerson }}</td>
          <td>{{ c.salesmanName || '未分配' }}</td>
          <td>{{ c.phone }}</td>
          <td>{{ c.email }}</td>
          <td>{{ c.address }}</td>
          <td>
            <span class="badge" :class="c.customerType === 'enterprise' ? 'info' : 'gray'">
              {{ c.customerType === 'individual' ? '个人' : '企业' }}
            </span>
          </td>
          <td>
            <span class="badge" :class="{
              'success': c.level === 'A',
              'warning': c.level === 'B',
              'gray': c.level === 'C'
            }">
              {{ c.level }}级
            </span>
          </td>
          <td>
            <button @click="edit(c)">编辑</button>
            <button @click="remove(c)" class="danger">删除</button>
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
        <h3>{{ editing?.id ? '编辑客户' : '新增客户' }}</h3>
        <div class="form-row"><label>客户名称</label><input v-model="form.name" /></div>
        <div class="form-row"><label>联系人</label><input v-model="form.contactPerson" /></div>
        <div class="form-row"><label>联系电话</label><input v-model="form.phone" /></div>
        <div class="form-row"><label>邮箱</label><input v-model="form.email" /></div>
        <div class="form-row"><label>地址</label><input v-model="form.address" /></div>
        <div class="form-row">
          <label>客户类型</label>
          <select v-model="form.customerType">
            <option value="individual">个人</option>
            <option value="enterprise">企业</option>
          </select>
        </div>
        <div class="form-row">
          <label>客户等级</label>
          <select v-model="form.level">
            <option value="A">A级</option>
            <option value="B">B级</option>
            <option value="C">C级</option>
          </select>
        </div>
        <div class="form-row" v-if="isAdmin">
          <label>负责销售员</label>
          <select v-model="form.salesmanId">
            <option :value="null">未分配</option>
            <option v-for="s in salesmanList" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-row"><label>备注</label><textarea v-model="form.remark" rows="3"></textarea></div>
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
import { customerApi, salesmanApi } from '../services/api';
import Pagination from './Pagination.vue';
const props = defineProps<{
  salesmanId?: number
}>();

const userRole = ref(localStorage.getItem('ms_role') || 'salesman');
const currentSalesmanId = ref(Number(localStorage.getItem('ms_salesmanId')) || props.salesmanId);
const isAdmin = computed(() => userRole.value === 'admin');

interface Customer {
  id?: number
  name: string
  contactPerson: string
  phone: string
  email: string
  address: string
  customerType: string
  level: string
  salesmanId?: number
  salesmanName?: string
  remark?: string
}

const list = ref<Customer[]>([]);
const allData = ref<Customer[]>([]); // 存储所有数据
const keyword = ref('');
const filterType = ref('');
const filterLevel = ref('');
const filterSalesmanId = ref<number | ''>(''); // 管理员用于筛选销售员
const salesmanList = ref<any[]>([]); // 管理员用的销售员列表
const showForm = ref(false);
const editing = ref<Customer | null>(null);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});

const form = reactive({
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  customerType: 'individual',
  level: 'B',
  salesmanId: null as number | null,
  remark: ''
});

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

async function load() {
  try {
    // 销售员只能看到自己的客户
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filterSalesmanId.value || undefined) // 管理员可以选择性过滤
      : currentSalesmanId.value; // 销售员只看自己的
    
    const res = await customerApi.getAllWithSalesman(role, salesmanId, filterLevel.value || undefined);
    
    if (res.code === 200) {
      let data = res.data || [];
      if (filterType.value) {
        data = data.filter((c: Customer) => c.customerType === filterType.value);
      }
      if (keyword.value.trim()) {
        const kw = keyword.value.toLowerCase();
        data = data.filter((c: Customer) => 
          c.name.toLowerCase().includes(kw) || 
          c.contactPerson?.toLowerCase().includes(kw)
        );
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载客户列表失败:', err);
  }
}

// 加载销售员列表（管理员用）
async function loadSalesmen() {
  if (!isAdmin.value) return;
  try {
    const res = await salesmanApi.getAll();
    if (res.code === 200) {
      salesmanList.value = res.data || [];
    }
  } catch (err) {
    console.error('加载销售员列表失败:', err);
  }
}

function openNew() {
  editing.value = null;
  form.name = '';
  form.contactPerson = '';
  form.phone = '';
  form.email = '';
  form.address = '';
  form.customerType = 'individual';
  form.level = 'B';
  form.salesmanId = null;
  form.remark = '';
  showForm.value = true;
}

function edit(item: Customer) {
  editing.value = item;
  form.name = item.name;
  form.contactPerson = item.contactPerson;
  form.phone = item.phone;
  form.email = item.email;
  form.address = item.address;
  form.customerType = item.customerType;
  form.level = item.level;
  form.salesmanId = item.salesmanId || null;
  form.remark = item.remark || '';
  showForm.value = true;
}

async function save() {
  if (!form.name.trim()) { alert('请输入客户名称'); return; }
  if (!form.contactPerson.trim()) { alert('请输入联系人'); return; }

  try {
    const data = { ...form } as any;
    // 销售员添加客户时自动关联自己
    if (!isAdmin.value && currentSalesmanId.value) {
      data.salesmanId = currentSalesmanId.value;
    }
    
    if (editing.value?.id) {
      await customerApi.update(editing.value.id, data);
    } else {
      await customerApi.create(data);
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

async function remove(item: Customer) {
  if (!confirm('确认删除客户 ' + item.name + ' ?')) return;
  try {
    if (item.id) {
      await customerApi.delete(item.id);
      await load();
    }
  } catch (err: any) {
    alert('删除失败：' + (err?.response?.data?.message || err?.message));
  }
}

onMounted(() => {
  loadSalesmen();
  load();
});
</script>

<style scoped>
@import '../assets/table-styles.css';
</style>
