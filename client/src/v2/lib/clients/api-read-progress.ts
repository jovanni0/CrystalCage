import type { ReadProgressCreateRequest, ReadProgressResponse } from "../dtos/read-progress.dto";
import type { SessionHttp } from "./session-http";



export class ApiReadProgress
{
    constructor(private session: SessionHttp)
    {}


    public getAllByReadId(read_id: string): Promise<ReadProgressResponse[]>
    {
        return this.session.send(`/read-progress/${encodeURIComponent(read_id)}`)
    }


    public create(request: ReadProgressCreateRequest): Promise<ReadProgressResponse>
    {
        return this.session.send<ReadProgressResponse>(`/read-progress`, {
            method: "POST",
            body: request,
        })
    }
}