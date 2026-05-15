<script lang="ts">
	import type { HTMLThAttributes } from 'svelte/elements';
	import { cn } from '$lib/utils.js';

	let { class: className, children, ...restProps }: HTMLThAttributes = $props();
</script>

<th
	class={cn(
		'h-11 px-4 text-left align-middle text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide [&:has([role=checkbox])]:pr-0',
		className
	)}
	{...restProps}
>
	{@render children?.()}
</th>
