<script lang="ts">
    import { api } from '$lib/api';
    import { goto } from '$app/navigation';

    let username = '';
    let password = '';
    let errorMsg = '';
    let loading = false;

    async function register() {
        loading = true;
        errorMsg = '';
        try {
            await api.post('/auth/register', { username, password });
            goto('/login');
        } catch (err: any) {
            errorMsg = err.response?.data || 'Registration failed. Username might be taken.';
        } finally {
            loading = false;
        }
    }
</script>

<div class="min-h-[80vh] flex items-center justify-center p-4">
    <div class="w-full max-w-md glass-panel p-8 rounded-3xl shadow-xl shadow-black/50 relative overflow-hidden">
        <div class="relative z-10">
            <div class="text-center mb-8">
                <h2 class="text-4xl font-bold text-zinc-100 mb-2">Create Account</h2>
                <p class="text-zinc-400">Join the community and start posting</p>
            </div>

            {#if errorMsg}
                <div class="bg-red-500/10 border border-red-500/30 text-red-400 px-4 py-3 rounded-xl mb-6 text-sm flex items-center gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                    {errorMsg}
                </div>
            {/if}

            <form on:submit|preventDefault={register} class="space-y-5">
                <div>
                    <label class="block text-sm font-medium text-zinc-400 mb-1.5 ml-1">Username</label>
                    <input type="text" bind:value={username} required 
                        class="w-full bg-zinc-950 border border-zinc-800 rounded-xl px-4 py-3 text-zinc-100 placeholder-zinc-600 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all" 
                        placeholder="Choose a unique username" />
                </div>
                <div>
                    <label class="block text-sm font-medium text-zinc-400 mb-1.5 ml-1">Password</label>
                    <input type="password" bind:value={password} required minlength="6"
                        class="w-full bg-zinc-950 border border-zinc-800 rounded-xl px-4 py-3 text-zinc-100 placeholder-zinc-600 focus:outline-none focus:ring-2 focus:ring-violet-500/50 focus:border-violet-500/50 transition-all" 
                        placeholder="Minimum 6 characters" />
                </div>
                
                <button type="submit" disabled={loading} 
                    class="w-full bg-violet-500 hover:bg-violet-600 text-white font-semibold py-3 rounded-xl shadow-md shadow-violet-500/20 transition-all transform hover:-translate-y-0.5 active:translate-y-0 disabled:opacity-70 disabled:cursor-not-allowed mt-4">
                    {#if loading}
                        <span class="flex items-center justify-center gap-2">
                            <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                            Registering...
                        </span>
                    {:else}
                        Create Account
                    {/if}
                </button>
            </form>

            <p class="mt-6 text-center text-sm text-zinc-400">
                Already have an account? <a href="/login" class="text-violet-400 hover:text-violet-300 font-medium transition-colors">Sign in</a>
            </p>
        </div>
    </div>
</div>
