package naga.framework.ui.controls;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import naga.fx.properties.Properties;

import static javafx.scene.layout.Region.USE_PREF_SIZE;

/**
 * @author Bruno Salmon
 */
public class LayoutUtil {

    public static GridPane createGoldLayout(Region child) {
        return createGoldLayout(child, 0, 0);
    }

    public static GridPane createGoldLayout(Region child, double percentageWidth, double percentageHeight) {
        GridPane goldPane = new GridPane();
        goldPane.setAlignment(Pos.TOP_CENTER); // Horizontal alignment
        RowConstraints headerRowConstraints = new RowConstraints();
        headerRowConstraints.prefHeightProperty().bind(Properties.combine(goldPane.heightProperty(), child.heightProperty(),
                (gpHeight, cHeight) -> {
                    if (percentageHeight != 0)
                        child.setPrefHeight(gpHeight.doubleValue() * percentageHeight);
                    Platform.runLater(() -> goldPane.getRowConstraints().setAll(headerRowConstraints));
                    return (gpHeight.doubleValue() - cHeight.doubleValue()) / 2.61;
                }));
        if (percentageWidth != 0)
            child.prefWidthProperty().bind(Properties.compute(goldPane.widthProperty(), gpWidth -> gpWidth.doubleValue() * percentageWidth));
        goldPane.add(setMaxSizeToPref(child), 0, 1);
        goldPane.setBackground(new Background(new BackgroundFill(Color.gray(0.3, 0.5), null, null)));
        return goldPane;
    }

    public static Region createHGrowable() {
        return setHGrowable(setMaxWidthToInfinite(new Region()));
    }

    public static <N extends Node> N setHGrowable(N node) {
        HBox.setHgrow(node, Priority.ALWAYS);
        return node;
    }

    public static <N extends Node> N setVGrowable(N node) {
        VBox.setVgrow(node, Priority.ALWAYS);
        return node;
    }

    public static <N extends Region> N setMinSizeToZero(N region) {
        return setMinSize(region, 0);
    }

    private static <N extends Region> N setMinSize(N region, double value) {
        region.setMinWidth(value);
        region.setMinHeight(value);
        return region;
    }

    public static <N extends Region> N setMinSizeToZeroAndPrefSizeToInfinite(N region) {
        return setMinSizeToZero(setPrefSizeToInfinite(region));
    }

    public static <N extends Region> N setPrefSizeToInfinite(N region) {
        return setPrefSize(setMaxSizeToInfinite(region), Double.MAX_VALUE);
    }

    private static <N extends Region> N setPrefSize(N region, double value) {
        region.setPrefWidth(value);
        region.setPrefHeight(value);
        return region;
    }

    public static <N extends Region> N setMaxSizeToInfinite(N region) {
        return setMaxSize(region, Double.MAX_VALUE);
    }

    public static <N extends Region> N setMaxSizeToPref(N region) {
        return setMaxSize(region, USE_PREF_SIZE);
    }

    private static <N extends Region> N setMaxSize(N region, double value) {
        region.setMaxWidth(value);
        region.setMaxHeight(value);
        return region;
    }

    public static <N extends Region> N setPrefWidthToInfinite(N region) {
        region.setPrefWidth(Double.MAX_VALUE);
        return setMaxWidthToInfinite(region);
    }

    public static <N extends Region> N setMaxWidthToInfinite(N region) {
        region.setMaxWidth(Double.MAX_VALUE);
        return region;
    }

    public static <N extends Node> N setUnmanagedWhenInvisible(N node) {
        node.managedProperty().bind(node.visibleProperty());
        return node;
    }

    public static <N extends Node> N setUnmanagedWhenInvisible(N node, ObservableValue<Boolean> visibleProperty) {
        node.visibleProperty().bind(visibleProperty);
        return setUnmanagedWhenInvisible(node);
    }
}
