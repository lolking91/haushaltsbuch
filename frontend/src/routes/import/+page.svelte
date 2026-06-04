<script lang="ts">
	import { page } from '$app/state';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { base } from '$app/paths';
	import { formatCurrency, formatDate } from '$lib/utils/format.js';
	import type { Account, ImportResult, ImportState, EtfImportResult } from '$lib/types/types.js';
	import { accountsApi } from '$lib/api/accounts.js';
	import { importApi } from '$lib/api/import.js';
	import { etfsApi } from '$lib/api/etfs.js';

	// --- Tab state ---
	// Read initial tab from ?tab=etf URL param (used when linking from the portfolio page)

	type Tab = 'ing' | 'etf';
	let activeTab: Tab = $state(
		page.url.searchParams.get('tab') === 'etf' ? 'etf' : 'ing'
	);

	function switchTab(tab: Tab) {
		if (tab === activeTab) return;
		activeTab = tab;
		resetAll();
	}

	// --- Shared upload state ---

	let importState: ImportState = $state('idle');
	let selectedFile: File | null = $state(null);
	let isDragging = $state(false);
	let errorMessage = $state('');

	// --- ING-specific state ---

	let applyRules = $state(false);
	let ingResult: ImportResult | null = $state(null);
	let ingAccount: Account | null = $state(null);

	// --- ETF-specific state ---

	let etfResult: EtfImportResult | null = $state(null);

	// --- File selection (shared) ---

	function onFileInput(event: Event) {
		const input = event.target as HTMLInputElement;
		if (input.files?.[0]) selectedFile = input.files[0];
	}

	function onDragOver(event: DragEvent) {
		event.preventDefault();
		isDragging = true;
	}

	function onDragLeave() {
		isDragging = false;
	}

	function onDrop(event: DragEvent) {
		event.preventDefault();
		isDragging = false;
		const file = event.dataTransfer?.files[0];
		if (file?.name.toLowerCase().endsWith('.csv')) selectedFile = file;
	}

	// --- Upload ---

	async function upload() {
		if (!selectedFile) return;

		importState = 'uploading';
		errorMessage = '';

		try {
			if (activeTab === 'ing') {
				ingResult = await importApi.importIng(selectedFile, applyRules);
				ingAccount = await accountsApi.getById(ingResult.accountId);
			} else {
				etfResult = await etfsApi.importDepot(selectedFile);
			}
			importState = 'success';
		} catch (e) {
			errorMessage = e instanceof Error ? e.message : 'Unknown error';
			importState = 'error';
		}
	}

	function resetAll() {
		importState = 'idle';
		selectedFile = null;
		ingResult = null;
		ingAccount = null;
		etfResult = null;
		errorMessage = '';
	}
</script>

<h1 class="text-2xl font-bold mb-6">{$_('nav.import')}</h1>

<!-- ── Format tabs ──────────────────────────────────────────────────────────── -->
<div class="flex gap-0 mb-6 border-b border-gray-200 dark:border-slate-700">
	{#each ([['ing', 'heroicons:building-library'], ['etf', 'heroicons:chart-bar']] as const) as [tab, icon]}
		<button
			onclick={() => switchTab(tab)}
			class="flex items-center gap-2 px-5 py-2.5 text-sm font-medium border-b-2 transition-colors -mb-px
				{activeTab === tab
					? 'border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
					: 'border-transparent text-gray-500 dark:text-slate-400 hover:text-gray-700 dark:hover:text-slate-200 hover:border-gray-300 dark:hover:border-slate-500'}"
		>
			<Icon {icon} class="w-4 h-4" />
			{$_(`import.tab_${tab}`)}
		</button>
	{/each}
</div>

<!-- ── Upload form (idle / error) ───────────────────────────────────────────── -->
{#if importState === 'idle' || importState === 'error'}
	<div class="max-w-lg space-y-4">

		<!-- Drop zone -->
		<div
			role="button"
			tabindex="0"
			aria-label={$_('import.drop_label')}
			ondragover={onDragOver}
			ondragleave={onDragLeave}
			ondrop={onDrop}
			class="relative flex flex-col items-center justify-center w-full h-44 rounded-xl border-2
			       border-dashed transition-colors cursor-pointer select-none
			       {isDragging
			         ? 'border-blue-500 bg-blue-50 dark:bg-blue-950/30'
			         : 'border-gray-300 dark:border-slate-600 hover:border-blue-400 dark:hover:border-blue-500'}"
		>
			<label class="flex flex-col items-center gap-2 cursor-pointer w-full h-full justify-center">
				<Icon icon="heroicons:arrow-up-tray" class="w-10 h-10 text-gray-400 dark:text-slate-500" />

				{#if selectedFile}
					<span class="text-sm font-medium text-blue-600 dark:text-blue-400">{selectedFile.name}</span>
					<span class="text-xs text-gray-400 dark:text-slate-500">{$_('import.drop_change')}</span>
				{:else}
					<span class="text-sm text-gray-600 dark:text-slate-300">
						{activeTab === 'etf' ? $_('portfolio_import.drop_hint') : $_('import.drop_hint')}
					</span>
					<span class="text-sm font-medium text-blue-600 dark:text-blue-400 underline">{$_('import.drop_action')}</span>
				{/if}

				<input type="file" accept=".csv" class="hidden" onchange={onFileInput} />
			</label>
		</div>

		<!-- Error -->
		{#if importState === 'error'}
			<div class="flex items-start gap-3 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400">
				<Icon icon="heroicons:exclamation-circle" class="w-5 h-5 mt-0.5 shrink-0" />
				<p class="text-sm">{errorMessage}</p>
			</div>
		{/if}

		<!-- ING only: apply rules toggle -->
		{#if activeTab === 'ing'}
			<label class="flex items-center gap-3 cursor-pointer select-none">
				<input type="checkbox" bind:checked={applyRules} class="w-4 h-4 rounded accent-blue-600" />
				<span class="text-sm text-gray-700 dark:text-slate-300">{$_('import.apply_rules')}</span>
			</label>
		{/if}

		<!-- Upload button -->
		<button
			onclick={upload}
			disabled={!selectedFile}
			class="w-full py-2.5 rounded-xl font-medium text-sm transition-colors
			       bg-blue-600 hover:bg-blue-700 text-white
			       disabled:opacity-40 disabled:cursor-not-allowed"
		>
			{$_('import.button')}
		</button>
	</div>
{/if}

<!-- ── Uploading ────────────────────────────────────────────────────────────── -->
{#if importState === 'uploading'}
	<div class="max-w-lg flex flex-col items-center gap-4 py-12 text-gray-500 dark:text-slate-400">
		<Icon icon="heroicons:arrow-path" class="w-10 h-10 animate-spin text-blue-600" />
		<p class="text-sm">{$_('import.uploading')}</p>
	</div>
{/if}

<!-- ── Success: ING ──────────────────────────────────────────────────────────── -->
{#if importState === 'success' && activeTab === 'ing' && ingResult}
	<div class="max-w-lg space-y-4">

		<div class="flex items-center gap-3 p-5 rounded-xl bg-green-50 dark:bg-green-950/30 border border-green-200 dark:border-green-800">
			<Icon icon="heroicons:check-circle" class="w-7 h-7 text-green-600 dark:text-green-400 shrink-0" />
			<div>
				<p class="font-semibold text-green-800 dark:text-green-300">{$_('import.success_title')}</p>
				<p class="text-xs text-green-700 dark:text-green-500">
					{$_('import.success_job', { values: { id: ingResult.importJobId } })}
				</p>
			</div>
		</div>

		<div class="grid gap-3" class:grid-cols-3={!applyRules} class:grid-cols-4={applyRules}>
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-blue-600 dark:text-blue-400">{ingResult.imported}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('import.stat_imported')}</p>
			</div>
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-gray-400 dark:text-slate-500">{ingResult.skipped}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('import.stat_skipped')}</p>
			</div>
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-red-400">{ingResult.errors}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('import.stat_errors')}</p>
			</div>
			{#if applyRules}
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
					<p class="text-3xl font-bold text-green-600 dark:text-green-400">{ingResult.categorized}</p>
					<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('import.stat_categorized')}</p>
				</div>
			{/if}
		</div>

		{#if ingAccount}
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm space-y-3">
				<h2 class="font-semibold text-sm text-gray-500 dark:text-slate-400 uppercase tracking-wide">
					{$_('import.account_section')}
				</h2>
				<div class="flex items-center justify-between">
					<div>
						<p class="font-semibold">{ingAccount.name}</p>
						<p class="text-sm text-gray-500 dark:text-slate-400">{ingAccount.bankName}</p>
					</div>
					<p class="text-lg font-bold tabular-nums">
						{formatCurrency(ingAccount.balance, ingAccount.currency)}
					</p>
				</div>
				<p class="text-xs font-mono text-gray-400 dark:text-slate-500 tracking-wider">{ingAccount.iban}</p>
			</div>
		{/if}

		<button
			onclick={resetAll}
			class="w-full py-2.5 rounded-xl font-medium text-sm transition-colors
			       border border-gray-300 dark:border-slate-600
			       hover:bg-gray-100 dark:hover:bg-slate-700"
		>
			{$_('import.restart')}
		</button>
	</div>
{/if}

<!-- ── Success: ETF ──────────────────────────────────────────────────────────── -->
{#if importState === 'success' && activeTab === 'etf' && etfResult}
	<div class="max-w-lg space-y-4">

		<div class="flex items-center gap-3 p-5 rounded-xl bg-green-50 dark:bg-green-950/30 border border-green-200 dark:border-green-800">
			<Icon icon="heroicons:check-circle" class="w-7 h-7 text-green-600 dark:text-green-400 shrink-0" />
			<div>
				<p class="font-semibold text-green-800 dark:text-green-300">{$_('portfolio_import.success_title')}</p>
				<p class="text-xs text-green-700 dark:text-green-500">
					{$_('portfolio_import.success_date', { values: { date: formatDate(etfResult.importDate) } })}
				</p>
			</div>
		</div>

		<div class="grid grid-cols-3 gap-3">
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-blue-600 dark:text-blue-400">{etfResult.created}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('portfolio_import.stat_created')}</p>
			</div>
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-green-600 dark:text-green-400">{etfResult.inserted}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('portfolio_import.stat_inserted')}</p>
			</div>
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-gray-400 dark:text-slate-500">{etfResult.updated}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('portfolio_import.stat_updated')}</p>
			</div>
		</div>

		<div class="flex gap-3">
			<a
				href="{base}/portfolio"
				class="flex-1 py-2.5 rounded-xl font-medium text-sm text-center transition-colors
				       bg-blue-600 hover:bg-blue-700 text-white"
			>
				{$_('portfolio_import.back')}
			</a>
			<button
				onclick={resetAll}
				class="flex-1 py-2.5 rounded-xl font-medium text-sm transition-colors
				       border border-gray-300 dark:border-slate-600
				       hover:bg-gray-100 dark:hover:bg-slate-700"
			>
				{$_('import.restart')}
			</button>
		</div>
	</div>
{/if}
