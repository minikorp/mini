# Mini
Mini is a minimal Kotlin and Flux architecture with a mix of useful features.

### Introduction
TODO
## How to Use
### Actions
Actions are helpers that pass data to the Dispatcher. They represent use cases of our application and are the start point of any change made during the application lifetime.

```kotlin
data class LoginAction(val username: String,
                       val password: String) : Action
```

### Dispatcher
The dispatcher receives these Actions and broadcast payloads to registered callbacks. The instance of the Dispatcher must be unique across the whole application and it will execute all the logic in the main thread making all the code synchronous.   

```kotlin
//Dispatch the action in the current Thread
dispatcher.dispatch(LoginAction(username = "user", password = "123"))

//Dispatch the action in the UI Thread
dispatcher.dispatchOnUi(LoginAction(username = "user", password = "123"))

//Post and event that will dispatch the action on the Ui thread 
//and block until the dispatch is complete.
dispatcher.dispatchOnUiSync(LoginAction(username = "user", password = "123"))
```

### Store
The Stores act as containers for application state & logic. The real work in the application is done in the Stores. The Stores registered to listen in on the actions of the Dispatcher will do accordingly and update the Views.

The Stores represent the application though States. An State is a group of information that represent all the application needed information for the logic that this store is taking care of.

Finally, Stores are subscribed to different actions expecting to change the application state when this action is dispatched. Mini manages this using the `@Reducer` annotation. Which should be used together with a **non-private function that receives an Action as parameter**. It can also receive a state for unitary testing purposes. 
```kotlin
data class SessionState(val loginTask : Task = taskIdle(),
                        val loggedUsername : String? = null)
                        

class SessionStore : Store<SessionState>() {

    @Reducer
    fun login(action: LoginAction): SessionState {
        return state.copy(loginTask = taskRunning())
    }

    @Reducer
    fun loginComplete(action: LoginCompleteAction, state: SessionState): WarcraftState {
        return state.copy(loginTask = taskSuccess(), loggedUsername = action.name)
    }
}
```

### View changes
Each ``Store`` exposes a custom `StoreCallback` though the method `observe` or a `Flowable` if you wanna make use of RxJava. Both of them emits changes produced on their states, allowing the view to listen reactive the state changes. Being able to update the UI according to the new `Store` state.

```kotlin
  //Using RxJava  
  userStore
          .flowable()
          .map { it.name }
          .subscribe { updateUserName(it) }
          
  // Custom callback      
  userStore
          .observe { state -> updateUserName(state.name) }
```  

If you make use of the RxJava methods, you can make use of the `SubscriptionTracker` interface to keep track of the `Disposables` used on your activities and fragments.

### Tasks
A Task is a basic object to represent an ongoing process. They should be used in the state of our `Store` to represent ongoing processes that must be represented in the UI.
Having the next code:

```kotlin

data class LoginAction(val username: String, val password: String)
data class LoginCompleteAction(val loginTask: Task,
                               val user: User?)

data class SessionState(val loginTask: Task = taskIdle(),
                        val loggedUser: User? = null)

class SessionStore @Inject constructor(val controller: SessionController) : Store<SessionState>() {
    @Reducer
    fun login(action: LoginAction): SessionState {
        controller.login(action.username, action.password)
        return state.copy(loginTask = taskRunning(), loggedUser = null)
    }

    @Reducer
    fun loginComplete(action: LoginCompleteAction): SessionState {
        return state.copy(loginTask = action.loginTask, loggedUser = action.user)
    }
}
```
The workflow will be:

- View dispatch `LoginAction`.
- Store changes his `LoginTask` status to running and call though his SessionController which will do all the async work to log in the given user.
- View shows an Spinner when `LoginTask` is in running state.
- The async call ends and `LoginCompleteAction` is dispatched on UI, sending a null `User` and an error state `Task` if the async work failed or a success `Task` and an `User`.
- The Store changes his state to the given values from `LoginCompleteAction`.
- The View redirect to the HomeActivity if the task was success or shows an error if not.

### Rx Utils
Mini includes some utility extensions over RxJava 2.0 to make easier listen state changes over the `Stores`.

- `mapNotNull`: Will emit only not null values over the given `map` clause.
- `select`: Like `mapNotNull` but avoiding repeated values.
- `onNextTerminalState`: Used to map a `Task` inside an state and listen the next terminal state(Success - Error). Executing a different closure depending of the result of the task.

### Navigation
To avoid loops over when working with navigation based on a process result. You will need to make use of `onNextTerminalState` after dispatch and `Action` that starts a process which result could navigate to a different screen.
For example:
```kotlin
  fun login(username: String, password: String) {
        dispatcher.dispatch(LoginAction(username, password))
        sessionStore.flowable()
                .onNextTerminalState(taskMapFn = { it.loginTask },
                        successFn = { navigateToLogin() },
                        failureFn = { showError(it) })
    }
```

If we continually listen the changes of a `Task` and we navigate to a specific screen when the `Task` becomes successful. The state will stay on SUCCESS and if we navigate back to the last screen we will be redirected again.

### Logging
Mini includes a custom `LoggerInterceptor` to log any change in your `Store` states produced from an `Action`. This will allow you to keep track of your actions, changes and side-effects more easily. 
To add the LoggerInterceptor to your application you just need to add a single instance of it to your `Dispatcher` after initialize it in your `Application` class or dependency injection code.
```kotlin
val loggerInterceptor = CustomLoggerInterceptor(stores().values)
dispatcher.addInterceptor(loggerInterceptor)
```

## Testing with Mini
Mini includes an extra library called mini-android-testing with a few methods and `Expresso TestRules` to simplify your UI tests over this architecture.

- `TestDispatcherRule` : This rule will intercept any action that arrives to the Dispatcher, avoiding any call to the Store and their controllers. If we include this rule we will need to change the states manually in our tests.
- `CleanStateRule` : It just reset the state of your stores before and after each test.

Example of test checking that an action is correctly dispatched:

```kotlin
@get:Rule
val testDispatcher = testDispatcherRule()

@Test
fun login_button_dispatch_login_action() {
    onView(withId(R.id.username_edit_text)).perform(typeText("someUsername"))
    onView(withId(R.id.password_edit_text)).perform(typeText("somePassword"))
    onView(withId(R.id.login_button)).perform(click())
    
    assertThat(testDispatcher.actions, contains(LoginAction(someUsername, somePassword)))
}
```

Example of test checking that a view correctly changes with an specify state:

```kotlin
@get:Rule
val cleanState = cleanStateRule()

@Test
fun login_redirects_to_home_with_success_task() {
     //Set login state to success
     onUiSync {
         val loggedUser = User(email = MockModels.anyEmail, uid = MockModels.anyId, username = MockModels.anyUsername, photoUrl = MockModels.anyPhoto)
         val state = SessionState().copy(loginRequestState = requestSuccess(), verified = false, loggedIn = true, loggedUser = loggedUser)
         sessionStore.setTestState(state)
     }
     //Redirect to Email verification activity
     intended(hasComponent(HomeActivity::class.java.name))
}
```

## Setting Up

### Import the library
To setup Mini in your application, first you will need to add the library itself together with the annotation processor:
```groovy
implementation 'com.github.pabloogc:Mini:1.0.5'
annotationProcessor 'com.github.pabloogc.Mini:mini-processor:1.0.5'
androidTestImplementation 'com.github.pabloogc.Mini:mini-android-testing:1.0.5' //Optional
```

### Setting up your App file
After setting it up on your gradle application. You will need to initialize a `Dispatcher` unique instance in your application together with a list of `Stores`. To achieve this you can use your favorite dependency injection framework.

With your Dispatcher and Stores ready, you will need to add the `MiniActionReducer` which is auto-generated depending of your `Stores` and `Reducers`. 

Finally, if you want to add *action-state* changes logging to your application you can add the `LoggerInterceptor` provided by the library or create your own one.  
```kotlin
val actionReducer = MiniActionReducer(stores = stores())
val loggerInterceptor = CustomLoggerInterceptor(stores().values)

dispatcher.addActionReducer(actionReducer)
dispatcher.addInterceptor(loggerInterceptor) //Optional

FluxUtil.initStores(stores())
```