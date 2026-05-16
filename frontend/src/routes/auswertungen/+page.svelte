<script lang="ts">
	import { _ } from 'svelte-i18n';
</script>

<h1 class="text-2xl font-bold mb-6">{$_('reports.title')}</h1>

<div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-6 shadow-sm">
		<h2 class="font-semibold text-base mb-4">{$_('reports.expenses_by_category')}</h2>
		<div class="h-48 flex items-center justify-center text-gray-400 dark:text-slate-500 text-sm">
			{$_('reports.no_data')}
		</div>
	</div>
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-6 shadow-sm">
		<h2 class="font-semibold text-base mb-4">{$_('reports.income_vs_expenses')}</h2>
		<div class="h-48 flex items-center justify-center text-gray-400 dark:text-slate-500 text-sm">
			{$_('reports.no_data')}
		</div>
	</div>
</div>
