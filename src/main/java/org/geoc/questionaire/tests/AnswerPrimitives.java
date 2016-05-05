package org.geoc.questionaire.tests;

import java.util.List;

/**
 * Created by lrodr_000 on 04/05/2016.
 */
public interface AnswerPrimitives {

    Object getValue();

    List<Object> getValues();

    Object getValue(int index);

    int count();


}
