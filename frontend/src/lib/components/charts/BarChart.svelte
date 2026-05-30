<script lang="ts">
	import Chart from 'chart.js/auto';
	import { theme } from '$lib/stores/theme.svelte.js';
	import { locale } from 'svelte-i18n';
	import type { MonthlyData } from '$lib/types/types.js';

	/** Monthly data points to render; every entry becomes one column group. */
	let { data, incomeLabel, expensesLabel }: {
		data: MonthlyData[];
		incomeLabel: string;
		expensesLabel: string;
	} = $props();

	let canvas: HTMLCanvasElement | undefined = $state();

	/**
	 * Formats a month key ("yyyy-MM") into a short label like "Jan. 24".
	 * Uses the current locale from svelte-i18n.
	 */
	function formatMonth(month: string): string {
		const [y, m] = month.split('-').map(Number);
		return new Intl.DateTimeFormat($locale ?? 'de-DE', {
			month: 'short',
			year: '2-digit'
		}).format(new Date(y, m - 1, 1));
	}

	/** Formats a raw euro amount for use in axis ticks and tooltips. */
	function formatCurrency(value: number): string {
		return new Intl.NumberFormat($locale ?? 'de-DE', {
			style: 'currency',
			currency: 'EUR',
			minimumFractionDigits: 0,
			maximumFractionDigits: 0
		}).format(value);
	}

	$effect(() => {
		if (!canvas) return;

		// Reading theme.dark and locale here makes the effect re-run on theme/locale changes.
		const isDark    = theme.dark;
		const textColor = isDark ? '#94a3b8' : '#6b7280';
		const gridColor = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';

		const chart = new Chart(canvas, {
			type: 'bar',
			data: {
				labels: data.map(d => formatMonth(d.month)),
				datasets: [
					{
						label: incomeLabel,
						data: data.map(d => d.income),
						backgroundColor: 'rgba(59,130,246,0.7)',
						borderRadius: 4
					},
					{
						label: expensesLabel,
						data: data.map(d => d.expenses),
						backgroundColor: 'rgba(239,68,68,0.7)',
						borderRadius: 4
					}
				]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				plugins: {
					legend: {
						labels: { color: textColor, boxWidth: 12 }
					},
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
							color: textColor,
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
