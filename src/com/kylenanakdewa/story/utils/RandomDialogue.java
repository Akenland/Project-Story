package com.kylenanakdewa.story.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * Some random dialogue for NPCs. Mostly used for automatically-generated objectives and quests.
 * @author Kyle Nanakdewa
 */
public class RandomDialogue {

    /** Dialogue in the format "I need you to..." or "You should..." */
    public final static Collection<String> objectiveStarters = Arrays.asList("I need you to ", "You should ", "You need to ", "Could you ", "I'd like you to ", "You could ");


    /** Gets a random line from a string collection. */
    public static String getRandomLine(Collection<String> collection){
        Iterator<String> iterator = collection.iterator();
        for(int i=0; i < new Random().nextInt(collection.size()); i++) iterator.next();
        return iterator.next();
    }

    /** Makes the first letter of a string lowercase. */
    public static String lowerFirstLetter(String string){
        if(string.isEmpty() || string.startsWith("I ") || string.startsWith("I'")) return string;
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }
}