package net.metacraft.mod;


import net.fabricmc.api.ModInitializer;
import net.metacraft.mod.api.ModScheme;
import net.metacraft.mod.client.AppHandler;
import net.metacraft.mod.client.BaseProxy;
import net.metacraft.mod.client.ClientProxy;
import net.metacraft.mod.client.DisplayHandler;
import net.minecraft.client.MinecraftClient;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaintingModInitializer implements ModInitializer {

	public static CefClient cefClient;
	public static CefApp cefApp;
	private static final HashMap<String, String> mimeTypeMap = new HashMap<>();

	public static DisplayHandler displayHandler = new DisplayHandler();

	private static final String CEF_VERSION = "95";

	public static BaseProxy PROXY = (BaseProxy) ClientProxy.getInstance();

	@Override
	public void onInitialize() {
		PROXY.onPreInit();
		PROXY.onInit();
	}

	private static CefClient initBrowser() {
		String ROOT = MinecraftClient.getInstance().runDirectory.getAbsolutePath().replaceAll("\\\\", "/");
		if(ROOT.endsWith(".")) {
			ROOT = ROOT.substring(0, ROOT.length() - 1);
			ROOT += CEF_VERSION + "/";
		}


		CefClient cefClient = null;
		if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {

			CefSettings settings = new CefSettings();
			settings.windowless_rendering_enabled = true;
			settings.background_color = settings.new ColorType(0, 255, 255, 255);
			settings.locales_dir_path = (new File(ROOT, "MCEFLocales")).getAbsolutePath();
			settings.cache_path = (new File(ROOT, "MCEFCache")).getAbsolutePath();

			settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;

			ArrayList<String> libs = new ArrayList<>();
			if (OS.isWindows()) {
				File subproc = new File(ROOT, "jcef_helper.exe");
				settings.browser_subprocess_path = subproc.getAbsolutePath();

				libs.add("d3dcompiler_47.dll");
				libs.add("libGLESv2.dll");
				libs.add("libEGL.dll");
				libs.add("chrome_elf.dll");
				libs.add("libcef.dll");
				libs.add("jcef.dll");
			}


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
		return cefClient;
	}


	public static void loadMimeTypeMapping() {
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

	public static void close(Object o) {
		try {
			o.getClass().getMethod("close").invoke(o);
		} catch(Throwable t) {}
	}

	public static String mimeTypeFromExtension(String ext) {
		ext = ext.toLowerCase();
		String ret = mimeTypeMap.get(ext);
		if(ret != null)
			return ret;

		//If the mimeTypeMap couldn't be loaded, fall back to common things
		switch(ext) {
			case "htm":
			case "html":
				return "text/html";

			case "css":
				return "text/css";

			case "js":
				return "text/javascript";

			case "png":
				return "image/png";

			case "jpg":
			case "jpeg":
				return "image/jpeg";

			case "gif":
				return "image/gif";

			case "svg":
				return "image/svg+xml";

			case "xml":
				return "text/xml";

			case "txt":
				return "text/plain";

			default:
				return null;
		}
	}
}
