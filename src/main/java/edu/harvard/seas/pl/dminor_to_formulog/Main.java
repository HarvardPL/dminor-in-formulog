package edu.harvard.seas.pl.dminor_to_formulog;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String file = args[0];
		Module mod = Extractor.extract(new FileReader(file));
		Map<String, List<String[]>> db = Relationizer.relationize(mod);
		Path outdir = Paths.get(file + "_facts");
		Files.createDirectories(outdir);	
		for (Map.Entry<String, List<String[]>> e : db.entrySet()) {
			Path out = outdir.resolve(e.getKey() + ".csv");
			try (BufferedWriter writer = Files.newBufferedWriter(out)) {
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
	}
}
