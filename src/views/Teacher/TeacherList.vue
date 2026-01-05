<script setup>
import { useRouter } from 'vue-router'; // 1. 引入路由工具
import { ShieldCheck, GraduationCap, Star, MapPin } from 'lucide-vue-next';

// 2. 初始化路由实例
const router = useRouter();

// 模拟后端返回的教师数据
// 对应文档 FR1.1 (资质) 和 FR2.2 (匹配结果)
const teachers = [
  {
    id: 1,
    name: '张老师',
    university: '北京师范大学',
    tags: ['实名认证', '英语专八', '3年教龄'],
    subjects: ['初中英语', '高中英语'],
    rating: 4.9,
    price: 200,
    distance: '2.5km'
  },
  {
    id: 2,
    name: '李同学',
    university: '同济大学 (在读)',
    tags: ['学生证认证', '奥数金牌'],
    subjects: ['小学奥数', '初中数学'],
    rating: 4.8,
    price: 120,
    distance: '1.2km'
  },
  {
    id: 3,
    name: '王老师',
    university: '华东师范大学',
    tags: ['在职教师', '物理竞赛'],
    subjects: ['高中物理'],
    rating: 5.0,
    price: 250,
    distance: '3.0km'
  }
];

// 跳转到详情页的方法
const goToDetail = (id) => {
  router.push(`/teacher/${id}`);
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-24"> <div class="bg-white p-4 shadow-sm sticky top-0 z-10">
      <h1 class="text-xl font-bold text-center">推荐教师</h1>
    </div>

    <div class="p-4 space-y-4">
      <div v-for="teacher in teachers" :key="teacher.id" 
           @click="goToDetail(teacher.id)"
           class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 cursor-pointer active:scale-[0.98] transition-all hover:shadow-md">
        
        <div class="flex justify-between items-start">
          <div>
            <h3 class="text-lg font-bold flex items-center gap-2">
              {{ teacher.name }}
              <ShieldCheck :size="16" class="text-green-500 fill-green-50" />
            </h3>
            <p class="text-sm text-gray-500 flex items-center gap-1 mt-1">
              <GraduationCap :size="14" /> {{ teacher.university }}
            </p>
          </div>
          <div class="text-right">
            <div class="text-red-500 font-bold text-lg">¥{{ teacher.price }}<span class="text-xs text-gray-400">/h</span></div>
            <div class="flex items-center justify-end gap-1 text-xs text-gray-400 mt-1">
              <MapPin :size="12" /> {{ teacher.distance }}
            </div>
          </div>
        </div>

        <div class="flex flex-wrap gap-2 mt-3">
          <span v-for="tag in teacher.tags" :key="tag" 
                class="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs rounded border border-blue-100">
            {{ tag }}
          </span>
        </div>

        <div class="pt-3 border-t flex justify-between items-center mt-3">
          <div class="flex items-center gap-1 font-bold text-gray-700">
            <Star :size="16" class="fill-yellow-400 text-yellow-400" /> 
            {{ teacher.rating }}
            <span class="text-xs text-gray-400 font-normal ml-1"> (120次授课)</span>
          </div>
          <button class="bg-black text-white px-4 py-1.5 rounded-lg text-sm font-medium">
            查看详情
          </button>
        </div>

      </div>
    </div>
  </div>
</template>