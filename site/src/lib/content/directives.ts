import type { Root }              from "mdast"
import type { ContainerDirective, 
              LeafDirective, 
              TextDirective }     from "mdast-util-directive"
import      { toString }          from "mdast-util-to-string"
import      { visit }             from "unist-util-visit"



const CALLOUT_TITLES: Record<string, string> = {
    note:    "Note",
    tip:     "Tip",
    warning: "Warning",
    danger:  "Danger",
}

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
        let term_count = 0

        const nextTermId = () =>
        {
            term_count += 1
            return `term-${ term_count }`
        }

        visit(tree, (node) =>
        {
            const is_directive = DIRECTIVE_TYPES.includes(node.type)

            if (!is_directive)
            {
                return
            }

            transformDirective(node as DirectiveNode, options.glossary, nextTermId)
        })
    }
}


function transformDirective(node: DirectiveNode, glossary: Glossary, nextTermId: () => string)
{
    const is_container = node.type === "containerDirective"

    if (is_container && CALLOUT_KINDS.includes(node.name))
    {
        setCalloutElement(node as ContainerDirective)
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

        setTermElement(node, term_key, definition, nextTermId())
        return
    }

    throw directiveError(node, `unknown directive "${ node.name }"`)
}



function setCalloutElement(node: ContainerDirective)
{
    const first_child = node.children[0]
    const has_custom_title = first_child?.type === "paragraph" && first_child.data?.directiveLabel === true

    let title = CALLOUT_TITLES[node.name]

    if (has_custom_title)
    {
        title = toString(first_child)
        node.children.shift()
    }

    node.children.unshift({
        type:     "paragraph",
        data:     { hName: "callout-title" },
        children: [{ type: "text", value: title }],
    })

    setElement(node, "callout-box", { kind: node.name, role: "note" })
}



function setTermElement(node: DirectiveNode, term_key: string, definition: string, term_id: string)
{
    const label = toString(node)

    node.data = {
        ...node.data,
        hName:       "help-term",
        hProperties: { term: term_key },
        hChildren:   [
            {
                type:       "element",
                tagName:    "button",
                properties: {
                    type:          "button",
                    className:     ["term-trigger"],
                    popovertarget: term_id,
                    style:         `anchor-name: --${ term_id }`,
                },
                children:   [{ type: "text", value: label }],
            },
            {
                type:       "element",
                tagName:    "span",
                properties: {
                    id:        term_id,
                    popover:   "",
                    className: ["term-popover"],
                    style:     `position-anchor: --${ term_id }`,
                },
                children:   [{ type: "text", value: definition }],
            },
        ],
    }
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