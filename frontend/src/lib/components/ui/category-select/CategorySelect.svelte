<script lang="ts">
	import Icon from '@iconify/svelte';
	import type { Category } from '$lib/types/types.js';

	interface Props {
		/** Bound selected category ID; null means no category selected. */
		value: number | null;
		/** Flat list of all categories — hierarchy is derived from parent references. */
		categories: Category[];
		placeholder?: string;
		id?: string;
		/** Additional CSS classes forwarded to the root element (e.g. "w-full"). */
		class?: string;
	}

	let {
		value = $bindable(null),
		categories,
		placeholder = 'Kategorie wählen…',
		id,
		class: className = ''
	}: Props = $props();

	// --- Types ---

	/** Category node enriched with resolved children for the dropdown tree. */
	type CategoryNode = Category & { children: Category[] };

	// --- Derived ---

	/** Flat lookup for the currently selected category. */
	let selectedCategory = $derived(
		value !== null ? (categories.find((c) => c.id === value) ?? null) : null
	);

	/**
	 * 2-level tree built from the flat category list.
	 * Root categories (parent === null) become section headers;
	 * their children are listed below them.
	 */
	let tree = $derived.by<CategoryNode[]>(() => {
		const roots = categories.filter((c) => c.parent === null);
		return roots.map((root) => ({
			...root,
			children: categories.filter((c) => c.parent?.id === root.id)
		}));
	});

	/** Filtered tree based on the current search query. */
	let filteredTree = $derived.by<CategoryNode[]>(() => {
		const q = inputValue.trim().toLowerCase();
		if (!q) return tree;

		return tree.flatMap((node) => {
			const parentMatches = node.name.toLowerCase().includes(q);
			const matchingChildren = node.children.filter((c) =>
				c.name.toLowerCase().includes(q)
			);

			if (parentMatches || matchingChildren.length > 0) {
				// When the parent name matches, show all its children; otherwise only matching children.
				return [{ ...node, children: parentMatches ? node.children : matchingChildren }];
			}
			return [];
		});
	});

	// --- State ---

	let open = $state(false);
	let inputValue = $state('');
	let containerEl: HTMLDivElement;

	/**
	 * Sync the input display text whenever the dropdown closes.
	 * Shows the selected category name or clears if nothing is selected.
	 */
	$effect(() => {
		if (!open) {
			inputValue = selectedCategory?.name ?? '';
		}
	});

	// --- Actions ---

	/** Opens the dropdown and clears the input so the user can type a search query. */
	function openDropdown() {
		open = true;
		inputValue = '';
	}

	/** Selects a category and closes the dropdown. */
	function selectCategory(cat: Category) {
		value = cat.id;
		open = false;
	}

	/** Clears the current selection. */
	function clearSelection(e: MouseEvent) {
		e.stopPropagation();
		value = null;
		open = false;
	}

	/**
	 * Closes the dropdown when focus leaves the component entirely.
	 * The onmousedown.preventDefault on the dropdown panel prevents the input
	 * from losing focus when the user clicks an option, so this handler only
	 * fires when focus genuinely moves outside.
	 */
	function handleContainerBlur(e: FocusEvent) {
		if (!containerEl.contains(e.relatedTarget as Node | null)) {
			open = false;
		}
	}

	function handleKeyDown(e: KeyboardEvent) {
		if (e.key === 'Escape') {
			open = false;
		}
	}
</script>

<div
	bind:this={containerEl}
	class="relative {className}"
	onfocusout={handleContainerBlur}
>
	<!-- Trigger / search input -->
	<div class="relative">
		<!-- Color dot when a category is selected (closed state), search icon otherwise -->
		{#if selectedCategory && !open}
			<span
				class="absolute left-3 top-1/2 -translate-y-1/2 w-2.5 h-2.5 rounded-full pointer-events-none"
				style="background-color: {selectedCategory.color}"
			></span>
		{:else}
			<Icon
				icon="heroicons:magnifying-glass"
				class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-slate-500 pointer-events-none"
			/>
		{/if}

		<input
			{id}
			type="text"
			bind:value={inputValue}
			placeholder={open ? 'Suchen…' : placeholder}
			onfocus={openDropdown}
			onkeydown={handleKeyDown}
			autocomplete="off"
			class="w-full pl-9 pr-8 py-2 rounded-lg border border-gray-300 dark:border-slate-600
			       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
		/>

		<!-- × to clear selection, or chevron indicator -->
		{#if value !== null}
			<button
				type="button"
				tabindex="-1"
				onclick={clearSelection}
				aria-label="Auswahl löschen"
				class="absolute right-2 top-1/2 -translate-y-1/2
				       text-gray-400 hover:text-gray-600 dark:hover:text-slate-300 transition-colors"
			>
				<Icon icon="heroicons:x-mark" class="w-4 h-4" />
			</button>
		{:else}
			<Icon
				icon="heroicons:chevron-down"
				class="absolute right-2 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none
				       transition-transform duration-150 {open ? 'rotate-180' : ''}"
			/>
		{/if}
	</div>

	<!-- Dropdown panel -->
	{#if open}
		<!--
			onmousedown.preventDefault keeps focus on the input when clicking options,
			so the onfocusout on the container does not fire prematurely.
		-->
		<div
			class="absolute z-50 mt-1 w-full max-h-60 overflow-y-auto
			       bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700 shadow-lg"
			onmousedown={(e) => e.preventDefault()}
		>
			{#if filteredTree.length === 0}
				<p class="px-3 py-6 text-sm text-center text-gray-400 dark:text-slate-500">
					Keine Kategorie gefunden
				</p>
			{:else}
				{#each filteredTree as node (node.id)}
					{#if node.children.length > 0}
						<!-- Parent with children: selectable, also serves as section header -->
						<button
							type="button"
							onclick={() => selectCategory(node)}
							class="w-full flex items-center gap-2 px-3 pt-2.5 pb-1 text-left transition-colors
							       {value === node.id
							         ? 'bg-blue-50 dark:bg-blue-950/40'
							         : 'hover:bg-gray-50 dark:hover:bg-slate-700'}"
						>
							<span
								class="w-2 h-2 rounded-full shrink-0"
								style="background-color: {node.color}"
							></span>
							<span class="text-xs font-semibold uppercase tracking-wide
							            {value === node.id
							              ? 'text-blue-700 dark:text-blue-300'
							              : 'text-gray-400 dark:text-slate-500'}">
								{node.name}
							</span>
							{#if value === node.id}
								<Icon icon="heroicons:check" class="w-3.5 h-3.5 ml-auto text-blue-600 dark:text-blue-400 shrink-0" />
							{/if}
						</button>
						{#each node.children as child (child.id)}
							<button
								type="button"
								onclick={() => selectCategory(child)}
								class="w-full flex items-center gap-2 pl-6 pr-3 py-2 text-sm text-left transition-colors
								       {value === child.id
								         ? 'bg-blue-50 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300 font-medium'
								         : 'text-gray-700 dark:text-slate-300 hover:bg-gray-50 dark:hover:bg-slate-700'}"
							>
								<span
									class="w-2 h-2 rounded-full shrink-0"
									style="background-color: {child.color}"
								></span>
								{child.name}
								{#if value === child.id}
									<Icon icon="heroicons:check" class="w-3.5 h-3.5 ml-auto text-blue-600 dark:text-blue-400 shrink-0" />
								{/if}
							</button>
						{/each}
					{:else}
						<!-- Root category without children: selectable -->
						<button
							type="button"
							onclick={() => selectCategory(node)}
							class="w-full flex items-center gap-2 px-3 py-2 text-sm text-left transition-colors
							       {value === node.id
							         ? 'bg-blue-50 dark:bg-blue-950/40 text-blue-700 dark:text-blue-300 font-medium'
							         : 'text-gray-700 dark:text-slate-300 hover:bg-gray-50 dark:hover:bg-slate-700'}"
						>
							<span
								class="w-2 h-2 rounded-full shrink-0"
								style="background-color: {node.color}"
							></span>
							{node.name}
							{#if value === node.id}
								<Icon icon="heroicons:check" class="w-3.5 h-3.5 ml-auto text-blue-600 dark:text-blue-400 shrink-0" />
							{/if}
						</button>
					{/if}
				{/each}
			{/if}
		</div>
	{/if}
</div>
