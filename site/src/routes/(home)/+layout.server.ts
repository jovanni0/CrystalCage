import type { Glossary }                       from "#lib/content/directives.ts"
import type { GalleryContent, HomeContent }    from "#lib/content/home-content.ts"
import      { readDataFile }                   from "#lib/content/content.ts"
import      { renderMarkdown }                 from "#lib/content/markdown.ts"



export async function load()
{
    const home    = readDataFile<HomeContent>("home")
    const why     = await renderMarkdown(home.why.body, readDataFile<Glossary>("glossary"))

    return { home, why_html: why.html }
}