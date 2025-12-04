<template>
  <div class="page">
    <div class="toolbar">
      <input v-model="keyword" placeholder="按产品名称或编号搜索" @input="load" />
      <select v-model="filterCategory" @change="load" class="filter-select">
        <option value="">所有类别</option>
        <option value="医疗设备">医疗设备</option>
        <option value="医疗耗材">医疗耗材</option>
        <option value="医用试剂">医用试剂</option>
        <option value="其他">其他</option>
      </select>
      <button @click="openNew">新增产品</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>产品编号</th>
          <th>产品名称</th>
          <th>类别</th>
          <th>规格</th>
          <th>单位</th>
          <th>单价</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in list" :key="p.id">
          <td>{{ p.productNo }}</td>
          <td>{{ p.name }}</td>
          <td>{{ p.category }}</td>
          <td>{{ p.specification }}</td>
          <td>{{ p.unit }}</td>
          <td>¥{{ p.price.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</td>
          <td>
            <button @click="edit(p)">编辑</button>
            <button @click="remove(p)" class="danger">删除</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="7" class="empty">无数据</td>
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
        <h3>{{ editing?.id ? '编辑产品' : '新增产品' }}</h3>
        <div class="form-row"><label>产品编号</label><input v-model="form.productNo" /></div>
        <div class="form-row"><label>产品名称</label><input v-model="form.name" /></div>
        <div class="form-row">
          <label>类别</label>
          <select v-model="form.category">
            <option value="医疗设备">医疗设备</option>
            <option value="医疗耗材">医疗耗材</option>
            <option value="医用试剂">医用试剂</option>
            <option value="其他">其他</option>
          </select>
        </div>
        <div class="form-row"><label>规格</label><input v-model="form.specification" /></div>
        <div class="form-row"><label>单位</label><input v-model="form.unit" placeholder="如: 件/盒/台" /></div>
        <div class="form-row"><label>单价</label><input type="number" step="0.01" v-model.number="form.price" /></div>
        <div class="form-row"><label>描述</label><textarea v-model="form.description" rows="3"></textarea></div>
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
import { productApi } from '../services/api';
import Pagination from './Pagination.vue';

interface Product {
  id?: number
  productNo: string
  name: string
  category: string
  specification: string
  unit: string
  price: number
  description?: string
}

const list = ref<Product[]>([]);
const allData = ref<Product[]>([]); // 存储所有数据
const keyword = ref('');
const filterCategory = ref('');
const showForm = ref(false);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});
const editing = ref<Product | null>(null);
const form = reactive({
  productNo: '',
  name: '',
  category: '医疗设备',
  specification: '',
  unit: '件',
  price: 0,
  description: ''
});

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

async function load() {
  try {
    const res = keyword.value.trim()
      ? await productApi.search(keyword.value)
      : await productApi.getAll();
    if (res.code === 200) {
      let data = res.data || [];
      if (filterCategory.value) {
        data = data.filter((p: Product) => p.category === filterCategory.value);
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载产品列表失败:', err);
  }
}

function openNew() {
  editing.value = null;
  form.productNo = '';
  form.name = '';
  form.category = '医疗设备';
  form.specification = '';
  form.unit = '件';
  form.price = 0;
  form.description = '';
  showForm.value = true;
}

function edit(item: Product) {
  editing.value = item;
  form.productNo = item.productNo;
  form.name = item.name;
  form.category = item.category;
  form.specification = item.specification;
  form.unit = item.unit;
  form.price = item.price;
  form.description = item.description || '';
  showForm.value = true;
}

async function save() {
  if (!form.productNo.trim()) { alert('请输入产品编号'); return; }
  if (!form.name.trim()) { alert('请输入产品名称'); return; }

  try {
    if (editing.value?.id) {
      await productApi.update(editing.value.id, { ...form });
    } else {
      await productApi.create({ ...form });
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

async function remove(item: Product) {
  if (!confirm('确认删除产品 ' + item.name + ' ?')) return;
  try {
    if (item.id) {
      await productApi.delete(item.id);
      await load();
    }
  } catch (err: any) {
    alert('删除失败：' + (err?.response?.data?.message || err?.message));
  }
}

onMounted(load);
</script>

<style scoped>
@import '../assets/table-styles.css';
</style>
