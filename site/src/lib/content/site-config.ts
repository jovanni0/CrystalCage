export type NavLink =
{
    label: string
    href:  string
    icon:  string
}

export type SocialLink =
{
    name: string
    href: string
    icon: string
}

export type SiteConfig =
{
    name: string
    tagline: string
    description: string
    repository: string
    nav: NavLink[]
    social: SocialLink[]
    footer: { note: string }
}