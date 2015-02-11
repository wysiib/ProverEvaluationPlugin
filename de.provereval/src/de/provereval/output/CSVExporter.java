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

public class CSVExporter {
	public static void exportToCSVFile(
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

			// header of csv file
			writer.write("Sequent,");
			for (int i = 0; i < reasoners.size(); i++) {
				writer.write(reasoners.get(i));
				writer.write(",");
				writer.write(reasoners.get(i) + "_Time");
				if (i + 1 < reasoners.size()) {
					writer.write(",");
				}
			}
			writer.newLine();

			List<String> keys = new ArrayList<String>(grouped.keySet());
			Collections.sort(keys);

			for (String key : keys) {
				writer.write(key.replace("\n", "") + ",");

				for (int i = 0; i < reasoners.size(); i++) {
					ProverEvaluationResult task = getResult(reasoners.get(i),
							grouped.get(key));
					if (task.isProven()) {
						writer.write("1");
					} else {
						writer.write("0");
					}
					writer.write(",");
					writer.write(Long.toString(task.getTook()));

					if (i + 1 < reasoners.size()) {
						writer.write(",");
					}
				}
				writer.newLine();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}

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
