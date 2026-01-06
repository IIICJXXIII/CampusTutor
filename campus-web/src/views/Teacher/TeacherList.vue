<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { matchTutors } from '../../api/match.js';
import { Search, MapPin, Filter, Star, Loader2 } from 'lucide-vue-next';

const router = useRouter();
const route = useRoute();

// 筛选条件
const activeFilter = ref('综合');
const loading = ref(false);
const teachers = ref([]);

// 获取匹配的教师列表
const fetchTeachers = async () => {
  loading.value = true;
  try {
    const demandId = route.query.demandId;
    const res = await matchTutors({
      demandId,
      latitude: 31.2304,  // 默认上海坐标
      longitude: 121.4737,
      page: 1,
      size: 20
    });
    
    // 转换后端数据格式
    teachers.value = (res.data || []).map(item => ({
      id: item.tutorId,
      name: item.realName || '老师',
      school: item.universityName || '未填写',
      subject: item.teachSubjects ? JSON.parse(item.teachSubjects)[0] : '综合',
      price: item.expectPrice || 150,
      matchScore: item.matchScore || 80,
      distance: item.distance ? `${item.distance.toFixed(1)}km` : '未知',
      style: item.teachStyle || '鼓励型',
      tags: buildTags(item),
      avatar: item.avatar || `https://api.dicebear.com/7.x/miniavs/svg?seed=${item.tutorId}`
    }));
  } catch (error) {
    console.error('获取教师列表失败:', error);
    // 使用模拟数据
    teachers.value = getMockTeachers();
  } finally {
    loading.value = false;
  }
};

// 构建标签
const buildTags = (item) => {
  const tags = [];
  if (item.certStatus === 2) tags.push('实名认证');
  if (item.orderCount > 10) tags.push('经验丰富');
  if (item.rating >= 4.8) tags.push('好评率高');
  return tags.length ? tags : ['新入驻'];
};

// 模拟教师数据
const getMockTeachers = () => [
  {
    id: 1,
    name: '张老师',
    school: '北京师范大学',
    subject: '数学',
    price: 200,
    matchScore: 95,
    distance: '1.2km',
    style: '鼓励型',
    tags: ['实名认证', '3年教龄'],
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=1'
  },
  {
    id: 2,
    name: '李同学',
    school: '同济大学',
    subject: '奥数',
    price: 120,
    matchScore: 88,
    distance: '2.5km',
    style: '趣味型',
    tags: ['奥数金牌', '学生证认证'],
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=2'
  },
  {
    id: 3,
    name: '王老师',
    school: '华东师范',
    subject: '物理',
    price: 250,
    matchScore: 75,
    distance: '5.8km',
    style: '严厉型',
    tags: ['在职教师', '提分快'],
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=3'
  },
  {
    id: 4,
    name: '赵同学',
    school: '复旦大学',
    subject: '英语',
    price: 180,
    matchScore: 65,
    distance: '3.2km',
    style: '鼓励型',
    tags: ['口语好', '有耐心'],
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=4'
  }
];

// 排序后的教师列表
const sortedTeachers = computed(() => {
  const list = [...teachers.value];
  switch (activeFilter.value) {
    case '距离':
      return list.sort((a, b) => parseFloat(a.distance) - parseFloat(b.distance));
    case '价格':
      return list.sort((a, b) => a.price - b.price);
    case '好评':
      return list.sort((a, b) => b.matchScore - a.matchScore);
    default:
      return list.sort((a, b) => b.matchScore - a.matchScore);
  }
});

onMounted(() => {
  fetchTeachers();
});

// 风格标签颜色映射
const styleColors = {
  '鼓励型': 'bg-green-100 text-green-700',
  '严厉型': 'bg-red-100 text-red-700',
  '趣味型': 'bg-blue-100 text-blue-700'
};

// 匹配分颜色映射
const getScoreColor = (score) => {
  if (score >= 80) return 'text-green-600';
  if (score >= 60) return 'text-yellow-600';
  return 'text-gray-400';
};

const getScoreBarColor = (score) => {
  if (score >= 80) return 'bg-green-500';
  if (score >= 60) return 'bg-yellow-500';
  return 'bg-gray-300';
};

const goToDetail = (id) => {
  router.push(`/teacher/${id}`);
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-24 font-sans">
    
    <div class="bg-white p-2 sticky top-0 z-10 shadow-sm flex justify-between items-center text-sm">
      <div class="flex gap-4 px-2">
        <span v-for="f in ['综合', '距离', '价格', '好评']" :key="f"
              @click="activeFilter = f"
              class="font-bold cursor-pointer transition-colors relative py-2"
              :class="activeFilter === f ? 'text-brand-blue' : 'text-gray-500'">
          {{ f }}
          <div v-if="activeFilter === f" class="absolute bottom-0 left-0 w-full h-1 bg-brand-blue rounded-full"></div>
        </span>
      </div>
      <div class="p-2 text-gray-400">
        <Filter :size="16" />
      </div>
    </div>

    <div class="p-3 grid grid-cols-2 gap-3">
      <!-- 加载中 -->
      <div v-if="loading" class="col-span-2 text-center py-20">
        <Loader2 :size="32" class="mx-auto mb-4 animate-spin text-brand-blue" />
        <p class="text-gray-400">正在匹配老师...</p>
      </div>
      
      <div v-else v-for="teacher in sortedTeachers" :key="teacher.id" 
           @click="goToDetail(teacher.id)"
           class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 active:scale-[0.98] transition-transform flex flex-col">
        
        <div class="p-3 flex items-start gap-2">
          <img :src="teacher.avatar" class="w-10 h-10 rounded-full bg-gray-100" />
          <div class="flex-1 min-w-0">
            <h3 class="font-bold text-gray-800 text-sm truncate">{{ teacher.name }}</h3>
            <p class="text-xs text-gray-500 truncate">{{ teacher.school }}</p>
          </div>
        </div>

        <div class="px-3 pb-2">
          <div class="flex justify-between items-end mb-1">
            <span class="text-xs font-bold text-gray-400">AI匹配度</span>
            <span class="text-lg font-bold leading-none" :class="getScoreColor(teacher.matchScore)">
              {{ teacher.matchScore }}<span class="text-xs">%</span>
            </span>
          </div>
          <div class="w-full h-1.5 bg-gray-100 rounded-full mb-3">
            <div class="h-full rounded-full transition-all duration-1000" 
                 :class="getScoreBarColor(teacher.matchScore)"
                 :style="{ width: teacher.matchScore + '%' }"></div>
          </div>

          <div class="flex justify-between items-center mb-2">
            <span class="text-brand-orange font-bold text-sm">¥{{ teacher.price }}<span class="text-xs text-gray-400">/h</span></span>
            <span class="text-xs px-2 py-0.5 rounded font-medium" :class="styleColors[teacher.style]">
              {{ teacher.style }}
            </span>
          </div>
          
          <div class="flex items-center gap-1 text-xs text-gray-400">
             <MapPin :size="10" /> {{ teacher.distance }}
          </div>
        </div>

        <div class="mt-auto border-t p-2">
          <button @click.stop="router.push(`/booking/${teacher.id}`)" class="...">
            预约试课
          </button>
        </div>
      </div>
    </div>
    
    <div class="text-center text-xs text-gray-400 mt-4">
      已显示全部高匹配教师
    </div>
  </div>
</template>