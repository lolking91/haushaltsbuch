<script lang="ts">
	import type { HTMLAttributes } from 'svelte/elements';
	import { cn } from '$lib/utils.js';

	let { class: className, children, ...restProps }: HTMLAttributes<HTMLTableRowElement> = $props();
</script>

<tr
	class={cn(
		'border-b border-gray-200 dark:border-slate-700 transition-colors hover:bg-gray-50 dark:hover:bg-slate-700/50',
		className
	)}
	{...restProps}
>
	{@render children?.()}
</tr>
