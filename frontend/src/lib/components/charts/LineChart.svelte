<script lang="ts">
	import Chart from 'chart.js/auto';
	import { theme } from '$lib/stores/theme.svelte.js';
	import { locale } from 'svelte-i18n';
	import type { MonthlyData } from '$lib/types/types.js';

	/** Monthly data — the {@code cumulativeBalance} field is used for the trend line. */
	let { data }: { data: MonthlyData[] } = $props();

	let canvas: HTMLCanvasElement | undefined = $state();

	function formatMonth(month: string): string {
		const [y, m] = month.split('-').map(Number);
		return new Intl.DateTimeFormat($locale ?? 'de-DE', {
			month: 'short',
			year:  '2-digit'
		}).format(new Date(y, m - 1, 1));
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
		if (!canvas) return;

		const isDark      = theme.dark;
		const textColor   = isDark ? '#94a3b8' : '#6b7280';
		const gridColor   = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';
		const lineColor   = 'rgb(59,130,246)';
		const fillColor   = isDark ? 'rgba(59,130,246,0.12)' : 'rgba(59,130,246,0.08)';

		const chart = new Chart(canvas, {
			type: 'line',
			data: {
				labels: data.map(d => formatMonth(d.month)),
				datasets: [
					{
						data:            data.map(d => d.cumulativeBalance),
						borderColor:     lineColor,
						backgroundColor: fillColor,
						fill:            true,
						tension:         0.35,
						pointRadius:     3,
						pointHoverRadius: 5,
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
