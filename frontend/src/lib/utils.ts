import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Merges Tailwind CSS class names, resolving conflicts via tailwind-merge
 * and handling conditionals via clsx.
 */
export function cn(...inputs: ClassValue[]): string {
	return twMerge(clsx(inputs));
}
