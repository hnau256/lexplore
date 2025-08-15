-dontwarn org.slf4j.impl.StaticLoggerBinder
-keep class com.google.errorprone.annotations.CanIgnoreReturnValue {
    *;
}

-keep class androidx.compose.runtime.internal.** { *; }
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidResourcesKt { *; }
-keep class androidx.compose.ui.res.** { *; }
-keep class android.content.res.** { *; }
-keep class android.util.** { *; }

-dontwarn androidx.test.platform.app.InstrumentationRegistry
-dontwarn ch.qos.logback.classic.Level
-dontwarn ch.qos.logback.classic.Logger
-dontwarn ch.qos.logback.classic.spi.ILoggingEvent
-dontwarn ch.qos.logback.classic.spi.LogbackServiceProvider
-dontwarn ch.qos.logback.classic.spi.LoggingEvent