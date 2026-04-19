# kotlinx.serialization: @Serializable クラスと生成された $$serializer を保持する。
# Navigation Compose の type-safe route がリフレクションで serializer を探すため、
# これらが R8 に削られると画面遷移で SerializationException になる。
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class net.meshpeak.mytodo.**$$serializer { *; }
-keepclassmembers class net.meshpeak.mytodo.** {
    *** Companion;
}
-keepclasseswithmembers class net.meshpeak.mytodo.** {
    kotlinx.serialization.KSerializer serializer(...);
}
