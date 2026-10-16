import type { Root }              from "mdast"
import type { ContainerDirective, 
              LeafDirective, 
              TextDirective }     from "mdast-util-directive"
import      { visit }             from "unist-util-visit"



export type Glossary = Record<string, string>

type DirectiveNode = ContainerDirective | LeafDirective | TextDirective

type DirectiveOptions =
{
    glossary: Glossary
}



const CALLOUT_KINDS = ["note", "tip", "warning", "danger"]
const DIRECTIVE_TYPES = ["containerDirective", "leafDirective", "textDirective"]



export function remarkSanctumDirectives(options: DirectiveOptions)
{
    return (tree: Root) =>
    {
        visit(tree, (node) =>
        {
            const is_directive = DIRECTIVE_TYPES.includes(node.type)

            if (!is_directive)
            {
                return
            }

            transformDirective(node as DirectiveNode, options.glossary)
        })
    }
}



function transformDirective(node: DirectiveNode, glossary: Glossary)
{
    const is_container = node.type === "containerDirective"

    if (is_container && CALLOUT_KINDS.includes(node.name))
    {
        setElement(node, "callout-box", { kind: node.name })
        return
    }

    if (is_container && node.name === "tabs")
    {
        setElement(node, "tab-group", {})
        return
    }

    if (is_container && node.name === "tab")
    {
        setElement(node, "tab-panel", { label: requireAttribute(node, "label") })
        return
    }

    if (node.type === "textDirective" && node.name === "term")
    {
        const term_key = requireAttribute(node, "key")
        const definition = glossary[term_key]

        if (definition === undefined)
        {
            throw directiveError(node, `unknown glossary key "${ term_key }"`)
        }

        setElement(node, "help-term", { term: term_key, definition })
        return
    }

    throw directiveError(node, `unknown directive "${ node.name }"`)
}



function setElement(node: DirectiveNode, tag_name: string, properties: Record<string, string>)
{
    node.data = { ...node.data, hName: tag_name, hProperties: properties }
}



function requireAttribute(node: DirectiveNode, attribute_name: string): string
{
    const value = node.attributes?.[attribute_name]

    if (!value)
    {
        throw directiveError(node, `"${ node.name }" needs a ${ attribute_name } attribute`)
    }

    return value
}



function directiveError(node: DirectiveNode, message: string): Error
{
    const line = node.position?.start.line ?? "?"

    return new Error(`Markdown directive on line ${ line }: ${ message }`)
}