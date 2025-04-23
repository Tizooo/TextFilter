package me.tizo.textfilter.modules.badwords;

public class Flow {

    public static String badwords (String text) {

        String normalized = NormalizeText.normalize(text);

        System.out.println("output: " + normalized);

        return normalized;

    }
}