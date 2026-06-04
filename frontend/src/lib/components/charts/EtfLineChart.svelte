<script lang="ts">
	import Chart from 'chart.js/auto';
	import { theme } from '$lib/stores/theme.svelte.js';
	import { locale } from 'svelte-i18n';
	import type { EtfSnapshot } from '$lib/types/types.js';

	/** Snapshot list to display — must be ordered by date ascending. */
	let { snapshots }: { snapshots: EtfSnapshot[] } = $props();

	let canvas: HTMLCanvasElement | undefined = $state();

	function formatDate(dateStr: string): string {
		return new Intl.DateTimeFormat($locale ?? 'de-DE', {
			day:   '2-digit',
			month: 'short',
			year:  '2-digit'
		}).format(new Date(dateStr));
	}

	function formatCurrency(value: number): string {
		return new Intl.NumberFormat($locale ?? 'de-DE', {
			style:                 'currency',
			currency:              'EUR',
			minimumFractionDigits: 0,
			maximumFractionDigits: 0
		}).format(value);
	}

	$effect(() => {
		if (!canvas || snapshots.length === 0) return;

		const isDark    = theme.dark;
		const textColor = isDark ? '#94a3b8' : '#6b7280';
		const gridColor = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';
		const lineColor = 'rgb(59,130,246)';
		const fillColor = isDark ? 'rgba(59,130,246,0.12)' : 'rgba(59,130,246,0.08)';

		const chart = new Chart(canvas, {
			type: 'line',
			data: {
				labels: snapshots.map(s => formatDate(s.date)),
				datasets: [
					{
						data:               snapshots.map(s => s.totalValue),
						borderColor:        lineColor,
						backgroundColor:    fillColor,
						fill:               true,
						tension:            0.35,
						pointRadius:        4,
						pointHoverRadius:   6,
						pointBackgroundColor: lineColor
					}
				]
			},
			options: {
				responsive:          true,
				maintainAspectRatio: false,
				plugins: {
					legend: { display: false },
					tooltip: {
						callbacks: {
							label: ctx => ` ${formatCurrency(ctx.parsed.y ?? 0)}`
						}
					}
				},
				scales: {
					x: {
						ticks: { color: textColor },
						grid:  { color: gridColor }
					},
					y: {
						ticks: {
							color:    textColor,
							callback: val => formatCurrency(Number(val))
						},
						grid: { color: gridColor }
					}
				}
			}
		});

		return () => chart.destroy();
	});
</script>

<canvas bind:this={canvas}></canvas>
