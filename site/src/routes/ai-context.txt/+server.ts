export const prerender = true



const DOC_FILES = import.meta.glob("/content/docs/*.md", { query: "?raw", import: "default", eager: true }) as Record<string, string>
const GLOSSARY  = import.meta.glob("/content/glossary.yaml", { query: "?raw", import: "default", eager: true }) as Record<string, string>



export function GET()
{
    const docs = Object.values(DOC_FILES).join("\n\n---\n\n")
    const glossary = Object.values(GLOSSARY).join("\n")

    const context = `${ docs }\n\n---\n\n# Glossary\n\n${ glossary }`

    return new Response(context, { headers: { "Content-Type": "text/plain; charset=utf-8" } })
}