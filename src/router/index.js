import { createRouter, createWebHistory } from 'vue-router';
import Login from '../components/Login.vue';
import StudentView from '../components/StudentView.vue';
import TeacherView from '../components/TeacherView.vue';
import AcademicAnalysis from '../components/AcademicAnalysis.vue';
import EditProfile from '../components/EditProfile.vue'; 
import CompetitionRecommend from '../components/CompetitionRecommend.vue';
import RecentCompetitions from '../components/RecentCompetitions.vue';
import JobAnalysis from '../components/JobAnalysis.vue';
import GrowthRecord from '../components/GrowthRecord.vue';
import CreateCourse from '../components/CreateCourse.vue';
import CourseSelection from '../components/CourseSelection.vue';
import GradingCourseSelection from '../components/GradingCourseSelection.vue';
import GradingPage from '../components/GradingPage.vue';

const routes = [
  {
    path: '/',
    redirect: '/login/student'
  },
  {
    path: '/login',
    redirect: '/login/student'
  },
  {
    path: '/login/:role',
    name: 'Login',
    component: Login,
    props: true
  },
  {
    path: '/student/dashboard',
    name: 'StudentDashboard',
    component: StudentView,
    meta: { requiresAuth: true, role: 'student' }
  },
   {
    path: '/edit-profile',
    name: 'EditProfile',
    component: EditProfile,
    meta: { requiresAuth: true } 
  },
  {
    path: '/teacher/dashboard',
    name: 'TeacherDashboard',
    component: TeacherView,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/academic-analysis',
    name: 'AcademicAnalysis',
    component: AcademicAnalysis,
    // 根据需要设置权限
    meta: { requiresAuth: true, role: 'student' } 
  },
  {
    path: '/job-analysis',
    name: 'JobAnalysis',
    component: JobAnalysis,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/competition-recommend',
    name: 'CompetitionRecommend',
    component: CompetitionRecommend
  },
  {
    path: '/recent-competitions',
    name: 'RecentCompetitions',
    component: RecentCompetitions
  },
    {
    path: '/growth-record',
    name: 'GrowthRecord',
    component: GrowthRecord,
    meta: { requiresAuth: true, role: 'student' }
  },
    {
    path: '/course-selection',
    name: 'CourseSelection',
    component: CourseSelection,
    meta: { requiresAuth: true, role: 'student' }
  },
    {
    path: '/grading-courses',
    name: 'GradingCourseSelection',
    component: GradingCourseSelection,
    meta: { requiresAuth: true, role: 'teacher' } // 教师权限
  },
  {
    path: '/grading/page/:courseId',
    name: 'GradingPage',
    component: GradingPage,
    props: true, // 允许将URL参数(courseId)作为prop传递给组件
    meta: { requiresAuth: true, role: 'teacher' } // 教师权限
  },
    {
    path: '/create-course',
    name: 'CreateCourse',
    component: CreateCourse,
    meta: { requiresAuth: true, role: 'teacher' }
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫 (这部分代码保持不变)
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('auth_token');
    const userRole = localStorage.getItem('user_role');
    
    if (!token) {
      next({ path: `/login/${to.meta.role || 'student'}` });
    } else if (to.meta.role && to.meta.role !== userRole) {
      next({ path: `/login/${userRole}` });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;