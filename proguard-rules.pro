
-keep class lin.xposed.**{*;}

-keep class HookItem.**{*;}


#保留类与成员名称
-keepclasseswithmembernames class * {
    native <methods>;
}

-dontobfuscate

#不警告
-dontwarn javax.**
-dontwarn java.awt.**
-dontwarn org.apache.bsf.*

-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile


# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keep class * implements java.io.Serializable { *; }

