/*******************************************************************************
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

import java.io.*;
import java.util.*;

public class UnicodeChecker {
  public static void main(String[] args) throws Exception {
    Hashtable<String, String> blockTable = new Hashtable<String, String>();
    blockTable.put("CJK Ideograph Extension A","CJK UNIFIED IDEOGRAPHS EXTENSION A");
    blockTable.put("CJK Ideograph","CJK UNIFIED IDEOGRAPHS");
    blockTable.put("Hangul Syllable","HANGUL SYLLABLES");
    blockTable.put("Non Private Use High Surrogate","HIGH SURROGATES");
    blockTable.put("Private Use High Surrogate","HIGH PRIVATE USE SURROGATES");
    blockTable.put("Low Surrogate","LOW SURROGATES");
    blockTable.put("Private Use","PRIVATE USE AREA");
    blockTable.put("Tangut Ideograph", "TANGUT");
    blockTable.put("Tangut Ideograph Supplement", "TANGUT SUPPLEMENT");
    blockTable.put("CJK Ideograph Extension B","CJK UNIFIED IDEOGRAPHS EXTENSION B");
    blockTable.put("CJK Ideograph Extension C","CJK UNIFIED IDEOGRAPHS EXTENSION C");
    blockTable.put("CJK Ideograph Extension D","CJK UNIFIED IDEOGRAPHS EXTENSION D");
    blockTable.put("CJK Ideograph Extension E","CJK UNIFIED IDEOGRAPHS EXTENSION E");
    blockTable.put("CJK Ideograph Extension F","CJK UNIFIED IDEOGRAPHS EXTENSION F");
    blockTable.put("CJK Ideograph Extension G","CJK UNIFIED IDEOGRAPHS EXTENSION G");
    blockTable.put("CJK Ideograph Extension H","CJK UNIFIED IDEOGRAPHS EXTENSION H");
    blockTable.put("CJK Ideograph Extension I","CJK UNIFIED IDEOGRAPHS EXTENSION I");
    blockTable.put("Plane 15 Private Use","SUPPLEMENTARY PRIVATE USE AREA A");
    blockTable.put("Plane 16 Private Use","SUPPLEMENTARY PRIVATE USE AREA B");

    TreeSet<Integer> codePointTable = new TreeSet<Integer>();
    for (int codePoint = 0;
      codePoint <= Character.MAX_CODE_POINT;codePoint++){
      if (Character.isDefined(codePoint)){
        codePointTable.add(codePoint);
      }
    }

    String[] vals;
    if (args.length > 0) {
      vals = args;
    } else {
      vals = UnicodeVers.getFiles("UnicodeData");
    }

    for(String filename : vals) {
      File f = new File(filename);
      BufferedReader reader = new BufferedReader(new FileReader(f));
      System.out.println("Checking "+f.getName());
      String s;
      String name = null;
      int firstCode = 0;
      String cName = null;
      int cnt = 0;
      while((s = reader.readLine()) != null){
        String[] ss = s.split(";");
        int code = Integer.parseInt(ss[0],16);
        String uName = ss[1];
        if ("<control>".equals(uName)){
            // special cases, ref JDK-7071819
            if (code == 0x7){
                uName = "BEL";
            }else if (code == 0x80){
                uName = "PADDING CHARACTER";
            }else if (code == 0x81){
                uName = "HIGH OCTET PRESET";
            }else if (code == 0x99){
                uName = "SINGLE GRAPHIC CHARACTER INTRODUCER";
            }else 
            // End of special cases
            if (ss.length >= 11 && 
                ss[10].length() > 0){
                uName = ss[10];
            }else{
                uName = String.format("LATIN 1 SUPPLEMENT %X",code);
            }
        }
        if (uName.toUpperCase(Locale.ROOT).lastIndexOf(", LAST>") != -1) {
          String h0 = String.format("%X", code);
          for(int i=firstCode; i<=code; i++) {
            String sName = String.format("%s %X", name, i);
            String h1 = String.format("%X", i);
            cName = Character.getName(i);
            if (!sName.equalsIgnoreCase(cName))
              System.err.printf("%X; [%s]<->[%s]%n", i, sName, cName);
            if (Character.isDefined(i)) codePointTable.remove(i);
            cnt++;
          }
          name = null;
          firstCode = 0;
        } else if (uName.toUpperCase(Locale.ROOT).lastIndexOf(", FIRST>") != -1) {
          name = uName.substring(1, uName.length()-8);
          if (blockTable.containsKey(name))
            name = blockTable.get(name);
          firstCode = code;
        } else {
          cName = Character.getName(code);
          if (!uName.equalsIgnoreCase(cName))
            System.err.printf("%X; [%s]<->[%s]%n", code, uName, cName);
          if (Character.isDefined(code)) codePointTable.remove(code);
          cnt++;
        }
      }
      reader.close();
      System.out.println("  Checked "+cnt+" characters");
    }
    if (codePointTable.size() > 0) {
      System.err.printf("%d characters were not checked%n", codePointTable.size());
    }
  }
}
