package me.tizo.textfilter.modules.badwords;

import java.text.Normalizer;
import java.util.*;

public class NormalizeText {

    // Obvious leetspeak mappings where the substitution is very clear and unambiguous
    // Letters like 'i' and 'l' are excluded due to visual ambiguity
    private static final Map<Character, List<String>> obviousLeetMap = new HashMap<>();
    static {
        obviousLeetMap.put('a', Arrays.asList("4", "@", "/\\", "/-\\", "Д"));
        obviousLeetMap.put('b', Arrays.asList("|3", "8", "ß"));
        obviousLeetMap.put('c', Arrays.asList("¢", "©"));
        obviousLeetMap.put('d', Arrays.asList("|)"));
        obviousLeetMap.put('e', Arrays.asList("3", "€", "£"));
        obviousLeetMap.put('f', Arrays.asList("ƒ"));
        obviousLeetMap.put('g', Arrays.asList("6"));
        obviousLeetMap.put('h', Arrays.asList("|-|"));
        obviousLeetMap.put('j', Arrays.asList("_|"));
        obviousLeetMap.put('k', Arrays.asList("|<"));
        obviousLeetMap.put('l', Arrays.asList("|_"));
        obviousLeetMap.put('m', Arrays.asList("|\\/|", "/V\\"));
        obviousLeetMap.put('n', Arrays.asList("|\\|", "/V", "ท", "И"));
        obviousLeetMap.put('o', Arrays.asList("0", "Ø"));
        obviousLeetMap.put('p', Arrays.asList("|*", "|°", "|^", "⁋", "℗"));
        obviousLeetMap.put('q', Arrays.asList("¶"));
        obviousLeetMap.put('r', Arrays.asList("|`", "®", "Я"));
        obviousLeetMap.put('s', Arrays.asList("5", "§"));
        obviousLeetMap.put('t', Arrays.asList("7", "†"));
        obviousLeetMap.put('u', Arrays.asList("|_|", "(_)", "บ"));
        obviousLeetMap.put('v', Arrays.asList("\\/"));
        obviousLeetMap.put('w', Arrays.asList("\\/\\/", "vv", "\\V/", "พ", "₩", "ω"));
        obviousLeetMap.put('x', Arrays.asList("><", "}{", "×"));
        obviousLeetMap.put('y', Arrays.asList("`/", "¥"));
        obviousLeetMap.put('z', Arrays.asList("2"));
    }

    public static String normalize(String text) {

        // Normalize the input using Unicode Normalization
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFKD);

        normalizedText = normalizedText.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Replace obvious leetspeak variants with their corresponding letters
        for (Map.Entry<Character, List<String>> entry : obviousLeetMap.entrySet()) {
            for (String variant : entry.getValue()) {
                normalizedText = normalizedText.replace(variant, entry.getKey().toString());
            }
        }

        // Convert the cleaned text to lowercase and return
        return normalizedText.toLowerCase();
    }
}
