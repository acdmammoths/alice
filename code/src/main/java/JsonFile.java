/*
 * Copyright (C) 2022 Alexander Lee and Matteo Riondato
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

/** A helper class to read and write JSON from and to disk. */
class JsonFile {
  static JSONObject read(String inPath) {
    JSONObject json = null;
    try {
      final File file = new File(inPath);
      final String content = FileUtils.readFileToString(file, "utf-8");
      json = new JSONObject(content);
    } catch (IOException e) {
      System.err.println("Error reading json file " + inPath);
      e.printStackTrace();
      System.exit(1);
    }
    return json;
  }

  static void write(JSONObject json, String outPath) {
    try {
      final BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
      bw.write(json.toString(2));
      bw.newLine();
      bw.close();
    } catch (IOException e) {
      System.err.println("Error writing json file " + outPath);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
