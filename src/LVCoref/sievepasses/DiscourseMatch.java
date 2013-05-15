package LVCoref.sievepasses;

import LVCoref.sievepasses.DeterministicCorefSieve;

public class DiscourseMatch extends DeterministicCorefSieve {
  public DiscourseMatch() {
    super();
    flags.USE_DISCOURSEMATCH = true;
  }
}
