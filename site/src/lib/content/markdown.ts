import type { Element, 
              Root as HastRoot }        from "hast"
import type { Glossary }                from "./directives"
import      { toString }                from "hast-util-to-string"
import      { unified }                 from "unified"
import      { visit }                   from "unist-util-visit"
import      { remarkSanctumDirectives } from "./directives"
import      rehypeSlug                  from "rehype-slug"
import      rehypeStringify             from "rehype-stringify"
import      remarkDirective             from "remark-directive"
import      remarkGfm                   from "remark-gfm"
import      remarkParse                 from "remark-parse"
import      remarkRehype                from "remark-rehype"



export type Heading =
{
    depth: number
    id: string
    text: string
}

export type RenderedMarkdown =
{
    html: string
    headings: Heading[]
}



const HEADING_DEPTHS: Record<string, number> = { h2: 2, h3: 3 }



export async function renderMarkdown(markdown: string, glossary: Glossary): Promise<RenderedMarkdown>
{
    const headings: Heading[] = []

    const file = await unified()
        .use(remarkParse)
        .use(remarkGfm)
        .use(remarkDirective)
        .use(remarkSanctumDirectives, { glossary })
        .use(remarkRehype)
        .use(rehypeSlug)
        .use(rehypeCollectHeadings, { headings })
        .use(rehypeStringify)
        .process(markdown)

    return { html: String(file), headings }
}



function rehypeCollectHeadings(options: { headings: Heading[] })
{
    return (tree: HastRoot) =>
    {
        visit(tree, "element", (node: Element) =>
        {
            const depth = HEADING_DEPTHS[node.tagName]

            if (depth === undefined)
            {
                return
            }

            options.headings.push({ depth, id: String(node.properties.id), text: toString(node) })
        })
    }
}