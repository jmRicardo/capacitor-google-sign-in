import { defineConfig } from 'vite';

export default defineConfig({
  root: './src',
  // The plugin is linked from `..`, which has its own @capacitor/core: keep a single copy.
  resolve: { dedupe: ['@capacitor/core'] },
  build: {
    outDir: '../dist',
    minify: false,
    emptyOutDir: true,
  },
});
