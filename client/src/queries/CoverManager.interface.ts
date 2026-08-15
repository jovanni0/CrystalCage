import type { DisplayCover } from "@root/types/DisplayCover"



export interface CoverManager
{
    covers: DisplayCover[]
    default_id: string | null
    addCover(file: File): void | Promise<void>
    removeCover(id: string): void | Promise<void>
    setDefaultCoverId(id: string): void | Promise<void>
    setCoverLabel(id: string, label: string): void | Promise<void>
}