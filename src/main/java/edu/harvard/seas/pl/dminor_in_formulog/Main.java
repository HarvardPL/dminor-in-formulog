package edu.harvard.seas.pl.dminor_in_formulog;

/*-
 * #%L
 * Formulog
 * %%
 * Copyright (C) 2018 - 2019 President and Fellows of Harvard College
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
		if (args.length != 1) {
			throw new IllegalArgumentException("Expected a single argument (the Dminor file to process).");
		}
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
