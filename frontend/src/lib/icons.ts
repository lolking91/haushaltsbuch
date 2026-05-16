import { addCollection } from '@iconify/svelte';
import heroiconsData from '@iconify-json/heroicons/icons.json';
import circleFlagsData from '@iconify-json/circle-flags/icons.json';

// Pre-loads both icon collections so all icons render from the local bundle
// without any Iconify CDN requests — required for offline use.
addCollection(heroiconsData as never);
addCollection(circleFlagsData as never);
