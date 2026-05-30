<script lang="ts">
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { analyticsApi } from '$lib/api/analytics.js';
	import BarChart from '$lib/components/charts/BarChart.svelte';
	import DonutChart from '$lib/components/charts/DonutChart.svelte';
	import HBarChart from '$lib/components/charts/HBarChart.svelte';
	import LineChart from '$lib/components/charts/LineChart.svelte';
	import type { AnalyticsResponse } from '$lib/types/types.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// ── Period filter ─────────────────────────────────────────────────────────

	type Period = 'month' | '3months' | '6months' | 'year' | 'custom';

	let period: Period     = $state('year');
	let customFrom: string = $state(data.defaultFrom);
	let customTo: string   = $state(data.defaultTo);

	// Bound to the date inputs so we can call showPicker() from the calendar icon button.
	let fromInput: HTMLInputElement | undefined = $state();
	let toInput:   HTMLInputElement | undefined = $state();

	/** Computes the ISO date range for a preset period relative to today. */
	function presetRange(p: Exclude<Period, 'custom'>): { from: string; to: string } {
		const today = new Date();
		const to    = today.toISOString().slice(0, 10);

		switch (p) {
			case 'month': {
				const from = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-01`;
				return { from, to };
			}
			case '3months': {
				const d = new Date(today);
				d.setMonth(d.getMonth() - 3);
				return { from: d.toISOString().slice(0, 10), to };
			}
			case '6months': {
				const d = new Date(today);
				d.setMonth(d.getMonth() - 6);
				return { from: d.toISOString().slice(0, 10), to };
			}
			case 'year':
			default:
				return { from: `${today.getFullYear()}-01-01`, to };
		}
	}

	// ── Data loading ──────────────────────────────────────────────────────────

	let analytics: AnalyticsResponse = $state(data.analytics);
	let loading: boolean              = $state(false);
	let loadError: string             = $state('');

	async function loadAnalytics(from: string, to: string) {
		loading   = true;
		loadError = '';
		try {
			analytics = await analyticsApi.getAnalytics(from, to);
		} catch {
			loadError = $_('reports.load_error');
		} finally {
			loading = false;
		}
	}

	function selectPeriod(p: Period) {
		period = p;
		if (p !== 'custom') {
			const range = presetRange(p);
			customFrom  = range.from;
			customTo    = range.to;
			loadAnalytics(range.from, range.to);
		}
	}

	function loadCustomRange() {
		if (customFrom && customTo && customFrom <= customTo) {
			loadAnalytics(customFrom, customTo);
		}
	}

	// ── Derived display values ────────────────────────────────────────────────

	/** Limits the horizontal bar chart to the top 8 categories for readability. */
	let topCategories = $derived(analytics.categoryExpenses.slice(0, 8));

	let hasData = $derived(analytics.totalIncome > 0 || analytics.totalExpenses > 0);

	// ── Formatting ────────────────────────────────────────────────────────────

	function formatCurrency(value: number): string {
		return new Intl.NumberFormat('de-DE', {
			style:                 'currency',
			currency:              'EUR',
			minimumFractionDigits: 2,
			maximumFractionDigits: 2
		}).format(value);
	}

	const PERIODS: { key: Period; label: () => string }[] = [
		{ key: 'month',   label: () => $_('reports.period_month') },
		{ key: '3months', label: () => $_('reports.period_3months') },
		{ key: '6months', label: () => $_('reports.period_6months') },
		{ key: 'year',    label: () => $_('reports.period_year') },
		{ key: 'custom',  label: () => $_('reports.period_custom') }
	];
</script>

<div class="space-y-6">

	<!-- Page header + period filter -->
	<div class="flex flex-col sm:flex-row sm:items-center gap-4">
		<h1 class="text-2xl font-bold shrink-0">{$_('reports.title')}</h1>
		<div class="flex flex-wrap gap-1">
			{#each PERIODS as p}
				<button
					onclick={() => selectPeriod(p.key)}
					class="px-3 py-1.5 rounded-lg text-sm font-medium transition-colors
					       {period === p.key
					         ? 'bg-blue-600 text-white'
					         : 'border border-gray-300 dark:border-slate-600 text-gray-600 dark:text-slate-300 hover:bg-gray-100 dark:hover:bg-slate-700'}"
				>
					{p.label()}
				</button>
			{/each}
		</div>
	</div>

	<!-- Custom date range inputs -->
	{#if period === 'custom'}
		<div class="flex flex-wrap items-end gap-3">
			<div>
				<label for="custom-from" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('reports.period_from')}
				</label>
				<div class="relative">
					<input
						bind:this={fromInput}
						id="custom-from"
						type="date"
						bind:value={customFrom}
						max={customTo || undefined}
						class="date-input px-3 py-2 pr-9 rounded-lg border border-gray-300 dark:border-slate-600
						       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
					/>
					<button
						type="button"
						onclick={() => fromInput?.showPicker()}
						tabindex="-1"
						aria-label="Datum wählen"
						class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400
						       hover:text-gray-600 dark:hover:text-slate-300 transition-colors"
					>
						<Icon icon="heroicons:calendar" class="w-4 h-4" />
					</button>
				</div>
			</div>
			<div>
				<label for="custom-to" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('reports.period_to')}
				</label>
				<div class="relative">
					<input
						bind:this={toInput}
						id="custom-to"
						type="date"
						bind:value={customTo}
						min={customFrom || undefined}
						class="date-input px-3 py-2 pr-9 rounded-lg border border-gray-300 dark:border-slate-600
						       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
					/>
					<button
						type="button"
						onclick={() => toInput?.showPicker()}
						tabindex="-1"
						aria-label="Datum wählen"
						class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400
						       hover:text-gray-600 dark:hover:text-slate-300 transition-colors"
					>
						<Icon icon="heroicons:calendar" class="w-4 h-4" />
					</button>
				</div>
			</div>
			<button
				onclick={loadCustomRange}
				disabled={!customFrom || !customTo || customFrom > customTo}
				class="px-4 py-2 rounded-lg text-sm font-medium bg-blue-600 hover:bg-blue-700
				       text-white transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
			>
				{$_('reports.btn_load')}
			</button>
		</div>
	{/if}

	<!-- Error banner -->
	{#if loadError}
		<p class="text-sm text-red-600 dark:text-red-400 flex items-center gap-1.5">
			<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
			{loadError}
		</p>
	{/if}

	<!-- Content area — dimmed during loading -->
	<div class="relative space-y-6 transition-opacity {loading ? 'opacity-40 pointer-events-none' : ''}">

		{#if loading}
			<div class="absolute inset-0 flex items-start justify-center pt-16 z-10">
				<Icon icon="heroicons:arrow-path" class="w-8 h-8 text-blue-500 animate-spin" />
			</div>
		{/if}

		{#if !hasData && !loading}
			<!-- Empty state -->
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700
			            shadow-sm p-12 flex flex-col items-center gap-4 text-center">
				<div class="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-950/40 flex items-center justify-center">
					<Icon icon="heroicons:chart-bar" class="w-8 h-8 text-blue-500 dark:text-blue-400" />
				</div>
				<div>
					<p class="font-semibold text-gray-700 dark:text-slate-300">{$_('reports.no_data_title')}</p>
					<p class="text-sm text-gray-400 dark:text-slate-500 mt-1">{$_('reports.no_data_text')}</p>
				</div>
			</div>

		{:else}

			<!-- KPI cards -->
			<div class="grid grid-cols-2 lg:grid-cols-4 gap-4">

				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
				            dark:border-slate-700 shadow-sm p-4">
					<p class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
						{$_('reports.kpi_income')}
					</p>
					<p class="text-xl font-bold mt-1 text-blue-600 dark:text-blue-400 tabular-nums">
						{formatCurrency(analytics.totalIncome)}
					</p>
				</div>

				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
				            dark:border-slate-700 shadow-sm p-4">
					<p class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
						{$_('reports.kpi_expenses')}
					</p>
					<p class="text-xl font-bold mt-1 text-red-600 dark:text-red-400 tabular-nums">
						{formatCurrency(analytics.totalExpenses)}
					</p>
				</div>

				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
				            dark:border-slate-700 shadow-sm p-4">
					<p class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
						{$_('reports.kpi_balance')}
					</p>
					<p class="text-xl font-bold mt-1 tabular-nums
					          {analytics.balance >= 0
					            ? 'text-green-600 dark:text-green-400'
					            : 'text-red-600 dark:text-red-400'}">
						{formatCurrency(analytics.balance)}
					</p>
				</div>

				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
				            dark:border-slate-700 shadow-sm p-4">
					<p class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
						{$_('reports.kpi_savings_rate')}
					</p>
					{#if analytics.savingsRate !== null}
						<p class="text-xl font-bold mt-1 tabular-nums
						          {analytics.savingsRate >= 0
						            ? 'text-green-600 dark:text-green-400'
						            : 'text-red-600 dark:text-red-400'}">
							{analytics.savingsRate.toFixed(1)}&thinsp;%
						</p>
					{:else}
						<p class="text-xl font-bold mt-1 text-gray-400 dark:text-slate-500">—</p>
					{/if}
				</div>

			</div>

			<!-- Monthly income vs. expenses bar chart -->
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
			            dark:border-slate-700 shadow-sm p-5">
				<h2 class="text-sm font-semibold text-gray-700 dark:text-slate-200 mb-4">
					{$_('reports.chart_monthly_title')}
				</h2>
				<div class="h-72">
					<BarChart
						data={analytics.monthlyData}
						incomeLabel={$_('reports.chart_monthly_income')}
						expensesLabel={$_('reports.chart_monthly_expenses')}
					/>
				</div>
			</div>

			<!-- Expenses by category: donut + ranked horizontal bar -->
			{#if analytics.categoryExpenses.length > 0}
				<div class="grid md:grid-cols-2 gap-4">

					<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
					            dark:border-slate-700 shadow-sm p-5">
						<h2 class="text-sm font-semibold text-gray-700 dark:text-slate-200 mb-4">
							{$_('reports.chart_category_title')}
						</h2>
						<div class="h-72">
							<DonutChart
								data={analytics.categoryExpenses}
								uncategorizedLabel={$_('reports.uncategorized')}
							/>
						</div>
					</div>

					<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
					            dark:border-slate-700 shadow-sm p-5">
						<h2 class="text-sm font-semibold text-gray-700 dark:text-slate-200 mb-4">
							{$_('reports.chart_top_title')}
						</h2>
						<div class="h-72">
							<HBarChart
								data={topCategories}
								uncategorizedLabel={$_('reports.uncategorized')}
							/>
						</div>
					</div>

				</div>
			{/if}

			<!-- Cumulative balance trend line chart -->
			{#if analytics.monthlyData.length > 1}
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
				            dark:border-slate-700 shadow-sm p-5">
					<h2 class="text-sm font-semibold text-gray-700 dark:text-slate-200 mb-4">
						{$_('reports.chart_trend_title')}
					</h2>
					<div class="h-64">
						<LineChart data={analytics.monthlyData} />
					</div>
				</div>
			{/if}

		{/if}

	</div>

</div>

<style>
	/* Hide the native browser calendar icon so only our custom Heroicons button is visible. */
	.date-input::-webkit-calendar-picker-indicator {
		display: none;
	}
</style>
