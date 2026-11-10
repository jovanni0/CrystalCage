


export abstract class Optional<type_T>
{
    public abstract readonly kind: "some" | "none"


    public static some<type_T>(value: type_T): Optional<type_T>
    {
        return new Some(value)
    }


    public static none<type_T>(): Optional<type_T>
    {
        return new None<type_T>()
    }


    public static from<type_T>(value: type_T | null | undefined): Optional<type_T>
    {
        if (value === null || value === undefined)
        {
            return new None<type_T>()
        }

        return new Some(value)
    }

    
    public abstract getOrElse(fallback: type_T): type_T
    public abstract getOrNull(): type_T | null
    public abstract equals(other: Optional<type_T>): boolean
}



export class Some<type_T> extends Optional<type_T>
{
    public readonly kind          = "some"
    public readonly value: type_T


    constructor(value: type_T)
    {
        super()

        this.value = value
    }


    public getOrElse(_: type_T): type_T 
    {
        return this.value
    }

    public getOrNull(): type_T
    {
        return this.value
    }

    public equals(other: Optional<type_T>): boolean 
    {
        return other instanceof Some && other.value === this.value
    }
}


export class None<type_T> extends Optional<type_T>
{
    public readonly kind = "none"


    public getOrElse(fallback: type_T): type_T 
    {
        return fallback
    }


    public getOrNull(): null 
    {
        return null
    }


    public equals(other: Optional<type_T>): boolean 
    {
        return other instanceof None
    }
}