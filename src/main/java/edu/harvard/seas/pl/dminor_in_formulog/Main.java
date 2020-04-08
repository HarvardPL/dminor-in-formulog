package edu.harvard.seas.pl.dminor_in_formulog;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
	private static final OpenOption[] opts = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
	private static final Map<Relation, Writer> writers = new HashMap<>();

	private static void openWriters(Path outDir) throws IOException {
		for (Relation rel : Relation.values()) {
			Path out = outDir.resolve(rel + ".csv");
			writers.put(rel, Files.newBufferedWriter(out, opts));
		}
	}

	private static void closeWriters() throws IOException {
		for (Writer writer : writers.values()) {
			writer.close();
		}
	}

	private static void dump(Module mod) throws IOException {
		Map<Relation, List<String[]>> db = Relationizer.relationize(mod);
		for (Map.Entry<Relation, List<String[]>> e : db.entrySet()) {
			Writer writer = writers.get(e.getKey());
			for (String[] ss : e.getValue()) {
				for (int i = 0; i < ss.length; ++i) {
					writer.write(ss[i]);
					if (i < ss.length - 1) {
						writer.write('\t');
					}
				}
				writer.write('\n');
			}
		}
	}

	public synchronized static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expected a single argument (the Dminor file to process).");
		}
		String file = args[0];
		Path outDir = Paths.get(file + "_facts");
		Files.createDirectories(outDir);
		openWriters(outDir);
		for (Module mod : Extractor.extract(new FileReader(file))) {
			dump(mod);
		}
		closeWriters();
	}
}
