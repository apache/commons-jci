package org.vafer;

import org.apache.javaflow.Main;

public class Test implements Main {
  public void main() {
      MyInterface i = new MyObject();
      i.my("");
      System.out.println("b");
  }
}
