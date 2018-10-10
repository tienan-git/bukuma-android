# Bukuma Android

Bukuma android originally develop by Nanameue Inc. This project is written in **Kotlin**
and support Android from api level 16 to 25+.

## Build

To build the project you need to have the
following programs & plugin installed in you machine.

- Android studio 2.2+
- Kotlin plugin 1.0.5+

## Project

Package name is `jp.com.labit.bukuma` and the name is `ブクマ！`
The following diagram illustrate the structure of project under the app package.

```
jp.com.labit.bukuma
|-- analytic // tracking event
|-- api
    |-- intercepter // interceptors that run before or after networking calls
    |-- resource // restful api resource
    |-- response // response model
    |-- ...
|-- database // migration file
|-- extension
|-- fcm // firebase push notification
|-- injection // dagger2 component and modules
|-- manager
|-- model
    |-- realm // model that going to be saved locally
    |-- ... // other temporary model
|-- ui
    |-- activity
    |-- adapter
    |-- custom // custom views and components
    |-- dialog
    |-- fragment
    |-- viewholder
|-- util
|-- BukumaApplication.kt
|-- BukumaConfig.kt // constants and configurations
|-- BukumaError.kt
|-- BukumaLifeCycle.kt
|-- BukumaPreference.kt

```

### Analytics

`BukumaAnalytic.kt` manager all tracking events. Currently we send events to **AppFlyer** and **Fabric**.
Further services should be added here.

### API

All APIs that Bukuma connect are listed in `BukumaApi.kt` interface. Custom logic before or after calling api
is implemented in `BukumaService.kt` or `api/resource` for specific restful resource.

Bukuma also use **Omise** for credit card and payment. Please refer to `OmiseApi.kt` for api.

#### Authentication

All requests that come out of the app will be intercepted by `api/interceptor/AccessTokenInterceptor.kt`
to add the User's access token into Http header before sending it to server.

### Database

This project use **Realm** as the persistent storage. All models those would be saved locally are in
`model/realm`. If you need to update one of their properties, you have to update migration file `database/Migration.kt`.
Please refer to Realm website for how to do it.

**Please not that `User.kt` model can exist only one object in the database as the current logged in user**

### UI

All UI related classes are separated by its type. The most important classes are

- `activity/BaseActivity.kt` inject important dependencies and force the app orientation to portrait.
- `fragment/BaseFragment.kt` inject important dependencies
- `fragment/BaseListFragment.kt` logic for displaying the list of data

## Third party Libraries

The following are important libraries. To see all please go to `app/build.gradle`

- **Dagger2** dependency injection
- **RxJava** Reactive Extension
- **Picasso** Image loading and caching
- **Retrofit2** networking layer
- **Realm** persistent layer

## Proguard

Code obfuscating is enable only when build with `release` config. Please make sure you test in both
`debug` and `release` before production release.

## Happy Coding !! <3