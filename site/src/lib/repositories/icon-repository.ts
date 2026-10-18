import type { Component } from "svelte";
import      Calendar      from "#lib/components/icons/calendar.svelte";
import      FileImage     from "#lib/components/icons/file-image.svelte";
import      FileType      from "#lib/components/icons/file-type.svelte";
import      Home          from "#lib/components/icons/home.svelte";
import      Mail          from "#lib/components/icons/mail.svelte";
import      ServerStack   from "#lib/components/icons/server-stack.svelte";
import      Signpost      from "#lib/components/icons/signpost.svelte";
import      Smartphone    from "#lib/components/icons/smartphone.svelte";
import      Tags          from "#lib/components/icons/tags.svelte";



export const ICON_REPOSITORY: Record<string, Component> =
{
    file_type: FileType,
    calendar: Calendar,
    file_image: FileImage,
    tags: Tags,
    home: Home,
    smartphone: Smartphone,
    server_stack: ServerStack,
    signpost: Signpost,
    mail: Mail,
}