import { Preferences } from "@capacitor/preferences"



/*
* Reader page counts depend on the device and font size, so they stay on the device, keyed by read record id.
*/
export class ReadPageCountStorage
{
    private readonly KEY_PREFIX = "read-page-count:"


    async load(read_record_id: string): Promise<number | null>
    {
        const { value } = await Preferences.get({ key: this.keyOf(read_record_id) })

        if (value === null)
        {
            return null
        }

        const page_count = Number(value)

        if (!Number.isInteger(page_count) || page_count <= 0)
        {
            return null
        }

        return page_count
    }


    async save(read_record_id: string, page_count: number | null): Promise<void>
    {
        if (page_count === null)
        {
            await Preferences.remove({ key: this.keyOf(read_record_id) })
            return
        }

        await Preferences.set({ key: this.keyOf(read_record_id), value: String(page_count) })
    }


    private keyOf(read_record_id: string): string
    {
        return `${this.KEY_PREFIX}${read_record_id}`
    }
}