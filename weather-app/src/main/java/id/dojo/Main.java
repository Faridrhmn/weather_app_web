package id.dojo;

import com.google.gson.Gson;
import id.dojo.models.*;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinFreemarker;
import kong.unirest.core.Unirest;
import kong.unirest.core.json.JSONArray;
import kong.unirest.core.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Main {
    static String apiKey = "03ff3627306c97ba67d1cc4eeba9b1e9";
    static String apiKey2 = "eae474142e194481807115104241005";
    static Gson gson;
    public static void main(String[] args) {
        gson = new Gson();
        Javalin app = Javalin.create(cfg -> {
            cfg.fileRenderer(new JavalinFreemarker());
            cfg.staticFiles.add(staticFile -> {
               staticFile.hostedPath ="/";
               staticFile.directory = "/public";
               staticFile.location = Location.CLASSPATH;
            });
        });

        app.get("/", ctx-> {
                    ctx.render("views/weatherView.html");
                });

        app.get("/api/weather_data", ctx -> {
            MetaData metaData = new MetaData(200, "Success");
            String lat = ctx.queryParam("lat");
            String lon = ctx.queryParam("lon");
            String urlNow = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=id";
            WeatherStatus weatherStatus = getTempHeader(urlNow);
            WeatherCondition weatherCondition = getConditionHeader(urlNow);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("temperature", weatherStatus);
            responseData.put("condition", weatherCondition);
            MyResponse myResponse = new MyResponse(metaData, responseData);
            ctx.json(myResponse);
        });

        app.get("/api/polution_data", ctx -> {
            MetaData metaData = new MetaData(200,"Yes");
            String lat = ctx.queryParam("lat");
            String lon = ctx.queryParam("lon");
            String urlNow = "http://api.openweathermap.org/data/2.5/air_pollution?lat="+lat+"&lon="+lon+"&appid="+apiKey;
            WeatherPolution weatherPolution = getWeatherPolution(urlNow);
            MyResponse myResponse = new MyResponse(metaData, weatherPolution);
            ctx.json(myResponse);
        });

        app.get("/api/forecast_data", ctx -> {
            MetaData metaData = new MetaData(200,"Yes");
            String lat = ctx.queryParam("lat");
            String lon = ctx.queryParam("lon");
            String urlNow = "https://api.weatherapi.com/v1/forecast.json?key="+apiKey2+"&q="+lat+","+lon+"&days=5";
            WeatherForecast weatherForecast = getForcast(urlNow);
            MyResponse myResponse = new MyResponse(metaData, weatherForecast);
            ctx.json(myResponse);
        });

        app.get("/api/golden_time", ctx -> {
            MetaData metaData = new MetaData(200,"Yes");
            String lat = ctx.queryParam("lat");
            String lon = ctx.queryParam("lon");
            String urlNow = "https://api.weatherapi.com/v1/astronomy.json?key="+apiKey2+"&q="+lat+","+lon;
            WeatherTime weatherTime = getTime(urlNow);
            MyResponse myResponse = new MyResponse(metaData, weatherTime);
            ctx.json(myResponse);
        });

        app.get("/api/weather_header", ctx -> {
            MetaData metaData = new MetaData(200,"Yes");
            String lat = ctx.queryParam("lat");
            String lon = ctx.queryParam("lon");
            String urlNow = "https://api.weatherapi.com/v1/current.json?key="+apiKey2+"&q="+lat+","+lon;
            WeatherHeader weatherHeader = getHeaderWeather(urlNow);
            MyResponse myResponse = new MyResponse(metaData, weatherHeader);
            ctx.json(myResponse);
        });

//        app.get("api/waktu", ctx->
//                ctx.result(getHeaderWeather().toString()));
//        app.get("/testing", ctx -> {
//            cobaAja = ctx.queryParam("pilihan");
//            ctx.result(cobaAja);
//        });
        app.start(1234);

    }


    public static WeatherStatus getTempHeader(String urlCurrent) {
        gson = new Gson();
        JSONObject main = Unirest.get(urlCurrent)
                .asJson()
                .getBody()
                .getObject()
                .getJSONObject("main");

        String mainData = main.toString();
        return gson.fromJson(mainData, WeatherStatus.class);
    }

    public static WeatherCondition getConditionHeader(String urlCurrent){
        gson = new Gson();
        JSONObject response2 = Unirest.get(urlCurrent).asJson()
                .getBody().getObject();
        JSONObject result = response2.getJSONArray("weather").getJSONObject(0);
        String cuaca = result.toString();
        return gson.fromJson(cuaca, WeatherCondition.class);
    }

    public static WeatherPolution getWeatherPolution(String urlCurrent){
        gson = new Gson();
        JSONObject result = Unirest.get(urlCurrent)
                .asJson().getBody()
                .getObject()
                .getJSONArray("list")
                .getJSONObject(0)
                .getJSONObject("main");
        String polusi = result.toString();
        return gson.fromJson(polusi, WeatherPolution.class);
    }

    public static WeatherTime getTime(String urlCurrent){
        gson = new Gson();
        JSONObject result = Unirest.get(urlCurrent)
                .asJson().getBody()
                .getObject()
                .getJSONObject("astronomy")
                .getJSONObject("astro");
        String waktu = result.toString();
        return gson.fromJson(waktu, WeatherTime.class);
    }

    public static WeatherForecast getForcast(String urlCurrent){
        gson = new Gson();
        double jumlah = 0.0, hasil = 0.0;
        JSONArray shortResponse2 = Unirest.get(urlCurrent)
                .asJson().getBody()
                .getObject().getJSONObject("forecast")
                .getJSONArray("forecastday");
        for(int i = 0; i< 5; i++){
            int day = shortResponse2.getJSONObject(i).getJSONObject("day").getInt("daily_chance_of_rain");;
            jumlah += day;
        }
        hasil = jumlah / 5;
        String akhir = String.valueOf(hasil);
        JSONObject forecastJson = new JSONObject();
        forecastJson.put("rataHujan", akhir);
        String rataRata = forecastJson.toString();
        return gson.fromJson(rataRata, WeatherForecast.class);
    }

    public static WeatherHeader getHeaderWeather(String urlCurrent){
        gson = new Gson();
        double latitude = -7.797068;
        double longitude = 110.370529;
        String urlNow = "https://api.weatherapi.com/v1/current.json?key="+apiKey2+"&q="+latitude+","+longitude;
        JSONObject location = Unirest.get(urlCurrent).asJson().getBody().getObject()
                .getJSONObject("location");
        String name = location.getString("name");
        String time = location.getString("localtime");
        JSONObject current = Unirest.get(urlCurrent).asJson().getBody().getObject()
                .getJSONObject("current");
        Double temp = current.getDouble("temp_c");
        JSONObject icon = Unirest.get(urlCurrent).asJson().getBody().getObject()
                .getJSONObject("current").getJSONObject("condition");
        String cuaca = icon.getString("text");
        String image = icon.getString("icon");
        JSONObject WeatherCurrent = new JSONObject();
        WeatherCurrent.put("name", name);
        WeatherCurrent.put("time", time);
        WeatherCurrent.put("temp", temp);
        WeatherCurrent.put("icon", image);
        WeatherCurrent.put("now", cuaca);
        String result = WeatherCurrent.toString();
        return gson.fromJson(result, WeatherHeader.class);
    }

}