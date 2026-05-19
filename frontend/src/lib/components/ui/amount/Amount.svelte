<script lang="ts">
	import { cn } from '$lib/utils.js';
	import { formatCurrency } from '$lib/utils/format.js';

	interface Props {
		/** The raw numeric amount. */
		amount: number;
		/** ISO 4217 currency code, defaults to EUR. */
		currency?: string;
		/** Transaction type — determines the text colour (green = income, red = expense). */
		type: 'INCOME' | 'EXPENSE';
		/** Additional Tailwind classes for font size, weight, etc. */
		class?: string;
	}

	let { amount, currency = 'EUR', type, class: className }: Props = $props();
</script>

<!--
  tabular-nums:    keeps digits aligned in table columns
  whitespace-nowrap: prevents awkward line breaks inside a currency string
-->
<span
	class={cn(
		'tabular-nums whitespace-nowrap',
		type === 'INCOME' ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400',
		className
	)}
>
	{formatCurrency(amount, currency)}
</span>
