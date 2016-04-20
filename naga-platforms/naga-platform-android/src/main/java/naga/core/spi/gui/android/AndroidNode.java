package naga.core.spi.gui.android;

import android.view.View;
import naga.core.spi.gui.GuiNode;

/**
 * @author Bruno Salmon
 */
public class AndroidNode<N extends View> implements GuiNode<N> {

    protected final N node;

    public AndroidNode(N node) {
        this.node = node;
    }

    @Override
    public N unwrapToToolkitNode() {
        return node;
    }

    @Override
    public void requestFocus() {
        node.requestFocus();
    }

}