<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import { page } from '$app/state';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { formatCurrency, formatDate } from '$lib/utils/format.js';
	import type { Etf, EtfSnapshot } from '$lib/types/types.js';
	import { etfsApi } from '$lib/api/etfs.js';
	import { ApiError } from '$lib/api/client.js';
	import EtfLineChart from '$lib/components/charts/EtfLineChart.svelte';

	// --- State ---

	let etf: Etf | null = $state(null);
	let snapshots: EtfSnapshot[] = $state([]);
	let loading = $state(true);
	let errorMessage = $state('');

	// Edit form
	let form = $state({ name: '', wkn: '', isin: '', brokerName: '', notes: '' });
	let saving = $state(false);
	let saveStatus: 'idle' | 'success' | 'error' = $state('idle');
	let serverFieldErrors = $state<Record<string, string>>({});

	// New snapshot form
	let snapshotForm = $state({ date: '', totalValue: '' as string });
	let addingSnapshot = $state(false);
	let snapshotError = $state('');

	// Delete ETF
	let confirmDeleteEtf = $state(false);
	let deleteEtfError = $state('');
	let deletingEtf = $state(false);

	// Delete snapshot
	let confirmDeleteSnapshotId: number | null = $state(null);

	// --- Derived ---

	let etfId = $derived(Number(page.params.id));

	/** True when the WKN in the form is syntactically valid. */
	let wknValid = $derived(/^[A-Za-z0-9]{6}$/.test(form.wkn.trim()));

	/** True when the form differs from the loaded ETF data. */
	let isDirty = $derived(
		etf !== null && (
			form.name !== (etf.name ?? '') ||
			form.wkn !== (etf.wkn ?? '') ||
			form.isin !== (etf.isin ?? '') ||
			form.brokerName !== (etf.brokerName ?? '') ||
			form.notes !== (etf.notes ?? '')
		)
	);

	let canSave = $derived(isDirty && form.name.trim().length > 0 && wknValid);

	let canAddSnapshot = $derived(
		snapshotForm.date.length > 0 && snapshotForm.totalValue !== '' && Number(snapshotForm.totalValue) > 0
	);

	// --- Data loading ---

	onMount(async () => {
		await loadData();
	});

	async function loadData() {
		loading = true;
		errorMessage = '';
		try {
			[etf, snapshots] = await Promise.all([
				etfsApi.getById(etfId),
				etfsApi.getSnapshots(etfId)
			]);
			// Populate edit form with loaded data
			form = {
				name:       etf.name ?? '',
				wkn:        etf.wkn ?? '',
				isin:       etf.isin ?? '',
				brokerName: etf.brokerName ?? '',
				notes:      etf.notes ?? ''
			};
		} catch (e) {
			errorMessage = e instanceof Error ? e.message : String(e);
		} finally {
			loading = false;
		}
	}

	// --- Actions ---

	function resetForm() {
		if (!etf) return;
		form = {
			name:       etf.name ?? '',
			wkn:        etf.wkn ?? '',
			isin:       etf.isin ?? '',
			brokerName: etf.brokerName ?? '',
			notes:      etf.notes ?? ''
		};
		saveStatus = 'idle';
		serverFieldErrors = {};
	}

	async function save() {
		saving = true;
		saveStatus = 'idle';
		serverFieldErrors = {};
		try {
			etf = await etfsApi.update(etfId, {
				name:       form.name.trim(),
				wkn:        form.wkn.trim().toUpperCase(),
				isin:       form.isin.trim() || undefined,
				brokerName: form.brokerName.trim() || undefined,
				notes:      form.notes.trim() || undefined
			});
			saveStatus = 'success';
		} catch (e: unknown) {
			if (e instanceof ApiError && e.status === 400) {
				try {
					serverFieldErrors = JSON.parse(e.message) as Record<string, string>;
				} catch {
					saveStatus = 'error';
				}
			} else if (e instanceof ApiError && e.status === 409) {
				serverFieldErrors = { wkn: e.message };
			} else {
				saveStatus = 'error';
			}
		} finally {
			saving = false;
		}
	}

	async function addSnapshot() {
		addingSnapshot = true;
		snapshotError = '';
		try {
			const created = await etfsApi.addSnapshot(etfId, {
				date:       snapshotForm.date,
				totalValue: Number(snapshotForm.totalValue)
			});
			// Upsert in local list: replace if same date, otherwise insert sorted
			const idx = snapshots.findIndex(s => s.date === created.date);
			if (idx >= 0) {
				snapshots = snapshots.map((s, i) => (i === idx ? created : s));
			} else {
				snapshots = [...snapshots, created].sort((a, b) => a.date.localeCompare(b.date));
			}
			snapshotForm = { date: '', totalValue: '' };
			// Also refresh the ETF to update latestSnapshot
			etf = await etfsApi.getById(etfId);
		} catch (e) {
			snapshotError = e instanceof Error ? e.message : String(e);
		} finally {
			addingSnapshot = false;
		}
	}

	async function deleteSnapshot(snapshotId: number) {
		try {
			await etfsApi.deleteSnapshot(etfId, snapshotId);
			snapshots = snapshots.filter(s => s.id !== snapshotId);
			confirmDeleteSnapshotId = null;
			etf = await etfsApi.getById(etfId);
		} catch (e) {
			snapshotError = e instanceof Error ? e.message : String(e);
		}
	}

	async function deleteEtf() {
		deletingEtf = true;
		deleteEtfError = '';
		try {
			await etfsApi.delete(etfId);
			await goto(`${base}/portfolio`);
		} catch (e: unknown) {
			const msg = e instanceof Error ? e.message : String(e);
			deleteEtfError = msg.includes('409') || msg.toLowerCase().includes('snapshot')
				? $_('portfolio.delete_has_snapshots')
				: $_('common.delete_error');
			deletingEtf = false;
		}
	}
</script>

<div class="space-y-6">

	<!-- Back link -->
	<a
		href="{base}/portfolio"
		class="inline-flex items-center gap-1.5 text-sm text-gray-500 dark:text-slate-400
		       hover:text-gray-900 dark:hover:text-white transition-colors"
	>
		<Icon icon="heroicons:arrow-left" class="w-4 h-4" />
		{$_('common.back_to_overview')}
	</a>

	<!-- Loading -->
	{#if loading}
		<div class="flex items-center justify-center gap-3 py-24 text-gray-400 dark:text-slate-500">
			<Icon icon="heroicons:arrow-path" class="w-6 h-6 animate-spin text-blue-600" />
			<span class="text-sm">{$_('portfolio.loading')}</span>
		</div>

	<!-- Error -->
	{:else if errorMessage}
		<div class="flex items-start gap-3 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400">
			<Icon icon="heroicons:exclamation-circle" class="w-5 h-5 mt-0.5 shrink-0" />
			<p class="text-sm">{errorMessage}</p>
		</div>

	{:else if etf}
		<h1 class="text-2xl font-bold text-gray-900 dark:text-white">{etf.name}</h1>

		<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

			<!-- ── Left column: Edit form ──────────────────────────────────── -->
			<div class="space-y-4">

				<!-- Edit card -->
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm">

					<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
						<Icon icon="heroicons:pencil-square" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
						<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">{$_('common.section_edit')}</h2>
					</div>

					<form
						onsubmit={(e) => { e.preventDefault(); save(); }}
						class="px-5 py-4 space-y-4"
					>
						<!-- Name -->
						<div>
							<label for="name" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
								{$_('portfolio.field_name')} *
							</label>
							<input
								id="name"
								type="text"
								bind:value={form.name}
								class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
							/>
						</div>

						<!-- WKN -->
						<div>
							<label for="wkn" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
								{$_('portfolio.field_wkn')} *
							</label>
							<input
								id="wkn"
								type="text"
								bind:value={form.wkn}
								maxlength="6"
								class="w-full px-3 py-2 rounded-lg border text-sm font-mono outline-none focus:ring-2
								       {form.wkn && !wknValid
								         ? 'border-red-400 dark:border-red-600 focus:ring-red-500 bg-red-50 dark:bg-red-950/20'
								         : 'border-gray-300 dark:border-slate-600 focus:ring-blue-500 bg-white dark:bg-slate-900'}"
							/>
							{#if serverFieldErrors.wkn}
								<p class="mt-1 text-xs text-red-600 dark:text-red-400">{serverFieldErrors.wkn}</p>
							{/if}
						</div>

						<!-- ISIN -->
						<div>
							<label for="isin" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
								{$_('portfolio.field_isin')}
							</label>
							<input
								id="isin"
								type="text"
								bind:value={form.isin}
								maxlength="12"
								class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm font-mono outline-none focus:ring-2 focus:ring-blue-500"
							/>
						</div>

						<!-- Broker -->
						<div>
							<label for="brokerName" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
								{$_('portfolio.field_broker')}
							</label>
							<input
								id="brokerName"
								type="text"
								bind:value={form.brokerName}
								class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
							/>
						</div>

						<!-- Notes -->
						<div>
							<label for="notes" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
								{$_('portfolio.field_notes')}
							</label>
							<textarea
								id="notes"
								bind:value={form.notes}
								rows="3"
								class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500 resize-none"
							></textarea>
						</div>

						<!-- Status feedback -->
						{#if saveStatus === 'success'}
							<p class="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
								<Icon icon="heroicons:check-circle" class="w-4 h-4" />
								{$_('common.save_success')}
							</p>
						{:else if saveStatus === 'error'}
							<p class="text-xs text-red-600 dark:text-red-400 flex items-center gap-1">
								<Icon icon="heroicons:exclamation-circle" class="w-4 h-4" />
								{$_('common.save_error')}
							</p>
						{/if}

						<!-- Buttons -->
						<div class="flex justify-between items-center pt-1">
							<button
								type="button"
								onclick={resetForm}
								disabled={!isDirty}
								class="px-4 py-2 rounded-lg text-sm font-medium border border-gray-300 dark:border-slate-600
								       hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors
								       disabled:opacity-40 disabled:cursor-not-allowed"
							>
								{$_('common.btn_reset')}
							</button>
							<button
								type="submit"
								disabled={!canSave || saving}
								class="px-4 py-2 rounded-lg text-sm font-medium bg-blue-600 hover:bg-blue-700 text-white
								       transition-colors disabled:opacity-40 disabled:cursor-not-allowed
								       flex items-center gap-1.5"
							>
								{#if saving}
									<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
								{/if}
								{$_('common.btn_save')}
							</button>
						</div>
					</form>
				</div>

				<!-- Delete ETF card -->
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-red-200 dark:border-red-800 shadow-sm p-5">
					{#if confirmDeleteEtf}
						<p class="text-sm font-medium text-gray-800 dark:text-slate-200 mb-3">
							{$_('portfolio.delete_confirm')}
						</p>
						{#if deleteEtfError}
							<p class="text-xs text-red-600 dark:text-red-400 mb-3">{deleteEtfError}</p>
						{/if}
						<div class="flex gap-2">
							<button
								onclick={deleteEtf}
								disabled={deletingEtf}
								class="px-3 py-1.5 rounded-lg text-sm font-medium bg-red-600 hover:bg-red-700 text-white
								       transition-colors disabled:opacity-40 flex items-center gap-1.5"
							>
								{#if deletingEtf}
									<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
								{/if}
								{$_('portfolio.delete_yes')}
							</button>
							<button
								onclick={() => { confirmDeleteEtf = false; deleteEtfError = ''; }}
								class="px-3 py-1.5 rounded-lg text-sm font-medium border border-gray-300 dark:border-slate-600
								       hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
							>
								{$_('common.btn_cancel')}
							</button>
						</div>
					{:else}
						<button
							onclick={() => (confirmDeleteEtf = true)}
							class="text-sm font-medium text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300
							       flex items-center gap-2 transition-colors"
						>
							<Icon icon="heroicons:trash" class="w-4 h-4" />
							{$_('portfolio.btn_delete_etf')}
						</button>
					{/if}
				</div>
			</div>

			<!-- ── Right column: Chart + Snapshots ────────────────────────── -->
			<div class="space-y-4">

				<!-- Chart (only when ≥2 snapshots) -->
				{#if snapshots.length >= 2}
					<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm p-5">
						<h2 class="font-semibold text-sm text-gray-500 dark:text-slate-400 uppercase tracking-wide mb-4">
							{$_('portfolio.chart_title')}
						</h2>
						<div class="h-48">
							<EtfLineChart {snapshots} />
						</div>
					</div>
				{/if}

				<!-- Add snapshot form -->
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm">
					<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex items-center gap-2">
						<Icon icon="heroicons:plus-circle" class="w-4 h-4 text-gray-400 dark:text-slate-500" />
						<h2 class="font-semibold text-sm text-gray-700 dark:text-slate-200">
							{$_('portfolio.snapshot_add_title')}
						</h2>
					</div>
					<form
						onsubmit={(e) => { e.preventDefault(); addSnapshot(); }}
						class="px-5 py-4 flex flex-col sm:flex-row gap-3"
					>
						<div class="flex-1">
							<label for="snapDate" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
								{$_('portfolio.snapshot_field_date')}
							</label>
							<input
								id="snapDate"
								type="date"
								bind:value={snapshotForm.date}
								class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
							/>
						</div>
						<div class="flex-1">
							<label for="snapValue" class="block text-xs font-medium text-gray-500 dark:text-slate-400 mb-1">
								{$_('portfolio.snapshot_field_value')}
							</label>
							<input
								id="snapValue"
								type="number"
								step="0.01"
								min="0"
								bind:value={snapshotForm.totalValue}
								class="w-full px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								       bg-white dark:bg-slate-900 text-sm outline-none focus:ring-2 focus:ring-blue-500"
							/>
						</div>
						<div class="flex items-end">
							<button
								type="submit"
								disabled={!canAddSnapshot || addingSnapshot}
								class="px-4 py-2 rounded-lg text-sm font-medium bg-blue-600 hover:bg-blue-700 text-white
								       transition-colors disabled:opacity-40 disabled:cursor-not-allowed
								       flex items-center gap-1.5 whitespace-nowrap"
							>
								{#if addingSnapshot}
									<Icon icon="heroicons:arrow-path" class="w-4 h-4 animate-spin" />
								{/if}
								{$_('portfolio.snapshot_btn_add')}
							</button>
						</div>
					</form>
					{#if snapshotError}
						<p class="px-5 pb-4 text-xs text-red-600 dark:text-red-400">{snapshotError}</p>
					{/if}
				</div>

				<!-- Snapshot history table -->
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm overflow-hidden">
					<div class="px-5 py-4 border-b border-gray-100 dark:border-slate-700">
						<h2 class="font-semibold text-sm text-gray-500 dark:text-slate-400 uppercase tracking-wide">
							{$_('portfolio.snapshots_title')}
						</h2>
					</div>

					{#if snapshots.length === 0}
						<p class="px-5 py-8 text-center text-sm text-gray-400 dark:text-slate-500">
							{$_('portfolio.snapshots_empty')}
						</p>
					{:else}
						<table class="w-full text-sm">
							<thead>
								<tr class="border-b border-gray-100 dark:border-slate-700">
									<th class="text-left px-4 py-2 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('portfolio.snapshot_col_date')}</th>
									<th class="text-right px-4 py-2 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('portfolio.snapshot_col_value')}</th>
									<th class="text-right px-4 py-2 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide hidden sm:table-cell">{$_('portfolio.snapshot_col_gain')}</th>
									<th class="px-4 py-2"></th>
								</tr>
							</thead>
							<!-- Newest first for display -->
							<tbody class="divide-y divide-gray-100 dark:divide-slate-700">
								{#each [...snapshots].reverse() as snapshot (snapshot.id)}
									<tr class="hover:bg-gray-50 dark:hover:bg-slate-700/50 transition-colors">
										<td class="px-4 py-2.5 text-gray-700 dark:text-slate-300">{formatDate(snapshot.date)}</td>
										<td class="px-4 py-2.5 text-right font-semibold tabular-nums">{formatCurrency(snapshot.totalValue)}</td>
										<td class="px-4 py-2.5 text-right hidden sm:table-cell">
											{#if snapshot.gainAbsolute != null}
												<span class="tabular-nums {snapshot.gainAbsolute >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}">
													{snapshot.gainAbsolute >= 0 ? '+' : ''}{formatCurrency(snapshot.gainAbsolute)}
												</span>
												{#if snapshot.gainPercent != null}
													<span class="text-xs ml-1 {snapshot.gainPercent >= 0 ? 'text-green-500' : 'text-red-500'}">
														({snapshot.gainPercent >= 0 ? '+' : ''}{snapshot.gainPercent.toFixed(2)} %)
													</span>
												{/if}
											{:else}
												<span class="text-gray-400 dark:text-slate-500">–</span>
											{/if}
										</td>
										<td class="px-4 py-2.5 text-right">
											{#if confirmDeleteSnapshotId === snapshot.id}
												<div class="flex items-center justify-end gap-2">
													<span class="text-xs text-gray-600 dark:text-slate-300">{$_('portfolio.snapshot_delete_confirm')}</span>
													<button
														onclick={() => deleteSnapshot(snapshot.id)}
														class="text-xs px-2 py-1 rounded-lg bg-red-600 hover:bg-red-700 text-white transition-colors"
													>
														{$_('portfolio.snapshot_delete_yes')}
													</button>
													<button
														onclick={() => (confirmDeleteSnapshotId = null)}
														class="text-xs px-2 py-1 rounded-lg border border-gray-300 dark:border-slate-600
														       hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
													>
														{$_('common.btn_cancel')}
													</button>
												</div>
											{:else}
												<button
													onclick={() => (confirmDeleteSnapshotId = snapshot.id)}
													class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
													       hover:bg-red-50 dark:hover:bg-red-950/30 hover:text-red-600 dark:hover:text-red-400 transition-colors"
													aria-label="Delete snapshot"
												>
													<Icon icon="heroicons:trash" class="w-4 h-4" />
												</button>
											{/if}
										</td>
									</tr>
								{/each}
							</tbody>
						</table>
					{/if}
				</div>
			</div>
		</div>
	{/if}
</div>
