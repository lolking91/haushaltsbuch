<script lang="ts">
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { accountsApi } from '$lib/api/accounts.js';
	import { ApiError } from '$lib/api/client.js';
	import { formatCurrency } from '$lib/utils/format.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	let accounts = $state(data.accounts);

	// --- Delete state ------------------------------------------------------------

	/** ID of the account currently showing the inline delete confirmation. */
	let confirmingDeleteId: number | null = $state(null);
	let deleteError = $state('');

	function requestDelete(id: number) {
		confirmingDeleteId = id;
		deleteError = '';
	}

	function cancelDelete() {
		confirmingDeleteId = null;
		deleteError = '';
	}

	async function confirmDelete(id: number) {
		try {
			await accountsApi.delete(id);
			accounts = accounts.filter((a) => a.id !== id);
			confirmingDeleteId = null;
		} catch (e: unknown) {
			// 409 Conflict means the account still has transactions
			if (e instanceof ApiError && e.status === 409) {
				deleteError = $_('accounts.delete_has_transactions');
			} else {
				deleteError = $_('common.delete_error');
			}
		}
	}
</script>

<div class="space-y-5">

	<!-- Page header -->
	<div class="flex items-center justify-between">
		<h1 class="text-2xl font-bold">{$_('nav.accounts')}</h1>
		<a
			href="{base}/accounts/new"
			class="inline-flex items-center gap-2 px-4 py-2 rounded-xl
			       bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
		>
			<Icon icon="heroicons:plus" class="w-4 h-4" />
			{$_('accounts.btn_new')}
		</a>
	</div>

	<!-- Empty state -->
	{#if accounts.length === 0}
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700
		            shadow-sm p-12 flex flex-col items-center gap-4 text-center">
			<div class="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-950/40
			            flex items-center justify-center">
				<Icon icon="heroicons:building-library" class="w-8 h-8 text-blue-500 dark:text-blue-400" />
			</div>
			<div>
				<p class="font-semibold text-gray-700 dark:text-slate-300">
					{$_('accounts.empty_title')}
				</p>
				<p class="text-sm text-gray-400 dark:text-slate-500 mt-1">
					{$_('accounts.empty_text')}
				</p>
			</div>
			<a
				href="{base}/accounts/new"
				class="inline-flex items-center gap-2 px-4 py-2 rounded-xl
				       bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
			>
				<Icon icon="heroicons:plus" class="w-4 h-4" />
				{$_('accounts.empty_cta')}
			</a>
		</div>

	{:else}
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
		            dark:border-slate-700 shadow-sm overflow-hidden">

			<ul class="divide-y divide-gray-100 dark:divide-slate-700/60">
				{#each accounts as account (account.id)}
					<li>
						{#if confirmingDeleteId === account.id}
							<!-- Inline delete confirmation -->
							<div class="flex items-center gap-3 px-5 py-4 bg-red-50 dark:bg-red-950/20">
								<Icon icon="heroicons:building-library"
								      class="w-5 h-5 text-gray-400 dark:text-slate-500 shrink-0" />
								<div class="flex-1 min-w-0">
									<span class="font-semibold text-gray-900 dark:text-white truncate block">
										{account.name}
									</span>
									<span class="text-xs text-gray-400 dark:text-slate-500">{account.bankName}</span>
								</div>
								<span class="text-sm text-red-600 dark:text-red-400 font-medium shrink-0">
									{$_('accounts.delete_confirm')}
								</span>
								{#if deleteError}
									<span class="text-xs text-red-600 dark:text-red-400 shrink-0 max-w-48">
										{deleteError}
									</span>
								{/if}
								<button
									onclick={cancelDelete}
									class="px-3 py-1.5 rounded-lg text-sm border border-gray-300 dark:border-slate-600
									       text-gray-600 dark:text-slate-300 hover:bg-white dark:hover:bg-slate-700
									       transition-colors shrink-0"
								>
									{$_('common.btn_cancel')}
								</button>
								<button
									onclick={() => confirmDelete(account.id)}
									class="px-3 py-1.5 rounded-lg text-sm font-medium
									       bg-red-600 hover:bg-red-700 text-white transition-colors shrink-0"
								>
									{$_('accounts.delete_yes')}
								</button>
							</div>

						{:else}
							<!-- Normal row -->
							<div class="flex items-center gap-3 px-5 py-4 hover:bg-gray-50
							            dark:hover:bg-slate-700/40 transition-colors">

								<Icon icon="heroicons:building-library"
								      class="w-5 h-5 text-gray-400 dark:text-slate-500 shrink-0" />

								<div class="flex-1 min-w-0">
									<span class="font-semibold text-gray-900 dark:text-white block truncate">
										{account.name}
									</span>
									<span class="text-xs text-gray-400 dark:text-slate-500">
										{account.bankName}
										{#if account.iban}
											&nbsp;·&nbsp;{account.iban}
										{/if}
									</span>
								</div>

								{#if account.balance != null}
									<span class="text-sm font-medium tabular-nums whitespace-nowrap shrink-0">
										{formatCurrency(account.balance, account.currency)}
									</span>
								{/if}

								<!-- Edit link -->
								<a
									href="{base}/accounts/{account.id}"
									class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
									       hover:text-blue-600 dark:hover:text-blue-400
									       hover:bg-blue-50 dark:hover:bg-blue-950/40 transition-colors"
									aria-label="Konto bearbeiten"
								>
									<Icon icon="heroicons:pencil-square" class="w-4 h-4" />
								</a>

								<!-- Delete button -->
								<button
									onclick={() => requestDelete(account.id)}
									class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
									       hover:text-red-600 dark:hover:text-red-400
									       hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors"
									aria-label="Konto löschen"
								>
									<Icon icon="heroicons:trash" class="w-4 h-4" />
								</button>
							</div>
						{/if}
					</li>
				{/each}
			</ul>
		</div>

		<p class="text-xs text-gray-400 dark:text-slate-500 text-right">
			{accounts.length}
			{accounts.length === 1 ? 'Konto' : 'Konten'}
		</p>
	{/if}

</div>
