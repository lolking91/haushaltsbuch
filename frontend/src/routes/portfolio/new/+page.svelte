<script lang="ts">
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { etfsApi } from '$lib/api/etfs.js';
	import { ApiError } from '$lib/api/client.js';

	// --- Form state ---

	let form = $state({
		name: '',
		wkn: '',
		isin: '',
		brokerName: '',
		notes: ''
	});

	let saving = $state(false);
	let errorMessage = $state('');
	let serverFieldErrors = $state<Record<string, string>>({});

	// --- Derived ---

	/** WKN must be exactly 6 alphanumeric characters. */
	let wknValid = $derived(/^[A-Za-z0-9]{6}$/.test(form.wkn.trim()));

	let canSave = $derived(
		form.name.trim().length > 0 &&
		wknValid
	);

	// --- Actions ---

	async function create() {
		saving = true;
		errorMessage = '';
		serverFieldErrors = {};
		try {
			const created = await etfsApi.create({
				name: form.name.trim(),
				wkn: form.wkn.trim().toUpperCase(),
				isin: form.isin.trim() || undefined,
				brokerName: form.brokerName.trim() || undefined,
				notes: form.notes.trim() || undefined
			});
			await goto(`${base}/portfolio/${created.id}`);
		} catch (e: unknown) {
			if (e instanceof ApiError && e.status === 400) {
				try {
					serverFieldErrors = JSON.parse(e.message) as Record<string, string>;
				} catch {
					errorMessage = $_('portfolio.create_error');
				}
			} else if (e instanceof ApiError && e.status === 409) {
				serverFieldErrors = { wkn: e.message };
			} else {
				errorMessage = $_('portfolio.create_error');
			}
			saving = false;
		}
	}
</script>

<div class="space-y-5">

	<!-- Back link -->
	<a
		href="{base}/portfolio"
		class="inline-flex items-center gap-1.5 text-sm text-gray-500 dark:text-slate-400
		       hover:text-gray-900 dark:hover:text-white transition-colors"
	>
		<Icon icon="heroicons:arrow-left" class="w-4 h-4" />
		{$_('common.back_to_overview')}
	</a>

	<h1 class="text-2xl font-bold text-gray-900 dark:text-white">{$_('portfolio.create_title')}</h1>

	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm max-w-lg">

		<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
			<Icon icon="heroicons:chart-bar" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
			<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">{$_('portfolio.create_section')}</h2>
		</div>

		<form
			onsubmit={(e) => { e.preventDefault(); create(); }}
			class="px-5 py-4 space-y-4"
		>
			<!-- Name -->
			<div>
				<label for="name" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('portfolio.field_name')} *
				</label>
				<input
					id="name"
					type="text"
					bind:value={form.name}
					required
					autofocus
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- WKN -->
			<div>
				<label for="wkn" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('portfolio.field_wkn')} *
				</label>
				<input
					id="wkn"
					type="text"
					bind:value={form.wkn}
					placeholder={$_('portfolio.wkn_hint')}
					maxlength="6"
					class="w-full px-3 py-2 rounded-lg border text-sm font-mono outline-none
					       focus:ring-2
					       {form.wkn && !wknValid
					         ? 'border-red-400 dark:border-red-600 focus:ring-red-500 bg-red-50 dark:bg-red-950/20'
					         : 'border-gray-300 dark:border-slate-600 focus:ring-blue-500 bg-white dark:bg-slate-900'}"
				/>
				{#if serverFieldErrors.wkn}
					<p class="mt-1 text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
						<Icon icon="heroicons:exclamation-circle" class="w-3.5 h-3.5 shrink-0" />
						{serverFieldErrors.wkn}
					</p>
				{/if}
			</div>

			<!-- ISIN (optional) -->
			<div>
				<label for="isin" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('portfolio.field_isin')}
				</label>
				<input
					id="isin"
					type="text"
					bind:value={form.isin}
					maxlength="12"
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm font-mono outline-none focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- Broker (optional) -->
			<div>
				<label for="brokerName" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('portfolio.field_broker')}
				</label>
				<input
					id="brokerName"
					type="text"
					bind:value={form.brokerName}
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- Notes (optional) -->
			<div>
				<label for="notes" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('portfolio.field_notes')}
				</label>
				<textarea
					id="notes"
					bind:value={form.notes}
					rows="3"
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500 resize-none"
				></textarea>
			</div>

			<!-- Generic error -->
			{#if errorMessage}
				<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
					<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
					{errorMessage}
				</p>
			{/if}

			<!-- Action -->
			<div class="flex justify-end pt-1">
				<button
					type="submit"
					disabled={!canSave || saving}
					class="px-4 py-2 rounded-lg text-sm font-medium
					       bg-blue-600 hover:bg-blue-700 text-white transition-colors
					       disabled:opacity-40 disabled:cursor-not-allowed
					       flex items-center gap-1.5"
				>
					{#if saving}
						<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
					{/if}
					{$_('portfolio.btn_create')}
				</button>
			</div>
		</form>
	</div>

</div>
