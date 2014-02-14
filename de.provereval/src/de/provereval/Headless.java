package de.provereval;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.*;

public class Headless implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] applicationArgs = Platform.getApplicationArgs();

		// the one and only argument should be the csv file to export to
		if (applicationArgs.length != 1 || !applicationArgs[0].endsWith(".csv")) {
			System.out.println("Wrong number of arguments / wrong usage!");
			System.out.println("You need to supply the path to a CSV file.");
			return null;
		}

		new EvalCommand(true).execute(null);

		return null;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
