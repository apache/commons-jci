package org.apache.commons.jci.compilers;


public interface JavaSources {
    
    String simple =
        "package jci;\n"
        + "public class Simple { \n"
        + "  public String toString() { \n"
        + "    return \"Simple\"; \n"
        + "  } \n"
        + "} \n";

    String SIMPLE = "package jci;\n"
        + "public class Simple { \n"
        + "  public String toString() { \n"
        + "    return \"SIMPLE\"; \n"
        + "  } \n"
        + "} \n";
    
    String extended =
        "package jci;\n"
        + "public class Extended extends Simple { \n"
        + "  public String toString() { \n"
        + "    return \"Extended:\" + super.toString(); \n"
        + "  } \n"
        + "} \n";

    String warning1 =
        "package jci;\n"
        + "public class Simple { \n"
        + "  public int generateWarning() { \n"
        + "    return new java.util.Date().getHours(); \n"
        + "  }\n"
        + "  public String toString() { \n"
        + "    return \"Simple\"; \n"
        + "  } \n"
        + "} \n";

    String warning2 =
        "package jci;\n"
        + "public class Simple { \n"
        + "  public static void generate() { \n"
        + "  }\n"
        + "  public static void generate2() { \n"
        + "    generate();\n"
        + "  }\n"
        + "  public String toString() { \n"
        + "    return \"Simple\"; \n"
        + "  } \n"
        + "} \n";

    String error =
        "package jci;\n"
        + "public class Simple { \n"
        + "  public String toString() { \n"
        + "    return 1; \n"
        + "  } \n"
        + "} \n";
}
