<script lang="ts">
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';

	// --- Types ---

	type ImportResult = {
		importJobId: number;
		status: string;
		imported: number;
		skipped: number;
		accountId: number;
	};

	type Account = {
		id: number;
		name: string;
		bankName: string;
		iban: string;
		currency: string;
		balance: number;
	};

	type State = 'idle' | 'uploading' | 'success' | 'error';

	// --- State ---

	let importState: State = $state('idle');
	let selectedFile: File | null = $state(null);
	let isDragging = $state(false);
	let result: ImportResult | null = $state(null);
	let account: Account | null = $state(null);
	let errorMessage = $state('');

	// --- File selection ---

	function onFileInput(event: Event) {
		const input = event.target as HTMLInputElement;
		if (input.files?.[0]) selectedFile = input.files[0];
	}

	function onDragOver(event: DragEvent) {
		event.preventDefault();
		isDragging = true;
	}

	function onDragLeave() {
		isDragging = false;
	}

	function onDrop(event: DragEvent) {
		event.preventDefault();
		isDragging = false;
		const file = event.dataTransfer?.files[0];
		if (file?.name.toLowerCase().endsWith('.csv')) selectedFile = file;
	}

	// --- Upload ---

	async function upload() {
		if (!selectedFile) return;

		importState = 'uploading';
		errorMessage = '';

		try {
			const body = new FormData();
			body.append('file', selectedFile);

			const res = await fetch(`${base}/api/import/ing`, { method: 'POST', body });

			if (!res.ok) {
				const text = await res.text();
				throw new Error(text || `HTTP ${res.status}`);
			}

			result = await res.json();

			// Load account details for the result summary
			const accountRes = await fetch(`${base}/api/accounts/${result!.accountId}`);
			if (accountRes.ok) account = await accountRes.json();

			importState = 'success';
		} catch (e) {
			errorMessage = e instanceof Error ? e.message : 'Unknown error';
			importState = 'error';
		}
	}

	function reset() {
		importState = 'idle';
		selectedFile = null;
		result = null;
		account = null;
		errorMessage = '';
	}

	// --- Formatting helpers ---

	// Intl.NumberFormat formats numbers according to locale:
	// 'de-DE' uses dots as thousands separator and comma as decimal → "14.514,69 €"
	function formatCurrency(amount: number, currency = 'EUR') {
		return new Intl.NumberFormat('de-DE', { style: 'currency', currency }).format(amount);
	}
</script>

<h1 class="text-2xl font-bold mb-6">{$_('import.title')}</h1>

<!-- Idle / File selection -->
{#if importState === 'idle' || importState === 'error'}
	<div class="max-w-lg space-y-4">
		<!-- Drop zone — ondragover/ondrop: browser file drag & drop API -->
		<div
			role="button"
			tabindex="0"
			aria-label={$_('import.drop_label')}
			ondragover={onDragOver}
			ondragleave={onDragLeave}
			ondrop={onDrop}
			class="relative flex flex-col items-center justify-center w-full h-44 rounded-xl border-2
				   border-dashed transition-colors cursor-pointer select-none
				   {isDragging
					? 'border-blue-500 bg-blue-50 dark:bg-blue-950/30'
					: 'border-gray-300 dark:border-slate-600 hover:border-blue-400 dark:hover:border-blue-500'}"
		>
			<label class="flex flex-col items-center gap-2 cursor-pointer w-full h-full justify-center">
				<Icon icon="heroicons:arrow-up-tray" class="w-10 h-10 text-gray-400 dark:text-slate-500" />

				{#if selectedFile}
					<span class="text-sm font-medium text-blue-600 dark:text-blue-400">{selectedFile.name}</span>
					<span class="text-xs text-gray-400 dark:text-slate-500">{$_('import.drop_change')}</span>
				{:else}
					<span class="text-sm text-gray-600 dark:text-slate-300">{$_('import.drop_hint')}</span>
					<span class="text-sm font-medium text-blue-600 dark:text-blue-400 underline">{$_('import.drop_action')}</span>
				{/if}

				<input type="file" accept=".csv" class="hidden" onchange={onFileInput} />
			</label>
		</div>

		<!-- Error message -->
		{#if importState === 'error'}
			<div class="flex items-start gap-3 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400">
				<Icon icon="heroicons:exclamation-circle" class="w-5 h-5 mt-0.5 shrink-0" />
				<p class="text-sm">{errorMessage}</p>
			</div>
		{/if}

		<!-- Upload button -->
		<button
			onclick={upload}
			disabled={!selectedFile}
			class="w-full py-2.5 rounded-xl font-medium text-sm transition-colors
				   bg-blue-600 hover:bg-blue-700 text-white
				   disabled:opacity-40 disabled:cursor-not-allowed"
		>
			{$_('import.button')}
		</button>
	</div>
{/if}

<!-- Uploading -->
{#if importState === 'uploading'}
	<div class="max-w-lg flex flex-col items-center gap-4 py-12 text-gray-500 dark:text-slate-400">
		<!-- animate-spin rotates the icon continuously -->
		<Icon icon="heroicons:arrow-path" class="w-10 h-10 animate-spin text-blue-600" />
		<p class="text-sm">{$_('import.uploading')}</p>
	</div>
{/if}

<!-- Success -->
{#if importState === 'success' && result}
	<div class="max-w-lg space-y-4">

		<!-- Header -->
		<div class="flex items-center gap-3 p-5 rounded-xl bg-green-50 dark:bg-green-950/30 border border-green-200 dark:border-green-800">
			<Icon icon="heroicons:check-circle" class="w-7 h-7 text-green-600 dark:text-green-400 shrink-0" />
			<div>
				<p class="font-semibold text-green-800 dark:text-green-300">{$_('import.success_title')}</p>
				<p class="text-xs text-green-700 dark:text-green-500">
					{$_('import.success_job', { values: { id: result.importJobId } })}
				</p>
			</div>
		</div>

		<!-- Import stats — grid-cols-2: two equal columns on all screen sizes -->
		<div class="grid grid-cols-2 gap-3">
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-blue-600 dark:text-blue-400">{result.imported}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('import.stat_imported')}</p>
			</div>
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-4 shadow-sm text-center">
				<p class="text-3xl font-bold text-gray-400 dark:text-slate-500">{result.skipped}</p>
				<p class="text-xs text-gray-500 dark:text-slate-400 mt-1">{$_('import.stat_skipped')}</p>
			</div>
		</div>

		<!-- Account details -->
		{#if account}
			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm space-y-3">
				<h2 class="font-semibold text-sm text-gray-500 dark:text-slate-400 uppercase tracking-wide">
					{$_('import.account_section')}
				</h2>
				<div class="flex items-center justify-between">
					<div>
						<p class="font-semibold">{account.name}</p>
						<p class="text-sm text-gray-500 dark:text-slate-400">{account.bankName}</p>
					</div>
					<p class="text-lg font-bold tabular-nums">
						{formatCurrency(account.balance, account.currency)}
					</p>
				</div>
				<!-- font-mono: monospaced font makes IBAN easier to read -->
				<p class="text-xs font-mono text-gray-400 dark:text-slate-500 tracking-wider">{account.iban}</p>
			</div>
		{/if}

		<button
			onclick={reset}
			class="w-full py-2.5 rounded-xl font-medium text-sm transition-colors
				   border border-gray-300 dark:border-slate-600
				   hover:bg-gray-100 dark:hover:bg-slate-700"
		>
			{$_('import.restart')}
		</button>
	</div>
{/if}
