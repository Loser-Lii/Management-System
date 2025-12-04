<template>
  <div class="pagination" v-if="totalPages > 1">
    <button @click="goToPage(1)" :disabled="currentPage === 1">首页</button>
    <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
    
    <span class="page-info">
      第 <input 
        type="number" 
        :value="currentPage" 
        @change="handlePageInput"
        :min="1" 
        :max="totalPages"
        class="page-input"
      /> / {{ totalPages }} 页
    </span>
    
    <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages">下一页</button>
    <button @click="goToPage(totalPages)" :disabled="currentPage === totalPages">末页</button>
    
    <span class="total-info">共 {{ total }} 条</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  total: number
  pageSize: number
  currentPage: number
}>()

const emit = defineEmits<{
  (e: 'update:currentPage', page: number): void
}>()

const totalPages = computed(() => Math.ceil(props.total / props.pageSize))

function goToPage(page: number) {
  if (page >= 1 && page <= totalPages.value) {
    emit('update:currentPage', page)
  }
}

function handlePageInput(e: Event) {
  const target = e.target as HTMLInputElement
  const page = parseInt(target.value)
  if (!isNaN(page)) {
    goToPage(page)
  }
}
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.pagination button {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  background: #f5f5f5;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #333;
}

.page-input {
  width: 50px;
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: center;
  font-size: 14px;
}

.page-input::-webkit-inner-spin-button,
.page-input::-webkit-outer-spin-button {
  opacity: 1;
}

.total-info {
  color: #666;
  font-size: 14px;
  margin-left: 10px;
}
</style>
