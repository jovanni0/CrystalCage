


export namespace Optional
{
    export function some<type_T>(value: type_T): Optional<type_T>
    {
        return new Some(value)
    }


    export function none<type_T>(): Optional<type_T>
    {
        return new None<type_T>()
    }


    export function from<type_T>(value: type_T | null | undefined): Optional<type_T>
    {
        if (value === null || value === undefined)
        {
            return new None<type_T>()
        }

        return new Some(value)
    }
    


    abstract class OptionalBase<type_T>
    {
        public abstract readonly kind: "some" | "none"

        public abstract getOrElse(fallback: type_T): type_T
        public abstract getOrNull(): type_T | null
        public abstract equals(other: Optional<type_T>): boolean
        public abstract isSome(): this is Some<type_T>
        public abstract isNone(): this is None<type_T>
        public abstract flatMap<type_U>(transform: (value: type_T) => Optional<type_U>): Optional<type_U>
    }
    
    
    
    export class Some<type_T> extends OptionalBase<type_T>
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
    
    
        public isSome(): this is Some<type_T>
        {
            return true
        }
    
    
        public isNone(): this is None<type_T>
        {
            return false
        }


        public flatMap<type_U>(transform: (value: type_T) => Optional<type_U>): Optional<type_U> 
        {
            return transform(this.value)
        }
    }
    
    
    export class None<type_T> extends OptionalBase<type_T>
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
    
    
        public isSome(): this is Some<type_T>
        {
            return false
        }
    
    
        public isNone(): this is None<type_T>
        {
            return true
        }


        public flatMap<type_U>(transform: (value: type_T) => Optional<type_U>): Optional<type_U> 
        {
            return new None<type_U>()
        }
    }
}


export type Optional<type_T> = Optional.Some<type_T> | Optional.None<type_T>