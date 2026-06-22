<script lang="ts">
	import { api } from '$lib/api';
	import { authStore } from '$lib/stores';
	import { onMount } from 'svelte';
	import { slide, fade, scale } from 'svelte/transition';

	let posts: any[] = [];
	let newPostContent = '';
	let replyTexts: Record<string, string> = {};
	let activeReplyPostId: string | null = null;
	let isCreatingPost = false;
	let loading = true;

	let showModal = false;
	let modalAction: (() => Promise<void>) | null = null;
	let modalTitle = '';
	let modalMessage = '';
	let isProcessingAction = false;
	let errorToast = '';

	$: isAdmin = $authStore?.roles?.includes('ROLE_ADMIN') || false;

	async function loadPosts() {
		if (!$authStore) {
			loading = false;
			return;
		}
		try {
			const res = await api.get('/posts');
			posts = res.data;
		} catch (err) {
			console.error('Failed to load posts', err);
		} finally {
			loading = false;
		}
	}

	onMount(() => {
		loadPosts();
	});

	async function createPost() {
		if (!newPostContent.trim()) return;
		isCreatingPost = true;
		try {
			await api.post('/posts', { content: newPostContent });
			newPostContent = '';
			await loadPosts();
		} catch (err) {
			console.error(err);
		} finally {
			isCreatingPost = false;
		}
	}

	async function addReply(postId: string) {
		const text = replyTexts[postId];
		if (!text || !text.trim()) return;
		try {
			await api.post(`/posts/${postId}/replies`, { text });
			replyTexts[postId] = '';
			activeReplyPostId = null;
			await loadPosts();
		} catch (err) {
			console.error(err);
		}
	}

	function confirmDeletePost(postId: string) {
		modalTitle = 'Delete Post';
		modalMessage =
			'Are you sure you want to delete this post? This action cannot be undone and will remove all its replies.';
		modalAction = async () => {
			await api.delete(`/posts/${postId}`);
			await loadPosts();
		};
		showModal = true;
	}

	function confirmDeleteReply(postId: string, replyId: string) {
		modalTitle = 'Delete Reply';
		modalMessage = 'Are you sure you want to delete this reply? This action cannot be undone.';
		modalAction = async () => {
			await api.delete(`/posts/${postId}/replies/${replyId}`);
			await loadPosts();
		};
		showModal = true;
	}

	async function executeModalAction() {
		if (!modalAction) return;
		isProcessingAction = true;
		try {
			await modalAction();
			showModal = false;
		} catch (err) {
			console.error(err);
			errorToast = 'Action failed. Please try again.';
			setTimeout(() => (errorToast = ''), 3000);
		} finally {
			isProcessingAction = false;
		}
	}

	function closeModal() {
		showModal = false;
		modalAction = null;
	}

	function formatTime(isoString: string) {
		const d = new Date(isoString);
		return (
			d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' }) +
			' ' +
			d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
		);
	}
</script>

{#if loading}
	<div class="flex items-center justify-center min-h-[60vh]">
		<div
			class="w-16 h-16 border-4 border-violet-500/30 border-t-violet-500 rounded-full animate-spin"
		></div>
	</div>
{:else if !$authStore}
	<div class="flex items-center justify-center min-h-[80vh] p-4" in:fade>
		<div
			class="max-w-2xl text-center glass-panel p-12 rounded-3xl shadow-2xl relative overflow-hidden"
		>
			<div class="relative z-10">
				<h1 class="text-6xl font-extrabold mb-6 tracking-tight text-zinc-100">A Simple Forum</h1>
				<p class="text-xl text-zinc-400 mb-10 leading-relaxed max-w-lg mx-auto">
					A sleek, dark, and simple space to collaborate, share thoughts, and connect.
				</p>
				<div class="flex justify-center gap-4">
					<a
						href="/login"
						class="px-8 py-3.5 bg-violet-500 hover:bg-violet-600 text-white font-bold rounded-2xl transition-all shadow-lg hover:-translate-y-1"
					>
						Sign In
					</a>
					<a
						href="/register"
						class="px-8 py-3.5 bg-zinc-800 hover:bg-zinc-700 text-zinc-100 font-bold rounded-2xl border border-zinc-700 transition-all"
					>
						Create Account
					</a>
				</div>
			</div>
		</div>
	</div>
{:else}
	<div class="container mx-auto px-4 py-8" in:fade={{ duration: 400 }}>
		{#if errorToast}
			<div
				class="fixed top-24 left-1/2 -translate-x-1/2 z-50 bg-red-500 text-white px-6 py-3 rounded-full shadow-lg font-medium"
				transition:fade
			>
				{errorToast}
			</div>
		{/if}
		{#if showModal}
			<div
				class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
				transition:fade={{ duration: 200 }}
			>
				<div
					class="bg-zinc-900 border border-zinc-800 p-8 rounded-3xl shadow-2xl max-w-md w-full relative"
					transition:scale={{ duration: 200, start: 0.95 }}
				>
					<div class="w-12 h-12 rounded-full bg-red-500/10 flex items-center justify-center mb-6">
						<svg
							xmlns="http://www.w3.org/2000/svg"
							class="w-6 h-6 text-red-500"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="2"
							stroke-linecap="round"
							stroke-linejoin="round"
							><path
								d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
							></path><line x1="12" y1="9" x2="12" y2="13"></line><line
								x1="12"
								y1="17"
								x2="12.01"
								y2="17"
							></line></svg
						>
					</div>
					<h3 class="text-2xl font-bold text-zinc-100 mb-2">{modalTitle}</h3>
					<p class="text-zinc-400 mb-8 leading-relaxed">{modalMessage}</p>
					<div class="flex gap-3 justify-end">
						<button
							on:click={closeModal}
							disabled={isProcessingAction}
							class="px-6 py-2.5 rounded-xl text-zinc-300 font-medium hover:bg-zinc-800 transition-colors disabled:opacity-50"
						>
							Cancel
						</button>
						<button
							on:click={executeModalAction}
							disabled={isProcessingAction}
							class="bg-red-500 hover:bg-red-600 text-white px-6 py-2.5 rounded-xl font-medium shadow-lg shadow-red-500/20 transition-all flex items-center gap-2 disabled:opacity-50"
						>
							{#if isProcessingAction}
								<svg
									class="animate-spin h-4 w-4"
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
									><circle
										class="opacity-25"
										cx="12"
										cy="12"
										r="10"
										stroke="currentColor"
										stroke-width="4"
									></circle><path
										class="opacity-75"
										fill="currentColor"
										d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
									></path></svg
								>
								Deleting...
							{:else}
								Yes, Delete
							{/if}
						</button>
					</div>
				</div>
			</div>
		{/if}
		<div class="mb-12 max-w-3xl mx-auto">
			<div class="glass-panel p-6 rounded-3xl shadow-xl">
				<form on:submit|preventDefault={createPost}>
					<div class="flex items-start gap-4">
						<div
							class="w-10 h-10 rounded-full bg-zinc-800 border border-zinc-700 flex items-center justify-center font-bold text-violet-400 shadow-inner flex-shrink-0"
						>
							{$authStore.username.charAt(0).toUpperCase()}
						</div>
						<div class="flex-1">
							<textarea
								bind:value={newPostContent}
								placeholder="Share something with the board..."
								class="w-full bg-zinc-950 border border-zinc-800 rounded-2xl p-4 text-zinc-100 placeholder-zinc-600 focus:outline-none focus:ring-2 focus:ring-violet-500/50 resize-none min-h-[100px] transition-all font-inter"
								required
							></textarea>
							<div class="flex justify-between items-center mt-3">
								<span class="text-xs text-zinc-500">{newPostContent.length} chars</span>
								<button
									type="submit"
									disabled={isCreatingPost || !newPostContent.trim()}
									class="bg-violet-500 hover:bg-violet-600 text-white px-6 py-2 rounded-xl font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-md flex items-center gap-2"
								>
									{#if isCreatingPost}
										<svg
											class="animate-spin h-4 w-4"
											xmlns="http://www.w3.org/2000/svg"
											fill="none"
											viewBox="0 0 24 24"
											><circle
												class="opacity-25"
												cx="12"
												cy="12"
												r="10"
												stroke="currentColor"
												stroke-width="4"
											></circle><path
												class="opacity-75"
												fill="currentColor"
												d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
											></path></svg
										>
									{:else}
										<svg
											xmlns="http://www.w3.org/2000/svg"
											width="16"
											height="16"
											viewBox="0 0 24 24"
											fill="none"
											stroke="currentColor"
											stroke-width="2"
											stroke-linecap="round"
											stroke-linejoin="round"
											><line x1="22" y1="2" x2="11" y2="13"></line><polygon
												points="22 2 15 22 11 13 2 9 22 2"
											></polygon></svg
										>
									{/if}
									Post
								</button>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>

		<!-- Masonry Grid -->
		{#if posts.length === 0}
			<div class="text-center py-20 text-zinc-600">
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="w-16 h-16 mx-auto mb-4 opacity-50"
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					stroke-width="1.5"
					stroke-linecap="round"
					stroke-linejoin="round"
					><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline
						points="14 2 14 8 20 8"
					></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line
						x1="16"
						y1="17"
						x2="8"
						y2="17"
					></line><polyline points="10 9 9 9 8 9"></polyline></svg
				>
				<p class="text-xl font-medium">It's quiet here...</p>
				<p>Be the first to share something!</p>
			</div>
		{:else}
			<div class="columns-1 sm:columns-2 lg:columns-3 xl:columns-4 gap-6 space-y-6 pb-20">
				{#each posts as post (post.id)}
					<div class="break-inside-avoid relative group" in:fade={{ duration: 300 }}>
						<div
							class="sticky-note bg-zinc-900 border-zinc-800 p-5 rounded-2xl shadow-lg border text-zinc-100"
						>
							<!-- Admin Delete Post Button -->
							{#if isAdmin}
								<button
									on:click={() => confirmDeletePost(post.id)}
									class="absolute top-2 right-2 text-red-500/50 hover:text-red-500 p-1.5 hover:bg-red-500/10 rounded-lg transition-colors z-20"
									title="Delete Post"
								>
									<svg
										xmlns="http://www.w3.org/2000/svg"
										width="16"
										height="16"
										viewBox="0 0 24 24"
										fill="none"
										stroke="currentColor"
										stroke-width="2"
										stroke-linecap="round"
										stroke-linejoin="round"
										><path d="M3 6h18"></path><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"
										></path><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path><line
											x1="10"
											y1="11"
											x2="10"
											y2="17"
										></line><line x1="14" y1="11" x2="14" y2="17"></line></svg
									>
								</button>
							{/if}

							<!-- Header -->
							<div class="flex items-center justify-between mb-3 border-b border-zinc-800 pb-2">
								<div class="flex items-center gap-2">
									<div
										class="w-7 h-7 rounded-full bg-zinc-800 border border-zinc-700 flex items-center justify-center font-bold text-xs text-violet-400"
									>
										{post.authorUsername.charAt(0).toUpperCase()}
									</div>
									<span class="font-bold text-sm tracking-tight">{post.authorUsername}</span>
								</div>
								<span class="text-[10px] uppercase font-bold text-zinc-500 tracking-wider mr-6"
									>{formatTime(post.createdAt)}</span
								>
							</div>

							<!-- Content -->
							<div class="text-base mb-5 whitespace-pre-wrap font-inter leading-relaxed">
								{post.content}
							</div>

							<!-- Replies Section -->
							<div
								class="mt-auto bg-zinc-950/50 -mx-5 -mb-5 p-4 rounded-b-2xl border-t border-zinc-800"
							>
								{#if post.replies && post.replies.length > 0}
									<div class="space-y-3 mb-4 max-h-[200px] overflow-y-auto pr-2 custom-scrollbar">
										{#each post.replies as reply}
											<div
												class="text-sm bg-zinc-900 p-2.5 rounded-xl border border-zinc-800 shadow-sm relative group/reply"
												transition:slide={{ duration: 200 }}
											>
												<!-- Admin Delete Reply Button -->
												{#if isAdmin}
													<button
														on:click={() => confirmDeleteReply(post.id, reply.replyId)}
														class="absolute top-1 right-1 text-red-500/0 group-hover/reply:text-red-500/50 hover:!text-red-500 p-1 hover:bg-red-500/10 rounded-md transition-all"
														title="Delete Reply"
													>
														<svg
															xmlns="http://www.w3.org/2000/svg"
															width="12"
															height="12"
															viewBox="0 0 24 24"
															fill="none"
															stroke="currentColor"
															stroke-width="2"
															stroke-linecap="round"
															stroke-linejoin="round"
															><path d="M3 6h18"></path><path
																d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"
															></path></svg
														>
													</button>
												{/if}
												<div class="flex items-baseline justify-between mb-1">
													<span class="font-bold text-xs text-violet-300"
														>{reply.authorUsername}</span
													>
													<span class="text-[9px] text-zinc-500 mr-4"
														>{formatTime(reply.repliedAt)}</span
													>
												</div>
												<p class="text-zinc-300 font-inter pr-4">{reply.text}</p>
											</div>
										{/each}
									</div>
								{/if}

								{#if activeReplyPostId === post.id}
									<div transition:slide={{ duration: 200 }}>
										<form on:submit|preventDefault={() => addReply(post.id)} class="flex gap-2">
											<input
												type="text"
												bind:value={replyTexts[post.id]}
												placeholder="Write a reply..."
												class="flex-1 text-sm p-2 bg-zinc-950 border border-zinc-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/50 placeholder-zinc-600 font-inter transition-all text-zinc-100"
												autofocus
											/>
											<button
												type="submit"
												class="bg-violet-500 hover:bg-violet-600 text-white px-3 py-1.5 text-xs font-bold rounded-lg transition-colors shadow-sm disabled:opacity-50"
											>
												Reply
											</button>
										</form>
										<button
											type="button"
											on:click={() => (activeReplyPostId = null)}
											class="mt-2 text-xs text-zinc-500 hover:text-zinc-300 font-medium"
											>Cancel</button
										>
									</div>
								{:else}
									<button
										on:click={() => (activeReplyPostId = post.id)}
										class="flex items-center gap-1.5 text-xs font-bold text-zinc-400 hover:text-zinc-200 transition-colors bg-zinc-800/50 hover:bg-zinc-800 px-3 py-1.5 rounded-lg w-full justify-center"
									>
										<svg
											xmlns="http://www.w3.org/2000/svg"
											width="12"
											height="12"
											viewBox="0 0 24 24"
											fill="none"
											stroke="currentColor"
											stroke-width="3"
											stroke-linecap="round"
											stroke-linejoin="round"
											><path
												d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"
											></path></svg
										>
										{post.replies && post.replies.length > 0
											? 'Add Reply'
											: 'Be the first to reply'}
									</button>
								{/if}
							</div>
						</div>
					</div>
				{/each}
			</div>
		{/if}
	</div>
{/if}

<style>
	.custom-scrollbar::-webkit-scrollbar {
		width: 4px;
	}
	.custom-scrollbar::-webkit-scrollbar-track {
		background: transparent;
	}
	.custom-scrollbar::-webkit-scrollbar-thumb {
		background: #27272a;
		border-radius: 4px;
	}
	.custom-scrollbar:hover::-webkit-scrollbar-thumb {
		background: #3f3f46;
	}
</style>
