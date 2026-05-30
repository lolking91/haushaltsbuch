<script lang="ts">
	import Chart from 'chart.js/auto';
	import { theme } from '$lib/stores/theme.svelte.js';
	import { locale } from 'svelte-i18n';
	import type { CategoryData } from '$lib/types/types.js';

	/** Fallback colour for the uncategorized bucket (color === null). */
	const UNCATEGORIZED_COLOR = '#9CA3AF';

	/** Category expense data to render as a donut. */
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

		const chart = new Chart(canvas, {
			type: 'doughnut',
			data: {
				labels: data.map(d => d.categoryName ?? uncategorizedLabel),
				datasets: [
					{
						data:            data.map(d => d.amount),
						backgroundColor: data.map(d => (d.color ?? UNCATEGORIZED_COLOR) + 'cc'), // 80 % opacity
						borderColor:     isDark ? '#1e293b' : '#ffffff',
						borderWidth:     2,
						hoverOffset:     6
					}
				]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				cutout: '60%',
				plugins: {
					legend: {
						position: 'bottom',
						labels: {
							color:     textColor,
							boxWidth:  12,
							padding:   10,
							font:      { size: 11 }
						}
					},
					tooltip: {
						callbacks: {
							label: ctx => ` ${formatCurrency(ctx.parsed)}`
						}
					}
				}
			}
		});

		return () => chart.destroy();
	});
</script>

<canvas bind:this={canvas}></canvas>
