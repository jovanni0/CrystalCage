import type { CapacitorConfig } from '@capacitor/cli';



const config: CapacitorConfig = {
    appId:   "dev.jovanni0.sanctum",
    appName: "Sanctum",
    webDir:  "build",
    plugins: 
    {
        CapacitorHttp: { enabled: true },
        SplashScreen: { launchAutoHide: false }, // the splash scrren is done native via android/app/src/main/res/values/styles.xml
    },
}


export default config