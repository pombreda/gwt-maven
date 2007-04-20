package test.server;

/**
 * Author: willpugh  Apr 13, 2007 - 2:04:30 PM
 */
public class TestGrandParent {
  private String  Granny = "Granny";

  public String getGranny() {
    return Granny;
  }

  public void setGranny(String granny) {
    Granny = granny;
  }


  public boolean equals(Object object) {
    TestGrandParent t = (TestGrandParent) object;
    return t.Granny.equals(t.Granny); 
  }
}
