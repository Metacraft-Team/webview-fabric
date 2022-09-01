package net.metacraft.mod.api;

import net.metacraft.mod.client.ClientProxy;

public class CEFApi {
    
    /**
     * Call this to get the API instance.
     * @return the CEF API or null if something failed.
     */
    public static API getAPI() {
        return ClientProxy.getInstance();
    }
    
    /**
     * Checks if MCEF was loaded by forge.
     * @return true if it is loaded. false otherwise.
     */
    public static boolean isMCEFLoaded() {
        return true;
    }

}
