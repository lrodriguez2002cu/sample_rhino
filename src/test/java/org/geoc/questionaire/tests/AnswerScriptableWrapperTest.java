package org.geoc.questionaire.tests;

import junit.framework.TestCase;
import org.mozilla.javascript.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lrodriguez2002cu on 04/05/2016.
 */
public class AnswerScriptableWrapperTest extends TestCase {

    AnswerPrimitives valueWithThreeAnswerItems = new AnswerPrimitives() {

        List<Object> objects = new ArrayList<Object>();
        {
            objects.add("answer1");
            objects.add("answer2");
            objects.add("answer3");
        }

        public Object getValue() {
            return "One Value";
        }

        public List<Object> getValues() {

            return objects;
        }

        public Object getValue(int index) {
            return objects.get(index);
        }

        public int count() {
            return objects.size();
        }
    };

    AnswerPrimitives valueWithFourAnswerItems = new AnswerPrimitives() {

        List<Object> objects = new ArrayList<Object>();

        {
            objects.add("answer1");
            objects.add("answer2");
            objects.add("answer3");
            objects.add("answer4");
        }

        public Object getValue() {
            return "One Value";
        }

        public List<Object> getValues() {

            return objects;
        }

        public Object getValue(int index) {
            return objects.get(index);
        }

        public int count() {
            return objects.size();
        }
    };

    AnswerPrimitives valueWithIntegerValue10 = new AnswerPrimitives() {

        public Object getValue() {
            return 10;
        }

        public List<Object> getValues() {

            throw new RuntimeException("This is a single value answer");
        }

        public Object getValue(int index) {
            throw new RuntimeException("This is a single value answer, so no indexes");
        }

        public int count() {
            throw new RuntimeException("This is a single value answer, so no count()");
        }
    };


    public void testExpression() throws IllegalAccessException, InvocationTargetException, InstantiationException {

        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();
            ScriptableObject.defineClass(scope, AnswerScriptableWrapper.class);

            Scriptable testCounter = cx.newObject(scope, "AnswerWrapper",
                    new Object[]{valueWithThreeAnswerItems});

            Object count = ScriptableObject.getProperty(testCounter, "count");
            System.out.println("count = " + count);

            Object value = ScriptableObject.getProperty(testCounter, "value");
            System.out.println("value = " + value);

            Object values = ScriptableObject.getProperty(testCounter, "values");
            System.out.println("values = " + values);

        } finally {
            Context.exit();
        }
    }

    public void testRhino() {

        Object[] params = new Object[]{"Hello"};

        // Every Rhino VM begins with the enter()
        // This Context is not Android's Context
        Context rhino = Context.enter();

        String javaScriptCode = "function append(a){  return a + ' world' }";
        String functionNameInJavaScriptCode = "append";

        // Turn off optimization to make Rhino Android compatible
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();

            // Note the forth argument is 1, which means the JavaScript source has
            // been compressed to only one line using something like YUI
            rhino.evaluateString(scope, javaScriptCode, "JavaScript", 1, null);

            // Get the functionName defined in JavaScriptCode
            Object obj = scope.get(functionNameInJavaScriptCode, scope);

            if (obj instanceof Function) {
                Function jsFunction = (Function) obj;

                // Call the function with params
                Object jsResult = jsFunction.call(rhino, scope, scope, params);
                // Parse the jsResult object to a String
                String result = Context.toString(jsResult);
            }
        } finally {
            Context.exit();
        }

    }

    public void testExpressionEvaluation() throws IllegalAccessException, InvocationTargetException, InstantiationException {

        String answerVarName = "answer";

        //some expressions on the answer
        Object answer = evaluateExpression("answer.value", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(((AnswerPrimitives) valueWithThreeAnswerItems).getValue()));

        answer = evaluateExpression("answer.value.length", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(((AnswerPrimitives) valueWithThreeAnswerItems).getValue().toString().length()));

        answer = evaluateExpression("answer.values.size()", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(((AnswerPrimitives) valueWithThreeAnswerItems).getValues().size()));

        answer = evaluateExpression("answer.values.get(1).length()", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(((AnswerPrimitives) valueWithThreeAnswerItems).getValues().get(1).toString().length()));

        answer = evaluateExpression("answer.values.get(1)", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);

        Object unwrappedValue = ((NativeJavaObject) answer).unwrap();

        assertTrue((unwrappedValue.toString()).equalsIgnoreCase(((AnswerPrimitives) valueWithThreeAnswerItems).getValues().get(1).toString()));

        //some more complex expressions
        answer = evaluateExpression("answer.values.size() > 0", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(true));

        answer = evaluateExpression("answer.values.size() > 0 && answer.values.size() < 100", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(true));

        answer = evaluateExpression("answer.value.length > 0 && answer.value.length < 100", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(true));


    }

    public Object evaluateExpression(String javaScriptCode /*= "answer.value";*/, String answerVarName,
                                     AnswerPrimitives primitives) throws IllegalAccessException,
            InvocationTargetException, InstantiationException {


        // Every Rhino VM begins with the enter()
        // This Context is not Android's Context
        Context rhino = Context.enter();
        // Turn off optimization to make Rhino Android compatible
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();

            ScriptableObject.defineClass(scope, AnswerScriptableWrapper.class);

            Scriptable testCounter = rhino.newObject(scope, "AnswerWrapper",
                    new Object[]{primitives});

            scope.put(answerVarName, scope, testCounter);

            Object result = rhino.evaluateString(scope, javaScriptCode, "JavaScript", 1, null);
            System.out.println(result);

            return result;

        } finally {
            Context.exit();
        }

    }


    static class Variable{
        private String variableName;
        private AnswerPrimitives questionAnswer;

        public Variable(String variableName, AnswerPrimitives questionAnswer) {
            this.variableName = variableName;
            this.questionAnswer = questionAnswer;
        }
    }


    public void testMultipleAnswerExpressions() throws IllegalAccessException, InstantiationException, InvocationTargetException {

        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithThreeAnswerItems));
        variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("answer.value == answer1.value", variables);
        assertTrue(answer.equals(true));

    }

    public void testWithAnswerExpression() throws IllegalAccessException, InstantiationException, InvocationTargetException {

        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithIntegerValue10));

        Object answer = evaluateExpressionMultipleVariables("answer.value == 10", variables);
        assertTrue(answer.equals(true));

        answer = evaluateExpressionMultipleVariables("answer.value > 8 && answer.value < 15", variables);
        assertTrue(answer.equals(true));

        answer = evaluateExpressionMultipleVariables("answer.value > 8 && answer.value < (answer.value +1)", variables);
        assertTrue(answer.equals(true));

        try {
            answer = evaluateExpressionMultipleVariables("answer.values", variables);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Object evaluateExpressionMultipleVariables(String javaScriptCode, List<Variable> variables) throws IllegalAccessException,
            InvocationTargetException, InstantiationException {

        // Every Rhino VM begins with the enter()
        // This Context is not Android's Context
        Context rhino = Context.enter();
        // Turn off optimization to make Rhino Android compatible
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();

            ScriptableObject.defineClass(scope, AnswerScriptableWrapper.class);

            for (Variable variable : variables) {

                String answerVarName = variable.variableName;
                AnswerPrimitives primitivesValue = variable.questionAnswer;

                Scriptable testCounter = rhino.newObject(scope, "AnswerWrapper",
                        new Object[]{primitivesValue});
                scope.put(answerVarName, scope, testCounter);
            }

            Object result = rhino.evaluateString(scope, javaScriptCode, "JavaScript", 1, null);
            System.out.println(result);

            return result;

        } finally {
            Context.exit();
        }

    }


}