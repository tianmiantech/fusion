// 需要路由鉴权的页面使用wrappers包裹
// 路由配置格式建议使用下面的格式
//  {
//   path: 父菜单,
//   routes: [{}] 子菜单列表
//  }
export default [
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
      }
    ],
  }
];
