import type { SessionHttp } from "./session-http";



export type CoverType = "original" | "thumbnail"



export class ApiCovers
{
    constructor(private session: SessionHttp)
    {}


    public getImage(cover_id: string, cover_type: CoverType): Promise<Blob>
    {
        return this.session.send<Blob>(`/covers/${encodeURIComponent(cover_id)}?size=${cover_type}`)
    }
}