<script lang="ts">
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { categoryRulesApi } from '$lib/api/categoryRules.js';
	import { NumberInput } from '$lib/components/ui/number-input/index.js';
	import type { ConditionField, ConditionMatcher, ConditionOperator } from '$lib/types/types.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- Form state ---

	type ConditionForm = { field: ConditionField; matcher: ConditionMatcher; value: string };

	let form = $state({
		name: '',
		categoryId: null as number | null,
		priority: 0,
		active: true,
		conditionOperator: 'ALL' as ConditionOperator,
		conditions: [{ field: 'COUNTERPARTY_NAME', matcher: 'CONTAINS', value: '' }] as ConditionForm[]
	});

	let saving = $state(false);
	let errorMessage = $state('');

	// --- Derived ---

	let canSave = $derived(
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

	// --- Submit ---

	async function create() {
		saving = true;
		errorMessage = '';
		try {
			const created = await categoryRulesApi.create({
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
			await goto(`${base}/category-rules/${created.id}`);
		} catch {
			errorMessage = $_('category_rules.create_error');
			saving = false;
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

	<h1 class="text-2xl font-bold text-gray-900 dark:text-white">
		{$_('category_rules.create_title')}
	</h1>

	<!-- Form card -->
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
	            dark:border-slate-700 shadow-sm max-w-2xl">

		<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
			<Icon icon="heroicons:funnel" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
			<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
				{$_('category_rules.btn_create')}
			</h2>
		</div>

		<form
			onsubmit={(e) => { e.preventDefault(); create(); }}
			class="px-5 py-4 space-y-4"
		>
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
					autofocus
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
				<select
					id="category"
					bind:value={form.categoryId}
					required
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				>
					<option value={null} disabled>— {$_('category_rules.field_category')} wählen —</option>
					{#each data.categories as cat (cat.id)}
						<option value={cat.id}>{cat.name}</option>
					{/each}
				</select>
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

			<!-- Conditions section -->
			<div class="space-y-3 pt-1">
				<div class="flex items-center justify-between">
					<p class="text-xs font-semibold text-gray-500 dark:text-slate-400 uppercase tracking-wide">
						{$_('category_rules.section_conditions')}
					</p>
					<!--
						Operator toggle — only meaningful with ≥2 conditions, but always visible
						so the user can set it before adding the second condition.
					-->
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
					<!-- Connector badge between conditions (not before the first) -->
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
						<!-- Field -->
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

						<!-- Matcher -->
						<select
							bind:value={condition.matcher}
							class="w-36 px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
							       bg-white dark:bg-slate-900 text-sm outline-none
							       focus:ring-2 focus:ring-blue-500"
						>
							<option value="CONTAINS">{$_('category_rules.matcher_contains')}</option>
							<option value="EXACT">{$_('category_rules.matcher_exact')}</option>
						</select>

						<!-- Value — select for TRANSACTION_TYPE, text input otherwise -->
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

						<!-- Remove button (hidden if only one condition) -->
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

			<!-- Error message -->
			{#if errorMessage}
				<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
					<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
					{errorMessage}
				</p>
			{/if}

			<!-- Submit -->
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
					{$_('category_rules.btn_create')}
				</button>
			</div>

		</form>
	</div>

</div>
