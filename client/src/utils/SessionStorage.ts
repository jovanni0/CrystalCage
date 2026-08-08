import type {PersistedSession} from "@data-types/PersistedSession";
import { Preferences } from '@capacitor/preferences';



export class SessionStorage
{
    private readonly SESSION_KEY = "session-data"


    async load(): Promise<PersistedSession | null>
    {
        const { value } = await Preferences.get({ key: this.SESSION_KEY })

        return value !== null ? JSON.parse(value) : null
    }


    async save(session: PersistedSession): Promise<void>
    {
        await Preferences.set({
            key: this.SESSION_KEY,
            value: JSON.stringify(session)
        })
    }


    async clear(): Promise<void>
    {
        await Preferences.remove({ key: this.SESSION_KEY })
    }
}