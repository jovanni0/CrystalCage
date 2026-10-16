import type { SiteConfig }   from "#lib/content/site-config.ts";
import      { readDataFile } from "#lib/content/content.ts";



export function load()
{
    return { site: readDataFile<SiteConfig>("site") }
}