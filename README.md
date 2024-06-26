# Weather Application
* Android application that accesses the API and shows the weather based on selected data.

#### Built with Kotlin
## Description
* Displays weather forecast three days ahead.
* Weather forecast is updated every 15 minutes.
* When entering the application, it is checked whether GPS is enabled and the appropriate permission is requested.
* The user can update the weather by clicking on the corresponding button.
* It is possible to enter the name of the city to view the weather forecast.

#### Main screen is divided into hourly and daily
![main_screen](https://github.com/IPseudoNemoI/weather-app/assets/123749636/d5b3ef6d-81d8-4f5a-a61c-adbc6742f0cb)

#### To select another city, click on the corresponding button and enter the name
![show_search](https://github.com/IPseudoNemoI/weather-app/assets/123749636/f0d9d82a-5aef-45a0-8983-6955e16b6ba0)

#### To return to the city by location, click on the corresponding button
![show_refresh](https://github.com/IPseudoNemoI/weather-app/assets/123749636/d2205142-8e38-4eb5-b1ee-77096ad0ffd4)

#### If your GPS is turned off, a window will appear with the option to turn it on
![show_gps_request](https://github.com/IPseudoNemoI/weather-app/assets/123749636/ee3a73ab-5562-485b-9833-401394c04585)

## Built With
* Kotlin
* Retrofit2 + OkHttp3
* Picasso

## Installing
* Type this in Git Bash:
```
$ git clone https://github.com/IPseudoNemoI/weather-app.git
```
* Get your API key from [https://www.weatherapi.com](https://www.weatherapi.com)
* Go for: app/src/main/java/com/example/weatherapp/fragments/MainFragment.kt and change
```kotlin
const val API_KEY = "YOUR API KEY"
```
## Authors
Andrey Bashkov </br>
[@GitHub](https://github.com/IPseudoNemoI) </br>
[@Telegram](https://t.me/ipseudonemoi) 