<script lang="ts">
	interface Props {
		/** Bound numeric value. */
		value: number | string;
		id?: string;
		min?: number | string;
		max?: number | string;
		step?: number | string;
		placeholder?: string;
		/** Additional CSS classes forwarded to the input (e.g. width utilities like "w-full" or "w-24"). */
		class?: string;
	}

	let { value = $bindable(), id, min, max, step, placeholder, class: className = '' }: Props = $props();
</script>

<!--
	Number input with consistent app styling.
	Spinner buttons are hidden globally via layout.css.
-->
<input
	{id}
	type="number"
	bind:value
	{min}
	{max}
	{step}
	{placeholder}
	class="px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
	       bg-white dark:bg-slate-900 text-sm outline-none
	       focus:ring-2 focus:ring-blue-500 {className}"
/>
