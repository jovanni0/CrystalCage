export type Tab = { 
    id:    string
    label: string
    href:  string 
}



export class TabRules
{
    public static isPathInsideTab(tab_href: string, pathname: string)
    {
        const tab_path = TabRules.withoutTrailingSlash(tab_href)
        const current_path = TabRules.withoutTrailingSlash(pathname)

        return current_path === tab_path || current_path.startsWith(tab_path + '/')
    }


    public static activeHref(tabs: Tab[], pathname: string): string | null
    {
        const matching_tabs = tabs.filter(it => TabRules.isPathInsideTab(it.href, pathname))

        if (matching_tabs.length === 0) return null

        const most_specific_tab = matching_tabs.reduce( (best_tab, tab) =>
        {
            return tab.href.length > best_tab.href.length ? tab : best_tab
        })

        return most_specific_tab.href
    }


    public static activeId(tabs: Tab[], pathname: string): string | null
    {
        const href = TabRules.activeHref(tabs, pathname)

        return tabs.find(it => it.href === href)?.id ?? null
    }


    private static withoutTrailingSlash(path: string)
    {
        return path.replace(/\/$/, '')
    }
}