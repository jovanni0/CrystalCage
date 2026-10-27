
export type StagedStatus = "done" | "in progress"



export type ContentRoadmap_Staged = {
    title:            string
    status:           StagedStatus
    text:             string
    completion_date?: string
}



export type ContentRoadmap_Ideas = {
    title: string
    items: string[]
}



export type ContentRoadmap = {
    title:   string
    caption: string
    staged:  ContentRoadmap_Staged[]
    ideas:   ContentRoadmap_Ideas
}