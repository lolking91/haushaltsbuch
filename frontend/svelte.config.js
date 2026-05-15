import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	compilerOptions: {
		runes: ({ filename }) => (filename.split(/[/\\]/).includes('node_modules') ? undefined : true)
	},
	kit: {
		adapter: adapter({
			pages: '../backend/src/main/resources/static',
			assets: '../backend/src/main/resources/static',
			fallback: 'index.html'
		}),
		paths: {
			base: '/banking'
		}
	}
};

export default config;
