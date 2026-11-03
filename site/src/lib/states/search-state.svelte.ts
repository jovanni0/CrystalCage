// opened from the header (search button); the search dialog reads it once it exists
class SearchState
{
    public is_open: boolean = $state(false)


    public open()
    {
        this.is_open = true
    }

    public close()
    {
        this.is_open = false
    }
}



export const search_state = new SearchState()
