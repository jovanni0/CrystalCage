import { marked }  from "marked"
import DOMPurify   from "dompurify"


const ALLOWED_TAGS = [
    "p", "br", "hr", "strong", "em", "del", "code", "pre", "blockquote",
    "ul", "ol", "li", "h1", "h2", "h3", "h4", "h5", "h6", "a",
]
const ALLOWED_ATTR = ["href", "title"]


// runs once when the module loads: every link opens outside the app and can't reach back into it
DOMPurify.addHook("afterSanitizeAttributes", node =>
{
    if (node.tagName === "A")
    {
        node.setAttribute("target", "_blank")
        node.setAttribute("rel", "noopener noreferrer")
    }
})


export function renderMarkdown(source: string): string
{
    if (!source.trim()) return ""

    const html = marked.parse(source, { async: false, gfm: true, breaks: true }) as string

    return DOMPurify.sanitize(html, {
        ALLOWED_TAGS,
        ALLOWED_ATTR,
        ALLOWED_URI_REGEXP: /^(https?:|mailto:)/i,   // only web and mail links
    })
}