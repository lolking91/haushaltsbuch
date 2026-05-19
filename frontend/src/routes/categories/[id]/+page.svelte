<script lang="ts">
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { categoriesApi } from '$lib/api/categories.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- State -------------------------------------------------------------------

	let category = $state(data.category);

	/** Editable fields – kept in sync with the form inputs. */
	let form = $state({
		name: category.name,
		// <input type="color"> requires lowercase hex; normalize on load
		color: category.color?.toLowerCase() ?? '#9ca3af',
		parentCategoryId: category.parent?.id ?? null
	});

	/** Snapshot of the last saved values – used to detect dirty state and reset. */
	let saved = $state({ ...form });

	let saving = $state(false);
	let saveStatus: 'idle' | 'success' | 'error' = $state('idle');

	// --- Derived -----------------------------------------------------------------

	let isDirty = $derived(
		form.name !== saved.name ||
		form.color !== saved.color ||
		form.parentCategoryId !== saved.parentCategoryId
	);

	/**
	 * All categories that are valid parent candidates:
	 * excludes the category itself and its own direct children
	 * to prevent trivial circular references.
	 */
	let parentCandidates = $derived(
		data.categories.filter(
			(c) => c.id !== category.id && c.parent?.id !== category.id
		)
	);

	/**
	 * The current parent, directly available from the nested response object.
	 * No local lookup needed.
	 */
	let currentParent = $derived(category.parent);

	// --- Actions -----------------------------------------------------------------

	/** Resets the form to the last saved state. */
	function reset() {
		form.name = saved.name;
		form.color = saved.color;
		form.parentCategoryId = saved.parentCategoryId;
		saveStatus = 'idle';
	}

	/** Persists editable fields via PATCH and updates the saved snapshot on success. */
	async function save() {
		saving = true;
		saveStatus = 'idle';
		try {
			const updated = await categoriesApi.update(category.id, {
				name: form.name,
				color: form.color,
				parentCategoryId: form.parentCategoryId
			});
			category = updated;
			saved = { ...form };
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
		href="{base}/categories"
		class="inline-flex items-center gap-1.5 text-sm text-gray-500 dark:text-slate-400
		       hover:text-gray-900 dark:hover:text-white transition-colors"
	>
		<Icon icon="heroicons:arrow-left" class="w-4 h-4" />
		{$_('category_detail.back')}
	</a>

	<!-- Page header -->
	<div class="flex items-center gap-3">
		<!-- Color dot (reflects current saved color, not the unsaved form value) -->
		<span
			class="w-4 h-4 rounded-full ring-2 ring-white dark:ring-slate-900 shrink-0"
			style="background-color: {category.color ?? '#9ca3af'}"
		></span>
		<div>
			<h1 class="text-2xl font-bold text-gray-900 dark:text-white">{category.name}</h1>
			<p class="text-sm text-gray-400 dark:text-slate-500 mt-0.5">
				{$_('category_detail.field_id')}: {category.id}
				{#if currentParent}
					&nbsp;·&nbsp;
					<span class="inline-flex items-center gap-1">
						<span
							class="w-2 h-2 rounded-full inline-block"
							style="background-color: {currentParent.color ?? '#9ca3af'}"
						></span>
						{currentParent.name}
					</span>
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
				{$_('category_detail.section_edit')}
			</h2>
		</div>

		<form
			onsubmit={(e) => { e.preventDefault(); save(); }}
			class="px-5 py-4 space-y-4"
		>
			<!-- Name -->
			<div>
				<label for="name" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('category_detail.field_name')}
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

			<!-- Color -->
			<div>
				<label for="color" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
					{$_('category_detail.field_color')}
				</label>
				<div class="flex items-center gap-3">
					<!--
						Native color picker — browser renders the swatch + picker dialog.
						Always produces lowercase hex output (e.g. #ff5733).
					-->
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
					{$_('category_detail.field_parent')}
				</label>
				<select
					id="parent"
					bind:value={form.parentCategoryId}
					class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
					       bg-white dark:bg-slate-900 text-sm outline-none
					       focus:ring-2 focus:ring-blue-500"
				>
					<option value={null}>{$_('category_detail.parent_none')}</option>
					{#each parentCandidates as candidate (candidate.id)}
						<option value={candidate.id}>{candidate.name}</option>
					{/each}
				</select>
			</div>

			<!-- Status message -->
			{#if saveStatus === 'success'}
				<p class="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
					<Icon icon="heroicons:check-circle" class="w-4 h-4" />
					{$_('category_detail.save_success')}
				</p>
			{:else if saveStatus === 'error'}
				<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
					<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
					{$_('category_detail.save_error')}
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
					{$_('category_detail.btn_reset')}
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
					{$_('category_detail.btn_save')}
				</button>
			</div>

		</form>
	</div>

</div>
