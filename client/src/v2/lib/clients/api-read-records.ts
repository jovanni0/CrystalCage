import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse"
import type { SessionHttp } from "./session-http"
import type { ReadRecordCreateRequest } from "@root/dtos/ReadRecordCreateRequest"
import type { ReadRecordUpdateRequest } from "@root/dtos/ReadRecordUpdateRequest"



export class ApiReadRecords
{
    constructor(private session: SessionHttp)
    {}


    public getAllByBookId(book_id: string): Promise<ReadRecordResponse[]>
    {
        return this.session.send(`/read-records?book_id=${encodeURIComponent(book_id)}`)
    }


    public getById(id: string): Promise<ReadRecordResponse>
    {
        return this.session.send(`/read-records/${encodeURIComponent(id)}`)
    }


    public create(request: ReadRecordCreateRequest): Promise<ReadRecordResponse>
    {
        return this.session.send<ReadRecordResponse>(`/read-records`, {
            method: "POST",
            body: request,
        })
    }


    public update(id: string, request: ReadRecordUpdateRequest): Promise<ReadRecordResponse>
    {
        return this.session.send<ReadRecordResponse>(`/read-records/${encodeURIComponent(id)}`, {
            method: "PATCH",
            body: request,
        })
    }


    public delete(id: string): Promise<void>
    {
        return this.session.send<void>(`/read-records/${encodeURIComponent(id)}`)
    }
}