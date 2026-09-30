


export type FormatColorVariant = {
    id:    string
    label: string
    color: string
}



export class FormatColorRegistry
{
    private static readonly formats: FormatColorVariant[] = [
        { id: 'epub',     label: 'EPUB',     color: 'hsl(175, 38%, 36%)' },
        { id: 'mobi',     label: 'MOBI',     color: 'hsl(215, 35%, 42%)' },
        { id: 'pdf',      label: 'PDF',      color: 'hsl(275, 32%, 42%)' },
        { id: 'tome',     label: 'TOME',     color: 'hsl(42, 48%, 40%)' },
        { id: 'physical', label: 'Physical', color: 'hsl(12, 40%, 40%)' },
        { id: 'other',    label: 'Other',    color: 'hsl(30, 22%, 40%)' },
        { id: 'unknown',  label: 'Unknown',  color: 'hsl(34, 5%, 40%)' }
    ]


    public static getVariant(format: string): FormatColorVariant
    {
        const format_normalized = format.toLowerCase()

        return this.formats.find(it => it.id === format) 
            || { id: 'unknown',  label: 'Unknown',  color: 'hsl(34, 5%, 40%)' }
    }
}