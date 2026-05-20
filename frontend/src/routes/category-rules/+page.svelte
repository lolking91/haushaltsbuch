<script lang="ts">
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { categoryRulesApi } from '$lib/api/categoryRules.js';
	import type { CategoryRule, ConditionField, ConditionMatcher } from '$lib/types/types.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	let rules: CategoryRule[] = $state(data.rules);

	// --- Delete state ---

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
			await categoryRulesApi.delete(id);
			rules = rules.filter((r) => r.id !== id);
			confirmingDeleteId = null;
		} catch {
			deleteError = $_('common.delete_error');
		}
	}

	// --- Apply rules ---

	let applying = $state(false);
	let applyMessage = $state('');
	let applyError = $state('');

	async function applyRules() {
		applying = true;
		applyMessage = '';
		applyError = '';
		try {
			const result = await categoryRulesApi.apply();
			applyMessage = $_('category_rules.apply_result', { values: { count: result.categorized } });
		} catch {
			applyError = $_('category_rules.apply_error');
		} finally {
			applying = false;
		}
	}

	// --- Condition label helpers ---

	const FIELD_KEYS: Record<ConditionField, string> = {
		COUNTERPARTY_NAME: 'category_rules.field_counterparty_name',
		DESCRIPTION:       'category_rules.field_description',
		TRANSACTION_TYPE:  'category_rules.field_transaction_type'
	};

	const MATCHER_KEYS: Record<ConditionMatcher, string> = {
		EXACT:    'category_rules.matcher_exact',
		CONTAINS: 'category_rules.matcher_contains'
	};
</script>

<div class="space-y-5">

	<!-- Page header -->
	<div class="flex items-center justify-between">
		<h1 class="text-2xl font-bold">{$_('nav.rules')}</h1>
		<div class="flex items-center gap-2">
			<!-- Apply to existing transactions -->
			<button
				onclick={applyRules}
				disabled={applying || rules.length === 0}
				class="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium
				       border border-gray-300 dark:border-slate-600
				       text-gray-700 dark:text-slate-200
				       hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors
				       disabled:opacity-40 disabled:cursor-not-allowed"
			>
				{#if applying}
					<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
				{:else}
					<Icon icon="heroicons:play" class="w-4 h-4" />
				{/if}
				{$_('category_rules.btn_apply')}
			</button>

			<a
				href="{base}/category-rules/new"
				class="inline-flex items-center gap-2 px-4 py-2 rounded-xl
				       bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
			>
				<Icon icon="heroicons:plus" class="w-4 h-4" />
				{$_('category_rules.btn_new')}
			</a>
		</div>
	</div>

	<!-- Apply feedback -->
	{#if applyMessage}
		<p class="text-sm text-green-600 dark:text-green-400 flex items-center gap-1.5">
			<Icon icon="heroicons:check-circle" class="w-4 h-4" />
			{applyMessage}
		</p>
	{:else if applyError}
		<p class="text-sm text-red-600 dark:text-red-400 flex items-center gap-1.5">
			<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
			{applyError}
		</p>
	{/if}

	<!-- Empty state -->
	{#if rules.length === 0}
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700
		            shadow-sm p-12 flex flex-col items-center gap-4 text-center">
			<div class="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-950/40 flex items-center justify-center">
				<Icon icon="heroicons:funnel" class="w-8 h-8 text-blue-500 dark:text-blue-400" />
			</div>
			<div>
				<p class="font-semibold text-gray-700 dark:text-slate-300">{$_('category_rules.empty_title')}</p>
				<p class="text-sm text-gray-400 dark:text-slate-500 mt-1">{$_('category_rules.empty_text')}</p>
			</div>
			<a
				href="{base}/category-rules/new"
				class="inline-flex items-center gap-2 px-4 py-2 rounded-xl
				       bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
			>
				<Icon icon="heroicons:plus" class="w-4 h-4" />
				{$_('category_rules.empty_cta')}
			</a>
		</div>

	{:else}
		<!-- Rules list -->
		<div class="space-y-2">
			{#each rules as rule (rule.id)}
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
				            dark:border-slate-700 shadow-sm overflow-hidden">

					{#if confirmingDeleteId === rule.id}
						<!-- Inline delete confirmation -->
						<div class="flex items-center gap-3 px-5 py-4 bg-red-50 dark:bg-red-950/20">
							<span class="flex-1 text-sm font-medium text-red-700 dark:text-red-300">
								{$_('categories.delete_confirm')}
							</span>
							{#if deleteError}
								<span class="text-xs text-red-600 dark:text-red-400">{deleteError}</span>
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
								onclick={() => confirmDelete(rule.id)}
								class="px-3 py-1.5 rounded-lg text-sm font-medium
								       bg-red-600 hover:bg-red-700 text-white transition-colors shrink-0"
							>
								{$_('categories.delete_yes')}
							</button>
						</div>

					{:else}
						<div class="flex items-start gap-4 px-5 py-4">

							<!-- Active indicator -->
							<span
								class="mt-1 w-2 h-2 rounded-full shrink-0 {rule.active
									? 'bg-green-500'
									: 'bg-gray-300 dark:bg-slate-600'}"
								title={rule.active ? 'Aktiv' : 'Inaktiv'}
							></span>

							<!-- Content -->
							<div class="flex-1 min-w-0">
								<!-- Name + category badge -->
								<div class="flex items-center gap-2 flex-wrap">
									<span class="font-medium text-gray-900 dark:text-white">
										{rule.name ?? '—'}
									</span>
									<span class="inline-flex items-center gap-1.5 text-xs px-2 py-0.5 rounded-full
									             bg-blue-50 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300">
										<Icon icon="heroicons:tag" class="w-3 h-3" />
										{rule.categoryName}
									</span>
									{#if rule.priority !== 0}
										<span class="text-xs px-2 py-0.5 rounded-full
										             bg-slate-100 dark:bg-slate-700 text-slate-500 dark:text-slate-400">
											P{rule.priority}
										</span>
									{/if}
								</div>

								<!-- Conditions -->
								<ul class="mt-1.5 space-y-0.5">
									{#each rule.conditions as c}
										<li class="text-sm text-gray-500 dark:text-slate-400">
											{$_(FIELD_KEYS[c.field])}
											<span class="text-gray-400 dark:text-slate-500 mx-1">{$_(MATCHER_KEYS[c.matcher])}</span>
											<span class="font-mono text-xs bg-gray-100 dark:bg-slate-700 px-1.5 py-0.5 rounded">
												{c.value}
											</span>
										</li>
									{/each}
								</ul>
							</div>

							<!-- Actions -->
							<div class="flex items-center gap-1 shrink-0">
								<a
									href="{base}/category-rules/{rule.id}"
									class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
									       hover:text-blue-600 dark:hover:text-blue-400
									       hover:bg-blue-50 dark:hover:bg-blue-950/40 transition-colors"
									aria-label="Regel bearbeiten"
								>
									<Icon icon="heroicons:pencil-square" class="w-4 h-4" />
								</a>
								<button
									onclick={() => requestDelete(rule.id)}
									class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
									       hover:text-red-600 dark:hover:text-red-400
									       hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors"
									aria-label="Regel löschen"
								>
									<Icon icon="heroicons:trash" class="w-4 h-4" />
								</button>
							</div>

						</div>
					{/if}

				</div>
			{/each}
		</div>
	{/if}

</div>
