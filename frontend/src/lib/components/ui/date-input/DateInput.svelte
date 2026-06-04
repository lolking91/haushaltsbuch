<script lang="ts">
	import Icon from '@iconify/svelte';

	interface Props {
		/** Bound date value in ISO format (yyyy-MM-dd). */
		value: string;
		id?: string;
		min?: string;
		max?: string;
		/** Additional CSS classes forwarded to the underlying input element. */
		class?: string;
	}

	let { value = $bindable(), id, min, max, class: className = '' }: Props = $props();

	/** Reference to the input element for programmatic picker invocation. */
	let inputEl: HTMLInputElement;
</script>

<!--
	Date input with a custom calendar icon button.
	Hides the native browser calendar indicator and replaces it with a
	Heroicons calendar icon that programmatically opens the date picker.
-->
<div class="relative">
	<input
		bind:this={inputEl}
		{id}
		type="date"
		bind:value
		{min}
		{max}
		class="date-input px-3 py-2 pr-9 rounded-lg border border-gray-300 dark:border-slate-600
		       bg-white dark:bg-slate-900 text-sm outline-none
		       focus:ring-2 focus:ring-blue-500 {className}"
	/>
	<button
		type="button"
		onclick={() => inputEl?.showPicker()}
		tabindex="-1"
		aria-label="Datum wählen"
		class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400
		       hover:text-gray-600 dark:hover:text-slate-300 transition-colors"
	>
		<Icon icon="heroicons:calendar" class="w-4 h-4" />
	</button>
</div>

<style>
	/* Hide the native browser calendar icon — only our custom button is visible. */
	.date-input::-webkit-calendar-picker-indicator {
		display: none;
	}
</style>
