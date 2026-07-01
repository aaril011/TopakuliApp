# Keep io.grpc classes for Firestore compatibility
-keep class io.grpc.** { *; }
-dontwarn io.grpc.**

# Keep Firestore internal classes
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Keep Google Auth library classes
-keep class com.google.auth.** { *; }
-dontwarn com.google.auth.**
