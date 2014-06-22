package com.anysoftkeyboard.dictionaries;

import android.util.Log;

import com.anysoftkeyboard.WordComposer;

import java.util.ArrayList;
import java.util.List;

import de.typology.predict.PredictionProvider;
import de.typology.predict.model.Prediction;
import de.typology.predict.model.PredictionContext;

/**
 * Created by till on 6/22/14.
 */
public class DictionaryPredictionProvider implements PredictionProvider {

    private static final String TAG = "DictionaryPredictionProvider";

    private static final int MAX_SUGGESTION_AMOUNT = 5;

    private final Dictionary mDictionary;

    public DictionaryPredictionProvider(final Dictionary dict) {
        mDictionary = dict;
    }

    @Override
    public void start() {
        if (mDictionary == null)
            return;
        try {
            DictionaryASyncLoader loader = new DictionaryASyncLoader(null);
            loader.execute(mDictionary);
        } catch (Exception e) {
            Log.e(TAG, "error loading dictionary: " + e);
        }
    }

    @Override
    public void stop() {
        if (mDictionary != null)
            mDictionary.close();
    }

    @Override
    public List<Prediction> getPredictions(final PredictionContext context) {
        final List<Prediction> predictions = new ArrayList<>(MAX_SUGGESTION_AMOUNT);
        predictions.add(new Prediction(context.getWordAt(context.getNumberOfWords() - 1), 128));

        if (mDictionary == null)
            return predictions;

        final Dictionary.WordCallback callback = new Dictionary.WordCallback() {
            @Override
            public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
                final Prediction prediction = createPrediction(word, wordOffset, wordLength, frequency);
                predictions.add(prediction);
                return predictions.size() < MAX_SUGGESTION_AMOUNT;
            }
        };
        mDictionary.getWords(createWordComposer(context), callback);

        return predictions;
    }

    private static WordComposer createWordComposer(final PredictionContext context) {
        final WordComposer composer = new WordComposer();
        final String curWord = context.getWordAt(context.getNumberOfWords() - 1);
        for (char c : curWord.toCharArray()) {
            composer.add(c, new int[]{c});
        }
        return composer;
    }

    private static Prediction createPrediction(char[] word, int wordOffset, int wordLength, int frequency) {
        final StringBuilder builder = new StringBuilder();
        final int wordEnd = wordOffset + wordLength;
        for (int i = wordOffset; i < wordEnd; i++) {
            builder.append(word[i]);
        }
        return new Prediction(builder.toString(), frequency);
    }
}
