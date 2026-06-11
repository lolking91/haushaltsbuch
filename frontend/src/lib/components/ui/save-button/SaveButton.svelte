<script lang="ts">
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';

	interface Props {
		/** Disables both the primary and chevron buttons. */
		disabled?: boolean;
		/** Shows spinner in the primary button while saving. */
		saving?: boolean;
		/** Label for the primary save button. */
		label: string;
		/** Called when "Speichern und neu" is selected from the dropdown. */
		onsaveandnew: () => void;
	}

	let { disabled = false, saving = false, label, onsaveandnew }: Props = $props();

	let dropdownOpen = $state(false);
	let containerEl: HTMLDivElement;

	/**
	 * Closes the dropdown when focus leaves the component entirely.
	 * onmousedown.preventDefault on the panel keeps focus on the chevron button
	 * so this only fires on genuine outside-clicks.
	 */
	function handleContainerBlur(e: FocusEvent) {
		if (!containerEl.contains(e.relatedTarget as Node | null)) {
			dropdownOpen = false;
		}
	}
</script>

<div bind:this={containerEl} class="relative inline-flex" onfocusout={handleContainerBlur}>
	<!-- Primary save button — type="submit" triggers the enclosing form -->
	<button
		type="submit"
		{disabled}
		class="px-4 py-2 rounded-l-lg text-sm font-medium
		       bg-blue-600 hover:bg-blue-700 text-white transition-colors
		       disabled:opacity-40 disabled:cursor-not-allowed
		       flex items-center gap-1.5"
	>
		{#if saving}
			<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
		{/if}
		{label}
	</button>

	<!-- Divider between primary and chevron -->
	<span class="w-px bg-blue-500 self-stretch {disabled ? 'opacity-40' : ''}"></span>

	<!-- Chevron button — opens the dropdown for secondary actions -->
	<button
		type="button"
		{disabled}
		onclick={() => (dropdownOpen = !dropdownOpen)}
		aria-label="Weitere Speicheroptionen"
		class="px-2 py-2 rounded-r-lg
		       bg-blue-600 hover:bg-blue-700 text-white transition-colors
		       disabled:opacity-40 disabled:cursor-not-allowed"
	>
		<Icon
			icon="heroicons:chevron-down"
			class="w-4 h-4 transition-transform duration-150 {dropdownOpen ? 'rotate-180' : ''}"
		/>
	</button>

	<!-- Dropdown panel -->
	{#if dropdownOpen}
		<!--
			onmousedown.preventDefault keeps focus on the chevron button when clicking
			an option, so handleContainerBlur does not fire prematurely.
		-->
		<div
			class="absolute right-0 top-full mt-1 z-50 w-44
			       bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700 shadow-lg"
			onmousedown={(e) => e.preventDefault()}
		>
			<button
				type="button"
				onclick={() => { dropdownOpen = false; onsaveandnew(); }}
				class="w-full px-3 py-2 text-sm text-left text-gray-700 dark:text-slate-300
				       hover:bg-gray-50 dark:hover:bg-slate-700 rounded-lg transition-colors"
			>
				{$_('common.btn_save_and_new')}
			</button>
		</div>
	{/if}
</div>
