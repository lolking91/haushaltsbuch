<script lang="ts">
	import './layout.css';
	import Nav from '$lib/components/Nav.svelte';
	import favicon from '$lib/assets/favicon.svg';
	import { theme } from '$lib/stores/theme.svelte';
	import { auth } from '$lib/stores/auth.svelte.js';
	import { setupI18n } from '$lib/i18n/index.js';
	import '$lib/icons.js';
	import { browser } from '$app/environment';
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import { page } from '$app/state';

	let { children } = $props();

	// Registers translation catalogues and restores persisted locale.
	// Must run before any component renders translated strings.
	setupI18n();

	$effect(() => {
		if (browser) {
			document.documentElement.classList.toggle('dark', theme.dark);
		}
	});

	const loginPath = `${base}/login`;
	let isLoginPage = $derived(page.url.pathname === loginPath);

	// adapter-static builds a pure SPA, so there is no server-side check we could
	// rely on — the session has to be verified client-side on every (re)load.
	$effect(() => {
		if (browser && !auth.checked) {
			auth.checkSession();
		}
	});

	$effect(() => {
		if (!auth.checked) return;
		if (!auth.isAuthenticated && !isLoginPage) {
			goto(loginPath);
		} else if (auth.isAuthenticated && isLoginPage) {
			goto(`${base}/`);
		}
	});

	/** Content is only rendered once the session check resolved and the user is allowed on this page. */
	let canRenderContent = $derived(auth.checked && (auth.isAuthenticated || isLoginPage));
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-gray-100 transition-colors">
	{#if canRenderContent}
		{#if auth.isAuthenticated}
			<Nav />
		{/if}
		<main class="max-w-6xl mx-auto px-4 sm:px-6 py-8">
			{@render children()}
		</main>
	{/if}
</div>
