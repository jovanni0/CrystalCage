import { logger, session_manager }  from "@root/CompositionRoot";
import { browser }                  from "$app/environment";
import { goto }                     from "$app/navigation";



const LOGIN_ROUTES = ["/login/select-server", "/login/select-account"]

export const prerender = true;
export const ssr = false;


// NOTE: the previous code was triggering all the `load` functions to run when navifating from a page to a child page,
// so the objects constructred on other `load` functions did not persist. this fixes it, but might create bugs with session management.
export const load = async () =>
{
    if (browser)
    {
        await session_manager.restoreSession()
    }
}

// export const load = async ({ url, route }) =>
// {
//     if (browser)
//     {
//         await session_manager.restoreSession()
        
//         const status = session_manager.status
//         const path = url.pathname
    
//         if (status === "UNKNOWN")
//         {
//             logger.info("session manager caught with status UNKNOWN")
//             return
//         }
    
//         const target_path =
//             status === "NO_SERVER"   ? "/login/select-server" :
//             status === "NEEDS_LOGIN" ? "/login/select-account" :
//                                         "/library"

//         const is_path_correct = status === "AUTHENTICATED" ? !LOGIN_ROUTES.includes(path) : path === target_path
    
//         if (!is_path_correct)
//         {
//             logger.info(`redirecting due to session status ${status} from ${path} to ${target_path}`)
//             goto(target_path, { replaceState: true })
//         }
//     }

// }