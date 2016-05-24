package org.geoc.questionaire.tests;

import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.mozilla.javascript.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by lrodriguez2002cu on 04/05/2016.
 */
public class AnswerScriptableWrapperTest extends TestCase {


    public class DateWrapFactory extends WrapFactory {

       public Object wrap(Context cx, Scriptable scope, Object obj, Class<?>
               staticType) {


           if (obj instanceof Date) {
               // Construct a JS date object
               long time = ((Date) obj).getTime();
               return cx.newObject(scope, "Date", new Object[] {time});
           }


           return super.wrap(cx, scope, obj, staticType);
       }

    }


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

    AnswerPrimitives valueWithDateNow = new AnswerPrimitives() {

        List<Object> objects = new ArrayList<Object>();
        {

        }

        public Object getValue() {
            return new Date();
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
    

    AnswerPrimitives valueWithDatesEqual = new AnswerPrimitives() {

        DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                .appendDayOfMonth(2)
                .appendLiteral('-')
                .appendMonthOfYearShortText()
                .appendLiteral('-')
                .appendTwoDigitYear(1956)  // pivot = 1956
                .toFormatter();

        List<Object> objects = new ArrayList<Object>();
        {

        }

        public Object getValue() {
                DateTime dt = new DateTime("2016-04-20T21:39:45.618+02:00");
                System.out.println("This is the date tested: " + fmt.print(dt));
                return dt.toDate();

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
            System.out.println("count = " + count + " in testExpression()");

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


    public void testExpressionEvaluationValueWithIndex() throws IllegalAccessException, InvocationTargetException, InstantiationException {

        String answerVarName = "answer";

        //some expressions on the answer
        Object answer = null;
        answer = evaluateExpression("answer.getValue(1).length", answerVarName, (AnswerPrimitives) valueWithThreeAnswerItems);
        assertTrue(answer.equals(((AnswerPrimitives) valueWithThreeAnswerItems).getValues().get(1).toString().length()));

    }

    public Object evaluateExpression(String javaScriptCode /*= "answer.value";*/, String answerVarName,
                                     AnswerPrimitives primitives) throws IllegalAccessException,
            InvocationTargetException, InstantiationException {


        // Every Rhino VM begins with the enter()
        // This Context is not Android's Context
        Context rhino = Context.enter();
        DateWrapFactory wrapFactory = new DateWrapFactory();
        //wrapFactory.setJavaPrimitiveWrap(false);

        rhino.setWrapFactory(wrapFactory);
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
    

    public void testDates() throws IllegalAccessException, InstantiationException, InvocationTargetException {

        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDateNow));

        //is now less than 03 - april-2019?
        Object answer = evaluateExpressionMultipleVariables("answer.value.getTime() < new Date('03/03/2029').getTime()", variables);
        assertTrue(answer.equals(true));

    }

    public void testBasic() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDatesEqual));
        Object answer = evaluateExpressionMultipleVariables("1 == 1", variables);
        assertTrue(answer.equals(true));

    }

    public void testBasic1() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDatesEqual));
        //variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("1 == 2", variables);

/*        getDate()	Returns the day of the month (from 1-31)
        getDay()	Returns the day of the week (from 0-6)
        getFullYear()*/
        assertTrue(answer.equals(false));

    }

    public void testBasicDate() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDatesEqual));
        //variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("new Date(2016, 04, 20).valueOf() == new Date(2016, 04, 20).valueOf()", variables);

/*        getDate()	Returns the day of the month (from 1-31)
        getDay()	Returns the day of the week (from 0-6)
        getFullYear()*/
        assertTrue(answer.equals(true));

    }

    public void testBasicDateFields() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDatesEqual));
        //variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("new Date(2016, 04, 20).getTime() == new Date(2016, 04, 20).getTime()", variables);

/*        getDate()	Returns the day of the month (from 1-31)
        getDay()	Returns the day of the week (from 0-6)
        getFullYear()*/
        assertTrue(answer.equals(true));

    }

    public void testBasicDateFields1() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDatesEqual));
        //variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("new Date(2016, 04, 20).getDate() == new Date(2016, 04, 20).getDate()", variables);

        assertTrue(answer.equals(true));

        answer = evaluateExpressionMultipleVariables("new Date(2016, 04, 20).getDay() == new Date(2016, 04, 20).getDay()", variables);

        assertTrue(answer.equals(true));

        answer = evaluateExpressionMultipleVariables("new Date(2016, 04, 20).getFullYear() == new Date(2016, 04, 20).getFullYear()", variables);

        assertTrue(answer.equals(true));

        answer = evaluateExpressionMultipleVariables("new Date(2017, 04, 20).getFullYear() > new Date(2016, 04, 20).getFullYear()", variables);

        assertTrue(answer.equals(true));

        answer = evaluateExpressionMultipleVariables("new Date(2017, 04, 20).getFullYear() < new Date(2016, 04, 20).getFullYear()", variables);

        assertTrue(answer.equals(false));

    }


    public void testDatesEqual() throws IllegalAccessException, InstantiationException, InvocationTargetException {

        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDatesEqual));
        //variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("answer.value.getFullYear() === new Date(2016, 03, 20).getFullYear()", variables);
        assertTrue(answer.equals(true));

        //in js getDate() gives the day of the month
        answer = evaluateExpressionMultipleVariables("answer.value.getDate() === new Date(2016, 03, 20).getDate()", variables);
        assertTrue(answer.equals(true));

        //In java, using joda time the month starts in 1-Jan, 2-Feb, and so on, while in js start in 0-jan
        //check the next fiddle for testing the js behavior https://jsfiddle.net/xv3kznc4/
        answer = evaluateExpressionMultipleVariables("answer.value.getDay() === new Date(2016, 03, 20).getDay()", variables);
        assertTrue(answer.equals(true));

    }
    
    public void testDates1() throws IllegalAccessException, InstantiationException, InvocationTargetException {

        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDateNow));
        //variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("answer.value < new Date('03/03/2012')", variables);
        assertTrue(answer.equals(false));

    }
    
    public void testDates2() throws IllegalAccessException, InstantiationException, InvocationTargetException {

        List<Variable> variables = new ArrayList<Variable>();
        variables.add(new Variable("answer", valueWithDateNow));
        //variables.add(new Variable("answer1", valueWithFourAnswerItems));

        Object answer = evaluateExpressionMultipleVariables("answer.value.getTime() < new Date('03/03/2019').getTime()", variables);
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
            //TODO: investigate how to deal with exceptions, at this moment is expected to see some exceptions in the log
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
        DateWrapFactory wrapFactory = new DateWrapFactory();
        rhino.setWrapFactory(wrapFactory);
        //wrapFactory.setJavaPrimitiveWrap(false);

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