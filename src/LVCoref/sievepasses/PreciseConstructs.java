/*******************************************************************************
 * Copyright 2013,2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package LVCoref.sievepasses;

public class PreciseConstructs extends DeterministicCorefSieve {
  public PreciseConstructs() {
    super();
    flags.USE_APPOSITION = true;
    flags.USE_PREDICATENOMINATIVES = true;
    flags.USE_ACRONYM = true;
    //flags.USE_RELATIVEPRONOUN = true;
    //flags.USE_ROLEAPPOSITION = true;
    //flags.USE_DEMONYM = true;
  }
}
