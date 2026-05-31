<script lang="ts">
	import { base } from '$app/paths';
	import { page } from '$app/state';
	import Icon from '@iconify/svelte';
	import { theme } from '$lib/stores/theme.svelte';
	import { locale, setLocale, type Locale } from '$lib/i18n/index.js';
	import { _ } from 'svelte-i18n';

	// Nav links are reactive so their labels update when the locale changes.
	let links = $derived([
		{ href: `${base}/`, label: $_('nav.dashboard') },
		{ href: `${base}/accounts`, label: $_('nav.accounts') },
		{ href: `${base}/transactions`, label: $_('nav.transactions') },
		{ href: `${base}/categories`, label: $_('nav.categories') },
		{ href: `${base}/category-rules`, label: $_('nav.rules') },
		{ href: `${base}/import`, label: $_('nav.import') },
		{ href: `${base}/auswertungen`, label: $_('nav.reports') }
	]);

	let menuOpen = $state(false);

	/** Available locales with their display metadata. */
	const LOCALES: { code: Locale; flag: string; label: string }[] = [
		{ code: 'de', flag: 'circle-flags:de', label: 'DE' },
		{ code: 'en', flag: 'circle-flags:gb', label: 'EN' }
	];

	let currentLocale = $derived($locale as Locale);
</script>

<nav class="bg-slate-800 dark:bg-slate-950 text-slate-100 shadow-md">
	<div class="max-w-6xl mx-auto px-4 sm:px-6">
		<div class="flex items-center justify-between h-14">

			<span class="font-bold text-lg tracking-wide">{$_('nav.app_name')}</span>

			<!-- Desktop links -->
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
				<!-- Language switcher: shows all locales, active one is highlighted -->
				<div class="flex items-center rounded-md overflow-hidden border border-slate-600">
					{#each LOCALES as loc}
						<button
							onclick={() => setLocale(loc.code)}
							disabled={loc.code === currentLocale}
							aria-label="Switch to {loc.label}"
							aria-current={loc.code === currentLocale ? 'true' : undefined}
							class="flex items-center gap-1.5 px-2 py-1 text-xs font-medium transition-colors
								{loc.code === currentLocale
									? 'bg-slate-600 text-white cursor-default'
									: 'text-slate-400 hover:bg-slate-700 hover:text-white'}"
						>
							<Icon icon={loc.flag} class="w-4 h-4" />
							<span>{loc.label}</span>
						</button>
					{/each}
				</div>

				<!-- Dark mode toggle -->
				<button
					onclick={() => theme.toggle()}
					class="p-2 rounded-md text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
					aria-label={$_('nav.toggle_dark_mode')}
				>
					<Icon
						icon={theme.dark ? 'heroicons:sun' : 'heroicons:moon'}
						class="w-5 h-5"
					/>
				</button>

				<!-- Hamburger (mobile only) -->
				<button
					onclick={() => (menuOpen = !menuOpen)}
					class="md:hidden p-2 rounded-md text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
					aria-label={$_('nav.open_menu')}
				>
					<Icon icon={menuOpen ? 'heroicons:x-mark' : 'heroicons:bars-3'} class="w-5 h-5" />
				</button>
			</div>
		</div>

		<!-- Mobile menu -->
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
