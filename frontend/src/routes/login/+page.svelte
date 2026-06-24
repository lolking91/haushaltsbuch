<script lang="ts">
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { auth } from '$lib/stores/auth.svelte.js';
	import { ApiError } from '$lib/api/client.js';

	let username = $state('');
	let password = $state('');
	let submitting = $state(false);
	let errorMessage = $state('');

	async function login() {
		submitting = true;
		errorMessage = '';
		try {
			await auth.login(username, password);
			await goto(`${base}/`);
		} catch (e: unknown) {
			errorMessage =
				e instanceof ApiError && e.status === 401
					? $_('login.error_invalid')
					: $_('login.error_generic');
		} finally {
			submitting = false;
		}
	}
</script>

<div class="flex justify-center pt-12">
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
	            dark:border-slate-700 shadow-sm w-full max-w-sm">

		<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
			<Icon icon="heroicons:lock-closed" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
			<h1 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
				{$_('login.title')}
			</h1>
		</div>

		<form onsubmit={(e) => { e.preventDefault(); login(); }} class="px-5 py-4 space-y-4">
			<div>
				<label for="username" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('login.field_username')}
				</label>
				<input
					id="username"
					type="text"
					bind:value={username}
					required
					autofocus
					autocomplete="username"
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<div>
				<label for="password" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('login.field_password')}
				</label>
				<input
					id="password"
					type="password"
					bind:value={password}
					required
					autocomplete="current-password"
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			{#if errorMessage}
				<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
					<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
					{errorMessage}
				</p>
			{/if}

			<button
				type="submit"
				disabled={submitting || !username || !password}
				class="w-full px-3 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium
				       hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
			>
				{submitting ? $_('login.btn_submitting') : $_('login.btn_submit')}
			</button>
		</form>
	</div>
</div>
