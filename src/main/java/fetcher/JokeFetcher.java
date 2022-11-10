package fetcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dtos.JokeDTO;
import utils.HttpUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;


public class JokeFetcher {
//    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();



    public static List<JokeDTO> get() {

        List<JokeDTO> jokeList = new ArrayList<>();

//        String chuck = HttpUtils.fetchData("https://api.chucknorris.io/jokes/random");
//        String dad = HttpUtils.fetchData("https://icanhazdadjoke.com");
//        HashMap<String, String> urls = new HashMap<>();
        List<String> urls = new ArrayList<>();
        urls.add("https://icanhazdadjoke.com");
        urls.add("https://api.chucknorris.io/jokes/random");
        urls.add("https://v2.jokeapi.dev/joke/Programming?blacklistFlags=nsfw,religious,political,racist,sexist,explicit&type=single");
        urls.add("https://v2.jokeapi.dev/joke/Dark?blacklistFlags=nsfw,religious,political,racist,sexist,explicit&type=single");

        List<String> name = new ArrayList<>();
        name.add("Dad");
        name.add("Chuck Norris");
        name.add("Programming");
        name.add("Dark");

        List<String> nameOfValue = new ArrayList<>();
        nameOfValue.add("joke");
        nameOfValue.add("value");
        nameOfValue.add("joke");
        nameOfValue.add("joke");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<JokeDTO>> futures = new ArrayList<>();

        for(int i = 0; i < urls.size(); i++){
            Callable<JokeDTO> navn = new CallableJoke(urls.get(i), name.get(i), nameOfValue.get(i));
            Future future = executor.submit(navn);
            futures.add(future);
        }
        executor.shutdown();

//        List<JokeDTO> output = new ArrayList<>();
            for (Future<JokeDTO> fjd : futures) {
                try {
                    jokeList.add(fjd.get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

        return jokeList;

//        JsonObject jobj = new Gson().fromJson(dad, JsonObject.class);
////        JsonObject jobj2 = new Gson().fromJson(chuck, JsonObject.class);
//
//        JokeDTO jdto = new JokeDTO("DadJoke", jobj.get("joke").getAsString());
//        JokeDTO jdto2 = new JokeDTO("ChuckJoke", jobj.get("value").toString());
//        jokeList.add(jdto);
//        jokeList.add(jdto);
////        jokeList.add(jdto2);
//
//        System.out.println(GSON.toJson(jokeList));
//
//        System.out.println(jdto);

//        String myJSONString = "{'test': '100.00'}";
//        JsonObject jobj = new Gson().fromJson(myJSONString, JsonObject.class);
//
//        String result = jobj.get("test").toString();
//
//        System.out.println(result);


//        System.out.println("JSON fetched from chucknorris:");
//        // Joke entity
//        // Returner liste Joke dtoer
//        // JokeDTO [Navnidgenrekategoritype: DadJoke, Joken: Haha]
//        System.out.println(chuck);
//        System.out.println("JSON fetched from dadjokes:");
//        System.out.println(dad);
        
       
    }
}
