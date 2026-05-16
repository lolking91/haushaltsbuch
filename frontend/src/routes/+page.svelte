<script lang="ts">
	import { _ } from 'svelte-i18n';
</script>

<h1 class="text-2xl font-bold mb-6">{$_('dashboard.title')}</h1>

<div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-6 shadow-sm">
		<p class="text-sm text-gray-500 dark:text-slate-400">{$_('dashboard.accounts')}</p>
		<p class="text-3xl font-bold mt-1">–</p>
	</div>
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-6 shadow-sm">
		<p class="text-sm text-gray-500 dark:text-slate-400">{$_('dashboard.income_month')}</p>
		<p class="text-3xl font-bold mt-1 text-green-600 dark:text-green-400">–</p>
	</div>
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-6 shadow-sm">
		<p class="text-sm text-gray-500 dark:text-slate-400">{$_('dashboard.expenses_month')}</p>
		<p class="text-3xl font-bold mt-1 text-red-600 dark:text-red-400">–</p>
	</div>
</div>

<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-6 shadow-sm">
	<p class="text-gray-500 dark:text-slate-400 text-sm">{$_('dashboard.overview_placeholder')}</p>
</div>
