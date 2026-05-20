<script lang="ts">
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { categoriesApi } from '$lib/api/categories.js';
	import type { Category } from '$lib/types/types.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	let categories = $state(data.categories);

	// --- Derived: build hierarchy groups ----------------------------------------
	//
	// A "group" is one root category plus all its direct children.
	// Categories whose parent points to a non-existent ID are treated as root
	// categories so they are never silently hidden.

	type CategoryGroup = { parent: Category; children: Category[] };

	let groups = $derived.by((): CategoryGroup[] => {
		const idSet = new Set(categories.map((c) => c.id));

		const roots = categories.filter(
			(c) => c.parent === null || !idSet.has(c.parent.id)
		);

		return roots.map((root) => ({
			parent: root,
			children: categories.filter((c) => c.parent?.id === root.id)
		}));
	});

	// --- Delete state ------------------------------------------------------------

	/**
	 * ID of the category currently showing the inline confirmation.
	 * Only one confirmation is visible at a time.
	 */
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
			await categoriesApi.delete(id);
			// Optimistic update: remove from local state, derived groups recompute
			categories = categories.filter((c) => c.id !== id);
			confirmingDeleteId = null;
		} catch {
			deleteError = $_('categories.delete_error');
		}
	}

	// --- Helpers -----------------------------------------------------------------

	/**
	 * Returns a CSS-safe color string. Falls back to a neutral gray so the dot
	 * is always rendered even when the color field is null/empty.
	 */
	function dotColor(color: string | null): string {
		return color?.trim() || '#9ca3af';
	}
</script>

<div class="space-y-5">

	<!-- Page header -->
	<div class="flex items-center justify-between">
		<h1 class="text-2xl font-bold">{$_('categories.title')}</h1>
		<a
			href="{base}/categories/new"
			class="inline-flex items-center gap-2 px-4 py-2 rounded-xl
			       bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
		>
			<Icon icon="heroicons:plus" class="w-4 h-4" />
			{$_('categories.btn_new')}
		</a>
	</div>

	<!-- Empty state -->
	{#if groups.length === 0}
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700
		            shadow-sm p-12 flex flex-col items-center gap-4 text-center">
			<div class="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-950/40
			            flex items-center justify-center">
				<Icon icon="heroicons:tag" class="w-8 h-8 text-blue-500 dark:text-blue-400" />
			</div>
			<div>
				<p class="font-semibold text-gray-700 dark:text-slate-300">
					{$_('categories.empty_title')}
				</p>
				<p class="text-sm text-gray-400 dark:text-slate-500 mt-1">
					{$_('categories.empty_text')}
				</p>
			</div>
			<a
				href="{base}/categories/new"
				class="inline-flex items-center gap-2 px-4 py-2 rounded-xl
				       bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
			>
				<Icon icon="heroicons:plus" class="w-4 h-4" />
				{$_('categories.empty_cta')}
			</a>
		</div>

	{:else}
		<!-- One card per root category -->
		<div class="space-y-3">
			{#each groups as group (group.parent.id)}
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200
				            dark:border-slate-700 shadow-sm overflow-hidden">

					<!-- Root category row -->
					{#if confirmingDeleteId === group.parent.id}
						<!-- Inline delete confirmation for root -->
						<div class="flex items-center gap-3 px-5 py-4 bg-red-50 dark:bg-red-950/20">
							<span
								class="w-3 h-3 rounded-full shrink-0 ring-2 ring-white dark:ring-slate-800"
								style="background-color: {dotColor(group.parent.color)}"
							></span>
							<span class="font-semibold text-gray-900 dark:text-white flex-1 truncate">
								{group.parent.name}
							</span>
							<span class="text-sm text-red-600 dark:text-red-400 font-medium shrink-0">
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
								{$_('categories.delete_cancel')}
							</button>
							<button
								onclick={() => confirmDelete(group.parent.id)}
								class="px-3 py-1.5 rounded-lg text-sm font-medium
								       bg-red-600 hover:bg-red-700 text-white transition-colors shrink-0"
							>
								{$_('categories.delete_yes')}
							</button>
						</div>
					{:else}
						<div class="flex items-center gap-3 px-5 py-4">
							<span
								class="w-3 h-3 rounded-full shrink-0 ring-2 ring-white dark:ring-slate-800"
								style="background-color: {dotColor(group.parent.color)}"
							></span>

							<span class="font-semibold text-gray-900 dark:text-white flex-1">
								{group.parent.name}
							</span>
							<span class="hidden sm:inline-flex text-xs px-2 py-0.5 rounded-full
							             bg-slate-100 dark:bg-slate-700 text-slate-500 dark:text-slate-400">
								{$_('categories.root_badge')}
							</span>

							<!-- Edit link -->
							<a
								href="{base}/categories/{group.parent.id}"
								class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
								       hover:text-blue-600 dark:hover:text-blue-400
								       hover:bg-blue-50 dark:hover:bg-blue-950/40 transition-colors"
								aria-label="Kategorie bearbeiten"
							>
								<Icon icon="heroicons:pencil-square" class="w-4 h-4" />
							</a>

							<!-- Delete button — disabled when children exist -->
							{#if group.children.length > 0}
								<span
									title={$_('categories.delete_has_children')}
									class="p-1.5 rounded-lg text-gray-300 dark:text-slate-600 cursor-not-allowed"
								>
									<Icon icon="heroicons:trash" class="w-4 h-4" />
								</span>
							{:else}
								<button
									onclick={() => requestDelete(group.parent.id)}
									class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
									       hover:text-red-600 dark:hover:text-red-400
									       hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors"
									aria-label="Kategorie löschen"
								>
									<Icon icon="heroicons:trash" class="w-4 h-4" />
								</button>
							{/if}
						</div>
					{/if}

					<!-- Children -->
					{#if group.children.length > 0}
						<ul class="border-t border-gray-100 dark:border-slate-700 divide-y
						           divide-gray-100 dark:divide-slate-700/60">
							{#each group.children as child (child.id)}
								<li class="hover:bg-gray-50 dark:hover:bg-slate-700/40 transition-colors">

									{#if confirmingDeleteId === child.id}
										<!-- Inline delete confirmation for child -->
										<div class="flex items-center gap-3 pl-10 pr-5 py-3
										            bg-red-50 dark:bg-red-950/20">
											<span class="relative flex items-center shrink-0">
												<span class="absolute -left-5 top-1/2 -translate-y-1/2
												             w-4 border-t border-dashed border-red-300 dark:border-red-800">
												</span>
												<span
													class="w-2.5 h-2.5 rounded-full ring-2 ring-white dark:ring-slate-800"
													style="background-color: {dotColor(child.color)}"
												></span>
											</span>
											<span class="text-sm text-gray-700 dark:text-slate-300 flex-1 truncate">
												{child.name}
											</span>
											<span class="text-sm text-red-600 dark:text-red-400 font-medium shrink-0">
												{$_('categories.delete_confirm')}
											</span>
											{#if deleteError}
												<span class="text-xs text-red-500">{deleteError}</span>
											{/if}
											<button
												onclick={cancelDelete}
												class="px-3 py-1.5 rounded-lg text-sm border border-gray-300 dark:border-slate-600
												       text-gray-600 dark:text-slate-300 hover:bg-white dark:hover:bg-slate-700
												       transition-colors shrink-0"
											>
												{$_('categories.delete_cancel')}
											</button>
											<button
												onclick={() => confirmDelete(child.id)}
												class="px-3 py-1.5 rounded-lg text-sm font-medium
												       bg-red-600 hover:bg-red-700 text-white transition-colors shrink-0"
											>
												{$_('categories.delete_yes')}
											</button>
										</div>
									{:else}
										<div class="flex items-center gap-3 pl-10 pr-5 py-3">
											<span class="relative flex items-center shrink-0">
												<span class="absolute -left-5 top-1/2 -translate-y-1/2
												             w-4 border-t border-dashed border-gray-300 dark:border-slate-600">
												</span>
												<span
													class="w-2.5 h-2.5 rounded-full ring-2 ring-white dark:ring-slate-800"
													style="background-color: {dotColor(child.color)}"
												></span>
											</span>

											<span class="text-sm text-gray-700 dark:text-slate-300 flex-1">
												{child.name}
											</span>

											<a
												href="{base}/categories/{child.id}"
												class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
												       hover:text-blue-600 dark:hover:text-blue-400
												       hover:bg-blue-50 dark:hover:bg-blue-950/40 transition-colors"
												aria-label="Unterkategorie bearbeiten"
											>
												<Icon icon="heroicons:pencil-square" class="w-4 h-4" />
											</a>

											<button
												onclick={() => requestDelete(child.id)}
												class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
												       hover:text-red-600 dark:hover:text-red-400
												       hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors"
												aria-label="Unterkategorie löschen"
											>
												<Icon icon="heroicons:trash" class="w-4 h-4" />
											</button>
										</div>
									{/if}

								</li>
							{/each}
						</ul>
					{/if}

				</div>
			{/each}
		</div>

		<p class="text-xs text-gray-400 dark:text-slate-500 text-right">
			{categories.length}
			{categories.length === 1 ? 'Kategorie' : 'Kategorien'},
			davon {groups.length} {groups.length === 1 ? 'Hauptkategorie' : 'Hauptkategorien'}
		</p>
	{/if}

</div>
