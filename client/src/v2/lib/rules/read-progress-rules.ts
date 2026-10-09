import type { ReadProgressResponse } from "../dtos/read-progress.dto"



export class ReadProgressRules
{
    public static latestOf(entries: ReadProgressResponse[]): ReadProgressResponse | null
    {
        let latest: ReadProgressResponse | null = null

        for (const entry of entries)
        {
            if (latest === null || Date.parse(entry.recorded_at) > Date.parse(latest.recorded_at))
            {
                latest = entry
            }
        }

        return latest
    }
}