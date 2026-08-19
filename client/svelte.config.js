import {vitePreprocess} from "@sveltejs/vite-plugin-svelte";
import adapter from "@sveltejs/adapter-static";



/** @type {import('@sveltejs/kit').Config} */
export default {
    preprocess: [
        vitePreprocess(),
        {
            name: 'strip-announcer',
            markup: ({ content: code }) => 
            {
                if (process.env.NODE_ENV !== 'development') 
                {
                    return { code }
                }

                code = code.replace(/<div id="svelte-announcer" [\s\S]*?<\/div>/, '')

                return { code }
            }
        },
    ],
    compilerOptions: {
        runes: ({ filename }) => (filename.includes("node_modules") ? undefined : true)
    },
    kit: {
        adapter: adapter({
            pages: "build",
            assets: "build",
            fallback: "index.html",
            precompress: false,
            strict: true,
        }),
        prerender: {
			handleUnseenRoutes: "ignore"
		},
        alias: {
            "@root": "./src",
            "@components": "./src/components",
            "@icons": "./src/icons",
            "@controllers": "./src/controllers",
            "@data-types": "./src/types",
            "@stores": "./src/stores",
            "@loggers": "./src/loggers",
            "@dtos": "./src/dtos",
        }
    },
    onwarn: (warning, handler) => 
    {
        if (warning.message.includes('https://svelte.dev/docs/kit/load#making-fetch-requests')) return;
        handler(warning);
    }
};