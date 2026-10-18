<script lang="ts">
    import FeaturesSection from './features-section.svelte';
    import GallerySection from './gallery-section.svelte';
    import HeroSection from './hero-section.svelte';
    import StartSection from './start-section.svelte';
    import VideoSection from './video-section.svelte';
    import WhySection from './why-section.svelte';



    let { data } = $props()
</script>



<HeroSection     content = { data.home.hero } />
<FeaturesSection content = { data.home.features } />
<GallerySection  content = { data.home.gallery } />
<VideoSection    content = { data.home.video } />
<WhySection      content = { { ...data.home.why, body: data.why_html } } />
<StartSection    content = { data.home.start } />