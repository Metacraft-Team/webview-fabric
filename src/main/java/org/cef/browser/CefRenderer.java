// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

// Modified by montoyo for MCEF

package org.cef.browser;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTBGRA;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

public class CefRenderer {

    //montoyo: debug tool
    private static final ArrayList<Integer> GL_TEXTURES = new ArrayList<>();
    public static void dumpVRAMLeak() {
        System.out.println(">>>>> MCEF: Beginning VRAM leak report");
        GL_TEXTURES.forEach(tex -> System.out.println((">>>>> MCEF: This texture has not been freed: " + tex)));
        System.out.println((">>>>> MCEF: End of VRAM leak report"));
    }

    private boolean transparent_;
    public int[] texture_id_ = new int[1];
    private int view_width_ = 0;
    private int view_height_ = 0;
    private Rectangle popup_rect_ = new Rectangle(0, 0, 0, 0);
    private Rectangle original_popup_rect_ = new Rectangle(0, 0, 0, 0);

    protected CefRenderer(boolean transparent) {
        transparent_ = transparent;
        initialize();
    }

    protected boolean isTransparent() {
        return transparent_;
    }

    @SuppressWarnings("static-access")
    protected void initialize() {
//        GlStateManager._enableTexture();
        texture_id_[0] = glGenTextures();
//        createTexture(texture_id_[0], DataHandler.INSTANCE.bufferedImage);
//        if(MCEF.CHECK_VRAM_LEAK)
//            GL_TEXTURES.add(texture_id_[0]);

        GlStateManager._bindTexture(texture_id_[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
////        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        GlStateManager._bindTexture(0);
    }



    /**
     * Uploads the opengl texture.
     *
     * @param textureId The texture id to upload to.
     * @param image     The image to upload.
     */
    private void createTexture(int textureId, BufferedImage image) {
        System.out.println(image.getWidth() + " " + image.getHeight());
        // Array of all the colors in the image.
        int[] pixels = new int[image.getWidth() * image.getHeight()];

        // Fetches all the colors in the image.
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        // Buffer that will store the texture data.
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB

        // Puts all the pixel data into the buffer.
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                // The pixel in the image.
                int pixel = pixels[y * image.getWidth() + x];

                // Puts the data into the byte buffer.
                buffer.put((byte)((pixel >> 16) & 0xFF));
                buffer.put((byte)((pixel >> 8) & 0xFF));
                buffer.put((byte)(pixel & 0xFF));
                buffer.put((byte)((pixel >> 24) & 0xFF));
            }
        }

        // Flips the byte buffer, not sure why this is needed.
        buffer.flip();
        GlStateManager._enableTexture();
        // Binds the opengl texture by the texture id.
        GlStateManager._bindTexture(textureId);

        // Sets the texture parameter stuff.
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Uploads the texture to opengl.
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Binds the opengl texture 0.
        GlStateManager._bindTexture(0);
    }

    protected void cleanup() {
        if(texture_id_[0] != 0) {
//            if(MCEF.CHECK_VRAM_LEAK)
//                GL_TEXTURES.remove((Object) texture_id_[0]);

            glDeleteTextures(texture_id_[0]);
        }
    }

    public void render(double x1, double y1, double x2, double y2) {
        if(view_width_ == 0 || view_height_ == 0)
            return;

        Tessellator t = Tessellator.getInstance();
        BufferBuilder vb = t.getBuffer();
//        GlStateManager._enableTexture();
//        GlStateManager._bindTexture(texture_id_[0]);
        RenderSystem.setShaderTexture(0, texture_id_[0]);
        vb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        vb.vertex(x1, y1, 0.0).texture(0.0f, 1.0f).color(255, 255, 255, 255).next();
        vb.vertex(x2, y1, 0.0).texture(1.f, 1.f).color(255, 255, 255, 255).next();
        vb.vertex(x2, y2, 0.0).texture(1.f, 0.0f).color(255, 255, 255, 255).next();
        vb.vertex(x1, y2, 0.0).texture(0.0f, 0.0f).color(255, 255, 255, 255).next();
        t.draw();
        GlStateManager._bindTexture(0);
    }

    protected void onPopupSize(Rectangle rect) {
        if(rect.width <= 0 || rect.height <= 0)
            return;
        original_popup_rect_ = rect;
        popup_rect_ = getPopupRectInWebView(original_popup_rect_);
    }

    protected Rectangle getPopupRectInWebView(Rectangle rc) {
        // if x or y are negative, move them to 0.
        if(rc.x < 0)
            rc.x = 0;
        if(rc.y < 0)
            rc.y = 0;
        // if popup goes outside the view, try to reposition origin
        if(rc.x + rc.width > view_width_)
            rc.x = view_width_ - rc.width;
        if(rc.y + rc.height > view_height_)
            rc.y = view_height_ - rc.height;
        // if x or y became negative, move them to 0 again.
        if(rc.x < 0)
            rc.x = 0;
        if(rc.y < 0)
            rc.y = 0;
        return rc;
    }

    protected void clearPopupRects() {
        popup_rect_.setBounds(0, 0, 0, 0);
        original_popup_rect_.setBounds(0, 0, 0, 0);
    }

    protected void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {
        if(transparent_) // Enable alpha blending.
            GlStateManager._enableBlend();

        final int size = (width * height) << 2;
        if(size > buffer.limit()) {
//            System.out.println("Bad data passed to CefRenderer.onPaint() triggered safe guards... (1)");
//            return;
        }

        // Enable 2D textures.
        GlStateManager._enableTexture();
        GlStateManager._bindTexture(texture_id_[0]);

        int oldAlignement = GlStateManager._getInteger(GL_UNPACK_ALIGNMENT);
        GlStateManager._pixelStore(GL_UNPACK_ALIGNMENT, 1);

        if(!popup) {
            if(completeReRender || width != view_width_ || height != view_height_) {
                // Update/resize the whole texture.
                view_width_ = width;
                view_height_ = height;
                glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
                glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, view_width_, view_height_, 0, EXTBGRA.GL_BGRA_EXT, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
            }
            else {
                GlStateManager._pixelStore(GL_UNPACK_ROW_LENGTH, view_width_);

                // Update just the dirty rectangles.
                for(Rectangle rect: dirtyRects) {
                    if(rect.x < 0 || rect.y < 0 || rect.x + rect.width > view_width_ || rect.y + rect.height > view_height_)
                        System.out.println("Bad data passed to CefRenderer.onPaint() triggered safe guards... (2)");
                    else {
                        System.out.println(""+rect.x+" "+rect.y+" "+rect.width+" "+rect.height);
                        GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, rect.x);
                        GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, rect.y);
                        glTexSubImage2D(GL_TEXTURE_2D, 0, rect.x, rect.y, rect.width, rect.height, EXTBGRA.GL_BGRA_EXT, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
                    }
                }

                GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, 0);
                GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, 0);
                GlStateManager._pixelStore(GL_UNPACK_ROW_LENGTH, 0);
            }
        } else if(popup_rect_.width > 0 && popup_rect_.height > 0) {
            int skip_pixels = 0, x = popup_rect_.x;
            int skip_rows = 0, y = popup_rect_.y;
            int w = width;
            int h = height;

            // Adjust the popup to fit inside the view.
            if(x < 0) {
                skip_pixels = -x;
                x = 0;
            }
            if(y < 0) {
                skip_rows = -y;
                y = 0;
            }
            if(x + w > view_width_)
                w -= x + w - view_width_;
            if(y + h > view_height_)
                h -= y + h - view_height_;

            // Update the popup rectangle.
            GlStateManager._pixelStore(GL_UNPACK_ROW_LENGTH, width);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, skip_pixels);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, skip_rows);
            glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, w, h, EXTBGRA.GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);
            GlStateManager._pixelStore(GL_UNPACK_ROW_LENGTH, 0);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, 0);
            GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, 0);
        }

        GlStateManager._pixelStore(GL_UNPACK_ALIGNMENT, oldAlignement);
        GlStateManager._bindTexture(0);
    }

    public int getViewWidth() {
        return view_width_;
    }

    public int getViewHeight() {
        return view_height_;
    }

}
