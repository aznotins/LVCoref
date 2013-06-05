package LVCoref.sievepasses;

public class NaiveMatch extends DeterministicCorefSieve {
  public NaiveMatch() {
    super();
    flags.USE_NAIVE_MATCH = true;
  }
}
