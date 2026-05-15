<script lang="ts">
	import type { HTMLAttributes } from 'svelte/elements';
	import { cn } from '$lib/utils.js';

	let { class: className, children, ...restProps }: HTMLAttributes<HTMLTableSectionElement> =
		$props();
</script>

<tfoot
	class={cn('border-t font-medium [&>tr]:last:border-b-0', className)}
	{...restProps}
>
	{@render children?.()}
</tfoot>
