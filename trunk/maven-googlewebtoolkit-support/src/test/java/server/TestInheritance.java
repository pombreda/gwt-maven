package server;

import java.util.Arrays;

/**
 * Author: willpugh  Apr 13, 2007 - 2:03:43 PM
 */
public class TestInheritance extends TestParent {
  private String[][][][]  manyStrings;
  private BBean[][][][]   manyBeans;


  public TestInheritance() {
    this.setManyStrings(new String[3][][][]);
    this.setManyBeans(new BBean[3][][][]);

    for (int i = 0; i<2; i++) {
      getManyStrings()[i] = new String[4][][];
      getManyBeans()[i] = new BBean[4][][];

      for (int j = 0; j<3; j++) {
        getManyStrings()[i][j] = new String[3][];
        getManyBeans()[i][j] = new BBean[3][];

        for (int k=0; k<2; k++) {
          getManyStrings()[i][j][k] = new String[] { "Hello", "There", "Kitty", null };
          getManyBeans()[i][j][k] = new BBean[] { new BBean(), new BBean(), null };
        }

      }
    }

  }

  public String[][][][] getManyStrings() {
    return manyStrings;
  }

  public void setManyStrings(String[][][][] manyStrings) {
    this.manyStrings = manyStrings;
  }

  public BBean[][][][] getManyBeans() {
    return manyBeans;
  }

  public void setManyBeans(BBean[][][][] manyBeans) {
    this.manyBeans = manyBeans;
  }


  public boolean equals(Object object) {
    TestInheritance t = (TestInheritance) object;
    return Arrays.deepEquals(manyBeans, t.manyBeans) && Arrays.deepEquals(manyStrings, t.manyStrings) && super.equals(t);
  }
}
