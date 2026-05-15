<script lang="ts">
	import { base } from '$app/paths';
	import { page } from '$app/state';
	import { theme } from '$lib/stores/theme.svelte';

	const links = [
		{ href: `${base}/`, label: 'Dashboard' },
		{ href: `${base}/transactions`, label: 'Transaktionen' },
		{ href: `${base}/import`, label: 'CSV Import' },
		{ href: `${base}/auswertungen`, label: 'Auswertungen' }
	];

	let menuOpen = $state(false);
</script>

<nav class="bg-slate-800 dark:bg-slate-950 text-slate-100 shadow-md">
	<div class="max-w-6xl mx-auto px-4 sm:px-6">
		<div class="flex items-center justify-between h-14">

			<span class="font-bold text-lg tracking-wide">Haushaltsbuch</span>

			<!-- Desktop-Links -->
			<ul class="hidden md:flex gap-1">
				{#each links as link}
					<li>
						<a
							href={link.href}
							class="px-3 py-1.5 rounded-md text-sm transition-colors
								{page.url.pathname === link.href
									? 'bg-blue-600 text-white'
									: 'text-slate-300 hover:bg-slate-700 hover:text-white'}"
						>
							{link.label}
						</a>
					</li>
				{/each}
			</ul>

			<div class="flex items-center gap-1">
				<!-- Dark-Mode-Toggle -->
				<button
					onclick={() => theme.toggle()}
					class="p-2 rounded-md text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
					aria-label="Dark Mode umschalten"
				>
					{#if theme.dark}
						<!-- Sonne -->
						<svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
								d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364-6.364l-.707.707M6.343 17.657l-.707.707M17.657 17.657l-.707-.707M6.343 6.343l-.707-.707M12 7a5 5 0 100 10 5 5 0 000-10z"/>
						</svg>
					{:else}
						<!-- Mond -->
						<svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
								d="M21 12.79A9 9 0 1111.21 3a7 7 0 109.79 9.79z"/>
						</svg>
					{/if}
				</button>

				<!-- Hamburger (nur mobil) -->
				<button
					onclick={() => (menuOpen = !menuOpen)}
					class="md:hidden p-2 rounded-md text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
					aria-label="Menü öffnen"
				>
					{#if menuOpen}
						<svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
						</svg>
					{:else}
						<svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
						</svg>
					{/if}
				</button>
			</div>
		</div>

		<!-- Mobile-Menü -->
		{#if menuOpen}
			<ul class="md:hidden pb-3 flex flex-col gap-1">
				{#each links as link}
					<li>
						<a
							href={link.href}
							onclick={() => (menuOpen = false)}
							class="block px-3 py-2 rounded-md text-sm transition-colors
								{page.url.pathname === link.href
									? 'bg-blue-600 text-white'
									: 'text-slate-300 hover:bg-slate-700 hover:text-white'}"
						>
							{link.label}
						</a>
					</li>
				{/each}
			</ul>
		{/if}
	</div>
</nav>
