<script lang="ts">
	import Icon from '@iconify/svelte';
	import { base } from '$app/paths';
	import { _ } from 'svelte-i18n';
	import { Badge } from '$lib/components/ui/badge/index.js';
	import { Amount } from '$lib/components/ui/amount/index.js';
	import { formatDate } from '$lib/utils/format.js';
	import { transactionsApi } from '$lib/api/transactions.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- State ---

	let transaction = $state(data.transaction);

	/** Editable fields – kept in sync with the form inputs. */
	let form = $state({
		counterpartyName: transaction.counterpartyName ?? '',
		description: transaction.description ?? '',
		bookingText: transaction.bookingText ?? ''
	});

	/** Snapshot of the last saved values – used to detect dirty state and to reset. */
	let saved = $state({
		counterpartyName: transaction.counterpartyName ?? '',
		description: transaction.description ?? '',
		bookingText: transaction.bookingText ?? ''
	});

	let saving = $state(false);
	let saveStatus: 'idle' | 'success' | 'error' = $state('idle');

	// --- Derived ---

	let isDirty = $derived(
		form.counterpartyName !== saved.counterpartyName ||
		form.description !== saved.description ||
		form.bookingText !== saved.bookingText
	);

	// --- Actions ---

	/** Resets the form to the last saved state. */
	function reset() {
		form.counterpartyName = saved.counterpartyName;
		form.description = saved.description;
		form.bookingText = saved.bookingText;
		saveStatus = 'idle';
	}

	/** Persists editable fields via PATCH and updates the saved snapshot on success. */
	async function save() {
		saving = true;
		saveStatus = 'idle';
		try {
			const updated = await transactionsApi.update(transaction.id, {
				counterpartyName: form.counterpartyName || null,
				description: form.description || null,
				bookingText: form.bookingText || null
			});
			transaction = updated;
			saved.counterpartyName = form.counterpartyName;
			saved.description = form.description;
			saved.bookingText = form.bookingText;
			saveStatus = 'success';
		} catch {
			saveStatus = 'error';
		} finally {
			saving = false;
		}
	}
</script>

<div class="space-y-5">

	<!-- Back link -->
	<a
		href="{base}/transactions"
		class="inline-flex items-center gap-1.5 text-sm text-gray-500 dark:text-slate-400
		       hover:text-gray-900 dark:hover:text-white transition-colors"
	>
		<Icon icon="heroicons:arrow-left" class="w-4 h-4" />
		{$_('transaction_detail.back')}
	</a>

	<!-- Page header: identity + amount -->
	<div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
		<div>
			<h1 class="text-2xl font-bold text-gray-900 dark:text-white">
				{transaction.counterpartyName ?? `#${transaction.id}`}
			</h1>
			<p class="text-sm text-gray-400 dark:text-slate-500 mt-0.5">
				{$_('transaction_detail.field_id')}: {transaction.id}
				&nbsp;·&nbsp;
				{transaction.account.name}
			</p>
		</div>
		<div class="flex items-center gap-3">
			<Badge variant={transaction.type === 'INCOME' ? 'income' : 'expense'}>
				{transaction.type === 'INCOME'
					? $_('transactions.badge_income')
					: $_('transactions.badge_expense')}
			</Badge>
			<Amount amount={transaction.amount} currency={transaction.currency} type={transaction.type} class="text-2xl font-bold" />
		</div>
	</div>

	<!-- Two-column layout on lg+ -->
	<div class="grid grid-cols-1 lg:grid-cols-2 gap-5">

		<!-- ── Read-only card ─────────────────────────────────────────── -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm">

			<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
				<Icon icon="heroicons:lock-closed" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
				<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
					{$_('transaction_detail.section_bank_data')}
				</h2>
			</div>

			<dl class="px-5 py-4 grid grid-cols-2 gap-x-6 gap-y-4">

				<div>
					<dt class="text-xs font-medium text-gray-400 dark:text-slate-500">
						{$_('transaction_detail.field_booking_date')}
					</dt>
					<dd class="mt-0.5 text-sm text-gray-900 dark:text-white">
						{formatDate(transaction.bookingDate)}
					</dd>
				</div>

				<div>
					<dt class="text-xs font-medium text-gray-400 dark:text-slate-500">
						{$_('transaction_detail.field_value_date')}
					</dt>
					<dd class="mt-0.5 text-sm text-gray-900 dark:text-white">
						{formatDate(transaction.valueDate)}
					</dd>
				</div>

				<div>
					<dt class="text-xs font-medium text-gray-400 dark:text-slate-500">
						{$_('transaction_detail.field_amount')}
					</dt>
					<dd class="mt-0.5">
						<Amount amount={transaction.amount} currency={transaction.currency} type={transaction.type} class="text-sm font-medium" />
					</dd>
				</div>

				<div>
					<dt class="text-xs font-medium text-gray-400 dark:text-slate-500">
						{$_('transaction_detail.field_type')}
					</dt>
					<dd class="mt-0.5">
						<Badge variant={transaction.type === 'INCOME' ? 'income' : 'expense'}>
							{transaction.type === 'INCOME'
								? $_('transactions.badge_income')
								: $_('transactions.badge_expense')}
						</Badge>
					</dd>
				</div>

				<div class="col-span-2">
					<dt class="text-xs font-medium text-gray-400 dark:text-slate-500">
						{$_('transaction_detail.field_account')}
					</dt>
					<dd class="mt-0.5 text-sm text-gray-900 dark:text-white">
						{transaction.account.name}
						<span class="text-gray-400 dark:text-slate-500 text-xs ml-1">
							({transaction.account.bankName})
						</span>
					</dd>
				</div>

			</dl>

			<div class="px-5 pb-4">
				<p class="text-xs text-gray-400 dark:text-slate-500 flex items-start gap-1.5">
					<Icon icon="heroicons:information-circle" class="w-3.5 h-3.5 mt-0.5 shrink-0" />
					{$_('transaction_detail.read_only_hint')}
				</p>
			</div>

		</div>

		<!-- ── Edit card ──────────────────────────────────────────────── -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm">

			<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
				<Icon icon="heroicons:pencil-square" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
				<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
					{$_('transaction_detail.section_edit')}
				</h2>
			</div>

			<form
				onsubmit={(e) => { e.preventDefault(); save(); }}
				class="px-5 py-4 space-y-4"
			>

				<!-- Counterparty -->
				<div>
					<label
						for="counterpartyName"
						class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1"
					>
						{$_('transaction_detail.field_counterparty')}
					</label>
					<input
						id="counterpartyName"
						type="text"
						bind:value={form.counterpartyName}
						class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
						       bg-white dark:bg-slate-900 text-sm outline-none
						       focus:ring-2 focus:ring-blue-500"
					/>
				</div>

				<!-- Description / purpose -->
				<div>
					<label
						for="description"
						class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1"
					>
						{$_('transaction_detail.field_description')}
					</label>
					<textarea
						id="description"
						bind:value={form.description}
						rows="3"
						class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
						       bg-white dark:bg-slate-900 text-sm outline-none resize-none
						       focus:ring-2 focus:ring-blue-500"
					></textarea>
				</div>

				<!-- Booking text -->
				<div>
					<label
						for="bookingText"
						class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1"
					>
						{$_('transaction_detail.field_booking_text')}
					</label>
					<input
						id="bookingText"
						type="text"
						bind:value={form.bookingText}
						class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
						       bg-white dark:bg-slate-900 text-sm outline-none
						       focus:ring-2 focus:ring-blue-500"
					/>
				</div>

				<!-- Status message -->
				{#if saveStatus === 'success'}
					<p class="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
						<Icon icon="heroicons:check-circle" class="w-4 h-4" />
						{$_('transaction_detail.save_success')}
					</p>
				{:else if saveStatus === 'error'}
					<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
						<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
						{$_('transaction_detail.save_error')}
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
						{$_('transaction_detail.btn_reset')}
					</button>
					<button
						type="submit"
						disabled={!isDirty || saving}
						class="px-4 py-2 rounded-lg text-sm font-medium
						       bg-blue-600 hover:bg-blue-700 text-white transition-colors
						       disabled:opacity-40 disabled:cursor-not-allowed
						       flex items-center gap-1.5"
					>
						{#if saving}
							<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
						{/if}
						{$_('transaction_detail.btn_save')}
					</button>
				</div>

			</form>
		</div>

	</div>
</div>
