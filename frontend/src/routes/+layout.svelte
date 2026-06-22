<script lang="ts">
    import '../app.css';
    import { authStore } from '$lib/stores';
    import { goto } from '$app/navigation';
    import { page } from '$app/stores';

    function logout() {
        authStore.set(null);
        goto('/login');
    }
</script>

<div class="min-h-screen flex flex-col">
    <nav class="sticky top-0 z-50 glass-panel shadow-md shadow-black/50">
        <div class="container mx-auto px-6 py-4 flex justify-between items-center">
            <a href="/" class="text-3xl font-extrabold tracking-tight text-zinc-100 hover:text-violet-400 transition-colors duration-300">
                A Simple Forum
            </a>
            <div class="flex items-center gap-4">
                {#if $authStore}
                    <div class="flex items-center gap-3">
                        <div class="hidden sm:flex items-center gap-2 bg-zinc-800/50 px-4 py-1.5 rounded-full border border-zinc-700">
                            <div class="w-2 h-2 rounded-full bg-violet-400 animate-pulse"></div>
                            <span class="text-sm font-medium text-zinc-100">{$authStore.username}</span>
                        </div>
                        <button on:click={logout} class="hover:bg-red-500/10 text-zinc-400 hover:text-red-400 px-4 py-2 rounded-xl transition-all duration-300 font-medium text-sm flex items-center gap-2">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                            Logout
                        </button>
                    </div>
                {:else}
                    <div class="flex gap-3">
                        <a href="/login" class="px-5 py-2 rounded-xl font-medium text-sm transition-all duration-300 { $page.url.pathname === '/login' ? 'bg-zinc-800 text-zinc-100' : 'text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50' }">Sign In</a>
                        <a href="/register" class="bg-violet-500 hover:bg-violet-600 text-white px-5 py-2 rounded-xl font-medium text-sm transition-all duration-300 shadow-md shadow-violet-500/20">Join Now</a>
                    </div>
                {/if}
            </div>
        </div>
    </nav>

    <main class="flex-1 w-full relative">
        <slot />
    </main>
</div>
