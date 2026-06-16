import axios from 'axios';
import md5 from 'js-md5';
import { Message } from 'element-ui';
import global from './components/js/global';
import session from './components/js/session';

// ==================== 请求拦截器 ====================
axios.interceptors.request.use(
    (config) => {
        const key = global.signKey;
        const secret = global.signSecret;
        const timestamp = new Date().getTime();
        const sign = md5(`key=${key}&secret=${secret}&timestamp=${timestamp}`);

        const token = session.getToken();

        // 适配 axios 1.x：直接操作 config.headers
        if (token != null && token !== 'undefined') {
            config.headers[global.tokenHeaderName] = token;
        }
        config.headers['key'] = key;
        config.headers['timestamp'] = timestamp;
        config.headers['sign'] = sign;

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// ==================== 响应拦截器 ====================
axios.interceptors.response.use(
    (response) => {
        // 后端自定义的登录过期码
        if (response.data && response.data.code === '1001') {
            session.removeUser();
            session.removeToken();
            location.href = './';
            return Promise.reject(new Error('登录已过期，请重新登录'));
        }
        return response;
    },
    (error) => {
        // axios 1.x 会将 HTTP 错误状态（4xx/5xx）抛到这里
        if (error.response) {
            const { status } = error.response;
            if (status === 401) {
                session.removeUser();
                session.removeToken();
                location.href = './';
            } else if (status === 403) {
                Message.error('无权访问该资源');
            } else if (status >= 500) {
                Message.error('服务器错误，请稍后重试');
            }
        } else if (error.request) {
            Message.error('网络请求失败，请检查网络连接');
        } else {
            Message.error(error.message || '请求配置出错');
        }
        return Promise.reject(error);
    }
);
