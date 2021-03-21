package dev.webfx.kit.mapper.peers.javafxcontrols.gwt.html;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import dev.webfx.kit.mapper.peers.javafxgraphics.base.RegionPeerBase;
import dev.webfx.kit.mapper.peers.javafxgraphics.base.RegionPeerMixin;
import dev.webfx.kit.mapper.peers.javafxgraphics.gwt.html.HtmlRegionPeer;
import dev.webfx.kit.mapper.peers.javafxgraphics.gwt.html.layoutmeasurable.HtmlLayoutMeasurable;
import dev.webfx.kit.mapper.peers.javafxgraphics.gwt.util.HtmlUtil;
import javafx.scene.chart.Chart;
import javafx.scene.layout.Region;

import java.util.logging.Logger;

public final class HtmlChartPeer
        <N extends Region, NB extends RegionPeerBase<N, NB, NM>, NM extends RegionPeerMixin<N, NB, NM>>

        extends HtmlRegionPeer<N, NB, NM>
        implements RegionPeerMixin<N, NB, NM>, HtmlLayoutMeasurable {

    public interface Resources extends ClientBundle {
        Resources INSTANCE =  GWT.create(Resources.class);

        @Source("modena.css")
        @DataResource.DoNotEmbed
        DataResource modenaStylsheet();
    }

    static {
//        String url = ResourceService.toUrl("modena.css", HtmlChartPeer.class);
        String url = Resources.INSTANCE.modenaStylsheet().getSafeUri().asString();
        LinkElement stylesheet = Document.get().createLinkElement();
        stylesheet.setPropertyString("rel", "stylesheet");
        stylesheet.setPropertyString("type", "text/css");
        stylesheet.setPropertyString("href", url);
        Element head = Document.get().getElementsByTagName("head").getItem(0);
        head.appendChild(stylesheet);
    }

    public HtmlChartPeer() {
        super((NB) new RegionPeerBase(), HtmlUtil.createElement("fx-htmlchartpeer"));
        Logger.getLogger(Chart.class.getName()).info("---------------- HtmlChartPeer() -----------------");
        Logger.getLogger(Chart.class.getName()).info("---------------- node: "+getNode()+" -----------------");
    }
}
