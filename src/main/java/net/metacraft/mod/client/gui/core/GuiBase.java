package net.metacraft.mod.client.gui.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public class GuiBase extends Screen {

    interface CallBack {
        boolean call(GuiBase c);
    }

    protected static List<Rect> clickRect = new ArrayList<>();

    protected GuiBase parent = null;

    private final List<GuiBase> children = new CopyOnWriteArrayList<>();

    public String name = "GuiBase";

    /**
     * The component's own size.
     */
    int properWidth;
    int properHeight;

    /**
     * The component's total calculated size, including itself and its children.
     */
    int contentWidth;
    int contentHeight;

    /**
     * If true, content size will be validated on the next update.
     */
    private boolean sizeIsInvalid = false;

    /**
     * If true, this GUI will not be rendered.
     */
    protected boolean isClipped = false;

    private int positionX = 0;

    private int positionY = 0;

    protected boolean isMouseOver = false;

    private final HoveringTextInfo hoveringTextInfo = new HoveringTextInfo();

    public GuiBase() {
        super(new LiteralText("GuiBase"));
    }

    public void setGuiPosition(int x, int y) {
        int dx = x - positionX;
        int dy = y - positionY;
        this.positionX = x;
        this.positionY = y;
        for (GuiBase child : children) {
            child.offsetGuiPosition(dx, dy);
        }
        if (parent != null && (dx != 0 || dy != 0)) {
            parent.invalidateSize();
        }
    }

    public final void setRelativePosition(int x, int y) {
        if (parent != null) {
            setGuiPosition(parent.getPositionX() + x, parent.getPositionY() + y);
        } else {
            setGuiPosition(x, y);
        }
    }

    public final void setRelativeX(int x) {
        if (parent != null) {
            setGuiPosition(parent.getPositionX() + x, positionY);
        } else {
            setGuiPosition(x, positionY);
        }
    }

    public final void setRelativeY(int y) {
        if (parent != null) {
            setGuiPosition(positionX, parent.getPositionY() + y);
        } else {
            setGuiPosition(positionX, y);
        }
    }

    public final void offsetGuiPosition(int dx, int dy) {
        setGuiPosition(positionX + dx, positionY + dy);
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getRelativeX() {
        return parent == null ? positionX : (positionX - parent.positionX);
    }

    public int getRelativeY() {
        return parent == null ? positionY : (positionY - parent.positionY);
    }

    public void setSize(int width, int height) {
        this.properWidth = width;
        this.properHeight = height;
        this.contentWidth = width;
        this.contentHeight = height;
        invalidateSize();
    }

    public GuiBase addChild(GuiBase child) {
        if (child == null || children.contains(child) || parent == child) {
            return child;
        }
        if (child.parent != null) {
            child.parent.children.remove(child);
        }
        children.add(child);
        child.parent = this;
        child.setGuiPosition(positionX, positionY);
        if (MinecraftClient.getInstance() != null) {
            child.clearChildren();
            child.init(MinecraftClient.getInstance(), width, height);
        }
        invalidateSize();
        return child;
    }

    public GuiBase addChildWithoutFixPos(GuiBase child) {
        if (child == null || children.contains(child) || parent == child) {
            return child;
        }
        if (child.parent != null) {
            child.parent.children.remove(child);
        }
        children.add(child);
        child.parent = this;
        if (MinecraftClient.getInstance() != null) {
            child.clearChildren();
            child.init(MinecraftClient.getInstance(), width, height);
        }
        invalidateSize();
        return child;
    }

    protected GuiBase removeChild(GuiBase child) {
        if (child != null && children.contains(child)) {
            child.parent = null;
            children.remove(child);
            invalidateSize();
        }
        return child;
    }

    void removeAllChildren() {
        children.forEach(base -> base = null);
        children.clear();
        invalidateSize();
    }

    public GuiBase getParent() {
        return parent;
    }

    public List<GuiBase> getChildren() {
        return children;
    }

    private boolean iterateInput(CallBack callMethod) {
        ListIterator<GuiBase> iterator = children.listIterator(children.size());
        while (iterator.hasPrevious()) {
            GuiBase child = iterator.previous();
            if (callMethod.call(child)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!iterateMouseInput(child -> child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))) {
            return super.mouseClicked(mouseX, mouseY, button);
        } else {
            return true;
        }
    }

    private boolean iterateMouseInput(CallBack callBack) {
        isMouseOver = isMouseInRegion(getPositionX(), getPositionY(), getContentWidth(), getContentHeight());
        if (!iterateInput(child -> {
            child.isMouseOver = child.isMouseInRegion(child.getPositionX(), child.getPositionY(), child.getContentWidth(), child.getContentHeight());
            return callBack.call(child);
        })) {
            return false;
        } else {
            isMouseOver = false;
            return true;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!iterateMouseInput(child -> child.mouseClicked(mouseX, mouseY, button))) {
            return super.mouseClicked(mouseX, mouseY, button);
        } else {
            return true;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!iterateMouseInput(child -> child.mouseReleased(mouseX, mouseY, button))) {
            return super.mouseReleased(mouseX, mouseY, button);
        } else {
            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!iterateMouseInput(child -> child.mouseScrolled(mouseX, mouseY, amount))) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        } else {
            return true;
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (!iterateMouseInput(child -> {
            child.mouseMoved(mouseX, mouseY);
            return false;
        })) {
            super.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int a, int b, int c) {
        if (!iterateInput(child -> child.keyPressed(a, b, c))) {
            return super.keyPressed(a, b, c);
        } else {
            return true;
        }
    }

    @Override
    public boolean charTyped(char aa, int bb) {
        if (!iterateInput(child -> child.charTyped(aa, bb))) {
            return super.charTyped(aa, bb);
        } else {
            return true;
        }
    }

    @Override
    public boolean keyReleased(int a, int b, int c) {
        if (!iterateInput(child -> child.keyReleased(a, b, c))) {
            return super.keyReleased(a, b, c);
        } else {
            return true;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTick) {
        super.render(matrices, mouseX, mouseY, partialTick);
        for (GuiBase child : children) {
            if (!child.isClipped) {
                child.render(matrices, mouseX, mouseY, partialTick);
            }
        }

        if (hoveringTextInfo.isShouldDraw() && !hoveringTextInfo.getLines().isEmpty()) {
            renderTooltip(matrices, hoveringTextInfo.getLines(), (int) hoveringTextInfo.getX() - 10, (int) hoveringTextInfo.getY());
            hoveringTextInfo.shouldDraw = false;
        }
    }

    protected void drawTooltip(List<Text> lines) {
        GuiBase topLevel = getTopLevelParent();
        topLevel.hoveringTextInfo.lines = lines;
        topLevel.hoveringTextInfo.x = getMouseX();
        topLevel.hoveringTextInfo.y = getMouseY();
        topLevel.hoveringTextInfo.shouldDraw = true;
    }

    public GuiBase getTopLevelParent() {
        GuiBase component = this;
        while (component.parent != null) {
            component = component.parent;
        }
        return component;
    }

    @Override
    public void onClose() {
        for (GuiBase child : children) {
            child.onClose();
        }
        super.onClose();
    }

    /**
     * Remove itself from its parent component (if any), notifying it.
     */
    public void close() {
        if (parent != null) {
            parent.removeChild(this); // This sets parent to null
        }
    }

    protected void closeAllChildren(GuiBase guiBase) {
        if (guiBase.children.isEmpty()) {
            return;
        }
        for (GuiBase child : guiBase.children) {
            child.isClipped = true;
            closeAllChildren(child);
            child.removeAllChildren();
        }
    }

    protected void closeAll() {
        if (parent == null) {
            onClose();
            return;
        }
        GuiBase superParent = parent;
        while (superParent.parent != null) {
            superParent = superParent.parent;
            onClose();
        }
        onClose();
    }

    @Override
    public void tick() {
        for (GuiBase child : children) {
            child.tick();
        }

        super.tick();
        if (sizeIsInvalid) {
            validateSize();
        }
    }

    @Override
    public void init() {
        super.init();
        for (GuiBase child : children) {
            child.init(MinecraftClient.getInstance(), width, height);
        }
    }

    protected int getContentWidth() {
        return contentWidth;
    }

    protected int getContentHeight() {
        return contentHeight;
    }

    protected void setClipped(boolean value) {
        this.isClipped = value;
    }


    private void invalidateSize() {
        sizeIsInvalid = true;
        if (parent != null) {
            parent.invalidateSize();
        }
    }

    protected void validateSize() {
        int leftmost = Integer.MAX_VALUE;
        int rightmost = Integer.MIN_VALUE;
        int topmost = Integer.MAX_VALUE;
        int bottommost = Integer.MIN_VALUE;
        for (GuiBase child : children) {
            int x = child.getPositionX();
            if (x < leftmost) {
                leftmost = x;
            }
            int childWidth = child.getContentWidth();
            if (x + childWidth > rightmost) {
                rightmost = x + childWidth;
            }
            int y = child.getPositionY();
            if (y < topmost) {
                topmost = y;
            }
            int childHeight = child.getContentHeight();
            if (y + childHeight > bottommost) {
                bottommost = y + childHeight;
            }
        }
        contentWidth = Math.max(properWidth, rightmost - leftmost);
        contentHeight = Math.max(properHeight, bottommost - topmost);
        sizeIsInvalid = false;
    }

    protected boolean isMouseInRegionWithParent(int left, int top, int width, int height) {
        GuiBase tmpParent = parent;
        while (tmpParent != null) {
            if (!isMouseInRegion(tmpParent.getPositionX(), tmpParent.getPositionY(), tmpParent.getContentWidth(), tmpParent.getContentHeight())) {
                return false;
            }
            tmpParent = tmpParent.parent;
        }
        double mouseX = getMouseX();
        double mouseY = getMouseY();
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    private boolean isMouseInClickRect(int mouseX, int mouseY) {
        for (Rect rect : clickRect) {
            boolean isInRect = mouseX >= rect.getX()
                    && mouseX < rect.getX() + rect.getRectWidth()
                    && mouseY >= rect.getY()
                    && mouseY < rect.getY() + rect.getRectHeight();
            if (!isInRect) {
                return false;
            }
        }
        return true;
    }

    protected boolean isMouseInRegion(int left, int top, int width, int height) {
        double mouseX = getMouseX();
        double mouseY = getMouseY();
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    protected double getMouseX() {
        return MinecraftClient.getInstance().mouse.getX() * width / MinecraftClient.getInstance().getWindow().getWidth();
    }

    protected double getMouseY() {
        return MinecraftClient.getInstance().mouse.getY() * height / MinecraftClient.getInstance().getWindow().getHeight();
    }

    private static class HoveringTextInfo {
        private List<Text> lines;

        private double x;

        private double y;

        private boolean shouldDraw = false;

        public List<Text> getLines() {
            return lines;
        }

        public void setLines(List<Text> lines) {
            this.lines = lines;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public boolean isShouldDraw() {
            return shouldDraw;
        }

        public void setShouldDraw(boolean shouldDraw) {
            this.shouldDraw = shouldDraw;
        }
    }

    protected static class Rect {
        private int x, y;

        private int rectWidth, rectHeight;

        public Rect(int x, int y, int rectWidth, int rectHeight) {
            this.x = x;
            this.y = y;
            this.rectWidth = rectWidth;
            this.rectHeight = rectHeight;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getRectWidth() {
            return rectWidth;
        }

        public void setRectWidth(int rectWidth) {
            this.rectWidth = rectWidth;
        }

        public int getRectHeight() {
            return rectHeight;
        }

        public void setRectHeight(int rectHeight) {
            this.rectHeight = rectHeight;
        }
    }
}
