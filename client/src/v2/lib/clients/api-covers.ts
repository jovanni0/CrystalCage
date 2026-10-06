import type { SessionHttp } from "./session-http";



export class ApiCovers
{
    constructor(private session: SessionHttp)
    {}


    public getImage(cover_id: string): Promise<Blob>
    {
        return this.session.send<Blob>(`/covers/${encodeURIComponent(cover_id)}`)
    }
}