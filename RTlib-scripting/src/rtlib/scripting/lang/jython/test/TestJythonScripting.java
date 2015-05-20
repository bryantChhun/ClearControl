package rtlib.scripting.lang.jython.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;
import org.python.core.Options;
import org.python.core.PyInteger;
import org.python.util.PythonInterpreter;

import rtlib.scripting.engine.ScriptingEngine;
import rtlib.scripting.engine.ScriptingEngineListener;
import rtlib.scripting.lang.groovy.GroovyUtils;
import rtlib.scripting.lang.jython.JythonScripting;
import rtlib.scripting.lang.jython.JythonUtils;

public class TestJythonScripting
{

	long cNumberIterations = 100000;

	@Test
	public void testJythonInterpreter() throws IOException
	{
		Options.importSite = false;
		final PythonInterpreter lPythonInterpreter = new PythonInterpreter();
		lPythonInterpreter.set("integer", new PyInteger(42));
		lPythonInterpreter.exec("square = integer*integer");
		final PyInteger square = (PyInteger) lPythonInterpreter.get("square");
		System.out.println("square: " + square.asInt());
		assertEquals(1764, square.asInt());
		lPythonInterpreter.close();
	}

	@Test
	public void testJythonUtils() throws IOException
	{
		final Double x = new Double(1);
		final Double y = new Double(2);

		final LinkedHashMap<String, Object> lMap = new LinkedHashMap<String, Object>();
		lMap.put("x", x);
		lMap.put("y", y);

		JythonUtils.runScript("Test", "x=y", lMap, null, false);

		assertEquals(lMap.get("x"), lMap.get("y"));
	}

	@Test
	public void testJythonScriptingWithScriptEngine()	throws IOException,
																										ExecutionException
	{
		final Double x = new Double(1);
		final Double y = new Double(2);
		
		final JythonScripting lJythonScripting = new JythonScripting();
		
		final ScriptingEngine lScriptingEngine = new ScriptingEngine(lJythonScripting,
																												null);
		
		lScriptingEngine.set("x", x);
		lScriptingEngine.set("y", y);
		lScriptingEngine.setScript("x=y");
		
		lScriptingEngine.addListener(new ScriptingEngineListener()
		{

			@Override
			public void updatedScript(ScriptingEngine pScriptingEngine,
																String pScript)
			{
			}

			@Override
			public void beforeScriptExecution(ScriptingEngine pScriptingEngine,
																				String pScriptString)
			{
				System.out.println("before");
			}

			@Override
			public void afterScriptExecution(	ScriptingEngine pScriptingEngine,
																				String pScriptString)
			{
				System.out.println("after");
			}

			@Override
			public void asynchronousResult(	ScriptingEngine pScriptingEngine,
																			String pScriptString,
																			Map<String, Object> pBinding,
																			Throwable pThrowable,
																			String pErrorMessage)
			{
				System.out.println(pBinding);

			}
		});

		lScriptingEngine.executeScriptAsynchronously();
		
		assertTrue(lScriptingEngine.waitForCompletion(1, TimeUnit.SECONDS));
		assertEquals(lScriptingEngine.get("x"), lScriptingEngine.get("y"));

	}

	@Test
	public void testPerformance() throws IOException
	{
		for (int i = 0; i < 100; i++)
			runTest();
	}

	private void runTest() throws IOException
	{
		final StopWatch lStopWatch = new StopWatch();
		lStopWatch.start();
		GroovyUtils.runScript("TestIndy",
													"double[] array = new double[1000]; for(int i=0; i<" + cNumberIterations
															+ "; i++) array[i%1000]+=1+array[(i+1)%1000] ",
													(Map<String, Object>) null,
													null,
													false);
		lStopWatch.stop();
		System.out.println("script:" + lStopWatch.getTime());

		lStopWatch.reset();
		lStopWatch.start();
		final double[] array = new double[1000];
		testMethod(array);
		lStopWatch.stop();
		System.out.println("native:" + lStopWatch.getTime());
	}

	private void testMethod(final double[] array)
	{
		for (int i = 0; i < cNumberIterations; i++)
			array[i % 1000] += 1 + array[(i + 1) % 1000];
	}
}