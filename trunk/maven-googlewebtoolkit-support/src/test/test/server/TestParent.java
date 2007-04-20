package test.server;

import java.util.ArrayList;

/**
 * Author: willpugh  Apr 13, 2007 - 2:04:12 PM
 */
public class TestParent extends TestGrandParent {
  private int i = 10;
  private boolean b = true;
  private long l = 10;
  private float f = 10;
  private double d = 10;

  private ArrayList<foo>  foos = new ArrayList<foo>();
  private ArrayList<bar>  bars = new ArrayList<bar>();
  private ArrayList<foo.baz>  bazes = new ArrayList<foo.baz>();

  public TestParent() {
    getFoos().add(new  foo());
    getBars().add(new bar());
    getBazes().add(new foo.baz());
  }

  public ArrayList<foo> getFoos() {
    return foos;
  }

  public void setFoos(ArrayList<foo> foos) {
    this.foos = foos;
  }

  public ArrayList<bar> getBars() {
    return bars;
  }

  public void setBars(ArrayList<bar> bars) {
    this.bars = bars;
  }

  public ArrayList<foo.baz> getBazes() {
    return bazes;
  }

  public void setBazes(ArrayList<foo.baz> bazes) {
    this.bazes = bazes;
  }


  static public class foo {
    private int i = 10;
    private boolean b = true;
    private long l = 10;
    private float f = 10;
    private double d = 10;

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }

    public boolean isB() {
      return b;
    }

    public void setB(boolean b) {
      this.b = b;
    }

    public long getL() {
      return l;
    }

    public void setL(long l) {
      this.l = l;
    }

    public float getF() {
      return f;
    }

    public void setF(float f) {
      this.f = f;
    }

    public double getD() {
      return d;
    }

    public void setD(double d) {
      this.d = d;
    }

    public boolean equals(Object object) {
      TestParent t = (TestParent) object;

      return i == t.i && b == t.b && l == t.l && f == t.f && d == t.d && super.equals(t);
    }

    static public class baz {
      private int i = 10;
      private boolean b = true;
      private long l = 10;
      private float f = 10;
      private double d = 10;

      public int getI() {
        return i;
      }

      public void setI(int i) {
        this.i = i;
      }

      public boolean isB() {
        return b;
      }

      public void setB(boolean b) {
        this.b = b;
      }

      public long getL() {
        return l;
      }

      public void setL(long l) {
        this.l = l;
      }

      public float getF() {
        return f;
      }

      public void setF(float f) {
        this.f = f;
      }

      public double getD() {
        return d;
      }

      public void setD(double d) {
        this.d = d;
      }

      public boolean equals(Object object) {
        TestParent t = (TestParent) object;

        return i == t.i && b == t.b && l == t.l && f == t.f && d == t.d && super.equals(t);
      }
    }
  }

  static public class bar {
    private int i = 10;
    private boolean b = true;
    private long l = 10;
    private float f = 10;
    private double d = 10;

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }

    public boolean isB() {
      return b;
    }

    public void setB(boolean b) {
      this.b = b;
    }

    public long getL() {
      return l;
    }

    public void setL(long l) {
      this.l = l;
    }

    public float getF() {
      return f;
    }

    public void setF(float f) {
      this.f = f;
    }

    public double getD() {
      return d;
    }

    public void setD(double d) {
      this.d = d;
    }

    public boolean equals(Object object) {
      TestParent t = (TestParent) object;

      return i == t.i && b == t.b && l == t.l && f == t.f && d == t.d && super.equals(t);
    }
  }

  public int getI() {
    return i;
  }

  public void setI(int i) {
    this.i = i;
  }

  public boolean isB() {
    return b;
  }

  public void setB(boolean b) {
    this.b = b;
  }

  public long getL() {
    return l;
  }

  public void setL(long l) {
    this.l = l;
  }

  public float getF() {
    return f;
  }

  public void setF(float f) {
    this.f = f;
  }

  public double getD() {
    return d;
  }

  public void setD(double d) {
    this.d = d;
  }


  public boolean equals(Object object) {
    TestParent t = (TestParent) object;

    return i == t.i && b == t.b && l == t.l && f == t.f && d == t.d && super.equals(t);
  }
}
