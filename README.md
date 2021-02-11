Latest version: [![Release](https://jitpack.io/v/minikorp/mini.svg)](https://jitpack.io/#minikorp/mini)


# Mini
Mini is a minimal Flux architecture written in Kotlin that also adds a mix of useful features to build UIs fast.

### Purpose
You should use this library if you aim to develop a reactive application with good performance (no reflection using code-gen).
Feature development using Mini is fast compared to traditional architectures (like CLEAN or MVP), low boilerplate and state based models make feature integration and bugfixing easy as well as removing several families of problems like concurrency or view consistency across screens.

## How to Use
### Actions

Annotate action classes with `@Action` or extend `BaseAction`.

Marking a class as action will make all the action and all supertypes available for listening from `@Reducer` functions.

### Dispatcher

```kotlin
//Dispatch an action on the main thread synchronously
dispatcher.dispatch(LoginAction(username = "user", password = "123"))

//Post an event that will dispatch the action on the UI thread and return immediately.
dispatcher.dispatchAsync(LoginAction(username = "user", password = "123"))
```

### Store
The Stores are holders for application state and state mutation logic. In order to do so they expose pure reducer functions that are later invoked by the dispatcher.

The state is plain object (usually a data class) that holds all information needed to display the view. State should always be inmutable. State classes should avoid using framework elements (View, Camera, Cursor...) in order to facilitate testing.

Stores subscribe to actions to change the application state after a dispatch. Mini generates the code that links dispatcher actions and stores using the `@Reducer` annotation over a **non-private function that receives an `@Action` as parameter**.

### Generated code

Mini generates `mini.MiniGen` class at compilation time to use as factory for `Dispatcher` and automatic `@Reducer` subscription calls. MiniGen is not required to use Mini, but encouraged to reduce boilerplate.

```kotlin
val dispatcher = MiniGen.newDispatcher()
val stores = listOf(your stores...)
//Bind @Reducer functions with dispatcher.
MiniGen.subscribe(dispatcher, stores)
```

### View changes
Each ``Store`` exposes a custom `StoreCallback` though the method `subscribe` or a `Flowable` if you wanna make use of RxJava. Both of them emits changes produced on their states, allowing the view to listen reactive the state changes. Being able to update the UI according to the new `Store` state.

```kotlin
  //Using Flow
  userStore
          .flow()
          .onEach { updateUserName(it.name) }
          .launchIn(lifecycleScope)
          
  // Default callback      
  userStore
          .subscribe { state -> updateUserName(state.name) }
```  

### Logging
Mini includes a custom `LoggerInterceptor` to log any change in your `Store` states produced from an `Action`. This will allow you to keep track of your actions, changes and side-effects more easily. 

## Gradle

[![Release](https://jitpack.io/v/minikorp/mini.svg)](https://jitpack.io/#minikorp/mini)

```groovy
apply plugin: kotlin
apply plugin: kotlin-kapt

dependencies {
    def mini_version = "4.2.0" //See latest version tag at top
    implementation "com.github.minikorp.mini:mini-common:$mini_version"
    kapt "com.github.minikorp.mini:mini-processor:$mini_version"
    //Optional dependencies
    implementation "com.github.minikorp.mini:mini-rx:$mini_version" //Rx bindings
    implementation "com.github.minikorp.mini:mini-flow:$mini_version" //Flow bindings
    implementation "com.github.minikorp.mini:mini-android:$mini_version" //Android utilities
}
```

## Issues

Jetifier might crash your build without reason, 
add this line to gradle.properties to exclude the compiler or fully disable it.

```properties
android.jetifier.blacklist=mini-processor.*\\.jar #Blacklist
android.enableJetifier=false #Or disable
```

Compiling JDK >8 might fail, make sure you set compatibility to java 8
both for Android and kapt. 

```groovy
android {
    compileOptions {
        sourceCompatibility "1.8"
        targetCompatibility "1.8"
    }
}

kapt {
    javacOptions {
        option("-source", "8")
        option("-target", "8")
    }
}
```

## Build performance

Make sure to enable incremental apt and worker api for faster builds with kapt.

```properties
# Some performance improvements
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
org.gradle.daemon=true
kapt.incremental.apt=true
kapt.use.worker.api=true
``` 
