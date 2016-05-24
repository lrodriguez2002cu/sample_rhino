package org.geoc.questionaire.tests;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

import java.util.List;

/**
 * Created by lrodr_000 on 04/05/2016.
 */
public class AnswerScriptableWrapper extends ScriptableObject implements AnswerPrimitives {

    private static final long serialVersionUID = 438270592527335642L;
    private AnswerPrimitives answer;

    public AnswerScriptableWrapper() {
        this.answer = null;
    }

    /*@JSConstructor*/
    public AnswerScriptableWrapper(Object answer) {
        this.answer = (AnswerPrimitives) answer;
    }

    @JSGetter
    public Object getValue() {
        return answer.getValue();
    }

    @JSGetter
    public List<Object> getValues() {
        return answer.getValues();
    }

    @JSFunction
    public Object getValue(int index) {
        return answer.getValue(index);
    }

    @JSGetter
    public int count() {
        return answer.count();
    }

    public String getClassName() {
        return "AnswerWrapper";
    }
}
