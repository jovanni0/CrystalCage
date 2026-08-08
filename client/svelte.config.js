import {vitePreprocess} from "@sveltejs/vite-plugin-svelte";
import adapter from "@sveltejs/adapter-static";



/** @type {import('@sveltejs/kit').Config} */
export default {
    preprocess: vitePreprocess(),
    compilerOptions: {
        runes: ({ filename }) => (filename.includes("node_modules") ? undefined : true)
    },
    kit: {
        adapter: adapter({
            pages: "build",
            assets: "build",
            fallback: "200.html",
            precompress: false,
            strict: true,
        }),
        alias: {
            "@root": "./src",
            "@components": "./src/components",
            "@icons": "./src/icons",
            "@controllers": "./src/controllers",
            "@services": "./src/services",
            "@data-types": "./src/types",
        }
    }
};