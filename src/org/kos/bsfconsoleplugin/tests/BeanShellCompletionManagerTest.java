package org.kos.bsfconsoleplugin.tests;

import bsh.Interpreter;
import org.kos.bsfconsoleplugin.languages.BeanShellCompletionManager;

import java.util.Arrays;

/**
 * A test for {@link BeanShellCompletionManager}.
 * 
 * @author <a href="mailto:konstantin.sobolev@gmail.com" title="">Konstantin Sobolev</a>
 */
public class BeanShellCompletionManagerTest extends AbstractCompletionManagerTest {
	@SuppressWarnings({"FieldCanBeLocal"})
	private Interpreter interpreter;

	public BeanShellCompletionManagerTest(final String s) throws Exception {
		super(s);

		interpreter = new Interpreter();
		completionManager = new BeanShellCompletionManager(interpreter);
	}

	public void testBeanShellCompletionManager() {
		checkCompletion("Intege", "r");
		checkCompletion("new Intege", "r");
		checkCompletionExists("new Integer(1).toS", "tring(");
		checkCompletionExists("System.out.println(new Integer(1).toS", "tring(");
		checkCompletionExists("\"1\" + new Integer(1).toS", "tring(");
		checkCompletionExists("1 + new Integer(1).intVal", "ue(");
		checkCompletionExists("print", "Banner");

		checkCompletionDoesntExist("new Integer(1).to","OctalString(");
		checkCompletionExists("Integer.to","OctalString(");
	}

	private void checkCompletion(final String line, final String expected) {
		final String[] res = completionManager.complete(line);
		assertEquals(1, res.length);
		assertEquals(expected, res[0]);
	}

	private void checkCompletionExists(final String line, final String completion) {
		final String[] res = completionManager.complete(line);
		assertTrue("Expected to get " + completion + " among possible variants", Arrays.asList(res).contains(completion));
	}

	private void checkCompletionDoesntExist(final String line, final String completion) {
		final String[] res = completionManager.complete(line);
		assertTrue("Expected NOT to get " + completion + " among possible variants", !Arrays.asList(res).contains(completion));
	}
}