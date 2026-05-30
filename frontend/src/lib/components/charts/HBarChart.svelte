<script lang="ts">
	import Chart from 'chart.js/auto';
	import { theme } from '$lib/stores/theme.svelte.js';
	import { locale } from 'svelte-i18n';
	import type { CategoryData } from '$lib/types/types.js';

	/** Fallback colour for the uncategorized bucket (color === null). */
	const UNCATEGORIZED_COLOR = '#9CA3AF';

	/** Category data; only the top entries are shown (sliced by the parent). */
	let { data, uncategorizedLabel }: {
		data: CategoryData[];
		uncategorizedLabel: string;
	} = $props();

	let canvas: HTMLCanvasElement | undefined = $state();

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

		const isDark    = theme.dark;
		const textColor = isDark ? '#94a3b8' : '#6b7280';
		const gridColor = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';

		const chart = new Chart(canvas, {
			type: 'bar',
			data: {
				labels: data.map(d => d.categoryName ?? uncategorizedLabel),
				datasets: [
					{
						data:            data.map(d => d.amount),
						backgroundColor: data.map(d => (d.color ?? UNCATEGORIZED_COLOR) + 'cc'),
						borderRadius:    4
					}
				]
			},
			options: {
				indexAxis:          'y',
				responsive:         true,
				maintainAspectRatio: false,
				plugins: {
					legend: { display: false },
					tooltip: {
						callbacks: {
							label: ctx => ` ${formatCurrency(ctx.parsed.x ?? 0)}`
						}
					}
				},
				scales: {
					x: {
						ticks: {
							color:    textColor,
							callback: val => formatCurrency(Number(val))
						},
						grid: { color: gridColor }
					},
					y: {
						ticks: { color: textColor },
						grid:  { display: false }
					}
				}
			}
		});

		return () => chart.destroy();
	});
</script>

<canvas bind:this={canvas}></canvas>
