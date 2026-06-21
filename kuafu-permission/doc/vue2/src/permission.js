import axios from 'axios';
import store from '@/vuex/store';
import {Message} from 'element-ui';
import session from '@/components/js/session';
import {router} from '@/basic.routes';
import global from '@/components/js/global';
import NProgress from 'nprogress';

const defaultTitle = document.title;

router.beforeEach((to, from, next) => {
    NProgress.start();
    document.title = defaultTitle + (to.meta.title ? ' - ' + to.meta.title : '');

    const user = session.getUser();
    if (!user) {
        if (to.path === '/login') {
            next();
        } else {
            axios.post(global.baseUrl + '/admin/logout', '').then(() => {});
            next({path: '/login'});
        }
        return;
    }

    // 动态菜单和按钮都已加载，直接放行
    if (store.getters.isDynamicMenuLoaded && store.getters.isDynamicButtonLoaded) {
        next();
        return;
    }

    // 先加载菜单，菜单完成后再加载按钮，避免并行 next() 竞争
    if (!store.getters.isDynamicMenuLoaded) {
        store.dispatch('GenerateMenus').then(aRoutes => {
            aRoutes.filter(rt => !rt.ifExt).forEach(rt => {
                router.addRoute(rt);
            });

            // 菜单加载完成后，如果按钮也未加载，继续加载按钮
            if (!store.getters.isDynamicButtonLoaded) {
                return store.dispatch('GenerateButtons');
            }
        }).then(() => {
            // 菜单和按钮都确保加载完成后跳转
            next({...to, replace: true});
        }).catch(err => {
            Message.error(err.message || '加载权限数据失败');
            next({path: '/'});
        });
        return;
    }

    // 菜单已加载，只加载按钮
    if (!store.getters.isDynamicButtonLoaded) {
        store.dispatch('GenerateButtons').then(() => {
            next();
        }).catch(err => {
            Message.error(err.message || '加载权限数据失败');
            next({path: '/'});
        });
        return;
    }
});

router.afterEach(() => {
    NProgress.done();
});
