package org.kos.bsfconsoleplugin;

import java.util.ArrayList;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.awt.*;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;


/**
 * Base class for language interpreters.
 *
 * @author <a href="mailto:konstantin.sobolev@gmail.com" title="">Konstantin Sobolev</a>
 */
public abstract class AbstractInterpreter implements Interpreter {
	protected ArrayList<InputListener> inputListeners = new ArrayList<InputListener>(5);
	protected LineNumberReader in;
	protected Thread inputThread;
	protected final BSFConsolePlugin plugin;
	protected final Console console;

	protected AbstractInterpreter(final BSFConsolePlugin plugin, final Console console) {
		this.plugin = plugin;
		this.console = console;
		in = new LineNumberReader(console.getIn());
	}

	@Override
	public void addInputListener(final InputListener inputListener) {
		inputListeners.add(inputListener);
	}

	@Override
	public void removeInputListener(final InputListener inputListener) {
		inputListeners.remove(inputListener);
	}

	void fireCommandInputted(final String command) {
		final InputListener[] listeners = inputListeners.toArray(new InputListener[inputListeners.size()]);
		for (int i = 0; i < listeners.length; listeners[i++].commandInputted(command)) ;
	}

	void createInputThread(final String languageName) {
		inputThread = new Thread(
				new Runnable() {
					@Override
					public void run() {
						final String lineSep = System.getProperty("line.separator");
						try {
							while (!Thread.interrupted()) {
								final StringBuilder text = new StringBuilder();
								String line = AbstractInterpreter.this.in.readLine();
								while (line.length() == 0 || line.charAt(line.length() - 1) != Console.EOC) {
									text.append(line).append(lineSep);
									line = AbstractInterpreter.this.in.readLine();
								}
								text.append(line.substring(0, line.length() - 1));
								line = text.toString();
//						final String line = BSFInterpreter.this.in.readLine();
//						if (line == null) {
//							print("got null line");
//							try {
//								Thread.sleep(500);
//							} catch (InterruptedException e) {
//								break;
//							}
//							continue;
//						}
								if (line.length() > 0) {
									execInEDT(line);
									fireCommandInputted(line);
									printPrompt();
								}
							}
						} catch (IOException e) {
							error("I/O Exception: " + e);
						}
						terminateEngine();
					}
				}, "interpreter for " + languageName
		);

		inputThread.setDaemon(true);
	}

	protected void execInEDT(@NotNull final String line) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				exec(line);
			}
		});
	}

	abstract void terminateEngine();

	protected void printPrompt() {
		console.asyncWaitAndPrintPrompt();
	}

	protected void println(final String str, final Color color) {
		console.asyncWaitAndPrintln(str, color);
	}

	protected void error(final String str) {
		console.asyncWaitAndError(str);
	}

	protected void printException(final Throwable e) {
		final String err;
		final BSFConsoleConfig state = plugin.getState();
		if (state != null && state.isHideExceptionStacktraces())
			err = e.toString();
		else {
			final StringWriter sw = new StringWriter(256);
			final PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			err = sw.toString();
		}
		error(err);
	}

	@Override
	public void start() {
		inputThread.start();
		printPrompt();
	}

	@Override
	public void stop() {
		inputThread.interrupt();
	}
}
