import type {UserProfileResponse} from "./UserProfileResponse.ts";



export type AuthLoginResponse = {
    token:  string
    user:   UserProfileResponse
}