import type { BookCreateRequest } from "@root/dtos/BookCreateRequest";
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse";



export class BookRules
{
    public static toCreateRequest(
        author_id:     string,
        universe_id:   string | null,
        series_id:     string | null,
        volume_number: number | null,
        title:         string,
        launch_date:   string,
        word_count:    number | null,
        blurb:         string | null,
        thoughts:      string | null,
        tag_ids:       string[],
    ): BookCreateRequest
    {
        return {
            author_id:     author_id,
            universe_id:   universe_id ?? undefined,
            series_id:     series_id ?? undefined,
            volume_number: volume_number ?? undefined,
            title:         title,
            launch_date:   launch_date?.slice(0, 10),
            word_count:    word_count ?? undefined,
            blurb:         blurb ?? undefined,
            notes:         thoughts ?? undefined,
            tag_ids:       tag_ids,
            covers:        [],
            editions:      [],
        }
    }
}