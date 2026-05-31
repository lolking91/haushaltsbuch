<script lang="ts">
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { accountsApi } from '$lib/api/accounts.js';
	import { ApiError } from '$lib/api/client.js';
	import { validateIban } from '$lib/utils/format.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- State -------------------------------------------------------------------

	let account = $state(data.account);

	/** Editable fields – kept in sync with the form inputs. */
	let form = $state({
		name: account.name,
		bankName: account.bankName,
		iban: account.iban ?? '',
		balance: account.balance != null ? String(account.balance) : '',
		currency: account.currency
	});

	/** Snapshot of the last saved values – used to detect dirty state and reset. */
	let saved = $state({ ...form });

	let saving = $state(false);
	let saveStatus: 'idle' | 'success' | 'error' = $state('idle');
	/** Field-level errors received from the server (field name → message). */
	let serverFieldErrors = $state<Record<string, string>>({});

	// --- Derived -----------------------------------------------------------------

	/** Live IBAN validation — only active when the field has content. */
	let ibanValidation = $derived(validateIban(form.iban));

	let isDirty = $derived(
		form.name !== saved.name ||
		form.bankName !== saved.bankName ||
		form.iban !== saved.iban ||
		form.balance !== saved.balance
	);

	let canSave = $derived(
		isDirty &&
		form.name.trim().length > 0 &&
		form.bankName.trim().length > 0 &&
		ibanValidation === null
	);

	// --- Actions -----------------------------------------------------------------

	/** Resets the form to the last saved state. */
	function reset() {
		form.name = saved.name;
		form.bankName = saved.bankName;
		form.iban = saved.iban;
		form.balance = saved.balance;
		saveStatus = 'idle';
		serverFieldErrors = {};
	}

	/** Persists editable fields via PUT and updates the saved snapshot on success. */
	async function save() {
		saving = true;
		saveStatus = 'idle';
		serverFieldErrors = {};
		try {
			const updated = await accountsApi.update(account.id, {
				name: form.name.trim(),
				bankName: form.bankName.trim(),
				iban: form.iban.trim() || undefined,
				currency: form.currency,
				balance: form.balance !== '' ? Number(form.balance) : undefined
			});
			account = updated;
			saved = { ...form };
			saveStatus = 'success';
		} catch (e: unknown) {
			if (e instanceof ApiError && e.status === 400) {
				try {
					serverFieldErrors = JSON.parse(e.message) as Record<string, string>;
					// Show generic error banner only if no field-level errors were parsed
					if (Object.keys(serverFieldErrors).length === 0) saveStatus = 'error';
				} catch {
					saveStatus = 'error';
				}
			} else {
				saveStatus = 'error';
			}
		} finally {
			saving = false;
		}
	}

	// --- Delete state ------------------------------------------------------------

	let confirmingDelete = $state(false);
	let deleteError = $state('');
	let deleting = $state(false);

	async function deleteAccount() {
		deleting = true;
		deleteError = '';
		try {
			await accountsApi.delete(account.id);
			await goto(`${base}/accounts`);
		} catch (e: unknown) {
			if (e instanceof ApiError && e.status === 409) {
				deleteError = $_('account_detail.delete_has_transactions');
			} else {
				deleteError = $_('common.delete_error');
			}
			deleting = false;
			confirmingDelete = false;
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
	<div class="flex items-center gap-3">
		<Icon icon="heroicons:building-library"
		      class="w-6 h-6 text-blue-500 dark:text-blue-400 shrink-0" />
		<div>
			<h1 class="text-2xl font-bold text-gray-900 dark:text-white">{account.name}</h1>
			<p class="text-sm text-gray-400 dark:text-slate-500 mt-0.5">
				{$_('common.field_id')}: {account.id}
				&nbsp;·&nbsp;{account.bankName}
				{#if account.iban}
					&nbsp;·&nbsp;<span class="font-mono">{account.iban}</span>
				{/if}
			</p>
		</div>
	</div>

	<!-- Edit card -->
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
	            dark:border-slate-700 shadow-sm max-w-lg">

		<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
			<Icon icon="heroicons:pencil-square" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
			<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
				{$_('common.section_edit')}
			</h2>
		</div>

		<form
			onsubmit={(e) => { e.preventDefault(); save(); }}
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
				<input
					id="balance"
					type="number"
					step="0.01"
					bind:value={form.balance}
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- Status message -->
			{#if saveStatus === 'success'}
				<p class="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
					<Icon icon="heroicons:check-circle" class="w-4 h-4" />
					{$_('common.save_success')}
				</p>
			{:else if saveStatus === 'error'}
				<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
					<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
					{$_('common.save_error')}
				</p>
			{/if}

			<!-- Action buttons -->
			<div class="flex justify-end gap-2 pt-1">
				<button
					type="button"
					onclick={reset}
					disabled={!isDirty || saving}
					class="px-4 py-2 rounded-lg text-sm border border-gray-300 dark:border-slate-600
					       text-gray-600 dark:text-slate-300
					       hover:bg-gray-50 dark:hover:bg-slate-700 transition-colors
					       disabled:opacity-40 disabled:cursor-not-allowed"
				>
					{$_('common.btn_reset')}
				</button>
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
					{$_('common.btn_save')}
				</button>
			</div>

		</form>
	</div>

	<!-- Delete section -->
	<div class="max-w-lg">
		{#if confirmingDelete}
			<!-- Confirmation card -->
			<div class="rounded-xl border border-red-300 dark:border-red-800
			            bg-red-50 dark:bg-red-950/20 p-5 space-y-4">
				<div class="flex items-start gap-3">
					<Icon icon="heroicons:exclamation-circle"
					      class="w-5 h-5 text-red-500 shrink-0 mt-0.5" />
					<div>
						<p class="font-semibold text-sm text-red-700 dark:text-red-300">
							{$_('accounts.delete_confirm')}
						</p>
						<p class="text-sm text-red-600 dark:text-red-400 mt-0.5">
							{$_('account_detail.delete_confirm_text')}
						</p>
					</div>
				</div>

				{#if deleteError}
					<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
						<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
						{deleteError}
					</p>
				{/if}

				<div class="flex gap-2 justify-end">
					<button
						onclick={() => (confirmingDelete = false)}
						disabled={deleting}
						class="px-4 py-2 rounded-lg text-sm border border-gray-300 dark:border-slate-600
						       text-gray-600 dark:text-slate-300
						       hover:bg-white dark:hover:bg-slate-700 transition-colors
						       disabled:opacity-40 disabled:cursor-not-allowed"
					>
						{$_('common.btn_cancel')}
					</button>
					<button
						onclick={deleteAccount}
						disabled={deleting}
						class="px-4 py-2 rounded-lg text-sm font-medium
						       bg-red-600 hover:bg-red-700 text-white transition-colors
						       disabled:opacity-40 disabled:cursor-not-allowed
						       flex items-center gap-1.5"
					>
						{#if deleting}
							<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
						{/if}
						{$_('account_detail.delete_confirm_yes')}
					</button>
				</div>
			</div>

		{:else}
			<!-- Delete trigger button -->
			<button
				onclick={() => (confirmingDelete = true)}
				class="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium
				       border border-red-300 dark:border-red-800
				       text-red-600 dark:text-red-400
				       hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
			>
				<Icon icon="heroicons:trash" class="w-4 h-4" />
				{$_('account_detail.btn_delete')}
			</button>
		{/if}
	</div>

</div>
