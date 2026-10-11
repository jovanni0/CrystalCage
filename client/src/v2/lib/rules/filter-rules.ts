import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse";
import type { ReadStatus } from "@root/types/ReadStatus";



export type FilterCondition = 
    | { field: "read_status", operator: "is" | "is_not",                    value: ReadStatus[] }
    | { field: "author",      operator: "is" | "is_not",                    value: string[] }
    | { field: "series",      operator: "is" | "is_not" | "is_not_set",     value: string[] }
    | { field: "volume",      operator: "is" | "is_not_set",                value: (number | null)[] }
    | { field: "word_count",  operator: "less_then" | "more_then",          value: number }
    | { field: "has_cover",   operator: "is",                               value: boolean }
    | { field: "reread",      operator: "at_least",                         value: number }



export type FilterDefinition = {
    version:    1
    match:      "all" | "any"
    conditions: FilterCondition[]
}


export type SavedFilter = {
    id:         string
    name:       string
    definition: FilterDefinition
}



export type BookFacts = {
    book:       BookSummaryResponse
    word_count: number | null
}


export type FilterPredicate = (facts: BookFacts) => boolean


type FieldCompilers = {
    [Field in FilterCondition["field"]]: (condition: Extract<FilterCondition, { field: Field }>) => FilterPredicate
}


const FIELD_COMPILERS: FieldCompilers = {
    read_status: condition =>
    {
        const statuses = new Set(condition.value)
        const is_match = (facts: BookFacts) => statuses.has(facts.book.read_status)

        return condition.operator === "is" ? is_match : facts => !is_match(facts)
    },

    word_count: condition =>
    {
        return facts =>
        {
            if (facts.word_count === null) return false

            return condition.operator === "less_then"
                ? facts.word_count < condition.value
                : facts.word_count > condition.value
        }
    },

    author: condition =>
    {
        const wanted = condition.value

        switch (condition.operator)
        {
            case "is":     return facts => wanted.includes(facts.book.author_id)
            case "is_not": return facts => !wanted.includes(facts.book.author_id)
        }
    },

    series: condition =>
    {
        const wanted = condition.value

        switch (condition.operator)
        {
            case "is":         return facts => facts.book.series_id ? wanted.includes(facts.book.series_id) : false
            case "is_not":     return facts => facts.book.series_id ? !wanted.includes(facts.book.series_id): true
            case "is_not_set": return facts => facts.book.series_id === null
        }
    },

    volume: condition => 
    {
        const wanted = condition.value

        switch(condition.operator)
        {
            case "is":         return facts => wanted.includes(facts.book.volume_number)
            case "is_not_set": return facts => facts.book.volume_number === null
        }
    },

    has_cover: condition =>
    {
        return facts => facts.book.cover_id !== null
    },

    reread: condition =>
    {
        return facts => facts.book.reread_count > condition.value
    }
}



export const BUILT_IN_FILTERS: SavedFilter[] = [
    {
        id: "all",
        name: "All",
        definition: {
            version: 1,
            match: "all",
            conditions: []
        }
    },
    { 
        id: "reading",
        name: "Reading",
        definition: { 
            version: 1, 
            match: "all", 
            conditions: [
                { field: "read_status", operator: "is", value: ["IN_PROGRESS"] }
            ] 
        } 
    },
    { 
        id: "tbr",
        name: "TBRs",
        definition: { 
            version: 1, 
            match: "all", 
            conditions: [
                { field: "read_status", operator: "is", value: ["UNREAD"] }, 
                { field: "volume",      operator: "is", value: [1, null] }
            ] 
        } 
    },
    { 
        id: "unread",
        name: "Unread",
        definition: { 
            version: 1, 
            match: "all", 
            conditions: [
                { field: "read_status", operator: "is", value: ["UNREAD"] }, 
            ] 
        } 
    },
    { 
        id: "dnf",
        name: "DNFs",
        definition: { 
            version: 1, 
            match: "all", 
            conditions: [
                { field: "read_status", operator: "is", value: ["DNF"] }
            ] 
        } 
    },
    { 
        id: "standalones", 
        name: "Standalones", 
        definition: { 
            version: 1, 
            match: "all", 
            conditions: [
                { field: "series", operator: "is_not_set", value: [] }
            ] 
        } 
    },
]



export class FilterRules
{
    public static compile(definition: FilterDefinition): FilterPredicate
    {
        const predicates = definition.conditions.map(it => FilterRules.compileCondition(it))

        if (predicates.length === 0) return () => true

        switch (definition.match)
        {
            case "all": return facts => predicates.every(it => it(facts))
            case "any": return facts => predicates.some(it => it(facts))
        }
    }


    public static compileCondition(condition: FilterCondition): FilterPredicate
    {
        const compiler = FIELD_COMPILERS[condition.field] as (condition: FilterCondition) => FilterPredicate

        return compiler(condition)
    }
}