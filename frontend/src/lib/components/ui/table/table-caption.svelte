<script lang="ts">
	import type { HTMLAttributes } from 'svelte/elements';
	import { cn } from '$lib/utils.js';

	let { class: className, children, ...restProps }: HTMLAttributes<HTMLElement> = $props();
</script>

<caption class={cn('mt-4 text-sm text-gray-500 dark:text-slate-400', className)} {...restProps}>
	{@render children?.()}
</caption>
