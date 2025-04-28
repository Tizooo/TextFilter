package me.tizo.textfilter.modules.badwords;

public class Flow {

    public static Boolean badwords (String text) {

        String normalized = NormalizeText.normalize(text);

        Boolean hasBadWords = ExpandedRegexCheck.regexCheck(normalized);

        return hasBadWords;

    }
}