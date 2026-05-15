<script lang="ts">
	import type { HTMLAttributes } from 'svelte/elements';
	import { cn } from '$lib/utils.js';

	type Variant = 'default' | 'income' | 'expense' | 'outline';

	let {
		class: className,
		variant = 'default',
		children,
		...restProps
	}: HTMLAttributes<HTMLSpanElement> & { variant?: Variant } = $props();

	const variants: Record<Variant, string> = {
		default:
			'bg-gray-100 text-gray-800 dark:bg-slate-700 dark:text-slate-200',
		income:
			'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-400',
		expense:
			'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-400',
		outline:
			'border border-gray-300 dark:border-slate-600 text-gray-700 dark:text-slate-300'
	};
</script>

<span
	class={cn(
		'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
		variants[variant],
		className
	)}
	{...restProps}
>
	{@render children?.()}
</span>
