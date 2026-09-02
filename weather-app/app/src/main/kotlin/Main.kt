package org.example
//Import all dependencies which we need for project
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Make data classes for parsing and set ignore conditions
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherResponse (
    val forecast: Forecast
)
// Join our class Forecast with JSON Respond from API
@JsonIgnoreProperties(ignoreUnknown = true)
data class Forecast (
    @JsonProperty("forecastday")
    val forecastDay: List<ForecastDay>
)
// Make a class for information about day from API
@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastDay (
    val date: String,
    val day: DayData,
    val hour: List<HourData> = emptyList()
)
// Make a class for forecast values and link JSON responds in our values 
@JsonIgnoreProperties(ignoreUnknown = true)
data class DayData (
    @JsonProperty("mintemp_c") val minTempC: Double,
    @JsonProperty("maxtemp_c") val maxTempC: Double,
    @JsonProperty("avghumidity") val humidity: Double,
    @JsonProperty("maxwind_kph") val maxWindKph: Double
)
// Make a class for wind direction, bc information about it we can get only in hours forecast, not for all of day
@JsonIgnoreProperties(ignoreUnknown = true)
data class HourData (
    @JsonProperty("wind_dir") val windDir: String
)
// Make a general class for all information before we will print it
data class CityForecast (
    val city: String,
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Double,
    val windSpeed: Double,
    val windDir: String
)
//Set a Retrofit pattern for network query on API and pass this info in WeatherResponse
interface WeatherApiService {
    @GET("v1/forecast.json")
    suspend fun getForecast (
        @Query("key") apiKey: String,
        @Query("q") city: String,
        @Query("days") days: Int = 2
    ): WeatherResponse
}

fun main() = runBlocking {
    // Reading the API key from system or can set YOUR_WEATHER_API_KEY in code
    val apiKey = System.getenv("WEATHER_API_KEY") ?: "YOUR_WEATHER_API_KEY"
    val cities = listOf("Chisinau", "Madrid", "Kyiv", "Amsterdam")
    // Make a ObjectMapper for Jackson to make a Kotlin data
    val objectMapper = ObjectMapper().registerKotlinModule()
    // Set Retrofit HTTP-client for convert JSON info in Kotlin object
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/")
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
    // Create a quety to Weather API and save the information in val results
    val api = retrofit.create(WeatherApiService::class.java)
    val results = mutableListOf<CityForecast>()
    // Make a cycle for each city and write the info in our results list
    for (city in cities) {
        try {
            val response = api.getForecast(apiKey, city, days = 2)
            // Take a info about tomorrow forecast or return Null if API no respond
            val nextDay = response.forecast.forecastDay.getOrNull(1)
            // Check the nextDay value and keep on if all correct
            if (nextDay != null) {
                // Safe call on winDir 12:00 if it was unsuccessful set N/A
                val windDirection = nextDay.hour.getOrNull(12)?.windDir ?: "N/A"
                // Add our information from API in results list
                results.add(
                    CityForecast(
                        city = city,
                        date = nextDay.date,
                        minTemp = nextDay.day.minTempC,
                        maxTemp = nextDay.day.maxTempC,
                        humidity = nextDay.day.humidity,
                        windSpeed = nextDay.day.maxWindKph,
                        windDir = windDirection
                    )
                )
                }
            // Print error message if we can't get info from API
            } catch (e: Exception) {
                System.err.println("Sorry, we can't upload info for $city: ${e.message}")
            }
        }
    // Pass the results list in function for printing tables
    printFormattedTable(results)
}
// Configure the printFormattedTable function
fun printFormattedTable(data: List<CityForecast>) {
    // Make a check what we have data in list
    if (data.isEmpty()) {
        println("Data is empty.")
        return
    }
    // Set parameters for header of table
    val header = String.format(
        "| %-12s | %-12s | %-11s | %-11s | %-13s | %-16s | %-14s |",
        "City", "Data", "Min.T (°C)", "Max.T (°C)", "Humidity (%)", "Wind Speed (kph)", "Wind Direction"
    )
    val separator = "-".repeat(header.length)
    // Draw a header for our table
    println(separator)
    println(header)
    println(separator)
    // Use a cycle for printing the information for each city which we have in list
    for (row in data) {
        println(
            String.format(
                "| %-12s | %-12s | %-11.1f | %-11.1f | %-13.0f | %-16.1f | %-14s |",
                row.city,
                row.date,
                row.minTemp,
                row.maxTemp,
                row.humidity,
                row.windSpeed,
                row.windDir
            )
        )
    }
    println(separator)
}