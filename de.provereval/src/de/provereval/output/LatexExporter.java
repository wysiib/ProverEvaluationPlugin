package de.provereval.output;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.provereval.ProverEvaluationResult;

public class LatexExporter {
	public static void exportToLatexFile(
			Map<String, List<ProverEvaluationResult>> grouped, String path) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path)));

			// get a list of solvers
			List<String> reasoners = new ArrayList<String>();

			Collection<List<ProverEvaluationResult>> values = grouped.values();
			List<ProverEvaluationResult> first = values.iterator().next();
			for (ProverEvaluationResult task : first) {
				reasoners.add(task.getProverName());
			}

			// header of latex file
			writer.write("\\begin{table}[h]");
			writer.newLine();

			writer.write("\\begin{center}");
			writer.newLine();

			writer.write("\\begin{longtable}{|l|");
			for (int i = 0; i < reasoners.size(); i++) {
				writer.write("c|");
			}
			writer.write("}");
			writer.newLine();

			writer.write("Sequent & ");
			for (int i = 0; i < reasoners.size(); i++) {
				writer.write(escape(reasoners.get(i)));
				if (i + 1 < reasoners.size()) {
					writer.write(" & ");
				}
			}
			writer.write("\\\\ \\hline");
			writer.newLine();

			String curMachine = "";

			List<String> keys = new ArrayList<String>(grouped.keySet());
			Collections.sort(keys);

			for (String key : keys) {
				String machine = key.substring(0, key.indexOf(':'));
				if (!machine.equals(curMachine)) {
					curMachine = machine;
					writer.write("\\multicolumn{" + (reasoners.size() + 1)
							+ "}{|c|}{");
					writer.write(escape(curMachine) + "} \\\\ \\hline");
					writer.newLine();
				}
				writer.write(escape(key.replace("\n", "").replace(
						curMachine + ":", "")
						+ " & "));

				for (int i = 0; i < reasoners.size(); i++) {
					ProverEvaluationResult task = getResult(reasoners.get(i),
							grouped.get(key));

					if (task.isProven()) {
						writer.write("\\checkmark");
					}

					if (i + 1 < reasoners.size()) {
						writer.write(" & ");
					}
				}
				writer.write(" \\\\ \\hline");
				writer.newLine();

			}

			writer.write("\\end{longtable}");
			writer.newLine();

			writer.write("\\end{center}");
			writer.newLine();

			writer.write("\\end{table}");
			writer.newLine();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}

	}

	private static String escape(String input) {
		String output = input.replace("_", "\\_");
		return output.replace("$", "\\$");
	}

	private static ProverEvaluationResult getResult(String reasoner,
			List<ProverEvaluationResult> list) {
		for (ProverEvaluationResult t : list) {
			if (t.getProverName().equals(reasoner)) {
				return t;
			}
		}
		return null;
	}
}
