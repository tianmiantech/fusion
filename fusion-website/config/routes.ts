// 需要路由鉴权的页面使用wrappers包裹
// 路由配置格式建议使用下面的格式
//  {
//   path: 父菜单,
//   routes: [{}] 子菜单列表
//  }
export default [
  {
    path: '/login',
    name: 'login',
    component: './login',
  },
  {
    path: '/register',
    name: 'register',
    component: './register',
  },
  {
    path: '/',
    component: '@/layouts/BasicLayout',
    routes: [
      {
        path: '/',
        redirect: '/home',
      },{
        path: '/home',
        component: './home',
      },
      
      {
        name: 'job',
        path: '/job/create',
        component: './job/Create',
      },
      {
        name: 'jobDetail',
        path: '/job/detail/:id',
        component: './job/Detail',
      },
    ],
  }
];
