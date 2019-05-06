// Generated by WebFx

module webfx.tutorial.customcontrol.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.shared.util;

    // Exported packages
    exports webfx.tutorial.customcontrol;
    exports webfx.tutorial.customcontrol.clock;
    exports webfx.tutorial.customcontrol.clock.emul;
    exports webfx.tutorial.customcontrol.clock.events;
    exports webfx.tutorial.customcontrol.clock.skins;
    exports webfx.tutorial.customcontrol.clock.tools;

    // Provided services
    provides javafx.application.Application with webfx.tutorial.customcontrol.CustomControlApplication;

}