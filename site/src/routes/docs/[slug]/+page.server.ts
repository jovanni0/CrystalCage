import { error }   from "@sveltejs/kit"
import { docSlugs, 
         loadDoc } from "#lib/content/content.ts"



export function entries()
{
    return docSlugs().map((slug) => ({ slug }))
}



export async function load({ params })
{
    const doc = await loadDoc(params.slug)

    if (doc === null)
    {
        error(404, "Page not found")
    }

    return { doc }
}