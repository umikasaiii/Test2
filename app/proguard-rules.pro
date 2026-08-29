# Keep model classes serialized to/from JSON via kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class com.glasslauncher.app.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.glasslauncher.app.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
