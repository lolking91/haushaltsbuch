<script lang="ts">
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import type { Category } from '$lib/types/types.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	let categories = $state(data.categories);

	// --- Derived: build hierarchy groups ----------------------------------------
	//
	// A "group" is one root category plus all its direct children.
	// Categories whose parentCategoryId points to a non-existent ID are treated
	// as root categories so they are never silently hidden.

	type CategoryGroup = { parent: Category; children: Category[] };

	let groups = $derived.by((): CategoryGroup[] => {
		const idSet = new Set(categories.map((c) => c.id));

		// Root = no parent, or parent ID not present in the loaded list (orphan)
		const roots = categories.filter(
			(c) => c.parent === null || !idSet.has(c.parent.id)
		);

		return roots.map((root) => ({
			parent: root,
			children: categories.filter((c) => c.parent?.id === root.id)
		}));
	});

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
					<div class="flex items-center gap-3 px-5 py-4">
						<!-- Color dot -->
						<span
							class="w-3 h-3 rounded-full shrink-0 ring-2 ring-white dark:ring-slate-800"
							style="background-color: {dotColor(group.parent.color)}"
						></span>

						<!-- Name + root badge -->
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
					</div>

					<!-- Children -->
					{#if group.children.length > 0}
						<ul class="border-t border-gray-100 dark:border-slate-700 divide-y
						           divide-gray-100 dark:divide-slate-700/60">
							{#each group.children as child (child.id)}
								<li class="flex items-center gap-3 pl-10 pr-5 py-3
								           hover:bg-gray-50 dark:hover:bg-slate-700/40 transition-colors">
									<!-- Connector line + dot -->
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
								</li>
							{/each}
						</ul>
					{/if}

				</div>
			{/each}
		</div>

		<!-- Summary footer -->
		<p class="text-xs text-gray-400 dark:text-slate-500 text-right">
			{categories.length}
			{categories.length === 1 ? 'Kategorie' : 'Kategorien'},
			davon {groups.length} {groups.length === 1 ? 'Hauptkategorie' : 'Hauptkategorien'}
		</p>
	{/if}

</div>
