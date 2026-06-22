import axios from 'axios';
import { get } from 'svelte/store';
import { authStore } from './stores';
import { goto } from '$app/navigation';

export const api = axios.create({
    baseURL: '/api'
});

api.interceptors.request.use((config) => {
    const auth = get(authStore);
    if (auth && auth.token) {
        config.headers.Authorization = `Bearer ${auth.token}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            authStore.set(null);
            localStorage.removeItem('auth');
            goto('/login');
        }
        return Promise.reject(error);
    }
);
