import { writable } from 'svelte/store';

let initialAuth = null;
if (typeof window !== 'undefined') {
    const stored = localStorage.getItem('auth');
    if (stored) {
        initialAuth = JSON.parse(stored);
    }
}

export const authStore = writable<{ token: string; username: string; roles: string[] } | null>(initialAuth);

if (typeof window !== 'undefined') {
    authStore.subscribe(value => {
        if (value) {
            localStorage.setItem('auth', JSON.stringify(value));
        } else {
            localStorage.removeItem('auth');
        }
    });
}
