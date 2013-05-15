package LVCoref.sievepasses;

public class ExactStringMatch extends DeterministicCorefSieve {
  public ExactStringMatch() {
    super();
    flags.USE_EXACTSTRINGMATCH = true;
  }
}
