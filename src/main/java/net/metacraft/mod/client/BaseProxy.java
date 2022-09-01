package net.metacraft.mod.client;

import net.metacraft.mod.api.*;

public class BaseProxy implements API {
    public void onPreInit() {
    }

    public void onInit() {
        System.out.println("MCEF is running on server. Nothing to do.");
    }

    @Override
    public IBrowser createBrowser(String url, boolean transp) {
        return null;
    }

    @Override
    public IBrowser createBrowser(String url) {
        return null;
    }

    @Override
    public void registerDisplayHandler(IDisplayHandler idh) {

    }

    @Override
    public void registerJSQueryHandler(IJSQueryHandler iqh) {

    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public void openExampleBrowser(String url) {

    }

    @Override
    public String mimeTypeFromExtension(String ext) {
        return null;
    }

    @Override
    public void registerScheme(String name, Class<? extends IScheme> schemeClass, boolean std, boolean local, boolean displayIsolated, boolean secure, boolean corsEnabled, boolean cspBypassing, boolean fetchEnabled) {

    }

    @Override
    public boolean isSchemeRegistered(String name) {
        return false;
    }

    @Override
    public String punycode(String url) {
        return null;
    }
}
