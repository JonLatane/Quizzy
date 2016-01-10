package com.jonlatane.quizzy.io;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by jonlatane on 11/24/15.
 */
public class GsonUtils {
    public static Gson getGsonWrapper() {
        return
        Converters.registerDateTime(
                new GsonBuilder().serializeNulls()
                        )
                        .create()
                ;
    }

    public static Gson getPrettyPrintWrapper() {
        return
                Converters.registerDateTime(
                        new GsonBuilder().serializeNulls().setPrettyPrinting()
                )
                        .create()
                ;
    }
}
