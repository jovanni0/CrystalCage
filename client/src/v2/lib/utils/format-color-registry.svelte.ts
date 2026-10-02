


export type EditionFileFormatDefinition = {
    id:      string
    label:   string
    hue:     number
    chroma?: number
}



const BUILTIN_EDITON_FORMATS: EditionFileFormatDefinition[] = [
    { id: "epub",     label: "EPUB",     hue: 185 },
    { id: "mobi",     label: "MOBI",     hue: 255 },
    { id: "pdf",      label: "PDF",      hue: 310 },
    { id: "tome",     label: "TOME",     hue: 85  },
    { id: "physical", label: "Physical", hue: 35  },
    { id: "other",    label: "Other",    hue: 65, chroma: 0.04 },
]



export class EditionFormatColorRegistry
{
    private overrides: Record<string, Partial<EditionFileFormatDefinition>> = $state({})
    private custom:    EditionFileFormatDefinition[]                        = $state([])

    public formats: EditionFileFormatDefinition[] = $derived([ ...BUILTIN_EDITON_FORMATS.map(it => ({ ...it, ...this.overrides[it.id]}) ), ...this.custom ])


    public resolve(id: string): EditionFileFormatDefinition
    {
        const key = id.toLowerCase()
        const result = this.formats.find(it => it.id === key) ?? { id: key, label: id.toUpperCase(), hue: hashHue(key), chroma: 0.02 }

        return result
    }

    // public update(id: string, patch: Partial<EditionFileFormatDefinition>) { /* override or edit custom, then persist */ }
    // public add(def: FormatDefinition) { /* push to custom, then persist */ }
    // public resetToDefault(id: string) { delete this.overrides[id] /* persist */ }
}



function hashHue(str: string): number
{
    let hue = 0

    for (const character of str) 
    {
        hue = (hue * 31 + character.charCodeAt(0)) % 360
    }

    return hue
}