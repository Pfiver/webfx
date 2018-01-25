package naga.fx.properties;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.util.Duration;
import naga.fx.spi.Toolkit;
import naga.util.Objects;
import naga.util.collection.Collections;
import naga.util.function.Consumer;
import naga.util.function.Func2;
import naga.util.function.Function;
import naga.util.function.Predicate;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public class Properties {

    public static Unregistrable runOnPropertiesChange(Consumer<ObservableValue> consumer, ObservableValue... properties) {
        return new Unregistrable(consumer, properties);
    }

    public static Unregistrable runNowAndOnPropertiesChange(Consumer<ObservableValue> consumer, ObservableValue... properties) {
        consumer.accept(properties.length == 1 ? properties[0] : null);
        return runOnPropertiesChange(consumer, properties);
    }

    // Same API but with Collection instead of varargs

    public static Unregistrable runNowAndOnPropertiesChange(Consumer<ObservableValue> consumer, Collection<ObservableValue> properties) {
        return runNowAndOnPropertiesChange(consumer, Collections.toArray(properties, ObservableValue[]::new));
    }

    public static Unregistrable runOnPropertiesChange(Consumer<ObservableValue> consumer, Collection<ObservableValue> properties) {
        return runOnPropertiesChange(consumer, Collections.toArray(properties, ObservableValue[]::new));
    }

    // Same API but with Runnable instead of Consumer

    public static Unregistrable runOnPropertiesChange(Runnable runnable, ObservableValue... properties) {
        return runOnPropertiesChange(p -> runnable.run(), properties);
    }

    public static Unregistrable runNowAndOnPropertiesChange(Runnable runnable, ObservableValue... properties) {
        return runNowAndOnPropertiesChange(p -> runnable.run(), properties);
    }

    public static Unregistrable runNowAndOnPropertiesChange(Runnable runnable, Collection<ObservableValue> properties) {
        return runNowAndOnPropertiesChange(p -> runnable.run(), properties);
    }

    public static Unregistrable runOnPropertiesChange(Runnable runnable, Collection<ObservableValue> properties) {
        return runOnPropertiesChange(p -> runnable.run(), properties);
    }


    public static <T, R> ObservableValue<R> compute(ObservableValue<? extends T> p, Function<? super T, ? extends R> function) {
        Property<R> combinedProperty = new SimpleObjectProperty<>();
        runNowAndOnPropertiesChange(arg -> combinedProperty.setValue(function.apply(p.getValue())), p);
        return combinedProperty;
    }

    public static ObservableValue<Boolean> not(ObservableValue<Boolean> p) {
        return compute(p, value -> !value);
    }

    public static <T1, T2, R> ObservableValue<R> combine(ObservableValue<? extends T1> p1, ObservableValue<? extends T2> p2, Func2<? super T1, ? super T2, ? extends R> combineFunction) {
        Property<R> combinedProperty = new SimpleObjectProperty<>();
        runNowAndOnPropertiesChange(arg -> combinedProperty.setValue(combineFunction.call(p1.getValue(), p2.getValue())), p1, p2);
        return combinedProperty;
    }

    public static <T> ObservableValue<T> filter(ObservableValue<T> property, Predicate<T> predicate) {
        Property<T> filteredProperty = new SimpleObjectProperty<>();
        runNowAndOnPropertiesChange(arg -> { if (predicate.test(property.getValue())) filteredProperty.setValue(property.getValue()); }, property);
        return filteredProperty;
    }

    public static <T> void consume(ObservableValue<T> property, Consumer<T> consumer) {
        runNowAndOnPropertiesChange(p -> consumer.accept(property.getValue()), property);
    }

    public static <T> void consumeInUiThread(ObservableValue<T> property, Consumer<T> consumer) {
        consume(property, arg -> Toolkit.get().scheduler().scheduleDeferred(() -> consumer.accept(arg)));
    }

    public static <T> void setIfNotBound(Property<T> property, T value) {
        if (!property.isBound())
            property.setValue(value);
    }

    public final static Interpolator EASE_OUT_INTERPOLATOR = Interpolator.SPLINE(0, .75, .25, 1);

    public static <T> void animateProperty(WritableValue<T> target, T finalValue) {
        animateProperty(target, finalValue, true);
    }

    public static <T> void animateProperty(WritableValue<T> target, T finalValue, boolean animate) {
        animateProperty(target, finalValue, animate ? EASE_OUT_INTERPOLATOR : null);
    }

    public static <T> void animateProperty(WritableValue<T> target, T finalValue, Interpolator interpolator) {
        if (!Objects.areEquals(target.getValue(), finalValue)) {
            if (interpolator == null)
                target.setValue(finalValue);
            else
                new Timeline(new KeyFrame(Duration.seconds(1), new KeyValue(target, finalValue, interpolator))).play();
        }
    }

    public static <T> void onPropertySet(ObservableValue<T> property, Consumer<T> valueConsumer) {
        onPropertySet(property, valueConsumer, false);
    }

    public static <T> void onPropertySet(ObservableValue<T> property, Consumer<T> valueConsumer, boolean callIfNullProperty) {
        if (property == null) {
            if (callIfNullProperty)
                valueConsumer.accept(null);
        } else {
            T value = property.getValue();
            if (value != null)
                valueConsumer.accept(value);
            else
                property.addListener(new ChangeListener<T>() {
                    @Override
                    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                        if (newValue != null) {
                            observable.removeListener(this);
                            valueConsumer.accept(newValue);
                        }
                    }
                });
        }
    }
}