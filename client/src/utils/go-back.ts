import { goto } from "$app/navigation";



export function goBack()
{
    if (history.length > 1)
    {
        history.back()
    }
    else 
    {
        goto('/')
    }
}   