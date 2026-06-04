<script lang="ts">
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { accountsApi } from '$lib/api/accounts.js';
	import { NumberInput } from '$lib/components/ui/number-input/index.js';
	import { ApiError } from '$lib/api/client.js';
	import { validateIban } from '$lib/utils/format.js';

	// --- Form state --------------------------------------------------------------

	let form = $state({
		name: '',
		bankName: '',
		iban: '',
		balance: '' as string,
		currency: 'EUR' as 'EUR'
	});

	let saving = $state(false);
	/** Generic fallback error shown when the server returns no field-level detail. */
	let errorMessage = $state('');
	/** Field-level errors received from the server (field name → message). */
	let serverFieldErrors = $state<Record<string, string>>({});

	// --- Derived -----------------------------------------------------------------

	/** Live IBAN validation — only shown when the field has content. */
	let ibanValidation = $derived(validateIban(form.iban));

	/** The Save button is disabled when required fields are empty or the IBAN is invalid. */
	let canSave = $derived(
		form.name.trim().length > 0 &&
		form.bankName.trim().length > 0 &&
		ibanValidation === null
	);

	// --- Actions -----------------------------------------------------------------

	async function create() {
		saving = true;
		errorMessage = '';
		serverFieldErrors = {};
		try {
			const created = await accountsApi.create({
				name: form.name.trim(),
				bankName: form.bankName.trim(),
				iban: form.iban.trim() || undefined,
				currency: form.currency,
				balance: form.balance !== '' ? Number(form.balance) : undefined
			});
			await goto(`${base}/accounts/${created.id}`);
		} catch (e: unknown) {
			if (e instanceof ApiError && e.status === 400) {
				// Try to parse the structured field-error JSON from the backend
				try {
					serverFieldErrors = JSON.parse(e.message) as Record<string, string>;
				} catch {
					errorMessage = $_('account_create.create_error');
				}
			} else {
				errorMessage = $_('account_create.create_error');
			}
			saving = false;
		}
	}
</script>

<div class="space-y-5">

	<!-- Back link -->
	<a
		href="{base}/accounts"
		class="inline-flex items-center gap-1.5 text-sm text-gray-500 dark:text-slate-400
		       hover:text-gray-900 dark:hover:text-white transition-colors"
	>
		<Icon icon="heroicons:arrow-left" class="w-4 h-4" />
		{$_('common.back_to_overview')}
	</a>

	<!-- Page header -->
	<h1 class="text-2xl font-bold text-gray-900 dark:text-white">
		{$_('account_create.title')}
	</h1>

	<!-- Create card -->
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
	            dark:border-slate-700 shadow-sm max-w-lg">

		<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
			<Icon icon="heroicons:building-library" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
			<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
				{$_('account_create.section')}
			</h2>
		</div>

		<form
			onsubmit={(e) => { e.preventDefault(); create(); }}
			class="px-5 py-4 space-y-4"
		>
			<!-- Account name -->
			<div>
				<label for="name" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('accounts.field_name')} *
				</label>
				<input
					id="name"
					type="text"
					bind:value={form.name}
					required
					autofocus
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- Bank name -->
			<div>
				<label for="bankName" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('accounts.field_bank_name')} *
				</label>
				<input
					id="bankName"
					type="text"
					bind:value={form.bankName}
					required
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- IBAN with live validation -->
			<div>
				<label for="iban" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('accounts.field_iban')}
				</label>
				<input
					id="iban"
					type="text"
					bind:value={form.iban}
					placeholder={$_('accounts.iban_hint')}
					class="w-full px-3 py-2 rounded-lg border text-sm font-mono outline-none
					       focus:ring-2 placeholder:font-sans
					       {ibanValidation
					         ? 'border-red-400 dark:border-red-600 focus:ring-red-500 bg-red-50 dark:bg-red-950/20'
					         : 'border-gray-300 dark:border-slate-600 focus:ring-blue-500 bg-white dark:bg-slate-900'}"
				/>
				{#if ibanValidation === 'format'}
					<p class="mt-1 text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
						<Icon icon="heroicons:exclamation-circle" class="w-3.5 h-3.5 shrink-0" />
						{$_('accounts.iban_error_format')}
					</p>
				{:else if ibanValidation === 'checksum'}
					<p class="mt-1 text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
						<Icon icon="heroicons:exclamation-circle" class="w-3.5 h-3.5 shrink-0" />
						{$_('accounts.iban_error_checksum')}
					</p>
				{:else if serverFieldErrors.iban}
					<!-- Server-side IBAN error (fallback if client validation was bypassed) -->
					<p class="mt-1 text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
						<Icon icon="heroicons:exclamation-circle" class="w-3.5 h-3.5 shrink-0" />
						{serverFieldErrors.iban}
					</p>
				{/if}
			</div>

			<!-- Balance -->
			<div>
				<label for="balance" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('accounts.field_balance')}
				</label>
				<NumberInput id="balance" step={0.01} bind:value={form.balance} class="w-full" />
			</div>

			<!-- Generic error message -->
			{#if errorMessage}
				<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
					<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
					{errorMessage}
				</p>
			{/if}

			<!-- Action button -->
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
					{$_('account_create.btn_create')}
				</button>
			</div>

		</form>
	</div>

</div>
