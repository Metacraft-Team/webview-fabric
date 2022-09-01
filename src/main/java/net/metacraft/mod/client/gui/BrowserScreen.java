package net.metacraft.mod.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.metacraft.mod.api.API;
import net.metacraft.mod.api.IBrowser;
import net.metacraft.mod.api.IDisplayHandler;
import net.metacraft.mod.api.CEFApi;
import net.metacraft.mod.client.ClientProxy;
import net.metacraft.mod.client.gui.core.GuiBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

public class BrowserScreen extends GuiBase implements IDisplayHandler {
    IBrowser browser = null;


    public BrowserScreen() {
    }

    @Override
    public void init() {
        super.init();

        if (browser == null) {
            try {
                API api = CEFApi.getAPI();
                browser = api.createBrowser("http://www.metacraft.cc/", false);
                System.out.println("create finish with browser: " + browser);
            } catch (Throwable e) {
                System.out.println("error : " + e);
            }

        }
        browser.resize(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight() - scaleY(20));

    }

    public int scaleY(int y) {
        double sy = ((double) y) / ((double) height) * ((double) MinecraftClient.getInstance().getWindow().getHeight());
        return (int) sy;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (browser != null) {
            GlStateManager._disableDepthTest();
            GlStateManager._enableTexture();
//            GlStateManager.sh(1.0f, 1.0f, 1.0f, 1.0f);
            browser.draw(.0d, height, width, .0d); //Don't forget to flip Y axis.
            GlStateManager._enableDepthTest();
        } else {

        }
    }

    @Override
    public void tick() {
        super.tick();
        if (browser != null) {
            ClientProxy.cefApp.N_DoMessageLoopWork();
            browser.mcefUpdate();
            ClientProxy.getInstance().getDisplayHandler().update();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String typeString = GLFW.glfwGetKeyName(keyCode, scanCode);
        if (StringUtils.isNotEmpty(typeString)) {
            browser.injectKeyPressedByKeyCode(keyCode, typeString.charAt(0), 0);

        }
        return super.keyPressed(keyCode, scanCode, modifiers);

    }




    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        String typeString = GLFW.glfwGetKeyName(keyCode, scanCode);
        if (StringUtils.isNotEmpty(typeString)) {
            browser.injectKeyPressedByKeyCode(keyCode, typeString.charAt(0), 0);

        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char aa, int bb) {
//        browser.injectKeyPressedByKeyCode(GLOBAL_KEYCODE, aa, 0);
        browser.injectKeyTyped(aa, 0);
        return super.charTyped(aa, bb);
    }


    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        double mx = MinecraftClient.getInstance().mouse.getX();
        double my = MinecraftClient.getInstance().mouse.getY();
        browser.injectMouseMove((int) mx, (int) my, 0, my<0);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("button is " + button);
        if (browser != null) {
            double mx = MinecraftClient.getInstance().mouse.getX();
            double my = MinecraftClient.getInstance().mouse.getY();

            int y = (int) (my - scaleY(20));
            if(button == -1)
                browser.injectMouseMove((int) mx, y, 0, y < 0);
            else {
                browser.injectMouseButton((int) mx, y, 0, button + 1, true, 1);
            }
        }
        return true;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (browser != null) {
            double mx = MinecraftClient.getInstance().mouse.getX();
            double my = MinecraftClient.getInstance().mouse.getY();
            int y = (int) (my - scaleY(20));
            browser.injectMouseWheel((int) mx, y, 0, 1, (int) (amount * 120));
        }


        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        System.out.println("button is " + button);
        if (browser != null) {
            double mx = MinecraftClient.getInstance().mouse.getX();
            double my = MinecraftClient.getInstance().mouse.getY();

            int y = (int) (my - scaleY(20));

            if(button == -1)
                browser.injectMouseMove((int) mx, y, 0, y < 0);
            else {
                browser.injectMouseButton((int) mx, y, 0, button + 1, false, 1);
            }
        }
        return true;
    }

    @Override
    public void onAddressChange(IBrowser browser, String url) {

    }

    @Override
    public void onTitleChange(IBrowser browser, String title) {

    }

    @Override
    public void onTooltip(IBrowser browser, String text) {

    }

    @Override
    public void onStatusMessage(IBrowser browser, String value) {

    }
}
