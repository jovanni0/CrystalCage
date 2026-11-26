import { Maths } from "../utils/maths"



export type Point =
{
    x: number
    y: number
}


export type Size =
{
    width:  number
    height: number
}


/*
* Applied as `translate(x, y) scale(scale)` with the origin at the container centre; `x`/`y` are in px from that centre.
*/
export type ZoomTransform =
{
    scale: number
    x:     number
    y:     number
}



export const MIN_ZOOM_SCALE        = 1
export const MAX_ZOOM_SCALE        = 4
export const DOUBLE_TAP_ZOOM_SCALE = 2.5



export class ZoomRules
{
    public static readonly IDENTITY: ZoomTransform = { scale: 1, x: 0, y: 0 }


    /*
    * Size of an image fitted inside a container without cropping, like `object-fit: contain`.
    */
    public static fittedSize(natural: Size, container: Size): Size
    {
        if (natural.width === 0 || natural.height === 0)
        {
            return { 
                width: 0, 
                height: 0 
            }
        }

        const ratio = Math.min(container.width / natural.width, container.height / natural.height)

        return { 
            width: natural.width * ratio, 
            height: natural.height * ratio 
        }
    }


    public static clampScale(scale: number): number
    {
        return Maths.clamp(scale, MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
    }


    /*
    * On an axis where the zoomed image overflows, its edges cannot move inside the container; on an axis where it fits, it stays centred.
    */
    public static clampOffset(transform: ZoomTransform, fitted: Size, container: Size): ZoomTransform
    {
        const max_x = Math.max(0, (fitted.width * transform.scale - container.width) / 2)
        const max_y = Math.max(0, (fitted.height * transform.scale - container.height) / 2)

        return {
            scale: transform.scale,
            x:     Math.min(max_x, Math.max(-max_x, transform.x)),
            y:     Math.min(max_y, Math.max(-max_y, transform.y)),
        }
    }


    /*
    * Changes the scale while keeping the image point under `focus` (relative to the container centre) in place.
    */
    public static zoomAround(transform: ZoomTransform, next_scale: number, focus: Point): ZoomTransform
    {
        const clamped_scale = ZoomRules.clampScale(next_scale)
        const ratio         = clamped_scale / transform.scale

        return {
            scale: clamped_scale,
            x:     focus.x - (focus.x - transform.x) * ratio,
            y:     focus.y - (focus.y - transform.y) * ratio,
        }
    }


    public static distance(first: Point, second: Point): number
    {
        return Math.hypot(first.x - second.x, first.y - second.y)
    }


    public static midpoint(first: Point, second: Point): Point
    {
        return { x: (first.x + second.x) / 2, y: (first.y + second.y) / 2 }
    }
}