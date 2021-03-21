package dev.webfx.kit.mapper.peers.javafxgraphics.gwt.html.layoutmeasurable;

import elemental2.dom.CSSProperties;
import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.HTMLElement;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import dev.webfx.kit.mapper.peers.javafxgraphics.gwt.html.HtmlNodePeer;
import dev.webfx.kit.mapper.peers.javafxgraphics.emul_coupling.LayoutMeasurable;

import java.util.logging.Logger;

/**
 * @author Bruno Salmon
 */
public interface HtmlLayoutMeasurable extends LayoutMeasurable {

    HTMLElement getElement();

    default Bounds getLayoutBounds() {
        Bounds layoutBounds;
        HtmlLayoutCache cache = getCache();
        if (cache != null) {
            layoutBounds = cache.getCachedLayoutBounds();
            if (layoutBounds != null)
                return layoutBounds;
        }
        layoutBounds = measureLayoutBounds();
        if (cache != null)
            cache.setCachedLayoutBounds(layoutBounds);
        return layoutBounds;
    }

    default Bounds measureLayoutBounds() {
        HTMLElement e = getElement();
        return new BoundingBox(0, 0, 0, measure(e, true), measure(e, false), 0);
    }

    default double minWidth(double height) {
        return 0;
    }

    default double maxWidth(double height) {
        return Double.MAX_VALUE;
    }

    default double minHeight(double width) {
        return 0;
    }

    default double maxHeight(double width) {
        return Double.MAX_VALUE;
    }

    default double prefWidth(double height) {
        return measureWidth(height);
    }

    default double prefHeight(double width) {
        return measureHeight(width);
    }

    default double measureWidth(double height) {
        return sizeAndMeasure(height, true);
    }

    default double measureHeight(double width) {
        return sizeAndMeasure(width, false);
    }

    default double sizeAndMeasure(double value, boolean width) {
        HtmlLayoutCache cache = getCache();
        if (cache != null) {
            double cachedSize = cache.getCachedSize(value, width);
            if (cachedSize >= 0)
                return cachedSize;
        }
        HTMLElement e = getElement();
        CSSStyleDeclaration style = e.style;
        CSSProperties.WidthUnionType styleWidth = style.width;
        CSSProperties.HeightUnionType styleHeight = style.height;
        if (width) {
            style.width = null;
            if (value >= 0)
                style.height = CSSProperties.HeightUnionType.of(HtmlNodePeer.toPx(value));
        } else {
            if (value >= 0)
                style.width = CSSProperties.WidthUnionType.of(HtmlNodePeer.toPx(value));
            style.height = null;
        }
        double result = measure(e, width);
        style.width = styleWidth;
        style.height = styleHeight;
        if (cache != null)
            cache.setCachedSize(value, width, result);

        Logger.getLogger(HtmlLayoutMeasurable.class.getName()).info("sizeAndMeasure() " + getClass() + " < " + value + " > " + result);

        return result;
    }

    default double measure(HTMLElement e, boolean width) {
        return width ? e.offsetWidth : e.offsetHeight;
    }

    default HtmlLayoutCache getCache() {
        return null;
    }

    @Override
    default void clearCache() {
        HtmlLayoutCache cache = getCache();
        if (cache != null)
            cache.clearCache();
    }
}
