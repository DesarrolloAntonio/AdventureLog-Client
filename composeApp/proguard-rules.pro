# R8 rules for the release build.
#
# Most libraries here ship their own consumer rules; what follows covers the cases those cannot
# know about - chiefly anything reached by reflection or by generated code whose entry point R8
# cannot see from the call graph.

# --- kotlinx.serialization -----------------------------------------------------------------
# Serializers are generated as nested $$serializer classes and looked up by name. R8 sees no
# reference to them from application code, so without this it strips every one of them and the
# app fails at the first API response.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations, AnnotationDefault
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class *
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-dontnote kotlinx.serialization.**

# --- Ktor ----------------------------------------------------------------------------------
# The client resolves engines through a ServiceLoader; the ones we do not bundle would otherwise
# be reported as missing.
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-keepnames class io.ktor.client.engine.** { *; }

# --- Koin ----------------------------------------------------------------------------------
# Resolution happens through reified generics at the call site, so nothing needs keeping by name.
-dontwarn org.koin.**

# --- Coroutines ----------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# --- Compose -------------------------------------------------------------------------------
-dontwarn androidx.compose.**

# --- Our own models ------------------------------------------------------------------------
# Everything crossing the network boundary, kept whole rather than trusting the annotation rules
# above to catch every shape (sealed hierarchies and generic wrappers are the awkward ones).
-keep class com.desarrollodroide.adventurelog.core.model.** { *; }
-keep class com.desarrollodroide.adventurelog.core.network.model.** { *; }
