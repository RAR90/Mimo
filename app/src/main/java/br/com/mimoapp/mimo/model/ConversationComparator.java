package br.com.mimoapp.mimo.model;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Created by rafael on 14/12/17.
 */

public class ConversationComparator implements Comparator<Conversation> {
    @Override
    public int compare(Conversation o1, Conversation o2) {
        return (int) (o1.getSort() - o2.getSort());
    }
}