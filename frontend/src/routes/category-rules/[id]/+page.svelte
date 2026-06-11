<script lang="ts">
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { categoryRulesApi } from '$lib/api/categoryRules.js';
	import { CategorySelect } from '$lib/components/ui/category-select/index.js';
	import { NumberInput } from '$lib/components/ui/number-input/index.js';
	import { SaveButton } from '$lib/components/ui/save-button/index.js';
	import type { ConditionField, ConditionMatcher, ConditionOperator } from '$lib/types/types.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- State ---

	type ConditionForm = { field: ConditionField; matcher: ConditionMatcher; value: string };

	/** Converts the loaded rule into a flat, editable form representation. */
	function toForm(rule: typeof data.rule) {
		return {
			name: rule.name ?? '',
			categoryId: rule.categoryId as number | null,
			priority: rule.priority,
			active: rule.active,
			conditionOperator: (rule.conditionOperator ?? 'ALL') as ConditionOperator,
			conditions: rule.conditions.map((c) => ({
				field: c.field,
				matcher: c.matcher,
				value: c.value
			})) as ConditionForm[]
		};
	}

	let form = $state(toForm(data.rule));
	let saved = $state(toForm(data.rule));

	let saving = $state(false);
	let saveStatus: 'idle' | 'success' | 'error' = $state('idle');

	// --- Derived ---

	let isDirty = $derived(JSON.stringify(form) !== JSON.stringify(saved));

	let canSave = $derived(
		isDirty &&
		form.categoryId !== null &&
		form.conditions.length > 0 &&
		form.conditions.every((c) => c.value.trim().length > 0)
	);

	// --- Condition helpers ---

	function addCondition() {
		form.conditions.push({ field: 'COUNTERPARTY_NAME', matcher: 'CONTAINS', value: '' });
	}

	function removeCondition(index: number) {
		form.conditions.splice(index, 1);
	}

	// --- Actions ---

	function reset() {
		form = toForm(data.rule);
		saveStatus = 'idle';
	}

	async function save() {
		saving = true;
		saveStatus = 'idle';
		try {
			await categoryRulesApi.update(data.rule.id, {
				categoryId: form.categoryId!,
				name: form.name.trim() || null,
				priority: form.priority,
				active: form.active,
				conditionOperator: form.conditionOperator,
				conditions: form.conditions.map((c) => ({
					field: c.field,
					matcher: c.matcher,
					value: c.value.trim()
				}))
			});
			saved = toForm({ ...data.rule, ...form, name: form.name.trim() || null });
			saveStatus = 'success';
		} catch {
			saveStatus = 'error';
		} finally {
			saving = false;
		}
	}

	async function saveAndNew() {
		await save();
		if (saveStatus !== 'error') await goto(`${base}/category-rules/new`);
	}

	// --- Delete state ---

	let confirmingDelete = $state(false);
	let deleteError = $state('');
	let deleting = $state(false);

	async function deleteRule() {
		deleting = true;
		deleteError = '';
		try {
			await categoryRulesApi.delete(data.rule.id);
			await goto(`${base}/category-rules`);
		} catch {
			deleteError = $_('common.delete_error');
			deleting = false;
			confirmingDelete = false;
		}
	}
</script>

<div class="space-y-5">

	<!-- Back link -->
	<a
		href="{base}/category-rules"
		class="inline-flex items-center gap-1.5 text-sm text-gray-500 dark:text-slate-400
		       hover:text-gray-900 dark:hover:text-white transition-colors"
	>
		<Icon icon="heroicons:arrow-left" class="w-4 h-4" />
		{$_('common.back_to_overview')}
	</a>

	<!-- Page header -->
	<h1 class="text-2xl font-bold text-gray-900 dark:text-white">
		{data.rule.name ?? data.rule.categoryName}
	</h1>

	<!-- Edit card -->
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
	            dark:border-slate-700 shadow-sm max-w-2xl">

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
			<!-- ID (read-only) -->
			<div>
				<p class="text-xs font-medium text-gray-400 dark:text-slate-500">
					{$_('common.field_id')}: {data.rule.id}
				</p>
			</div>

			<!-- Label (optional) -->
			<div>
				<label for="name" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('category_rules.field_name')}
				</label>
				<input
					id="name"
					type="text"
					bind:value={form.name}
					placeholder="z.B. Amazon → Shopping"
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- Category -->
			<div>
				<label for="category" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('category_rules.field_category')} *
				</label>
				<CategorySelect
					id="category"
					bind:value={form.categoryId}
					categories={data.categories}
					class="w-full"
				/>
			</div>

			<!-- Priority + Active (inline row) -->
			<div class="flex items-end gap-4">
				<div class="flex-1">
					<label for="priority" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
						{$_('category_rules.field_priority')}
					</label>
					<NumberInput id="priority" bind:value={form.priority} class="w-full" />
					<p class="text-xs text-gray-400 dark:text-slate-500 mt-1">
						{$_('category_rules.field_priority_hint')}
					</p>
				</div>
				<label class="flex items-center gap-2 cursor-pointer pb-7 select-none">
					<input type="checkbox" bind:checked={form.active} class="w-4 h-4 rounded accent-blue-600" />
					<span class="text-sm text-gray-700 dark:text-slate-300">{$_('category_rules.field_active')}</span>
				</label>
			</div>

			<!-- Conditions -->
			<div class="space-y-3 pt-1">
				<div class="flex items-center justify-between">
					<p class="text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wide">
						{$_('category_rules.section_conditions')}
					</p>
					<div class="flex rounded-lg overflow-hidden border border-gray-300 dark:border-slate-600 text-xs font-medium">
						<button
							type="button"
							onclick={() => (form.conditionOperator = 'ALL')}
							class="px-3 py-1.5 transition-colors
							       {form.conditionOperator === 'ALL'
							         ? 'bg-blue-600 text-white'
							         : 'text-gray-500 dark:text-slate-400 hover:bg-gray-50 dark:hover:bg-slate-700'}"
						>
							{$_('category_rules.operator_all')}
						</button>
						<button
							type="button"
							onclick={() => (form.conditionOperator = 'ANY')}
							class="px-3 py-1.5 border-l border-gray-300 dark:border-slate-600 transition-colors
							       {form.conditionOperator === 'ANY'
							         ? 'bg-blue-600 text-white'
							         : 'text-gray-500 dark:text-slate-400 hover:bg-gray-50 dark:hover:bg-slate-700'}"
						>
							{$_('category_rules.operator_any')}
						</button>
					</div>
				</div>

				{#each form.conditions as condition, i (i)}
					{#if i > 0}
						<div class="flex items-center gap-2">
							<span class="text-xs font-semibold px-2 py-0.5 rounded-full
							             {form.conditionOperator === 'ALL'
							               ? 'bg-gray-100 dark:bg-slate-700 text-gray-500 dark:text-slate-400'
							               : 'bg-blue-100 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400'}">
								{form.conditionOperator === 'ALL' ? 'UND' : 'ODER'}
							</span>
							<span class="flex-1 border-t border-dashed border-gray-200 dark:border-slate-700"></span>
						</div>
					{/if}
					<div class="flex items-start gap-2">
						<select
							bind:value={condition.field}
							class="flex-1 px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
							       bg-white dark:bg-slate-900 text-sm outline-none
							       focus:ring-2 focus:ring-blue-500"
						>
							<option value="COUNTERPARTY_NAME">{$_('category_rules.field_counterparty_name')}</option>
							<option value="DESCRIPTION">{$_('category_rules.field_description')}</option>
							<option value="TRANSACTION_TYPE">{$_('category_rules.field_transaction_type')}</option>
						</select>

						<select
							bind:value={condition.matcher}
							class="w-36 px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
							       bg-white dark:bg-slate-900 text-sm outline-none
							       focus:ring-2 focus:ring-blue-500"
						>
							<option value="CONTAINS">{$_('category_rules.matcher_contains')}</option>
							<option value="EXACT">{$_('category_rules.matcher_exact')}</option>
						</select>

						{#if condition.field === 'TRANSACTION_TYPE'}
							<select
								bind:value={condition.value}
								class="flex-1 px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm outline-none
								       focus:ring-2 focus:ring-blue-500"
							>
								<option value="">—</option>
								<option value="INCOME">{$_('transactions.badge_income')}</option>
								<option value="EXPENSE">{$_('transactions.badge_expense')}</option>
							</select>
						{:else}
							<input
								type="text"
								bind:value={condition.value}
								placeholder={$_('category_rules.condition_value')}
								class="flex-1 px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm outline-none
								       focus:ring-2 focus:ring-blue-500"
							/>
						{/if}

						<button
							type="button"
							onclick={() => removeCondition(i)}
							disabled={form.conditions.length === 1}
							class="p-2 rounded-lg text-gray-400 dark:text-slate-500
							       hover:text-red-600 dark:hover:text-red-400
							       hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors
							       disabled:opacity-30 disabled:cursor-not-allowed"
							aria-label="Bedingung entfernen"
						>
							<Icon icon="heroicons:minus-circle" class="w-4 h-4" />
						</button>
					</div>
				{/each}

				<button
					type="button"
					onclick={addCondition}
					class="inline-flex items-center gap-1.5 text-sm text-blue-600 dark:text-blue-400
					       hover:text-blue-700 dark:hover:text-blue-300 transition-colors"
				>
					<Icon icon="heroicons:plus-circle" class="w-4 h-4" />
					{$_('category_rules.btn_add_condition')}
				</button>
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
				<SaveButton
					label={$_('common.btn_save')}
					disabled={!canSave || saving}
					{saving}
					onsaveandnew={saveAndNew}
				/>
			</div>

		</form>
	</div>

	<!-- Delete section -->
	<div class="max-w-2xl">
		{#if confirmingDelete}
			<div class="rounded-xl border border-red-300 dark:border-red-800
			            bg-red-50 dark:bg-red-950/20 p-5 space-y-4">
				<div class="flex items-start gap-3">
					<Icon icon="heroicons:exclamation-circle"
					      class="w-5 h-5 text-red-500 shrink-0 mt-0.5" />
					<div>
						<p class="font-semibold text-sm text-red-700 dark:text-red-300">
							{$_('categories.delete_confirm')}
						</p>
						<p class="text-sm text-red-600 dark:text-red-400 mt-0.5">
							{$_('category_detail.delete_confirm_text')}
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
						onclick={deleteRule}
						disabled={deleting}
						class="px-4 py-2 rounded-lg text-sm font-medium
						       bg-red-600 hover:bg-red-700 text-white transition-colors
						       disabled:opacity-40 disabled:cursor-not-allowed
						       flex items-center gap-1.5"
					>
						{#if deleting}
							<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
						{/if}
						{$_('category_detail.delete_confirm_yes')}
					</button>
				</div>
			</div>

		{:else}
			<button
				onclick={() => (confirmingDelete = true)}
				class="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium
				       border border-red-300 dark:border-red-800
				       text-red-600 dark:text-red-400
				       hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
			>
				<Icon icon="heroicons:trash" class="w-4 h-4" />
				{$_('category_rules.btn_delete')}
			</button>
		{/if}
	</div>

</div>
