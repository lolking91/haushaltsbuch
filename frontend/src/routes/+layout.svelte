<script lang="ts">
	import './layout.css';
	import Nav from '$lib/components/Nav.svelte';
	import favicon from '$lib/assets/favicon.svg';
	import { theme } from '$lib/stores/theme.svelte';
	import { browser } from '$app/environment';

	let { children } = $props();

	$effect(() => {
		if (browser) {
			document.documentElement.classList.toggle('dark', theme.dark);
		}
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-gray-100 transition-colors">
	<Nav />
	<main class="max-w-6xl mx-auto px-4 sm:px-6 py-8">
		{@render children()}
	</main>
</div>
