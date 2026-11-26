import type { EditionFileFormatDefinition } from "../utils/format-color-registry.svelte";



export class EditionData
{
    public readonly id:     string
    public readonly label:  string
    public readonly format: EditionFileFormatDefinition


    constructor(id: string, label: string, format: EditionFileFormatDefinition)
    {
        this.id     = id
        this.label  = label
        this.format = format
    }
}