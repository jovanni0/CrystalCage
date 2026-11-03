import { defineEnvVars } from "@sveltejs/kit/env"



export const variables = defineEnvVars({
    PUBLIC_GA_MEASUREMENT_ID: {
        public:      true,
        static:      true,
        description: "Google Analytics 4 measurement ID (G-XXXXXXXXXX).",
        schema:      (value) =>
        {
            if (value === undefined || !/^G-[A-Z0-9]+$/.test(value))
            {
                throw new Error("PUBLIC_GA_MEASUREMENT_ID must look like G-XXXXXXXXXX")
            }

            return value
        },
    },
    PUBLIC_OLLAMA_URL: {
        public:      true,
        static:      true,
        description: "Base URL of the Ollama server used by the chat.",
        schema:      (value) => value ?? "http://localhost:11434",
    },
    PUBLIC_OLLAMA_MODEL: {
        public:      true,
        static:      true,
        description: "Ollama model name used by the chat.",
        schema:      (value) => value ?? "qwen2.5:7b",
    },
})