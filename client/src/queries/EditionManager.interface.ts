import type { DisplayEdition } from "@root/types/DisplayEdition"



export interface EditionManager
{
    editions: DisplayEdition[]
    addEdition(file: File): void | Promise<void>
    removeEdition(id: string): void | Promise<void>
    setEditionLabel(id: string, label: string): void | Promise<void>
}