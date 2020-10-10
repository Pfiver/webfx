package webfx.platform.teavm.services.worker.spi.impl;

import org.teavm.interop.Import;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.MessageEvent;
import webfx.platform.shared.services.worker.spi.abstrimpl.RunningWorker;
import webfx.platform.shared.services.worker.spi.abstrimpl.JavaApplicationWorker;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public final class TeaVmRunningWorker implements RunningWorker {

    public static void executeJavaCodedWorker(JavaApplicationWorker javaApplicationWorker) {
        javaApplicationWorker.setPlatformWorker(new TeaVmRunningWorker());
        javaApplicationWorker.onLoaded();
    }

    @Override
    public void log(String message) {
        native_log(message);
    }

    @Override
    public void log(Object object) {
        native_log((JSObject) object);
    }

    @Override
    public void postMessage(Object msg) {
        if (msg instanceof String)
            native_postMessage((String) msg);
        else
            native_postMessage((JSObject) msg);
    }

    @Override
    public Object toNativeJsonArray(double[] doubleArray) {
        int n = doubleArray.length;
        JSArray jsArray = JSArray.create();
        for (int i = 0; i < n; ++i) {
            native_setDoubleArray(jsArray, i, doubleArray[i]);
        }
        return jsArray;
    }

    @Override
    public Object toNativeJsonArray(int[] intArray) {
        int n = intArray.length;
        JSArray jsArray = JSArray.create();
        for (int i = 0; i < n; ++i) {
            native_setDoubleArray(jsArray, i, intArray[i]);
        }
        return jsArray;
    }

    @Override
    public int getJsonInt(Object nativeObject, String key) {
        return (int) getJsonDouble(nativeObject, key);
    }

    @Override
    public double getJsonDouble(Object nativeObject, String key) {
        return js2Double(getJSValue((JSObject) nativeObject, key));
    }

    @JSBody(params = {"jso", "key"}, script = "return jso[key] || null;")
    static native JSObject getJSValue(JSObject jso, String key);

    static double js2Double(JSObject jsv) {
        return jsv == null || isUndefined(jsv) ? 0 : ((JSNumber) jsv).doubleValue();
    }

    @JSBody(params = "object", script = "return typeof object === 'undefined';")
    static native boolean isUndefined(JSObject object);

    @JSBody(params={"jsArray", "index", "value"}, script="jsArray[index] = value")
    @Import(name = "setDoubleArray")
    private static native void native_setDoubleArray(JSArray jsArray, int index, double value);

    @JSBody(params={"jsArray", "index", "value"}, script="jsArray[index] = value")
    @Import(name = "setIntArray")
    private static native void native_setIntArray(JSArray jsArray, int index, int value);

    @Override
    public void setOnMessageHandler(Consumer<Object> onMessageHandler) {
        native_setOnMessageHandler(evt -> onMessageHandler.accept(evt.getData()));
    }

    @Override
    public void terminate() {
    }

    @JSBody(params={"handler"}, script="self.onmessage=handler")
    @Import(name = "setOnMessageHandler")
    public static native void native_setOnMessageHandler(EventListener<MessageEvent> handler);

    @JSBody(params={"msg"}, script="self.postMessage(msg)")
    @Import(name = "postMessageObject")
    private static native void native_postMessage(JSObject message);

    @JSBody(params={"msg","param"}, script="self.postMessage(msg,param)")
    @Import(name = "postMessageObjectWithParam")
    private static native void native_postMessage(JSObject message, JSObject param);

    @JSBody(params={"msg"}, script="self.postMessage(msg)")
    @Import(name = "postMessageString")
    private static native void native_postMessage(String message);

    @JSBody(params={"msg"}, script="console.log(msg)")
    @Import(name = "logString")
    private static native void native_log(String msg);

    @JSBody(params={"js"}, script="console.log(js)")
    @Import(name = "logJs")
    private static native void native_log(JSObject js);

}
