import type { ContentRoadmap } from "#lib/content/content-roadmap.ts"
import      { readDataFile } from "#lib/content/content.ts"




export async function load()
{
    const roadmap = readDataFile<ContentRoadmap>("roadmap")

    return { roadmap }
}