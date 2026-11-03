import { buildSearchIndex } from "#lib/content/search-index.ts"



export const prerender = true



export async function GET()
{
    return Response.json(await buildSearchIndex())
}
