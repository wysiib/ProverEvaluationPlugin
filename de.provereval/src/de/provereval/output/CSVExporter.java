package de.provereval.output;

import java.io.*;
import java.util.*;

import de.provereval.ProverEvaluationTask;

public class CSVExporter {
	public static void exportToCSVFile(
			Map<String, List<ProverEvaluationTask>> grouped, String path) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path)));

			// get a list of solvers
			List<String> reasoners = new ArrayList<String>();

			Collection<List<ProverEvaluationTask>> values = grouped.values();
			List<ProverEvaluationTask> first = values.iterator().next();
			for (ProverEvaluationTask task : first) {
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
			writer.write("\n");

			List<String> keys = new ArrayList<String>(grouped.keySet());
			Collections.sort(keys);

			for (String key : keys) {
				writer.write(key.replace("\n", "") + ",");

				for (int i = 0; i < reasoners.size(); i++) {
					ProverEvaluationTask task = getTask(reasoners.get(i),
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
				writer.write("\n");
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

	private static ProverEvaluationTask getTask(String reasoner,
			List<ProverEvaluationTask> list) {
		for (ProverEvaluationTask t : list) {
			if (t.getProverName().equals(reasoner)) {
				return t;
			}
		}
		return null;
	}
}
