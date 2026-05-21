<script lang="ts">
	import type { Category } from '$lib/types/types.js';
	import type { HTMLAttributes } from 'svelte/elements';
	import { cn } from '$lib/utils.js';

	/**
	 * Pill badge for a transaction category.
	 *
	 * When a category is provided the badge uses the category's hex color as text
	 * color and a 15%-opacity tint of the same color as background, matching the
	 * visual style of the income/expense badges.
	 *
	 * Falls back to a muted gray badge with an em-dash when no category is set.
	 */
	let {
		category,
		class: className,
		...restProps
	}: HTMLAttributes<HTMLSpanElement> & { category: Category | null } = $props();

	const base = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium';
</script>

{#if category}
	<!-- '26' hex-suffix = 15 % opacity → subtle tinted background from the category color -->
	<span
		style="background-color: {category.color}26; color: {category.color};"
		class={cn(base, className)}
		{...restProps}
	>
		{category.name}
	</span>
{:else}
	<span
		class={cn(base, 'bg-gray-100 text-gray-400 dark:bg-slate-700 dark:text-slate-500', className)}
		{...restProps}
	>
		—
	</span>
{/if}
