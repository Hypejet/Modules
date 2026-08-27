/**
 * A Java library making modular programming easier.
 */
module net.hypejet.modules {
    requires transitive org.slf4j;
    requires static transitive org.jetbrains.annotations;
    requires static transitive org.jspecify;

    exports net.hypejet.modules;
    exports net.hypejet.modules.annotation;
    exports net.hypejet.modules.exception;
}