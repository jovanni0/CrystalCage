import type { Logger }      from "@root/loggers/Logger"
import type { ApiCovers }   from "../clients/api-covers"
import type { AsyncResult } from "../utils/result"
import      { attempt }     from "../utils/result"



/*
*   The max memory footprint the cache can have. If it goes over this limit, images start to be discarded.
*/
const DEFAULT_MAX_BYTES = 48 * 1024 * 1024



export class CoverImageCache
{
    private blobs      = new Map<string, Blob>()
    private in_flight  = new Map<string, AsyncResult<Blob>>()
    private used_bytes = 0
    private generation = 0


    constructor(
        private source:    ApiCovers,
        private logger:    Logger,
        private max_bytes: number     = DEFAULT_MAX_BYTES,
    ) {}


    public peek(cover_id: string): Blob | null
    {
        const blob = this.blobs.get(cover_id)

        if (blob === undefined) return null

        this.touch(cover_id, blob)

        return blob
    }



    public load(cover_id: string): AsyncResult<Blob>
    {
        const cached = this.peek(cover_id)

        if (cached)
        {
            return Promise.resolve({
                ok: true,
                value: cached,
            })
        }

        const pending = this.in_flight.get(cover_id)

        if (pending !== undefined)
        {
            return pending
        }

        const request = this.fetchAndStore(cover_id)
        this.in_flight.set(cover_id, request)

        return request
    }


    public forget(cover_id: string): void
    {
        const blob = this.blobs.get(cover_id)

        if (!blob) return

        this.blobs.delete(cover_id)
        this.used_bytes -= blob.size
    }


    public reset(): void
    {
        this.generation += 1
        this.blobs.clear()
        this.in_flight.clear()
        this.used_bytes = 0
    }


    private async fetchAndStore(cover_id: string): AsyncResult<Blob>
    {
        const generation = this.generation
        const result = await attempt(this.logger, `load cover image ${cover_id}`, () => this.source.getImage(cover_id))

        if (generation !== this.generation) return result

        this.in_flight.delete(cover_id)

        if (result.ok)
        {
            this.store(cover_id, result.value)
        }

        return result
    }


    private store(cover_id: string, blob: Blob): void
    {
        if (blob.size > this.max_bytes) return

        this.forget(cover_id)
        this.blobs.set(cover_id, blob)
        this.used_bytes += blob.size

        this.evictOverflow()
    }


    private evictOverflow(): void
    {
        for (const [oldest_id] of this.blobs)
        {
            if (this.used_bytes <= this.max_bytes) return

            this.forget(oldest_id)
        }
    }


    /*
    *   Delete and re-insert the cover to make the entry be the most recent.
    */
    private touch(cover_id: string, blob: Blob): void
    {
        this.blobs.delete(cover_id)
        this.blobs.set(cover_id, blob)
    }
}