import Vue from 'vue';
import VueRouter from 'vue-router';

import Layout from '@/views/Layout.vue';

Vue.use(VueRouter);


export const basicRoutes = [
	{
        path: '/login',
        name: '',
        component: () => import('@/views/Login.vue'),
        hidden: true
	},
	{
        path: '/404',
        name: '',
        component: () => import('@/views/404.vue'),
        hidden: true
	},
//	{
//		path: '/',
//		redirect: '/monitor',
//		hidden: true
//	},
    {
        path: '',
        name: '',
        component: Layout,
        leaf: true,
        children: [
            {
                path: '/',
                name: 'home',
                component: () => import('@/views/Home.vue'),
                meta: {title: '首页', icon: 'home'}
            }
        ]
    }
];

export const createBasicRouter = () => new VueRouter({
    //mode: 'history', // 去掉url中的#
    scrollBehavior: () => ({y: 0}),
    routes: basicRoutes
});
export const router = createBasicRouter();

//修改原型对象中的push方法
const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err);
}
