package net.metacraft.mod.client;

import net.metacraft.mod.ExampleClientMod;
import net.metacraft.mod.PaintingModInitializer;
import net.metacraft.mod.api.API;
import net.metacraft.mod.api.IBrowser;
import net.metacraft.mod.api.ModScheme;
import net.minecraft.client.MinecraftClient;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserOsr;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientProxy extends BaseProxy{
    private final ArrayList<CefBrowserOsr> browsers = new ArrayList<>();

    public static ClientProxy instance;

    public static ClientProxy getInstance() {
        if (instance == null) {
            instance = new ClientProxy();
        }
        return instance;
    }

    private ExampleClientMod exampleMod;
    public static String ROOT = ".";
    public static CefApp cefApp;
    private static final String CEF_VERSION = "95";
    private CefClient cefClient = null;

    public CefApp getCefApp() {
        return cefApp;
    }

    public DisplayHandler getDisplayHandler() {
        return displayHandler;
    }

    private final DisplayHandler displayHandler = new DisplayHandler();
    private final HashMap<String, String> mimeTypeMap = new HashMap<>();
    private final AppHandler appHandler = new AppHandler(new String[]{"--disable-gpu"});

    @Override
    public void onPreInit() {
        exampleMod = new ExampleClientMod();
        exampleMod.onPreInit(); //Do it even if example mod is disabled because it registers the "mod://" scheme
    }





    @Override
    public void onInit() {
        String ROOT = MinecraftClient.getInstance().runDirectory.getAbsolutePath().replaceAll("\\\\", "/");
        if(ROOT.endsWith(".")) {
            ROOT = ROOT.substring(0, ROOT.length() - 1);
            ROOT += CEF_VERSION + "/";
        }


        if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {

            CefSettings settings = new CefSettings();
            settings.windowless_rendering_enabled = true;
            settings.background_color = settings.new ColorType(0, 255, 255, 255);
            settings.locales_dir_path = (new File(ROOT, "MCEFLocales")).getAbsolutePath();
            settings.cache_path = (new File(ROOT, "MCEFCache")).getAbsolutePath();
            File subproc = new File(ROOT, "jcef_helper.exe");
            settings.browser_subprocess_path = subproc.getAbsolutePath();
            settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;

            ArrayList<String> libs = new ArrayList<>();
            libs.add("d3dcompiler_47.dll");
            libs.add("libGLESv2.dll");
            libs.add("libEGL.dll");
            libs.add("chrome_elf.dll");
            libs.add("libcef.dll");
            libs.add("jcef.dll");

            for(String lib: libs) {
                File f = new File(ROOT, lib);
                try {
                    f = f.getCanonicalFile();
                } catch(IOException ex) {
                    f = f.getAbsoluteFile();
                }

                System.load(f.getPath());
            }
            CefApp.startup();
            cefApp = CefApp.getInstance(settings);

            CefApp.CefVersion version = cefApp.getVersion();
            System.out.println("Using:\n" + version);

        }
        loadMimeTypeMapping();
        AppHandler appHandler = new AppHandler(new String[]{"--disable-gpu", "crash-test"});
        appHandler.registerScheme("mod", ModScheme.class, true, false, false, true, true, false, false);
        CefApp.addAppHandler(appHandler);
        cefClient = cefApp.createClient();
        CefMessageRouter cefRouter = CefMessageRouter.create(new CefMessageRouter.CefMessageRouterConfig("mcefQuery", "mcefCancel"));
        cefClient.addMessageRouter(cefRouter);

        cefClient.addDisplayHandler(displayHandler);
        cefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean doClose(CefBrowser browser) {
                browser.close(true);
                return false;
            }
        });
        exampleMod.onPreInit();

    }

    public void loadMimeTypeMapping() {
        Pattern p = Pattern.compile("^(\\S+)\\s+(\\S+)\\s*(\\S*)\\s*(\\S*)$");
        String line = "";
        int cLine = 0;
        mimeTypeMap.clear();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(PaintingModInitializer.class.getResourceAsStream("/assets/mcef/mime.types")));

            while(true) {
                cLine++;
                line = br.readLine();
                if(line == null)
                    break;

                line = line.trim();
                if(!line.startsWith("#")) {
                    Matcher m = p.matcher(line);
                    if(!m.matches())
                        continue;

                    mimeTypeMap.put(m.group(2), m.group(1));
                    if(m.groupCount() >= 4 && !m.group(3).isEmpty()) {
                        mimeTypeMap.put(m.group(3), m.group(1));

                        if(m.groupCount() >= 5 && !m.group(4).isEmpty())
                            mimeTypeMap.put(m.group(4), m.group(1));
                    }
                }
            }

            close(br);
        } catch(Throwable e) {
            System.out.println("[Mime Types] Error while parsing \"%s\" at line %d:" + line + cLine);
            e.printStackTrace();
        }

        System.out.println ("Loaded %d mime types " + mimeTypeMap.size());
    }

    /**
     * Calls "close" on the specified object without throwing any exceptions.
     * This is usefull with input and output streams.
     *
     * @param o The object to call close on.
     */
    public static void close(Object o) {
        try {
            o.getClass().getMethod("close").invoke(o);
        } catch(Throwable t) {}
    }

    @Override
    public IBrowser createBrowser(String url, boolean transp) {
        CefBrowserOsr ret = (CefBrowserOsr) cefClient.createBrowser(url, true, transp);
        ret.setCloseAllowed();
        ret.createImmediately();

        browsers.add(ret);
        return ret;
    }

    @Override
    public IBrowser createBrowser(String url) {
        return createBrowser(url, false);
    }
}
