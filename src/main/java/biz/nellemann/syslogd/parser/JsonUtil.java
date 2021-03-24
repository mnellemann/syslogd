package biz.nellemann.syslogd.parser;

/*
    Code from https://gist.github.com/jjfiv/2ac5c081e088779f49aa, which is BSD licensed: http://lemurproject.org/galago-license
*/

public class JsonUtil {

  public static String encode(String input) {

    StringBuilder output = new StringBuilder();

    for(int i=0; i<input.length(); i++) {
      char ch = input.charAt(i);
      int chx = (int) ch;

      // let's not put any nulls in our strings
      if(chx == 0) {
          continue;
      }

      if(ch == '\n') {
        output.append("\\n");
      } else if(ch == '\t') {
        output.append("\\t");
      } else if(ch == '\r') {
        output.append("\\r");
      } else if(ch == '\\') {
        output.append("\\\\");
      } else if(ch == '"') {
        output.append("\\\"");
      } else if(ch == '\b') {
        output.append("\\b");
      } else if(ch == '\f') {
        output.append("\\f");
      } else if(chx > 127) {
        output.append(String.format("\\u%04x", chx));
      } else {
        output.append(ch);
      }
    }

    return output.toString();
  }


  public static String decode(String input) {

    StringBuilder builder = new StringBuilder();

    int i = 0;
    while (i < input.length()) {
      char delimiter = input.charAt(i); i++; // consume letter or backslash

      if(delimiter == '\\' && i < input.length()) {

        // consume first after backslash
        char ch = input.charAt(i); i++;

        if(ch == '\\' || ch == '/' || ch == '"' || ch == '\'') {
          builder.append(ch);
        }
        else if(ch == 'n') builder.append('\n');
        else if(ch == 'r') builder.append('\r');
        else if(ch == 't') builder.append('\t');
        else if(ch == 'b') builder.append('\b');
        else if(ch == 'f') builder.append('\f');
        else if(ch == 'u') {

          StringBuilder hex = new StringBuilder();

          // expect 4 digits
          if (i+4 > input.length()) {
            throw new RuntimeException("Not enough unicode digits! ");
          }
          for (char x : input.substring(i, i + 4).toCharArray()) {
            if(!Character.isLetterOrDigit(x)) {
              throw new RuntimeException("Bad character in unicode escape.");
            }
            hex.append(Character.toLowerCase(x));
          }
          i+=4; // consume those four digits.

          int code = Integer.parseInt(hex.toString(), 16);
          builder.append((char) code);
        } else {
          throw new RuntimeException("Illegal escape sequence: \\"+ch);
        }
      } else { // it's not a backslash, or it's the last character.
        builder.append(delimiter);
      }
    }

    return builder.toString();
  }

}
