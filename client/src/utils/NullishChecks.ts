


export function isNullOrUndefined<TypeT>(value: TypeT | null | undefined): value is null | undefined
{
    return value === null || value === undefined
}


export function isNull<TypeT>(value: TypeT | null): value is null
{
    return value === null
}


export function isNotNull<TypeT>(value: TypeT | null): value is TypeT
{
    return value !== null
}