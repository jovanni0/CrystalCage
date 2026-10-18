export type Action =
{
    label: string
    href: string
    style?: "primary" | "secondary"
}

export type Screenshot =
{
    src: string
    alt: string
    caption?: string
}

export type IconItem =
{
    title: string
    text: string
    icon: string
}


export type HomeContent_HeroSection = { 
    eyebrow:     string
    title:       string
    lead:        string
    actions:     Action[]
    screenshots: Screenshot[]
}

export type HomeContent_FeaturesSection = { 
    title: string
    items: IconItem[] 
}


export type HomeContent_GallerySection = { 
    title:       string
    screenshots: Screenshot[] 
}


export type HomeContent_VideoSection = { 
    title:         string
    youtube_id:    string
    thumbnail:     string
    thumbnail_alt: string
    caption:       string 
}


export type HomeContent_WhySection = { 
    title: string
    body:  string 
}


export type HomeContent_StartSection = { 
    title: string
    items: (IconItem & { action: Action })[] 
}

export type HomeContent =
{
    hero:     HomeContent_HeroSection
    features: HomeContent_FeaturesSection
    gallery:  HomeContent_GallerySection
    video:    HomeContent_VideoSection
    why:      HomeContent_WhySection
    start:    HomeContent_StartSection
}

export type GalleryContent =
{
    images: Screenshot[]
}