import type { ReadDraft } from "../types/read-draft";
import { Optional } from "../_other/optional";
import type { ReadRecordCreateRequest } from "@root/dtos/ReadRecordCreateRequest";
import { ReadTarget } from "../data-models/read-target";
import type { ReadRecordUpdateRequest } from "@root/dtos/ReadRecordUpdateRequest";
import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse";



export class ReadDraftRules
{
    public static empty(target: ReadTarget): ReadDraft
    {
        return {
            target:        target,
            start_date:    "",
            end_date:      "",
            edition_id:    null,
            notes:         "",
            is_historical: false,
            is_abandoned:  false,
        }
    }


    public static fromReadRecordResponse(record: ReadRecordResponse): ReadDraft
    {
        return {
            target:        new ReadTarget.Existing(record.id),
            start_date:    record.start_date ?? "",
            end_date:      record.end_date ?? "",
            edition_id:    record.edition_id,
            notes:         record.notes ?? "",
            is_historical: record.is_historical,
            is_abandoned:  record.did_not_finish,
        }
    }

    public static toReadRecordCreateRequest(draft: ReadDraft, book_id: string): Optional<ReadRecordCreateRequest>
    {
        if (draft.target instanceof ReadTarget.Existing) return Optional.none()

        return Optional.from({
            book_id:    book_id,
            edition_id: draft.edition_id,
            start_date: draft.start_date !== "" ? draft.start_date : null,
            end_date:   draft.end_date !== "" ? draft.end_date : null,
            did_not_finish: draft.is_abandoned,
            is_historical: draft.is_historical,
            stop_position: null,
            notes: draft.notes
        })
    }


    public static toReadRecordUpdateRequest(draft: ReadDraft): Optional<ReadRecordUpdateRequest>
    {
        if (draft.target instanceof ReadTarget.New)
        {
            return Optional.none()
        }

        return Optional.from({
            edition_id:     draft.edition_id,
            start_date:     draft.start_date !== "" ? draft.start_date : null,
            end_date:       draft.end_date !== "" ? draft.end_date : null,
            did_not_finish: draft.is_abandoned,
            is_historical:  draft.is_historical,
            stop_position:  undefined,
            notes:          draft.notes,
        })
    }
}