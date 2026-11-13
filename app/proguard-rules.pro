# Keep the AccessibilityService (referenced by manifest)
-keep class com.reelblocker.app.service.BlockerAccessibilityService { *; }

# Keep Firebase Remote Config model classes
-keep class com.google.firebase.remoteconfig.** { *; }

# Keep data classes used with DataStore
-keep class com.reelblocker.app.model.** { *; }

# Standard Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
