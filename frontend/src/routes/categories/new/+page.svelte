<script lang="ts">
	import { goto, invalidateAll } from '$app/navigation';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { categoriesApi } from '$lib/api/categories.js';
	import { randomColor } from '$lib/utils/format.js';
	import { SaveButton } from '$lib/components/ui/save-button/index.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- Form state --------------------------------------------------------------

	function defaultForm() {
		return { name: '', color: randomColor(), parentCategoryId: null as number | null };
	}

	let form = $state(defaultForm());
	let saving = $state(false);
	let errorMessage = $state('');

	// --- Derived -----------------------------------------------------------------

	/** The Save button is only active when a name has been entered. */
	let canSave = $derived(form.name.trim().length > 0);

	// --- Actions -----------------------------------------------------------------

	async function create(saveAndNew = false) {
		saving = true;
		errorMessage = '';
		try {
			const created = await categoriesApi.create({
				name: form.name.trim(),
				color: form.color,
				parentCategoryId: form.parentCategoryId
			});
			if (saveAndNew) {
				// Same-route navigation doesn't re-mount the component, so reset state and
				// re-run the load function to refresh server data (e.g. parent category dropdown).
				form = defaultForm();
				await invalidateAll();
			} else {
				await goto(`${base}/categories/${created.id}`);
			}
		} catch {
			errorMessage = $_('category_create.create_error');
		} finally {
			saving = false;
		}
	}
</script>

<div class="space-y-5">

	<!-- Back link -->
	<a
		href="{base}/categories"
		class="inline-flex items-center gap-1.5 text-sm text-gray-500 dark:text-slate-400
		       hover:text-gray-900 dark:hover:text-white transition-colors"
	>
		<Icon icon="heroicons:arrow-left" class="w-4 h-4" />
		{$_('common.back_to_overview')}
	</a>

	<!-- Page header -->
	<h1 class="text-2xl font-bold text-gray-900 dark:text-white">
		{$_('category_create.title')}
	</h1>

	<!-- Create card -->
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
	            dark:border-slate-700 shadow-sm max-w-lg">

		<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
			<Icon icon="heroicons:tag" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
			<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
				{$_('category_create.section')}
			</h2>
		</div>

		<form
			onsubmit={(e) => { e.preventDefault(); create(); }}
			class="px-5 py-4 space-y-4"
		>
			<!-- Name -->
			<div>
				<label for="name" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('categories.field_name')}
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

			<!-- Color -->
			<div>
				<label for="color" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('categories.field_color')}
				</label>
				<div class="flex items-center gap-3">
					<input
						id="color"
						type="color"
						bind:value={form.color}
						class="h-9 w-14 rounded-lg border border-gray-300 dark:border-slate-600
						       bg-white dark:bg-slate-900 cursor-pointer p-0.5"
					/>
					<span class="text-sm font-mono text-gray-500 dark:text-slate-400">
						{form.color}
					</span>
				</div>
			</div>

			<!-- Parent category -->
			<div>
				<label for="parent" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('categories.field_parent')}
				</label>
				<select
					id="parent"
					bind:value={form.parentCategoryId}
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				>
					<option value={null}>{$_('categories.parent_none')}</option>
					{#each data.categories as candidate (candidate.id)}
						<option value={candidate.id}>{candidate.name}</option>
					{/each}
				</select>
			</div>

			<!-- Error message -->
			{#if errorMessage}
				<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
					<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
					{errorMessage}
				</p>
			{/if}

			<!-- Action button -->
			<div class="flex justify-end pt-1">
				<SaveButton
					label={$_('category_create.btn_create')}
					disabled={!canSave || saving}
					{saving}
					onsaveandnew={() => create(true)}
				/>
			</div>

		</form>
	</div>

</div>
