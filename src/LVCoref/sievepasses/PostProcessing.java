package LVCoref.sievepasses;

public class PostProcessing extends DeterministicCorefSieve {
  public PostProcessing() {
    super();
    flags.REMOVE_NESTED_MENTIONS = true;
    flags.REMOVE_SINGLETONS = false;
    
    
  }
}
