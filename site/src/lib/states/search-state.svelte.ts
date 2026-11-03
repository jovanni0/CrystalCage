// opened from the header search button or a shortcut; read by elements/search-dialog.svelte
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
